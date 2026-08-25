package ir.divarfiling.mobile.feature.crm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.util.PhoneNormalizer

data class ContactChannelDef(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val usesPhone: Boolean = false,
    val placeholder: String = "",
)

object CrmContactChannels {
    val builtinChannels: List<ContactChannelDef> = listOf(
        ContactChannelDef("whatsapp", "واتساپ", DfIcons.MessageCircle, Color(0xFF25D366), usesPhone = true),
        ContactChannelDef("sms", "پیامک", DfIcons.MessageSquare, Color(0xFF2563EB), usesPhone = true),
        ContactChannelDef("phone", "تماس", DfIcons.Phone, Color(0xFF10B981), usesPhone = true),
    )

    val socialNetworks: List<ContactChannelDef> = listOf(
        ContactChannelDef("telegram", "تلگرام", DfIcons.Send, Color(0xFF2AABEE), placeholder = "@username یا ۰۹۱۲…"),
        ContactChannelDef("bale", "بله", DfIcons.MessageCircle, Color(0xFF00C2A8), placeholder = "آیدی یا ۰۹۱۲…"),
        ContactChannelDef("rubika", "روبیکا", DfIcons.MessageSquare, Color(0xFF6C2BD9), placeholder = "آیدی یا ۰۹۱۲…"),
        ContactChannelDef("eitaa", "ایتا", DfIcons.Send, Color(0xFFF7941D), placeholder = "آیدی یا ۰۹۱۲…"),
        ContactChannelDef("instagram", "اینستاگرام", DfIcons.Share2, Color(0xFFE1306C), placeholder = "@username"),
        ContactChannelDef("soroush", "سروش", DfIcons.MessageCircle, Color(0xFF00A693), placeholder = "آیدی یا ۰۹۱۲…"),
    )

    val phoneDerivedSocialKeys: Set<String> = setOf("telegram", "bale", "rubika", "eitaa", "soroush")

    val allKeys: Set<String> = (builtinChannels + socialNetworks).map { it.key }.toSet()

    fun defaultActiveChannelsForSource(source: String): Set<String> = when (source.trim()) {
        "تلگرام" -> setOf("telegram", "phone")
        "اینستاگرام" -> setOf("instagram", "phone")
        "دیوار", "سایت" -> setOf("phone", "whatsapp")
        else -> setOf("phone", "whatsapp", "sms")
    }

    fun channelDef(key: String): ContactChannelDef? =
        (builtinChannels + socialNetworks).firstOrNull { it.key == key }

    fun requiresManualSocialHandle(key: String): Boolean = key == "instagram"

    fun usesPhoneForSocialHandle(key: String): Boolean = key in phoneDerivedSocialKeys

    fun phoneReady(rawPhone: String): Boolean = PhoneNormalizer.normalize(rawPhone).isNotBlank()

    fun autoSocialHandle(networkKey: String, rawPhone: String): String? {
        if (requiresManualSocialHandle(networkKey)) return null
        if (!usesPhoneForSocialHandle(networkKey)) return null
        val normalized = PhoneNormalizer.normalize(rawPhone)
        return normalized.takeIf { it.isNotBlank() }
    }

    fun syncPhoneDerivedSocialLinks(
        activeChannels: Set<String>,
        phone: String,
        currentLinks: Map<String, String>,
    ): Map<String, String> {
        val next = currentLinks.toMutableMap()
        activeChannels.filter { usesPhoneForSocialHandle(it) }.forEach { key ->
            autoSocialHandle(key, phone)?.let { next[key] = it }
        }
        return next
    }
}
