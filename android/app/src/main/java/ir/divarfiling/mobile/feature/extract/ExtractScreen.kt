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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.license.ExtractLightLimits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractScreen(
    onViewDataset: (String) -> Unit,
    viewModel: ExtractViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val canExtract = state.license.canUseLightExtract && state.gateMessage == null
    val scroll = rememberScrollState()
    val tx = ExtractCategories.transactionTypes.firstOrNull { it.label == state.transactionType }
    val subcategories = tx?.subcategories.orEmpty()

    Scaffold(topBar = { TopAppBar(title = { Text("استخراج سبک") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LicenseCard(canExtract, state)

            ExtractDropdown(
                label = "نوع معامله",
                value = state.transactionType,
                options = ExtractCategories.transactionTypes.map { it.label },
                enabled = canExtract && !state.isRunning,
                onSelect = viewModel::onTransactionTypeChange,
            )
            ExtractDropdown(
                label = "زیردسته",
                value = state.subcategoryLabel,
                options = subcategories.map { it.label },
                enabled = canExtract && !state.isRunning,
                onSelect = viewModel::onSubcategoryChange,
            )
            ExtractDropdown(
                label = "استان",
                value = state.provinceName,
                options = state.provinces,
                enabled = canExtract && !state.isRunning,
                onSelect = viewModel::onProvinceChange,
            )
            ExtractDropdown(
                label = "شهر",
                value = state.cityName,
                options = state.cities.map { it.name },
                enabled = canExtract && !state.isRunning,
                onSelect = { name ->
                    state.cities.firstOrNull { it.name == name }?.let(viewModel::onCityChange)
                },
            )
            if (state.districts.isNotEmpty()) {
                ExtractDropdown(
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
            ExtractDropdown(
                label = "مرتب‌سازی",
                value = ExtractCategories.sortOptions.firstOrNull { it.first == state.sort }?.second ?: "",
                options = ExtractCategories.sortOptions.map { it.second },
                enabled = canExtract && !state.isRunning,
                onSelect = { label ->
                    ExtractCategories.sortOptions.firstOrNull { it.second == label }?.first?.let(viewModel::onSortChange)
                },
            )
            ExtractDropdown(
                label = "نوع آگهی‌دهنده",
                value = ExtractCategories.advertiserOptions.firstOrNull { it.first == state.advertiserFilter }?.second ?: "",
                options = ExtractCategories.advertiserOptions.map { it.second },
                enabled = canExtract && !state.isRunning,
                onSelect = { label ->
                    ExtractCategories.advertiserOptions.firstOrNull { it.second == label }?.first
                        ?.let(viewModel::onAdvertiserFilterChange)
                },
            )

            OutlinedTextField(
                value = state.maxItems.toString(),
                onValueChange = { viewModel.onMaxItemsChange(it.toIntOrNull() ?: 50) },
                label = { Text("تعداد (حداکثر ۱۰۰)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = canExtract && !state.isRunning,
                singleLine = true,
            )

            TextButton(onClick = viewModel::toggleAdvanced, enabled = canExtract && !state.isRunning) {
                Text(if (state.showAdvanced) "بستن فیلترهای پیشرفته" else "فیلترهای پیشرفته (قیمت، متراژ، …)")
            }

            if (state.showAdvanced) {
                AdvancedFilters(state, canExtract, viewModel)
            }

            if (state.isRunning) {
                if (state.progressTotal > 0) {
                    LinearProgressIndicator(
                        progress = { state.progressCurrent.toFloat() / state.progressTotal },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("${state.progressCurrent} / ${state.progressTotal}")
                } else {
                    CircularProgressIndicator()
                }
                Button(onClick = viewModel::cancel, modifier = Modifier.fillMaxWidth()) {
                    Text("لغو")
                }
            } else {
                Button(
                    onClick = viewModel::startExtraction,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canExtract,
                ) {
                    Text("شروع استخراج و آپلود")
                }
            }

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                state.lastDatasetId?.let { id ->
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onViewDataset(id) }) {
                        Text("مشاهده در فایلینگ")
                    }
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
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (canExtract) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("لایسنس", style = MaterialTheme.typography.titleMedium)
            Text(state.license.licenseLabel)
            if (canExtract) {
                Text("حداکثر ${ExtractLightLimits.MAX_ITEMS} آگهی — منطق هم‌تراز با ویندوز")
                Text("نتیجه مستقیم به Workspace آپلود می‌شود")
            } else {
                Text(
                    state.gateMessage ?: "استخراج فقط با لایسنس فعال امکان‌پذیر است.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtractDropdown(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = enabled,
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
