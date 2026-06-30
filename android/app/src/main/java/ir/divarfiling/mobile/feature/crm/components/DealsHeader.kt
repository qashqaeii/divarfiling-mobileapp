package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader

@Composable
fun DealsHeader(
    userName: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    DfHubPageHeader(
        title = "معاملات",
        subtitle = "مدیریت معاملات",
        titleIconRes = DfDecorIcons.TrendingUp,
        userName = userName,
        notificationCount = notificationCount,
        onNotificationsClick = onNotificationsClick,
        onMenuClick = onMenuClick,
        onBack = onBack,
        menuIcon = DfIcons.Menu,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DealsHeaderPreview() {
    DivarFilingTheme {
        DealsHeader(
            userName = "حسین",
            notificationCount = 9,
            onNotificationsClick = {},
            onMenuClick = {},
        )
    }
}
