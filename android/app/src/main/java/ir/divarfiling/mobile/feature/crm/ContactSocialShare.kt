package ir.divarfiling.mobile.feature.crm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.share.DossierShareActions
import ir.divarfiling.mobile.core.util.PhoneNormalizer

data class ContactShareChannel(
    val key: String,
    val label: String,
    val accent: Color,
    @DrawableRes val iconRes: Int? = null,
    val emoji: String? = null,
    val vectorIcon: ImageVector? = null,
)

data class PendingContactSocialShare(
    val channel: String,
    val message: String,
    val phone: String? = null,
    val socialLinks: Map<String, String> = emptyMap(),
)

object ContactSocialShare {
    private val messagingOrder = listOf(
        "whatsapp", "bale", "telegram", "rubika", "soroush", "eitaa", "instagram",
    )

    fun resolveMessagingChannels(
        phone: String?,
        activeChannels: List<String>,
        socialLinks: Map<String, String>,
    ): List<ContactShareChannel> {
        val normalizedPhone = phone?.trim().orEmpty()
        val hasPhone = normalizedPhone.isNotBlank()
        val active = activeChannels.map { it.trim() }.filter { it.isNotBlank() }.toSet()
        val links = socialLinks.mapKeys { it.key.trim() }.filterValues { it.isNotBlank() }

        fun isAvailable(key: String): Boolean = when (key) {
            "whatsapp" -> hasPhone || "whatsapp" in active
            "bale" -> hasPhone || "bale" in active || !links["bale"].isNullOrBlank()
            "instagram" -> "instagram" in active && !links["instagram"].isNullOrBlank()
            else -> {
                if (key !in active && key !in links.keys) return false
                if (CrmContactChannels.usesPhoneForSocialHandle(key)) {
                    !links[key].isNullOrBlank() || hasPhone
                } else {
                    !links[key].isNullOrBlank()
                }
            }
        }

        return messagingOrder
            .filter { isAvailable(it) }
            .mapNotNull { key -> channelButton(key) }
    }

    private fun channelButton(key: String): ContactShareChannel? = when (key) {
        "whatsapp" -> ContactShareChannel(
            key = key,
            label = "واتساپ",
            accent = Color(0xFF25D366),
            iconRes = R.drawable.ic_whatsapp,
        )
        "bale" -> ContactShareChannel(
            key = key,
            label = "بله",
            accent = Color(0xFF00C2A8),
            emoji = "💬",
            vectorIcon = DfIcons.MessageCircle,
        )
        "telegram" -> ContactShareChannel(
            key = key,
            label = "تلگرام",
            accent = Color(0xFF2AABEE),
            emoji = "✈️",
            vectorIcon = DfIcons.Send,
        )
        "rubika" -> ContactShareChannel(
            key = key,
            label = "روبیکا",
            accent = Color(0xFF6C2BD9),
            emoji = "💜",
            vectorIcon = DfIcons.MessageSquare,
        )
        "soroush" -> ContactShareChannel(
            key = key,
            label = "سروش",
            accent = Color(0xFF1E6BFF),
            emoji = "📱",
            vectorIcon = DfIcons.MessageCircle,
        )
        "eitaa" -> ContactShareChannel(
            key = key,
            label = "ایتا",
            accent = Color(0xFFF7941D),
            emoji = "📨",
            vectorIcon = DfIcons.Send,
        )
        "instagram" -> ContactShareChannel(
            key = key,
            label = "اینستاگرام",
            accent = Color(0xFFE1306C),
            emoji = "📷",
            vectorIcon = DfIcons.Share2,
        )
        else -> CrmContactChannels.channelDef(key)?.let { def ->
            ContactShareChannel(
                key = def.key,
                label = def.label,
                accent = def.accent,
                vectorIcon = def.icon,
            )
        }
    }

    fun open(context: Context, pending: PendingContactSocialShare) {
        open(
            context = context,
            channel = pending.channel,
            message = pending.message,
            phone = pending.phone,
            socialLinks = pending.socialLinks,
        )
    }

    fun open(
        context: Context,
        channel: String,
        message: String,
        phone: String?,
        socialLinks: Map<String, String>,
    ) {
        val text = message.trim()
        if (text.isBlank()) return
        when (channel) {
            "whatsapp" -> DossierShareActions.openWhatsApp(context, text, phone)
            "bale" -> DossierShareActions.openBale(context, text, phone)
            "telegram" -> openTelegram(context, text, socialLinks["telegram"], phone)
            "rubika" -> openProfileLink(context, text, socialLinks["rubika"], phone, "https://rubika.ir/")
            "soroush" -> openProfileLink(context, text, socialLinks["soroush"], phone, "https://splus.ir/")
            "eitaa" -> openProfileLink(context, text, socialLinks["eitaa"], phone, "https://eitaa.com/")
            "instagram" -> openInstagram(context, socialLinks["instagram"])
            else -> DossierShareActions.shareText(context, text, "ارسال پیام")
        }
    }

    private fun openTelegram(context: Context, message: String, handle: String?, phone: String?) {
        val username = normalizeHandle(handle) ?: phone?.let { PhoneNormalizer.normalize(it) }?.takeIf { it.isNotBlank() }
        if (!username.isNullOrBlank()) {
            val opened = runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$username")),
                )
            }.isSuccess
            if (opened) {
                DossierShareActions.copyToClipboard(context, message)
                Toast.makeText(context, "متن در کلیپ‌بورد — در تلگرام paste کنید", Toast.LENGTH_SHORT).show()
                return
            }
        }
        DossierShareActions.openTelegram(context, message)
    }

    private fun openProfileLink(
        context: Context,
        message: String,
        handle: String?,
        phone: String?,
        baseUrl: String,
    ) {
        val slug = normalizeHandle(handle)
            ?: phone?.let { PhoneNormalizer.baleUserId(it) }?.takeIf { it.isNotBlank() }
        if (!slug.isNullOrBlank()) {
            val opened = runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$baseUrl$slug")))
            }.isSuccess
            if (opened) {
                DossierShareActions.copyToClipboard(context, message)
                Toast.makeText(context, "متن کپی شد — در پیام paste کنید", Toast.LENGTH_SHORT).show()
                return
            }
        }
        DossierShareActions.shareText(context, message, "ارسال پیام")
    }

    private fun openInstagram(context: Context, handle: String?) {
        val username = normalizeHandle(handle)?.removePrefix("@")
        if (username.isNullOrBlank()) {
            Toast.makeText(context, "آیدی اینستاگرام ثبت نشده", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/$username")),
            )
        }.onFailure {
            Toast.makeText(context, "امکان باز کردن اینستاگرام نیست", Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalizeHandle(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return null
        return value.removePrefix("@").removePrefix("https://").substringBefore("/")
    }
}
