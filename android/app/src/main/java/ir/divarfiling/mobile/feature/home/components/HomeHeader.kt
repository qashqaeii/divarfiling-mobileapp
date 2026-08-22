package ir.divarfiling.mobile.feature.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfGreetingHeader
import ir.divarfiling.mobile.core.design.components.DfHeaderSections

@Composable
fun HomeHeader(
    userName: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfGreetingHeader(
        title = "سلام $userName",
        subtitle = "میزکار فایلینگ دیوار",
        sectionLabel = DfHeaderSections.HOME,
        userName = userName,
        notificationCount = notificationCount,
        onNotificationsClick = onNotificationsClick,
        onMenuClick = onMenuClick,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun HomeHeaderPreview() {
    DivarFilingTheme {
        HomeHeader(
            userName = "حسین",
            notificationCount = 9,
            onNotificationsClick = {},
            onMenuClick = {},
        )
    }
}
