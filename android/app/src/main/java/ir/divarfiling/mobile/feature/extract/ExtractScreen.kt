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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfCountSlider
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.license.ExtractLightLimits
import ir.divarfiling.mobile.feature.extract.ExtractProgressPanel

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

    Scaffold(topBar = { DfTopBar(title = "استخراج فایل", showLogo = true, onBack = onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LicenseCard(canExtract, state)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onOpenSchedules) { Text("زمان‌بندی‌ها") }
                state.remainingToday?.let { remaining ->
                    Text(
                        "باقی‌مانده امروز: $remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.canExtractNow) DfColors.Green else MaterialTheme.colorScheme.error,
                    )
                }
            }

            OutlinedTextField(
                value = state.scheduleIntervalHours.toString(),
                onValueChange = viewModel::onScheduleIntervalChange,
                label = { Text("فاصله زمان‌بندی (ساعت)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = canExtract && !state.isRunning,
                singleLine = true,
            )
            TextButton(
                onClick = viewModel::createSchedule,
                enabled = canExtract && !state.isRunning,
            ) {
                Text("ذخیره فیلترها به‌عنوان زمان‌بندی خودکار")
            }

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
                Text(if (state.showAdvanced) "بستن فیلترهای پیشرفته" else "فیلترهای پیشرفته (قیمت، متراژ، …)")
            }

            if (state.showAdvanced) {
                AdvancedFilters(state, canExtract, viewModel)
            }

            if (state.isRunning) {
                ExtractProgressPanel(state.progressCurrent, state.progressTotal)
                DfPrimaryButton(text = "لغو استخراج", onClick = viewModel::cancel)
            } else {
                DfPrimaryButton(
                    text = "شروع استخراج و آپلود",
                    onClick = viewModel::startExtraction,
                    enabled = canExtract && state.canExtractNow && state.maxItems > 0,
                )
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
private fun LicenseCard(canExtract: Boolean, state: ExtractUiState) {
    DfCard(
        containerColor = if (canExtract) DfColors.PurpleContainer else MaterialTheme.colorScheme.errorContainer,
    ) {
        Text("لایسنس", style = MaterialTheme.typography.titleMedium)
        Text(state.license.licenseLabel)
        if (canExtract) {
            Text("حداکثر ${ExtractLightLimits.MAX_ITEMS} آگهی — هم‌تراز با ویندوز")
            Text("خروجی با نام و فرمت استاندارد در میزکار ذخیره می‌شود")
        } else {
            Text(
                state.gateMessage ?: "استخراج فقط با لایسنس فعال امکان‌پذیر است.",
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
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
    DfCard {
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
