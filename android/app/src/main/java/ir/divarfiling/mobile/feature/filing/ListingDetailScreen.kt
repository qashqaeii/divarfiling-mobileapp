package ir.divarfiling.mobile.feature.filing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfTopBar
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    onBack: () -> Unit,
    onLinkToContact: (String, String, String) -> Unit = { _, _, _ -> },
    viewModel: ListingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listing = state.listing
    val formatter = NumberFormat.getNumberInstance(Locale("fa", "IR"))

    Scaffold(
        topBar = {
            DfTopBar(
                title = listing?.title?.take(40) ?: "جزئیات آگهی",
                onBack = onBack,
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
            when {
                state.isLoading -> DfEmptyState(title = "در حال بارگذاری…", subtitle = "")
                state.error != null && listing == null -> {
                    Column(Modifier.padding(16.dp)) { DfErrorBanner(state.error!!) }
                }
                listing != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (listing.images.isNotEmpty()) {
                            item {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(listing.images) { url ->
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .height(200.dp)
                                                .fillMaxWidth(0.85f),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            DfPremiumCard {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(listing.title ?: "—", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
                                    listing.price?.let {
                                        Text(
                                            "قیمت: ${formatter.format(it)} تومان",
                                            style = AppTypography.cardTitle,
                                            color = AppColors.Purple,
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        listing.deposit?.let {
                                            Text("ودیعه: ${formatter.format(it)}", style = AppTypography.bodyDescription)
                                        }
                                        listing.rent?.let {
                                            Text("اجاره: ${formatter.format(it)}", style = AppTypography.bodyDescription)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            DfPremiumCard {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("مشخصات ملک", style = AppTypography.cardTitle)
                                    SpecRow("متراژ", listing.area?.let { "$it متر" })
                                    SpecRow("اتاق", listing.rooms?.toString())
                                    SpecRow("سال ساخت", listing.yearBuilt)
                                    SpecRow("طبقه", listing.floor)
                                    SpecRow("شهر", listing.city)
                                    SpecRow("محله", listing.district)
                                    SpecRow("نوع آگهی‌دهنده", listing.advertiserType)
                                    listing.description?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = AppTypography.bodyDescription, color = AppColors.TextSecondary)
                                    }
                                }
                            }
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listing.shareLink?.let { link ->
                                    TextButton(onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                                    }) { Text("باز کردن دیوار") }
                                }
                                TextButton(onClick = {
                                    val share = listing.shareLink ?: ""
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "${listing.title}\n$share")
                                            },
                                            "اشتراک",
                                        ),
                                    )
                                }) { Text("اشتراک") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = AppTypography.bodyDescription, color = AppColors.TextMuted)
            Text(value, style = AppTypography.bodyDescription)
        }
    }
}
