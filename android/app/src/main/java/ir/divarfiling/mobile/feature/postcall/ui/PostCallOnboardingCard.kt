package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton

@Composable
fun PostCallOnboardingCard(
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text(
                "دستیار پس از تماس",
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
            )
            Text(
                "بعد از هر تماس، فایلینگ دیوار مشتری را شناسایی می‌کند تا همان‌جا نتیجه صحبت‌هایت، یادداشت‌ها و پیگیری بعدی را ثبت کنی. اگر شماره جدید باشد، می‌توانی سریع به مخاطبانت اضافه‌اش کنی.",
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
                lineHeight = AppTypography.bodyDescription.lineHeight,
            )
            Text(
                "بدون ضبط مکالمه؛ فقط برای اینکه هیچ پیگیری‌ای فراموش نشود.",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                DfPrimaryButton(
                    text = "فعال‌کردن دستیار",
                    onClick = onEnable,
                    modifier = Modifier.weight(1f),
                )
                DfSecondaryButton(
                    text = "فعلاً نه",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
