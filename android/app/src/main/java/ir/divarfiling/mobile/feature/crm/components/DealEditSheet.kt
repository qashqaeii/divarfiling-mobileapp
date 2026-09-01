package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ir.divarfiling.mobile.core.design.components.DfMoneyField
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetExpandableSection
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.DealStageDefDto
import ir.divarfiling.mobile.core.network.PropertyDto

private const val NO_PROPERTY = "بدون ملک"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealEditSheet(
    title: String,
    amount: String,
    commissionRate: String,
    notes: String,
    stages: List<String>,
    selectedStage: String,
    properties: List<PropertyDto>,
    selectedPropertyId: Long?,
    expectedCloseDate: String,
    listingToken: String,
    probability: String,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCommissionRateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onStageChange: (String) -> Unit,
    onPropertySelect: (Long?) -> Unit,
    onExpectedCloseDateChange: (String) -> Unit,
    onListingTokenChange: (String) -> Unit,
    onProbabilityChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    stageDefs: List<DealStageDefDto> = emptyList(),
    onPropertySearch: (String) -> Unit = {},
) {
    var moreExpanded by remember {
        mutableStateOf(expectedCloseDate.isNotBlank() || listingToken.isNotBlank() || probability.isNotBlank())
    }
    val selectedPropertyLabel = properties.firstOrNull { it.id == selectedPropertyId }?.title ?: NO_PROPERTY

    DfSheetScaffold(
        title = "ویرایش معامله",
        subtitle = "عنوان، مبلغ، ملک، مرحله و جزئیات پرونده را به‌روز کنید",
        icon = DfIcons.Handshake,
        iconContainerColor = DfColors.PurpleContainer,
        iconTint = DfColors.Purple,
        onClose = onDismiss,
        bodyHeightFraction = 0.92f,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره تغییرات",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting && title.isNotBlank(),
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "جزئیات") {
            DfTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "عنوان معامله",
                enabled = !isSubmitting,
            )
            DfMoneyField(
                value = amount,
                onValueChange = onAmountChange,
                label = "ارزش معامله",
                enabled = !isSubmitting,
            )
            DfTextField(
                value = commissionRate,
                onValueChange = onCommissionRateChange,
                label = "نرخ کمیسیون (٪)",
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "ملک مرتبط") {
            if (properties.isEmpty()) {
                Text(
                    text = "فایل شخصی برای اتصال موجود نیست",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            } else {
                DealSearchablePropertyField(
                    selectedLabel = selectedPropertyLabel,
                    properties = properties,
                    enabled = !isSubmitting,
                    onSearch = onPropertySearch,
                    onSelect = onPropertySelect,
                )
            }
        }

        DfSheetSection(title = "مرحله فروش") {
            DealStageOptionList(
                stages = stages,
                stageDefs = stageDefs,
                selectedStage = selectedStage,
                onStageSelect = onStageChange,
                enabled = !isSubmitting,
            )
        }

        DfSheetSection(title = "یادداشت") {
            DfTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = "یادداشت",
                singleLine = false,
                minLines = 3,
                enabled = !isSubmitting,
            )
        }

        DfSheetExpandableSection(
            title = "اطلاعات بیشتر",
            subtitle = "تاریخ بستن، شناسه آگهی و احتمال",
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
                value = listingToken,
                onValueChange = onListingTokenChange,
                label = "شناسه آگهی",
                enabled = !isSubmitting,
            )
            DfTextField(
                value = probability,
                onValueChange = onProbabilityChange,
                label = "احتمال بستن (٪)",
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
private fun DealSearchablePropertyField(
    selectedLabel: String,
    properties: List<PropertyDto>,
    enabled: Boolean,
    onSearch: (String) -> Unit,
    onSelect: (Long?) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var browsing by remember { mutableStateOf(false) }
    val items = remember(query, properties) {
        val needle = query.trim()
        val mapped = properties.map { it.id to it.title }
        if (needle.isBlank()) mapped.take(12) else mapped.filter { it.second.contains(needle, ignoreCase = true) }.take(20)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DfSheetOptionRow(
            label = selectedLabel,
            selected = selectedLabel != NO_PROPERTY,
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
                placeholder = "جستجوی فایل شخصی",
            )
            DfSheetOptionRow(
                label = NO_PROPERTY,
                selected = selectedLabel == NO_PROPERTY,
                onClick = {
                    onSelect(null)
                    browsing = false
                },
            )
            items.forEach { (id, label) ->
                DfSheetOptionRow(
                    label = label,
                    selected = label == selectedLabel,
                    onClick = {
                        onSelect(id)
                        browsing = false
                    },
                )
            }
        }
    }
}
