package ir.divarfiling.mobile.feature.contentstudio.canvas

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PcsSceneDocument(
    @SerialName("schemaVersion") val schemaVersion: Int = 2,
    val layers: List<PcsLayer> = emptyList(),
    @SerialName("layerOrder") val layerOrder: List<String> = emptyList(),
    val canvas: PcsSceneCanvas? = null,
)

@Serializable
data class PcsSceneCanvas(
    val width: Int = 1080,
    val height: Int = 1920,
    val format: String = "story",
)

@Serializable
data class PcsLayer(
    val id: String,
    val type: String,
    val name: String? = null,
    val visible: Boolean = true,
    val locked: Boolean = false,
    @SerialName("parentId") val parentId: String? = null,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val binding: PcsBinding? = null,
    val style: PcsTextStyle? = null,
    val text: String? = null,
    @SerialName("imageIndex") val imageIndex: Int? = null,
    @SerialName("templateRole") val templateRole: String? = null,
    @SerialName("contentContract") val contentContract: PcsContentContract? = null,
    @SerialName("assetRef") val assetRef: String? = null,
    @SerialName("overlay_url") val overlayUrl: String? = null,
    @SerialName("borderRadius") val borderRadius: Float? = null,
    @SerialName("objectFit") val objectFit: String? = null,
    val fill: JsonElement? = null,
    val children: List<PcsLayer>? = null,
)

@Serializable
data class PcsBinding(
    val source: String = "",
    val field: String = "",
    val mode: String = "linked",
)

@Serializable
data class PcsContentContract(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    val resolver: String = "",
    @SerialName("template_role") val templateRole: String = "",
    @SerialName("default_binding") val defaultBinding: PcsBinding? = null,
)

@Serializable
data class PcsTextStyle(
    @SerialName("fontKey") val fontKey: String? = null,
    @SerialName("fontSize") val fontSize: Float? = null,
    @SerialName("fontWeight") val fontWeight: Int? = null,
    val fill: String? = null,
    val align: String? = null,
    val direction: String? = null,
    @SerialName("lineHeight") val lineHeight: Float? = null,
    val background: String? = null,
    @SerialName("borderRadius") val borderRadius: Float? = null,
    @SerialName("objectFit") val objectFit: String? = null,
    @SerialName("cropPanX") val cropPanX: Float? = null,
    @SerialName("cropPanY") val cropPanY: Float? = null,
    @SerialName("cropZoom") val cropZoom: Float? = null,
    @SerialName("textPreset") val textPreset: String? = null,
    @SerialName("fontStyle") val fontStyle: String? = null,
    @SerialName("textDecoration") val textDecoration: String? = null,
    val brightness: Float? = null,
    val blur: Float? = null,
    val opacity: Float? = null,
)

@Serializable
data class PcsStudioProperty(
    val id: Long = 0,
    val title: String = "",
    val headline: String = "",
    @SerialName("property_type") val propertyType: String = "",
    @SerialName("deal_mode") val dealMode: String = "",
    @SerialName("area_label") val areaLabel: String = "",
    @SerialName("rooms_label") val roomsLabel: String = "",
    @SerialName("floor_label") val floorLabel: String = "",
    @SerialName("build_year_label") val buildYearLabel: String = "",
    @SerialName("location_line") val locationLine: String = "",
    val city: String = "",
    val images: List<String> = emptyList(),
    @SerialName("cover_image") val coverImage: String = "",
    @SerialName("detail_chips") val detailChips: List<String> = emptyList(),
    val price: PcsPriceBlock? = null,
    @SerialName("editor_features") val editorFeatures: List<PcsEditorFeature> = emptyList(),
)

@Serializable
data class PcsPriceBlock(
    val show: Boolean = false,
    val label: String = "",
    @SerialName("deal_mode") val dealMode: String = "",
)

@Serializable
data class PcsEditorFeature(
    val key: String = "",
    val label: String = "",
    val value: String = "",
)
