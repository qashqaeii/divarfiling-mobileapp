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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.update.UpdateDistribution
import ir.divarfiling.mobile.feature.update.AppUpdateViewModel
import ir.divarfiling.mobile.feature.update.AppUpdateInlineBanner
import ir.divarfiling.mobile.feature.update.AppUpdatePhase

private data class MoreHubItem(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    val background: Color,
    val action: MoreHubAction,
    val badgeCount: Int = 0,
)

private sealed class MoreHubAction {
    data class Navigate(val route: String) : MoreHubAction()
    data class External(val url: String) : MoreHubAction()
    data object CheckUpdate : MoreHubAction()
}

private data class MoreHubSection(
    val title: String,
    val items: List<MoreHubItem>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreHubScreen(
    onBack: (() -> Unit)? = null,
    onNavigateTools: () -> Unit = {},
    onNavigateExtract: () -> Unit = {},
    onNavigateFilingSearch: () -> Unit = {},
    onNavigateTemplates: () -> Unit = {},
    onNavigateCalendar: () -> Unit = {},
    onNavigateAi: () -> Unit = {},
    onNavigateCloudExtract: () -> Unit = {},
    onNavigateTeam: () -> Unit = {},
    onNavigateSupport: () -> Unit = {},
    onNavigateInstallHelp: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateDeals: () -> Unit = {},
    onNavigateProperties: () -> Unit = {},
    onNavigateCrm: () -> Unit = {},
    viewModel: MoreHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val updateViewModel: AppUpdateViewModel = hiltViewModel(activity)
    val updateState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val usesInAppApkUpdate = UpdateDistribution.usesInAppApkUpdate

    val sections = listOf(
        MoreHubSection(
            "پیگیری و پرونده",
            listOf(
                MoreHubItem("معاملات", "پایپ‌لاین فروش و اجاره", DfDecorIcons.Handshake, AppColors.GreenLight, MoreHubAction.Navigate("deals")),
                MoreHubItem("فایل‌های شخصی", "ملک‌های ثبت‌شده در پرونده شما", DfDecorIcons.Building, AppColors.AmberLight, MoreHubAction.Navigate("properties")),
                MoreHubItem("تقویم", "یادآورها و برنامه روز", DfDecorIcons.Calendar, AppColors.GreenLight, MoreHubAction.Navigate("calendar")),
                MoreHubItem("مدیریت مشتری", "نمای کلی مخاطب، معامله و ملک", DfDecorIcons.Users, AppColors.PurpleContainer, MoreHubAction.Navigate("crm")),
                MoreHubItem("قالب پیام", "پیام‌های آماده برای مخاطب", DfDecorIcons.FileText, AppColors.AmberLight, MoreHubAction.Navigate("templates")),
            ),
        ),
        MoreHubSection(
            "استخراج و فایلینگ",
            listOf(
                MoreHubItem("استخراج سبک", "استخراج آگهی دیوار روی گوشی", DfDecorIcons.Download, AppColors.BlueLight, MoreHubAction.Navigate("extract")),
                MoreHubItem("استخراج ابری", "استخراج از سرور بدون درگیر کردن گوشی", DfDecorIcons.Download, AppColors.BlueLight, MoreHubAction.Navigate("cloud-extract")),
                MoreHubItem("جستجوی فایلینگ", "جستجو در آگهی‌های استخراج‌شده", DfDecorIcons.Search, AppColors.SurfaceVariant, MoreHubAction.Navigate("filing-search")),
            ),
        ),
        MoreHubSection(
            "آژانس و ابزار",
            listOf(
                MoreHubItem(
                    "آژانس",
                    if (state.teamUnreadCount > 0) "${state.teamUnreadCount} مورد خوانده‌نشده" else "اعضا، پیام‌ها و اعلامیه‌ها",
                    DfDecorIcons.Users,
                    AppColors.PinkLight,
                    MoreHubAction.Navigate("team"),
                    badgeCount = state.teamUnreadCount,
                ),
                MoreHubItem("ابزارها", "محاسبه‌گرها و ابزار مشاور", DfDecorIcons.Calculator, AppColors.PurpleContainer, MoreHubAction.Navigate("tools")),
                MoreHubItem("دستیار AI", "پیش‌نویس پیام و خلاصه آگهی", DfDecorIcons.Sparkles, AppColors.PurpleContainer, MoreHubAction.Navigate("ai")),
            ),
        ),
        MoreHubSection(
            "حساب و پشتیبانی",
            listOf(
                MoreHubItem("اعلان‌ها", "یادآور، تطبیق و هشدارها", DfDecorIcons.Bell, AppColors.SurfaceVariant, MoreHubAction.Navigate("notifications")),
                MoreHubItem("پشتیبانی", "تیکت و درخواست کمک", DfDecorIcons.Phone, AppColors.AmberLight, MoreHubAction.Navigate("support")),
                MoreHubItem("تنظیمات", "پروفایل و ترجیحات اعلان", DfDecorIcons.Settings, AppColors.SurfaceVariant, MoreHubAction.Navigate("settings")),
                MoreHubItem("راهنمای نصب", "Play Protect و نصب نسخه", DfDecorIcons.Download, AppColors.SurfaceVariant, MoreHubAction.Navigate("install-help")),
                MoreHubItem("آکادمی", "آموزش و راهنما", DfDecorIcons.Rocket, AppColors.BlueLight, MoreHubAction.External(AppLinks.ACADEMY)),
                MoreHubItem(
                    "بروزرسانی اپ",
                    "بررسی نسخه جدید و نصب",
                    DfDecorIcons.Download,
                    AppColors.GreenLight,
                    MoreHubAction.CheckUpdate,
                ),
                MoreHubItem("حریم خصوصی", "سیاست حفظ حریم", DfDecorIcons.Database, AppColors.SurfaceVariant, MoreHubAction.External(AppLinks.PRIVACY)),
            ),
        ),
    )

    fun handleItem(item: MoreHubItem) {
        when (val action = item.action) {
            is MoreHubAction.External -> {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action.url)))
            }
            is MoreHubAction.Navigate -> when (action.route) {
                "tools" -> onNavigateTools()
                "extract" -> onNavigateExtract()
                "filing-search" -> onNavigateFilingSearch()
                "templates" -> onNavigateTemplates()
                "calendar" -> onNavigateCalendar()
                "ai" -> onNavigateAi()
                "cloud-extract" -> onNavigateCloudExtract()
                "team" -> onNavigateTeam()
                "support" -> onNavigateSupport()
                "install-help" -> onNavigateInstallHelp()
                "settings" -> onNavigateSettings()
                "notifications" -> onNavigateNotifications()
                "deals" -> onNavigateDeals()
                "properties" -> onNavigateProperties()
                "crm" -> onNavigateCrm()
            }
            MoreHubAction.CheckUpdate -> updateViewModel.checkManually()
        }
    }

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "بیشتر",
                        subtitle = "ابزارها، آژانس و میانبرهای کم‌استفاده",
                        titleIconRes = DfDecorIcons.Layers,
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                        onBack = onBack,
                    )
                }
                if (usesInAppApkUpdate && updateState.visible && updateState.phase != AppUpdatePhase.UpToDate) {
                    item {
                        AppUpdateInlineBanner(
                            state = updateState,
                            onPrimaryClick = {
                                when (updateState.phase) {
                                    AppUpdatePhase.Available, AppUpdatePhase.Error -> updateViewModel.startUpdate()
                                    AppUpdatePhase.ReadyToInstall -> updateViewModel.installNow()
                                    AppUpdatePhase.AwaitingInstallPermission ->
                                        context.startActivity(updateViewModel.openInstallPermissionSettings())
                                    else -> Unit
                                }
                            },
                            onOpenStore = { url ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                sections.forEach { section ->
                    item {
                        Box(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            DfSectionHeader(title = section.title)
                        }
                    }
                    items(section.items, key = { it.title }) { item ->
                        MoreHubRow(
                            item = item,
                            onClick = { handleItem(item) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreHubRow(
    item: MoreHubItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp),
        onClick = onClick,
        containerColor = DfThemeColors.surface(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
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
                if (item.badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(AppColors.Rose, AppShapes.Chip)
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = item.badgeCount.coerceAtMost(99).toString(),
                            style = AppTypography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
}
