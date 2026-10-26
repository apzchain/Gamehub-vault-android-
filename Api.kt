
package org.apz.gamevault

import com.google.gson.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class Api(private val session: SessionStore) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
        .build()
    private fun base() = (session.server ?: "").trim().removeSuffix("/")
    private fun request(path: String, method: String = "GET", body: RequestBody? = null): Request.Builder {
        val b = Request.Builder().url(base() + path).method(method, body)
            .header("Accept", "application/json")
        session.access?.let { b.header("Authorization", "Bearer $it") }
        return b
    }

    fun login(user: String, pass: String, cb: (Boolean, String) -> Unit) {
        val json = Gson().toJson(mapOf("username" to user, "password" to pass))
        val body = json.toRequestBody("application/json".toMediaType())
        Thread {
            try {
                val r = http.newCall(request("/api/auth/basic/login","POST",body).build()).execute()
                val text = r.body?.string().orEmpty()
                if (!r.isSuccessful) throw IOException("HTTP ${r.code}: $text")
                val o = JsonParser.parseString(text).asJsonObject
                session.access = o.get("access_token")?.asString
                session.refresh = o.get("refresh_token")?.asString
                cb(true, "OK")
            } catch (e: Exception) { cb(false, e.message ?: "Login failed") }
        }.start()
    }

    fun refreshToken(cb: (Boolean) -> Unit) {
        val token = session.refresh ?: return cb(false)
        val body = Gson().toJson(mapOf("refresh_token" to token))
            .toRequestBody("application/json".toMediaType())
        Thread {
            try {
                val r = http.newCall(request("/api/auth/refresh","POST",body).build()).execute()
                val o = JsonParser.parseString(r.body?.string().orEmpty()).asJsonObject
                session.access = o.get("access_token")?.asString
                session.refresh = o.get("refresh_token")?.asString
                cb(r.isSuccessful && session.access != null)
            } catch (_: Exception) { cb(false) }
        }.start()
    }

    fun games(query: String?, cb: (List<Game>, String?) -> Unit) {
        Thread {
            try {
                val path = if (query.isNullOrBlank()) "/api/games" else "/api/games?search=" + java.net.URLEncoder.encode(query, "UTF-8")
                val r = http.newCall(request(path).build()).execute()
                val text = r.body?.string().orEmpty()
                if (!r.isSuccessful) throw IOException("HTTP ${r.code}: $text")
                val root = JsonParser.parseString(text)
                val arr = when {
                    root.isJsonArray -> root.asJsonArray
                    root.isJsonObject && root.asJsonObject.has("games") -> root.asJsonObject["games"].asJsonArray
                    else -> JsonArray()
                }
                val list = arr.map { x ->
                    val o=x.asJsonObject
                    Game(
                        o.get("id")?.asString ?: o.get("game_id")?.asString ?: "",
                        o.get("title")?.asString ?: o.get("name")?.asString ?: "Unknown",
                        first(o,"cover","cover_url","image","image_url","thumbnail"),
                        first(o,"description","summary"),
                        first(o,"release_year","year"),
                        first(o,"size","file_size")
                    )
                }
                cb(list, null)
            } catch(e: Exception) { cb(emptyList(), e.message) }
        }.start()
    }

    fun download(gameId: String, cb:(Boolean,String)->Unit) {
        Thread {
            try {
                // The exact download endpoint can vary with server release/configuration.
                // We first use the documented REST API route family and report the server response.
                val r = http.newCall(request("/api/games/$gameId/download").build()).execute()
                val text=r.body?.string().orEmpty()
                cb(r.isSuccessful, if(r.isSuccessful) "Download started" else "HTTP ${r.code}: $text")
            } catch(e:Exception){ cb(false,e.message ?: "Download failed") }
        }.start()
    }

    private fun first(o: JsonObject, vararg keys: String): String? =
        keys.firstNotNullOfOrNull { k -> o.get(k)?.takeUnless { it.isJsonNull }?.asString }
}
