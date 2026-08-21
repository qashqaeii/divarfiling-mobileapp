package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors

@Composable
fun LicenseGateBanner(
    message: String,
    onBuyLicense: () -> Unit,
    onOpenDashboard: () -> Unit,
    onRefresh: (() -> Unit)? = null,
    buyLabel: String = "خرید لایسنس",
    title: String = "لایسنس فعال نیست",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfStatusBanner(
            message = message,
            tone = DfStatusTone.Locked,
            title = title,
            icon = DfIcons.Lock,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "هر لایسنس شامل یک ربات ویندوز و یک اپ اندروید است — پس از خرید، با همین حساب وارد شوید.",
            style = AppTypography.meta,
            color = DfThemeColors.textMuted(),
            modifier = Modifier.padding(horizontal = AppSpacing.xxs),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            DfPrimaryButton(
                text = buyLabel,
                onClick = onBuyLicense,
                modifier = Modifier.weight(1f),
            )
            DfSecondaryButton(
                text = "داشبورد",
                onClick = onOpenDashboard,
                modifier = Modifier.weight(1f),
            )
        }
        onRefresh?.let { refresh ->
            DfTextButton(
                text = "بررسی مجدد وضعیت",
                onClick = refresh,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
