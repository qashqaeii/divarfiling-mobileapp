package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.CrmConstants

private const val NO_PROPERTY = "بدون ملک"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealCreateSheet(
    contacts: List<ContactDto>,
    properties: List<PropertyDto>,
    stages: List<String>,
    stageDefs: List<ir.divarfiling.mobile.core.network.DealStageDefDto> = emptyList(),
    selectedContactId: Long?,
    selectedPropertyId: Long?,
    selectedStage: String,
    title: String,
    amount: String,
    nextActionType: String,
    nextActionAt: String,
    nextActionNote: String,
    isSubmitting: Boolean,
    onContactSelect: (Long) -> Unit,
    onPropertySelect: (Long?) -> Unit,
    onStageChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNextActionTypeChange: (String) -> Unit,
    onNextActionAtChange: (String) -> Unit,
    onNextActionNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val contactNames = contacts.map { it.fullName }
    val selectedName = contacts.firstOrNull { it.id == selectedContactId }?.fullName.orEmpty()
    val propertyOptions = listOf(NO_PROPERTY) + properties.map { it.title }
    val selectedPropertyLabel = properties.firstOrNull { it.id == selectedPropertyId }?.title ?: NO_PROPERTY
    val stageOptions = stages.ifEmpty { CrmConstants.DEAL_STAGES }
    val resolvedStage = selectedStage.ifBlank { stageOptions.first() }
    val selectedContact = contacts.firstOrNull { it.id == selectedContactId }
    val selectedProperty = properties.firstOrNull { it.id == selectedPropertyId }

    LaunchedEffect(selectedContactId, selectedPropertyId) {
        if (title.isBlank() && selectedContact != null) {
            val base = selectedProperty?.title?.let { "$it — ${selectedContact.fullName}" }
                ?: "معامله — ${selectedContact.fullName}"
            onTitleChange(base)
        }
        if (amount.isBlank() && selectedProperty != null) {
            val suggested = selectedProperty.salePrice ?: selectedProperty.rent
            if (suggested != null && suggested > 0) onAmountChange(suggested.toString())
        }
    }

    DfSheetScaffold(
        title = "معامله جدید",
        subtitle = "مخاطب، فایل و اقدام بعدی — بدون جزئیات مالی",
        icon = DfIcons.Handshake,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت معامله",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting && selectedContactId != null && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "مخاطب") {
            if (contacts.isEmpty()) {
                Text(
                    text = "ابتدا در بخش مخاطبین، یک مشتری ثبت کنید",
                    style = AppTypography.labelSmall,
                    color = DfColors.Amber,
                )
            } else {
                DfDropdown(
                    label = "انتخاب مخاطب",
                    value = selectedName.ifBlank { "مخاطب را انتخاب کنید" },
                    options = contactNames,
                    enabled = !isSubmitting,
                    onSelect = { name ->
                        contacts.firstOrNull { it.fullName == name }?.id?.let(onContactSelect)
                    },
                )
            }
        }

        DfSheetSection(title = "ملک مرتبط") {
            DfDropdown(
                label = "انتخاب فایل شخصی",
                value = selectedPropertyLabel,
                options = propertyOptions,
                enabled = !isSubmitting,
                onSelect = { label ->
                    if (label == NO_PROPERTY) onPropertySelect(null)
                    else properties.firstOrNull { it.title == label }?.id?.let(onPropertySelect)
                },
            )
        }

        DfSheetSection(title = "جزئیات") {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان معامله") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً: خرید آپارتمان برای آقای رضایی") },
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("ارزش تقریبی (تومان)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("اختیاری") },
                enabled = !isSubmitting,
            )
            DealStageOptionList(
                stages = stageOptions,
                stageDefs = stageDefs,
                selectedStage = resolvedStage,
                onStageSelect = onStageChange,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "اقدام بعدی") {
            DfDropdown(
                label = "نوع",
                value = NEXT_ACTION_TYPES.firstOrNull { it.first == nextActionType }?.second ?: "— بدون اقدام —",
                options = listOf("— بدون اقدام —") + NEXT_ACTION_TYPES.map { it.second },
                enabled = !isSubmitting,
                onSelect = { label ->
                    if (label == "— بدون اقدام —") onNextActionTypeChange("")
                    else NEXT_ACTION_TYPES.firstOrNull { it.second == label }?.first?.let(onNextActionTypeChange)
                },
            )
            OutlinedTextField(
                value = nextActionAt,
                onValueChange = onNextActionAtChange,
                label = { Text("تاریخ و ساعت") },
                placeholder = { Text("۱۴۰۴/۰۶/۱۵ ۱۸:۰۰") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = nextActionNote,
                onValueChange = onNextActionNoteChange,
                label = { Text("یادداشت") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }
    }
}

private val NEXT_ACTION_TYPES = listOf(
    "call" to "تماس",
    "message" to "پیام",
    "send_file" to "ارسال فایل",
    "visit" to "بازدید",
    "negotiation" to "مذاکره",
    "contract_followup" to "پیگیری قرارداد",
    "documents" to "دریافت مدارک",
    "commission" to "پیگیری کمیسیون",
    "other" to "سایر",
)
