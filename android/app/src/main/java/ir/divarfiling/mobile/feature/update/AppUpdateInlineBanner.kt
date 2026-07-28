package ir.divarfiling.mobile.feature.update

import android.net.Uri
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton

@Composable
fun AppUpdateInlineBanner(
    state: AppUpdateUiState,
    onPrimaryClick: () -> Unit,
    onOpenStore: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (!state.visible || state.phase == AppUpdatePhase.Idle || state.phase == AppUpdatePhase.UpToDate) return

    val version = state.version
    val title = when (state.phase) {
        AppUpdatePhase.Available -> if (state.forceUpdate) "به‌روزرسانی ضروری" else "نسخه جدید آماده است"
        AppUpdatePhase.Downloading -> "در حال دانلود نسخه جدید"
        AppUpdatePhase.ReadyToInstall -> "نسخه جدید آماده نصب است"
        AppUpdatePhase.AwaitingInstallPermission -> "اجازه نصب لازم است"
        AppUpdatePhase.Installing -> "در حال آماده‌سازی نصب"
        AppUpdatePhase.Error -> "خطا در به‌روزرسانی"
        AppUpdatePhase.Checking -> "در حال بررسی نسخه"
        else -> "به‌روزرسانی اپ"
    }
    val message = when {
        state.error != null -> state.error
        state.phase == AppUpdatePhase.Downloading && state.progressLabel.isNotBlank() ->
            "دانلود نسخه ${version?.versionName.orEmpty()} - ${state.progressLabel}"
        state.phase == AppUpdatePhase.ReadyToInstall ->
            "فایل آپدیت دانلود شده و برای نصب آماده است."
        state.phase == AppUpdatePhase.AwaitingInstallPermission ->
            "برای ادامه نصب، اجازه نصب از منابع ناشناس را فعال کنید."
        version != null && version.releaseNotes.isNotBlank() -> version.releaseNotes
        version != null -> "نسخه ${version.versionName} در دسترس است."
        else -> state.message ?: "وضعیت به‌روزرسانی اپ را بررسی کنید."
    }
    val actionLabel = when (state.phase) {
        AppUpdatePhase.Available, AppUpdatePhase.Error -> if (state.forceUpdate) "به‌روزرسانی ضروری" else "دانلود و نصب"
        AppUpdatePhase.AwaitingInstallPermission -> "فعال‌سازی اجازه نصب"
        AppUpdatePhase.ReadyToInstall -> "ادامه نصب"
        AppUpdatePhase.Downloading, AppUpdatePhase.Installing, AppUpdatePhase.Checking -> "در حال انجام…"
        else -> "مشاهده"
    }

    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = title,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = if (state.forceUpdate || state.error != null) DfColors.Rose else DfColors.PurpleDark,
            )
            Text(
                text = message,
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
            )
            DfPrimaryButton(
                text = actionLabel,
                onClick = onPrimaryClick,
                enabled = state.phase !in setOf(
                    AppUpdatePhase.Downloading,
                    AppUpdatePhase.Installing,
                    AppUpdatePhase.Checking,
                ),
                loading = state.phase in setOf(
                    AppUpdatePhase.Downloading,
                    AppUpdatePhase.Installing,
                    AppUpdatePhase.Checking,
                ),
            )
            version?.storeUrl?.takeIf { it.isNotBlank() }?.let {
                DfSecondaryButton(
                    text = "باز کردن صفحه نسخه",
                    onClick = { onOpenStore(it) },
                )
            }
        }
    }
}
