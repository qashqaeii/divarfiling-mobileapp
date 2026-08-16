package ir.divarfiling.mobile.feature.update

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.update.UpdateDistribution
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfTextButton

@Composable
fun AppUpdateGate(
    viewModel: AppUpdateViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity,
    ),
    content: @Composable () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    if (!UpdateDistribution.usesInAppApkUpdate) {
        content()
        return
    }

    LaunchedEffect(Unit) {
        viewModel.checkOnLaunch()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.onInstallPermissionResult(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else {
                true
            },
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                state.phase == AppUpdatePhase.AwaitingInstallPermission
            ) {
                viewModel.onInstallPermissionResult(
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.packageManager.canRequestPackageInstalls()
                    } else {
                        true
                    },
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    content()

    if (state.visible) {
        AppUpdateSheet(
            state = state,
            onDismiss = {
                when (state.phase) {
                    AppUpdatePhase.UpToDate, AppUpdatePhase.Error -> viewModel.clearManualMessage()
                    else -> viewModel.dismissSoftUpdate()
                }
            },
            onUpdate = viewModel::startUpdate,
            onInstall = viewModel::installNow,
            onOpenPermissionSettings = {
                permissionLauncher.launch(viewModel.openInstallPermissionSettings())
            },
            onOpenWebsite = { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppUpdateSheet(
    state: AppUpdateUiState,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    onInstall: () -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onOpenWebsite: (String) -> Unit,
) {
    val version = state.version
    val canDismiss = !state.forceUpdate &&
        state.phase != AppUpdatePhase.Downloading &&
        state.phase != AppUpdatePhase.Installing

    DfModalBottomSheet(
        onDismissRequest = { if (canDismiss) onDismiss() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(bottom = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(DfColors.PurpleDark, DfColors.Purple, DfColors.PurpleGradientStart),
                        ),
                    )
                    .padding(AppSpacing.md),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = DfIcons.Download,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = when (state.phase) {
                                AppUpdatePhase.UpToDate -> "اپ به‌روز است"
                                AppUpdatePhase.Error -> "خطا در به‌روزرسانی"
                                AppUpdatePhase.AwaitingInstallPermission -> "اجازه نصب لازم است"
                                else -> if (state.forceUpdate) "به‌روزرسانی ضروری" else "نسخه جدید آماده است"
                            },
                            style = AppTypography.sectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = "نسخه فعلی ${DateUtils.toPersianDigits(BuildConfig.VERSION_NAME)}",
                            style = AppTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            if (version != null && state.phase != AppUpdatePhase.UpToDate) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "نسخه ${DateUtils.toPersianDigits(version.versionName)} " +
                            "(${DateUtils.toPersianDigits(version.versionCode.toString())})",
                        style = AppTypography.bodyDescription,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.TextPrimary,
                    )
                    if (version.apkSizeLabel.isNotBlank()) {
                        Text(
                            text = "حجم تقریبی: ${version.apkSizeLabel}",
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                        )
                    }
                    if (version.releaseNotes.isNotBlank()) {
                        Text(
                            text = version.releaseNotes,
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextSecondary,
                        )
                    }
                }
            }

            state.message?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.PurpleDark)
            }
            state.error?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.Rose)
            }

            if (state.phase == AppUpdatePhase.Downloading) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = DfColors.Purple,
                        trackColor = DfColors.PurpleContainer,
                    )
                    Text(
                        text = "در حال دانلود… ${DateUtils.toPersianDigits(state.progressLabel)}",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }

            when (state.phase) {
                AppUpdatePhase.Available, AppUpdatePhase.Error -> {
                    DfPrimaryButton(
                        text = if (state.forceUpdate) "به‌روزرسانی ضروری" else "دانلود و نصب",
                        onClick = onUpdate,
                    )
                    version?.websiteUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        DfTextButton(text = "دانلود از سایت", onClick = { onOpenWebsite(url) })
                    }
                    if (canDismiss) {
                        DfTextButton(text = "بعداً", onClick = onDismiss)
                    }
                }
                AppUpdatePhase.AwaitingInstallPermission -> {
                    DfPrimaryButton(
                        text = "فعال‌سازی اجازه نصب",
                        onClick = onOpenPermissionSettings,
                    )
                    if (canDismiss) {
                        DfTextButton(text = "بعداً", onClick = onDismiss)
                    }
                }
                AppUpdatePhase.ReadyToInstall -> {
                    DfPrimaryButton(text = "ادامه نصب", onClick = onInstall)
                    if (canDismiss) {
                        DfTextButton(text = "بعداً", onClick = onDismiss)
                    }
                }
                AppUpdatePhase.Downloading, AppUpdatePhase.Installing, AppUpdatePhase.Checking -> {
                    DfPrimaryButton(
                        text = if (state.phase == AppUpdatePhase.Checking) "در حال بررسی…" else "لطفاً صبر کنید…",
                        onClick = {},
                        enabled = false,
                        loading = true,
                    )
                }
                AppUpdatePhase.UpToDate -> {
                    DfPrimaryButton(text = "باشه", onClick = onDismiss)
                }
                AppUpdatePhase.Idle -> Unit
            }
        }
    }
}
