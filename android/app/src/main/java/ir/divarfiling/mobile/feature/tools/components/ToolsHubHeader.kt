package ir.divarfiling.mobile.feature.tools.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader

@Composable
fun ToolsHubHeader(
    userName: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    DfHubPageHeader(
        title = "ابزارهای هوشمند",
        subtitle = "محاسبات تخصصی املاک و معاملات",
        titleIconRes = DfDecorIcons.Sparkles,
        userName = userName,
        notificationCount = notificationCount,
        onNotificationsClick = onNotificationsClick,
        onMenuClick = onMenuClick,
        menuIcon = DfIcons.Menu,
        onBack = onBack,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ToolsHubHeaderPreview() {
    DivarFilingTheme {
        ToolsHubHeader(
            userName = "حسین",
            notificationCount = 9,
            onNotificationsClick = {},
            onMenuClick = {},
        )
    }
}
