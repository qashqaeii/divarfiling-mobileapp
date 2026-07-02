package ir.divarfiling.mobile.feature.settings

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfGlassButton
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
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
        shape = DfShapes.Card,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(DfColors.PurpleGradientStart, DfColors.PurpleGradientEnd),
                    ),
                )
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initials,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        user?.fullName ?: "مشاور املاک",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    user?.agencyName?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = Color.White.copy(alpha = 0.88f), style = MaterialTheme.typography.bodyMedium)
                    }
                    user?.phone?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                    }
                }
                IconButton(onClick = onEditProfile) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = Color.White)
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
        !license.valid -> DfColors.Rose
        license.expiringSoon -> DfColors.Amber
        else -> DfColors.Green
    }
    val bg = when {
        !license.valid -> DfColors.RoseLight
        license.expiringSoon -> DfColors.AmberLight
        else -> DfColors.GreenLight
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = DfShapes.Card,
        color = bg,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("وضعیت لایسنس", fontWeight = FontWeight.SemiBold)
                    Text(license.licenseLabel, color = DfColors.TextSecondary)
                    if (!license.valid) {
                        Text(
                            "پس از خرید با همین حساب وارد اپ شوید. هر لایسنس: یک ویندوز + یک اندروید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DfColors.TextMuted,
                        )
                    }
                    Text(
                        license.expiryHeadline,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }
                Icon(DfIcons.Sparkles, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "زمان باقی‌مانده",
                    style = MaterialTheme.typography.bodySmall,
                    color = DfColors.TextMuted,
                )
                Text(
                    "${license.expiryProgressPercent}٪",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(DfShapes.Chip),
                color = accent,
                trackColor = Color.White.copy(alpha = 0.65f),
            )
            license.expiresAt?.takeIf { it.isNotBlank() }?.let {
                val formatted = DateUtils.formatForDisplay(it)
                Text(
                    "انقضا: $formatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = DfColors.TextMuted,
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
            if (onOpenDashboard != null && !license.valid) {
                DfGlassButton(
                    text = "مشاهده کلید در داشبورد",
                    onClick = onOpenDashboard,
                    icon = DfIcons.ExternalLink,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeatureChips(license: LicenseState) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FeatureChip("CRM", license.valid && license.crmEnabled)
        FeatureChip("فایلینگ", license.valid && license.filingEnabled)
        FeatureChip("استخراج فایل", license.canUseLightExtract)
        FeatureChip("Push", license.valid)
    }
}

@Composable
private fun FeatureChip(label: String, enabled: Boolean) {
    DfBadge(
        text = label,
        color = if (enabled) DfColors.Green.copy(alpha = 0.15f) else DfColors.SurfaceVariant,
        textColor = if (enabled) DfColors.Green else DfColors.TextMuted,
    )
}

@Composable
fun SettingsSectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = AppTypography.sectionTitle, fontWeight = FontWeight.SemiBold)
        subtitle?.let {
            Text(it, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
        }
    }
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
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(DfShapes.IconContainer)
                .background(DfColors.PurpleContainer),
            contentAlignment = Alignment.Center,
        ) {
            when {
                iconRes != null -> DfDecorImage(resId = iconRes, size = 22.dp)
                icon != null -> Icon(icon, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DfColors.TextSecondary, maxLines = 2)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
fun DigestHourPicker(
    hour: Int,
    onHourChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("ساعت خلاصه روزانه", fontWeight = FontWeight.Medium)
            DfBadge(text = String.format("%02d:00", hour), color = DfColors.BlueLight, textColor = DfColors.Blue)
        }
        Slider(
            value = hour.toFloat(),
            onValueChange = { onHourChange(it.toInt()) },
            valueRange = 6f..22f,
            steps = 15,
        )
        Text(
            "اعلان «کارهای امروز» و پیگیری معوق حدود این ساعت ارسال می‌شود.",
            style = MaterialTheme.typography.bodySmall,
            color = DfColors.TextMuted,
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
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = DfColors.TextSecondary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DfColors.TextSecondary)
            }
            trailing?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = DfColors.Purple)
            } ?: Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = DfColors.TextMuted)
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
    isSaving: Boolean,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    if (!visible) return
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "ویرایش پروفایل",
            subtitle = "نام و شماره تماس نمایشی خود را به‌روز کنید",
            icon = Icons.Default.Edit,
            onClose = onDismiss,
            footer = {
                DfSheetActions(
                    primaryText = if (isSaving) "در حال ذخیره…" else "ذخیره تغییرات",
                    onPrimary = onSave,
                    primaryEnabled = !isSaving && fullName.isNotBlank(),
                    isSubmitting = isSaving,
                    onSecondary = onDismiss,
                )
            },
        ) {
            DfSheetSection(title = "اطلاعات کاربری") {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("نام کامل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSaving,
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("تلفن") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSaving,
                )
            }
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = DfShapes.Card,
        color = DfColors.RoseLight,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = DfColors.Rose)
            Text(
                "خروج از حساب",
                modifier = Modifier.padding(start = 8.dp),
                color = DfColors.Rose,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
