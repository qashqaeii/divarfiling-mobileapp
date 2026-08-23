package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@Composable
fun ContactsStatsRow(
    todayCount: Int,
    newCount: Int,
    followUpCount: Int,
    totalCount: Int,
    selectedFilter: ContactsFilters.QuickFilter,
    onFilterSelect: (ContactsFilters.QuickFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats = listOf(
        ContactsStatItem("کل", totalCount, DfThemeColors.primary(), DfThemeColors.primaryContainer(), DfIcons.Users, ContactsFilters.QuickFilter.ALL),
        ContactsStatItem("پیگیری", followUpCount, DfThemeColors.success(), DfThemeColors.successContainer(), DfIcons.RefreshCw, ContactsFilters.QuickFilter.FOLLOW_UP),
        ContactsStatItem("جدید", newCount, DfThemeColors.info(), DfThemeColors.infoContainer(), DfIcons.UserPlus, ContactsFilters.QuickFilter.NEW),
        ContactsStatItem("امروز", todayCount, DfThemeColors.warning(), DfThemeColors.warningContainer(), DfIcons.Calendar, ContactsFilters.QuickFilter.TODAY),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "نمای کلی",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
            )
            if (selectedFilter != ContactsFilters.QuickFilter.ALL) {
                Text(
                    text = "برای برداشتن فیلتر دوباره لمس کنید",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.primary(),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            stats.forEach { stat ->
                ContactsStatChip(
                    stat = stat,
                    selected = selectedFilter == stat.filter,
                    onClick = {
                        onFilterSelect(
                            if (selectedFilter == stat.filter) {
                                ContactsFilters.QuickFilter.ALL
                            } else {
                                stat.filter
                            },
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class ContactsStatItem(
    val label: String,
    val value: Int,
    val accent: Color,
    val container: Color,
    val icon: ImageVector,
    val filter: ContactsFilters.QuickFilter,
)

@Composable
private fun ContactsStatChip(
    stat: ContactsStatItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = if (selected) stat.container else DfThemeColors.surface(),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) stat.accent.copy(alpha = 0.45f) else DfThemeColors.outlineSubtle(),
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = if (selected) stat.accent else DfThemeColors.textMuted(),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = DateUtils.toPersianDigits(stat.value.toString()),
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) stat.accent else DfThemeColors.textPrimary(),
                maxLines = 1,
            )
            Text(
                text = stat.label,
                style = AppTypography.labelSmall,
                color = if (selected) stat.accent else DfThemeColors.textMuted(),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ContactsStatsRowPreview() {
    DivarFilingTheme {
        ContactsStatsRow(
            todayCount = 4,
            newCount = 12,
            followUpCount = 28,
            totalCount = 248,
            selectedFilter = ContactsFilters.QuickFilter.NEW,
            onFilterSelect = {},
            modifier = Modifier.padding(vertical = AppSpacing.sm),
        )
    }
}
