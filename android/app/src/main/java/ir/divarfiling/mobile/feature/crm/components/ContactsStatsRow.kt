package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import ir.divarfiling.mobile.core.design.DivarFilingTheme
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
        ContactsStatItem("کل", numberFormat.format(totalCount), DfColors.Purple, ContactsFilters.QuickFilter.ALL),
        ContactsStatItem("پیگیری", numberFormat.format(followUpCount), DfColors.Green, ContactsFilters.QuickFilter.FOLLOW_UP),
        ContactsStatItem("جدید", numberFormat.format(newCount), DfColors.Blue, ContactsFilters.QuickFilter.NEW),
        ContactsStatItem("امروز", numberFormat.format(todayCount), DfColors.Amber, ContactsFilters.QuickFilter.TODAY),
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
                .height(52.dp)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            stats.forEachIndexed { index, stat ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .padding(vertical = 6.dp)
                            .background(DfColors.Outline.copy(alpha = 0.45f)),
                    )
                }
                ContactsMiniStatCell(
                    value = stat.value,
                    label = stat.label,
                    valueColor = stat.accent,
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
)

@Composable
private fun ContactsMiniStatCell(
    value: String,
    label: String,
    valueColor: Color,
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
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
    ) {
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
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
