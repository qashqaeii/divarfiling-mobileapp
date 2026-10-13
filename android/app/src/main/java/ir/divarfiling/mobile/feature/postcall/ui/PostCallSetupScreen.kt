package ir.divarfiling.mobile.feature.postcall.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dagger.hilt.android.EntryPointAccessors
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.feature.postcall.PostCallEntryPoint
import ir.divarfiling.mobile.feature.postcall.permission.MiuiBackgroundPopupStatus
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionSnapshot
import ir.divarfiling.mobile.feature.settings.SettingsInfoRow
import kotlinx.coroutines.launch

private enum class SetupRowStatus {
    Granted,
    Pending,
    Manual,
    Denied,
}

private data class SetupPermissionRow(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: SetupRowStatus,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCallSetupScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit = onBack,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val entry = remember(context) {
        EntryPointAccessors.fromApplication(context, PostCallEntryPoint::class.java)
    }
    val permissionManager = remember { entry.postCallPermissionManager() }
    val controller = remember { entry.postCallController() }
    var snapshot by remember { mutableStateOf(permissionManager.snapshot()) }
    var lockScreenEnabled by remember { mutableStateOf(permissionManager.isShowOnLockScreenEnabled()) }

    fun refresh() {
        snapshot = permissionManager.snapshot()
        lockScreenEnabled = permissionManager.isShowOnLockScreenEnabled()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val phoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { refresh() }
    val callLogLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { refresh() }
    val notificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { refresh() }

    fun openSettings(intent: Intent) {
        runCatching { context.startActivity(intent) }
    }

    val rows = remember(snapshot, lockScreenEnabled, permissionManager) {
        buildSetupRows(
            snapshot = snapshot,
            permissionManager = permissionManager,
            lockScreenEnabled = lockScreenEnabled,
            onPhone = { phoneLauncher.launch(permissionManager.phoneStatePermission()) },
            onCallLog = { callLogLauncher.launch(permissionManager.callLogPermission()) },
            onOverlay = { openSettings(permissionManager.overlaySettingsIntent()) },
            onMiuiPopup = {
                val intent = permissionManager.miuiBackgroundPopupSettingsIntentOrNull()
                    ?: permissionManager.appDetailsIntent()
                openSettings(intent)
                permissionManager.setMiuiBackgroundPopupUserAcknowledged(true)
            },
            onNotifications = {
                permissionManager.postNotificationsPermissionOrNull()?.let {
                    notificationsLauncher.launch(it)
                }
            },
            onAutostart = {
                permissionManager.xiaomiAutostartIntentOrNull()?.let { openSettings(it) }
            },
            onBattery = { openSettings(permissionManager.batteryOptimizationIntent()) },
            onLockScreenToggle = {
                val next = !permissionManager.isShowOnLockScreenEnabled()
                permissionManager.setShowOnLockScreenEnabled(next)
                lockScreenEnabled = next
            },
        )
    }

    val progress = rows.count { it.status == SetupRowStatus.Granted }.toFloat() /
        rows.count { it.id != "lock_screen" }.coerceAtLeast(1).toFloat()
    val ready = snapshot.phoneStateGranted

    Scaffold(
        containerColor = DfScreenContainerColor,
        topBar = {
            DfHubPageHeader(
                title = "راه‌اندازی دستیار پس از تماس",
                subtitle = "مدیریت دسترسی‌ها در یک صفحه",
                onBack = onBack,
                modifier = Modifier.statusBarsPadding(),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = AppSpacing.screenHorizontal,
                vertical = AppSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                Text(
                    if (ready) "دستیار آماده فعال‌سازی است" else "برای تشخیص پایان تماس، وضعیت تلفن الزامی است",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                )
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.sm),
                    color = DfColors.Purple,
                )
                Text(
                    "${(progress * 100).toInt()}٪ از دسترسی‌های قابل بررسی",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
            }
            if (snapshot.isXiaomiFamilyDevice) {
                item {
                    DfCard {
                        Text(
                            "در گوشی‌های شیائومی / ردمی / پوکو، علاوه بر Overlay، «پنجره پس‌زمینه» و «اجرای خودکار» را در تنظیمات امنیتی فعال کنید.",
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textSecondary(),
                            modifier = Modifier.padding(AppSpacing.md),
                        )
                    }
                }
            }
            items(rows, key = { it.id }) { row ->
                SetupPermissionRowView(row = row)
            }
            item {
                DfSecondaryButton(
                    text = "بررسی مجدد",
                    onClick = refresh,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                DfPrimaryButton(
                    text = "فعال‌سازی دستیار",
                    onClick = {
                        scope.launch {
                            controller.setEnabled(true)
                            onFinished()
                        }
                    },
                    enabled = ready,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SetupPermissionRowView(row: SetupPermissionRow) {
    val statusLabel = when (row.status) {
        SetupRowStatus.Granted -> "فعال"
        SetupRowStatus.Pending -> "نیاز به فعال‌سازی"
        SetupRowStatus.Manual -> "تأیید دستی"
        SetupRowStatus.Denied -> "رد شده / نامشخص"
    }
    DfCard {
        SettingsInfoRow(
            title = row.title,
            subtitle = "${row.subtitle} · $statusLabel",
            icon = DfIcons.Settings,
            onClick = row.onClick,
        )
    }
}

private fun buildSetupRows(
    snapshot: PostCallPermissionSnapshot,
    permissionManager: PostCallPermissionManager,
    lockScreenEnabled: Boolean,
    onPhone: () -> Unit,
    onCallLog: () -> Unit,
    onOverlay: () -> Unit,
    onMiuiPopup: () -> Unit,
    onNotifications: () -> Unit,
    onAutostart: () -> Unit,
    onBattery: () -> Unit,
    onLockScreenToggle: () -> Unit,
): List<SetupPermissionRow> = buildList {
    add(
        SetupPermissionRow(
            id = "phone",
            title = "وضعیت تلفن",
            subtitle = "تشخیص پایان تماس",
            status = if (snapshot.phoneStateGranted) SetupRowStatus.Granted else SetupRowStatus.Pending,
            onClick = onPhone,
        ),
    )
    add(
        SetupPermissionRow(
            id = "call_log",
            title = "تاریخچه تماس",
            subtitle = "شماره، تاریخ و مدت مکالمه",
            status = when {
                snapshot.callLogRuntimeAvailable -> SetupRowStatus.Granted
                snapshot.callLogGranted -> SetupRowStatus.Manual
                else -> SetupRowStatus.Pending
            },
            onClick = onCallLog,
        ),
    )
    add(
        SetupPermissionRow(
            id = "overlay",
            title = "نمایش روی سایر برنامه‌ها",
            subtitle = "پنجره فشرده پس از تماس",
            status = if (snapshot.canShowOverlay) SetupRowStatus.Granted else SetupRowStatus.Pending,
            onClick = onOverlay,
        ),
    )
    if (snapshot.isXiaomiFamilyDevice) {
        add(
            SetupPermissionRow(
                id = "miui_popup",
                title = "پنجره پس‌زمینه شیائومی",
                subtitle = "جدا از Overlay سیستم",
                status = when (snapshot.miuiBackgroundPopupStatus) {
                    MiuiBackgroundPopupStatus.UserAcknowledged -> SetupRowStatus.Manual
                    else -> SetupRowStatus.Pending
                },
                onClick = onMiuiPopup,
            ),
        )
    }
    add(
        SetupPermissionRow(
            id = "notifications",
            title = "اعلان‌ها",
            subtitle = "وقتی اپ در پس‌زمینه است",
            status = if (snapshot.canNotify) SetupRowStatus.Granted else SetupRowStatus.Pending,
            onClick = onNotifications,
        ),
    )
    if (snapshot.isXiaomiFamilyDevice) {
        add(
            SetupPermissionRow(
                id = "autostart",
                title = "اجرای خودکار شیائومی",
                subtitle = "بدون تأیید سیستمی قابل بررسی نیست",
                status = SetupRowStatus.Manual,
                onClick = onAutostart,
            ),
        )
    }
    add(
        SetupPermissionRow(
            id = "battery",
            title = "محدودیت باتری",
            subtitle = "اجرای پایدار در پس‌زمینه",
            status = SetupRowStatus.Manual,
            onClick = onBattery,
        ),
    )
    add(
        SetupPermissionRow(
            id = "lock_screen",
            title = "نمایش روی صفحه قفل",
            subtitle = if (lockScreenEnabled) "روشن (پیش‌فرض خاموش)" else "خاموش — فقط اعلان عمومی روی قفل",
            status = if (lockScreenEnabled) SetupRowStatus.Manual else SetupRowStatus.Pending,
            onClick = onLockScreenToggle,
        ),
    )
}
