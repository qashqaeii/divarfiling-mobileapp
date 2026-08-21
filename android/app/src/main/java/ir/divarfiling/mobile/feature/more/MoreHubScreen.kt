package ir.divarfiling.mobile.feature.more

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
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
    val icon: ImageVector,
    val action: MoreHubAction,
    val badgeCount: Int = 0,
    val featured: Boolean = false,
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
    onNavigatePlans: () -> Unit = {},
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
                MoreHubItem("معاملات", "پایپ‌لاین فروش و اجاره", DfIcons.Handshake, MoreHubAction.Navigate("deals"), featured = true),
                MoreHubItem("فایل‌های شخصی", "ملک‌های ثبت‌شده در پرونده شما", DfIcons.Building, MoreHubAction.Navigate("properties"), featured = true),
                MoreHubItem("تقویم", "یادآورها و برنامه روز", DfIcons.Calendar, MoreHubAction.Navigate("calendar")),
                MoreHubItem("مدیریت مشتری", "نمای کلی مخاطب، معامله و ملک", DfIcons.Users, MoreHubAction.Navigate("crm")),
                MoreHubItem("قالب پیام", "پیام‌های آماده برای مخاطب", DfIcons.File, MoreHubAction.Navigate("templates")),
            ),
        ),
        MoreHubSection(
            "استخراج و فایلینگ",
            listOf(
                MoreHubItem("استخراج سبک", "استخراج آگهی دیوار روی گوشی", DfIcons.Download, MoreHubAction.Navigate("extract"), featured = true),
            ),
        ),
        MoreHubSection(
            "آژانس و ابزار",
            listOf(
                MoreHubItem(
                    "آژانس",
                    if (state.teamUnreadCount > 0) "${state.teamUnreadCount} مورد خوانده‌نشده" else "اعضا، پیام‌ها و اعلامیه‌ها",
                    DfIcons.Users,
                    MoreHubAction.Navigate("team"),
                    badgeCount = state.teamUnreadCount,
                    featured = true,
                ),
                MoreHubItem("ابزارها", "محاسبه‌گرها و ابزار مشاور", DfIcons.Calculator, MoreHubAction.Navigate("tools"), featured = true),
                MoreHubItem("دستیار AI", "پیش‌نویس پیام و خلاصه آگهی", DfIcons.Sparkles, MoreHubAction.Navigate("ai")),
            ),
        ),
        MoreHubSection(
            "حساب و پشتیبانی",
            listOf(
                MoreHubItem("حساب و اشتراک", "وضعیت لایسنس، پلن و تمدید", DfIcons.Lock, MoreHubAction.Navigate("plans"), featured = true),
                MoreHubItem("Product Hub", "مرکز محصول و ابزارهای پیشرفته وب", DfIcons.Layers, MoreHubAction.External(AppLinks.PRODUCT_HUB)),
                MoreHubItem("اعلان‌ها", "یادآور، تطبیق و هشدارها", DfIcons.Bell, MoreHubAction.Navigate("notifications")),
                MoreHubItem("پشتیبانی", "تیکت و درخواست کمک", DfIcons.Phone, MoreHubAction.Navigate("support")),
                MoreHubItem("تنظیمات", "پروفایل و ترجیحات اعلان", DfIcons.Settings, MoreHubAction.Navigate("settings")),
                MoreHubItem("راهنمای نصب", "Play Protect و نصب نسخه", DfIcons.Download, MoreHubAction.Navigate("install-help")),
                MoreHubItem("آکادمی", "آموزش و راهنما", DfIcons.Rocket, MoreHubAction.External(AppLinks.ACADEMY)),
                MoreHubItem("بروزرسانی اپ", "بررسی نسخه جدید و نصب", DfIcons.Download, MoreHubAction.CheckUpdate),
                MoreHubItem("حریم خصوصی", "سیاست حفظ حریم", DfIcons.Database, MoreHubAction.External(AppLinks.PRIVACY)),
            ),
        ),
    )

    fun handleItem(item: MoreHubItem) {
        when (val action = item.action) {
            is MoreHubAction.External -> {
                ir.divarfiling.mobile.core.ExternalBrowser.open(context, action.url)
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
                "plans" -> onNavigatePlans()
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
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                item {
                    DfHubPageHeader(
                        title = "بیشتر",
                        subtitle = "ابزارها، آژانس و میانبرهای کم‌استفاده",
                        titleIcon = DfIcons.Layers,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreHubRow(
    item: MoreHubItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconTint = if (item.featured) DfColors.Purple else DfThemeColors.textSecondary()
    val iconBg = if (item.featured) DfColors.PurpleContainer else DfThemeColors.surfaceVariant()
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (item.featured) 56.dp else 48.dp),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        shadowElevation = if (item.featured) AppElevations.subtle else AppElevations.none,
        tonalElevation = AppElevations.none,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(if (item.featured) 36.dp else 32.dp)
                    .background(iconBg, AppShapes.IconContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
                if (item.badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(DfColors.Rose, AppShapes.Chip)
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
                if (item.featured || item.badgeCount > 0) {
                    Text(
                        item.subtitle,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfThemeColors.textMuted(),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
