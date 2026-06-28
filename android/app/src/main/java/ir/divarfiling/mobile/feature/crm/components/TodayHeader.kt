package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfActionPageHeader

@Composable
fun TodayHeader(
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfActionPageHeader(
        title = "کارهای امروز",
        subtitle = "همه کارهای برنامه‌ریزی‌شده برای امروز",
        titleIcon = DfIcons.Calendar,
        onLeadingClick = onFilterClick,
        leadingIcon = DfIcons.Filter,
        leadingContentDescription = "فیلتر",
        onTrailingClick = onBack,
        trailingIcon = DfIcons.ChevronLeft,
        trailingContentDescription = "بازگشت",
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TodayHeaderPreview() {
    DivarFilingTheme {
        TodayHeader(onBack = {}, onFilterClick = {})
    }
}
