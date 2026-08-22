package ir.divarfiling.mobile.feature.settings

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfDestructiveButton
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.UserDto

@Composable
fun SettingsHeroCard(
    user: UserDto?,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initials = user?.fullName
        ?.trim()
        ?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.toString() }
        ?.take(2)
        ?.joinToString("")
        ?.ifBlank { "؟" } ?: "؟"

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = Color.Transparent,
        border = BorderStroke(1.dp, AppColors.OutlineSubtle),
        shadowElevation = AppElevations.subtle,
        tonalElevation = AppElevations.none,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(AppColors.PurpleGradientStart, AppColors.PurpleGradientEnd),
                    ),
                )
                .padding(AppSpacing.cardPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AppColors.Surface.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!user?.avatarUrl.isNullOrBlank()) {
                        DfAsyncImage(
                            url = user?.avatarUrl,
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            contentDescription = "آواتار",
                        )
                    } else {
                        Text(
                            initials,
                            style = AppTypography.sectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Surface,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                ) {
                    Text(
                        user?.fullName ?: "مشاور املاک",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Surface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    user?.agencyName?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            color = AppColors.Surface.copy(alpha = 0.88f),
                            style = AppTypography.bodyDescription,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    user?.phone?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            color = AppColors.Surface.copy(alpha = 0.75f),
                            style = AppTypography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(onClick = onEditProfile) {
                    Icon(DfIcons.Pencil, contentDescription = "ویرایش", tint = AppColors.Surface)
                }
            }
        }
    }
}

@Composable
fun LicenseInsightCard(
    license: LicenseState,
    onRenew: (() -> Unit)? = null,
    onOpenDashboard: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = license.expiryProgress,
        label = "licenseProgress",
    )
    val accent = when {
        !license.valid -> AppColors.Rose
        license.expiringSoon -> AppColors.Amber
        else -> AppColors.Green
    }
    val bg = when {
        !license.valid -> AppColors.RoseLight
        license.expiringSoon -> AppColors.AmberLight
        else -> AppColors.GreenLight
    }

    DfCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = bg,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                ) {
                    Text(
                        "وضعیت لایسنس",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        license.licenseLabel,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                    Text(
                        license.expiryHeadline,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                    license.expiresAt?.let { raw ->
                        val formatted = DateUtils.formatJalaliDateTime(raw) ?: DateUtils.formatJalaliDate(raw)
                        if (!formatted.isNullOrBlank()) {
                            Text(
                                "پایان: $formatted",
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textSecondary(),
                            )
                        }
                    }
                }
                Icon(
                    DfIcons.Sparkles,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp),
                )
            }
            if (!license.valid) {
                DfStatusBanner(
                    message = "پس از خرید با همین حساب وارد اپ شوید. هر لایسنس: یک ویندوز + یک اندروید.",
                    tone = DfStatusTone.Locked,
                    title = "لایسنس فعال نیست",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "زمان باقی‌مانده",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
                Text(
                    "${license.expiryProgressPercent}٪",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(AppShapes.Chip),
                color = accent,
                trackColor = AppColors.Surface.copy(alpha = 0.65f),
            )
            license.expiresAt?.takeIf { it.isNotBlank() }?.let {
                val formatted = DateUtils.formatForDisplay(it)
                Text(
                    "انقضا: $formatted",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            FeatureChips(license)
            if (onRenew != null && (!license.valid || license.expiringSoon)) {
                DfPrimaryButton(
                    text = if (license.valid) "تمدید لایسنس" else "خرید / فعال‌سازی لایسنس",
                    onClick = onRenew,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (onRenew != null && license.valid && !license.expiringSoon) {
                DfSecondaryButton(
                    text = "مدیریت لایسنس",
                    onClick = onRenew,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (onOpenDashboard != null) {
                DfSecondaryButton(
                    text = "مشاهده کلید در داشبورد",
                    onClick = onOpenDashboard,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeatureChips(license: LicenseState) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        FeatureChip("مدیریت مشتری", license.valid && license.crmEnabled)
        FeatureChip("فایلینگ", license.valid && license.filingEnabled)
        FeatureChip("استخراج فایل", license.canUseLightExtract)
        FeatureChip("Push", license.valid)
    }
}

@Composable
private fun FeatureChip(label: String, enabled: Boolean) {
    DfBadge(
        text = label,
        color = if (enabled) AppColors.GreenLight else DfThemeColors.surfaceVariant(),
        textColor = if (enabled) AppColors.Green else DfThemeColors.textMuted(),
    )
}

@Composable
fun SettingsSectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    ir.divarfiling.mobile.core.design.components.DfSectionHeader(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
    )
}

@Composable
fun SettingsRowDivider() {
    HorizontalDivider(
        color = DfThemeColors.outlineSubtle(),
        modifier = Modifier.padding(vertical = AppSpacing.xxs),
    )
}

@Composable
fun NotificationPrefRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    showDivider: Boolean = false,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (showDivider) SettingsRowDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = AppSpacing.listRowMinHeight)
                .padding(vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(AppShapes.IconContainer)
                    .background(DfThemeColors.primaryContainer()),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    iconRes != null -> DfDecorImage(resId = iconRes, size = 20.dp)
                    icon != null -> Icon(
                        icon,
                        contentDescription = null,
                        tint = DfThemeColors.primary(),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Text(
                    title,
                    style = AppTypography.bodyDescription,
                    fontWeight = FontWeight.Medium,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
    }
}

@Composable
fun DigestHourPicker(
    hour: Int,
    onHourChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        SettingsRowDivider()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "ساعت خلاصه روزانه",
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.Medium,
                color = DfThemeColors.textPrimary(),
            )
            DfBadge(
                text = String.format("%02d:00", hour),
                color = AppColors.BlueLight,
                textColor = AppColors.Blue,
            )
        }
        Slider(
            value = hour.toFloat(),
            onValueChange = { onHourChange(it.toInt()) },
            valueRange = 6f..22f,
            steps = 15,
        )
        Text(
            "اعلان «کارهای امروز» و پیگیری معوق حدود این ساعت ارسال می‌شود.",
            style = AppTypography.labelSmall,
            color = DfThemeColors.textMuted(),
        )
    }
}

@Composable
fun SettingsInfoRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailing: String? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = false,
) {
    val content: @Composable () -> Unit = {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (showDivider) SettingsRowDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = AppSpacing.listRowMinHeight)
                    .padding(vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(AppShapes.IconContainer)
                        .background(DfThemeColors.surfaceVariant()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = DfThemeColors.textSecondary(),
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                ) {
                    Text(
                        title,
                        style = AppTypography.bodyDescription,
                        fontWeight = FontWeight.Medium,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        subtitle,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                when {
                    trailing != null -> Text(
                        trailing,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.primary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    onClick != null -> Icon(
                        DfIcons.ChevronLeft,
                        contentDescription = null,
                        tint = DfThemeColors.textMuted(),
                    )
                }
            }
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, color = Color.Transparent) { content() }
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditSheet(
    visible: Boolean,
    fullName: String,
    phone: String,
    avatarUrl: String?,
    isSaving: Boolean,
    isUploadingAvatar: Boolean,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPickAvatar: () -> Unit,
    onRemoveAvatar: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    if (!visible) return
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "ویرایش پروفایل",
            subtitle = "نام، شماره و عکس نمایشی خود را به‌روز کنید",
            icon = DfIcons.Pencil,
            onClose = onDismiss,
            footer = {
                DfSheetActions(
                    primaryText = if (isSaving) "در حال ذخیره…" else "ذخیره تغییرات",
                    onPrimary = onSave,
                    primaryEnabled = !isSaving && !isUploadingAvatar && fullName.isNotBlank(),
                    isSubmitting = isSaving,
                    onSecondary = onDismiss,
                )
            },
        ) {
            DfSheetSection(title = "عکس پروفایل") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AppColors.PurpleContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!avatarUrl.isNullOrBlank()) {
                            DfAsyncImage(
                                url = avatarUrl,
                                modifier = Modifier.size(64.dp),
                                shape = CircleShape,
                                contentDescription = "آواتار",
                            )
                        } else {
                            Text("عکس", style = AppTypography.labelSmall)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        DfGlassTextButton(
                            text = if (isUploadingAvatar) "در حال بارگذاری…" else "انتخاب از گالری",
                            onClick = onPickAvatar,
                        )
                        if (!avatarUrl.isNullOrBlank()) {
                            DfGlassTextButton(
                                text = "حذف عکس",
                                onClick = onRemoveAvatar,
                            )
                        }
                    }
                }
            }
            DfSheetSection(title = "اطلاعات کاربری") {
                DfTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = "نام کامل",
                    enabled = !isSaving,
                )
                DfTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = "تلفن",
                    enabled = !isSaving,
                )
            }
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DfDestructiveButton(
        text = "خروج از حساب",
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    )
}
