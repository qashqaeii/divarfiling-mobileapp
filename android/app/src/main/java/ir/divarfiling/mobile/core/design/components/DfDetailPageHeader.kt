package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@Composable
fun DfDetailPageHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleIcon: ImageVector? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    DfStandardPageHeader(
        title = title,
        subtitle = subtitle.orEmpty(),
        titleIcon = titleIcon,
        onBack = onBack,
        modifier = modifier,
        toolbarContent = actions,
    )
}

@Composable
fun DfPillChipRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        labels.forEachIndexed { index, label ->
            DfGlassChip(
                text = label,
                selected = index == selectedIndex,
                onClick = { onSelected(index) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfPillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.ButtonPill,
        color = if (selected) DfColors.PurpleContainer else DfColors.Surface,
        shadowElevation = if (selected) 0.dp else AppElevations.subtle,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 8.dp),
            style = AppTypography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) DfColors.Purple else DfColors.TextSecondary,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DfDetailPageHeaderPreview() {
    DivarFilingTheme {
        Column {
            DfDetailPageHeader(
                title = "علی محمدی",
                subtitle = "۰۹۱۲۱۲۳۴۵۶۷",
                titleIcon = DfIcons.User,
                onBack = {},
            )
            DfPillChipRow(
                labels = listOf("سطح ۱", "سطح ۲", "کارشناسی"),
                selectedIndex = 0,
                onSelected = {},
            )
        }
    }
}
