
package org.apz.gamevault
import android.content.Context
class SessionStore(context: Context) {
    private val p = context.getSharedPreferences("gv_session", Context.MODE_PRIVATE)
    var server: String? get() = p.getString("server", null)
        set(v) = p.edit().putString("server", v).apply()
    var access: String? get() = p.getString("access", null)
        set(v) = p.edit().putString("access", v).apply()
    var refresh: String? get() = p.getString("refresh", null)
        set(v) = p.edit().putString("refresh", v).apply()
    fun clear() = p.edit().clear().apply()
}
