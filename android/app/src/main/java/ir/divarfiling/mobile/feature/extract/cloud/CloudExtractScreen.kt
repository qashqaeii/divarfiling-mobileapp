package ir.divarfiling.mobile.feature.extract.cloud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.network.CloudExtractionJobDto
import ir.divarfiling.mobile.feature.extract.ExtractCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudExtractScreen(
    onBack: () -> Unit,
    onOpenDataset: (String) -> Unit,
    viewModel: CloudExtractViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val subcategories = ExtractCategories.transactionTypes.first().subcategories

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "استخراج ابری",
                        subtitle = "استخراج از سرور بدون مصرف باتری دستگاه",
                        sectionLabel = DfHeaderSections.EXTRACT,
                        titleIconRes = DfDecorIcons.Download,
                        onBack = onBack,
                    )
                }
                item {
                    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            CityDropdown(state.cities, state.cityName, viewModel::onCityChange)
                            CategoryDropdown(subcategories.map { it.label }, state.categoryLabel, viewModel::onCategoryLabelChange)
                            OutlinedTextField(
                                value = state.maxItems,
                                onValueChange = viewModel::onMaxItemsChange,
                                label = { Text("حداکثر آگهی") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = AppShapes.Field,
                            )
                            DfPrimaryButton(
                                text = "شروع استخراج ابری",
                                onClick = viewModel::createJob,
                                loading = state.isSubmitting,
                            )
                        }
                    }
                }
                state.error?.let { error ->
                    item {
                        DfErrorBanner(error, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                }
                if (state.isLoading) {
                    item {
                        DfCardListSkeleton(count = 3, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                } else if (state.jobs.isEmpty()) {
                    item {
                        DfEmptyState(
                            title = "درخواستی ثبت نشده",
                            subtitle = "اولین استخراج ابری خود را ایجاد کنید.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(state.jobs, key = { it.id }) { job ->
                        CloudJobCard(
                            job = job,
                            onOpenDataset = onOpenDataset,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDropdown(
    cities: List<ir.divarfiling.mobile.core.places.PlaceOption>,
    selectedName: String,
    onSelect: (ir.divarfiling.mobile.core.places.PlaceOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("شهر") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = ir.divarfiling.mobile.core.design.AppShapes.Field,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.name) },
                    onClick = {
                        onSelect(city)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("دسته‌بندی") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = ir.divarfiling.mobile.core.design.AppShapes.Field,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
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

@Composable
private fun CloudJobCard(
    job: CloudExtractionJobDto,
    onOpenDataset: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusTone = when (job.status.lowercase()) {
        "success", "completed", "done" -> DfStatusTone.Success
        "failed", "error" -> DfStatusTone.Error
        "running", "pending", "queued" -> DfStatusTone.Info
        else -> DfStatusTone.Warning
    }
    val statusTitle = when (statusTone) {
        DfStatusTone.Success -> "موفق"
        DfStatusTone.Error -> "ناموفق"
        DfStatusTone.Info -> "در حال اجرا"
        else -> job.status
    }
    DfCard(
        modifier = modifier.fillMaxWidth(),
        onClick = job.datasetId?.takeIf { job.status == "success" }?.let { { onOpenDataset(it) } },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                "درخواست #${job.id}",
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
            DfStatusBanner(
                message = "${job.ingestedCount} / ${job.maxItems} آگهی",
                tone = statusTone,
                title = statusTitle,
            )
            job.createdAt?.let {
                Text(it, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
            job.error?.takeIf { it.isNotBlank() }?.let {
                DfStatusBanner(message = it, tone = DfStatusTone.Error)
            }
            job.datasetId?.takeIf { job.status == "success" }?.let {
                Text("مشاهده فایل", style = AppTypography.labelSmall, color = DfThemeColors.info())
            }
        }
    }
}
