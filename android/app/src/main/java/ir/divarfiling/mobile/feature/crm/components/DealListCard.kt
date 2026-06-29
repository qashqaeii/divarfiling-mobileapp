package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealListCard(
    deal: DealDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stage = deal.stage ?: "سرنخ"
    val stageColors = DealUiUtils.dealStageColors(stage)
    val progress = DealsFilters.progressPercent(deal)
    val (jalaliDate, jalaliTime) = DealsFilters.splitDateTime(deal.updatedAt)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(stageColors.first, stageColors.first.copy(alpha = 0.4f)),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.size(48.dp),
                            color = stageColors.first,
                            trackColor = DfColors.SurfaceVariant,
                            strokeWidth = 3.dp,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = DealsFilters.customerInitials(deal.customerName),
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = stageColors.first,
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
                    }
                    DealStageBadge(stage = stage, colors = stageColors)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    deal.amount?.let { amount ->
                        Surface(shape = AppShapes.Chip, color = DfColors.PurpleContainer.copy(alpha = 0.65f)) {
                            Text(
                                text = FormatUtils.formatPriceShort(amount) + " تومان",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DfColors.Purple,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } ?: Text(
                        text = "بدون مبلغ",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                    deal.commissionAmount?.takeIf { it > 0 }?.let { commission ->
                        Surface(shape = AppShapes.Chip, color = DfColors.GreenLight) {
                            Text(
                                text = "کمیسیون ${FormatUtils.formatPriceShort(commission)}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DfColors.Green,
                                maxLines = 1,
                            )
                        }
                    }
                }

                HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.12f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.Percent,
                            contentDescription = null,
                            tint = stageColors.first,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "احتمال بسته‌شدن $progress٪",
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = stageColors.first,
                        )
                    }
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
                commissionAmount = 125_000_000,
                updatedAt = "2026-06-28T14:30:00Z",
            ),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
