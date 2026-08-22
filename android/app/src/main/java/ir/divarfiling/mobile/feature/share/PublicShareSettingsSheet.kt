package ir.divarfiling.mobile.feature.share

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.ShareToggleRow

private val LOCATION_RADIUS_OPTIONS = listOf("300", "500", "750", "1000", "1500")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicShareSettingsSheet(
    consultantName: String,
    consultantPhone: String,
    welcomeMessage: String,
    defaultShareMessage: String = "",
    isActive: Boolean,
    showDivarLink: Boolean,
    showFullAddress: Boolean,
    showInternalNotes: Boolean,
    approximateLocation: Boolean = false,
    approximateLocationRadiusM: String = "500",
    showNearbyPois: Boolean = false,
    isSubmitting: Boolean,
    onConsultantNameChange: (String) -> Unit,
    onConsultantPhoneChange: (String) -> Unit,
    onWelcomeMessageChange: (String) -> Unit,
    onDefaultShareMessageChange: (String) -> Unit = {},
    onIsActiveChange: (Boolean) -> Unit,
    onShowDivarLinkChange: (Boolean) -> Unit,
    onShowFullAddressChange: (Boolean) -> Unit,
    onShowInternalNotesChange: (Boolean) -> Unit,
    onApproximateLocationChange: (Boolean) -> Unit = {},
    onApproximateLocationRadiusChange: (String) -> Unit = {},
    onShowNearbyPoisChange: (Boolean) -> Unit = {},
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val radiusLabel = approximateLocationRadiusM.ifBlank { "500" }

    DfSheetScaffold(
        title = "تنظیمات صفحه عمومی",
        subtitle = "اطلاعات مشاور، حریم نمایش و پیام پیش‌فرض برای مشتری",
        icon = DfIcons.Settings,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره تنظیمات",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "اطلاعات مشاور") {
            OutlinedTextField(
                value = consultantName,
                onValueChange = onConsultantNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نام مشاور") },
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = consultantPhone,
                onValueChange = onConsultantPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("شماره تماس مشاور") },
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = welcomeMessage,
                onValueChange = onWelcomeMessageChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("پیام خوش‌آمد") },
                placeholder = { Text("مثلاً برای بازدید با من هماهنگ کنید") },
                minLines = 3,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = defaultShareMessage,
                onValueChange = onDefaultShareMessageChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("پیام پیش‌فرض اشتراک") },
                placeholder = { Text("متن آماده برای ارسال سریع به مشتری") },
                minLines = 3,
                enabled = !isSubmitting,
            )
        }
        DfSheetSection(title = "تنظیمات نمایش") {
            ShareToggleRow(
                title = "صفحه عمومی فعال باشد",
                subtitle = "در صورت خاموش‌بودن، لینک مشتری غیرفعال می‌شود",
                checked = isActive,
                onCheckedChange = onIsActiveChange,
            )
            ShareToggleRow(
                title = "نمایش لینک دیوار",
                subtitle = "لینک آگهی اصلی هم در صفحه مشتری نمایش داده شود",
                checked = showDivarLink,
                onCheckedChange = onShowDivarLinkChange,
            )
            ShareToggleRow(
                title = "نمایش آدرس کامل",
                subtitle = "آدرس دقیق ملک به مشتری نشان داده شود",
                checked = showFullAddress,
                onCheckedChange = onShowFullAddressChange,
            )
            ShareToggleRow(
                title = "نمایش یادداشت داخلی",
                subtitle = "یادداشت داخلی پرونده هم برای مشتری قابل مشاهده باشد",
                checked = showInternalNotes,
                onCheckedChange = onShowInternalNotesChange,
            )
            ShareToggleRow(
                title = "موقعیت تقریبی",
                subtitle = "به‌جای آدرس دقیق، محدوده تقریبی روی نقشه نمایش داده شود",
                checked = approximateLocation,
                onCheckedChange = onApproximateLocationChange,
            )
            if (approximateLocation) {
                DfDropdown(
                    label = "شعاع محدوده (متر)",
                    value = "$radiusLabel متر",
                    options = LOCATION_RADIUS_OPTIONS.map { "$it متر" },
                    enabled = !isSubmitting,
                    onSelect = { selected ->
                        LOCATION_RADIUS_OPTIONS
                            .firstOrNull { selected.startsWith(it) }
                            ?.let(onApproximateLocationRadiusChange)
                    },
                )
            }
            ShareToggleRow(
                title = "نمایش اماکن نزدیک",
                subtitle = "مترو، مدرسه و مراکز مهم اطراف ملک در صفحه مشتری",
                checked = showNearbyPois,
                onCheckedChange = onShowNearbyPoisChange,
            )
        }
    }
}
