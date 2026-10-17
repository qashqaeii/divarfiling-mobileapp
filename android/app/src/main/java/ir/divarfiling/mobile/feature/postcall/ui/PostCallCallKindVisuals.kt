package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.feature.postcall.PostCallLogic

@Immutable
data class PostCallCallKindVisual(
    val accent: Color,
    val container: Color,
    val onContainer: Color,
    val icon: ImageVector,
    val kindLabel: String,
    val sheetSectionLabel: String,
)

object PostCallCallKindVisuals {

    @Composable
    @ReadOnlyComposable
    fun forKind(kind: PostCallLogic.CallEndKind): PostCallCallKindVisual = when (kind) {
        PostCallLogic.CallEndKind.Missed -> PostCallCallKindVisual(
            accent = DfThemeColors.warning(),
            container = DfThemeColors.warningContainer(),
            onContainer = DfThemeColors.onWarning(),
            icon = DfIcons.PhoneMissed,
            kindLabel = "تماس بی‌پاسخ",
            sheetSectionLabel = "پیگیری تماس از دست‌رفته",
        )
        PostCallLogic.CallEndKind.Answered -> PostCallCallKindVisual(
            accent = DfThemeColors.success(),
            container = DfThemeColors.successContainer(),
            onContainer = DfThemeColors.onSuccess(),
            icon = DfIcons.PhoneIncoming,
            kindLabel = "تماس پاسخ‌داده‌شده",
            sheetSectionLabel = "ثبت نتیجه مکالمه",
        )
        PostCallLogic.CallEndKind.Outgoing -> PostCallCallKindVisual(
            accent = DfThemeColors.info(),
            container = DfThemeColors.infoContainer(),
            onContainer = DfThemeColors.onInfo(),
            icon = DfIcons.PhoneOutgoing,
            kindLabel = "تماس خروجی",
            sheetSectionLabel = "نتیجه تماس خروجی",
        )
        PostCallLogic.CallEndKind.Ignored -> PostCallCallKindVisual(
            accent = DfThemeColors.textMuted(),
            container = DfThemeColors.surfaceVariant(),
            onContainer = DfThemeColors.textSecondary(),
            icon = DfIcons.Phone,
            kindLabel = "تماس",
            sheetSectionLabel = "دستیار پس از تماس",
        )
    }
}
