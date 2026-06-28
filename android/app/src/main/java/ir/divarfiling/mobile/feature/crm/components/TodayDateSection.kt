package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TodayDateSection(
    dateLabel: String,
    totalCount: Int,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "کارهای امروز",
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
            Text(
                text = "جمع کل: ${numberFormat.format(totalCount)} مورد",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TodayHeaderIconButton(
                icon = DfIcons.ChevronLeft,
                contentDescription = "روز قبل",
                onClick = onDateClick,
            )
            TodayDatePill(dateLabel = dateLabel, onClick = onDateClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayDatePill(
    dateLabel: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.ButtonPill,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = DfIcons.Calendar,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = TodayTaskLabels.formatDisplayDate(dateLabel),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.Purple,
            )
            Icon(
                imageVector = DfIcons.ChevronDown,
                contentDescription = null,
                tint = DfColors.TextMuted,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayHeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.IconContainer,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
        modifier = Modifier.size(36.dp),
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = DfColors.TextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
