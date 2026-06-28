package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.DealDto
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealListCard(
    deal: DealDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stage = deal.stage ?: "سرنخ"
    val stageColors = dealStageColors(stage)
    val progress = DealsFilters.progressPercent(deal) / 100f
    val (jalaliDate, jalaliTime) = DealsFilters.splitDateTime(deal.updatedAt)
    val accent = dealAccentColor(deal.customerName ?: deal.title)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(stageColors.first),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.72f)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = DealsFilters.customerInitials(deal.customerName),
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = deal.title,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        deal.customerName?.takeIf { it.isNotBlank() }?.let { name ->
                            Text(
                                text = name,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        deal.propertyTitle?.takeIf { it.isNotBlank() }?.let { property ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = DfIcons.Building,
                                    contentDescription = null,
                                    tint = DfColors.TextMuted,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = property,
                                    style = AppTypography.labelSmall,
                                    color = DfColors.TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    DealStageBadge(stage = stage, colors = stageColors)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    deal.amount?.let { amount ->
                        Text(
                            text = FormatUtils.formatPriceShort(amount) + " تومان",
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.Purple,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } ?: Text(
                        text = "بدون مبلغ",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = jalaliDate,
                            style = AppTypography.labelSmall,
                            color = DfColors.TextSecondary,
                            maxLines = 1,
                        )
                        if (jalaliTime != "—") {
                            Text(
                                text = jalaliTime,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextMuted,
                                maxLines = 1,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(AppShapes.Chip),
                        color = stageColors.first,
                        trackColor = DfColors.SurfaceVariant,
                    )
                    Text(
                        text = "${DealsFilters.progressPercent(deal)}٪",
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = stageColors.first,
                    )
                }
            }
        }
    }
}

@Composable
private fun DealStageBadge(
    stage: String,
    colors: Pair<Color, Color>,
) {
    Surface(shape = AppShapes.Chip, color = colors.second) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.first),
            )
            Text(
                text = stage,
                style = AppTypography.labelSmall,
                color = colors.first,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun dealAccentColor(seed: String): Color {
    val palette = listOf(DfColors.Purple, DfColors.Blue, DfColors.Green, DfColors.Amber, DfColors.Rose)
    return palette[seed.hashCode().absoluteValue % palette.size]
}

private fun dealStageColors(stage: String): Pair<Color, Color> = when {
    stage.contains("از دست") || stage.contains("سرد") -> DfColors.OverdueAccent to DfColors.RoseLight
    stage.contains("بسته") -> DfColors.Green to DfColors.GreenLight
    stage.contains("قرارداد") || stage.contains("پیش") -> Color(0xFFEC4899) to Color(0xFFFCE7F3)
    stage.contains("بازدید") -> DfColors.Blue to DfColors.BlueLight
    stage.contains("مذاکره") -> DfColors.Amber to DfColors.AmberLight
    stage == "سرنخ" || stage == "جدید" -> DfColors.Blue to DfColors.BlueLight
    else -> DfColors.Purple to DfColors.PurpleContainer
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DealListCardPreview() {
    DivarFilingTheme {
        DealListCard(
            deal = DealDto(
                id = 1,
                title = "فروش آپارتمان ۱۲۰ متری",
                stage = "مذاکره",
                amount = 12_500_000_000,
                customerName = "رضا محمدی",
                propertyTitle = "ولنجک — ۱۲۰ متر",
                probability = 40,
                updatedAt = "2026-06-28T14:30:00Z",
            ),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
