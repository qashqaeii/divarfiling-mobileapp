package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.ApiEnvelope
import ir.divarfiling.mobile.core.network.ApiResult
import ir.divarfiling.mobile.core.network.ContentStudioDraftPutRequest
import ir.divarfiling.mobile.core.network.ContentStudioDraftResponse
import ir.divarfiling.mobile.core.network.ContentStudioManifestData
import ir.divarfiling.mobile.core.network.ContentStudioSessionData
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentStudioRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    private val manifestCache = mutableMapOf<String, ContentStudioManifestData>()

    suspend fun loadSession(propertyId: Long): ApiResult<ContentStudioSessionData> {
        return try {
            val response = api.getContentStudioSession(propertyId)
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در بارگذاری استودیو", response.code)
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun loadManifest(
        propertyId: Long,
        templateId: String,
        format: String,
        expectedVersion: String?,
    ): ApiResult<ContentStudioManifestData> {
        val cacheKey = "$templateId|$format"
        if (!expectedVersion.isNullOrBlank()) {
            manifestCache[cacheKey]?.takeIf { it.manifestVersion == expectedVersion }?.let {
                return ApiResult.Success(it)
            }
        }
        return try {
            val response = api.getContentStudioManifest(propertyId, templateId, format)
            if (!response.ok) {
                if (response.code == "MANIFEST_VERSION_MISMATCH") {
                    manifestCache.remove(cacheKey)
                }
                return ApiResult.Error(response.error ?: "خطا در Manifest", response.code)
            }
            val data = response.requireData<ContentStudioManifestData>(json)
            if (!expectedVersion.isNullOrBlank() && data.manifestVersion != expectedVersion) {
                return ApiResult.Error("نسخه Manifest ناسازگار است", "MANIFEST_VERSION_MISMATCH")
            }
            manifestCache[cacheKey] = data
            ApiResult.Success(data)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun saveDraft(
        propertyId: Long,
        draft: JsonElement,
        syncPropertyContent: Boolean = false,
    ): ApiResult<ContentStudioDraftResponse> {
        return try {
            val response = api.putContentStudioDraft(
                propertyId,
                ContentStudioDraftPutRequest(
                    draft = draft,
                    syncPropertyContent = syncPropertyContent,
                ),
            )
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در ذخیره", response.code)
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun syncDraftProperty(propertyId: Long): ApiResult<ContentStudioDraftResponse> {
        return try {
            val response = api.putContentStudioDraft(
                propertyId,
                ContentStudioDraftPutRequest(syncProperty = true),
            )
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در همگام‌سازی", response.code)
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    fun clearManifestCache() {
        manifestCache.clear()
    }
}
