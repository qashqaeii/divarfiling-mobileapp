package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfBottomSheetPicker

data class ScheduleIntervalOption(
    val label: String,
    val hours: Double,
)

val ScheduleIntervalOptions = listOf(
    ScheduleIntervalOption("هر ۱۵ دقیقه", 0.25),
    ScheduleIntervalOption("هر ۳۰ دقیقه", 0.5),
    ScheduleIntervalOption("هر ۱ ساعت", 1.0),
    ScheduleIntervalOption("هر ۲ ساعت", 2.0),
    ScheduleIntervalOption("هر ۶ ساعت", 6.0),
    ScheduleIntervalOption("هر ۱۲ ساعت", 12.0),
    ScheduleIntervalOption("هر ۲۴ ساعت", 24.0),
)

fun scheduleLabelForHours(hours: Double): String =
    ScheduleIntervalOptions.firstOrNull { it.hours == hours }?.label
        ?: "هر ${hours.toInt()} ساعت"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleIntervalField(
    selectedHours: Double,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = DfColors.SurfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("فاصله زمان‌بندی", style = MaterialTheme.typography.labelMedium, color = DfColors.TextMuted)
                Text(
                    scheduleLabelForHours(selectedHours),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) DfColors.TextPrimary else DfColors.TextMuted,
                )
            }
            Icon(Icons.Default.Schedule, contentDescription = null, tint = DfColors.Purple)
        }
    }
}

@Composable
fun ScheduleIntervalBottomSheet(
    visible: Boolean,
    selectedHours: Double,
    onSelect: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    DfBottomSheetPicker(
        visible = visible,
        title = "انتخاب فاصله زمان‌بندی",
        onDismiss = onDismiss,
    ) {
        ScheduleIntervalOptions.forEach { option ->
            val selected = option.hours == selectedHours
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelect(option.hours)
                        onDismiss()
                    },
                shape = MaterialTheme.shapes.medium,
                color = if (selected) DfColors.PurpleContainer else DfColors.SurfaceVariant,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) DfColors.PurpleDark else DfColors.TextPrimary,
                    )
                    if (selected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = DfColors.Purple)
                    }
                }
            }
        }
    }
}
