package ir.divarfiling.mobile.feature.contentstudio.canvas

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PcsStudioBrand(
    @SerialName("brand_name") val brandName: String = "",
    @SerialName("consultant_name") val consultantName: String = "",
    @SerialName("contact_phone") val contactPhone: String = "",
    val visibility: PcsBrandVisibility? = null,
)

@Serializable
data class PcsBrandVisibility(
    @SerialName("consultant_name") val consultantName: Boolean? = null,
    @SerialName("contact_phone") val contactPhone: Boolean? = null,
)

object PcsBrandFooter {
    fun overlayFooterLine(name: String, phone: String): String {
        return when {
            name.isNotBlank() && phone.isNotBlank() -> "$name  ·  $phone"
            phone.isNotBlank() -> phone
            else -> name
        }
    }

    fun resolve(
        brand: PcsStudioBrand?,
        posterConfConsultant: String = "",
        posterConfPhone: String = "",
        posterConfBrandName: String = "",
    ): String {
        val vis = brand?.visibility
        val name = if (vis?.consultantName == false) {
            ""
        } else {
            posterConfConsultant.ifBlank { brand?.consultantName.orEmpty() }
                .ifBlank { posterConfBrandName.ifBlank { brand?.brandName.orEmpty() } }
        }
        val phone = if (vis?.contactPhone == false) {
            ""
        } else {
            posterConfPhone.ifBlank { brand?.contactPhone.orEmpty() }
        }
        return overlayFooterLine(name.trim(), phone.trim())
    }
}
