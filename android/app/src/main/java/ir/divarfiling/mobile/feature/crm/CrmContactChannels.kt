package ir.divarfiling.mobile.feature.crm

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

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

    val allKeys: Set<String> = (builtinChannels + socialNetworks).map { it.key }.toSet()

    fun defaultActiveChannelsForSource(source: String): Set<String> = when (source.trim()) {
        "تلگرام" -> setOf("telegram", "phone")
        "اینستاگرام" -> setOf("instagram", "phone")
        "دیوار", "سایت" -> setOf("phone", "whatsapp")
        else -> setOf("phone", "whatsapp", "sms")
    }

    fun channelDef(key: String): ContactChannelDef? =
        (builtinChannels + socialNetworks).firstOrNull { it.key == key }
}
