package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader

@Composable
fun FilingHubHeader(
    userName: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfHubPageHeader(
        title = "فایلینگ دیوار",
        subtitle = "استخراج از دیوار",
        titleIconRes = DfDecorIcons.Layers,
        userName = userName,
        notificationCount = notificationCount,
        onNotificationsClick = onNotificationsClick,
        onMenuClick = onMenuClick,
        menuIcon = DfIcons.Menu,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FilingHubHeaderPreview() {
    DivarFilingTheme {
        FilingHubHeader(
            userName = "حسین",
            notificationCount = 9,
            onNotificationsClick = {},
            onMenuClick = {},
        )
    }
}
