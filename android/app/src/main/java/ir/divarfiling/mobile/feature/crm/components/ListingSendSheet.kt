package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    isSubmitting: Boolean,
    onNoteChange: (String) -> Unit,
    onSend: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "ارسال به CRM",
        subtitle = "پیام حرفه‌ای بدون لینک دیوار برای مخاطب ارسال می‌شود",
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
        DfSheetSection(title = "یادداشت همراه") {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("یادداشت (اختیاری)") },
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
