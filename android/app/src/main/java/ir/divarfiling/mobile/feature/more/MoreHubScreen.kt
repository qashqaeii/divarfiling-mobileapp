package ir.divarfiling.mobile.feature.more

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfDecorSize
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.feature.update.AppUpdateViewModel

private data class MoreHubItem(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val tint: Color,
    val background: Color,
    val action: MoreHubAction,
)

private sealed class MoreHubAction {
    data class Navigate(val route: String) : MoreHubAction()
    data class External(val url: String) : MoreHubAction()
    data object CheckUpdate : MoreHubAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreHubScreen(
    onBack: (() -> Unit)? = null,
    onNavigateTools: () -> Unit = {},
    onNavigateExtract: () -> Unit = {},
    onNavigateTemplates: () -> Unit = {},
    onNavigateCalendar: () -> Unit = {},
    onNavigateAi: () -> Unit = {},
    onNavigateSupport: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    viewModel: MoreHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val updateViewModel: AppUpdateViewModel = hiltViewModel(activity)

    val items = listOf(
        MoreHubItem("ابزارهای هوشمند", "محاسبه‌گرها و ابزار مشاور", DfDecorIcons.Calculator, DfColors.Purple, DfColors.PurpleContainer, MoreHubAction.Navigate("tools")),
        MoreHubItem("استخراج سبک", "استخراج آگهی از دیوار روی گوشی", DfDecorIcons.Download, DfColors.Blue, DfColors.BlueLight, MoreHubAction.Navigate("extract")),
        MoreHubItem("قالب پیام", "پیام‌های آماده برای مشتری", DfDecorIcons.FileText, DfColors.Amber, DfColors.AmberLight, MoreHubAction.Navigate("templates")),
        MoreHubItem("تقویم", "یادآورها و برنامه روز", DfDecorIcons.Calendar, DfColors.Green, DfColors.GreenLight, MoreHubAction.Navigate("calendar")),
        MoreHubItem("تیم", "مدیریت تیم در میزکار وب", DfDecorIcons.Users, DfColors.Pink, DfColors.PinkLight, MoreHubAction.External(AppLinks.WORKSPACE_TEAM)),
        MoreHubItem("دستیار AI", "به‌زودی — در حال توسعه", DfDecorIcons.Sparkles, DfColors.Purple, DfColors.PurpleContainer, MoreHubAction.Navigate("ai")),
        MoreHubItem("آکادمی", "آموزش و راهنما", DfDecorIcons.Rocket, DfColors.Blue, DfColors.BlueLight, MoreHubAction.External(AppLinks.ACADEMY)),
        MoreHubItem("پشتیبانی", "تیکت و درخواست کمک", DfDecorIcons.Phone, DfColors.Amber, DfColors.AmberLight, MoreHubAction.Navigate("support")),
        MoreHubItem("بروزرسانی اپ", "بررسی نسخه جدید و نصب", DfDecorIcons.Download, DfColors.Green, DfColors.GreenLight, MoreHubAction.CheckUpdate),
        MoreHubItem("حریم خصوصی", "سیاست حفظ حریم", DfDecorIcons.Database, DfColors.TextSecondary, DfColors.SurfaceVariant, MoreHubAction.External(AppLinks.PRIVACY)),
        MoreHubItem("تنظیمات", "پروفایل و اعلان‌ها", DfDecorIcons.Settings, DfColors.TextSecondary, DfColors.SurfaceVariant, MoreHubAction.Navigate("settings")),
    )

    fun handleItem(item: MoreHubItem) {
        when (val action = item.action) {
            is MoreHubAction.External -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action.url)))
            is MoreHubAction.Navigate -> when (action.route) {
                "tools" -> onNavigateTools()
                "extract" -> onNavigateExtract()
                "templates" -> onNavigateTemplates()
                "calendar" -> onNavigateCalendar()
                "ai" -> onNavigateAi()
                "support" -> onNavigateSupport()
                "settings" -> onNavigateSettings()
            }
            MoreHubAction.CheckUpdate -> updateViewModel.checkManually()
        }
    }

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        DfPullRefresh(
            isRefreshing = false,
            onRefresh = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DfHubPageHeader(
                    title = "بیشتر",
                    subtitle = "ابزارها، پشتیبانی و میانبرهای میزکار",
                    titleIconRes = DfDecorIcons.Layers,
                    userName = state.userName,
                    notificationCount = state.notificationBadgeCount,
                    onNotificationsClick = onNavigateNotifications,
                    onMenuClick = onNavigateSettings,
                    onBack = onBack,
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AppSpacing.screenHorizontal,
                        vertical = AppSpacing.sm,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    items(items, key = { it.title }) { item ->
                        MoreHubCard(item = item, onClick = { handleItem(item) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreHubCard(item: MoreHubItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = AppShapes.Card,
        color = item.background.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            DfDecorImage(
                resId = item.iconRes,
                size = DfDecorSize.Medium,
                contentDescription = item.title,
            )
            Text(
                item.title,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                item.subtitle,
                style = AppTypography.labelSmall,
                color = DfColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
