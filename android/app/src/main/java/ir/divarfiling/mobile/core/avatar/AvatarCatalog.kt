package ir.divarfiling.mobile.core.avatar

import androidx.annotation.DrawableRes
import ir.divarfiling.mobile.R

object AvatarCatalog {
    const val DEFAULT_KEY = "avatar_01"

    val allKeys: List<String> = (1..16).map { index -> "avatar_%02d".format(index) }

    fun normalizeKey(key: String?): String {
        if (key.isNullOrBlank()) return DEFAULT_KEY
        val raw = key.trim().lowercase().replace('-', '_')
        if (allKeys.contains(raw)) return raw
        val digits = raw.removePrefix("avatar").trimStart('_')
        val parsed = digits.toIntOrNull()
        if (parsed != null && parsed in 1..16) {
            return "avatar_%02d".format(parsed)
        }
        return DEFAULT_KEY
    }

    @DrawableRes
    fun drawableForKey(key: String?): Int = when (normalizeKey(key)) {
        "avatar_01" -> R.drawable.avatar_01
        "avatar_02" -> R.drawable.avatar_02
        "avatar_03" -> R.drawable.avatar_03
        "avatar_04" -> R.drawable.avatar_04
        "avatar_05" -> R.drawable.avatar_05
        "avatar_06" -> R.drawable.avatar_06
        "avatar_07" -> R.drawable.avatar_07
        "avatar_08" -> R.drawable.avatar_08
        "avatar_09" -> R.drawable.avatar_09
        "avatar_10" -> R.drawable.avatar_10
        "avatar_11" -> R.drawable.avatar_11
        "avatar_12" -> R.drawable.avatar_12
        "avatar_13" -> R.drawable.avatar_13
        "avatar_14" -> R.drawable.avatar_14
        "avatar_15" -> R.drawable.avatar_15
        "avatar_16" -> R.drawable.avatar_16
        else -> R.drawable.avatar_01
    }
}
