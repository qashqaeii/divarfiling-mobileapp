package ir.divarfiling.mobile.feature.more

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.feature.update.AppUpdateViewModel

private data class MoreHubItem(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
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
        MoreHubItem("ابزارهای هوشمند", "محاسبه‌گرها و ابزار مشاور", DfDecorIcons.Calculator, AppColors.PurpleContainer, MoreHubAction.Navigate("tools")),
        MoreHubItem("استخراج سبک", "استخراج آگهی از دیوار روی گوشی", DfDecorIcons.Download, AppColors.BlueLight, MoreHubAction.Navigate("extract")),
        MoreHubItem("قالب پیام", "پیام‌های آماده برای مشتری", DfDecorIcons.FileText, AppColors.AmberLight, MoreHubAction.Navigate("templates")),
        MoreHubItem("تقویم", "یادآورها و برنامه روز", DfDecorIcons.Calendar, AppColors.GreenLight, MoreHubAction.Navigate("calendar")),
        MoreHubItem("تیم", "مدیریت تیم در میزکار وب", DfDecorIcons.Users, AppColors.PinkLight, MoreHubAction.External(AppLinks.WORKSPACE_TEAM)),
        MoreHubItem("دستیار AI", "به‌زودی — در حال توسعه", DfDecorIcons.Sparkles, AppColors.PurpleContainer, MoreHubAction.Navigate("ai")),
        MoreHubItem("آکادمی", "آموزش و راهنما", DfDecorIcons.Rocket, AppColors.BlueLight, MoreHubAction.External(AppLinks.ACADEMY)),
        MoreHubItem("پشتیبانی", "تیکت و درخواست کمک", DfDecorIcons.Phone, AppColors.AmberLight, MoreHubAction.Navigate("support")),
        MoreHubItem("بروزرسانی اپ", "بررسی نسخه جدید و نصب", DfDecorIcons.Download, AppColors.GreenLight, MoreHubAction.CheckUpdate),
        MoreHubItem("حریم خصوصی", "سیاست حفظ حریم", DfDecorIcons.Database, AppColors.SurfaceVariant, MoreHubAction.External(AppLinks.PRIVACY)),
        MoreHubItem("تنظیمات", "پروفایل و اعلان‌ها", DfDecorIcons.Settings, AppColors.SurfaceVariant, MoreHubAction.Navigate("settings")),
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
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    items(items, key = { it.title }) { item ->
                        MoreHubCard(item = item, onClick = { handleItem(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreHubCard(item: MoreHubItem, onClick: () -> Unit) {
    DfCard(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 112.dp),
        onClick = onClick,
        containerColor = DfThemeColors.surface(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.background, AppShapes.IconContainer),
                contentAlignment = Alignment.Center,
            ) {
                DfDecorImage(
                    resId = item.iconRes,
                    size = 22.dp,
                    contentDescription = item.title,
                )
            }
            Text(
                item.title,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                item.subtitle,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
