package ir.divarfiling.mobile.feature.crm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
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
            iconRes = DfDecorIcons.Users,
            filter = ContactsFilters.QuickFilter.ALL,
        ),
        ContactsStatItem(
            label = "در پیگیری",
            value = numberFormat.format(followUpCount),
            accent = DfColors.Green,
            icon = DfIcons.RefreshCw,
            filter = ContactsFilters.QuickFilter.FOLLOW_UP,
        ),
        ContactsStatItem(
            label = "سرنخ جدید",
            value = numberFormat.format(newCount),
            accent = DfColors.Blue,
            icon = DfIcons.UserPlus,
            filter = ContactsFilters.QuickFilter.NEW,
        ),
        ContactsStatItem(
            label = "به‌روز امروز",
            value = numberFormat.format(todayCount),
            accent = DfColors.Amber,
            icon = DfIcons.Calendar,
            filter = ContactsFilters.QuickFilter.TODAY,
        ),
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        shape = AppShapes.Field,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 76.dp)
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            stats.forEachIndexed { index, stat ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp)
                            .background(DfColors.Outline.copy(alpha = 0.45f)),
                    )
                }
                ContactsMiniStatCell(
                    value = stat.value,
                    label = stat.label,
                    valueColor = stat.accent,
                    icon = stat.icon,
                    iconRes = stat.iconRes,
                    selected = selectedFilter == stat.filter,
                    onClick = {
                        onFilterSelect(
                            if (selectedFilter == stat.filter) ContactsFilters.QuickFilter.ALL else stat.filter,
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
    val value: String,
    val accent: Color,
    val filter: ContactsFilters.QuickFilter,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
)

@Composable
private fun ContactsMiniStatCell(
    value: String,
    label: String,
    valueColor: Color,
    icon: ImageVector?,
    @DrawableRes iconRes: Int?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .background(
                if (selected) valueColor.copy(alpha = 0.12f) else Color.Transparent,
            )
            .padding(horizontal = 3.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (selected) valueColor.copy(alpha = 0.16f) else DfColors.SurfaceVariant,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                iconRes != null -> DfDecorImage(resId = iconRes, size = 13.dp)
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) valueColor else DfColors.TextMuted,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
        Text(
            text = value,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (selected) valueColor else valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = if (selected) valueColor else DfColors.TextMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
            lineHeight = AppTypography.labelSmall.lineHeight,
        )
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
