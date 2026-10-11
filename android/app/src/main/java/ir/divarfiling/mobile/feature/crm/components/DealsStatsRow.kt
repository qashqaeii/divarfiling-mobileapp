package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
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
    val stats = listOf(
        DealsStatItem("فعال", numberFormat.format(activeCount), DfThemeColors.warning(), DfThemeColors.warningContainer(), icon = DfIcons.Zap),
        DealsStatItem("Pipeline", pipelineValueLabel, DfThemeColors.info(), DfThemeColors.infoContainer(), iconRes = DfDecorIcons.BarChart),
        DealsStatItem("پیش‌بینی", weightedForecastLabel, DfThemeColors.primary(), DfThemeColors.primaryContainer(), iconRes = DfDecorIcons.Scale),
        DealsStatItem("کمیسیون", closedCommissionLabel, DfThemeColors.success(), DfThemeColors.successContainer(), icon = DfIcons.Coins),
        DealsStatItem("نرخ بستن", "${numberFormat.format(closingRate)}٪", DfThemeColors.primary(), DfThemeColors.surfaceVariant(), icon = DfIcons.Trophy),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = "خلاصه معاملات",
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = DfThemeColors.textPrimary(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            stats.forEach { stat ->
                DealsStatPill(stat = stat)
            }
        }
    }
}

private data class DealsStatItem(
    val label: String,
    val value: String,
    val accent: Color,
    val container: Color,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
)

@Composable
private fun DealsStatPill(stat: DealsStatItem) {
    Surface(
        shape = AppShapes.CardSmall,
        color = stat.container.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, stat.accent.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                stat.iconRes != null -> Icon(
                    painter = painterResource(stat.iconRes),
                    contentDescription = null,
                    tint = stat.accent,
                    modifier = Modifier.size(16.dp),
                )
                stat.icon != null -> Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stat.value,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stat.label,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                    maxLines = 1,
                )
            }
        }
    }
}
