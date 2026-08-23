package ir.divarfiling.mobile.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfThemeColors

/**
 * پالت ظریف هدر — فقط accent و tint ملایم، بدون بنر رنگی.
 */
data class DfHeaderTheme(
    val accent: Color,
    val iconBackground: Color,
    val iconTint: Color,
    val surfaceTint: Color,
)

@Composable
@ReadOnlyComposable
fun headerThemeForSection(sectionLabel: String?): DfHeaderTheme {
    val dark = DfThemeColors.isDark()
    return when (sectionLabel) {
        DfHeaderSections.HOME -> palette(dark, DfColors.Purple, DfColors.PurpleLight)
        DfHeaderSections.CRM -> palette(dark, DfColors.Blue, DfColors.BlueLight)
        DfHeaderSections.FILING -> palette(dark, Color(0xFF6366F1), Color(0xFFEEF2FF))
        DfHeaderSections.EXTRACT -> palette(dark, DfColors.Amber, DfColors.AmberLight)
        DfHeaderSections.TOOLS -> palette(dark, Color(0xFF0EA5E9), Color(0xFFE0F2FE))
        DfHeaderSections.SETTINGS -> palette(dark, Color(0xFF64748B), DfColors.SurfaceVariant)
        DfHeaderSections.NOTIFICATIONS -> palette(dark, DfColors.Purple, DfColors.PurpleLight)
        DfHeaderSections.TEAM -> palette(dark, Color(0xFF7C3AED), Color(0xFFEDE9FE))
        DfHeaderSections.AI -> palette(dark, DfColors.Pink, DfColors.PinkLight)
        DfHeaderSections.MORE -> palette(dark, Color(0xFF8B5CF6), DfColors.PurpleLight)
        DfHeaderSections.SUPPORT -> palette(dark, Color(0xFF14B8A6), Color(0xFFCCFBF1))
        DfHeaderSections.LICENSE -> palette(dark, Color(0xFF9333EA), Color(0xFFF3E8FF))
        else -> palette(dark, DfThemeColors.primary(), DfThemeColors.primaryContainer())
    }
}

@Composable
@ReadOnlyComposable
fun sectionAccentColor(sectionLabel: String?): Color = headerThemeForSection(sectionLabel).accent

private fun palette(
    dark: Boolean,
    accent: Color,
    container: Color,
): DfHeaderTheme {
    return if (dark) {
        DfHeaderTheme(
            accent = accent,
            iconBackground = accent.copy(alpha = 0.14f),
            iconTint = accent,
            surfaceTint = accent.copy(alpha = 0.05f),
        )
    } else {
        DfHeaderTheme(
            accent = accent,
            iconBackground = container.copy(alpha = 0.65f),
            iconTint = accent,
            surfaceTint = accent.copy(alpha = 0.035f),
        )
    }
}
