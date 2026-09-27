package ir.divarfiling.mobile.core.avatar

import ir.divarfiling.mobile.core.network.UserDto

/** منطق باز کردن شیت انتخاب آواتار و فعال‌بودن دکمه ذخیره — قابل تست بدون ViewModel. */
object AvatarPickerLogic {
    fun seedKey(user: UserDto?): String {
        if (user == null) return AvatarCatalog.DEFAULT_KEY
        if (user.hasCustomAvatar) return AvatarCatalog.DEFAULT_KEY
        return user.avatarKey
            ?.takeIf { it.isNotBlank() }
            ?.let(AvatarCatalog::normalizeKey)
            ?: AvatarCatalog.DEFAULT_KEY
    }

    /**
     * @param pickerTouched کاربر حداقل یک بار در گرید انتخاب کرده (یا از عکس گالری می‌آید).
     */
    fun canSave(
        user: UserDto?,
        pendingKey: String?,
        pickerTouched: Boolean,
    ): Boolean {
        if (user == null) return false
        val pending = AvatarCatalog.normalizeKey(pendingKey ?: AvatarCatalog.DEFAULT_KEY)
        if (user.hasCustomAvatar) {
            return pickerTouched
        }
        val saved = user.avatarKey
            ?.takeIf { it.isNotBlank() }
            ?.let(AvatarCatalog::normalizeKey)
            ?: AvatarCatalog.DEFAULT_KEY
        return pending != saved
    }

    /** با عکس گالری، انتخاب preset برای جایگزینی بلافاصله قابل ذخیره است. */
    fun initialPickerTouched(user: UserDto?): Boolean = user?.hasCustomAvatar == true
}
