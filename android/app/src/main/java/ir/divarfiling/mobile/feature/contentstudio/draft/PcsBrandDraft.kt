package ir.divarfiling.mobile.feature.contentstudio.draft

import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsBrandColors
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsBrandVisibility
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioBrand
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.Json

@Serializable
data class PcsBrandOverrideDraft(
    @SerialName("brand_name") val brandName: String? = null,
    @SerialName("consultant_name") val consultantName: String? = null,
    @SerialName("contact_phone") val contactPhone: String? = null,
    val instagram: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    val colors: PcsBrandColors? = null,
    val visibility: PcsBrandVisibility? = null,
)

object PcsBrandDraftPersistence {

    fun readOverride(draft: JsonElement?, json: Json): PcsBrandOverrideDraft? {
        val root = draft as? JsonObject ?: return null
        val el = root["brand_override"] ?: return null
        return runCatching {
            json.decodeFromJsonElement(PcsBrandOverrideDraft.serializer(), el)
        }.getOrNull()
    }

    fun buildOverridePatch(override: PcsBrandOverrideDraft?, clear: Boolean = false): JsonObject {
        return buildJsonObject {
            if (clear || override == null) {
                put("brand_override", buildJsonObject { })
            } else {
                put("brand_override", jsonObjectFromOverride(override))
            }
        }
    }

    fun jsonObjectFromOverride(override: PcsBrandOverrideDraft): JsonObject {
        return buildJsonObject {
            override.brandName?.let { put("brand_name", it) }
            override.consultantName?.let { put("consultant_name", it) }
            override.contactPhone?.let { put("contact_phone", it) }
            override.instagram?.let { put("instagram", it) }
            override.logoUrl?.let { put("logo_url", it) }
            override.colors?.let { c ->
                put(
                    "colors",
                    buildJsonObject {
                        c.primary?.let { put("primary", it) }
                        c.accent?.let { put("accent", it) }
                        c.textOnPrimary?.let { put("text_on_primary", it) }
                    },
                )
            }
            override.visibility?.let { v ->
                put(
                    "visibility",
                    buildJsonObject {
                        v.consultantName?.let { put("consultant_name", it) }
                        v.contactPhone?.let { put("contact_phone", it) }
                        v.instagram?.let { put("instagram", it) }
                    },
                )
            }
        }
    }
}

object PcsBrandMerge {
    fun effective(base: PcsStudioBrand?, override: PcsBrandOverrideDraft?): PcsStudioBrand {
        val b = base ?: PcsStudioBrand()
        if (override == null) return b
        return b.copy(
            brandName = override.brandName?.takeIf { it.isNotBlank() } ?: b.brandName,
            consultantName = override.consultantName?.takeIf { it.isNotBlank() } ?: b.consultantName,
            contactPhone = override.contactPhone?.takeIf { it.isNotBlank() } ?: b.contactPhone,
            instagram = override.instagram?.takeIf { it.isNotBlank() } ?: b.instagram,
            logoUrl = override.logoUrl?.takeIf { it.isNotBlank() } ?: b.logoUrl,
            colors = override.colors ?: b.colors,
            visibility = override.visibility ?: b.visibility,
        )
    }
}
