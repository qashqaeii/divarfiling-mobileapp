package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader

@Composable
fun ExtractHeader(
    userName: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    DfHubPageHeader(
        title = "استخراج جدید",
        subtitle = "استخراج املاک دیوار",
        titleIconRes = DfDecorIcons.Sparkles,
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
private fun ExtractHeaderPreview() {
    DivarFilingTheme {
        ExtractHeader(
            userName = "حسین",
            notificationCount = 9,
            onNotificationsClick = {},
            onMenuClick = {},
        )
    }
}
