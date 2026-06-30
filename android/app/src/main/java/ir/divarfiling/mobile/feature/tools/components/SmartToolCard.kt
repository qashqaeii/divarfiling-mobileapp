package ir.divarfiling.mobile.feature.tools.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
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
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
        tonalElevation = AppElevations.none,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppSpacing.sm,
                        end = AppSpacing.cardPadding,
                        top = AppSpacing.cardPadding,
                        bottom = AppSpacing.cardPadding,
                    ),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmartToolIllustration(toolId = tool.id)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
                ) {
                    Text(
                        text = tool.title,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = tool.subtitle,
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Icon(
                    imageVector = DfIcons.ChevronLeft,
                    contentDescription = null,
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }

            Surface(
                shape = AppShapes.CardSmall,
                color = tool.background,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(AppSpacing.sm)
                    .size(28.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = numberFormat.format(tool.number),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = tool.tint,
                    )
                }
            }
        }
    }
}

@Composable
fun SmartToolIllustration(
    toolId: SmartToolId,
    modifier: Modifier = Modifier,
) {
    val (tint, background, iconRes) = when (toolId) {
        SmartToolId.RentCommission -> Triple(DfColors.Purple, DfColors.PurpleContainer, DfDecorIcons.Calculator)
        SmartToolId.DepositConvert -> Triple(DfColors.Blue, DfColors.BlueLight, DfDecorIcons.RotateCcw)
        SmartToolId.Compare -> Triple(DfColors.Green, DfColors.GreenLight, DfDecorIcons.Scale)
        SmartToolId.AreaPrice -> Triple(DfColors.Amber, DfColors.AmberLight, DfDecorIcons.BarChart)
        SmartToolId.Discount -> Triple(DfColors.Pink, DfColors.PinkLight, DfDecorIcons.Percent)
        SmartToolId.SalesCommission -> Triple(Color(0xFF0D9488), Color(0xFFCCFBF1), DfDecorIcons.Calculator)
    }
    ToolIllustrationBox(
        modifier = modifier,
        tint = tint,
        background = background,
        iconRes = iconRes,
    )
}

@Composable
private fun ToolIllustrationBox(
    tint: Color,
    background: Color,
    iconRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(width = 76.dp, height = 72.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.1f)),
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(background, Color.White),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            DfDecorImage(resId = iconRes, size = 36.dp)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SmartToolCardPreview() {
    DivarFilingTheme {
        SmartToolCard(
            tool = smartToolsCatalog.first(),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
