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

    fun resolveContact(contact: ContactDto): ResolvedAvatar {
        if (!contact.photoUrl.isNullOrBlank()) {
            return ResolvedAvatar(remoteUrl = contact.photoUrl)
        }
        return presetOnly(AvatarCatalog.DEFAULT_KEY)
    }

    fun resolveContact(name: String?, photoUrl: String?): ResolvedAvatar {
        if (!photoUrl.isNullOrBlank()) {
            return ResolvedAvatar(remoteUrl = photoUrl)
        }
        return presetOnly(AvatarCatalog.DEFAULT_KEY)
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
