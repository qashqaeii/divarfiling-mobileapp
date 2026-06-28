package ir.divarfiling.mobile.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    onNavigateNotifications: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    fun openWeb(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    ProfileEditSheet(
        visible = state.showProfileSheet,
        fullName = state.editFullName,
        phone = state.editPhone,
        isSaving = state.isSavingProfile,
        onFullNameChange = viewModel::onEditFullNameChange,
        onPhoneChange = viewModel::onEditPhoneChange,
        onDismiss = { viewModel.toggleProfileSheet(false) },
        onSave = viewModel::saveProfile,
    )

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refreshAll,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            if (state.isLoading && state.user == null) {
                DfDetailSkeleton()
                return@DfPullRefresh
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl + 72.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "تنظیمات",
                        subtitle = "پروفایل، اعلان‌ها و امنیت",
                        titleIcon = DfIcons.Settings,
                        userName = state.user?.fullName?.substringBefore(" ") ?: "کاربر",
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                    )
                }

                item {
                    SettingsHeroCard(
                        user = state.user,
                        onEditProfile = { viewModel.toggleProfileSheet(true) },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    LicenseInsightCard(
                        license = state.license,
                        onRenew = { openWeb("https://divarfiling.ir/shop/") },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    SettingsSectionTitle(
                        title = "اعلان‌ها",
                        subtitle = if (state.isSavingPrefs) "در حال ذخیره…" else "کنترل دقیق رویدادهای Push",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column(Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)) {
                            NotificationPrefRow(
                                title = "یادآور CRM",
                                subtitle = "تماس، بازدید و پیگیری‌های سررسید",
                                icon = DfIcons.Bell,
                                checked = state.notificationPrefs.crmReminders,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(crmReminders = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            NotificationPrefRow(
                                title = "کارهای امروز",
                                subtitle = "خلاصه صبحگاهی کارهای روز",
                                icon = DfIcons.Calendar,
                                checked = state.notificationPrefs.todayDigest,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(todayDigest = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            NotificationPrefRow(
                                title = "پیگیری معوق",
                                subtitle = "هشدار مشتریان عقب‌افتاده",
                                icon = DfIcons.Phone,
                                checked = state.notificationPrefs.overdueFollowup,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(overdueFollowup = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            HorizontalDivider(color = DfColors.OutlineSubtle)
                            NotificationPrefRow(
                                title = "فایل جدید",
                                subtitle = "dataset تازه از ویندوز یا موبایل",
                                icon = DfIcons.Folder,
                                checked = state.notificationPrefs.newDataset,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(newDataset = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            NotificationPrefRow(
                                title = "کاهش قیمت",
                                subtitle = "تغییرات قیمت آگهی‌های تحت نظر",
                                icon = DfIcons.TrendingDown,
                                checked = state.notificationPrefs.priceDrop,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(priceDrop = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            NotificationPrefRow(
                                title = "فایل مناسب مشتری",
                                subtitle = "تطبیق هوشمند فایلینگ",
                                icon = DfIcons.Star,
                                checked = state.notificationPrefs.customerMatch,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(customerMatch = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            HorizontalDivider(color = DfColors.OutlineSubtle)
                            NotificationPrefRow(
                                title = "پایان استخراج",
                                subtitle = "وقتی آپلود به Workspace تمام شد",
                                icon = DfIcons.Download,
                                checked = state.notificationPrefs.extractComplete,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(extractComplete = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            NotificationPrefRow(
                                title = "زمان‌بندی استخراج",
                                subtitle = "یادآور اجرای schedule روی دستگاه",
                                icon = DfIcons.Smartphone,
                                checked = state.notificationPrefs.extractScheduleDue,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(extractScheduleDue = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            NotificationPrefRow(
                                title = "انقضای لایسنس",
                                subtitle = "۱۴، ۷، ۳ و ۱ روز قبل از پایان",
                                icon = DfIcons.Sparkles,
                                checked = state.notificationPrefs.licenseAlerts,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(licenseAlerts = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            HorizontalDivider(
                                color = DfColors.OutlineSubtle,
                                modifier = Modifier.padding(vertical = AppSpacing.xs),
                            )
                            DigestHourPicker(
                                hour = state.notificationPrefs.digestHour,
                                onHourChange = viewModel::onDigestHourChange,
                            )
                        }
                    }
                }

                item {
                    SettingsSectionTitle(
                        title = "درباره اپ",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column(Modifier.padding(horizontal = AppSpacing.cardPadding, vertical = AppSpacing.xs)) {
                            SettingsInfoRow(
                                title = "نسخه اپ",
                                subtitle = "Divar Filing Companion",
                                icon = Icons.Default.Info,
                                trailing = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            )
                            SettingsInfoRow(
                                title = "میزکار وب",
                                subtitle = "استخراج حرفه‌ای، Excel و مدیریت کامل",
                                icon = DfIcons.ExternalLink,
                                trailing = "divarfiling.ir",
                                onClick = { openWeb("https://divarfiling.ir/") },
                            )
                            SettingsInfoRow(
                                title = "نصب امن",
                                subtitle = "در صورت هشدار Play Protect گزینه Install anyway",
                                icon = Icons.Default.Security,
                            )
                        }
                    }
                }

                item {
                    LogoutButton(
                        onClick = { viewModel.logout(onLoggedOut) },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    Text(
                        "ساخته‌شده برای مشاورانی که هر روز در حرکت‌اند.",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextMuted,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }
        }
    }
}
