package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant

@Composable
fun LicenseGateBanner(
    message: String,
    onBuyLicense: () -> Unit,
    onOpenDashboard: () -> Unit,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.RoseLight,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = DfIcons.Sparkles,
                    contentDescription = null,
                    tint = DfColors.Rose,
                    modifier = Modifier.size(20.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "لایسنس فعال نیست",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Rose,
                    )
                    Text(
                        text = message,
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                    )
                }
            }
            Text(
                text = "هر لایسنس شامل یک ربات ویندوز و یک اپ اندروید است — پس از خرید، با همین حساب وارد اپ شوید.",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                DfGlassButton(
                    text = "خرید لایسنس",
                    onClick = onBuyLicense,
                    icon = DfIcons.ExternalLink,
                    variant = DfGlassButtonVariant.Primary,
                    accent = DfColors.Rose,
                    modifier = Modifier.weight(1f),
                )
                DfGlassButton(
                    text = "داشبورد",
                    onClick = onOpenDashboard,
                    icon = DfIcons.User,
                    modifier = Modifier.weight(1f),
                )
            }
            onRefresh?.let { refresh ->
                DfGlassButton(
                    text = "بررسی مجدد وضعیت",
                    onClick = refresh,
                    icon = DfIcons.RefreshCw,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
