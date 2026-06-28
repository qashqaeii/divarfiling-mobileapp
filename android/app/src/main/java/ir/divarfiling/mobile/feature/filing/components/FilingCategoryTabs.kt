package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

data class FilingCategoryTab(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

val FilingCategoryTabs = listOf(
    FilingCategoryTab("all", "همه فایل‌ها", DfIcons.Layers),
    FilingCategoryTab("residential", "املاک مسکونی", DfIcons.Home),
    FilingCategoryTab("commercial", "املاک تجاری", DfIcons.Building),
    FilingCategoryTab("land", "زمین و ویلا", DfIcons.Map),
    FilingCategoryTab("favorites", "مورد علاقه", DfIcons.Star),
)

@Composable
fun FilingCategoryTabsRow(
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        FilingCategoryTabs.forEach { tab ->
            FilingCategoryChip(
                tab = tab,
                selected = tab.id == selectedTabId,
                onClick = { onTabSelected(tab.id) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilingCategoryChip(
    tab: FilingCategoryTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.ButtonPill,
        color = if (selected) DfColors.PurpleContainer else DfColors.Surface,
        shadowElevation = if (selected) 0.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = if (selected) DfColors.Purple else DfColors.TextMuted,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = tab.label,
                style = AppTypography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) DfColors.Purple else DfColors.TextSecondary,
            )
        }
    }
}
