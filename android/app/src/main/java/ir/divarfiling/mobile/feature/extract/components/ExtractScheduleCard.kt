package ir.divarfiling.mobile.feature.extract.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfGlassButton
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant

data class ScheduleChipOption(
    val label: String,
    val hours: Double,
    val icon: ImageVector,
)

val ExtractScheduleChipOptions = listOf(
    ScheduleChipOption("۱۵ دقیقه", 0.25, DfIcons.Zap),
    ScheduleChipOption("۳۰ دقیقه", 0.5, DfIcons.Timer),
    ScheduleChipOption("۱ ساعت", 1.0, DfIcons.Clock),
    ScheduleChipOption("۲ ساعت", 2.0, DfIcons.Clock),
    ScheduleChipOption("۶ ساعت", 6.0, DfIcons.Cloud),
    ScheduleChipOption("۱۲ ساعت", 12.0, DfIcons.Moon),
    ScheduleChipOption("۲۴ ساعت", 24.0, DfIcons.Calendar),
)

fun scheduleDescriptionForHours(hours: Double): String = when (hours) {
    0.25 -> "هر ۱۵ دقیقه یکبار استخراج به صورت خودکار انجام خواهد شد."
    0.5 -> "هر ۳۰ دقیقه یکبار استخراج به صورت خودکار انجام خواهد شد."
    1.0 -> "هر ۱ ساعت یکبار استخراج به صورت خودکار انجام خواهد شد."
    2.0 -> "هر ۲ ساعت یکبار استخراج به صورت خودکار انجام خواهد شد."
    12.0 -> "هر ۱۲ ساعت یکبار استخراج به صورت خودکار انجام خواهد شد."
    24.0 -> "هر ۲۴ ساعت یکبار استخراج به صورت خودکار انجام خواهد شد."
    else -> "هر ${hours.toInt()} ساعت یکبار استخراج به صورت خودکار انجام خواهد شد."
}

@Composable
fun ExtractScheduleCard(
    selectedHours: Double,
    enabled: Boolean,
    onSelect: (Double) -> Unit,
    onOpenSchedules: () -> Unit,
    onCreateSchedule: () -> Unit,
    canCreateSchedule: Boolean,
    modifier: Modifier = Modifier,
) {
    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ExtractSectionTitle(title = "زمان‌بندی استخراج", icon = DfIcons.Clock)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                ExtractScheduleChipOptions.forEach { option ->
                    ScheduleIntervalChip(
                        label = option.label,
                        icon = option.icon,
                        selected = option.hours == selectedHours,
                        enabled = enabled,
                        onClick = { onSelect(option.hours) },
                    )
                }
            }

            Text(
                text = scheduleDescriptionForHours(selectedHours),
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                DfGlassButton(
                    text = "مدیریت",
                    onClick = onOpenSchedules,
                    icon = DfIcons.Clock,
                    modifier = Modifier.weight(1f),
                )
                DfGlassButton(
                    text = "ذخیره",
                    onClick = onCreateSchedule,
                    icon = DfIcons.Check,
                    variant = DfGlassButtonVariant.Primary,
                    enabled = canCreateSchedule,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleIntervalChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.ButtonPill,
        color = if (selected) DfColors.Purple else DfColors.SurfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else DfColors.TextMuted,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else DfColors.TextSecondary,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ExtractScheduleCardPreview() {
    DivarFilingTheme {
        ExtractScheduleCard(
            selectedHours = 1.0,
            enabled = true,
            onSelect = {},
            onOpenSchedules = {},
            onCreateSchedule = {},
            canCreateSchedule = true,
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
