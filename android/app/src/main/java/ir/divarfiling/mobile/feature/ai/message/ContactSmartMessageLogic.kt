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

    fun humanizeError(message: String): String {
        val m = message.trim()
        if (m.isBlank()) return "خطایی رخ داد. دوباره امتحان کنید."
        if (m.contains("Json", ignoreCase = true) ||
            m.contains("serialized", ignoreCase = true) ||
            m.contains("JSON", ignoreCase = true)
        ) {
            return "بارگذاری تنظیمات پیام هوشمند ناموفق بود. لطفاً دوباره امتحان کنید."
        }
        return m
    }

    fun accessFromCapabilities(caps: AiCapabilitiesData): CrmMessageAccessState {
        val tones = caps.crmMessageTones
        val canUse = caps.canUseCrmMessage
        val block = when {
            caps.isUnlimited -> null
            !caps.aiEnabled -> caps.messages.aiDisabled
            !caps.hasLicense -> caps.messages.noLicense
            !canUse -> "پیام هوشمند فعال نیست."
            (caps.crmMessage?.resolvedRemaining() ?: caps.quota?.resolvedRemaining())?.let { it <= 0 } == true ->
                caps.messages.quotaExhausted
            else -> null
        }
        val quotaHint = caps.crmMessage?.let { q ->
            val lim = q.effectiveLimit()
            val rem = q.effectiveRemaining()
            if (lim > 0 && !caps.isUnlimited) "$rem از $lim پیام امروز باقی مانده" else null
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
