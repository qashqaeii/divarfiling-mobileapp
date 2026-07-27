package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.network.AppVersionData
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.update.ApkInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class ApkDownloadProgress(
    val bytesRead: Long,
    val contentLength: Long,
) {
    val fraction: Float
        get() = if (contentLength > 0L) {
            (bytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

@Singleton
class AppUpdateRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
    private val apkInstaller: ApkInstaller,
    @Named("download") private val downloadClient: OkHttpClient,
) {
    suspend fun checkForUpdate(
        currentBuild: Int = BuildConfig.VERSION_CODE,
    ): ApiResult<AppVersionData> {
        return try {
            val response = api.getAppVersion(currentBuild)
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "بررسی نسخه ناموفق بود", response.code)
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه در بررسی نسخه")
        }
    }

    suspend fun downloadApk(
        url: String,
        filename: String,
        expectedSha256: String = "",
        onProgress: (ApkDownloadProgress) -> Unit = {},
    ): ApiResult<File> = withContext(Dispatchers.IO) {
        try {
            val target = apkInstaller.apkFile(filename.ifBlank { "divar-filing-update.apk" })
            if (target.exists()) target.delete()
            val partial = File(target.parentFile, "${target.name}.part")
            if (partial.exists()) partial.delete()

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.android.package-archive")
                .get()
                .build()
            downloadClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Error("دانلود ناموفق (${response.code})")
                }
                val body = response.body ?: return@withContext ApiResult.Error("پاسخ خالی از سرور")
                val contentLength = body.contentLength()
                val digest = if (expectedSha256.isNotBlank()) MessageDigest.getInstance("SHA-256") else null
                body.byteStream().use { input ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var readTotal = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            digest?.update(buffer, 0, read)
                            readTotal += read
                            onProgress(ApkDownloadProgress(readTotal, contentLength))
                        }
                        output.flush()
                    }
                }
                if (digest != null) {
                    val actual = digest.digest().joinToString("") { "%02x".format(it) }
                    if (!actual.equals(expectedSha256, ignoreCase = true)) {
                        partial.delete()
                        return@withContext ApiResult.Error("فایل دانلودشده معتبر نیست (مغایرت هش)")
                    }
                }
                if (!partial.renameTo(target)) {
                    partial.copyTo(target, overwrite = true)
                    partial.delete()
                }
                ApiResult.Success(target)
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطا در دانلود آپدیت")
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 64 * 1024
    }
}
