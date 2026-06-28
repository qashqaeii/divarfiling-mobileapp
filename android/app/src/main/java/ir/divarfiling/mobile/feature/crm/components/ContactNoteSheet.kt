package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactNoteSheet(
    note: String,
    isSubmitting: Boolean,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "یادداشت جدید",
        subtitle = "یادداشت را به پرونده مخاطب اضافه کنید",
        icon = DfIcons.File,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت یادداشت",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && note.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "متن یادداشت") {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                placeholder = { Text("متن یادداشت…") },
                enabled = !isSubmitting,
            )
        }
    }
}
