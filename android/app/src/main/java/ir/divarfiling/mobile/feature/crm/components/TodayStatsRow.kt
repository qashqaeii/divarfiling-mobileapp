package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TodayStatsRow(
    todayCount: Int,
    doneCount: Int,
    overdueCount: Int,
    selectedTab: TodayFilterTab,
    onTodayClick: () -> Unit,
    onDoneClick: () -> Unit,
    onOverdueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    val stats = listOf(
        TodayStatCell(
            label = "معوق",
            value = numberFormat.format(overdueCount),
            color = DfColors.OverdueAccent,
            icon = DfIcons.Clock,
            tab = TodayFilterTab.Overdue,
            onClick = onOverdueClick,
        ),
        TodayStatCell(
            label = "انجام‌شده",
            value = numberFormat.format(doneCount),
            color = DfColors.Green,
            icon = DfIcons.CircleCheck,
            tab = TodayFilterTab.Done,
            onClick = onDoneClick,
        ),
        TodayStatCell(
            label = "امروز",
            value = numberFormat.format(todayCount),
            color = DfColors.Purple,
            icon = DfIcons.ListTodo,
            tab = TodayFilterTab.All,
            onClick = onTodayClick,
        ),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        shape = AppShapes.Field,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            stats.forEachIndexed { index, stat ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .padding(vertical = 6.dp)
                            .background(DfColors.Outline.copy(alpha = 0.45f)),
                    )
                }
                TodayMiniStatCell(
                    label = stat.label,
                    value = stat.value,
                    color = stat.color,
                    icon = stat.icon,
                    selected = selectedTab == stat.tab,
                    onClick = stat.onClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class TodayStatCell(
    val label: String,
    val value: String,
    val color: Color,
    val icon: ImageVector,
    val tab: TodayFilterTab,
    val onClick: () -> Unit,
)

@Composable
private fun TodayMiniStatCell(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(AppShapes.Field)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) color.copy(alpha = 0.14f) else DfColors.SurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) color else DfColors.TextMuted,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = value,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (selected) color else DfColors.TextPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = if (selected) color else DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TodayStatsRowPreview() {
    DivarFilingTheme {
        TodayStatsRow(
            todayCount = 20,
            doneCount = 5,
            overdueCount = 3,
            selectedTab = TodayFilterTab.All,
            onTodayClick = {},
            onDoneClick = {},
            onOverdueClick = {},
        )
    }
}
