package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.feature.postcall.PostCallLogic

@Immutable
data class PostCallCallKindVisual(
    val accent: Color,
    val container: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val onContainer: Color,
    val icon: ImageVector,
    val kindLabel: String,
    val sheetSectionLabel: String,
) {
    val containerBrush: Brush
        get() = Brush.linearGradient(listOf(gradientStart, gradientEnd))
}

object PostCallCallKindVisuals {

    @Composable
    @ReadOnlyComposable
    fun forKind(kind: PostCallLogic.CallEndKind): PostCallCallKindVisual = when (kind) {
        PostCallLogic.CallEndKind.Missed -> softVisual(
            baseAccent = DfThemeColors.warning(),
            icon = DfIcons.PhoneMissed,
            kindLabel = "تماس بی‌پاسخ",
            sheetSectionLabel = "پیگیری تماس از دست‌رفته",
        )
        PostCallLogic.CallEndKind.Answered -> softVisual(
            baseAccent = DfThemeColors.success(),
            icon = DfIcons.PhoneIncoming,
            kindLabel = "تماس پاسخ‌داده‌شده",
            sheetSectionLabel = "ثبت نتیجه مکالمه",
        )
        PostCallLogic.CallEndKind.Outgoing -> softVisual(
            baseAccent = DfThemeColors.info(),
            icon = DfIcons.PhoneOutgoing,
            kindLabel = "تماس خروجی",
            sheetSectionLabel = "نتیجه تماس خروجی",
        )
        PostCallLogic.CallEndKind.Ignored -> softVisual(
            baseAccent = DfThemeColors.textMuted(),
            icon = DfIcons.Phone,
            kindLabel = "تماس",
            sheetSectionLabel = "دستیار پس از تماس",
            muted = true,
        )
    }

    @Composable
    private fun softVisual(
        baseAccent: Color,
        icon: ImageVector,
        kindLabel: String,
        sheetSectionLabel: String,
        muted: Boolean = false,
    ): PostCallCallKindVisual {
        val surface = DfThemeColors.surface()
        val blend = if (muted) 0.06f else 0.11f
        val gradientStrong = if (muted) 0.10f else 0.16f
        val container = lerp(surface, baseAccent, blend)
        val gradientStart = lerp(surface, baseAccent, blend * 0.65f)
        val gradientEnd = lerp(surface, baseAccent, gradientStrong)
        val accent = baseAccent.copy(alpha = if (muted) 0.55f else 0.62f)
        val onContainer = DfThemeColors.textPrimary().copy(alpha = 0.92f)
        return PostCallCallKindVisual(
            accent = accent,
            container = container,
            gradientStart = gradientStart,
            gradientEnd = gradientEnd,
            onContainer = onContainer,
            icon = icon,
            kindLabel = kindLabel,
            sheetSectionLabel = sheetSectionLabel,
        )
    }
}
