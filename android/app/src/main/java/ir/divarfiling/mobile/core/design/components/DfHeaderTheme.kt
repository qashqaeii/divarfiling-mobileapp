package ir.divarfiling.mobile.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfThemeColors

/**
 * پالت بصری هدر — گرادیان نرم، حاشیه و رنگ تأکید متناسب با هر بخش.
 */
data class DfHeaderTheme(
    val gradientStart: Color,
    val gradientEnd: Color,
    val accent: Color,
    val iconBackground: Color,
    val iconTint: Color,
    val borderColor: Color,
    val orbColor: Color,
    val sectionChipBackground: Color,
)

@Composable
@ReadOnlyComposable
fun headerThemeForSection(sectionLabel: String?): DfHeaderTheme {
    val dark = DfThemeColors.isDark()
    return when (sectionLabel) {
        DfHeaderSections.HOME -> palette(
            dark = dark,
            accent = DfColors.Purple,
            container = DfColors.PurpleLight,
            gradientA = DfColors.PurpleGradientStart,
            gradientB = DfColors.PurpleContainer,
        )
        DfHeaderSections.CRM -> palette(
            dark = dark,
            accent = DfColors.Blue,
            container = DfColors.BlueLight,
            gradientA = Color(0xFF3B82F6),
            gradientB = Color(0xFFDBEAFE),
        )
        DfHeaderSections.FILING -> palette(
            dark = dark,
            accent = Color(0xFF6366F1),
            container = Color(0xFFEEF2FF),
            gradientA = Color(0xFF6366F1),
            gradientB = Color(0xFFE0E7FF),
        )
        DfHeaderSections.EXTRACT -> palette(
            dark = dark,
            accent = DfColors.Amber,
            container = DfColors.AmberLight,
            gradientA = Color(0xFFF59E0B),
            gradientB = Color(0xFFFEF3C7),
        )
        DfHeaderSections.TOOLS -> palette(
            dark = dark,
            accent = Color(0xFF0EA5E9),
            container = Color(0xFFE0F2FE),
            gradientA = Color(0xFF0284C7),
            gradientB = Color(0xFFBAE6FD),
        )
        DfHeaderSections.SETTINGS -> palette(
            dark = dark,
            accent = Color(0xFF64748B),
            container = DfColors.SurfaceVariant,
            gradientA = Color(0xFF94A3B8),
            gradientB = Color(0xFFF1F5F9),
        )
        DfHeaderSections.NOTIFICATIONS -> palette(
            dark = dark,
            accent = DfColors.Rose,
            container = DfColors.RoseLight,
            gradientA = Color(0xFFF43F5E),
            gradientB = Color(0xFFFCE7F3),
        )
        DfHeaderSections.TEAM -> palette(
            dark = dark,
            accent = Color(0xFF7C3AED),
            container = Color(0xFFEDE9FE),
            gradientA = Color(0xFF8B5CF6),
            gradientB = Color(0xFFDDD6FE),
        )
        DfHeaderSections.AI -> palette(
            dark = dark,
            accent = DfColors.Pink,
            container = DfColors.PinkLight,
            gradientA = Color(0xFFEC4899),
            gradientB = Color(0xFFFBCFE8),
        )
        DfHeaderSections.MORE -> palette(
            dark = dark,
            accent = Color(0xFF8B5CF6),
            container = DfColors.PurpleLight,
            gradientA = Color(0xFFA78BFA),
            gradientB = Color(0xFFF3E8FF),
        )
        DfHeaderSections.SUPPORT -> palette(
            dark = dark,
            accent = Color(0xFF14B8A6),
            container = Color(0xFFCCFBF1),
            gradientA = Color(0xFF0D9488),
            gradientB = Color(0xFF99F6E4),
        )
        DfHeaderSections.LICENSE -> palette(
            dark = dark,
            accent = Color(0xFF9333EA),
            container = Color(0xFFF3E8FF),
            gradientA = Color(0xFF9333EA),
            gradientB = Color(0xFFFDE68A),
        )
        else -> palette(
            dark = dark,
            accent = DfThemeColors.primary(),
            container = DfThemeColors.primaryContainer(),
            gradientA = DfColors.PurpleGradientStart,
            gradientB = DfColors.PurpleContainer,
        )
    }
}

@Composable
@ReadOnlyComposable
fun sectionAccentColor(sectionLabel: String?): Color = headerThemeForSection(sectionLabel).accent

private fun palette(
    dark: Boolean,
    accent: Color,
    container: Color,
    gradientA: Color,
    gradientB: Color,
): DfHeaderTheme {
    return if (dark) {
        DfHeaderTheme(
            gradientStart = gradientA.copy(alpha = 0.32f),
            gradientEnd = gradientB.copy(alpha = 0.10f),
            accent = accent,
            iconBackground = accent.copy(alpha = 0.18f),
            iconTint = accent,
            borderColor = accent.copy(alpha = 0.24f),
            orbColor = accent.copy(alpha = 0.10f),
            sectionChipBackground = accent.copy(alpha = 0.16f),
        )
    } else {
        DfHeaderTheme(
            gradientStart = gradientA.copy(alpha = 0.22f),
            gradientEnd = gradientB.copy(alpha = 0.55f),
            accent = accent,
            iconBackground = container,
            iconTint = accent,
            borderColor = accent.copy(alpha = 0.16f),
            orbColor = accent.copy(alpha = 0.08f),
            sectionChipBackground = accent.copy(alpha = 0.10f),
        )
    }
}
