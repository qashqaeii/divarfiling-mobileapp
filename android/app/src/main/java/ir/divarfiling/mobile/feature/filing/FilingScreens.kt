package ir.divarfiling.mobile.feature.filing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDatasetCardSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfStatChip
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetsScreen(
    onDatasetClick: (String) -> Unit,
    onGlobalSearch: (String) -> Unit = {},
    viewModel: DatasetsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchDraft by remember { mutableStateOf("") }

    Scaffold(topBar = { DfTopBar(title = "فایلینگ دیوار", showLogo = true) }) { padding ->
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
                        value = searchDraft,
                        onValueChange = { searchDraft = it },
                        placeholder = "جستجوی سراسری در همه فایل‌ها…",
                        onSearch = { onGlobalSearch(searchDraft.trim()) },
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DfStatChip(
                            label = "فایل‌ها",
                            value = "${state.datasets.size}",
                            modifier = Modifier.weight(1f),
                        )
                        val totalAds = state.datasets.sumOf { it.itemCount }
                        DfStatChip(
                            label = "کل آگهی",
                            value = "$totalAds",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Text(
                        "فایل‌های آپلودشده از ویندوز یا استخراج موبایل",
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                    )
                }
                state.error?.let { error ->
                    item { DfErrorBanner(error) }
                }
                if (state.isLoading && state.datasets.isEmpty()) {
                    item { DfDatasetCardSkeleton(count = 4) }
                } else if (!state.isLoading && state.datasets.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = "فایلی یافت نشد",
                            subtitle = "از تب استخراج یک فایل جدید بسازید یا از ویندوز آپلود کنید",
                        )
                    }
                } else {
                    items(state.datasets, key = { it.id }) { ds ->
                        DatasetRow(ds, onClick = { onDatasetClick(ds.id) })
                    }
                    if (state.hasMore) {
                        item {
                            TextButton(
                                onClick = viewModel::loadMore,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(if (state.isLoadingMore) "در حال بارگذاری…" else "فایل‌های بیشتر")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatasetRow(ds: DatasetDto, onClick: () -> Unit) {
    DfCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DfColors.SurfaceVariant),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                val thumb = ds.thumbnailUrl?.takeIf { it.isNotBlank() }
                if (thumb != null) {
                    AsyncImage(
                        model = thumb,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    androidx.compose.material3.Icon(
                        DfIcons.Building,
                        contentDescription = null,
                        tint = DfColors.TextMuted,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    ds.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ds.fileFormat?.uppercase()?.let { DfBadge(text = it) }
                    ds.transactionType?.let {
                        DfBadge(text = it, color = DfColors.Blue.copy(alpha = 0.1f), textColor = DfColors.Blue)
                    }
                }
                val location = listOfNotNull(ds.city, ds.district).joinToString("، ")
                if (location.isNotBlank()) {
                    Text(location, style = MaterialTheme.typography.bodySmall, color = DfColors.TextSecondary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "${ds.itemCount} آگهی",
                        style = MaterialTheme.typography.labelLarge,
                        color = DfColors.PurpleDark,
                        fontWeight = FontWeight.Medium,
                    )
                    ds.source?.let {
                        Text(
                            datasetSourceLabel(it),
                            style = MaterialTheme.typography.labelSmall,
                            color = DfColors.TextMuted,
                        )
                    }
                }
            }
        }
    }
}

private fun datasetSourceLabel(source: String): String = when {
    source.contains("mobile", ignoreCase = true) -> "موبایل"
    source.contains("windows", ignoreCase = true) -> "ویندوز"
    else -> source
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
