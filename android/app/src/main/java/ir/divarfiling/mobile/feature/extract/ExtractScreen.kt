package ir.divarfiling.mobile.feature.extract

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCountSlider
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSectionTitle
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.feature.extract.components.ExtractDailyUsageCard
import ir.divarfiling.mobile.feature.extract.components.ExtractLoadingExperience
import ir.divarfiling.mobile.feature.extract.components.PlaceSearchField
import ir.divarfiling.mobile.feature.extract.components.ScheduleIntervalBottomSheet
import ir.divarfiling.mobile.feature.extract.components.ScheduleIntervalField
import ir.divarfiling.mobile.feature.extract.components.extractPhaseFromProgress
import ir.divarfiling.mobile.feature.extract.components.placeSelectionSummary
import ir.divarfiling.mobile.feature.extract.components.rememberDebouncedQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractScreen(
    onViewDataset: (String) -> Unit,
    onOpenSchedules: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ExtractViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val canExtract = state.license.canUseLightExtract && state.gateMessage == null
    val scroll = rememberScrollState()
    val tx = ExtractCategories.transactionTypes.firstOrNull { it.label == state.transactionType }
    val subcategories = tx?.subcategories.orEmpty()
    var showSchedulePicker by remember { mutableStateOf(false) }

    rememberDebouncedQuery(state.placeQuery) { viewModel.onPlaceSearchDebounced(it) }

    ScheduleIntervalBottomSheet(
        visible = showSchedulePicker,
        selectedHours = state.scheduleIntervalHours,
        onSelect = viewModel::onScheduleIntervalSelect,
        onDismiss = { showSchedulePicker = false },
    )

    Scaffold(
        topBar = {
            DfTopBar(title = "استخراج فایل", showLogo = true, onBack = onBack)
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            if (state.remainingToday != null && state.extractionsDailyLimit != null) {
                ExtractDailyUsageCard(
                    extractionsToday = state.extractionsToday ?: 0,
                    remainingToday = state.remainingToday ?: 0,
                    dailyLimit = state.extractionsDailyLimit ?: 0,
                    canExtractNow = state.canExtractNow,
                )
            }

            DfPremiumCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DfSectionTitle(title = "زمان‌بندی خودکار")
                    ScheduleIntervalField(
                        selectedHours = state.scheduleIntervalHours,
                        enabled = canExtract && !state.isRunning,
                        onClick = { showSchedulePicker = true },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onOpenSchedules) { Text("مدیریت زمان‌بندی‌ها") }
                        TextButton(
                            onClick = viewModel::createSchedule,
                            enabled = canExtract && !state.isRunning,
                        ) {
                            Text("ذخیره زمان‌بندی")
                        }
                    }
                }
            }

            DfPremiumCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DfSectionTitle(title = "مکان")
                    PlaceSearchField(
                        query = state.placeQuery,
                        suggestions = state.placeSuggestions,
                        selectedSummary = placeSelectionSummary(
                            state.provinceName,
                            state.cityName,
                            state.districts.firstOrNull { it.id == state.districtId }?.name,
                        ).takeIf { state.provinceName.isNotBlank() },
                        enabled = canExtract && !state.isRunning,
                        onQueryChange = viewModel::onPlaceQueryChange,
                        onSuggestionSelect = viewModel::onPlaceSuggestionSelect,
                    )
                    DfDropdown(
                        label = "استان",
                        value = state.provinceName,
                        options = state.provinces,
                        enabled = canExtract && !state.isRunning,
                        onSelect = viewModel::onProvinceChange,
                    )
                    DfDropdown(
                        label = "شهر",
                        value = state.cityName,
                        options = state.cities.map { it.name },
                        enabled = canExtract && !state.isRunning,
                        onSelect = { name ->
                            state.cities.firstOrNull { it.name == name }?.let(viewModel::onCityChange)
                        },
                    )
                    if (state.districts.isNotEmpty()) {
                        DfDropdown(
                            label = "منطقه (اختیاری)",
                            value = state.districts.firstOrNull { it.id == state.districtId }?.name ?: "همه مناطق",
                            options = listOf("همه مناطق") + state.districts.map { it.name },
                            enabled = canExtract && !state.isRunning,
                            onSelect = { name ->
                                if (name == "همه مناطق") {
                                    viewModel.onDistrictChange("")
                                } else {
                                    state.districts.firstOrNull { it.name == name }?.id?.let(viewModel::onDistrictChange)
                                }
                            },
                        )
                    }
                }
            }

            DfPremiumCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DfSectionTitle(title = "فیلترهای استخراج")
                    DfDropdown(
                        label = "نوع معامله",
                        value = state.transactionType,
                        options = ExtractCategories.transactionTypes.map { it.label },
                        enabled = canExtract && !state.isRunning,
                        onSelect = viewModel::onTransactionTypeChange,
                    )
                    DfDropdown(
                        label = "زیردسته",
                        value = state.subcategoryLabel,
                        options = subcategories.map { it.label },
                        enabled = canExtract && !state.isRunning,
                        onSelect = viewModel::onSubcategoryChange,
                    )
                    DfDropdown(
                        label = "مرتب‌سازی",
                        value = ExtractCategories.sortOptions.firstOrNull { it.first == state.sort }?.second ?: "",
                        options = ExtractCategories.sortOptions.map { it.second },
                        enabled = canExtract && !state.isRunning,
                        onSelect = { label ->
                            ExtractCategories.sortOptions.firstOrNull { it.second == label }?.first?.let(viewModel::onSortChange)
                        },
                    )
                    DfDropdown(
                        label = "نوع آگهی‌دهنده",
                        value = ExtractCategories.advertiserOptions.firstOrNull { it.first == state.advertiserFilter }?.second ?: "",
                        options = ExtractCategories.advertiserOptions.map { it.second },
                        enabled = canExtract && !state.isRunning,
                        onSelect = { label ->
                            ExtractCategories.advertiserOptions.firstOrNull { it.second == label }?.first
                                ?.let(viewModel::onAdvertiserFilterChange)
                        },
                    )
                    DfCountSlider(
                        value = state.maxItems,
                        onValueChange = viewModel::onMaxItemsChange,
                        enabled = canExtract && !state.isRunning,
                        label = "تعداد آگهی (۰ تا ۱۰۰)",
                    )
                    TextButton(onClick = viewModel::toggleAdvanced, enabled = canExtract && !state.isRunning) {
                        Text(if (state.showAdvanced) "بستن فیلترهای پیشرفته" else "فیلترهای پیشرفته")
                    }
                    if (state.showAdvanced) {
                        AdvancedFilters(state, canExtract, viewModel)
                    }
                }
            }

            if (state.isRunning) {
                ExtractLoadingExperience(
                    phase = extractPhaseFromProgress(state.progressCurrent, state.progressTotal, state.isRunning),
                    progressCurrent = state.progressCurrent,
                    progressTotal = state.progressTotal,
                )
                DfPrimaryButton(text = "لغو استخراج", onClick = viewModel::cancel)
            } else {
                DfPrimaryButton(
                    text = "شروع استخراج و آپلود",
                    onClick = viewModel::startExtraction,
                    enabled = canExtract && state.canExtractNow && state.maxItems > 0,
                )
            }

            state.gateMessage?.let { gate ->
                Text(gate, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            state.message?.let {
                Text(it, color = DfColors.PurpleDark, fontWeight = FontWeight.Medium)
                state.lastUploadStats?.let { stats ->
                    Spacer(Modifier.height(8.dp))
                    IngestStatsCard(stats)
                }
                state.lastDatasetId?.let { id ->
                    Spacer(Modifier.height(8.dp))
                    DfPrimaryButton(text = "مشاهده در فایلینگ", onClick = { onViewDataset(id) })
                }
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AdvancedFilters(
    state: ExtractUiState,
    canExtract: Boolean,
    viewModel: ExtractViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.isRent) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.depositMin,
                    onValueChange = viewModel::onDepositMinChange,
                    label = { Text("ودیعه از") },
                    modifier = Modifier.weight(1f),
                    enabled = canExtract && !state.isRunning,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.depositMax,
                    onValueChange = viewModel::onDepositMaxChange,
                    label = { Text("ودیعه تا") },
                    modifier = Modifier.weight(1f),
                    enabled = canExtract && !state.isRunning,
                    singleLine = true,
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.rentMin,
                    onValueChange = viewModel::onRentMinChange,
                    label = { Text("اجاره از") },
                    modifier = Modifier.weight(1f),
                    enabled = canExtract && !state.isRunning,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.rentMax,
                    onValueChange = viewModel::onRentMaxChange,
                    label = { Text("اجاره تا") },
                    modifier = Modifier.weight(1f),
                    enabled = canExtract && !state.isRunning,
                    singleLine = true,
                )
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.priceMin,
                    onValueChange = viewModel::onPriceMinChange,
                    label = { Text("قیمت از") },
                    modifier = Modifier.weight(1f),
                    enabled = canExtract && !state.isRunning,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.priceMax,
                    onValueChange = viewModel::onPriceMaxChange,
                    label = { Text("قیمت تا") },
                    modifier = Modifier.weight(1f),
                    enabled = canExtract && !state.isRunning,
                    singleLine = true,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.areaMin,
                onValueChange = viewModel::onAreaMinChange,
                label = { Text("متراژ از") },
                modifier = Modifier.weight(1f),
                enabled = canExtract && !state.isRunning,
                singleLine = true,
            )
            OutlinedTextField(
                value = state.areaMax,
                onValueChange = viewModel::onAreaMaxChange,
                label = { Text("متراژ تا") },
                modifier = Modifier.weight(1f),
                enabled = canExtract && !state.isRunning,
                singleLine = true,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.yearMin,
                onValueChange = viewModel::onYearMinChange,
                label = { Text("سال ساخت از") },
                modifier = Modifier.weight(1f),
                enabled = canExtract && !state.isRunning,
                singleLine = true,
            )
            OutlinedTextField(
                value = state.yearMax,
                onValueChange = viewModel::onYearMaxChange,
                label = { Text("سال ساخت تا") },
                modifier = Modifier.weight(1f),
                enabled = canExtract && !state.isRunning,
                singleLine = true,
            )
        }
        OutlinedTextField(
            value = state.rooms,
            onValueChange = viewModel::onRoomsChange,
            label = { Text("اتاق (مثلاً 2,3)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = canExtract && !state.isRunning,
            singleLine = true,
        )
    }
}

@Composable
private fun IngestStatsCard(stats: ir.divarfiling.mobile.core.network.ExtractionUploadData) {
    DfPremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            stats.datasetName?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IngestStatRow("پردازش‌شده", stats.ingestedCount)
            IngestStatRow("جدید", stats.createdCount)
            IngestStatRow("به‌روزرسانی", stats.updatedCount)
            if (stats.duplicateCount > 0) {
                IngestStatRow("تکراری/ردشده", stats.duplicateCount + stats.skippedCount)
            } else if (stats.skippedCount > 0) {
                IngestStatRow("ردشده", stats.skippedCount)
            }
            IngestStatRow("کل فایلینگ", stats.totalInDataset)
            if (stats.datasetMerged) {
                Text("با فایلینگ قبلی ادغام شد", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun IngestStatRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), fontWeight = FontWeight.SemiBold)
    }
}
