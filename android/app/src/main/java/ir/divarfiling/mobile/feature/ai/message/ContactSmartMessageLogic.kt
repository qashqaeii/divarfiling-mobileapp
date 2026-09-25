package ir.divarfiling.mobile.feature.ai.message

import ir.divarfiling.mobile.core.network.AiCapabilitiesData
import ir.divarfiling.mobile.core.network.AiToneOptionDto

data class CrmMessageAccessState(
    val canUse: Boolean,
    val blockMessage: String?,
    val quotaHint: String?,
    val tones: List<AiToneOptionDto>,
    val defaultToneId: String,
)

object ContactSmartMessageLogic {

    fun accessFromCapabilities(caps: AiCapabilitiesData): CrmMessageAccessState {
        val tones = caps.crmMessageTones
        val canUse = caps.canUseCrmMessage
        val block = when {
            !caps.aiEnabled -> caps.messages.aiDisabled
            !caps.hasLicense -> caps.messages.noLicense
            !canUse -> "پیام هوشمند فعال نیست."
            (caps.crmMessage?.remaining ?: caps.quota?.remaining)?.let { it <= 0 } == true ->
                caps.messages.quotaExhausted
            else -> null
        }
        val quotaHint = caps.crmMessage?.let { q ->
            if (q.limit > 0) "${q.remaining} از ${q.limit} پیام امروز باقی مانده" else null
        }
        return CrmMessageAccessState(
            canUse = canUse && block == null,
            blockMessage = block,
            quotaHint = quotaHint,
            tones = tones,
            defaultToneId = tones.firstOrNull()?.id ?: "رسمی",
        )
    }
}
