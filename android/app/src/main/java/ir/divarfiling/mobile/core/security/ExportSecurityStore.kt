package ir.divarfiling.mobile.core.security

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportSecurityStore @Inject constructor() {
    private var token: String? = null
    private var expiresAtMillis: Long = 0L

    fun hasValidToken(): Boolean {
        val current = token
        return !current.isNullOrBlank() && System.currentTimeMillis() < expiresAtMillis
    }

    fun getToken(): String? = if (hasValidToken()) token else null

    fun saveToken(token: String, expiresInSeconds: Long) {
        this.token = token
        expiresAtMillis = System.currentTimeMillis() + (expiresInSeconds * 1000L)
    }

    fun clear() {
        token = null
        expiresAtMillis = 0L
    }
}
