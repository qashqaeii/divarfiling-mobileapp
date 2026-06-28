package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DealsStatsRow(
    activeCount: Int,
    pipelineValueLabel: String,
    weightedForecastLabel: String,
    closedCommissionLabel: String,
    closingRate: Int,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        DealsStatCard(
            value = numberFormat.format(activeCount),
            label = "فعال معامله",
            icon = DfIcons.Zap,
            background = DfColors.AmberLight,
            iconTint = DfColors.Amber,
        )
        DealsStatCard(
            value = pipelineValueLabel,
            label = "ارزش pipeline",
            icon = DfIcons.BarChart,
            background = DfColors.BlueLight,
            iconTint = DfColors.Blue,
        )
        DealsStatCard(
            value = weightedForecastLabel,
            label = "پیش‌بینی وزنی",
            icon = DfIcons.Scale,
            background = DfColors.PurpleContainer,
            iconTint = DfColors.Purple,
        )
        DealsStatCard(
            value = closedCommissionLabel,
            label = "کمیسیون بسته",
            icon = DfIcons.Coins,
            background = DfColors.GreenLight,
            iconTint = DfColors.Green,
        )
        DealsStatCard(
            value = "${numberFormat.format(closingRate)}%",
            label = "نرخ بستن معاملات",
            icon = DfIcons.Trophy,
            background = DfColors.PurpleContainer,
            iconTint = DfColors.Purple,
        )
    }
}

@Composable
private fun DealsStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    background: Color,
    iconTint: Color,
) {
    Surface(
        shape = AppShapes.Card,
        color = background,
        shadowElevation = AppElevations.none,
        modifier = Modifier.size(width = 132.dp, height = 108.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
