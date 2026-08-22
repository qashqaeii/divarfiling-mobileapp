package ir.divarfiling.mobile.feature.crm.components

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIconBox
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import java.text.NumberFormat
import java.util.Locale

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
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    val stats = listOf(
        ContactsStatItem(
            label = "کل مخاطبین",
            value = numberFormat.format(totalCount),
            accent = DfColors.Purple,
            container = DfColors.PurpleContainer,
            iconRes = DfDecorIcons.Users,
            filter = ContactsFilters.QuickFilter.ALL,
        ),
        ContactsStatItem(
            label = "در پیگیری",
            value = numberFormat.format(followUpCount),
            accent = DfColors.Green,
            container = DfColors.GreenLight,
            icon = DfIcons.RefreshCw,
            filter = ContactsFilters.QuickFilter.FOLLOW_UP,
        ),
        ContactsStatItem(
            label = "سرنخ جدید",
            value = numberFormat.format(newCount),
            accent = DfColors.Blue,
            container = DfColors.BlueLight,
            icon = DfIcons.UserPlus,
            filter = ContactsFilters.QuickFilter.NEW,
        ),
        ContactsStatItem(
            label = "به‌روز امروز",
            value = numberFormat.format(todayCount),
            accent = DfColors.Amber,
            container = DfColors.AmberLight,
            icon = DfIcons.Calendar,
            filter = ContactsFilters.QuickFilter.TODAY,
        ),
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
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
            if (selectedFilter != ContactsFilters.QuickFilter.ALL) {
                Text(
                    text = "فیلتر فعال · لمس برای حذف",
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            stats.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    rowItems.forEach { stat ->
                        ContactsStatCard(
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
    }
}

private data class ContactsStatItem(
    val label: String,
    val value: String,
    val accent: Color,
    val container: Color,
    val filter: ContactsFilters.QuickFilter,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
)

@Composable
private fun ContactsStatCard(
    stat: ContactsStatItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Card,
        color = if (selected) stat.container.copy(alpha = 0.65f) else DfColors.Surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) stat.accent.copy(alpha = 0.55f) else DfColors.Outline.copy(alpha = 0.35f),
        ),
        shadowElevation = if (selected) AppElevations.card else AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    stat.iconRes != null -> DfDecorIconBox(
                        resId = stat.iconRes,
                        containerSize = 36.dp,
                        imageSize = 18.dp,
                        background = stat.container,
                    )
                    stat.icon != null -> Surface(
                        shape = AppShapes.IconContainer,
                        color = stat.container,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = stat.icon,
                                contentDescription = null,
                                tint = stat.accent,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                if (selected) {
                    Surface(
                        shape = AppShapes.Chip,
                        color = stat.accent.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = "فعال",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = AppTypography.labelSmall,
                            color = stat.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stat.value,
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) stat.accent else DfColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stat.label,
                    style = AppTypography.labelSmall,
                    color = if (selected) stat.accent.copy(alpha = 0.85f) else DfColors.TextMuted,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                )
            }
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
