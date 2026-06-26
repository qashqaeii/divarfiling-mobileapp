package ir.divarfiling.mobile.core.network

import ir.divarfiling.mobile.core.datastore.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenRefresher: TokenRefresher,
    private val sessionStore: SessionStore,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401 || responseCount(response) >= 2) {
            return null
        }
        val path = response.request.url.encodedPath
        if (path.contains("/auth/login") || path.contains("/auth/refresh")) {
            return null
        }
        if (!tokenRefresher.refreshSync()) {
            return null
        }
        val access = runBlocking { sessionStore.getAccessToken() } ?: return null
        return response.request.newBuilder()
            .header("Authorization", "Bearer $access")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
