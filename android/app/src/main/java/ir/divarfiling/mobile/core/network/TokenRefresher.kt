package ir.divarfiling.mobile.core.network

import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.datastore.SessionStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenRefresher @Inject constructor(
    @Named("plain") private val plainClient: OkHttpClient,
    private val sessionStore: SessionStore,
    private val json: Json,
) {
    private val refreshLock = Any()

    fun refreshSync(): Boolean = synchronized(refreshLock) {
        val refreshToken = runBlocking { sessionStore.getRefreshToken() }
        if (refreshToken.isNullOrBlank()) {
            return false
        }

        val body = buildJsonObject { put("refresh", refreshToken) }
            .toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${BuildConfig.API_BASE_URL}auth/refresh")
            .post(body)
            .header("Accept", "application/json")
            .header("X-Platform", "android")
            .build()

        return try {
            plainClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 401) {
                        runBlocking { sessionStore.clear() }
                    }
                    return false
                }
                val raw = response.body?.string().orEmpty()
                val envelope = json.decodeFromString<ApiEnvelope>(raw)
                if (!envelope.ok) {
                    return false
                }
                val data = envelope.parseData<RefreshData>(json) ?: return false
                runBlocking {
                    sessionStore.updateTokens(data.access, data.refresh)
                    val ttlMs = (data.expiresIn ?: 900L) * 1000L
                    sessionStore.saveAccessExpiresAt(System.currentTimeMillis() + ttlMs)
                }
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
