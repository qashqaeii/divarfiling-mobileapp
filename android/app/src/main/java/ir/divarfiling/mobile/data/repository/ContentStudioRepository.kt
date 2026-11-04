package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.core.network.ApiEnvelope
import ir.divarfiling.mobile.core.network.ContentStudioDraftPutRequest
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioBrand
import ir.divarfiling.mobile.core.network.ContentStudioDraftResponse
import ir.divarfiling.mobile.core.network.ContentStudioManifestData
import ir.divarfiling.mobile.core.network.ContentStudioSessionData
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.json.Json
import ir.divarfiling.mobile.core.network.toApiFailure
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
            val failure = e.toApiFailure("خطا در بارگذاری استودیو")
            ApiResult.Error(failure.message, failure.code)
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
            val failure = e.toApiFailure("خطا در Manifest")
            ApiResult.Error(failure.message, failure.code)
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
            val failure = e.toApiFailure("خطا در ذخیره")
            ApiResult.Error(failure.message, failure.code)
        }
    }

    suspend fun saveUserBrand(
        propertyId: Long,
        brand: PcsStudioBrand,
    ): ApiResult<PcsStudioBrand> {
        return try {
            val body = mapOf(
                "brand_name" to brand.brandName,
                "consultant_name" to brand.consultantName,
                "contact_phone" to brand.contactPhone,
                "instagram" to brand.instagram,
                "logo_url" to brand.logoUrl,
            )
            val response = api.putContentStudioBrand(propertyId, body)
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در ذخیره برند", response.code)
            }
            val el = response.data
            val brandJson = (el as? kotlinx.serialization.json.JsonObject)?.get("brand") ?: el
            val decoded = brandJson?.let {
                runCatching { json.decodeFromJsonElement(PcsStudioBrand.serializer(), it) }.getOrNull()
            } ?: brand
            ApiResult.Success(decoded)
        } catch (e: Exception) {
            val failure = e.toApiFailure("خطا در ذخیره برند")
            ApiResult.Error(failure.message, failure.code)
        }
    }

    suspend fun runAiAction(
        propertyId: Long,
        action: String,
        body: Map<String, String> = emptyMap(),
    ): ApiResult<JsonElement> {
        return try {
            val response = api.postContentStudioAi(propertyId, action, body)
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطای AI", response.code)
            }
            ApiResult.Success(response.data ?: kotlinx.serialization.json.JsonNull)
        } catch (e: Exception) {
            val failure = e.toApiFailure("خطای AI")
            ApiResult.Error(failure.message, failure.code)
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
            val failure = e.toApiFailure("خطا در همگام‌سازی")
            ApiResult.Error(failure.message, failure.code)
        }
    }

    fun clearManifestCache() {
        manifestCache.clear()
    }
}
