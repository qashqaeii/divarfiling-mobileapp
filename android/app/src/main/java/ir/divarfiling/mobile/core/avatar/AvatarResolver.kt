package ir.divarfiling.mobile.core.avatar

import androidx.annotation.DrawableRes
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.UserDto

data class ResolvedAvatar(
    val remoteUrl: String? = null,
    @DrawableRes val presetDrawable: Int? = null,
    val presetKey: String? = null,
    val usePreset: Boolean = false,
)

object AvatarResolver {
    fun resolveUser(user: UserDto?): ResolvedAvatar {
        if (user == null) {
            return presetOnly(AvatarCatalog.DEFAULT_KEY)
        }
        if (user.hasCustomAvatar && !user.avatarUrl.isNullOrBlank()) {
            return ResolvedAvatar(remoteUrl = user.avatarUrl)
        }
        val key = user.avatarKey?.takeIf { it.isNotBlank() } ?: AvatarCatalog.DEFAULT_KEY
        return presetOnly(key)
    }

    fun resolveContact(contact: ContactDto): ResolvedAvatar =
        resolveContact(contact.fullName, contact.photoUrl, contact.avatarKey)

    fun resolveContact(name: String?, photoUrl: String?, avatarKey: String? = null): ResolvedAvatar {
        val presetFallback = contactPresetFallback(avatarKey)
        if (!photoUrl.isNullOrBlank()) {
            return ResolvedAvatar(
                remoteUrl = photoUrl,
                presetDrawable = presetFallback.presetDrawable,
                presetKey = presetFallback.presetKey,
                usePreset = false,
            )
        }
        return presetFallback
    }

    private fun contactPresetFallback(avatarKey: String?): ResolvedAvatar {
        val key = avatarKey?.takeIf { it.isNotBlank() } ?: AvatarCatalog.DEFAULT_KEY
        return presetOnly(key)
    }

    fun initialsFromName(name: String?): String {
        val parts = name?.trim()?.split(Regex("\\s+"))?.filter { it.isNotBlank() } ?: emptyList()
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(1)
            else -> "${parts[0].take(1)}${parts[1].take(1)}"
        }
    }

    private fun presetOnly(key: String): ResolvedAvatar {
        val normalized = AvatarCatalog.normalizeKey(key)
        return ResolvedAvatar(
            presetDrawable = AvatarCatalog.drawableForKey(normalized),
            presetKey = normalized,
            usePreset = true,
        )
    }
}
