package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.ListingMessageFormatter
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.ListingUpdateRequest
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.SendListingRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.DealsRepository
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListingEditForm(
    val title: String = "",
    val price: String = "",
    val deposit: String = "",
    val rent: String = "",
    val area: String = "",
    val rooms: String = "",
    val floor: String = "",
    val buildYear: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val description: String = "",
    val ownerPhone: String = "",
)

data class ListingDetailUiState(
    val listing: ListingDetailDto? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLinking: Boolean = false,
    val isSavingProperty: Boolean = false,
    val isSavingPhone: Boolean = false,
    val isSavingEdit: Boolean = false,
    val ownerPhoneDraft: String = "",
    val showContactPicker: Boolean = false,
    val showSendDialog: Boolean = false,
    val showEditSheet: Boolean = false,
    val editForm: ListingEditForm = ListingEditForm(),
    val sendNote: String = "",
    val pendingContactId: Long? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val pendingWhatsAppShare: String? = null,
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
    private val crmRepository: CrmRepository,
    private val dealsRepository: DealsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val token: String = savedStateHandle.get<String>("token") ?: ""
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
                    it.copy(
                        listing = result.data,
                        ownerPhoneDraft = result.data.ownerPhone.orEmpty(),
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

    fun onOwnerPhoneChange(phone: String) = _uiState.update { it.copy(ownerPhoneDraft = phone) }

    fun saveOwnerPhone() {
        val phone = _uiState.value.ownerPhoneDraft.trim()
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPhone = true) }
            when (val result = filingRepository.updateListing(token, ListingUpdateRequest(ownerPhone = phone))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        listing = result.data,
                        ownerPhoneDraft = result.data.ownerPhone.orEmpty(),
                        isSavingPhone = false,
                        successMessage = "شماره تماس ذخیره شد",
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
                    buildYear = listing.yearBuilt.orEmpty(),
                    neighborhood = listing.district.orEmpty(),
                    city = listing.city.orEmpty(),
                    description = listing.description.orEmpty(),
                    ownerPhone = listing.ownerPhone.orEmpty(),
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
                rooms = form.rooms.trim().ifBlank { null },
                floor = form.floor.trim().ifBlank { null },
                buildYear = form.buildYear.trim().ifBlank { null },
                neighborhood = form.neighborhood.trim().ifBlank { null },
                city = form.city.trim().ifBlank { null },
                description = form.description.trim().ifBlank { null },
                ownerPhone = form.ownerPhone.trim().ifBlank { null },
            )
            when (val result = filingRepository.updateListing(token, request)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        listing = result.data,
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

    fun dismissSendDialog() {
        _uiState.update { it.copy(showSendDialog = false, pendingContactId = null, sendNote = "") }
    }

    fun sendToContact(shareViaWhatsApp: Boolean = false) {
        val listing = _uiState.value.listing ?: return
        val contactId = _uiState.value.pendingContactId ?: return
        val note = _uiState.value.sendNote.trim()
        val shareMessage = ListingMessageFormatter.fromDetail(listing, note)
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
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isLinking = false,
                        showSendDialog = false,
                        pendingContactId = null,
                        sendNote = "",
                        successMessage = "فایل به مخاطب ارسال شد",
                        pendingWhatsAppShare = if (shareViaWhatsApp) shareMessage else null,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLinking = false, error = result.message)
                }
            }
        }
    }

    fun clearPendingWhatsAppShare() = _uiState.update { it.copy(pendingWhatsAppShare = null) }

    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }

    fun showMessage(message: String) = _uiState.update { it.copy(successMessage = message) }

    fun saveAsPersonalProperty() {
        val listing = _uiState.value.listing ?: return
        val dealMode = when {
            listing.rent != null || listing.deposit != null -> "رهن و اجاره"
            else -> "فروش"
        }
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
                    token = listing.token,
                    link = listing.shareLink.orEmpty(),
                ),
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSavingProperty = false, successMessage = "به فایل‌های شخصی اضافه شد")
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingProperty = false, error = result.message)
                }
            }
        }
    }
}
