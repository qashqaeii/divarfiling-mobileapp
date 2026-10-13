package ir.divarfiling.mobile.feature.postcall.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.feature.postcall.permission.MiuiBackgroundPopupStatus
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionManager
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionSnapshot

private enum class WizardStep {
    PhoneState,
    CallLog,
    Overlay,
    MiuiBackgroundPopup,
    Summary,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCallEnableWizard(
    visible: Boolean,
    permissionManager: PostCallPermissionManager,
    onDismiss: () -> Unit,
    onEnableConfirmed: () -> Unit,
) {
    if (!visible) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var snapshot by remember { mutableStateOf(permissionManager.snapshot()) }
    var stepIndex by remember { mutableIntStateOf(0) }

    fun refresh() {
        snapshot = permissionManager.snapshot()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isXiaomi = permissionManager.isXiaomiFamilyDevice()
    val steps = remember(isXiaomi) {
        buildList {
            add(WizardStep.PhoneState)
            add(WizardStep.CallLog)
            add(WizardStep.Overlay)
            if (isXiaomi) add(WizardStep.MiuiBackgroundPopup)
            add(WizardStep.Summary)
        }
    }
    val step = steps.getOrElse(stepIndex) { WizardStep.Summary }

    val phoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        refresh()
        if (granted || permissionManager.snapshot().phoneStateGranted) {
            stepIndex = (stepIndex + 1).coerceAtMost(steps.lastIndex)
        }
    }
    val callLogLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { refresh() }
    val notificationsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { refresh() }

    fun openSettings(intent: Intent) {
        runCatching { context.startActivity(intent) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg)
                .padding(bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                "راه‌اندازی دستیار پس از تماس",
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
            )
            LinearProgressIndicator(
                progress = { (stepIndex + 1).toFloat() / steps.size.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = DfColors.Purple,
            )
            WizardStepContent(
                step = step,
                snapshot = snapshot,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                when (step) {
                    WizardStep.PhoneState -> {
                        DfPrimaryButton(
                            text = if (snapshot.phoneStateGranted) "ادامه" else "اجازه وضعیت تماس",
                            onClick = {
                                if (snapshot.phoneStateGranted) {
                                    stepIndex++
                                } else {
                                    phoneLauncher.launch(permissionManager.phoneStatePermission())
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DfSecondaryButton(text = "بعداً", onClick = onDismiss, modifier = Modifier.weight(1f))
                    }
                    WizardStep.CallLog -> {
                        DfPrimaryButton(
                            text = when {
                                snapshot.callLogGranted -> "ادامه"
                                else -> "اجازه تاریخچه تماس"
                            },
                            onClick = {
                                if (snapshot.callLogGranted) {
                                    stepIndex++
                                } else {
                                    callLogLauncher.launch(permissionManager.callLogPermission())
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DfSecondaryButton(
                            text = "رد کردن",
                            onClick = { stepIndex++ },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    WizardStep.Overlay -> {
                        DfPrimaryButton(
                            text = if (snapshot.overlayGranted) "ادامه" else "باز کردن تنظیمات",
                            onClick = {
                                if (snapshot.overlayGranted) {
                                    stepIndex++
                                } else {
                                    openSettings(permissionManager.overlaySettingsIntent())
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DfSecondaryButton(
                            text = "بعداً",
                            onClick = { stepIndex++ },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    WizardStep.MiuiBackgroundPopup -> {
                        DfPrimaryButton(
                            text = "باز کردن مجوزهای شیائومی",
                            onClick = {
                                val intent = permissionManager.miuiBackgroundPopupSettingsIntentOrNull()
                                    ?: permissionManager.appDetailsIntent()
                                openSettings(intent)
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DfSecondaryButton(
                            text = "تأیید دستی انجام شد",
                            onClick = {
                                permissionManager.setMiuiBackgroundPopupUserAcknowledged(true)
                                refresh()
                                stepIndex++
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    WizardStep.Summary -> {
                        val notifPerm = permissionManager.postNotificationsPermissionOrNull()
                        if (notifPerm != null && !snapshot.notificationsGranted) {
                            DfSecondaryButton(
                                text = "اجازه اعلان",
                                onClick = { notificationsLauncher.launch(notifPerm) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        DfPrimaryButton(
                            text = "فعال‌سازی دستیار",
                            onClick = {
                                if (snapshot.phoneStateGranted) {
                                    onEnableConfirmed()
                                    onDismiss()
                                }
                            },
                            enabled = snapshot.phoneStateGranted,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DfSecondaryButton(
                            text = "بستن",
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardStepContent(
    step: WizardStep,
    snapshot: PostCallPermissionSnapshot,
) {
    when (step) {
        WizardStep.PhoneState -> WizardTextBlock(
            title = "۱. وضعیت تماس",
            body = "برای تشخیص پایان تماس، اجازه خواندن وضعیت تلفن لازم است.",
            status = statusLine(snapshot.phoneStateGranted, "فعال", "نیاز به اجازه"),
        )
        WizardStep.CallLog -> WizardTextBlock(
            title = "۲. تاریخچه تماس",
            body = "برای شماره معتبر بعد از تماس (به‌ویژه تماس بی‌پاسخ)، دسترسی به گزارش تماس توصیه می‌شود.",
            status = when {
                snapshot.callLogRuntimeAvailable -> "گزارش تماس: فعال"
                snapshot.callLogGranted -> "گزارش تماس: محدود"
                else -> "گزارش تماس: اختیاری — می‌توانید رد کنید"
            },
        )
        WizardStep.Overlay -> WizardTextBlock(
            title = "۳. نمایش روی سایر برنامه‌ها",
            body = "مجوز استاندارد اندروید را در تنظیمات سیستم فعال کنید. بدون آن، از اعلان به‌عنوان مسیر جایگزین استفاده می‌شود.",
            status = statusLine(snapshot.overlayGranted, "پنجره شناور: فعال", "پنجره شناور: غیرفعال"),
        )
        WizardStep.MiuiBackgroundPopup -> WizardTextBlock(
            title = "۴. پنجره پس‌زمینه (شیائومی)",
            body = "در تنظیمات MIUI گزینه «Display pop-up windows while running in the background» را برای فایلینگ دیوار فعال کنید. این مجوز با Overlay استاندارد یکی نیست.",
            status = when (snapshot.miuiBackgroundPopupStatus) {
                MiuiBackgroundPopupStatus.UserAcknowledged -> "وضعیت: تأیید دستی توسط شما"
                MiuiBackgroundPopupStatus.NeedsManualConfirmation -> "وضعیت: نیازمند تأیید دستی"
                MiuiBackgroundPopupStatus.NotApplicable -> ""
            },
        )
        WizardStep.Summary -> WizardTextBlock(
            title = "۵. بررسی نهایی",
            body = buildSummaryBody(snapshot),
            status = if (snapshot.phoneStateGranted) "آماده فعال‌سازی" else "ابتدا مجوز وضعیت تماس را بدهید",
        )
    }
}

@Composable
private fun WizardTextBlock(title: String, body: String, status: String) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(title, style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
        Text(body, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
        if (status.isNotBlank()) {
            Text(status, style = AppTypography.labelSmall, color = DfColors.Purple)
        }
    }
}

private fun statusLine(ok: Boolean, okText: String, failText: String): String =
    if (ok) okText else failText

private fun buildSummaryBody(snapshot: PostCallPermissionSnapshot): String {
    val lines = mutableListOf<String>()
    lines += if (snapshot.phoneStateGranted) "تشخیص تماس: بله" else "تشخیص تماس: خیر"
    lines += if (snapshot.callLogRuntimeAvailable) {
        "شناسایی شماره از گزارش تماس: بله"
    } else {
        "شناسایی شماره از گزارش تماس: محدود"
    }
    lines += if (snapshot.canShowOverlay) "Overlay استاندارد: بله" else "Overlay استاندارد: خیر"
    if (snapshot.isXiaomiFamilyDevice) {
        lines += when (snapshot.miuiBackgroundPopupStatus) {
            MiuiBackgroundPopupStatus.UserAcknowledged -> "پنجره پس‌زمینه MIUI: تأیید دستی"
            else -> "پنجره پس‌زمینه MIUI: نیازمند تأیید دستی"
        }
    }
    lines += if (snapshot.canNotify) "اعلان: بله" else "اعلان: خیر (پیشنهاد می‌شود)"
    return lines.joinToString("\n")
}
