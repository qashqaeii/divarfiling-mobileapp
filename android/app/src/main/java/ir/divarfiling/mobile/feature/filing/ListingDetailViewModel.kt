package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.SendListingRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListingDetailUiState(
    val listing: ListingDetailDto? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLinking: Boolean = false,
    val showContactPicker: Boolean = false,
    val showSendDialog: Boolean = false,
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
                    it.copy(listing = result.data, isLoading = false, isRefreshing = false)
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
        val shareMessage = listOfNotNull(
            listing.title?.takeIf { it.isNotBlank() },
            listing.shareLink?.takeIf { it.isNotBlank() },
            note.takeIf { it.isNotBlank() },
        ).joinToString("\n")
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
}
