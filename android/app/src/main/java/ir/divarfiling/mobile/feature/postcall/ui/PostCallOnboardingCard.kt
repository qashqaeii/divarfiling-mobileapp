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
import ir.divarfiling.mobile.core.design.DfColors
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
                "بعد از تماس تلفنی، شماره را با CRM تطبیق دهید و سریع یادداشت یا سرنخ ثبت کنید.",
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                DfPrimaryButton(
                    text = "فعال‌سازی",
                    onClick = onEnable,
                    modifier = Modifier.weight(1f),
                )
                DfSecondaryButton(
                    text = "فعلاً نه",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                "تا زمانی که فعال نکنید، مجوز تماس درخواست نمی‌شود.",
                style = AppTypography.labelSmall,
                color = DfColors.Purple,
            )
        }
    }
}
