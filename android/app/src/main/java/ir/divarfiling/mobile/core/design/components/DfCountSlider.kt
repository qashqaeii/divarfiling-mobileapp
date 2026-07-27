package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.license.ExtractLightLimits
import kotlin.math.roundToInt

@Composable
fun DfCountSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "تعداد آگهی",
    valueRange: ClosedFloatingPointRange<Float> = 0f..ExtractLightLimits.MAX_ITEMS.toFloat(),
    enabled: Boolean = true,
) {
    val max = valueRange.endInclusive.roundToInt().coerceAtLeast(1)
    val stepSize = when {
        max <= 50 -> 1
        max <= 100 -> 2
        else -> 5
    }
    val steps = ((max / stepSize) - 1).coerceAtLeast(0)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = DfColors.TextSecondary)
            Text(
                "$value",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DfColors.Purple,
            )
        }
        Slider(
            value = value.toFloat().coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = {
                val snapped = ((it / stepSize).roundToInt() * stepSize)
                    .coerceIn(valueRange.start.roundToInt(), max)
                onValueChange(snapped)
            },
            valueRange = valueRange,
            enabled = enabled,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = DfColors.Purple,
                activeTrackColor = DfColors.Purple,
                inactiveTrackColor = DfColors.PurpleContainer,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("۰", style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
            Text("${max / 2}", style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
            Text("$max", style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
        }
    }
}
