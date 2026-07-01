package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingSendSheet(
    note: String,
    previewText: String,
    isSubmitting: Boolean,
    onNoteChange: (String) -> Unit,
    onSend: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "ارسال به مخاطب CRM",
        subtitle = "پیام حرفه‌ای بدون لینک دیوار ثبت و برای مخاطب ذخیره می‌شود",
        icon = DfIcons.UserPlus,
        iconContainerColor = DfColors.PurpleContainer,
        iconTint = DfColors.Purple,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ارسال…" else "ارسال به مخاطب",
                onPrimary = onSend,
                primaryEnabled = !isSubmitting,
                isSubmitting = isSubmitting,
                secondaryText = "انصراف",
                onSecondary = onDismiss,
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
                        .heightIn(min = 100.dp, max = 180.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextPrimary,
                )
            }
        }

        DfSheetSection(title = "یادداشت همراه") {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("یادداشت (اختیاری)") },
                placeholder = { Text("مثلاً: بازدید فردا ساعت ۱۶") },
                minLines = 3,
                enabled = !isSubmitting,
            )
            Text(
                text = "می‌توانید همزمان از واتساپ هم ارسال کنید",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            DfGlassTextButton(
                text = "ارسال + واتساپ",
                onClick = onSendWhatsApp,
            )
        }
    }
}
