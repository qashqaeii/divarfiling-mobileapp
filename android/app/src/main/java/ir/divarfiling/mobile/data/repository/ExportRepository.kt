package ir.divarfiling.mobile.data.repository

import android.content.Context
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.export.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ExportRepository @Inject constructor(
    @Named("mobile") private val client: OkHttpClient,
    private val sessionStore: SessionStore,
) {
    suspend fun downloadExport(
        context: Context,
        path: String,
        format: ExportFormat,
        queryParams: Map<String, String> = emptyMap(),
        defaultFileName: String,
    ): ApiResult<File> = withContext(Dispatchers.IO) {
        try {
            val base = BuildConfig.API_BASE_URL.trimEnd('/')
            val urlBuilder = "$base/$path".trimStart('/').let { "$base/$it" }
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?: return@withContext ApiResult.Error("آدرس نامعتبر")

            urlBuilder.addQueryParameter("format", format.apiValue)
            queryParams.forEach { (key, value) ->
                if (value.isNotBlank()) urlBuilder.addQueryParameter(key, value)
            }

            val token = sessionStore.getAccessToken()
            val deviceId = sessionStore.getDeviceId()
            val requestBuilder = Request.Builder()
                .url(urlBuilder.build())
                .get()
                .header("X-Platform", "android")
                .header("X-App-Version", BuildConfig.VERSION_NAME)

            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            if (!deviceId.isNullOrBlank()) {
                requestBuilder.header("X-Device-Id", deviceId)
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Error("خطا در دریافت فایل (${response.code})")
                }
                val body = response.body ?: return@withContext ApiResult.Error("پاسخ خالی")
                val fileName = response.header("Content-Disposition")
                    ?.substringAfter("filename=\"")
                    ?.substringBefore("\"")
                    ?.takeIf { it.isNotBlank() }
                    ?: defaultFileName
                val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                val file = File(dir, fileName)
                file.outputStream().use { out -> body.byteStream().copyTo(out) }
                ApiResult.Success(file)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای دانلود")
        }
    }

    suspend fun exportDataset(
        context: Context,
        datasetId: String,
        datasetName: String,
        format: ExportFormat,
    ): ApiResult<File> = downloadExport(
        context = context,
        path = "filing/datasets/$datasetId/export",
        format = format,
        defaultFileName = "${sanitizeFileName(datasetName)}.${format.extension}",
    )

    suspend fun exportContacts(
        context: Context,
        format: ExportFormat,
        query: String? = null,
        status: String? = null,
    ): ApiResult<File> {
        val params = buildMap {
            query?.takeIf { it.isNotBlank() }?.let { put("q", it) }
            status?.takeIf { it.isNotBlank() }?.let { put("status", it) }
        }
        return downloadExport(
            context = context,
            path = "crm/contacts/export",
            format = format,
            queryParams = params,
            defaultFileName = "crm_contacts.${format.extension}",
        )
    }

    suspend fun exportProperties(
        context: Context,
        format: ExportFormat,
        query: String? = null,
        dealMode: String? = null,
        propertyType: String? = null,
        transactionStatus: String? = null,
    ): ApiResult<File> {
        val params = buildMap {
            query?.takeIf { it.isNotBlank() }?.let { put("q", it) }
            dealMode?.takeIf { it.isNotBlank() }?.let { put("deal_mode", it) }
            propertyType?.takeIf { it.isNotBlank() }?.let { put("property_type", it) }
            transactionStatus?.takeIf { it.isNotBlank() }?.let { put("transaction_status", it) }
        }
        return downloadExport(
            context = context,
            path = "crm/properties/export",
            format = format,
            queryParams = params,
            defaultFileName = "crm_properties.${format.extension}",
        )
    }

    private fun sanitizeFileName(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|]"), "-").ifBlank { "export" }
}
