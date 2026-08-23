package ir.divarfiling.mobile.feature.filing

import ir.divarfiling.mobile.core.design.DossierShareOptions
import ir.divarfiling.mobile.core.design.DossierShareFormatter
import ir.divarfiling.mobile.core.design.DossierShareKind
import ir.divarfiling.mobile.core.design.components.DossierShareSheet
import ir.divarfiling.mobile.core.share.DossierShareActions
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
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
import ir.divarfiling.mobile.feature.filing.components.ListingAiSummarySection
import ir.divarfiling.mobile.feature.filing.components.ListingContactsSection
import ir.divarfiling.mobile.feature.filing.components.ListingNotesSection
import ir.divarfiling.mobile.feature.filing.components.ListingNotesSheet
import ir.divarfiling.mobile.feature.filing.components.ListingMarketBenchmarkCard
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
    onOpenContact: (Long) -> Unit = {},
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
                .padding(padding),
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
                            sectionLabel = DfHeaderSections.FILING,
                            onBack = onBack,
                            titleIcon = DfIcons.File,
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
                        isFavorite = listing.meta?.isFavorite == true,
                        aiSummary = state.aiSummary,
                        isSummarizing = state.isSummarizing,
                        aiIsFallback = state.aiIsFallback,
                        onBack = onBack,
                        onFavoriteToggle = viewModel::toggleFavorite,
                        onEdit = viewModel::openEditSheet,
                        onOwnerPhone = viewModel::openOwnerPhoneSheet,
                        onSendToContact = { viewModel.toggleContactPicker(true) },
                        onShare = { viewModel.toggleShareSheet(true) },
                        onWhatsAppShare = { viewModel.toggleShareSheet(true) },
                        onOpenDivar = (listing.shareLink ?: listing.link)?.takeIf { it.isNotBlank() }?.let { link ->
                            { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
                        },
                        onSetReminder = viewModel::openReminderSheet,
                        onSaveAsPersonal = viewModel::saveAsPersonalProperty,
                        onOpenAi = { onOpenAi(listing.token) },
                        onDelete = viewModel::requestDelete,
                        onEditNotes = viewModel::openNotesSheet,
                        onGenerateSummary = viewModel::summarizeListing,
                        onCopySummary = {
                            val text = state.aiSummary
                            if (text.isNotBlank()) {
                                copyToClipboard(context, text)
                                viewModel.showMessage("خلاصه کپی شد")
                            }
                        },
                        onOpenContact = onOpenContact,
                        onUnlinkContact = viewModel::unlinkContact,
                        onCallContact = { phone -> dialPhone(context, phone) },
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
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }.onFailure {
                                    viewModel.showMessage("اپلیکیشن نقشه در دسترس نیست")
                                }
                            } else {
                                viewModel.showMessage("مختصات ملک ثبت نشده است")
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
                onBale = { DossierShareActions.openBale(context, preview) },
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
        val sendPreview = DossierShareFormatter.fromDetail(
            listing,
            DossierShareOptions(
                customNote = state.sendNote,
                includePublicPageLink = true,
                publicPageUrl = listing.publicShare?.shareUrl.orEmpty(),
            ),
        )
        DfModalBottomSheet(onDismissRequest = viewModel::dismissSendDialog) {
                ListingSendSheet(
                note = state.sendNote,
                previewText = sendPreview,
                isSubmitting = state.isLinking,
                onNoteChange = viewModel::onSendNoteChange,
                onSend = { viewModel.sendToContact(false) },
                onSendWhatsApp = { viewModel.sendToContact(true) },
                onSendBale = {
                    DossierShareActions.openBale(context, sendPreview)
                    viewModel.sendToContact(false)
                },
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
                onCall = { phone -> DossierShareActions.dial(context, phone) },
                onSms = { phone -> DossierShareActions.openSms(context, "سلام", phone) },
                onWhatsApp = { phone -> DossierShareActions.openWhatsApp(context, "سلام", phone) },
                onBale = { phone -> DossierShareActions.openBale(context, "سلام", phone) },
                onDismiss = viewModel::dismissOwnerPhoneSheet,
                divarUrl = listing?.shareLink,
                onOpenDivar = listing?.shareLink?.takeIf { it.contains("divar.ir", ignoreCase = true) }?.let { url ->
                    { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                },
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
            ListingEditSheet(
                listing = listing,
                form = state.editForm,
                isSubmitting = state.isSavingEdit,
                onFormChange = { viewModel.onEditFormChange { _ -> it } },
                onCallOwner = state.editForm.ownerPhone.trim().takeIf { it.isNotBlank() }?.let { phone ->
                    { DossierShareActions.dial(context, phone) }
                },
                onSmsOwner = state.editForm.ownerPhone.trim().takeIf { it.isNotBlank() }?.let { phone ->
                    { DossierShareActions.openSms(context, "سلام", phone) }
                },
                onWhatsAppOwner = state.editForm.ownerPhone.trim().takeIf { it.isNotBlank() }?.let { phone ->
                    { DossierShareActions.openWhatsApp(context, "سلام", phone) }
                },
                onBaleOwner = state.editForm.ownerPhone.trim().takeIf { it.isNotBlank() }?.let { phone ->
                    { DossierShareActions.openBale(context, "سلام", phone) }
                },
                onSave = viewModel::saveEdit,
                onDismiss = viewModel::dismissEditSheet,
            )
        }
    }

    if (state.showNotesSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissNotesSheet) {
            ListingNotesSheet(
                note = state.noteDraft,
                tag = state.tagDraft,
                tagOptions = listing?.meta?.tagOptions.orEmpty().ifEmpty {
                    listOf("تماس", "بازدید", "علاقه‌مند", "رد")
                },
                isSubmitting = state.isSavingMeta,
                onNoteChange = viewModel::onNoteDraftChange,
                onTagChange = viewModel::onTagDraftChange,
                onSave = viewModel::saveNotes,
                onDismiss = viewModel::dismissNotesSheet,
            )
        }
    }

    if (state.showPublicShareSettingsSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.togglePublicShareSettingsSheet(false) }) {
            PublicShareSettingsSheet(
                consultantName = state.shareConsultantName,
                consultantPhone = state.shareConsultantPhone,
                welcomeMessage = state.shareWelcomeMessage,
                defaultShareMessage = state.shareDefaultShareMessage,
                isActive = state.sharePublicIsActive,
                showDivarLink = state.sharePublicShowDivarLink,
                showFullAddress = state.sharePublicShowFullAddress,
                showInternalNotes = state.sharePublicShowInternalNotes,
                approximateLocation = state.shareApproximateLocation,
                approximateLocationRadiusM = state.shareApproximateLocationRadiusM,
                showNearbyPois = state.shareShowNearbyPois,
                isSubmitting = state.isSavingEdit,
                onConsultantNameChange = viewModel::onShareConsultantNameChange,
                onConsultantPhoneChange = viewModel::onShareConsultantPhoneChange,
                onWelcomeMessageChange = viewModel::onShareWelcomeMessageChange,
                onDefaultShareMessageChange = viewModel::onShareDefaultShareMessageChange,
                onIsActiveChange = viewModel::onSharePublicIsActiveChange,
                onShowDivarLinkChange = viewModel::onSharePublicShowDivarLinkChange,
                onShowFullAddressChange = viewModel::onSharePublicShowFullAddressChange,
                onShowInternalNotesChange = viewModel::onSharePublicShowInternalNotesChange,
                onApproximateLocationChange = viewModel::onShareApproximateLocationChange,
                onApproximateLocationRadiusChange = viewModel::onShareApproximateLocationRadiusChange,
                onShowNearbyPoisChange = viewModel::onShareShowNearbyPoisChange,
                onSave = viewModel::savePublicShareSettings,
                onDismiss = { viewModel.togglePublicShareSettingsSheet(false) },
            )
        }
    }

    if (state.showDeleteDialog) {
        DfConfirmBottomSheet(
            title = "حذف آگهی",
            message = "این آگهی از فایلینگ حذف می‌شود و قابل بازگشت نیست. ادامه می‌دهید؟",
            confirmText = "حذف آگهی",
            cancelText = "انصراف",
            destructive = true,
            isSubmitting = state.isDeleting,
            icon = DfIcons.Trash,
            onConfirm = { viewModel.deleteListing(onBack) },
            onDismiss = viewModel::dismissDeleteDialog,
        )
    }
}

@Composable
private fun ListingDetailContent(
    listing: ListingDetailDto,
    isFavorite: Boolean,
    aiSummary: String,
    isSummarizing: Boolean,
    aiIsFallback: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onEdit: () -> Unit,
    onOwnerPhone: () -> Unit,
    onSendToContact: () -> Unit,
    onShare: () -> Unit,
    onWhatsAppShare: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onSetReminder: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    onOpenAi: () -> Unit,
    onDelete: () -> Unit,
    onEditNotes: () -> Unit,
    onGenerateSummary: () -> Unit,
    onCopySummary: () -> Unit,
    onOpenContact: (Long) -> Unit,
    onUnlinkContact: (Long) -> Unit,
    onCallContact: (String) -> Unit,
    onCopyLink: () -> Unit,
    onCopyAdCode: () -> Unit,
    onNavigate: () -> Unit,
) {
    val context = LocalContext.current
    val galleryImages = ListingImageUtils.buildGalleryUrls(listing)
    val location = listOfNotNull(
        listing.address?.takeIf { it.isNotBlank() },
        listing.region?.takeIf { it.isNotBlank() },
        listing.district,
        listing.city,
    ).distinct().joinToString("، ")
    val hasCoordinates = listing.latitude != null && listing.longitude != null
    val canOpenDivar = onOpenDivar != null
    val canNavigate = hasCoordinates

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
            item {
                ListingDetailGallerySection(
                    images = galleryImages,
                    title = listing.title.orEmpty(),
                    isFavorite = isFavorite,
                    onBack = onBack,
                    onFavoriteToggle = onFavoriteToggle,
                    onEdit = onEdit,
                    onSaveAsPersonal = onSaveAsPersonal,
                    onOpenDivar = if (canOpenDivar) onOpenDivar else null,
                    onNavigate = if (canNavigate) onNavigate else null,
                    quickActions = {
                        ListingQuickActionsRow(
                            onSendToContact = onSendToContact,
                            onShare = onShare,
                            onOwnerPhone = onOwnerPhone,
                            onWhatsAppShare = onWhatsAppShare,
                            onOpenDivar = onOpenDivar,
                            onSetReminder = onSetReminder,
                            onSaveAsPersonal = onSaveAsPersonal,
                            onOpenAi = onOpenAi,
                            onDelete = onDelete,
                            showSaveAsPersonal = false,
                        )
                    },
                )
            }

            item {
                ListingDetailHeader(
                    listing = listing,
                    onCopyAdCode = onCopyAdCode,
                )
            }

            if (listing.market != null) {
                item {
                    ListingMarketBenchmarkCard(
                        market = listing.market,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            item {
                ListingNotesSection(
                    meta = listing.meta,
                    onEdit = onEditNotes,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            item {
                ListingContactsSection(
                    contacts = listing.linkedContacts,
                    ownerName = listing.ownerName.orEmpty(),
                    ownerPhone = listing.ownerPhone.orEmpty(),
                    tenantName = listing.tenantName.orEmpty(),
                    tenantPhone = listing.tenantPhone.orEmpty(),
                    isVacant = listing.isVacant,
                    onAdd = onSendToContact,
                    onContactClick = onOpenContact,
                    onCall = onCallContact,
                    onSms = { phone -> DossierShareActions.openSms(context, "سلام", phone) },
                    onWhatsApp = { phone -> DossierShareActions.openWhatsApp(context, "سلام", phone) },
                    onBale = { phone -> DossierShareActions.openBale(context, "سلام", phone) },
                    onUnlink = onUnlinkContact,
                    onEditOwner = onOwnerPhone,
                    onEditTenant = onEdit,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
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
                        address = listing.address.orEmpty(),
                        city = listing.city.orEmpty(),
                        region = listing.region.orEmpty(),
                        neighborhood = listing.neighborhood?.takeIf { it.isNotBlank() } ?: listing.district.orEmpty(),
                        latitude = listing.latitude,
                        longitude = listing.longitude,
                        hasCoordinates = hasCoordinates,
                        onNavigate = onNavigate,
                        onCopyLink = onCopyLink,
                        onOpenDivar = onOpenDivar,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            item {
                ListingAiSummarySection(
                    summary = aiSummary,
                    isLoading = isSummarizing,
                    isFallback = aiIsFallback,
                    onGenerate = onGenerateSummary,
                    onCopy = onCopySummary,
                    onOpenAssistant = onOpenAi,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
    }
}

private fun dialPhone(context: Context, phone: String) {
    DossierShareActions.dial(context, phone)
}

private fun openWhatsApp(context: Context, message: String) {
    val text = Uri.encode(message)
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=$text")))
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("divar_link", text))
}
