package ir.divarfiling.mobile.feature.crm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ActivityDto
import ir.divarfiling.mobile.core.network.ContactDetailData
import ir.divarfiling.mobile.core.network.ContactUpdateRequest
import ir.divarfiling.mobile.core.network.LinkListingRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactDetailUiState(
    val data: ContactDetailData? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showNoteDialog: Boolean = false,
    val showReminderDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val noteText: String = "",
    val reminderTitle: String = "",
    val reminderDueAt: String = "",
    val editName: String = "",
    val editPhone: String = "",
)

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L
    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    init {
        if (contactId > 0) load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.data == null && !it.isRefreshing,
                    error = null,
                )
            }
            when (val result = crmRepository.getContactDetail(contactId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        data = result.data,
                        isLoading = false,
                        isRefreshing = false,
                        editName = result.data.contact.fullName,
                        editPhone = result.data.contact.phone.orEmpty(),
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

    fun logActivity(type: String, content: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = crmRepository.createActivity(contactId, type, content)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "ثبت شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun submitNote() {
        val note = _uiState.value.noteText.trim()
        if (note.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (crmRepository.createNote(contactId, note)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, showNoteDialog = false, noteText = "", successMessage = "یادداشت ثبت شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun submitReminder() {
        val title = _uiState.value.reminderTitle.trim()
        val dueAt = _uiState.value.reminderDueAt.trim()
        if (title.isBlank() || dueAt.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (crmRepository.createReminder(contactId, title, dueAt)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showReminderDialog = false,
                            reminderTitle = "",
                            reminderDueAt = "",
                            successMessage = "یادآور ثبت شد",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun saveEdit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val state = _uiState.value
            when (crmRepository.updateContact(
                contactId,
                ContactUpdateRequest(fullName = state.editName, phone = state.editPhone),
            )) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, showEditDialog = false, successMessage = "ذخیره شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun linkListing(token: String, title: String, link: String) {
        viewModelScope.launch {
            when (crmRepository.linkListing(contactId, LinkListingRequest(token, title, link = link))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(successMessage = "آگهی لینک شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = it.error) }
            }
        }
    }

    fun toggleNoteDialog(show: Boolean) = _uiState.update { it.copy(showNoteDialog = show) }
    fun toggleReminderDialog(show: Boolean) = _uiState.update { it.copy(showReminderDialog = show) }
    fun toggleEditDialog(show: Boolean) = _uiState.update { it.copy(showEditDialog = show) }
    fun onNoteChange(v: String) = _uiState.update { it.copy(noteText = v) }
    fun onReminderTitleChange(v: String) = _uiState.update { it.copy(reminderTitle = v) }
    fun onReminderDueChange(v: String) = _uiState.update { it.copy(reminderDueAt = v) }
    fun onEditNameChange(v: String) = _uiState.update { it.copy(editName = v) }
    fun onEditPhoneChange(v: String) = _uiState.update { it.copy(editPhone = v) }
    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }
}
