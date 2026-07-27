package ir.divarfiling.mobile.feature.tools.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.feature.tools.SmartTool
import ir.divarfiling.mobile.feature.tools.SmartToolId
import ir.divarfiling.mobile.feature.tools.smartToolsCatalog
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartToolCard(
    tool: SmartTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    DfCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        containerColor = DfThemeColors.surface(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartToolIllustration(toolId = tool.id)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tool.title,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(AppShapes.Chip)
                            .background(tool.background),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = numberFormat.format(tool.number),
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tool.tint,
                        )
                    }
                }
                Text(
                    text = tool.subtitle,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfThemeColors.textMuted(),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun SmartToolIllustration(
    toolId: SmartToolId,
    modifier: Modifier = Modifier,
) {
    val (_, background, iconRes) = when (toolId) {
        SmartToolId.RentCommission -> Triple(AppColors.Purple, AppColors.PurpleContainer, DfDecorIcons.Calculator)
        SmartToolId.DepositConvert -> Triple(AppColors.Blue, AppColors.BlueLight, DfDecorIcons.RotateCcw)
        SmartToolId.Compare -> Triple(AppColors.Green, AppColors.GreenLight, DfDecorIcons.Scale)
        SmartToolId.AreaPrice -> Triple(AppColors.Amber, AppColors.AmberLight, DfDecorIcons.BarChart)
        SmartToolId.Discount -> Triple(AppColors.Pink, AppColors.PinkLight, DfDecorIcons.Percent)
        SmartToolId.SalesCommission -> Triple(AppColors.Green, AppColors.GreenLight, DfDecorIcons.Calculator)
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(AppShapes.IconContainer)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        DfDecorImage(resId = iconRes, size = 28.dp)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SmartToolCardPreview() {
    DivarFilingTheme {
        SmartToolCard(
            tool = smartToolsCatalog.first(),
            onClick = {},
            modifier = Modifier,
        )
    }
}
