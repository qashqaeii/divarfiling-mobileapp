package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.ContactDto
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayNewTaskSheet(
    contacts: List<ContactDto>,
    selectedContactId: Long?,
    title: String,
    dueMillis: Long,
    isSubmitting: Boolean,
    onContactSelect: (Long) -> Unit,
    onTitleChange: (String) -> Unit,
    onDueChange: (Long) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val contactNames = contacts.map { it.fullName }
    val selectedName = contacts.firstOrNull { it.id == selectedContactId }?.fullName.orEmpty()

    DfSheetScaffold(
        title = "کار جدید",
        subtitle = "یادآور یا پیگیری جدید برای مخاطب ثبت کنید",
        icon = DfIcons.ListTodo,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت کار",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && selectedContactId != null && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "جزئیات کار") {
            if (contacts.isEmpty()) {
                Text(
                    text = "ابتدا یک مخاطب در CRM ثبت کنید",
                    style = AppTypography.labelSmall,
                    color = DfColors.Amber,
                )
            } else {
                DfDropdown(
                    label = "مخاطب",
                    value = selectedName.ifBlank { "انتخاب مخاطب" },
                    options = contactNames,
                    enabled = !isSubmitting,
                    onSelect = { name ->
                        contacts.firstOrNull { it.fullName == name }?.id?.let(onContactSelect)
                    },
                )
            }
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان کار") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً پیگیری تماس") },
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = formatDueLabel(dueMillis),
                onValueChange = {},
                label = { Text("زمان انجام (شمسی)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                DfGlassTextButton(text = "۱ ساعت دیگر", onClick = {
                    onDueChange(System.currentTimeMillis() + 3_600_000L)
                }, compact = true)
                DfGlassTextButton(text = "فردا ۱۰:۰۰", onClick = {
                    val tomorrow = java.time.LocalDate.now().plusDays(1)
                        .atTime(10, 0)
                        .atZone(ZoneId.systemDefault())
                    onDueChange(tomorrow.toInstant().toEpochMilli())
                }, compact = true)
                DfGlassTextButton(text = "۳ روز دیگر", onClick = {
                    onDueChange(System.currentTimeMillis() + 3 * 86_400_000L)
                }, compact = true)
            }
        }
    }
}

private fun formatDueLabel(millis: Long): String =
    DateUtils.formatJalaliDateTimeFromMillis(millis)
