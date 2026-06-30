package ir.divarfiling.mobile.feature.filing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDatasetCardSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfExportLinkButton
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfExportSheet
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.export.ExportFormat
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.feature.filing.components.FilingCategoryTabsRow
import ir.divarfiling.mobile.feature.filing.components.FilingDatasetFilters
import ir.divarfiling.mobile.feature.filing.components.FilingDatasetCard
import ir.divarfiling.mobile.feature.filing.components.FilingDatasetsSection
import ir.divarfiling.mobile.feature.filing.components.FilingExtractFab
import ir.divarfiling.mobile.feature.filing.components.FilingHubHeader
import ir.divarfiling.mobile.feature.filing.components.FilingSearchFilterPanel
import ir.divarfiling.mobile.feature.filing.components.FilingStatsRow
import ir.divarfiling.mobile.feature.filing.components.ListingsActiveFilterChips
import ir.divarfiling.mobile.feature.filing.components.ListingsSearchFilterPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetsScreen(
    onDatasetClick: (String) -> Unit,
    onGlobalSearch: (String) -> Unit = {},
    onNavigateExtract: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    viewModel: DatasetsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchDraft by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var formatFilter by remember { mutableStateOf(FilingDatasetFilters.ALL_FORMATS) }
    var cityFilter by remember { mutableStateOf(FilingDatasetFilters.ALL_CITIES) }
    var transactionFilter by remember { mutableStateOf(FilingDatasetFilters.ALL_TRANSACTIONS) }
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }

    val filteredDatasets = remember(
        state.datasets,
        selectedCategory,
        formatFilter,
        cityFilter,
        transactionFilter,
        searchDraft,
        favoriteIds,
    ) {
        FilingDatasetFilters.filterDatasets(
            datasets = state.datasets,
            categoryTabId = selectedCategory,
            favoriteIds = favoriteIds,
            formatFilter = formatFilter,
            cityFilter = cityFilter,
            transactionFilter = transactionFilter,
            localQuery = searchDraft,
        )
    }

    val totalAds = remember(state.datasets) { FilingDatasetFilters.totalAds(state.datasets) }
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.exportMessage, state.deleteMessage, state.error) {
        state.exportMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearExportMessage()
        }
        state.deleteMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearDeleteMessage()
        }
        state.error?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FilingExtractFab(onClick = onNavigateExtract)
        },
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
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance + AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    FilingHubHeader(
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                    )
                }
                item {
                    FilingStatsRow(
                        totalAds = totalAds,
                        filesCount = state.datasets.size,
                        estimatedSizeGb = FilingDatasetFilters.estimatedSizeGb(totalAds),
                        datasetsThisMonth = FilingDatasetFilters.datasetsThisMonth(state.datasets),
                    )
                }
                item {
                    FilingSearchFilterPanel(
                        query = searchDraft,
                        onQueryChange = { searchDraft = it },
                        onSearch = { onGlobalSearch(searchDraft.trim()) },
                        formats = FilingDatasetFilters.uniqueFormats(state.datasets),
                        cities = FilingDatasetFilters.uniqueCities(state.datasets),
                        transactions = FilingDatasetFilters.uniqueTransactions(state.datasets),
                        selectedFormat = formatFilter,
                        selectedCity = cityFilter,
                        selectedTransaction = transactionFilter,
                        onFormatChange = { formatFilter = it },
                        onCityChange = { cityFilter = it },
                        onTransactionChange = { transactionFilter = it },
                        onApplyFilters = { },
                    )
                }
                item {
                    FilingCategoryTabsRow(
                        selectedTabId = selectedCategory,
                        onTabSelected = { selectedCategory = it },
                    )
                }
                state.error?.let { error ->
                    item { DfErrorBanner(error, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) }
                }
                if (state.isLoading && state.datasets.isEmpty()) {
                    item { DfDatasetCardSkeleton(count = 4, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) }
                } else if (!state.isLoading && filteredDatasets.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = if (state.datasets.isEmpty()) "فایلی یافت نشد" else "نتیجه‌ای با این فیلتر نیست",
                            subtitle = if (state.datasets.isEmpty()) {
                                "از استخراج جدید یک فایل بسازید یا از ویندوز آپلود کنید"
                            } else {
                                "فیلترها یا جستجو را تغییر دهید"
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    item {
                        FilingDatasetsSection(
                            title = "همه فایل‌ها",
                            count = filteredDatasets.size,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                filteredDatasets.forEach { dataset ->
                                    FilingDatasetCard(
                                        dataset = dataset,
                                        onClick = { onDatasetClick(dataset.id) },
                                        onExport = { viewModel.openExportSheet(dataset) },
                                        onDelete = { viewModel.openDeleteSheet(dataset) },
                                    )
                                }
                            }
                        }
                    }
                    if (state.hasMore) {
                        item {
                            TextButton(
                                onClick = viewModel::loadMore,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(if (state.isLoadingMore) "در حال بارگذاری…" else "مشاهده بیشتر")
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showExportSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissExportSheet) {
            DfExportSheet(
                title = "خروجی فایل",
                subtitle = state.exportTarget?.name ?: "انتخاب فرمت خروجی",
                formats = listOf(ExportFormat.XLSX, ExportFormat.JSON, ExportFormat.CSV),
                isExporting = state.isExporting,
                onSelect = { format -> viewModel.exportDataset(context, format) },
                onDismiss = viewModel::dismissExportSheet,
            )
        }
    }

    if (state.showDeleteSheet) {
        val target = state.deleteTarget
        if (target != null) {
            DfConfirmBottomSheet(
                title = "حذف فایل",
                message = "فایل «${target.name}» و ${target.itemCount} آگهی وابسته حذف می‌شود. این عمل قابل بازگشت نیست.",
                confirmText = "حذف فایل",
                cancelText = "انصراف",
                destructive = true,
                isSubmitting = state.isDeleting,
                onConfirm = viewModel::confirmDeleteDataset,
                onDismiss = viewModel::dismissDeleteSheet,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    datasetId: String,
    onBack: () -> Unit = {},
    onListingClick: (String) -> Unit = {},
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showFilters by remember { mutableStateOf(false) }
    val filterCount = activeListingFilterCount(
        state.priceMin, state.priceMax, state.areaMin, state.areaMax, state.rooms,
    )

    LaunchedEffect(datasetId) { viewModel.load(datasetId) }

    LaunchedEffect(state.exportMessage, state.error) {
        state.exportMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearExportMessage()
        }
        state.error?.let { snackbar.showSnackbar(it) }
    }

    ListingFiltersSheet(
        visible = showFilters,
        priceMin = state.priceMin,
        priceMax = state.priceMax,
        areaMin = state.areaMin,
        areaMax = state.areaMax,
        rooms = state.rooms,
        onDismiss = { showFilters = false },
        onApply = { pMin, pMax, aMin, aMax, r ->
            viewModel.applyFilters(datasetId, pMin, pMax, aMin, aMax, r)
        },
        onClear = { viewModel.clearFilters(datasetId) },
    )

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh(datasetId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = state.datasetName ?: "آگهی‌های فایل",
                        subtitle = "آگهی‌های استخراج‌شده از دیوار",
                        titleIconRes = DfDecorIcons.Building,
                        onBack = onBack,
                    )
                }
                item {
                    DfExportLinkButton(
                        onClick = viewModel::openExportSheet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                item {
                    ListingsSearchFilterPanel(
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = { viewModel.load(datasetId, reset = true) },
                        activeFilterCount = filterCount,
                        onOpenFilters = { showFilters = true },
                        activeFilterChips = {
                            ListingsActiveFilterChips(
                                priceMin = state.priceMin,
                                priceMax = state.priceMax,
                                areaMin = state.areaMin,
                                areaMax = state.areaMax,
                                rooms = state.rooms,
                                formatPrice = ::formatFilterNumber,
                            )
                        },
                    )
                }
                state.error?.let { error ->
                    item {
                        DfErrorBanner(
                            error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.isLoading && state.listings.isEmpty()) {
                    item {
                        DfCardListSkeleton(
                            count = 5,
                            itemHeight = 280.dp,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (!state.isLoading && state.listings.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = "آگهی‌ای یافت نشد",
                            subtitle = "فیلتر جستجو را تغییر دهید یا عبارت دیگری امتحان کنید",
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(state.listings, key = { it.token }) { listing ->
                        FilingListingCard(
                            listing = listing,
                            onClick = { onListingClick(listing.token) },
                            onOpenDivar = listing.shareLink?.takeIf { it.isNotBlank() }?.let { link ->
                                {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(link)),
                                    )
                                }
                            },
                            onShare = listing.shareLink?.takeIf { it.isNotBlank() }?.let { link ->
                                {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, link)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "اشتراک آگهی"))
                                }
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    if (state.hasMore) {
                        item {
                            TextButton(
                                onClick = { viewModel.loadMore(datasetId) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(if (state.isLoadingMore) "در حال بارگذاری…" else "بارگذاری بیشتر")
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showExportSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissExportSheet) {
            DfExportSheet(
                title = "خروجی فایل",
                subtitle = state.datasetName ?: "انتخاب فرمت خروجی",
                formats = listOf(ExportFormat.XLSX, ExportFormat.JSON, ExportFormat.CSV),
                isExporting = state.isExporting,
                onSelect = { format -> viewModel.exportDataset(context, format) },
                onDismiss = viewModel::dismissExportSheet,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilingSearchScreen(
    initialQuery: String = "",
    onBack: () -> Unit = {},
    onListingClick: (String) -> Unit = {},
    viewModel: FilingSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showFilters by remember { mutableStateOf(false) }
    val filterCount = activeListingFilterCount(
        state.priceMin, state.priceMax, state.areaMin, state.areaMax, state.rooms,
    )

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) viewModel.setInitialQuery(initialQuery)
    }

    ListingFiltersSheet(
        visible = showFilters,
        priceMin = state.priceMin,
        priceMax = state.priceMax,
        areaMin = state.areaMin,
        areaMax = state.areaMax,
        rooms = state.rooms,
        onDismiss = { showFilters = false },
        onApply = viewModel::applyFilters,
        onClear = viewModel::clearFilters,
    )

    Scaffold(
        containerColor = DfScreenContainerColor,
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
                contentPadding = PaddingValues(bottom = AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "جستجوی فایلینگ",
                        subtitle = "جستجو در همه فایل‌های استخراج‌شده",
                        titleIconRes = DfDecorIcons.Search,
                        onBack = onBack,
                    )
                }
                item {
                    ListingsSearchFilterPanel(
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = { viewModel.search(reset = true) },
                        activeFilterCount = filterCount,
                        onOpenFilters = { showFilters = true },
                        activeFilterChips = {
                            ListingsActiveFilterChips(
                                priceMin = state.priceMin,
                                priceMax = state.priceMax,
                                areaMin = state.areaMin,
                                areaMax = state.areaMax,
                                rooms = state.rooms,
                                formatPrice = ::formatFilterNumber,
                            )
                        },
                    )
                }
                state.error?.let { error ->
                    item {
                        DfErrorBanner(
                            error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.query.isBlank()) {
                    item {
                        DfEmptyState(
                            title = "جستجو در همه فایل‌ها",
                            subtitle = "عبارت مورد نظر را وارد کنید تا در تمام فایل‌ها جستجو شود",
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (state.isLoading && state.listings.isEmpty()) {
                    item {
                        DfCardListSkeleton(
                            count = 5,
                            itemHeight = 280.dp,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (!state.isLoading && state.listings.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = "نتیجه‌ای یافت نشد",
                            subtitle = "عبارت یا فیلترها را تغییر دهید",
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(state.listings, key = { it.token }) { listing ->
                        SearchListingItem(
                            listing = listing,
                            onClick = { onListingClick(listing.token) },
                            context = context,
                        )
                    }
                    if (state.hasMore) {
                        item {
                            TextButton(
                                onClick = viewModel::loadMore,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(if (state.isLoadingMore) "در حال بارگذاری…" else "بارگذاری بیشتر")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchListingItem(
    listing: ListingDto,
    onClick: () -> Unit,
    context: android.content.Context,
) {
    FilingListingCard(
        listing = listing,
        onClick = onClick,
        datasetLabel = listing.datasetName?.takeIf { it.isNotBlank() },
        onOpenDivar = listing.shareLink?.takeIf { it.isNotBlank() }?.let { link ->
            {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            }
        },
        onShare = listing.shareLink?.takeIf { it.isNotBlank() }?.let { link ->
            {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, link)
                }
                context.startActivity(Intent.createChooser(shareIntent, "اشتراک آگهی"))
            }
        },
        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
    )
}

private fun formatFilterNumber(value: Long): String {
    return when {
        value >= 1_000_000_000 -> "${value / 1_000_000_000} میلیارد"
        value >= 1_000_000 -> "${value / 1_000_000} میلیون"
        else -> value.toString()
    }
}
