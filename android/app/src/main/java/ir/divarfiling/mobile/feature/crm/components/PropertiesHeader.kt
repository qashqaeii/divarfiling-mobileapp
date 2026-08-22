package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader

@Composable
fun PropertiesHeader(
    userName: String,
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    DfHubPageHeader(
        title = "فایل‌های شخصی",
        subtitle = "مدیریت فایل‌های ملکی و وضعیت معاملات",
        sectionLabel = DfHeaderSections.CRM,
        titleIconRes = DfDecorIcons.Building,
        userName = userName,
        notificationCount = notificationCount,
        onNotificationsClick = onNotificationsClick,
        onMenuClick = onMenuClick,
        onBack = onBack,
        menuIcon = DfIcons.Menu,
        modifier = modifier,
    )
}
