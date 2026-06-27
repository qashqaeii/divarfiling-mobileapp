package ir.divarfiling.mobile.feature.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendFilingSheet(
    step: Int,
    datasets: List<DatasetDto>,
    listings: List<ListingDto>,
    note: String,
    isLoading: Boolean,
    isSubmitting: Boolean,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onDatasetSelected: (String) -> Unit,
    onBackToDatasets: () -> Unit,
    onListingSend: (ListingDto, Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (step == 0) "انتخاب فایلینگ" else "انتخاب آگهی برای ارسال",
                style = AppTypography.sectionTitle,
            )
            if (step == 1) {
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("یادداشت برای مخاطب") },
                    minLines = 2,
                    placeholder = { Text("مثلاً: این ملک مناسب بودجه شماست") },
                )
                TextButton(onClick = onBackToDatasets) { Text("بازگشت به لیست فایلینگ") }
            }
            when {
                isLoading -> DfCardListSkeleton(count = 4, itemHeight = 72.dp)
                step == 0 -> {
                    if (datasets.isEmpty()) {
                        DfEmptyState(title = "فایلینگی یافت نشد", subtitle = "ابتدا استخراج انجام دهید")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(datasets, key = { it.id }) { dataset ->
                                DfPremiumCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onDatasetSelected(dataset.id) },
                                ) {
                                    Column(Modifier.padding(4.dp)) {
                                        Text(dataset.name ?: dataset.id, style = AppTypography.cardTitle)
                                        dataset.itemCount.let {
                                            if (it > 0) Text("$it آگهی", style = AppTypography.bodyDescription)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                listings.isEmpty() -> {
                    DfEmptyState(title = "آگهی‌ای یافت نشد", subtitle = "")
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(listings, key = { it.token }) { listing ->
                            DfPremiumCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(listing.title ?: listing.token, style = AppTypography.cardTitle)
                                    listing.price?.let {
                                        Text(FormatUtils.formatPriceToman(it), style = AppTypography.bodyDescription)
                                    }
                                    listing.district?.let {
                                        Text(it, style = AppTypography.labelSmall)
                                    }
                                    DfPrimaryButton(
                                        text = "ارسال به مخاطب",
                                        onClick = { onListingSend(listing, false) },
                                        enabled = !isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    DfPrimaryButton(
                                        text = "ارسال + واتساپ",
                                        onClick = { onListingSend(listing, true) },
                                        enabled = !isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
