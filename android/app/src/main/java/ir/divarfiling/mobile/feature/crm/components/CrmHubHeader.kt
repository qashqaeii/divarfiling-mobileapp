package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfGreetingHeader

@Composable
fun CrmHubHeader(
    userName: String,
    modifier: Modifier = Modifier,
) {
    DfGreetingHeader(
        title = "مدیریت مشتری",
        subtitle = "مشتریان، یادآورها و سرنخ‌های شما در یک نگاه",
        userName = userName,
        showBrandLogo = true,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CrmHubHeaderPreview() {
    DivarFilingTheme {
        CrmHubHeader(userName = "حسین")
    }
}
