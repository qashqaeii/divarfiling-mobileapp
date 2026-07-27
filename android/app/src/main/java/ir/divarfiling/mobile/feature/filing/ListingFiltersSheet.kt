package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfTextButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListingFiltersSheet(
    visible: Boolean,
    state: ListingFilterState,
    neighborhoods: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onApply: (ListingFilterState) -> Unit,
    onClear: () -> Unit,
    onSaveFilter: ((ListingFilterState) -> Unit)? = null,
) {
    if (!visible) return

    var draft by remember(visible, state) { mutableStateOf(state) }
    var priceMinText by remember(visible, state.priceMin) {
        mutableStateOf(state.priceMin?.toString().orEmpty())
    }
    var priceMaxText by remember(visible, state.priceMax) {
        mutableStateOf(state.priceMax?.toString().orEmpty())
    }
    var areaMinText by remember(visible, state.areaMin) {
        mutableStateOf(state.areaMin?.toString().orEmpty())
    }
    var areaMaxText by remember(visible, state.areaMax) {
        mutableStateOf(state.areaMax?.toString().orEmpty())
    }
    var yearMinText by remember(visible, state.yearMin) {
        mutableStateOf(state.yearMin?.toString().orEmpty())
    }
    var yearMaxText by remember(visible, state.yearMax) {
        mutableStateOf(state.yearMax?.toString().orEmpty())
    }

    fun syncNumericDraft(): ListingFilterState = draft.copy(
        priceMin = priceMinText.toLongOrNull(),
        priceMax = priceMaxText.toLongOrNull(),
        areaMin = areaMinText.toIntOrNull(),
        areaMax = areaMaxText.toIntOrNull(),
        yearMin = yearMinText.toIntOrNull(),
        yearMax = yearMaxText.toIntOrNull(),
    )

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "فیلتر حرفه‌ای",
            subtitle = "محله، امکانات، آگهی‌دهنده، ارزش و مرتب‌سازی",
            icon = DfIcons.Filter,
            onClose = onDismiss,
            footer = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    if (onSaveFilter != null) {
                        DfTextButton(
                            text = "ذخیره فیلتر فعلی",
                            onClick = { onSaveFilter(syncNumericDraft()) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        DfSecondaryButton(
                            text = "پاک کردن",
                            onClick = {
                                draft = ListingFilterState()
                                priceMinText = ""
                                priceMaxText = ""
                                areaMinText = ""
                                areaMaxText = ""
                                yearMinText = ""
                                yearMaxText = ""
                                onClear()
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DfPrimaryButton(
                            text = "اعمال فیلتر",
                            onClick = {
                                onApply(syncNumericDraft())
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            },
        ) {
            DfSheetSection(title = "مرتب‌سازی") {
                DfFilterChipRow(
                    options = ListingSortOptions.map { DfFilterOption(it.first, it.second) },
                    selected = draft.sort,
                    onSelect = { draft = draft.copy(sort = it) },
                )
            }
            DfSheetSection(title = "سریع") {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    DfSoftChip(
                        text = "بدون تکرار",
                        selected = draft.unique,
                        onClick = { draft = draft.copy(unique = !draft.unique) },
                    )
                    DfSoftChip(
                        text = "فقط جدید",
                        selected = draft.newOnly,
                        onClick = { draft = draft.copy(newOnly = !draft.newOnly) },
                    )
                }
            }
            DfSheetSection(title = "محله") {
                OutlinedTextField(
                    value = draft.neighborhood,
                    onValueChange = { draft = draft.copy(neighborhood = it) },
                    label = { Text("نام محله") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = AppShapes.Field,
                )
                if (neighborhoods.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        neighborhoods.take(24).forEach { name ->
                            DfSoftChip(
                                text = name,
                                selected = draft.neighborhood == name,
                                onClick = {
                                    draft = draft.copy(
                                        neighborhood = if (draft.neighborhood == name) "" else name,
                                    )
                                },
                            )
                        }
                    }
                }
            }
            DfSheetSection(title = "محدوده قیمت") {
                OutlinedTextField(
                    value = priceMinText,
                    onValueChange = { priceMinText = it.filter(Char::isDigit) },
                    label = { Text("حداقل قیمت (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = AppShapes.Field,
                )
                OutlinedTextField(
                    value = priceMaxText,
                    onValueChange = { priceMaxText = it.filter(Char::isDigit) },
                    label = { Text("حداکثر قیمت (تومان)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = AppShapes.Field,
                )
            }
            DfSheetSection(title = "مشخصات ملک") {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedTextField(
                        value = areaMinText,
                        onValueChange = { areaMinText = it.filter(Char::isDigit) },
                        label = { Text("متراژ از") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = AppShapes.Field,
                    )
                    OutlinedTextField(
                        value = areaMaxText,
                        onValueChange = { areaMaxText = it.filter(Char::isDigit) },
                        label = { Text("متراژ تا") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = AppShapes.Field,
                    )
                }
                OutlinedTextField(
                    value = draft.rooms,
                    onValueChange = { draft = draft.copy(rooms = it.filter(Char::isDigit)) },
                    label = { Text("تعداد اتاق") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = AppShapes.Field,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedTextField(
                        value = yearMinText,
                        onValueChange = { yearMinText = it.filter(Char::isDigit) },
                        label = { Text("سال از") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = AppShapes.Field,
                    )
                    OutlinedTextField(
                        value = yearMaxText,
                        onValueChange = { yearMaxText = it.filter(Char::isDigit) },
                        label = { Text("سال تا") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = AppShapes.Field,
                    )
                }
            }
            DfSheetSection(title = "امکانات") {
                Text("پارکینگ", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                DfFilterChipRow(
                    options = ListingTriStateOptions.map { DfFilterOption(it.first, it.second) },
                    selected = draft.parking,
                    onSelect = { draft = draft.copy(parking = it) },
                )
                Text("آسانسور", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                DfFilterChipRow(
                    options = ListingTriStateOptions.map { DfFilterOption(it.first, it.second) },
                    selected = draft.elevator,
                    onSelect = { draft = draft.copy(elevator = it) },
                )
                Text("انباری", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                DfFilterChipRow(
                    options = ListingTriStateOptions.map { DfFilterOption(it.first, it.second) },
                    selected = draft.storage,
                    onSelect = { draft = draft.copy(storage = it) },
                )
            }
            DfSheetSection(title = "نوع آگهی‌دهنده") {
                DfFilterChipRow(
                    options = ListingAdvertiserOptions.map { DfFilterOption(it.first, it.second) },
                    selected = draft.consultant,
                    onSelect = { draft = draft.copy(consultant = it) },
                )
            }
            DfSheetSection(title = "ارزش پیشنهادی") {
                DfFilterChipRow(
                    options = ListingValueOptions.map { DfFilterOption(it.first, it.second) },
                    selected = draft.value,
                    onSelect = { draft = draft.copy(value = it) },
                )
            }
        }
    }
}

fun activeListingFilterCount(state: ListingFilterState): Int = state.activeCount()

/** سازگاری با فراخوانی‌های قدیمی. */
fun activeListingFilterCount(
    priceMin: Long?,
    priceMax: Long?,
    areaMin: Int?,
    areaMax: Int?,
    rooms: Int?,
): Int = listOf(priceMin, priceMax, areaMin, areaMax, rooms).count { it != null }
