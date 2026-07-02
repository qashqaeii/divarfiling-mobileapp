package ir.divarfiling.mobile.feature.crm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ActivityDto
import ir.divarfiling.mobile.core.network.ContactDetailData
import ir.divarfiling.mobile.core.network.ContactMatchesData
import ir.divarfiling.mobile.core.network.ContactUpdateRequest
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.LinkListingRequest
import ir.divarfiling.mobile.core.design.ListingMessageFormatter
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.network.PropertyMatchDto
import ir.divarfiling.mobile.core.network.SendListingRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.FilingRepository
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ir.divarfiling.mobile.feature.crm.components.ContactEditMoneyState
import ir.divarfiling.mobile.feature.crm.components.ContactEditPrefsState
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
    val showEditSheet: Boolean = false,
    val showActivitySheet: Boolean = false,
    val noteText: String = "",
    val reminderTitle: String = "",
    val reminderNote: String = "",
    val reminderDueMillis: Long = System.currentTimeMillis() + 3_600_000L,
    val editName: String = "",
    val editPhone: String = "",
    val editStatus: String = "",
    val editCustomerType: String = "",
    val editPriority: String = "",
    val editMoney: ContactEditMoneyState = ContactEditMoneyState(),
    val editPrefs: ContactEditPrefsState = ContactEditPrefsState(),
    val editNotes: String = "",
    val activityContent: String = "",
    val selectedActivityType: String = "پیگیری",
    val showSendFilingSheet: Boolean = false,
    val sendListingNote: String = "",
    val filingPickerStep: Int = 0,
    val filingDatasets: List<DatasetDto> = emptyList(),
    val filingListings: List<ListingDto> = emptyList(),
    val selectedDatasetId: String? = null,
    val isFilingLoading: Boolean = false,
    val pendingWhatsAppShare: String? = null,
    val showMatchesSheet: Boolean = false,
    val matchesData: ContactMatchesData? = null,
    val matchesLoading: Boolean = false,
)

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
    private val filingRepository: FilingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contactId: Long = savedStateHandle.get<Long>("contactId") ?: 0L
    private val openMatchesOnLoad: Boolean = savedStateHandle.get<Boolean>("openMatches") ?: false
    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

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
                is ApiResult.Success -> {
                    val contact = result.data.contact
                    _uiState.update {
                        it.copy(
                            data = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            editName = contact.fullName,
                            editPhone = contact.phone.orEmpty(),
                            editStatus = contact.status.orEmpty(),
                            editCustomerType = contact.customerType.orEmpty(),
                            editPriority = contact.priority.orEmpty(),
                            editMoney = ContactEditMoneyState(
                                budgetMin = contact.budgetMin?.toString().orEmpty(),
                                budgetMax = contact.budgetMax?.toString().orEmpty(),
                                depositMin = contact.depositMin?.toString().orEmpty(),
                                depositMax = contact.depositMax?.toString().orEmpty(),
                                rentMin = contact.rentMin?.toString().orEmpty(),
                                rentMax = contact.rentMax?.toString().orEmpty(),
                            ),
                            editPrefs = ContactEditPrefsState(
                                propertyType = contact.propertyType.orEmpty(),
                                rooms = contact.rooms.orEmpty(),
                                minArea = contact.minArea?.toString().orEmpty(),
                                maxArea = contact.maxArea?.toString().orEmpty(),
                                areas = contact.areas.orEmpty(),
                            ),
                            editNotes = contact.notes.orEmpty(),
                        )
                    }
                    if (openMatchesOnLoad || _uiState.value.showMatchesSheet) {
                        loadMatches(openSheet = openMatchesOnLoad)
                    }
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

    fun logActivity(type: String, content: String = "", title: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = crmRepository.createActivity(contactId, type, content, title)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "فعالیت ثبت شد",
                            showActivitySheet = false,
                            activityContent = "",
                        )
                    }
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
                        it.copy(
                            isSubmitting = false,
                            showNoteDialog = false,
                            noteText = "",
                            successMessage = "یادداشت ثبت شد",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun submitReminder() {
        val title = _uiState.value.reminderTitle.trim()
        if (title.isBlank()) return
        val dueAt = millisToIso(_uiState.value.reminderDueMillis)
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (crmRepository.createReminder(contactId, title, dueAt, _uiState.value.reminderNote)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showReminderDialog = false,
                            reminderTitle = "",
                            reminderNote = "",
                            successMessage = "یادآور ثبت شد",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = it.error) }
            }
        }
    }

    fun completeReminder(reminderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (crmRepository.completeTodayTask(reminderId = reminderId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "یادآور انجام شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = it.error) }
            }
        }
    }

    fun postponeReminder(reminderId: Long, days: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (crmRepository.postponeTodayTask(reminderId = reminderId, days = days)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "یادآور تعویق شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = it.error) }
            }
        }
    }

    fun saveEdit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val state = _uiState.value
            val money = state.editMoney
            val prefs = state.editPrefs
            when (crmRepository.updateContact(
                contactId,
                ContactUpdateRequest(
                    fullName = state.editName.trim(),
                    phone = state.editPhone.trim(),
                    status = state.editStatus.ifBlank { null },
                    customerType = state.editCustomerType.ifBlank { null },
                    priority = state.editPriority.ifBlank { null },
                    notes = state.editNotes,
                    budgetMin = parseMoneyInput(money.budgetMin),
                    budgetMax = parseMoneyInput(money.budgetMax),
                    depositMin = parseMoneyInput(money.depositMin),
                    depositMax = parseMoneyInput(money.depositMax),
                    rentMin = parseMoneyInput(money.rentMin),
                    rentMax = parseMoneyInput(money.rentMax),
                    propertyType = prefs.propertyType.ifBlank { "" },
                    rooms = prefs.rooms.ifBlank { "" },
                    minArea = parseMoneyInput(prefs.minArea)?.toInt(),
                    maxArea = parseMoneyInput(prefs.maxArea)?.toInt(),
                    areas = prefs.areas.ifBlank { "" },
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, showEditSheet = false, successMessage = "ذخیره شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = it.error) }
            }
        }
    }

    fun changeStatus(newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (crmRepository.updateContact(contactId, ContactUpdateRequest(status = newStatus))) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "وضعیت به‌روز شد", editStatus = newStatus)
                    }
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

    fun toggleSendFilingSheet(show: Boolean) {
        _uiState.update {
            it.copy(
                showSendFilingSheet = show,
                filingPickerStep = 0,
                selectedDatasetId = null,
                filingListings = emptyList(),
                sendListingNote = if (show) it.sendListingNote else "",
            )
        }
        if (show) loadFilingDatasets()
    }

    fun loadFilingDatasets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFilingLoading = true) }
            when (val result = filingRepository.getDatasets(pageSize = 50)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(filingDatasets = result.data.items, isFilingLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isFilingLoading = false, error = result.message)
                }
            }
        }
    }

    fun selectFilingDataset(datasetId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedDatasetId = datasetId,
                    filingPickerStep = 1,
                    isFilingLoading = true,
                    filingListings = emptyList(),
                )
            }
            when (val result = filingRepository.getListings(datasetId, pageSize = 50)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(filingListings = result.data.items, isFilingLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isFilingLoading = false, error = result.message)
                }
            }
        }
    }

    fun backToFilingDatasets() {
        _uiState.update {
            it.copy(filingPickerStep = 0, selectedDatasetId = null, filingListings = emptyList())
        }
    }

    fun sendListingFromFiling(listing: ListingDto, shareViaWhatsApp: Boolean = false) {
        val note = _uiState.value.sendListingNote.trim()
        val priceText = listing.price?.toString().orEmpty()
        val areaText = listing.area?.toString().orEmpty()
        val shareMessage = ListingMessageFormatter.fromListing(listing, note)
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = crmRepository.sendListing(
                contactId,
                SendListingRequest(
                    token = listing.token,
                    title = listing.title.orEmpty(),
                    price = priceText,
                    area = areaText,
                    link = listing.shareLink.orEmpty(),
                    note = note,
                    shareMessage = shareMessage,
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showSendFilingSheet = false,
                            sendListingNote = "",
                            successMessage = "فایل به مخاطب ارسال شد",
                            pendingWhatsAppShare = if (shareViaWhatsApp) shareMessage else null,
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun toggleMatchesSheet(show: Boolean) {
        _uiState.update { it.copy(showMatchesSheet = show) }
        if (show && _uiState.value.matchesData == null) {
            loadMatches(openSheet = true)
        }
    }

    fun loadMatches(openSheet: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    matchesLoading = true,
                    showMatchesSheet = openSheet || it.showMatchesSheet,
                    error = null,
                )
            }
            when (val result = crmRepository.getContactMatches(contactId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(matchesData = result.data, matchesLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(matchesLoading = false, error = result.message)
                }
            }
        }
    }

    fun suggestMatches(matches: List<PropertyMatchDto>, shareViaWhatsApp: Boolean = false) {
        if (matches.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = crmRepository.suggestContactMatches(contactId, matches)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showMatchesSheet = false,
                            successMessage = "${result.data.suggestedCount} ملک پیشنهاد شد",
                            pendingWhatsAppShare = if (shareViaWhatsApp) {
                                result.data.whatsappText
                            } else {
                                null
                            },
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun uploadDocument(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = crmRepository.uploadDocument(contactId, uri)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "مدرک آپلود شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun deleteDocument(documentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = crmRepository.deleteDocument(contactId, documentId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "مدرک حذف شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    private fun buildShareMessage(title: String?, link: String?, note: String): String {
        return listOfNotNull(
            title?.takeIf { it.isNotBlank() },
            link?.takeIf { it.isNotBlank() },
            note.takeIf { it.isNotBlank() },
        ).joinToString("\n")
    }

    fun onSendListingNoteChange(v: String) = _uiState.update { it.copy(sendListingNote = v) }
    fun clearPendingWhatsAppShare() = _uiState.update { it.copy(pendingWhatsAppShare = null) }
    fun toggleNoteDialog(show: Boolean) = _uiState.update { it.copy(showNoteDialog = show) }
    fun toggleReminderDialog(show: Boolean) = _uiState.update { it.copy(showReminderDialog = show) }
    fun toggleEditSheet(show: Boolean) = _uiState.update { it.copy(showEditSheet = show) }
    fun toggleActivitySheet(show: Boolean) = _uiState.update { it.copy(showActivitySheet = show) }
    fun onNoteChange(v: String) = _uiState.update { it.copy(noteText = v) }
    fun onReminderTitleChange(v: String) = _uiState.update { it.copy(reminderTitle = v) }
    fun onReminderNoteChange(v: String) = _uiState.update { it.copy(reminderNote = v) }
    fun onReminderDueChange(millis: Long) = _uiState.update { it.copy(reminderDueMillis = millis) }
    fun onEditNameChange(v: String) = _uiState.update { it.copy(editName = v) }
    fun onEditPhoneChange(v: String) = _uiState.update { it.copy(editPhone = v) }
    fun onEditStatusChange(v: String) = _uiState.update { it.copy(editStatus = v) }
    fun onEditCustomerTypeChange(v: String) = _uiState.update { it.copy(editCustomerType = v) }
    fun onEditPriorityChange(v: String) = _uiState.update { it.copy(editPriority = v) }
    fun onEditBudgetMinChange(v: String) = _uiState.update { it.copy(editMoney = it.editMoney.copy(budgetMin = v)) }
    fun onEditBudgetMaxChange(v: String) = _uiState.update { it.copy(editMoney = it.editMoney.copy(budgetMax = v)) }
    fun onEditDepositMinChange(v: String) = _uiState.update { it.copy(editMoney = it.editMoney.copy(depositMin = v)) }
    fun onEditDepositMaxChange(v: String) = _uiState.update { it.copy(editMoney = it.editMoney.copy(depositMax = v)) }
    fun onEditRentMinChange(v: String) = _uiState.update { it.copy(editMoney = it.editMoney.copy(rentMin = v)) }
    fun onEditRentMaxChange(v: String) = _uiState.update { it.copy(editMoney = it.editMoney.copy(rentMax = v)) }
    fun onEditPropertyTypeChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(propertyType = v)) }
    fun onEditRoomsChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(rooms = v)) }
    fun onEditMinAreaChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(minArea = v)) }
    fun onEditMaxAreaChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(maxArea = v)) }
    fun onEditAreasChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(areas = v)) }
    fun onEditNotesChange(v: String) = _uiState.update { it.copy(editNotes = v) }
    fun onActivityContentChange(v: String) = _uiState.update { it.copy(activityContent = v) }
    fun onActivityTypeChange(v: String) = _uiState.update { it.copy(selectedActivityType = v) }
    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }

    private fun parseMoneyInput(raw: String): Long? {
        val digits = raw.filter { it.isDigit() }
        return digits.toLongOrNull()
    }

    private fun millisToIso(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(isoFormatter)
    }
}
