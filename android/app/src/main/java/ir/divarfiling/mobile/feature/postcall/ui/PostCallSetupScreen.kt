package ir.divarfiling.mobile.feature.postcall.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
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
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionSnapshot
import ir.divarfiling.mobile.feature.postcall.permission.PostCallSetupReadiness
import kotlinx.coroutines.launch

private enum class SetupRowStatus {
    Granted,
    Pending,
    Manual,
    UserConfirmed,
    Denied,
    Error,
}

private data class SetupPermissionRow(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: SetupRowStatus,
    val countsTowardProgress: Boolean,
    val onClick: () -> Unit,
    val onManualConfirm: (() -> Unit)? = null,
    val infoText: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCallSetupScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit = onBack,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val entry = remember(context) {
        EntryPointAccessors.fromApplication(context, PostCallEntryPoint::class.java)
    }
    val permissionManager = remember { entry.postCallPermissionManager() }
    val controller = remember { entry.postCallController() }
    var snapshot by remember { mutableStateOf(permissionManager.snapshot()) }
    var lockScreenEnabled by remember { mutableStateOf(permissionManager.isShowOnLockScreenEnabled()) }
    var infoDialogText by remember { mutableStateOf<String?>(null) }
    var enableError by remember { mutableStateOf<String?>(null) }

    var autoPermissionPassDone by remember { mutableStateOf(false) }
    var phonePermissionRequested by remember { mutableStateOf(false) }
    var callLogPermissionRequested by remember { mutableStateOf(false) }

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
    ) {
        refresh()
        if (!permissionManager.snapshot().phoneStateGranted) {
            autoPermissionPassDone = true
        } else if (!autoPermissionPassDone && !permissionManager.snapshot().callLogGranted) {
            callLogPermissionRequested = true
            callLogLauncher.launch(permissionManager.callLogPermission())
        } else {
            autoPermissionPassDone = true
        }
    }
    val callLogLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        refresh()
        autoPermissionPassDone = true
    }
    val notificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { refresh() }

    fun requestRuntimePermission(
        permission: String,
        wasRequestedBefore: Boolean,
        markRequested: () -> Unit,
        launch: (String) -> Unit,
    ) {
        val showRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } ?: true
        if (wasRequestedBefore && !showRationale) {
            runCatching { context.startActivity(permissionManager.appDetailsIntent()) }
            return
        }
        markRequested()
        launch(permission)
    }

    fun requestPhonePermission() {
        requestRuntimePermission(
            permission = permissionManager.phoneStatePermission(),
            wasRequestedBefore = phonePermissionRequested,
            markRequested = { phonePermissionRequested = true },
            launch = phoneLauncher::launch,
        )
    }

    fun requestCallLogPermission() {
        requestRuntimePermission(
            permission = permissionManager.callLogPermission(),
            wasRequestedBefore = callLogPermissionRequested,
            markRequested = { callLogPermissionRequested = true },
            launch = callLogLauncher::launch,
        )
    }

    fun continueAutoPermissionPass() {
        if (autoPermissionPassDone) return
        val snap = permissionManager.snapshot()
        when {
            !snap.phoneStateGranted -> requestPhonePermission()
            !snap.callLogGranted -> requestCallLogPermission()
            else -> autoPermissionPassDone = true
        }
    }

    LaunchedEffect(Unit) {
        continueAutoPermissionPass()
    }

    fun openSettings(intent: Intent) {
        runCatching { context.startActivity(intent) }
    }

    val rows = remember(snapshot, lockScreenEnabled, phonePermissionRequested, callLogPermissionRequested) {
        buildSetupRows(
            snapshot = snapshot,
            lockScreenEnabled = lockScreenEnabled,
            phoneWasRequested = phonePermissionRequested,
            callLogWasRequested = callLogPermissionRequested,
            onPhone = { requestPhonePermission() },
            onCallLog = { requestCallLogPermission() },
            onOverlay = { openSettings(permissionManager.overlaySettingsIntent()) },
            onMiuiPopupOpen = {
                val intent = permissionManager.miuiBackgroundPopupSettingsIntentOrNull()
                    ?: permissionManager.appDetailsIntent()
                openSettings(intent)
            },
            onMiuiPopupConfirm = {
                permissionManager.setMiuiBackgroundPopupUserAcknowledged(true)
                refresh()
            },
            onNotifications = {
                permissionManager.postNotificationsPermissionOrNull()?.let {
                    notificationsLauncher.launch(it)
                } ?: openSettings(permissionManager.appDetailsIntent())
            },
            onAutostart = {
                permissionManager.xiaomiAutostartIntentOrNull()?.let { openSettings(it) }
                    ?: openSettings(permissionManager.appDetailsIntent())
            },
            onBattery = { openSettings(permissionManager.batteryOptimizationIntent()) },
            onLockScreenToggle = {
                val next = !permissionManager.isShowOnLockScreenEnabled()
                permissionManager.setShowOnLockScreenEnabled(next)
                lockScreenEnabled = next
            },
        )
    }

    val measurable = rows.filter { it.countsTowardProgress }
    val grantedMeasurable = measurable.count { it.status == SetupRowStatus.Granted }
    val progress = if (measurable.isEmpty()) {
        0f
    } else {
        grantedMeasurable.toFloat() / measurable.size
    }
    val ready = PostCallSetupReadiness.meetsMinimumForEnable(snapshot)

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DfHubPageHeader(
                title = "دستیار پس از تماس",
                subtitle = "",
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
                PostCallPendingInboxSection()
            }
            item {
                Text(
                    "دستیار تماس را آماده کنیم",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "برای اینکه بعد از پایان تماس، اطلاعات مشتری و گزینه‌های ثبت یادداشت و پیگیری را ببینی، فایلینگ دیوار به چند دسترسی نیاز دارد.",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
                Text(
                    "تماس‌ها ضبط نمی‌شوند و فایل صوتی مکالمه ذخیره نمی‌شود.",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                    modifier = Modifier.padding(top = AppSpacing.sm),
                )
            }
            item {
                Text(
                    "دسترسی‌های موردنیاز",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.sm),
                    color = DfColors.Purple,
                )
                Text(
                    PostCallSetupReadiness.progressText(snapshot),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
            }
            items(rows, key = { it.id }) { row ->
                SetupPermissionRowView(
                    row = row,
                    onInfoClick = { text -> infoDialogText = text },
                )
            }
            enableError?.let { message ->
                item {
                    Text(
                        message,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.error(),
                    )
                }
            }
            item {
                DfSecondaryButton(
                    text = "بررسی مجدد",
                    onClick = { refresh() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                DfPrimaryButton(
                    text = "فعال‌سازی دستیار",
                    onClick = {
                        scope.launch {
                            enableError = null
                            refresh()
                            val snap = permissionManager.snapshot()
                            if (!PostCallSetupReadiness.meetsMinimumForEnable(snap)) {
                                enableError = PostCallSetupReadiness.enableBlockedReason(snap)
                                return@launch
                            }
                            val ok = controller.setEnabled(true)
                            if (ok) {
                                snackbarHostState.showSnackbar(
                                    "دستیار فعال شد! از این به بعد بعد از تماس‌ها می‌توانی نتیجه مکالمه و پیگیری‌ها را سریع ثبت کنی.",
                                )
                                onFinished()
                            } else {
                                enableError = "فعال‌سازی انجام نشد. دسترسی‌های بالا را کامل کن و دوباره تلاش کن."
                            }
                        }
                    },
                    enabled = ready,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    infoDialogText?.let { body ->
        AlertDialog(
            onDismissRequest = { infoDialogText = null },
            title = { Text("راهنما") },
            text = { Text(body, style = AppTypography.bodyDescription) },
            confirmButton = {
                TextButton(onClick = { infoDialogText = null }) {
                    Text("متوجه شدم", color = DfColors.Purple)
                }
            },
        )
    }
}

@Composable
private fun SetupPermissionRowView(
    row: SetupPermissionRow,
    onInfoClick: (String) -> Unit,
) {
    val statusLabel = when (row.status) {
        SetupRowStatus.Granted -> "فعال"
        SetupRowStatus.Pending -> "نیاز به اقدام"
        SetupRowStatus.Manual -> "نیاز به تنظیم دستی"
        SetupRowStatus.UserConfirmed -> "تأییدشده توسط شما"
        SetupRowStatus.Denied -> "رد شده — دوباره تلاش کن"
        SetupRowStatus.Error -> "مشکل در بررسی دسترسی"
    }
    val statusTint = when (row.status) {
        SetupRowStatus.Granted -> Color(0xFF2E7D32)
        SetupRowStatus.UserConfirmed -> Color(0xFFE65100)
        SetupRowStatus.Manual -> DfThemeColors.textSecondary()
        SetupRowStatus.Denied -> DfThemeColors.error()
        SetupRowStatus.Error -> DfThemeColors.error()
        SetupRowStatus.Pending -> DfThemeColors.textSecondary()
    }
    DfCard(
        onClick = row.onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                when (row.status) {
                    SetupRowStatus.Granted -> {
                        Icon(
                            DfIcons.CircleCheck,
                            contentDescription = "فعال",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    SetupRowStatus.UserConfirmed -> {
                        Icon(
                            DfIcons.CircleCheck,
                            contentDescription = "تأییدشده توسط شما",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    else -> {
                        Icon(
                            DfIcons.Settings,
                            contentDescription = null,
                            tint = DfThemeColors.textMuted(),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                    ) {
                        Text(
                            row.title,
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        row.infoText?.let { info ->
                            IconButton(
                                onClick = { onInfoClick(info) },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    DfIcons.Info,
                                    contentDescription = "راهنمای ${row.title}",
                                    tint = DfThemeColors.textMuted(),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    Text(
                        row.subtitle,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                        modifier = Modifier.padding(top = AppSpacing.xxs),
                    )
                    Text(
                        statusLabel,
                        style = AppTypography.labelSmall,
                        color = statusTint,
                        modifier = Modifier.padding(top = AppSpacing.xxs),
                    )
                }
            }
            row.onManualConfirm?.let { confirm ->
                TextButton(
                    onClick = confirm,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("انجام دادم", color = DfColors.Purple)
                }
            }
        }
    }
}

private fun buildSetupRows(
    snapshot: PostCallPermissionSnapshot,
    lockScreenEnabled: Boolean,
    phoneWasRequested: Boolean,
    callLogWasRequested: Boolean,
    onPhone: () -> Unit,
    onCallLog: () -> Unit,
    onOverlay: () -> Unit,
    onMiuiPopupOpen: () -> Unit,
    onMiuiPopupConfirm: () -> Unit,
    onNotifications: () -> Unit,
    onAutostart: () -> Unit,
    onBattery: () -> Unit,
    onLockScreenToggle: () -> Unit,
): List<SetupPermissionRow> = buildList {
    add(
        SetupPermissionRow(
            id = "phone",
            title = "تشخیص پایان تماس",
            subtitle = "تا فایلینگ دیوار متوجه شود چه زمانی تماس تمام شده است.",
            status = when {
                snapshot.phoneStateGranted -> SetupRowStatus.Granted
                phoneWasRequested -> SetupRowStatus.Denied
                else -> SetupRowStatus.Pending
            },
            countsTowardProgress = true,
            onClick = onPhone,
        ),
    )
    add(
        SetupPermissionRow(
            id = "call_log",
            title = "اطلاعات تماس‌ها",
            subtitle = "برای شناخت شماره تماس و نمایش تاریخ و مدت مکالمه.",
            status = when {
                snapshot.callLogRuntimeAvailable -> SetupRowStatus.Granted
                snapshot.callLogGranted && !snapshot.callLogRuntimeAvailable -> SetupRowStatus.Error
                callLogWasRequested && !snapshot.callLogGranted -> SetupRowStatus.Denied
                else -> SetupRowStatus.Pending
            },
            countsTowardProgress = true,
            onClick = onCallLog,
        ),
    )
    add(
        SetupPermissionRow(
            id = "overlay",
            title = "نمایش دستیار روی صفحه",
            subtitle = "تا بعد از تماس، پنجره کوچک دستیار روی گوشی ظاهر شود.",
            status = if (snapshot.canShowOverlay) SetupRowStatus.Granted else SetupRowStatus.Pending,
            countsTowardProgress = true,
            onClick = onOverlay,
            infoText = "در صفحه تنظیمات، فایلینگ دیوار را از فهرست برنامه‌ها انتخاب کن و گزینه «اجازه نمایش روی سایر برنامه‌ها» را روشن کن. بعد به فایلینگ دیوار برگرد.",
        ),
    )
    if (snapshot.isXiaomiFamilyDevice) {
        val miuiAcked = snapshot.miuiBackgroundPopupStatus == MiuiBackgroundPopupStatus.UserAcknowledged
        add(
            SetupPermissionRow(
                id = "miui_popup",
                title = "پنجره پس‌زمینه",
                subtitle = "برای نمایش دستیار وقتی برنامه در پس‌زمینه است.",
                status = if (miuiAcked) SetupRowStatus.UserConfirmed else SetupRowStatus.Manual,
                countsTowardProgress = false,
                onClick = onMiuiPopupOpen,
                onManualConfirm = if (miuiAcked) null else onMiuiPopupConfirm,
                infoText = "در تنظیمات دسترسی‌های فایلینگ دیوار، گزینه نمایش پنجره هنگام اجرای برنامه در پس‌زمینه را فعال کن و سپس به برنامه برگرد.",
            ),
        )
    }
    add(
        SetupPermissionRow(
            id = "notifications",
            title = "اعلان‌های دستیار",
            subtitle = "اگر پنجره دستیار نمایش داده نشد، بتوانی از طریق اعلان به آن برگردی.",
            status = if (snapshot.canNotify) SetupRowStatus.Granted else SetupRowStatus.Pending,
            countsTowardProgress = true,
            onClick = onNotifications,
        ),
    )
    if (snapshot.isXiaomiFamilyDevice) {
        add(
            SetupPermissionRow(
                id = "autostart",
                title = "اجرای خودکار",
                subtitle = "تا بعد از تماس بتواند دستیار را به‌موقع نشان دهد.",
                status = SetupRowStatus.Manual,
                countsTowardProgress = false,
                onClick = onAutostart,
                infoText = "اگر گوشی اجازه اجرای خودکار برنامه‌ها را مدیریت می‌کند، اجرای خودکار فایلینگ دیوار را فعال کن تا بعد از تماس بتواند دستیار را نمایش دهد.",
            ),
        )
    }
    add(
        SetupPermissionRow(
            id = "battery",
            title = "فعالیت در پس‌زمینه",
            subtitle = "اگر گوشی برنامه را در پس‌زمینه محدود می‌کند.",
            status = SetupRowStatus.Manual,
            countsTowardProgress = false,
            onClick = onBattery,
            infoText = "اگر گوشی فعالیت برنامه را در پس‌زمینه محدود می‌کند، از تنظیمات باتری اجازه فعالیت پس‌زمینه فایلینگ دیوار را بده.",
        ),
    )
    add(
        SetupPermissionRow(
            id = "lock_screen",
            title = "نمایش روی صفحه قفل",
            subtitle = if (lockScreenEnabled) "روشن" else "خاموش (اختیاری)",
            status = if (lockScreenEnabled) SetupRowStatus.UserConfirmed else SetupRowStatus.Pending,
            countsTowardProgress = false,
            onClick = onLockScreenToggle,
        ),
    )
}
