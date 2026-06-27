package ir.divarfiling.mobile.feature.filing

import ir.divarfiling.mobile.core.design.DfColors

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.feature.crm.ContactPickerSheet
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    onBack: () -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listing = state.listing
    val formatter = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pendingWhatsAppShare) {
        val message = state.pendingWhatsAppShare ?: return@LaunchedEffect
        val text = Uri.encode(message)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$text")))
        viewModel.clearPendingWhatsAppShare()
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            DfTopBar(
                title = listing?.title?.take(40) ?: "جزئیات آگهی",
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
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
                                            color = DfColors.Purple,
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
                                        Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                                    }
                                }
                            }
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { viewModel.toggleContactPicker(true) }) {
                                    Text("ارسال به مخاطب")
                                }
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

    if (state.showContactPicker) {
        ContactPickerSheet(
            onDismiss = { viewModel.toggleContactPicker(false) },
            onContactSelected = { contact -> viewModel.onContactSelectedForSend(contact.id) },
        )
    }

    if (state.showSendDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSendDialog,
            title = { Text("ارسال فایل به مخاطب") },
            text = {
                OutlinedTextField(
                    value = state.sendNote,
                    onValueChange = viewModel::onSendNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("یادداشت (اختیاری)") },
                    minLines = 3,
                    placeholder = { Text("پیام همراه فایل برای مخاطب") },
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.sendToContact(false) }, enabled = !state.isLinking) {
                    Text("ارسال")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSendDialog) { Text("انصراف") }
            },
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
            Text(value, style = AppTypography.bodyDescription)
        }
    }
}
