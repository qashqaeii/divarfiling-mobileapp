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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.DealDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealListCard(
    deal: DealDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = DealsFilters.progressPercent(deal) / 100f
    val (date, time) = DealsFilters.splitDateTime(deal.updatedAt)
    val stage = deal.stage ?: "—"
    val stageColors = dealStageColors(stage)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = DfIcons.Handshake,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(18.dp),
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(AppShapes.IconContainer)
                    .background(DfColors.PurpleContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = DealsFilters.customerInitials(deal.customerName),
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.PurpleDark,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = deal.title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                deal.customerName?.let {
                    Text(
                        text = it,
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Surface(shape = AppShapes.Chip, color = stageColors.second) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(stageColors.first),
                    )
                    Text(
                        text = stage,
                        style = AppTypography.labelSmall,
                        color = stageColors.first,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1.1f),
            ) {
                deal.amount?.let {
                    Text(
                        text = FormatUtils.formatPriceShort(it) + " تومان",
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                        maxLines = 1,
                    )
                }
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape4),
                    color = stageColors.first,
                    trackColor = DfColors.SurfaceVariant,
                )
                Text(
                    text = "${DealsFilters.progressPercent(deal)}%",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = DfIcons.Calendar,
                    contentDescription = null,
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(12.dp),
                )
                Text(text = date, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                Text(text = time, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = DfIcons.MoreVertical,
                    contentDescription = "بیشتر",
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealGridCard(
    deal: DealDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stage = deal.stage ?: "—"
    val stageColors = dealStageColors(stage)
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = deal.title,
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            deal.amount?.let {
                Text(
                    text = FormatUtils.formatPriceShort(it) + " تومان",
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Surface(shape = AppShapes.Chip, color = stageColors.second) {
                Text(
                    text = stage,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = AppTypography.labelSmall,
                    color = stageColors.first,
                )
            }
        }
    }
}

private val RoundedCornerShape4 = AppShapes.Chip

private fun dealStageColors(stage: String): Pair<Color, Color> = when {
    stage.contains("از دست") || stage.contains("سرد") -> DfColors.OverdueAccent to DfColors.RoseLight
    stage.contains("بسته") -> DfColors.Green to DfColors.GreenLight
    stage.contains("قرارداد") || stage.contains("پیش") -> Color(0xFFEC4899) to Color(0xFFFCE7F3)
    stage.contains("بازدید") -> DfColors.Blue to DfColors.BlueLight
    stage.contains("مذاکره") -> DfColors.Amber to DfColors.AmberLight
    stage == "سرنخ" || stage == "جدید" -> DfColors.Blue to DfColors.BlueLight
    else -> DfColors.Purple to DfColors.PurpleContainer
}
