package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
fun TodayStatsRow(
    todayCount: Int,
    doneCount: Int,
    overdueCount: Int,
    onTodayClick: () -> Unit,
    onDoneClick: () -> Unit,
    onOverdueClick: () -> Unit,
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
        TodayStatCard(
            value = numberFormat.format(overdueCount),
            label = "معوق",
            icon = DfIcons.Clock,
            background = DfColors.GreenLight,
            iconTint = DfColors.Green,
            onViewClick = onOverdueClick,
        )
        TodayStatCard(
            value = numberFormat.format(doneCount),
            label = "انجام‌شده",
            icon = DfIcons.CircleCheck,
            background = DfColors.AmberLight,
            iconTint = DfColors.Amber,
            onViewClick = onDoneClick,
        )
        TodayStatCard(
            value = numberFormat.format(todayCount),
            label = "امروز",
            icon = DfIcons.Calendar,
            background = DfColors.PurpleContainer,
            iconTint = DfColors.Purple,
            onViewClick = onTodayClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    background: Color,
    iconTint: Color,
    onViewClick: () -> Unit,
) {
    Surface(
        shape = AppShapes.Card,
        color = background,
        shadowElevation = AppElevations.none,
        modifier = Modifier.size(width = 118.dp, height = 112.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = value,
                style = AppTypography.pageTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
            Surface(
                onClick = onViewClick,
                shape = AppShapes.Chip,
                color = Color.White.copy(alpha = 0.65f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "مشاهده",
                        style = AppTypography.labelSmall,
                        color = iconTint,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = DfIcons.ChevronLeft,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}
