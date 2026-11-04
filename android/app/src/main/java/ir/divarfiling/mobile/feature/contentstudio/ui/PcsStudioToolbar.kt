package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.feature.contentstudio.PcsStudioToolTab

@Composable
fun PcsStudioToolbar(
    activeTool: PcsStudioToolTab?,
    onToolSelected: (PcsStudioToolTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        PcsStudioToolTab.Templates to ("قالب‌ها" to DfIcons.LayoutGrid),
        PcsStudioToolTab.Images to ("تصاویر" to DfIcons.Upload),
        PcsStudioToolTab.Text to ("متن" to DfIcons.Pencil),
        PcsStudioToolTab.Layers to ("لایه‌ها" to DfIcons.Layers),
        PcsStudioToolTab.Brand to ("برند" to DfIcons.Briefcase),
        PcsStudioToolTab.Ai to ("هوش مصنوعی" to DfIcons.Sparkles),
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.BottomNav,
        color = DfThemeColors.surface(),
        tonalElevation = 1.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            tabs.forEach { (tab, labelIcon) ->
                val (label, icon) = labelIcon
                PcsStudioToolChip(
                    label = label,
                    icon = icon,
                    selected = activeTool == tab,
                    onClick = { onToolSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun PcsStudioToolChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) DfThemeColors.primaryContainer().copy(alpha = 0.55f) else DfThemeColors.surfaceVariant().copy(alpha = 0.35f)
    val content = if (selected) DfThemeColors.primary() else DfThemeColors.textSecondary()
    Column(
        modifier = Modifier
            .clip(AppShapes.Chip)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(icon, contentDescription = label, tint = content, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = AppTypography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
