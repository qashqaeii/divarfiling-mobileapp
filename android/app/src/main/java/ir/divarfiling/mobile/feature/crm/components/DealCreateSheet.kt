package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfJalaliDateField
import ir.divarfiling.mobile.core.design.components.DfJalaliDateTimeField
import ir.divarfiling.mobile.core.design.components.DfMoneyField
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetExpandableSection
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.DealStageDefDto
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.CrmConstants
import androidx.compose.material3.Text

private const val NO_PROPERTY = "بدون ملک"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealCreateSheet(
    contacts: List<ContactDto>,
    properties: List<PropertyDto>,
    stages: List<String>,
    stageDefs: List<DealStageDefDto> = emptyList(),
    selectedContactId: Long?,
    selectedPropertyId: Long?,
    selectedStage: String,
    title: String,
    amount: String,
    nextActionType: String,
    nextActionAt: String,
    nextActionNote: String,
    expectedCloseDate: String,
    notes: String,
    isSubmitting: Boolean,
    onContactSelect: (Long) -> Unit,
    onPropertySelect: (Long?) -> Unit,
    onStageChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNextActionTypeChange: (String) -> Unit,
    onNextActionAtChange: (String) -> Unit,
    onNextActionNoteChange: (String) -> Unit,
    onExpectedCloseDateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onContactSearch: (String) -> Unit = {},
    onPropertySearch: (String) -> Unit = {},
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    customerLocked: Boolean = false,
) {
    val selectedContact = contacts.firstOrNull { it.id == selectedContactId }
    val selectedProperty = properties.firstOrNull { it.id == selectedPropertyId }
    val stageOptions = stages.ifEmpty { CrmConstants.DEAL_STAGES }
    val resolvedStage = selectedStage.ifBlank { stageOptions.first() }
    var nextActionExpanded by remember { mutableStateOf(nextActionType.isNotBlank() || nextActionAt.isNotBlank()) }
    var moreExpanded by remember { mutableStateOf(expectedCloseDate.isNotBlank() || notes.isNotBlank()) }

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
        subtitle = "مخاطب، فایل، مرحله و اقدام بعدی — هم‌راستا با میزکار",
        icon = DfIcons.Handshake,
        onClose = onDismiss,
        bodyHeightFraction = 0.92f,
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
        DfSheetSection(title = "مخاطب *") {
            if (contacts.isEmpty() && selectedContactId == null) {
                Text(
                    text = "ابتدا در بخش مخاطبین، یک مشتری ثبت کنید",
                    style = AppTypography.labelSmall,
                    color = DfColors.Amber,
                )
            } else {
                DealSearchablePicker(
                    label = if (customerLocked) "مخاطب انتخاب‌شده" else "جستجو و انتخاب مخاطب",
                    selectedLabel = selectedContact?.fullName ?: if (selectedContactId != null) "مخاطب انتخاب‌شده" else "",
                    placeholder = "نام یا تلفن را جستجو کنید",
                    enabled = !isSubmitting && !customerLocked,
                    items = contacts.map { it.id to "${it.fullName}${it.phone?.let { phone -> " · $phone" }.orEmpty()}" },
                    onSearch = onContactSearch,
                    onSelect = { id -> id?.let(onContactSelect) },
                )
            }
        }

        DfSheetSection(title = "ملک مرتبط") {
            DealSearchablePicker(
                label = "انتخاب فایل شخصی — اختیاری",
                selectedLabel = selectedProperty?.title ?: NO_PROPERTY,
                placeholder = "عنوان ملک را جستجو کنید",
                enabled = !isSubmitting,
                allowNone = true,
                noneLabel = NO_PROPERTY,
                items = properties.map { property ->
                    val price = property.salePrice ?: property.rent
                    val suffix = price?.let { " · ${ir.divarfiling.mobile.core.design.FormatUtils.formatPriceShort(it)}" }.orEmpty()
                    property.id to "${property.title}$suffix"
                },
                onSearch = onPropertySearch,
                onSelect = onPropertySelect,
            )
        }

        DfSheetSection(title = "جزئیات معامله") {
            DfTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "عنوان معامله *",
                placeholder = "مثلاً: خرید آپارتمان برای آقای رضایی",
                enabled = !isSubmitting,
            )
            DfMoneyField(
                value = amount,
                onValueChange = onAmountChange,
                label = "ارزش تقریبی",
                enabled = !isSubmitting,
                helperText = "تومان — اختیاری؛ در صورت خالی بودن از قیمت ملک پر می‌شود",
            )
            DealStageOptionList(
                stages = stageOptions,
                stageDefs = stageDefs,
                selectedStage = resolvedStage,
                onStageSelect = onStageChange,
                enabled = !isSubmitting,
            )
        }

        DfSheetExpandableSection(
            title = "اقدام بعدی",
            subtitle = if (nextActionType.isBlank()) "اختیاری — تماس، بازدید یا پیگیری" else CrmConstants.dealNextActionLabel(nextActionType),
            expanded = nextActionExpanded,
            onToggle = { nextActionExpanded = !nextActionExpanded },
        ) {
            DealNextActionTypePicker(
                selectedKey = nextActionType,
                enabled = !isSubmitting,
                onSelect = onNextActionTypeChange,
            )
            DfJalaliDateTimeField(
                value = nextActionAt,
                onValueChange = onNextActionAtChange,
                label = "زمان اقدام بعدی",
                enabled = !isSubmitting,
            )
            DfTextField(
                value = nextActionNote,
                onValueChange = onNextActionNoteChange,
                label = "یادداشت کوتاه",
                placeholder = "مثلاً: پیگیری بازدید فردا",
                enabled = !isSubmitting,
            )
        }

        DfSheetExpandableSection(
            title = "اطلاعات بیشتر",
            subtitle = "تاریخ تخمینی بستن و یادداشت",
            expanded = moreExpanded,
            onToggle = { moreExpanded = !moreExpanded },
        ) {
            DfJalaliDateField(
                value = expectedCloseDate,
                onValueChange = onExpectedCloseDateChange,
                label = "تاریخ تخمینی بستن",
                enabled = !isSubmitting,
            )
            DfTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = "یادداشت",
                singleLine = false,
                minLines = 3,
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
fun DealNextActionTypePicker(
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    includeEmpty: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (includeEmpty) {
            DfSheetOptionRow(
                label = "— بدون اقدام —",
                selected = selectedKey.isBlank(),
                onClick = { if (enabled) onSelect("") },
            )
        }
        CrmConstants.DEAL_NEXT_ACTION_TYPES.forEach { (key, label) ->
            DfSheetOptionRow(
                label = label,
                selected = selectedKey == key,
                onClick = { if (enabled) onSelect(key) },
                icon = DealUiUtils.nextActionIcon(key),
            )
        }
    }
}

@Composable
fun DealFollowUpTypePicker(
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CrmConstants.DEAL_FOLLOWUP_TYPES.forEach { (key, label) ->
            DfSheetOptionRow(
                label = label,
                selected = selectedKey == key || (selectedKey.isBlank() && key == "call"),
                onClick = { if (enabled) onSelect(key) },
                icon = DealUiUtils.nextActionIcon(key),
            )
        }
    }
}

@Composable
private fun DealSearchablePicker(
    label: String,
    selectedLabel: String,
    placeholder: String,
    items: List<Pair<Long, String>>,
    onSelect: (Long?) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    allowNone: Boolean = false,
    noneLabel: String = "بدون انتخاب",
) {
    var query by remember { mutableStateOf("") }
    var browsing by remember { mutableStateOf(false) }
    val filtered = remember(query, items) {
        val needle = query.trim()
        if (needle.isBlank()) items.take(12)
        else items.filter { it.second.contains(needle, ignoreCase = true) }.take(20)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DfSheetOptionRow(
            label = selectedLabel.ifBlank { label },
            selected = selectedLabel.isNotBlank() && selectedLabel != noneLabel,
            onClick = { if (enabled) browsing = !browsing },
            trailing = if (browsing) "بستن" else "انتخاب",
        )
        if (browsing && enabled) {
            DfSearchField(
                value = query,
                onValueChange = {
                    query = it
                    onSearch(it)
                },
                placeholder = placeholder,
            )
            if (allowNone) {
                DfSheetOptionRow(
                    label = noneLabel,
                    selected = selectedLabel == noneLabel || selectedLabel.isBlank(),
                    onClick = {
                        onSelect(null)
                        browsing = false
                        query = ""
                    },
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                filtered.forEach { (id, labelText) ->
                    DfSheetOptionRow(
                        label = labelText,
                        selected = selectedLabel.isNotBlank() && labelText.startsWith(selectedLabel),
                        onClick = {
                            onSelect(id)
                            browsing = false
                            query = ""
                        },
                    )
                }
            }
            if (filtered.isEmpty()) {
                Text(
                    text = "موردی یافت نشد",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }
    }
}
