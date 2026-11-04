package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import ir.divarfiling.mobile.R

object PcsFontResolver {
    fun typeface(context: Context, fontKey: String?, fontWeight: Int?): Typeface? {
        val weight = fontWeight ?: 400
        val key = fontKey?.lowercase()?.trim().orEmpty()
        val vazirmatn = when {
            weight >= 800 -> ResourcesCompat.getFont(context, R.font.vazirmatn_bold)
            weight >= 600 -> ResourcesCompat.getFont(context, R.font.vazirmatn_semibold)
            else -> ResourcesCompat.getFont(context, R.font.vazirmatn_regular)
        }
        return when (key) {
            "", "vazirmatn", "estedad", "parastoo", "markazi", "lalezar", "gulzar" -> vazirmatn
            else -> vazirmatn
        }
    }
}
