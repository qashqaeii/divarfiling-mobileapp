package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.DossierShareFormatter
import ir.divarfiling.mobile.core.design.DossierShareOptions
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingPublicShareUpdateRequest
import ir.divarfiling.mobile.core.network.ReminderCreateRequest
import ir.divarfiling.mobile.core.network.ListingMetaDto
import ir.divarfiling.mobile.core.network.ListingMetaUpdateRequest
import ir.divarfiling.mobile.core.network.AiSummarizeListingRequest
import ir.divarfiling.mobile.core.network.ListingUpdateRequest
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.SendListingRequest
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.DealsRepository
import ir.divarfiling.mobile.data.repository.FilingRepository
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ListingEditForm(
    val title: String = "",
    val price: String = "",
    val deposit: String = "",
    val rent: String = "",
    val area: String = "",
    val rooms: String = "",
    val floor: String = "",
    val totalFloors: String = "",
    val buildYear: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val region: String = "",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val description: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val tenantName: String = "",
    val tenantPhone: String = "",
    val businessType: String = "",
    val link: String = "",
    val isExpired: Boolean = false,
    val isVacant: Boolean = false,
    val elevator: Boolean? = null,
    val parking: Boolean? = null,
    val storage: Boolean? = null,
    val balcony: Boolean? = null,
    val lobby: Boolean? = null,
    val pool: Boolean? = null,
    val roofGarden: Boolean? = null,
    val jacuzzi: Boolean? = null,
    val gym: Boolean? = null,
    val images: List<String> = emptyList(),
    val features: Map<String, String> = emptyMap(),
)

data class ListingDetailUiState(
    val listing: ListingDetailDto? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLinking: Boolean = false,
    val isSavingProperty: Boolean = false,
    val pendingCreatedPropertyId: Long? = null,
    val isSavingPhone: Boolean = false,
    val isSavingReminder: Boolean = false,
    val isSavingEdit: Boolean = false,
    val ownerNameDraft: String = "",
    val ownerPhoneDraft: String = "",
    val showContactPicker: Boolean = false,
    val showSendDialog: Boolean = false,
    val showEditSheet: Boolean = false,
    val showOwnerPhoneSheet: Boolean = false,
    val showReminderSheet: Boolean = false,
    val editForm: ListingEditForm = ListingEditForm(),
    val sendNote: String = "",
    val reminderTitle: String = "",
    val reminderNote: String = "",
    val reminderDueMillis: Long = System.currentTimeMillis() + 60 * 60 * 1000L,
    val reminderRecurrence: String = "",
    val pendingContactId: Long? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val pendingWhatsAppShare: String? = null,
    val showShareSheet: Boolean = false,
    val showPublicShareSettingsSheet: Boolean = false,
    val shareNote: String = "",
    val shareIncludeLink: Boolean = false,
    val shareIncludePublicPage: Boolean = true,
    val shareIncludeAddress: Boolean = false,
    val shareIncludeAmenities: Boolean = true,
    val shareConsultantName: String = "",
    val shareConsultantPhone: String = "",
    val shareWelcomeMessage: String = "",
    val sharePublicIsActive: Boolean = true,
    val sharePublicShowDivarLink: Boolean = false,
    val sharePublicShowFullAddress: Boolean = false,
    val sharePublicShowInternalNotes: Boolean = false,
    val shareDefaultShareMessage: String = "",
    val shareApproximateLocation: Boolean = false,
    val shareApproximateLocationRadiusM: String = "500",
    val shareShowNearbyPois: Boolean = false,
    val showNotesSheet: Boolean = false,
    val noteDraft: String = "",
    val tagDraft: String = "",
    val isSavingMeta: Boolean = false,
    val isTogglingFavorite: Boolean = false,
    val isUnlinkingContact: Boolean = false,
    val aiSummary: String = "",
    val isSummarizing: Boolean = false,
    val aiIsFallback: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
    private val crmRepository: CrmRepository,
    private val dealsRepository: DealsRepository,
    private val extrasRepository: WorkspaceExtrasRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val token: String = savedStateHandle.get<String>("token") ?: ""
    private val zone = ZoneId.systemDefault()
    private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val _uiState = MutableStateFlow(ListingDetailUiState())
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()

    init {
        if (token.isNotBlank()) load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.listing == null, error = null) }
            when (val result = filingRepository.getListingDetail(token)) {
                is ApiResult.Success -> _uiState.update {
                    val publicShare = result.data.publicShare
                    it.copy(
                        listing = result.data,
                        ownerNameDraft = result.data.ownerName.orEmpty(),
                        ownerPhoneDraft = result.data.ownerPhone.orEmpty(),
                        shareConsultantName = publicShare?.consultantName.orEmpty(),
                        shareConsultantPhone = publicShare?.consultantPhone.orEmpty(),
                        shareWelcomeMessage = publicShare?.welcomeMessage.orEmpty(),
                        sharePublicIsActive = publicShare?.isActive ?: true,
                        sharePublicShowDivarLink = publicShare?.showDivarLink ?: false,
                        sharePublicShowFullAddress = publicShare?.showFullAddress ?: false,
                        sharePublicShowInternalNotes = publicShare?.showInternalNotes ?: false,
                        shareDefaultShareMessage = publicShare?.defaultShareMessage.orEmpty(),
                        shareApproximateLocation = publicShare?.approximateLocation ?: false,
                        shareApproximateLocationRadiusM = (publicShare?.approximateLocationRadiusM ?: 500).toString(),
                        shareShowNearbyPois = publicShare?.showNearbyPois ?: false,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    fun onOwnerNameChange(name: String) = _uiState.update { it.copy(ownerNameDraft = name) }
    fun onOwnerPhoneChange(phone: String) = _uiState.update {
                it.copy(ownerPhoneDraft = PhoneNormalizer.normalize(phone))
    }

    fun openOwnerPhoneSheet() {
        val listing = _uiState.value.listing
        _uiState.update {
            it.copy(
                showOwnerPhoneSheet = true,
                ownerNameDraft = listing?.ownerName.orEmpty(),
                ownerPhoneDraft = listing?.ownerPhone.orEmpty(),
            )
        }
    }

    fun dismissOwnerPhoneSheet() = _uiState.update { it.copy(showOwnerPhoneSheet = false) }

    fun openReminderSheet() {
        val listing = _uiState.value.listing ?: return
        val defaultTitle = buildString {
            append("پیگیری آگهی")
            listing.title?.takeIf { it.isNotBlank() }?.let { append(" $it") }
        }
        val defaultNote = buildString {
            listing.city?.takeIf { it.isNotBlank() }?.let { append(it) }
            listing.district?.takeIf { it.isNotBlank() }?.let {
                if (isNotBlank()) append("، ")
                append(it)
            }
            listing.shareLink?.takeIf { it.isNotBlank() }?.let {
                if (isNotBlank()) append("\n")
                append(it)
            }
        }
        _uiState.update {
            it.copy(
                showReminderSheet = true,
                reminderTitle = defaultTitle,
                reminderNote = defaultNote,
                reminderDueMillis = System.currentTimeMillis() + 60 * 60 * 1000L,
                reminderRecurrence = "",
            )
        }
    }

    fun dismissReminderSheet() = _uiState.update { it.copy(showReminderSheet = false) }
    fun onReminderTitleChange(value: String) = _uiState.update { it.copy(reminderTitle = value) }
    fun onReminderNoteChange(value: String) = _uiState.update { it.copy(reminderNote = value) }
    fun onReminderDueChange(value: Long) = _uiState.update { it.copy(reminderDueMillis = value) }
    fun onReminderRecurrenceChange(value: String) = _uiState.update { it.copy(reminderRecurrence = value) }

    fun createStandaloneReminder() {
        val state = _uiState.value
        val title = state.reminderTitle.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "عنوان یادآور را وارد کنید") }
            return
        }
        val dueAt = Instant.ofEpochMilli(state.reminderDueMillis).atZone(zone).format(isoFormatter)
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingReminder = true) }
            when (
                val result = crmRepository.createStandaloneReminder(
                    ReminderCreateRequest(
                        title = title,
                        dueAt = dueAt,
                        note = state.reminderNote.trim(),
                        recurrence = state.reminderRecurrence,
                        token = token,
                    ),
                )
            ) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSavingReminder = false,
                        showReminderSheet = false,
                        successMessage = "یادآور ثبت شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingReminder = false, error = result.message)
                }
            }
        }
    }

    fun saveOwnerPhone() {
        val name = _uiState.value.ownerNameDraft.trim()
        val phone = PhoneNormalizer.normalize(_uiState.value.ownerPhoneDraft)
        if (phone.isNotBlank() && !PhoneNormalizer.isValidIranMobile(phone)) {
            _uiState.update { it.copy(error = "شماره موبایل مالک معتبر نیست") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPhone = true, error = null) }
            when (val result = filingRepository.updateListing(token, ListingUpdateRequest(ownerName = name, ownerPhone = phone))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        listing = result.data,
                        ownerNameDraft = result.data.ownerName.orEmpty(),
                        ownerPhoneDraft = result.data.ownerPhone.orEmpty(),
                        isSavingPhone = false,
                        showOwnerPhoneSheet = false,
                        successMessage = "اطلاعات مالک ذخیره شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingPhone = false, error = result.message)
                }
            }
        }
    }

    fun openEditSheet() {
        val listing = _uiState.value.listing ?: return
        _uiState.update {
            it.copy(
                showEditSheet = true,
                editForm = ListingEditForm(
                    title = listing.title.orEmpty(),
                    price = listing.price?.toString().orEmpty(),
                    deposit = listing.deposit?.toString().orEmpty(),
                    rent = listing.rent?.toString().orEmpty(),
                    area = listing.area?.toString().orEmpty(),
                    rooms = listing.rooms?.toString().orEmpty(),
                    floor = listing.floor.orEmpty(),
                    totalFloors = listing.totalFloors.orEmpty(),
                    buildYear = listing.yearBuilt.orEmpty(),
                    neighborhood = listing.neighborhood?.takeIf { it.isNotBlank() } ?: listing.district.orEmpty(),
                    city = listing.city.orEmpty(),
                    region = listing.region.orEmpty(),
                    address = listing.address.orEmpty(),
                    latitude = listing.latitude?.toString().orEmpty(),
                    longitude = listing.longitude?.toString().orEmpty(),
                    description = listing.description.orEmpty(),
                    ownerName = listing.ownerName.orEmpty(),
                    ownerPhone = listing.ownerPhone.orEmpty(),
                    tenantName = listing.tenantName.orEmpty(),
                    tenantPhone = listing.tenantPhone.orEmpty(),
                    businessType = listing.businessType?.takeIf { it.isNotBlank() } ?: "شخصی",
                    link = listing.link?.takeIf { it.isNotBlank() } ?: listing.shareLink.orEmpty(),
                    isExpired = listing.isExpired,
                    isVacant = listing.isVacant,
                    elevator = listing.hasElevator,
                    parking = listing.hasParking,
                    storage = listing.hasStorage,
                    balcony = listing.hasBalcony,
                    lobby = listing.hasLobby,
                    pool = listing.hasPool,
                    roofGarden = listing.hasRoofGarden,
                    jacuzzi = listing.hasJacuzzi,
                    gym = listing.hasGym,
                    images = listing.images.filter { it.isNotBlank() },
                    features = listing.featureFields.associate { field ->
                        field.key to field.value
                    },
                ),
            )
        }
    }

    fun dismissEditSheet() = _uiState.update { it.copy(showEditSheet = false) }

    fun onEditFormChange(transform: (ListingEditForm) -> ListingEditForm) {
        _uiState.update { it.copy(editForm = transform(it.editForm)) }
    }

    fun saveEdit() {
        val form = _uiState.value.editForm
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingEdit = true) }
            val request = ListingUpdateRequest(
                title = form.title.trim(),
                price = form.price.toLongOrNull(),
                deposit = form.deposit.toLongOrNull(),
                rent = form.rent.toLongOrNull(),
                area = form.area.toDoubleOrNull(),
                rooms = form.rooms.trim(),
                floor = form.floor.trim(),
                totalFloors = form.totalFloors.trim(),
                buildYear = form.buildYear.trim(),
                neighborhood = form.neighborhood.trim(),
                city = form.city.trim(),
                district = form.region.trim(),
                address = form.address.trim(),
                latitude = form.latitude.trim().toDoubleOrNull(),
                longitude = form.longitude.trim().toDoubleOrNull(),
                businessType = form.businessType.trim().ifBlank { "شخصی" },
                description = form.description.trim(),
                link = form.link.trim(),
                isExpired = form.isExpired,
                isVacant = form.isVacant,
                ownerName = form.ownerName.trim(),
                ownerPhone = form.ownerPhone.trim(),
                tenantName = form.tenantName.trim(),
                tenantPhone = form.tenantPhone.trim(),
                elevator = form.elevator.toTriPayload(),
                parking = form.parking.toTriPayload(),
                storage = form.storage.toTriPayload(),
                balcony = form.balcony.toTriPayload(),
                lobby = form.lobby.toTriPayload(),
                pool = form.pool.toTriPayload(),
                roofGarden = form.roofGarden.toTriPayload(),
                jacuzzi = form.jacuzzi.toTriPayload(),
                gym = form.gym.toTriPayload(),
                images = form.images.map { it.trim() }.filter { it.isNotBlank() },
                features = form.features,
            )
            when (val result = filingRepository.updateListing(token, request)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        listing = result.data,
                        ownerNameDraft = result.data.ownerName.orEmpty(),
                        ownerPhoneDraft = result.data.ownerPhone.orEmpty(),
                        isSavingEdit = false,
                        showEditSheet = false,
                        successMessage = "آگهی به‌روزرسانی شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingEdit = false, error = result.message)
                }
            }
        }
    }

    fun toggleContactPicker(show: Boolean) {
        _uiState.update { it.copy(showContactPicker = show, pendingContactId = null) }
    }

    fun onContactSelectedForSend(contactId: Long) {
        _uiState.update {
            it.copy(
                showContactPicker = false,
                showSendDialog = true,
                pendingContactId = contactId,
                sendNote = "",
            )
        }
    }

    fun onSendNoteChange(note: String) = _uiState.update { it.copy(sendNote = note) }

    fun toggleShareSheet(show: Boolean) = _uiState.update { it.copy(showShareSheet = show) }
    fun togglePublicShareSettingsSheet(show: Boolean) = _uiState.update { it.copy(showPublicShareSettingsSheet = show) }
    fun onShareNoteChange(note: String) = _uiState.update { it.copy(shareNote = note) }
    fun onShareIncludeLinkChange(value: Boolean) = _uiState.update { it.copy(shareIncludeLink = value) }
    fun onShareIncludePublicPageChange(value: Boolean) = _uiState.update { it.copy(shareIncludePublicPage = value) }
    fun onShareIncludeAmenitiesChange(value: Boolean) = _uiState.update { it.copy(shareIncludeAmenities = value) }
    fun onShareConsultantNameChange(value: String) = _uiState.update { it.copy(shareConsultantName = value) }
    fun onShareConsultantPhoneChange(value: String) = _uiState.update { it.copy(shareConsultantPhone = value) }
    fun onShareWelcomeMessageChange(value: String) = _uiState.update { it.copy(shareWelcomeMessage = value) }
    fun onSharePublicIsActiveChange(value: Boolean) = _uiState.update { it.copy(sharePublicIsActive = value) }
    fun onSharePublicShowDivarLinkChange(value: Boolean) = _uiState.update { it.copy(sharePublicShowDivarLink = value) }
    fun onSharePublicShowFullAddressChange(value: Boolean) = _uiState.update { it.copy(sharePublicShowFullAddress = value) }
    fun onSharePublicShowInternalNotesChange(value: Boolean) = _uiState.update { it.copy(sharePublicShowInternalNotes = value) }
    fun onShareDefaultShareMessageChange(value: String) = _uiState.update { it.copy(shareDefaultShareMessage = value) }
    fun onShareApproximateLocationChange(value: Boolean) = _uiState.update { it.copy(shareApproximateLocation = value) }
    fun onShareApproximateLocationRadiusChange(value: String) = _uiState.update { it.copy(shareApproximateLocationRadiusM = value) }
    fun onShareShowNearbyPoisChange(value: Boolean) = _uiState.update { it.copy(shareShowNearbyPois = value) }

    fun listingShareOptions(): DossierShareOptions {
        val state = _uiState.value
        val publicUrl = state.listing?.publicShare?.shareUrl.orEmpty()
        return DossierShareOptions(
            customNote = state.shareNote,
            includeDivarLink = state.shareIncludeLink,
            includePublicPageLink = state.shareIncludePublicPage && publicUrl.isNotBlank(),
            publicPageUrl = publicUrl,
            includeAmenities = state.shareIncludeAmenities,
        )
    }

    fun dismissSendDialog() {
        _uiState.update { it.copy(showSendDialog = false, pendingContactId = null, sendNote = "") }
    }

    fun savePublicShareSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingEdit = true, error = null) }
            when (
                val result = filingRepository.updateListingPublicShare(
                    token,
                    ListingPublicShareUpdateRequest(
                        consultantName = _uiState.value.shareConsultantName.trim().ifBlank { null },
                        consultantPhone = _uiState.value.shareConsultantPhone.trim().ifBlank { null },
                        welcomeMessage = _uiState.value.shareWelcomeMessage.trim().ifBlank { null },
                        showDivarLink = _uiState.value.sharePublicShowDivarLink,
                        showFullAddress = _uiState.value.sharePublicShowFullAddress,
                        showInternalNotes = _uiState.value.sharePublicShowInternalNotes,
                        isActive = _uiState.value.sharePublicIsActive,
                        defaultShareMessage = _uiState.value.shareDefaultShareMessage.trim().ifBlank { null },
                        approximateLocation = _uiState.value.shareApproximateLocation,
                        approximateLocationRadiusM = _uiState.value.shareApproximateLocationRadiusM.trim().toIntOrNull(),
                        showNearbyPois = _uiState.value.shareShowNearbyPois,
                    ),
                )
            ) {
                is ApiResult.Success -> _uiState.update { state ->
                    state.copy(
                        isSavingEdit = false,
                        showPublicShareSettingsSheet = false,
                        listing = state.listing?.copy(publicShare = result.data),
                        successMessage = "تنظیمات صفحه عمومی ذخیره شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingEdit = false, error = result.message)
                }
            }
        }
    }

    fun sendToContact(shareViaWhatsApp: Boolean = false) {
        val listing = _uiState.value.listing ?: return
        val contactId = _uiState.value.pendingContactId ?: return
        val note = _uiState.value.sendNote.trim()
        val shareMessage = DossierShareFormatter.fromDetail(
            listing,
            DossierShareOptions(
                customNote = note,
                includePublicPageLink = true,
                publicPageUrl = listing.publicShare?.shareUrl.orEmpty(),
            ),
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isLinking = true) }
            when (val result = crmRepository.sendListing(
                contactId,
                SendListingRequest(
                    token = listing.token,
                    title = listing.title.orEmpty(),
                    price = listing.price?.toString().orEmpty(),
                    area = listing.area?.toString().orEmpty(),
                    link = listing.shareLink.orEmpty(),
                    note = note,
                    shareMessage = shareMessage,
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLinking = false,
                            showSendDialog = false,
                            pendingContactId = null,
                            sendNote = "",
                            successMessage = "فایل به مخاطب ارسال شد",
                            pendingWhatsAppShare = if (shareViaWhatsApp) shareMessage else null,
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLinking = false, error = result.message)
                }
            }
        }
    }

    fun clearPendingWhatsAppShare() = _uiState.update { it.copy(pendingWhatsAppShare = null) }

    fun clearPendingCreatedProperty() = _uiState.update { it.copy(pendingCreatedPropertyId = null) }

    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }

    fun showMessage(message: String) = _uiState.update { it.copy(successMessage = message) }

    fun toggleFavorite() {
        val listing = _uiState.value.listing ?: return
        if (_uiState.value.isTogglingFavorite) return
        val current = listing.meta?.isFavorite == true
        _uiState.update {
            it.copy(
                isTogglingFavorite = true,
                listing = listing.copy(
                    meta = (listing.meta ?: ListingMetaDto()).copy(isFavorite = !current),
                ),
            )
        }
        viewModelScope.launch {
            when (val result = filingRepository.toggleListingFavorite(token)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        listing = result.data,
                        isTogglingFavorite = false,
                        successMessage = if (result.data.meta?.isFavorite == true) "به علاقه‌مندی‌ها اضافه شد" else "از علاقه‌مندی‌ها حذف شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        listing = listing,
                        isTogglingFavorite = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    fun openNotesSheet() {
        val meta = _uiState.value.listing?.meta
        _uiState.update {
            it.copy(
                showNotesSheet = true,
                noteDraft = meta?.note.orEmpty(),
                tagDraft = meta?.tag.orEmpty(),
            )
        }
    }

    fun dismissNotesSheet() = _uiState.update { it.copy(showNotesSheet = false) }
    fun onNoteDraftChange(value: String) = _uiState.update { it.copy(noteDraft = value) }
    fun onTagDraftChange(value: String) = _uiState.update { it.copy(tagDraft = value) }

    fun saveNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingMeta = true, error = null) }
            when (
                val result = filingRepository.updateListingMeta(
                    token,
                    ListingMetaUpdateRequest(
                        note = _uiState.value.noteDraft,
                        tag = _uiState.value.tagDraft,
                    ),
                )
            ) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        listing = result.data,
                        isSavingMeta = false,
                        showNotesSheet = false,
                        successMessage = "یادداشت ذخیره شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingMeta = false, error = result.message)
                }
            }
        }
    }

    fun unlinkContact(linkId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUnlinkingContact = true, error = null) }
            when (val result = filingRepository.unlinkListingContact(token, linkId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        listing = result.data,
                        isUnlinkingContact = false,
                        successMessage = "پیوند مخاطب حذف شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isUnlinkingContact = false, error = result.message)
                }
            }
        }
    }

    fun summarizeListing() {
        if (_uiState.value.isSummarizing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true, error = null) }
            when (val result = extrasRepository.aiSummarizeListing(AiSummarizeListingRequest(listingToken = token))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSummarizing = false,
                        aiSummary = result.data.text,
                        aiIsFallback = result.data.isFallback,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSummarizing = false, error = result.message)
                }
            }
        }
    }

    fun saveAsPersonalProperty() {
        val listing = _uiState.value.listing ?: return
        val dealMode = when {
            listing.rent != null || listing.deposit != null -> "رهن و اجاره"
            else -> "فروش"
        }
        val buildYear = listing.yearBuilt
            ?.filter { it.isDigit() }
            ?.take(4)
            ?.toIntOrNull()
        val images = buildList {
            listing.thumbnailUrl?.takeIf { it.isNotBlank() }?.let { add(it) }
            addAll(listing.images.filter { it.isNotBlank() })
        }.distinct()
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProperty = true) }
            when (val result = dealsRepository.createProperty(
                PropertyCreateRequest(
                    title = listing.title.orEmpty().ifBlank { "فایل شخصی" },
                    dealMode = dealMode,
                    city = listing.city.orEmpty(),
                    district = listing.district.orEmpty(),
                    salePrice = listing.price,
                    deposit = listing.deposit,
                    rent = listing.rent,
                    area = listing.area?.toDouble(),
                    rooms = listing.rooms?.toString().orEmpty(),
                    floor = listing.floor?.filter { it.isDigit() }?.toIntOrNull(),
                    buildYear = buildYear,
                    hasParking = listing.hasParking,
                    hasStorage = listing.hasStorage,
                    hasElevator = listing.hasElevator,
                    images = images,
                    token = listing.token,
                    link = listing.shareLink.orEmpty(),
                    ownerPhone = listing.ownerPhone.orEmpty(),
                    ownerName = listing.ownerName.orEmpty(),
                ),
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSavingProperty = false,
                        pendingCreatedPropertyId = result.data.id,
                        successMessage = "به فایل‌های شخصی اضافه شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingProperty = false, error = result.message)
                }
            }
        }
    }

    fun requestDelete() {
        _uiState.update { it.copy(showDeleteDialog = true, error = null) }
    }

    fun dismissDeleteDialog() {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteListing(onDeleted: () -> Unit) {
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            when (val result = filingRepository.deleteListing(token)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                    onDeleted()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isDeleting = false,
                        showDeleteDialog = false,
                        error = result.message,
                    )
                }
            }
        }
    }
}

private fun Boolean?.toTriPayload(): String = when (this) {
    true -> "1"
    false -> "0"
    null -> ""
}
