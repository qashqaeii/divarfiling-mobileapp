package ir.divarfiling.mobile.core.network

import ir.divarfiling.mobile.core.datastore.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = runBlocking { sessionStore.getAccessToken() }
        val deviceId = runBlocking { sessionStore.getDeviceId() }
        val path = request.url.encodedPath
        val skipAuth = path.endsWith("/auth/login") || path.endsWith("/auth/refresh")

        val builder = request.newBuilder()
            .header("Accept", "application/json")
            .header("X-Platform", "android")
            .header("X-App-Version", "1.0.0")

        if (!skipAuth && !token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        if (!deviceId.isNullOrBlank()) {
            builder.header("X-Device-Id", deviceId)
        }

        return chain.proceed(builder.build())
    }
}
