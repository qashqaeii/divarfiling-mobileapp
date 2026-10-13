package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.feature.postcall.permission.MiuiBackgroundPopupStatus
import ir.divarfiling.mobile.feature.postcall.permission.PostCallHealthSnapshot
import ir.divarfiling.mobile.feature.settings.SettingsInfoRow
import ir.divarfiling.mobile.feature.settings.SettingsSectionTitle

@Composable
fun PostCallHealthBlock(
    health: PostCallHealthSnapshot,
    onOpenOverlay: () -> Unit,
    onOpenMiui: () -> Unit,
    onOpenBattery: () -> Unit,
    onOpenAutostart: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsSectionTitle(
            title = "بررسی سلامت دستیار",
            subtitle = "وضعیت تشخیص تماس و نمایش پس از تماس",
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal),
        ) {
            HealthLine("تشخیص تماس", health.callDetectionReady)
            HealthLine("گزارش تماس", health.callLogReady, unknown = health.callLogUnknown)
            HealthLine("Overlay استاندارد", health.overlayReady)
            if (health.isXiaomiFamily) {
                Text(
                    when (health.miuiBackgroundPopupStatus) {
                        MiuiBackgroundPopupStatus.UserAcknowledged -> "پنجره پس‌زمینه MIUI: تأیید دستی"
                        else -> "پنجره پس‌زمینه MIUI: نیازمند تأیید دستی"
                    },
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                    modifier = Modifier.padding(vertical = AppSpacing.xxs),
                )
            }
            HealthLine("اعلان‌ها", health.notificationsReady)
            if (health.batteryRestrictionHint) {
                Text(
                    "محدودیت باتری/Autostart: احتمالاً نیازمند بررسی دستی",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                    modifier = Modifier.padding(vertical = AppSpacing.xxs),
                )
            }
            SettingsInfoRow(
                title = "مجوز Overlay",
                subtitle = "تنظیمات سیستم",
                icon = DfIcons.ExternalLink,
                onClick = onOpenOverlay,
                showDivider = true,
            )
            if (health.isXiaomiFamily) {
                SettingsInfoRow(
                    title = "مجوزهای MIUI",
                    subtitle = "پنجره پس‌زمینه و محدودیت‌ها",
                    icon = DfIcons.Settings,
                    onClick = onOpenMiui,
                    showDivider = true,
                )
                SettingsInfoRow(
                    title = "Autostart",
                    subtitle = "در صورت وجود در تنظیمات سازنده",
                    icon = DfIcons.ExternalLink,
                    onClick = onOpenAutostart,
                    showDivider = true,
                )
            }
            SettingsInfoRow(
                title = "بهینه‌سازی باتری",
                subtitle = "غیرفعال‌کردن محدودیت برای فایلینگ دیوار",
                icon = DfIcons.ExternalLink,
                onClick = onOpenBattery,
                showDivider = true,
            )
            SettingsInfoRow(
                title = "تنظیمات برنامه",
                subtitle = "سایر مجوزها",
                icon = DfIcons.Settings,
                onClick = onOpenAppSettings,
            )
        }
    }
}

@Composable
private fun HealthLine(label: String, ok: Boolean, unknown: Boolean = false) {
    val value = when {
        unknown -> "نامشخص"
        ok -> "فعال"
        else -> "نیاز به اقدام"
    }
    val color = when {
        unknown -> DfThemeColors.textSecondary()
        ok -> DfColors.Purple
        else -> DfThemeColors.error()
    }
    Text(
        "$label: $value",
        style = AppTypography.labelSmall,
        color = color,
        modifier = Modifier.padding(vertical = AppSpacing.xxs),
    )
}
