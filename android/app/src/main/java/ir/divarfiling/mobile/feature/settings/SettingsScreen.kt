package ir.divarfiling.mobile.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.share.DossierShareActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    onNavigateNotifications: () -> Unit = {},
    onNavigateTools: () -> Unit = {},
    onNavigateSupport: () -> Unit = {},
    onNavigateInstallHelp: () -> Unit = {},
    onNavigatePlans: () -> Unit = {},
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

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::uploadAvatar)
    }

    ProfileEditSheet(
        visible = state.showProfileSheet,
        fullName = state.editFullName,
        phone = state.editPhone,
        avatarUrl = state.user?.avatarUrl,
        isSaving = state.isSavingProfile,
        isUploadingAvatar = state.isUploadingAvatar,
        onFullNameChange = viewModel::onEditFullNameChange,
        onPhoneChange = viewModel::onEditPhoneChange,
        onPickAvatar = { avatarPicker.launch("image/*") },
        onRemoveAvatar = viewModel::removeAvatar,
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
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "تنظیمات",
                        subtitle = "میزکار موبایل",
                        sectionLabel = DfHeaderSections.SETTINGS,
                        titleIconRes = DfDecorIcons.Settings,
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
                        onRenew = onNavigatePlans,
                        onOpenDashboard = { openWeb(AppLinks.DASHBOARD_LICENSES) },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    SettingsSectionTitle(
                        title = "میانبرها",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column {
                            SettingsInfoRow(
                                title = "ابزارهای هوشمند",
                                subtitle = "محاسبه‌گرها و ابزار مشاور",
                                icon = DfIcons.Sparkles,
                                onClick = onNavigateTools,
                            )
                            SettingsInfoRow(
                                title = "پشتیبانی",
                                subtitle = "تیکت و درخواست کمک",
                                icon = DfIcons.Phone,
                                onClick = onNavigateSupport,
                                showDivider = true,
                            )
                            SettingsInfoRow(
                                title = "حریم خصوصی",
                                subtitle = "سیاست حفظ حریم",
                                icon = DfIcons.Lock,
                                onClick = { openWeb(AppLinks.PRIVACY) },
                                showDivider = true,
                            )
                            SettingsInfoRow(
                                title = "آکادمی",
                                subtitle = "آموزش و راهنما",
                                icon = DfIcons.ExternalLink,
                                onClick = { openWeb(AppLinks.ACADEMY) },
                                showDivider = true,
                            )
                        }
                    }
                }

                item {
                    SettingsSectionTitle(
                        title = "اعلان‌ها",
                        subtitle = if (state.isSavingPrefs) {
                            "در حال ذخیره…"
                        } else {
                            "انتخاب کنید چه اعلان‌هایی روی گوشی‌تان نمایش داده شود"
                        },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column {
                            NotificationPrefRow(
                                title = "یادآور CRM",
                                subtitle = "تماس، بازدید و پیگیری‌های سررسید",
                                iconRes = DfDecorIcons.Bell,
                                checked = state.notificationPrefs.crmReminders,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(crmReminders = v) }
                                },
                                enabled = !state.isSavingPrefs,
                            )
                            NotificationPrefRow(
                                title = "کارهای امروز",
                                subtitle = "خلاصه صبحگاهی کارهای روز",
                                iconRes = DfDecorIcons.Calendar,
                                checked = state.notificationPrefs.todayDigest,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(todayDigest = v) }
                                },
                                enabled = !state.isSavingPrefs,
                                showDivider = true,
                            )
                            NotificationPrefRow(
                                title = "پیگیری معوق",
                                subtitle = "هشدار مشتریان عقب‌افتاده",
                                iconRes = DfDecorIcons.Phone,
                                checked = state.notificationPrefs.overdueFollowup,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(overdueFollowup = v) }
                                },
                                enabled = !state.isSavingPrefs,
                                showDivider = true,
                            )
                            NotificationPrefRow(
                                title = "اطلاعیه‌ها و پشتیبانی",
                                subtitle = "پیام‌های مدیریتی، به‌روزرسانی و پشتیبانی",
                                icon = DfIcons.Sparkles,
                                checked = state.notificationPrefs.announcements,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(announcements = v) }
                                },
                                enabled = !state.isSavingPrefs,
                                showDivider = true,
                            )
                            NotificationPrefRow(
                                title = "فایل مناسب مشتری",
                                subtitle = "فایل‌های پیشنهادی برای مخاطب",
                                icon = DfIcons.Star,
                                checked = state.notificationPrefs.customerMatch,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(customerMatch = v) }
                                },
                                enabled = !state.isSavingPrefs,
                                showDivider = true,
                            )
                            NotificationPrefRow(
                                title = "پایان استخراج",
                                subtitle = "وقتی آپلود به Workspace تمام شد",
                                icon = DfIcons.Download,
                                checked = state.notificationPrefs.extractComplete,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(extractComplete = v) }
                                },
                                enabled = !state.isSavingPrefs,
                                showDivider = true,
                            )
                            NotificationPrefRow(
                                title = "زمان‌بندی استخراج",
                                subtitle = "یادآور اجرای schedule روی دستگاه",
                                iconRes = DfDecorIcons.Timer,
                                checked = state.notificationPrefs.extractScheduleDue,
                                onCheckedChange = { v ->
                                    viewModel.updatePref { it.copy(extractScheduleDue = v) }
                                },
                                enabled = !state.isSavingPrefs,
                                showDivider = true,
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
                                showDivider = true,
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
                        title = "دستگاه و نشست",
                        subtitle = "اطلاعات این دستگاه برای پشتیبانی و اتصال به حساب",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }

                item {
                    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column {
                            SettingsInfoRow(
                                title = "شناسه دستگاه",
                                subtitle = if (state.deviceId.isBlank()) {
                                    "هنوز ثبت نشده"
                                } else {
                                    state.deviceId
                                },
                                icon = DfIcons.Phone,
                                trailing = if (state.deviceId.isBlank()) null else "کپی",
                                onClick = state.deviceId.takeIf { it.isNotBlank() }?.let { deviceId ->
                                    {
                                        DossierShareActions.copyToClipboard(context, deviceId, "device_id")
                                        android.widget.Toast.makeText(
                                            context,
                                            "شناسه دستگاه کپی شد",
                                            android.widget.Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                },
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
                        Column {
                            SettingsInfoRow(
                                title = "نسخه اپ",
                                subtitle = "Divar Filing Companion",
                                icon = DfIcons.CircleAlert,
                                trailing = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            )
                            SettingsInfoRow(
                                title = "میزکار وب",
                                subtitle = "استخراج حرفه‌ای، Excel و مدیریت کامل",
                                icon = DfIcons.ExternalLink,
                                trailing = "divarfiling.ir",
                                onClick = { openWeb("https://divarfiling.ir/") },
                                showDivider = true,
                            )
                            SettingsInfoRow(
                                title = "نصب امن",
                                subtitle = "در صورت هشدار Play Protect گزینه Install anyway",
                                icon = DfIcons.Lock,
                                onClick = onNavigateInstallHelp,
                                trailing = "راهنما",
                                showDivider = true,
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
                        color = DfThemeColors.textMuted(),
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }
        }
    }
}
