package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.feature.contentstudio.PcsStudioToolTab

@Composable
fun PcsStudioToolbar(
    activeTool: PcsStudioToolTab?,
    onToolSelected: (PcsStudioToolTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        PcsStudioToolTab.Templates to "قالب‌ها",
        PcsStudioToolTab.Images to "تصاویر",
        PcsStudioToolTab.Text to "متن",
        PcsStudioToolTab.Layers to "لایه‌ها",
        PcsStudioToolTab.Brand to "برند",
        PcsStudioToolTab.Ai to "هوش مصنوعی",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        tabs.forEach { (tab, label) ->
            FilterChip(
                selected = activeTool == tab,
                onClick = { onToolSelected(tab) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DfThemeColors.primary().copy(alpha = 0.15f),
                ),
            )
        }
    }
}
