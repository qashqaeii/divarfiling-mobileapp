package ir.divarfiling.mobile.feature.filing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfListingRow
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
    viewModel: DatasetsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { DfTopBar(title = "فایلینگ دیوار", showLogo = true) }) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DfStatChip(label = "فایل‌ها", value = "${state.datasets.size}")
                    val totalAds = state.datasets.sumOf { it.itemCount }
                    DfStatChip(label = "کل آگهی", value = "$totalAds")
                }

                Text(
                    "فایل‌های آپلودشده از ویندوز یا استخراج موبایل",
                    style = MaterialTheme.typography.bodySmall,
                    color = DfColors.TextSecondary,
                )

                state.error?.let { DfErrorBanner(it) }

                if (!state.isLoading && state.datasets.isEmpty() && state.error == null) {
                    DfEmptyState(
                        title = "فایلی یافت نشد",
                        subtitle = "از تب استخراج یک فایل جدید بسازید یا از ویندوز آپلود کنید",
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.datasets, key = { it.id }) { ds ->
                            DatasetRow(ds, onClick = { onDatasetClick(ds.id) })
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                ds.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ds.fileFormat?.uppercase()?.let { DfBadge(text = it) }
                ds.transactionType?.let { DfBadge(text = it, color = DfColors.Blue.copy(alpha = 0.1f), textColor = DfColors.Blue) }
                ds.subcategory?.let { DfBadge(text = it, color = DfColors.SurfaceVariant, textColor = DfColors.TextSecondary) }
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
            ds.originalFilename?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
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
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(datasetId) { viewModel.load(datasetId) }

    Scaffold(
        topBar = {
            DfTopBar(
                title = state.datasetName ?: "آگهی‌ها",
                onBack = onBack,
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.load(datasetId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DfSearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "جستجو در عنوان…",
                    onSearch = { viewModel.load(datasetId) },
                )

                state.error?.let { DfErrorBanner(it) }

                if (!state.isLoading && state.listings.isEmpty() && state.error == null) {
                    DfEmptyState(
                        title = "آگهی‌ای یافت نشد",
                        subtitle = "فیلتر جستجو را تغییر دهید یا فایل دیگری انتخاب کنید",
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.listings, key = { it.token }) { listing ->
                            ListingItem(
                                listing = listing,
                                onOpenDivar = listing.shareLink?.let { link ->
                                    {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(link)),
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingItem(
    listing: ListingDto,
    onOpenDivar: (() -> Unit)?,
) {
    val advertiser = listing.advertiserType
        ?: listing.businessType?.takeIf { it.isNotBlank() }
    DfListingRow(
        title = listing.title ?: "بدون عنوان",
        price = listing.price,
        area = listing.area,
        rooms = listing.rooms,
        district = listing.district,
        advertiserType = advertiser,
        onClick = onOpenDivar,
    )
}
