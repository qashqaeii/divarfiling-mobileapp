package ir.divarfiling.mobile.feature.filing

import ir.divarfiling.mobile.core.design.DossierShareOptions
import ir.divarfiling.mobile.core.design.DossierShareFormatter
import ir.divarfiling.mobile.core.design.DossierShareKind
import ir.divarfiling.mobile.core.design.components.DossierShareSheet
import ir.divarfiling.mobile.core.share.DossierShareActions
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.feature.crm.ContactPickerSheet
import ir.divarfiling.mobile.feature.crm.components.ContactReminderSheet
import ir.divarfiling.mobile.core.filing.ListingImageUtils
import ir.divarfiling.mobile.feature.share.PublicShareSettingsSheet
import ir.divarfiling.mobile.feature.filing.components.ListingDetailGallerySection
import ir.divarfiling.mobile.feature.filing.components.ListingDetailHeader
import ir.divarfiling.mobile.feature.filing.components.ListingEditSheet
import ir.divarfiling.mobile.feature.filing.components.ListingLocationSection
import ir.divarfiling.mobile.feature.filing.components.ListingOwnerPhoneSheet
import ir.divarfiling.mobile.feature.filing.components.ListingQuickActionsRow
import ir.divarfiling.mobile.feature.filing.components.ListingSpecsCard
import ir.divarfiling.mobile.core.design.components.FeatureProfilePanels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    onBack: () -> Unit,
    onOpenCreatedProperty: (Long) -> Unit = {},
    onOpenAi: (String) -> Unit = {},
    viewModel: ListingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listing = state.listing
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pendingWhatsAppShare) {
        val message = state.pendingWhatsAppShare ?: return@LaunchedEffect
        openWhatsApp(context, message)
        viewModel.clearPendingWhatsAppShare()
    }

    LaunchedEffect(state.pendingCreatedPropertyId) {
        val propertyId = state.pendingCreatedPropertyId ?: return@LaunchedEffect
        viewModel.clearPendingCreatedProperty()
        onOpenCreatedProperty(propertyId)
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
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                state.error != null && listing == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        DfDetailPageHeader(
                            title = "جزئیات آگهی",
                            onBack = onBack,
                            titleIconRes = DfDecorIcons.FileText,
                        )
                        DfErrorBanner(
                            state.error!!,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                        DfEmptyState(
                            title = "بارگذاری ناموفق",
                            subtitle = "اتصال را بررسی کنید و دوباره تلاش کنید",
                            variant = DfEmptyVariant.Error,
                            actionLabel = "تلاش مجدد",
                            onAction = viewModel::refresh,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                listing != null -> {
                    ListingDetailContent(
                        listing = listing,
                        onBack = onBack,
                        onEdit = viewModel::openEditSheet,
                        onOwnerPhone = viewModel::openOwnerPhoneSheet,
                        onSendToContact = { viewModel.toggleContactPicker(true) },
                        onShare = { viewModel.toggleShareSheet(true) },
                        onWhatsAppShare = { viewModel.toggleShareSheet(true) },
                        onOpenDivar = listing.shareLink?.takeIf { it.isNotBlank() }?.let { link ->
                            { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
                        },
                        onSetReminder = viewModel::openReminderSheet,
                        onSaveAsPersonal = viewModel::saveAsPersonalProperty,
                        onOpenAi = { onOpenAi(listing.token) },
                        onCopyLink = {
                            val publicUrl = listing.publicShare?.shareUrl?.takeIf { it.isNotBlank() }
                            if (publicUrl != null) {
                                copyToClipboard(context, publicUrl)
                                viewModel.showMessage("لینک صفحه عمومی کپی شد")
                            } else if (!listing.shareLink.isNullOrBlank()) {
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

    if (state.showShareSheet && listing != null) {
        val shareOptions = viewModel.listingShareOptions()
        val preview = DossierShareFormatter.fromDetail(listing, shareOptions)
        val publicShare = listing.publicShare
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleShareSheet(false) }) {
            DossierShareSheet(
                previewText = preview,
                kind = DossierShareKind.FILING,
                note = state.shareNote,
                includeDivarLink = state.shareIncludeLink,
                publicShareUrl = publicShare?.shareUrl,
                publicShareViewCount = publicShare?.viewCount ?: 0,
                includePublicPageLink = state.shareIncludePublicPage,
                onIncludePublicPageLinkChange = viewModel::onShareIncludePublicPageChange,
                includeAddress = false,
                includeInternalNotes = false,
                includeAmenities = state.shareIncludeAmenities,
                onNoteChange = viewModel::onShareNoteChange,
                onIncludeDivarLinkChange = viewModel::onShareIncludeLinkChange,
                onIncludeAddressChange = {},
                onIncludeInternalNotesChange = {},
                onIncludeAmenitiesChange = viewModel::onShareIncludeAmenitiesChange,
                onShare = { DossierShareActions.shareText(context, preview) },
                onWhatsApp = { DossierShareActions.openWhatsApp(context, preview) },
                onTelegram = { DossierShareActions.openTelegram(context, preview) },
                onSms = { DossierShareActions.openSms(context, preview) },
                onCopy = {
                    DossierShareActions.copyToClipboard(context, preview)
                    viewModel.showMessage("متن پیام کپی شد")
                },
                onCopyPublicLink = publicShare?.shareUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    {
                        copyToClipboard(context, url)
                        viewModel.showMessage("لینک صفحه عمومی کپی شد")
                    }
                },
                onOpenPublicPreview = publicShare?.shareUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
                onManagePublicShare = {
                    viewModel.toggleShareSheet(false)
                    viewModel.togglePublicShareSettingsSheet(true)
                },
                onSendToContact = {
                    viewModel.toggleShareSheet(false)
                    viewModel.toggleContactPicker(true)
                },
                onDismiss = { viewModel.toggleShareSheet(false) },
            )
        }
    }

    if (state.showContactPicker) {
        ContactPickerSheet(
            onDismiss = { viewModel.toggleContactPicker(false) },
            onContactSelected = { contact -> viewModel.onContactSelectedForSend(contact.id) },
        )
    }

    if (state.showSendDialog && listing != null) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissSendDialog) {
                ListingSendSheet(
                note = state.sendNote,
                previewText = DossierShareFormatter.fromDetail(
                    listing,
                    DossierShareOptions(
                        customNote = state.sendNote,
                        includePublicPageLink = true,
                        publicPageUrl = listing.publicShare?.shareUrl.orEmpty(),
                    ),
                ),
                isSubmitting = state.isLinking,
                onNoteChange = viewModel::onSendNoteChange,
                onSend = { viewModel.sendToContact(false) },
                onSendWhatsApp = { viewModel.sendToContact(true) },
                onDismiss = viewModel::dismissSendDialog,
            )
        }
    }

    if (state.showOwnerPhoneSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissOwnerPhoneSheet) {
            ListingOwnerPhoneSheet(
                name = state.ownerNameDraft,
                phone = state.ownerPhoneDraft,
                isSaving = state.isSavingPhone,
                onNameChange = viewModel::onOwnerNameChange,
                onPhoneChange = viewModel::onOwnerPhoneChange,
                onSave = viewModel::saveOwnerPhone,
                onCall = { phone -> dialPhone(context, phone) },
                onDismiss = viewModel::dismissOwnerPhoneSheet,
            )
        }
    }

    if (state.showReminderSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissReminderSheet) {
            ContactReminderSheet(
                title = state.reminderTitle,
                note = state.reminderNote,
                dueMillis = state.reminderDueMillis,
                recurrence = state.reminderRecurrence,
                isSubmitting = state.isSavingReminder,
                sheetTitle = "یادآور آگهی",
                onTitleChange = viewModel::onReminderTitleChange,
                onNoteChange = viewModel::onReminderNoteChange,
                onDueChange = viewModel::onReminderDueChange,
                onRecurrenceChange = viewModel::onReminderRecurrenceChange,
                onDismiss = viewModel::dismissReminderSheet,
                onSubmit = viewModel::createStandaloneReminder,
            )
        }
    }

    if (state.showEditSheet && listing != null) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissEditSheet) {
            val form = state.editForm
            ListingEditSheet(
                listing = listing,
                title = form.title,
                price = form.price,
                deposit = form.deposit,
                rent = form.rent,
                area = form.area,
                rooms = form.rooms,
                floor = form.floor,
                buildYear = form.buildYear,
                neighborhood = form.neighborhood,
                city = form.city,
                description = form.description,
                ownerName = form.ownerName,
                ownerPhone = form.ownerPhone,
                isSubmitting = state.isSavingEdit,
                onTitleChange = { viewModel.onEditFormChange { f -> f.copy(title = it) } },
                onPriceChange = { viewModel.onEditFormChange { f -> f.copy(price = it) } },
                onDepositChange = { viewModel.onEditFormChange { f -> f.copy(deposit = it) } },
                onRentChange = { viewModel.onEditFormChange { f -> f.copy(rent = it) } },
                onAreaChange = { viewModel.onEditFormChange { f -> f.copy(area = it) } },
                onRoomsChange = { viewModel.onEditFormChange { f -> f.copy(rooms = it) } },
                onFloorChange = { viewModel.onEditFormChange { f -> f.copy(floor = it) } },
                onBuildYearChange = { viewModel.onEditFormChange { f -> f.copy(buildYear = it) } },
                onNeighborhoodChange = { viewModel.onEditFormChange { f -> f.copy(neighborhood = it) } },
                onCityChange = { viewModel.onEditFormChange { f -> f.copy(city = it) } },
                onDescriptionChange = { viewModel.onEditFormChange { f -> f.copy(description = it) } },
                onOwnerNameChange = { viewModel.onEditFormChange { f -> f.copy(ownerName = it) } },
                onOwnerPhoneChange = { viewModel.onEditFormChange { f -> f.copy(ownerPhone = it) } },
                onCallOwner = form.ownerPhone.trim().takeIf { it.isNotBlank() }?.let { phone ->
                    { dialPhone(context, phone) }
                },
                onSave = viewModel::saveEdit,
                onDismiss = viewModel::dismissEditSheet,
            )
        }
    }

    if (state.showPublicShareSettingsSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.togglePublicShareSettingsSheet(false) }) {
            PublicShareSettingsSheet(
                consultantName = state.shareConsultantName,
                consultantPhone = state.shareConsultantPhone,
                welcomeMessage = state.shareWelcomeMessage,
                isActive = state.sharePublicIsActive,
                showDivarLink = state.sharePublicShowDivarLink,
                showFullAddress = state.sharePublicShowFullAddress,
                showInternalNotes = state.sharePublicShowInternalNotes,
                isSubmitting = state.isSavingEdit,
                onConsultantNameChange = viewModel::onShareConsultantNameChange,
                onConsultantPhoneChange = viewModel::onShareConsultantPhoneChange,
                onWelcomeMessageChange = viewModel::onShareWelcomeMessageChange,
                onIsActiveChange = viewModel::onSharePublicIsActiveChange,
                onShowDivarLinkChange = viewModel::onSharePublicShowDivarLinkChange,
                onShowFullAddressChange = viewModel::onSharePublicShowFullAddressChange,
                onShowInternalNotesChange = viewModel::onSharePublicShowInternalNotesChange,
                onSave = viewModel::savePublicShareSettings,
                onDismiss = { viewModel.togglePublicShareSettingsSheet(false) },
            )
        }
    }
}

@Composable
private fun ListingDetailContent(
    listing: ListingDetailDto,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOwnerPhone: () -> Unit,
    onSendToContact: () -> Unit,
    onShare: () -> Unit,
    onWhatsAppShare: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onSetReminder: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    onOpenAi: () -> Unit,
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
                    onBack = onBack,
                    onEdit = onEdit,
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
                    onShare = onShare,
                    onOwnerPhone = onOwnerPhone,
                    onWhatsAppShare = onWhatsAppShare,
                    onOpenDivar = onOpenDivar,
                    onSetReminder = onSetReminder,
                    onSaveAsPersonal = onSaveAsPersonal,
                    onOpenAi = onOpenAi,
                    showSaveAsPersonal = false,
                )
            }

            item {
                ListingSpecsCard(
                    listing = listing,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            item {
                FeatureProfilePanels(
                    profile = listing.featureProfile,
                    highlights = listing.listingHighlights,
                    title = "مشخصات کامل ملک",
                    subtitle = "ساختمان، امکانات، سند، شرایط سکونت و تأسیسات",
                    emptyMessage = null,
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

private fun dialPhone(context: Context, phone: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
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
