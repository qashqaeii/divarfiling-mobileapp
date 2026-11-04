package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import ir.divarfiling.mobile.R

object PcsFontResolver {
    /** Keys from PCS manifests; assets not bundled per-key yet — falls back to Vazirmatn with weight. */
    private val knownKeys = setOf(
        "vazirmatn", "estedad", "parastoo", "markazi", "lalezar", "gulzar", "sahel", "shabnam",
    )

    fun typeface(context: Context, fontKey: String?, fontWeight: Int?): Typeface? {
        val weight = fontWeight ?: 400
        val key = fontKey?.lowercase()?.trim().orEmpty()
        val vazirmatn = when {
            weight >= 800 -> ResourcesCompat.getFont(context, R.font.vazirmatn_bold)
            weight >= 600 -> ResourcesCompat.getFont(context, R.font.vazirmatn_semibold)
            else -> ResourcesCompat.getFont(context, R.font.vazirmatn_regular)
        }
        return when {
            key.isBlank() || key in knownKeys -> vazirmatn
            else -> vazirmatn
        }
    }

    fun resolvedFontLabel(fontKey: String?): String {
        val key = fontKey?.lowercase()?.trim().orEmpty()
        return if (key in knownKeys && key != "vazirmatn") {
            "$key → Vazirmatn (fallback)"
        } else {
            "Vazirmatn"
        }
    }
}
