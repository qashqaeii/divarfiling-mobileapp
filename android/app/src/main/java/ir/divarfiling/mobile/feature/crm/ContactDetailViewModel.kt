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
import ir.divarfiling.mobile.core.network.MessageTemplateDto
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.ListingMessageFormatter
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.network.PropertyMatchDto
import ir.divarfiling.mobile.core.network.SendListingRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.FilingRepository
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ir.divarfiling.mobile.feature.crm.components.ContactEditBuilderState
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
    val editingReminderId: Long? = null,
    val showEditSheet: Boolean = false,
    val showActivitySheet: Boolean = false,
    val noteText: String = "",
    val reminderTitle: String = "",
    val reminderNote: String = "",
    val reminderDueMillis: Long = System.currentTimeMillis() + 3_600_000L,
    val reminderRecurrence: String = "",
    val editName: String = "",
    val editPhone: String = "",
    val editStatus: String = "",
    val editCustomerType: String = "",
    val editPriority: String = "",
    val editMoney: ContactEditMoneyState = ContactEditMoneyState(),
    val editPrefs: ContactEditPrefsState = ContactEditPrefsState(),
    val editBuilder: ContactEditBuilderState = ContactEditBuilderState(),
    val editNotes: String = "",
    val showDiscardEditDialog: Boolean = false,
    val activityContent: String = "",
    val selectedActivityType: String = "پیگیری",
    val selectedActivityStatus: String = "در حال پیگیری",
    val showSendFilingSheet: Boolean = false,
    val sendListingNote: String = "",
    val filingPickerStep: Int = 0,
    val filingDatasets: List<DatasetDto> = emptyList(),
    val filingListings: List<ListingDto> = emptyList(),
    val selectedDatasetId: String? = null,
    val isFilingLoading: Boolean = false,
    val messageTemplates: List<MessageTemplateDto> = emptyList(),
    val templatesLoading: Boolean = false,
    val showTemplatePicker: Boolean = false,
    val pendingWhatsAppShare: String? = null,
    val showMatchesSheet: Boolean = false,
    val matchesData: ContactMatchesData? = null,
    val matchesLoading: Boolean = false,
    val matchSuggestNote: String = "",
    val showMatchTemplatePicker: Boolean = false,
    val showTeamAssignSheet: Boolean = false,
    val teamAssignMode: String = "assign",
    val teamMembers: List<ir.divarfiling.mobile.core.network.TeamMemberDto> = emptyList(),
    val selectedTeamMemberId: Long? = null,
    val teamTransferNote: String = "",
    val teamMembersLoading: Boolean = false,
)

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
    private val filingRepository: FilingRepository,
    private val extrasRepository: WorkspaceExtrasRepository,
    private val teamRepository: ir.divarfiling.mobile.data.repository.TeamRepository,
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
                                propertyType = contact.propertyType.orEmpty().ifBlank {
                                    if (contact.customerType == "سازنده") "آپارتمان" else ""
                                },
                                rooms = contact.rooms.orEmpty(),
                                minArea = contact.minArea?.toString().orEmpty(),
                                maxArea = contact.maxArea?.toString().orEmpty(),
                                areas = contact.areas.orEmpty(),
                                city = contact.city.orEmpty(),
                                yearMin = contact.yearMin?.toString().orEmpty(),
                                yearMax = contact.yearMax?.toString().orEmpty(),
                                floorMin = contact.floorMin?.toString().orEmpty(),
                                floorMax = contact.floorMax?.toString().orEmpty(),
                                wantParking = contact.wantParking,
                                wantStorage = contact.wantStorage,
                                wantElevator = contact.wantElevator,
                            ),
                            editBuilder = ContactEditBuilderState(
                                buyBudgetMin = contact.builderBuyBudgetMin?.toString().orEmpty(),
                                buyBudgetMax = contact.builderBuyBudgetMax?.toString().orEmpty(),
                                buyMinArea = contact.builderBuyMinArea?.toString().orEmpty(),
                                buyMaxArea = contact.builderBuyMaxArea?.toString().orEmpty(),
                                buyAreas = contact.builderBuyAreas.orEmpty(),
                                buyPropertyTypes = contact.builderBuyPropertyTypes.orEmpty()
                                    .ifBlank { "ویلا, کلنگی, زمین" },
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
                    val statusToApply = _uiState.value.selectedActivityStatus.takeIf { it.isNotBlank() }
                    val statusResult = if (statusToApply != null) {
                        crmRepository.updateContact(
                            contactId,
                            ContactUpdateRequest(status = statusToApply),
                        )
                    } else {
                        ApiResult.Success(_uiState.value.data?.contact)
                    }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = when (statusResult) {
                                is ApiResult.Success ->
                                    if (statusToApply != null) "فعالیت و وضعیت مخاطب ثبت شد" else "فعالیت ثبت شد"
                                is ApiResult.Error -> "فعالیت ثبت شد، ولی وضعیت مخاطب به‌روزرسانی نشد"
                            },
                            showActivitySheet = false,
                            activityContent = "",
                            selectedActivityStatus = defaultStatusForActivityType(it.selectedActivityType),
                        )
                    }
                    if (statusResult is ApiResult.Success && statusToApply != null) {
                        maybeOpenSmartMatchAfterStatus(statusToApply)
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
        val state = _uiState.value
        val title = state.reminderTitle.trim()
        if (title.isBlank()) return
        val dueAt = millisToIso(state.reminderDueMillis)
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val editingReminderId = state.editingReminderId
            val result = if (editingReminderId == null) {
                crmRepository.createReminder(
                    contactId,
                    title,
                    dueAt,
                    state.reminderNote,
                    state.reminderRecurrence,
                )
            } else {
                crmRepository.patchReminder(
                    editingReminderId,
                    ir.divarfiling.mobile.core.network.ReminderPatchRequest(
                        title = title,
                        note = state.reminderNote,
                        dueAt = dueAt,
                        recurrence = state.reminderRecurrence,
                    ),
                )
            }
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showReminderDialog = false,
                            editingReminderId = null,
                            reminderTitle = "",
                            reminderNote = "",
                            reminderRecurrence = "",
                            successMessage = if (editingReminderId == null) "یادآور ثبت شد" else "یادآور ویرایش شد",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = it.error) }
            }
        }
    }

    fun openReminderEditor(reminder: ir.divarfiling.mobile.core.network.ReminderDto) {
        _uiState.update {
            it.copy(
                showReminderDialog = true,
                editingReminderId = reminder.id,
                reminderTitle = reminder.title.orEmpty(),
                reminderNote = reminder.note.orEmpty(),
                reminderRecurrence = reminder.recurrence,
                reminderDueMillis = reminder.dueAt?.let(::isoToMillis) ?: it.reminderDueMillis,
            )
        }
    }

    fun deleteReminder(reminderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = crmRepository.deleteReminder(reminderId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showReminderDialog = false,
                            editingReminderId = null,
                            reminderTitle = "",
                            reminderNote = "",
                            reminderRecurrence = "",
                            successMessage = "یادآور حذف شد",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
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
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val state = _uiState.value
            val money = state.editMoney
            val prefs = state.editPrefs
            val builder = state.editBuilder
            when (val result = crmRepository.updateContact(
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
                    propertyType = prefs.propertyType.ifBlank {
                        if (state.editCustomerType == "سازنده") "آپارتمان" else ""
                    },
                    rooms = prefs.rooms.ifBlank { "" },
                    minArea = parseMoneyInput(prefs.minArea)?.toInt(),
                    maxArea = parseMoneyInput(prefs.maxArea)?.toInt(),
                    areas = prefs.areas.ifBlank { "" },
                    builderBuyBudgetMin = parseMoneyInput(builder.buyBudgetMin),
                    builderBuyBudgetMax = parseMoneyInput(builder.buyBudgetMax),
                    builderBuyMinArea = parseMoneyInput(builder.buyMinArea)?.toInt(),
                    builderBuyMaxArea = parseMoneyInput(builder.buyMaxArea)?.toInt(),
                    builderBuyAreas = builder.buyAreas.ifBlank { "" },
                    builderBuyPropertyTypes = builder.buyPropertyTypes.ifBlank { "" },
                    city = prefs.city.ifBlank { "" },
                    yearMin = parseMoneyInput(prefs.yearMin)?.toInt(),
                    yearMax = parseMoneyInput(prefs.yearMax)?.toInt(),
                    floorMin = parseMoneyInput(prefs.floorMin)?.toInt(),
                    floorMax = parseMoneyInput(prefs.floorMax)?.toInt(),
                    wantParking = prefs.wantParking,
                    wantStorage = prefs.wantStorage,
                    wantElevator = prefs.wantElevator,
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, showEditSheet = false, successMessage = "ذخیره شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
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
                    maybeOpenSmartMatchAfterStatus(newStatus)
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
                showTemplatePicker = false,
            )
        }
        if (show) {
            loadFilingDatasets()
            if (_uiState.value.messageTemplates.isEmpty()) loadMessageTemplates()
        }
    }

    fun loadMessageTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(templatesLoading = true) }
            when (val result = extrasRepository.getMessageTemplates()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(messageTemplates = result.data, templatesLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(templatesLoading = false, error = result.message)
                }
            }
        }
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
        _uiState.update {
            it.copy(
                showMatchesSheet = show,
                matchSuggestNote = if (show) it.matchSuggestNote else "",
                showMatchTemplatePicker = false,
            )
        }
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
        val note = _uiState.value.matchSuggestNote.trim().ifBlank { null }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = crmRepository.suggestContactMatches(contactId, matches, note)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showMatchesSheet = false,
                            matchSuggestNote = "",
                            showMatchTemplatePicker = false,
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
    fun toggleTemplatePicker(show: Boolean) = _uiState.update { it.copy(showTemplatePicker = show) }
    fun applyMessageTemplate(template: MessageTemplateDto) = _uiState.update {
        val current = it.sendListingNote.trim()
        val next = if (current.isBlank()) {
            template.body.trim()
        } else {
            "$current\n\n${template.body.trim()}"
        }
        it.copy(sendListingNote = next, showTemplatePicker = false)
    }
    fun onMatchSuggestNoteChange(v: String) = _uiState.update { it.copy(matchSuggestNote = v) }
    fun toggleMatchTemplatePicker(show: Boolean) = _uiState.update { it.copy(showMatchTemplatePicker = show) }
    fun applyMatchMessageTemplate(template: MessageTemplateDto) = _uiState.update {
        val current = it.matchSuggestNote.trim()
        val next = if (current.isBlank()) {
            template.body.trim()
        } else {
            "$current\n\n${template.body.trim()}"
        }
        it.copy(matchSuggestNote = next, showMatchTemplatePicker = false)
    }
    fun clearPendingWhatsAppShare() = _uiState.update { it.copy(pendingWhatsAppShare = null) }
    fun toggleNoteDialog(show: Boolean) = _uiState.update { it.copy(showNoteDialog = show) }
    fun toggleReminderDialog(show: Boolean) = _uiState.update {
        if (show) {
            it.copy(showReminderDialog = true, editingReminderId = null)
        } else {
            it.copy(
                showReminderDialog = false,
                editingReminderId = null,
                reminderTitle = "",
                reminderNote = "",
                reminderRecurrence = "",
            )
        }
    }
    fun toggleEditSheet(show: Boolean) {
        if (show) {
            _uiState.update { it.copy(showEditSheet = true, showDiscardEditDialog = false) }
        } else {
            requestDismissEdit()
        }
    }

    fun requestDismissEdit() {
        val state = _uiState.value
        if (state.showEditSheet && isContactEditDirty(state)) {
            _uiState.update { it.copy(showEditSheet = false, showDiscardEditDialog = true) }
        } else {
            _uiState.update { it.copy(showEditSheet = false, showDiscardEditDialog = false) }
        }
    }

    fun cancelDiscardEdit() {
        _uiState.update { it.copy(showDiscardEditDialog = false, showEditSheet = true) }
    }

    fun confirmDiscardEdit() {
        val contact = _uiState.value.data?.contact
        _uiState.update {
            if (contact == null) {
                it.copy(showDiscardEditDialog = false, showEditSheet = false)
            } else {
                it.copy(
                    showDiscardEditDialog = false,
                    showEditSheet = false,
                    editName = contact.fullName,
                    editPhone = contact.phone.orEmpty(),
                    editStatus = contact.status.orEmpty(),
                    editCustomerType = contact.customerType.orEmpty(),
                    editPriority = contact.priority.orEmpty(),
                    editNotes = contact.notes.orEmpty(),
                )
            }
        }
    }

    private fun isContactEditDirty(state: ContactDetailUiState): Boolean {
        val contact = state.data?.contact ?: return false
        val money = state.editMoney
        val prefs = state.editPrefs
        return state.editName != contact.fullName ||
            state.editPhone != contact.phone.orEmpty() ||
            state.editStatus != contact.status.orEmpty() ||
            state.editCustomerType != contact.customerType.orEmpty() ||
            state.editPriority != contact.priority.orEmpty() ||
            state.editNotes != contact.notes.orEmpty() ||
            money.budgetMin != contact.budgetMin?.toString().orEmpty() ||
            money.budgetMax != contact.budgetMax?.toString().orEmpty() ||
            money.depositMin != contact.depositMin?.toString().orEmpty() ||
            money.depositMax != contact.depositMax?.toString().orEmpty() ||
            money.rentMin != contact.rentMin?.toString().orEmpty() ||
            money.rentMax != contact.rentMax?.toString().orEmpty() ||
            prefs.propertyType != contact.propertyType.orEmpty().ifBlank {
                if (contact.customerType == "سازنده") "آپارتمان" else ""
            } ||
            prefs.rooms != contact.rooms.orEmpty() ||
            prefs.minArea != contact.minArea?.toString().orEmpty() ||
            prefs.maxArea != contact.maxArea?.toString().orEmpty() ||
            prefs.areas != contact.areas.orEmpty() ||
            prefs.city != contact.city.orEmpty() ||
            prefs.yearMin != contact.yearMin?.toString().orEmpty() ||
            prefs.yearMax != contact.yearMax?.toString().orEmpty() ||
            prefs.floorMin != contact.floorMin?.toString().orEmpty() ||
            prefs.floorMax != contact.floorMax?.toString().orEmpty() ||
            prefs.wantParking != contact.wantParking ||
            prefs.wantStorage != contact.wantStorage ||
            prefs.wantElevator != contact.wantElevator
    }

    fun openActivitySheet(type: String = "پیگیری", content: String = "") = _uiState.update {
        it.copy(
            showActivitySheet = true,
            selectedActivityType = type,
            activityContent = content,
            selectedActivityStatus = defaultStatusForActivityType(type),
        )
    }

    fun toggleActivitySheet(show: Boolean) = _uiState.update {
        if (show) {
            it.copy(
                showActivitySheet = true,
                selectedActivityStatus = defaultStatusForActivityType(it.selectedActivityType),
            )
        } else {
            it.copy(showActivitySheet = false, activityContent = "")
        }
    }
    fun onNoteChange(v: String) = _uiState.update { it.copy(noteText = v) }
    fun onReminderTitleChange(v: String) = _uiState.update { it.copy(reminderTitle = v) }
    fun onReminderNoteChange(v: String) = _uiState.update { it.copy(reminderNote = v) }
    fun onReminderDueChange(millis: Long) = _uiState.update { it.copy(reminderDueMillis = millis) }
    fun onReminderRecurrenceChange(v: String) = _uiState.update { it.copy(reminderRecurrence = v) }
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
    fun onEditCityChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(city = v)) }
    fun onEditYearMinChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(yearMin = v)) }
    fun onEditYearMaxChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(yearMax = v)) }
    fun onEditFloorMinChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(floorMin = v)) }
    fun onEditFloorMaxChange(v: String) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(floorMax = v)) }
    fun onEditWantParkingChange(v: Boolean) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(wantParking = v)) }
    fun onEditWantStorageChange(v: Boolean) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(wantStorage = v)) }
    fun onEditWantElevatorChange(v: Boolean) = _uiState.update { it.copy(editPrefs = it.editPrefs.copy(wantElevator = v)) }
    fun onEditBuilderBuyBudgetMinChange(v: String) = _uiState.update { it.copy(editBuilder = it.editBuilder.copy(buyBudgetMin = v)) }
    fun onEditBuilderBuyBudgetMaxChange(v: String) = _uiState.update { it.copy(editBuilder = it.editBuilder.copy(buyBudgetMax = v)) }
    fun onEditBuilderBuyMinAreaChange(v: String) = _uiState.update { it.copy(editBuilder = it.editBuilder.copy(buyMinArea = v)) }
    fun onEditBuilderBuyMaxAreaChange(v: String) = _uiState.update { it.copy(editBuilder = it.editBuilder.copy(buyMaxArea = v)) }
    fun onEditBuilderBuyAreasChange(v: String) = _uiState.update { it.copy(editBuilder = it.editBuilder.copy(buyAreas = v)) }
    fun onEditBuilderBuyTypesChange(v: String) = _uiState.update { it.copy(editBuilder = it.editBuilder.copy(buyPropertyTypes = v)) }
    fun onEditNotesChange(v: String) = _uiState.update { it.copy(editNotes = v) }
    fun onActivityContentChange(v: String) = _uiState.update { it.copy(activityContent = v) }
    fun onActivityTypeChange(v: String) = _uiState.update {
        it.copy(
            selectedActivityType = v,
            selectedActivityStatus = defaultStatusForActivityType(v),
        )
    }
    fun onActivityStatusChange(v: String) = _uiState.update { it.copy(selectedActivityStatus = v) }

    fun openTeamAssign(mode: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showTeamAssignSheet = true,
                    teamAssignMode = mode,
                    teamMembersLoading = true,
                    teamTransferNote = "",
                )
            }
            when (val result = teamRepository.getMembers(excludeSelf = true)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        teamMembers = result.data.members,
                        selectedTeamMemberId = result.data.members.firstOrNull()?.id,
                        teamMembersLoading = false,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        showTeamAssignSheet = false,
                        teamMembersLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    fun dismissTeamAssign() = _uiState.update { it.copy(showTeamAssignSheet = false) }
    fun onTeamMemberSelect(id: Long) = _uiState.update { it.copy(selectedTeamMemberId = id) }
    fun onTeamTransferNoteChange(value: String) = _uiState.update { it.copy(teamTransferNote = value) }

    fun submitTeamAssign() {
        val state = _uiState.value
        val memberId = state.selectedTeamMemberId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val result = if (state.teamAssignMode == "transfer") {
                teamRepository.transferContact(contactId, memberId, state.teamTransferNote.ifBlank { null })
            } else {
                teamRepository.assignContact(contactId, memberId)
            }
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showTeamAssignSheet = false,
                            successMessage = if (state.teamAssignMode == "transfer") {
                                "پرونده منتقل شد"
                            } else {
                                "مخاطب تخصیص داده شد"
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

    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }

    private fun maybeOpenSmartMatchAfterStatus(status: String) {
        val customerType = _uiState.value.data?.contact?.customerType
        if (!CrmConstants.isMatchEligible(customerType)) return
        if (!CrmConstants.shouldPromptSmartMatch(status)) return
        toggleMatchesSheet(true)
    }

    private fun parseMoneyInput(raw: String): Long? = FormatUtils.parseLocalizedLong(raw)

    private fun millisToIso(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(isoFormatter)
    }
}

private fun defaultStatusForActivityType(type: String): String = when (type) {
    "بازدید" -> "بازدید انجام شد"
    "تماس", "واتساپ", "پیامک", "پیگیری", "جلسه" -> "در حال پیگیری"
    else -> ""
}

private fun isoToMillis(value: String): Long? {
    return runCatching { java.time.OffsetDateTime.parse(value).toInstant().toEpochMilli() }
        .recoverCatching { Instant.parse(value).toEpochMilli() }
        .getOrNull()
}
