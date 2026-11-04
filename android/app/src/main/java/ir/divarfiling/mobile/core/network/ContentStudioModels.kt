package ir.divarfiling.mobile.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ContentStudioSessionData(
    val property: JsonElement? = null,
    val draft: JsonElement? = null,
    val brand: JsonElement? = null,
    val templatesCatalog: List<ContentStudioTemplateCatalogItem>? = null,
    @SerialName("can_edit_property") val canEditProperty: Boolean = false,
    @SerialName("property_fingerprint") val propertyFingerprint: String? = null,
    @SerialName("draft_stale") val draftStale: Boolean? = null,
)

@Serializable
data class ContentStudioTemplateCatalogItem(
    @SerialName("template_id") val templateId: String,
    val label: String = "",
    val description: String = "",
    @SerialName("overlay_url") val overlayUrl: String = "",
    @SerialName("supported_formats") val supportedFormats: List<String> = emptyList(),
    @SerialName("manifest_version") val manifestVersion: String = "",
)

@Serializable
data class ContentStudioManifestData(
    @SerialName("template_id") val templateId: String,
    @SerialName("schema_version") val schemaVersion: Int = 2,
    @SerialName("manifest_version") val manifestVersion: String = "",
    @SerialName("layout_revision") val layoutRevision: String = "",
    val format: String = "story",
    val canvas: PcsCanvasSpec = PcsCanvasSpec(),
    @SerialName("overlay_url") val overlayUrl: String = "",
    val scene: JsonElement? = null,
    @SerialName("dynamic_content") val dynamicContent: JsonElement? = null,
)

@Serializable
data class PcsCanvasSpec(
    val width: Int = 1080,
    val height: Int = 1920,
)

@Serializable
data class ContentStudioDraftPutRequest(
    val draft: JsonElement? = null,
    val payload: JsonElement? = null,
    @SerialName("sync_property") val syncProperty: Boolean? = null,
    @SerialName("sync_property_content") val syncPropertyContent: Boolean? = null,
)

@Serializable
data class ContentStudioDraftResponse(
    val draft: JsonElement? = null,
    @SerialName("draft_stale") val draftStale: Boolean = false,
    @SerialName("property_fingerprint") val propertyFingerprint: String? = null,
    val property: JsonElement? = null,
)
