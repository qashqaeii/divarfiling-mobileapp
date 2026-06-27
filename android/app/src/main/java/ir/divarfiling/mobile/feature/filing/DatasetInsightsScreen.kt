package ir.divarfiling.mobile.feature.filing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfActionButton
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfStatChip
import ir.divarfiling.mobile.core.design.components.DfTopBar

@Composable
fun DatasetInsightsScreen(
    onBack: () -> Unit,
    viewModel: DatasetInsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            DfTopBar(
                title = state.datasetName?.let { "تحلیل $it" } ?: "تحلیل و نقشه",
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
                state.isLoading -> Column(Modifier.padding(16.dp)) {
                    DfCardListSkeleton(count = 4, itemHeight = 120.dp)
                }
                state.error != null && state.listings.isEmpty() -> {
                    Column(Modifier.padding(16.dp)) { DfErrorBanner(state.error!!) }
                }
                state.listings.isEmpty() -> DfEmptyState(
                    title = "داده‌ای برای تحلیل نیست",
                    subtitle = "ابتدا آگهی‌ها را در این فایل بارگذاری کنید",
                )
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text(
                                "${state.listings.size} آگهی تحلیل شد",
                                style = AppTypography.bodyDescription,
                                color = DfColors.TextMuted,
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                state.avgPrice?.let {
                                    DfStatChip(
                                        label = "میانگین قیمت",
                                        value = FormatUtils.formatPriceShort(it),
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                state.avgArea?.let {
                                    DfStatChip(
                                        label = "میانگین متراژ",
                                        value = FormatUtils.formatArea(it),
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                        item {
                            DfPremiumCard {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text("تحلیل قیمت", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
                                    InsightRow("کمترین", state.minPrice?.let { FormatUtils.formatPriceToman(it) })
                                    InsightRow("بیشترین", state.maxPrice?.let { FormatUtils.formatPriceToman(it) })
                                    InsightRow("میانگین", state.avgPrice?.let { FormatUtils.formatPriceToman(it) })
                                }
                            }
                        }
                        if (state.roomDistribution.isNotEmpty()) {
                            item {
                                DfPremiumCard {
                                    Column(
                                        modifier = Modifier.padding(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text("توزیع اتاق", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            state.roomDistribution.entries.sortedBy { it.key }.forEach { (rooms, count) ->
                                                DfBadge(
                                                    text = "${FormatUtils.formatRooms(rooms)} ($count)",
                                                    color = DfColors.PurpleContainer,
                                                    textColor = DfColors.PurpleDark,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            DfPremiumCard {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    ) {
                                        Icon(DfIcons.MapPin, contentDescription = null, tint = DfColors.Green)
                                        Text("نقشه آگهی‌ها", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(
                                        if (state.mapPoints > 0) {
                                            "${state.mapPoints} آگهی با موقعیت مکانی"
                                        } else {
                                            "موقعیت مکانی برای این فایل ثبت نشده"
                                        },
                                        style = AppTypography.bodyDescription,
                                        color = DfColors.TextSecondary,
                                    )
                                    if (state.centerLat != null && state.centerLng != null) {
                                        DfActionButton(
                                            text = "مشاهده مرکز نقشه",
                                            onClick = {
                                                val uri = Uri.parse(
                                                    "geo:${state.centerLat},${state.centerLng}?q=${state.centerLat},${state.centerLng}",
                                                )
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            },
                                            icon = Icons.AutoMirrored.Filled.OpenInNew,
                                            containerColor = DfColors.GreenLight,
                                            contentColor = DfColors.Green,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            val districts = state.listings
                                .mapNotNull { it.district?.takeIf { d -> d.isNotBlank() } }
                                .groupingBy { it }
                                .eachCount()
                                .entries
                                .sortedByDescending { it.value }
                                .take(8)
                            if (districts.isNotEmpty()) {
                                DfPremiumCard {
                                    Column(
                                        modifier = Modifier.padding(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text("محله‌های پرتکرار", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
                                        districts.forEach { (name, count) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(name, style = AppTypography.bodyDescription)
                                                Text("$count", style = AppTypography.labelSmall, color = DfColors.Purple)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
            Text(value, style = AppTypography.bodyDescription, fontWeight = FontWeight.Medium)
        }
    }
}
