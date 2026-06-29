package ir.divarfiling.mobile.feature.filing

import ir.divarfiling.mobile.core.design.ListingMessageFormatter
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.feature.crm.components.ListingSendSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import ir.divarfiling.mobile.core.filing.ListingImageUtils
import ir.divarfiling.mobile.feature.filing.components.ListingDetailGallerySection
import ir.divarfiling.mobile.feature.filing.components.ListingDetailHeader
import ir.divarfiling.mobile.feature.filing.components.ListingLocationSection
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
        containerColor = DfScreenContainerColor,
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
                        onSaveAsPersonal = viewModel::saveAsPersonalProperty,
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
                                val uri = Uri.parse(
                                    "geo:${listing.latitude},${listing.longitude}?q=${listing.latitude},${listing.longitude}",
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
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
        DfModalBottomSheet(onDismissRequest = viewModel::dismissSendDialog) {
            ListingSendSheet(
                note = state.sendNote,
                isSubmitting = state.isLinking,
                onNoteChange = viewModel::onSendNoteChange,
                onSend = { viewModel.sendToContact(false) },
                onSendWhatsApp = { viewModel.sendToContact(true) },
                onDismiss = viewModel::dismissSendDialog,
            )
        }
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
    onSaveAsPersonal: () -> Unit,
    onCopyLink: () -> Unit,
    onCopyAdCode: () -> Unit,
    onNavigate: () -> Unit,
) {
    val galleryImages = ListingImageUtils.buildGalleryUrls(listing)
    val location = listOfNotNull(listing.district, listing.city).joinToString("، ")
    val hasCoordinates = listing.latitude != null && listing.longitude != null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
            item {
                ListingDetailGallerySection(
                    images = galleryImages,
                    isFavorite = isFavorite,
                    onBack = onBack,
                    onFavoriteToggle = onFavoriteToggle,
                    onSaveAsPersonal = onSaveAsPersonal,
                    onCopyLink = onCopyLink,
                )
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
                    onOpenDivar = onOpenDivar,
                    onSetReminder = onSetReminder,
                    onSaveAsPersonal = onSaveAsPersonal,
                    showSaveAsPersonal = false,
                )
            }

            item {
                ListingSpecsCard(
                    listing = listing,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            if (location.isNotBlank() || hasCoordinates || onOpenDivar != null) {
                item {
                    ListingLocationSection(
                        locationLabel = location,
                        hasCoordinates = hasCoordinates,
                        onNavigate = onNavigate,
                        onCopyLink = onCopyLink,
                        onOpenDivar = onOpenDivar,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }
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
