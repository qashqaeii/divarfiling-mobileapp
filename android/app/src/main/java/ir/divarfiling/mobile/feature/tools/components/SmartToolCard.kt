package ir.divarfiling.mobile.feature.tools.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    when (toolId) {
        SmartToolId.RentCommission -> ToolIllustrationStack(
            modifier = modifier,
            tint = DfColors.Purple,
            background = DfColors.PurpleContainer,
            primaryIcon = DfIcons.Calculator,
            secondaryIcon = DfIcons.Percent,
            accent = Color(0xFF8B5CF6),
        )
        SmartToolId.DepositConvert -> ToolIllustrationStack(
            modifier = modifier,
            tint = DfColors.Blue,
            background = DfColors.BlueLight,
            primaryIcon = DfIcons.Building,
            secondaryIcon = DfIcons.RotateCcw,
            accent = Color(0xFF3B82F6),
        )
        SmartToolId.Compare -> ToolIllustrationStack(
            modifier = modifier,
            tint = DfColors.Green,
            background = DfColors.GreenLight,
            primaryIcon = DfIcons.Scale,
            secondaryIcon = DfIcons.LayoutGrid,
            accent = Color(0xFF059669),
        )
        SmartToolId.AreaPrice -> ToolIllustrationStack(
            modifier = modifier,
            tint = DfColors.Amber,
            background = DfColors.AmberLight,
            primaryIcon = DfIcons.Ruler,
            secondaryIcon = DfIcons.BarChart,
            accent = Color(0xFFD97706),
        )
        SmartToolId.Discount -> ToolIllustrationStack(
            modifier = modifier,
            tint = DfColors.Pink,
            background = DfColors.PinkLight,
            primaryIcon = DfIcons.Tag,
            secondaryIcon = DfIcons.Percent,
            accent = Color(0xFFDB2777),
        )
        SmartToolId.SalesCommission -> ToolIllustrationStack(
            modifier = modifier,
            tint = Color(0xFF0D9488),
            background = Color(0xFFCCFBF1),
            primaryIcon = DfIcons.Coins,
            secondaryIcon = DfIcons.TrendingUp,
            accent = Color(0xFF14B8A6),
        )
    }
}

@Composable
private fun ToolIllustrationStack(
    tint: Color,
    background: Color,
    primaryIcon: ImageVector,
    secondaryIcon: ImageVector,
    accent: Color,
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
                .size(48.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            background,
                            Color.White,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = primaryIcon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-2).dp, y = 4.dp)
                .size(20.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = secondaryIcon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(11.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 4.dp, y = (-4).dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.25f)),
        )
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
