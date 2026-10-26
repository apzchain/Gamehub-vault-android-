
# GameVault Android 2.0

Unofficial Android companion for a self-hosted GameVault server.

Implemented:
- Real username/password JWT login via `POST /api/auth/basic/login`
- Bearer token on authenticated calls
- Refresh-token storage and API refresh method
- Library loading from `/api/games`
- Search parameter
- Cover/thumbnail loading
- Game metadata
- Download action hook
- Persian/English UI toggle
- Pull-to-refresh
- HTTPS-only application policy
- No hard-coded credentials

GameVault officially documents JWT authentication and the login/refresh endpoints.
It also documents a REST API and recommends using the server's OpenAPI spec at
`/api/docs-json` to generate clients. The exact game download endpoint can differ
with server versions, so the download method is isolated in `Api.kt` and should be
verified against the target server's OpenAPI document before production release.

This app is an unofficial third-party client and is not endorsed by Phalcode.

## Build
Open in Android Studio, sync Gradle, then:
Build -> Build APK(s)

Debug output:
app/build/outputs/apk/debug/app-debug.apk
