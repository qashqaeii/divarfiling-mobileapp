package ir.divarfiling.mobile.feature.crm.components

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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
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
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = AppShapes.IconContainer,
                color = background,
                modifier = Modifier.size(40.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
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
