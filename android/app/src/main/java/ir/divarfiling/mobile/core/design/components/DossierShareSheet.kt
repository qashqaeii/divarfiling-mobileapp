package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DossierShareKind
import ir.divarfiling.mobile.core.design.DossierShareOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DossierShareSheet(
    previewText: String,
    kind: DossierShareKind,
    note: String,
    includeDivarLink: Boolean,
    publicShareUrl: String? = null,
    publicShareViewCount: Int = 0,
    includePublicPageLink: Boolean = true,
    onIncludePublicPageLinkChange: ((Boolean) -> Unit)? = null,
    includeAddress: Boolean,
    includeInternalNotes: Boolean,
    includeAmenities: Boolean,
    onNoteChange: (String) -> Unit,
    onIncludeDivarLinkChange: (Boolean) -> Unit,
    onIncludeAddressChange: (Boolean) -> Unit,
    onIncludeInternalNotesChange: (Boolean) -> Unit,
    onIncludeAmenitiesChange: (Boolean) -> Unit,
    onShare: () -> Unit,
    onWhatsApp: () -> Unit,
    onBale: (() -> Unit)? = null,
    onTelegram: (() -> Unit)? = null,
    onSms: (() -> Unit)? = null,
    onCopy: () -> Unit,
    onCopyPublicLink: (() -> Unit)? = null,
    onOpenPublicPreview: (() -> Unit)? = null,
    onManagePublicShare: (() -> Unit)? = null,
    onSendToContact: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val subtitle = when (kind) {
        DossierShareKind.FILING -> "پیام حرفه‌ای برای مشتری — کنترل کامل محتوا و لینک عمومی"
        DossierShareKind.PERSONAL -> "ارسال فایل شخصی به مشتری با واتساپ، کپی یا لینک صفحه عمومی"
    }

    DfSheetScaffold(
        title = "اشتراک با مشتری",
        subtitle = subtitle,
        icon = DfIcons.Share2,
        iconContainerColor = DfColors.BlueLight,
        iconTint = DfColors.Blue,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = "ارسال واتساپ",
                onPrimary = onWhatsApp,
                secondaryText = "اشتراک سیستم",
                onSecondary = onShare,
            )
        },
    ) {
        DfSheetSection(title = "پیش‌نمایش پیام") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
            ) {
                Text(
                    text = previewText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 220.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextPrimary,
                )
            }
        }

        DfSheetSection(title = "تنظیمات پیام") {
            if (!publicShareUrl.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Card,
                    color = DfColors.BlueLight.copy(alpha = 0.35f),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "صفحه اختصاصی مشتری",
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "مشتری بدون نیاز به اپ، جزئیات و عکس‌ها را در مرورگر می‌بیند.",
                            style = AppTypography.labelSmall,
                            color = DfColors.TextSecondary,
                        )
                        Text(
                            publicShareUrl,
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                        )
                        if (publicShareViewCount > 0) {
                            Text(
                                "$publicShareViewCount بازدید",
                                style = AppTypography.labelSmall,
                                color = DfColors.TextMuted,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            onCopyPublicLink?.let { copyLink ->
                                DfGlassButton(
                                    text = "کپی لینک",
                                    onClick = copyLink,
                                    icon = DfIcons.Copy,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            onOpenPublicPreview?.let { openPreview ->
                                DfGlassButton(
                                    text = "پیش‌نمایش",
                                    onClick = openPreview,
                                    icon = DfIcons.ExternalLink,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        onManagePublicShare?.let { managePublicShare ->
                            DfGlassButton(
                                text = "تنظیمات صفحه عمومی",
                                onClick = managePublicShare,
                                icon = DfIcons.Settings,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                onIncludePublicPageLinkChange?.let { onChange ->
                    ShareToggleRow(
                        title = "لینک صفحه عمومی در پیام",
                        subtitle = "صفحه حرفه‌ای با اطلاعات شما برای مشتری",
                        checked = includePublicPageLink,
                        onCheckedChange = onChange,
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("یادداشت شخصی (اختیاری)") },
                placeholder = { Text("مثلاً: مناسب خانواده، طبقه میانی، بازدید با هماهنگی") },
                minLines = 2,
            )

            ShareToggleRow(
                title = "امکانات ملک",
                subtitle = "پارکینگ، انباری و ویژگی‌های کلیدی",
                checked = includeAmenities,
                onCheckedChange = onIncludeAmenitiesChange,
            )

            if (kind == DossierShareKind.FILING) {
                ShareToggleRow(
                    title = "لینک دیوار",
                    subtitle = "افزودن لینک آگهی به انتهای پیام",
                    checked = includeDivarLink,
                    onCheckedChange = onIncludeDivarLinkChange,
                )
            }

            if (kind == DossierShareKind.PERSONAL) {
                ShareToggleRow(
                    title = "آدرس دقیق",
                    subtitle = "نمایش آدرس کامل در پیام",
                    checked = includeAddress,
                    onCheckedChange = onIncludeAddressChange,
                )
                ShareToggleRow(
                    title = "یادداشت داخلی",
                    subtitle = "یادداشت‌های محرمانه پرونده",
                    checked = includeInternalNotes,
                    onCheckedChange = onIncludeInternalNotesChange,
                )
                ShareToggleRow(
                    title = "لینک آگهی",
                    subtitle = "اگر لینک دیوار ثبت شده باشد",
                    checked = includeDivarLink,
                    onCheckedChange = onIncludeDivarLinkChange,
                )
            }
        }

        DfSheetSection(title = "ارسال سریع به مشتری") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DfGlassButton(
                    text = "واتساپ به مشتری",
                    onClick = onWhatsApp,
                    icon = DfIcons.MessageCircle,
                    modifier = Modifier.fillMaxWidth(),
                )
                onBale?.let { bale ->
                    DfGlassButton(
                        text = "بله",
                        onClick = bale,
                        icon = DfIcons.MessageCircle,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                DfGlassButton(
                    text = "کپی متن پیام",
                    onClick = onCopy,
                    icon = DfIcons.Copy,
                    modifier = Modifier.fillMaxWidth(),
                )
                onTelegram?.let { telegram ->
                    DfGlassButton(
                        text = "تلگرام",
                        onClick = telegram,
                        icon = DfIcons.Share2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                onSms?.let { sms ->
                    DfGlassButton(
                        text = "پیامک",
                        onClick = sms,
                        icon = DfIcons.MessageCircle,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                onSendToContact?.let { sendToContact ->
                    DfGlassButton(
                        text = "ارسال به مخاطب CRM",
                        onClick = sendToContact,
                        icon = DfIcons.UserPlus,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun ShareToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DfColors.Surface, AppShapes.Field)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = AppTypography.labelSmall, color = DfColors.TextMuted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
