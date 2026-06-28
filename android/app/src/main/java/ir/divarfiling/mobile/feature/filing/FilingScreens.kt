package ir.divarfiling.mobile.feature.filing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDatasetCardSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.feature.filing.components.FilingCategoryTabsRow
import ir.divarfiling.mobile.feature.filing.components.FilingDatasetFilters
import ir.divarfiling.mobile.feature.filing.components.FilingDatasetGridCard
import ir.divarfiling.mobile.feature.filing.components.FilingDatasetListRow
import ir.divarfiling.mobile.feature.filing.components.FilingDatasetsSection
import ir.divarfiling.mobile.feature.filing.components.FilingExtractFab
import ir.divarfiling.mobile.feature.filing.components.FilingFilterBar
import ir.divarfiling.mobile.feature.filing.components.FilingHubHeader
import ir.divarfiling.mobile.feature.filing.components.FilingSearchToolbar
import ir.divarfiling.mobile.feature.filing.components.FilingStatsRow
import ir.divarfiling.mobile.feature.filing.components.FilingTutorialBanner
import ir.divarfiling.mobile.feature.filing.components.FilingViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetsScreen(
    onDatasetClick: (String) -> Unit,
    onGlobalSearch: (String) -> Unit = {},
    onNavigateExtract: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    onNavigateTools: () -> Unit = {},
    onDatasetMapClick: (String) -> Unit = {},
    onDatasetInsightsClick: (String) -> Unit = {},
    viewModel: DatasetsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchDraft by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var formatFilter by remember { mutableStateOf(FilingDatasetFilters.ALL_FORMATS) }
    var cityFilter by remember { mutableStateOf(FilingDatasetFilters.ALL_CITIES) }
    var transactionFilter by remember { mutableStateOf(FilingDatasetFilters.ALL_TRANSACTIONS) }
    var viewMode by remember { mutableStateOf(FilingViewMode.List) }
    var showTutorial by remember { mutableStateOf(true) }
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

    Scaffold(
        containerColor = DfScreenContainerColor,
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
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl + 72.dp),
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
                    FilingSearchToolbar(
                        query = searchDraft,
                        onQueryChange = { searchDraft = it },
                        onSearch = { onGlobalSearch(searchDraft.trim()) },
                        onUploadClick = onNavigateExtract,
                        onTutorialClick = { showTutorial = true },
                        onToolsClick = onNavigateTools,
                        onCompareClick = {
                            state.datasets.firstOrNull()?.let { onDatasetInsightsClick(it.id) }
                        },
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
                    FilingCategoryTabsRow(
                        selectedTabId = selectedCategory,
                        onTabSelected = { selectedCategory = it },
                    )
                }
                if (showTutorial) {
                    item {
                        FilingTutorialBanner(
                            onDismiss = { showTutorial = false },
                            onWatchClick = { showTutorial = false },
                        )
                    }
                }
                item {
                    FilingFilterBar(
                        formats = FilingDatasetFilters.uniqueFormats(state.datasets),
                        cities = FilingDatasetFilters.uniqueCities(state.datasets),
                        transactions = FilingDatasetFilters.uniqueTransactions(state.datasets),
                        selectedFormat = formatFilter,
                        selectedCity = cityFilter,
                        selectedTransaction = transactionFilter,
                        viewMode = viewMode,
                        onFormatChange = { formatFilter = it },
                        onCityChange = { cityFilter = it },
                        onTransactionChange = { transactionFilter = it },
                        onViewModeChange = { viewMode = it },
                        onApplyFilters = { },
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
                            if (viewMode == FilingViewMode.Grid) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                    filteredDatasets.chunked(2).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                        ) {
                                            rowItems.forEach { dataset ->
                                                FilingDatasetGridCard(
                                                    dataset = dataset,
                                                    onClick = { onDatasetClick(dataset.id) },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            if (rowItems.size == 1) {
                                                Box(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                    filteredDatasets.forEachIndexed { index, dataset ->
                                        FilingDatasetListRow(
                                            dataset = dataset,
                                            onClick = { onDatasetClick(dataset.id) },
                                            onMapClick = { onDatasetMapClick(dataset.id) },
                                            onInsightsClick = { onDatasetInsightsClick(dataset.id) },
                                            onRefreshClick = viewModel::refresh,
                                            isFavorite = favoriteIds.contains(dataset.id),
                                            onToggleFavorite = {
                                                favoriteIds = if (favoriteIds.contains(dataset.id)) {
                                                    favoriteIds - dataset.id
                                                } else {
                                                    favoriteIds + dataset.id
                                                }
                                            },
                                        )
                                        if (index < filteredDatasets.lastIndex) {
                                            HorizontalDivider(color = DfColors.OutlineSubtle)
                                        }
                                    }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    datasetId: String,
    onBack: () -> Unit = {},
    onListingClick: (String) -> Unit = {},
    onInsightsClick: (String) -> Unit = {},
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showFilters by remember { mutableStateOf(false) }
    val filterCount = activeListingFilterCount(
        state.priceMin, state.priceMax, state.areaMin, state.areaMax, state.rooms,
    )

    LaunchedEffect(datasetId) { viewModel.load(datasetId) }

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
        topBar = {
            DfTopBar(
                title = state.datasetName ?: "آگهی‌ها",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { onInsightsClick(datasetId) }) {
                        Icon(Icons.Default.Analytics, contentDescription = "تحلیل و نقشه")
                    }
                    BadgedBox(
                        badge = {
                            if (filterCount > 0) {
                                androidx.compose.material3.Badge { Text("$filterCount") }
                            }
                        },
                    ) {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(Icons.Default.Tune, contentDescription = "فیلتر")
                        }
                    }
                },
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh(datasetId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DfSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "جستجو در عنوان…",
                        onSearch = { viewModel.load(datasetId, reset = true) },
                    )
                }
                if (filterCount > 0) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            state.priceMin?.let { DfBadge(text = "از ${formatFilterNumber(it)}") }
                            state.priceMax?.let { DfBadge(text = "تا ${formatFilterNumber(it)}") }
                            state.areaMin?.let { DfBadge(text = "متراژ از $it") }
                            state.areaMax?.let { DfBadge(text = "متراژ تا $it") }
                            state.rooms?.let { DfBadge(text = "$it اتاق") }
                        }
                    }
                }
                state.error?.let { error ->
                    item { DfErrorBanner(error) }
                }
                if (state.isLoading && state.listings.isEmpty()) {
                    item { DfCardListSkeleton(count = 6, itemHeight = 120.dp) }
                } else if (!state.isLoading && state.listings.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = "آگهی‌ای یافت نشد",
                            subtitle = "فیلتر جستجو را تغییر دهید یا فایل دیگری انتخاب کنید",
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
        topBar = {
            DfTopBar(
                title = "جستجوی فایلینگ",
                onBack = onBack,
                actions = {
                    BadgedBox(
                        badge = {
                            if (filterCount > 0) {
                                androidx.compose.material3.Badge { Text("$filterCount") }
                            }
                        },
                    ) {
                        IconButton(onClick = { showFilters = true }) {
                            Icon(Icons.Default.Tune, contentDescription = "فیلتر")
                        }
                    }
                },
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DfSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "عنوان، محله یا شهر…",
                        onSearch = { viewModel.search(reset = true) },
                    )
                }
                state.error?.let { error ->
                    item { DfErrorBanner(error) }
                }
                if (state.query.isBlank()) {
                    item {
                        DfEmptyState(
                            title = "جستجو در همه فایل‌ها",
                            subtitle = "عبارت مورد نظر را وارد کنید تا در تمام datasetها جستجو شود",
                        )
                    }
                } else if (state.isLoading && state.listings.isEmpty()) {
                    item { DfCardListSkeleton(count = 6, itemHeight = 88.dp) }
                } else if (!state.isLoading && state.listings.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = "نتیجه‌ای یافت نشد",
                            subtitle = "عبارت یا فیلترها را تغییر دهید",
                        )
                    }
                } else {
                    items(state.listings, key = { it.token }) { listing ->
                        SearchListingItem(
                            listing = listing,
                            onClick = { onListingClick(listing.token) },
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
) {
    FilingListingCard(
        listing = listing,
        onClick = onClick,
        datasetLabel = listing.datasetName?.takeIf { it.isNotBlank() },
    )
}

private fun formatFilterNumber(value: Long): String {
    return when {
        value >= 1_000_000_000 -> "${value / 1_000_000_000} میلیارد"
        value >= 1_000_000 -> "${value / 1_000_000} میلیون"
        else -> value.toString()
    }
}
