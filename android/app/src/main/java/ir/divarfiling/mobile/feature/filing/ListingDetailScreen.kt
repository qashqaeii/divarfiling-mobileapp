package ir.divarfiling.mobile.feature.filing

import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.ListingMessageFormatter
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPullRefresh

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.feature.crm.ContactPickerSheet
import ir.divarfiling.mobile.feature.filing.components.ListingDetailHeader
import ir.divarfiling.mobile.feature.filing.components.ListingDetailTopBar
import ir.divarfiling.mobile.feature.filing.components.ListingMapCard
import ir.divarfiling.mobile.feature.filing.components.ListingMosaicGallery
import ir.divarfiling.mobile.feature.filing.components.ListingQuickActionsRow
import ir.divarfiling.mobile.feature.filing.components.ListingSpecsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    onBack: () -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listing = state.listing
    val snackbarHostState = remember { SnackbarHostState() }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(state.pendingWhatsAppShare) {
        val message = state.pendingWhatsAppShare ?: return@LaunchedEffect
        openWhatsApp(context, message)
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DfColors.Background,
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DfColors.Background),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                state.error != null && listing == null -> {
                    Column(Modifier.padding(16.dp)) { DfErrorBanner(state.error!!) }
                }
                listing != null -> {
                    ListingDetailContent(
                        listing = listing,
                        isFavorite = isFavorite,
                        onBack = onBack,
                        onFavoriteToggle = { isFavorite = !isFavorite },
                        onSendToContact = { viewModel.toggleContactPicker(true) },
                        onWhatsAppShare = { openWhatsApp(context, ListingMessageFormatter.fromDetail(listing)) },
                        onOpenDivar = listing.shareLink?.takeIf { it.isNotBlank() }?.let { link ->
                            { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
                        },
                        onSetReminder = { viewModel.toggleContactPicker(true) },
                        onShare = {
                            val message = ListingMessageFormatter.fromDetail(listing)
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    },
                                    "اشتراک فایل",
                                ),
                            )
                        },
                        onCopyLink = {
                            if (!listing.shareLink.isNullOrBlank()) {
                                copyToClipboard(context, listing.shareLink!!)
                                viewModel.showMessage("لینک آگهی کپی شد")
                            } else {
                                copyToClipboard(context, listing.token)
                                viewModel.showMessage("کد آگهی کپی شد")
                            }
                        },
                        onCopyAdCode = {
                            copyToClipboard(context, listing.token)
                            viewModel.showMessage("کد آگهی کپی شد")
                        },
                        onNavigate = {
                            if (listing.latitude != null && listing.longitude != null) {
                                val uri = Uri.parse("geo:${listing.latitude},${listing.longitude}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        },
                        onCall = {
                            listing.shareLink?.takeIf { it.isNotBlank() }?.let { link ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                            } ?: viewModel.showMessage("لینک تماس در دسترس نیست")
                        },
                        onNote = { viewModel.toggleContactPicker(true) },
                        onReport = {
                            val message = "گزارش آگهی: ${listing.title.orEmpty()}\n${listing.shareLink.orEmpty()}"
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    },
                                    "گزارش آگهی",
                                ),
                            )
                        },
                    )
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
            title = { Text("ارسال به CRM") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "پیام حرفه‌ای بدون لینک دیوار ارسال می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                    )
                    OutlinedTextField(
                        value = state.sendNote,
                        onValueChange = viewModel::onSendNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("یادداشت (اختیاری)") },
                        minLines = 3,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.sendToContact(false) }, enabled = !state.isLinking) {
                    Text("ارسال")
                }
            },
            dismissButton = {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(onClick = { viewModel.sendToContact(true) }, enabled = !state.isLinking) {
                        Text("واتساپ")
                    }
                    TextButton(onClick = viewModel::dismissSendDialog) { Text("انصراف") }
                }
            },
        )
    }
}

@Composable
private fun ListingDetailContent(
    listing: ListingDetailDto,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onSendToContact: () -> Unit,
    onWhatsAppShare: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onSetReminder: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onCopyAdCode: () -> Unit,
    onNavigate: () -> Unit,
    onCall: () -> Unit,
    onNote: () -> Unit,
    onReport: () -> Unit,
) {
    val galleryImages = buildList {
        addAll(listing.images)
        listing.thumbnailUrl?.let { if (it !in listing.images) add(it) }
    }
    val location = listOfNotNull(listing.district, listing.city).joinToString("، ")
    val hasCoordinates = listing.latitude != null && listing.longitude != null

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                ListingMosaicGallery(images = galleryImages)
            }

            item {
                ListingDetailHeader(
                    listing = listing,
                    onCopyAdCode = onCopyAdCode,
                )
            }

            item {
                ListingQuickActionsRow(
                    onSendToContact = onSendToContact,
                    onWhatsAppShare = onWhatsAppShare,
                    onOpenDivar = onOpenDivar ?: onShare,
                    onSetReminder = onSetReminder,
                )
            }

            item {
                ListingSpecsCard(
                    listing = listing,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            if (hasCoordinates || location.isNotBlank()) {
                item {
                    ListingMapCard(
                        locationLabel = location,
                        hasCoordinates = hasCoordinates,
                        onNavigate = onNavigate,
                        onCall = onCall,
                        onNote = onNote,
                        onReport = onReport,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }
        }

        ListingDetailTopBar(
            onBack = onBack,
            onFavoriteToggle = onFavoriteToggle,
            isFavorite = isFavorite,
            onShare = onShare,
            onCopyLink = onCopyLink,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun openWhatsApp(context: Context, message: String) {
    val text = Uri.encode(message)
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$text")))
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("divar_link", text))
}
