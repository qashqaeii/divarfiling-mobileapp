package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.feature.postcall.permission.MiuiBackgroundPopupStatus
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionSnapshot
import ir.divarfiling.mobile.feature.settings.NotificationPrefRow
import ir.divarfiling.mobile.feature.settings.SettingsInfoRow
import ir.divarfiling.mobile.feature.settings.SettingsSectionTitle

@Composable
fun PostCallSettingsBlock(
    enabled: Boolean,
    permissions: PostCallPermissionSnapshot,
    isBusy: Boolean,
    statusLine: String?,
    onToggle: (Boolean) -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenMiuiBackgroundSettings: (() -> Unit)? = null,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsSectionTitle(
            title = "دستیار پس از تماس",
            subtitle = "ثبت سریع مخاطب یا یادداشت بعد از تماس",
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )
        DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
            Column {
                NotificationPrefRow(
                    title = "فعال‌سازی دستیار",
                    subtitle = "تشخیص پایان تماس و پیشنهاد ثبت در CRM",
                    icon = DfIcons.Phone,
                    checked = enabled,
                    onCheckedChange = onToggle,
                    enabled = !isBusy,
                )
                if (enabled) {
                    Text(
                        buildPermissionSummary(permissions),
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                    )
                    statusLine?.let {
                        Text(
                            it,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                        )
                    }
                    SettingsInfoRow(
                        title = "پنجره روی سایر اپ‌ها",
                        subtitle = if (permissions.canShowOverlay) "فعال" else "برای نمایش روی شماره‌گیر نیاز است",
                        icon = DfIcons.ExternalLink,
                        onClick = onOpenOverlaySettings,
                        showDivider = true,
                    )
                    if (permissions.isXiaomiFamilyDevice && onOpenMiuiBackgroundSettings != null) {
                        val miuiSubtitle = when (permissions.miuiBackgroundPopupStatus) {
                            MiuiBackgroundPopupStatus.UserAcknowledged -> "تأیید دستی ثبت شد"
                            else -> "نیازمند تأیید دستی (جدا از Overlay)"
                        }
                        SettingsInfoRow(
                            title = "پنجره پس‌زمینه MIUI",
                            subtitle = miuiSubtitle,
                            icon = DfIcons.ExternalLink,
                            onClick = onOpenMiuiBackgroundSettings,
                            showDivider = true,
                        )
                    }
                    SettingsInfoRow(
                        title = "مجوزهای تماس",
                        subtitle = "تنظیمات برنامه در اندروید",
                        icon = DfIcons.Settings,
                        onClick = onOpenAppSettings,
                    )
                }
            }
        }
    }
}

private fun buildPermissionSummary(snap: PostCallPermissionSnapshot): String {
    val parts = mutableListOf<String>()
    parts += if (snap.phoneStateGranted) "وضعیت تماس: بله" else "وضعیت تماس: خیر"
    parts += if (snap.callLogRuntimeAvailable) "گزارش تماس: بله" else "گزارش تماس: محدود"
    parts += if (snap.canShowOverlay) "Overlay: بله" else "Overlay: خیر"
    if (snap.isXiaomiFamilyDevice) {
        parts += when (snap.miuiBackgroundPopupStatus) {
            MiuiBackgroundPopupStatus.UserAcknowledged -> "پنجره پس‌زمینه MIUI: تأیید دستی"
            else -> "پنجره پس‌زمینه MIUI: نیازمند تأیید دستی"
        }
    }
    return parts.joinToString(" · ")
}
