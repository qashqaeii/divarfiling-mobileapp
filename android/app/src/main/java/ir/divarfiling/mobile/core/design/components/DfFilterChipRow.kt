package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing

data class DfFilterOption<T>(
    val value: T,
    val label: String,
)

@Composable
fun <T> DfFilterChipRow(
    options: List<DfFilterOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            DfGlassChip(
                text = option.label,
                selected = isSelected,
                onClick = { onSelect(option.value) },
            )
        }
    }
}
