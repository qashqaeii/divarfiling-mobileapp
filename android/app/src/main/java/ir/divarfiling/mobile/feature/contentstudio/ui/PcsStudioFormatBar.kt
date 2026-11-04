package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors

@Composable
fun PcsStudioFormatBar(
    selectedFormat: String,
    onSelectFormat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formats = listOf(
        "story" to "استوری",
        "post" to "پست",
        "square" to "مربع",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Field)
            .background(DfThemeColors.surfaceVariant().copy(alpha = 0.45f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        formats.forEach { (id, label) ->
            val selected = selectedFormat == id
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(AppShapes.Chip)
                    .background(
                        if (selected) DfThemeColors.surface() else androidx.compose.ui.graphics.Color.Transparent,
                    )
                    .clickable { onSelectFormat(id) }
                    .padding(vertical = AppSpacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = AppTypography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) DfThemeColors.primary() else DfThemeColors.textSecondary(),
                )
            }
        }
    }
}
