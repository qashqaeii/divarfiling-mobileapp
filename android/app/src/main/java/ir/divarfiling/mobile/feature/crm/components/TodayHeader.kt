package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfStandardPageHeader

@Composable
fun TodayHeader(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    DfStandardPageHeader(
        title = "کارهای امروز",
        subtitle = "تماس، پیگیری و یادآورهای برنامه‌ریزی‌شده",
        titleIconRes = DfDecorIcons.ListTodo,
        onBack = onBack,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun TodayHeaderPreview() {
    DivarFilingTheme {
        TodayHeader(onBack = null)
    }
}
