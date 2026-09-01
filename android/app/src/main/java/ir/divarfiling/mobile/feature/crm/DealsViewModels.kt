package ir.divarfiling.mobile.feature.crm

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.DossierShareOptions
import ir.divarfiling.mobile.core.export.ExportFormat
import ir.divarfiling.mobile.core.export.ExportShareHelper
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.ContactSuggestionItemDto
import ir.divarfiling.mobile.core.network.ContactSuggestResponse
import ir.divarfiling.mobile.core.network.DealCreateRequest
import ir.divarfiling.mobile.core.network.DealFinanceDashboardData
import ir.divarfiling.mobile.core.network.DealFinanceDefaultsDto
import ir.divarfiling.mobile.core.network.DealFinanceSaveRequest
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealPipelineColumnDto
import ir.divarfiling.mobile.core.network.DealStageDefDto
import ir.divarfiling.mobile.core.network.DealStagePresetDto
import ir.divarfiling.mobile.core.network.DealStagesSaveRequest
import ir.divarfiling.mobile.core.network.DealUpdateRequest
import ir.divarfiling.mobile.feature.crm.components.suggestionMessage
import ir.divarfiling.mobile.core.network.PropertyContactMatchItemDto
import ir.divarfiling.mobile.core.network.PropertyContactMatchesData
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.PropertyCabinetCreateRequest
import ir.divarfiling.mobile.core.network.PropertyCabinetDto
import ir.divarfiling.mobile.core.network.PropertyCabinetUpdateRequest
import ir.divarfiling.mobile.core.network.PropertyFolderCreateRequest
import ir.divarfiling.mobile.core.network.PropertyFolderDto
import ir.divarfiling.mobile.core.network.PropertyFolderUpdateRequest
import ir.divarfiling.mobile.core.network.NearbyPoisPayloadDto
import ir.divarfiling.mobile.core.network.PropertyDetailData
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.PropertyLinkContactRequest
import ir.divarfiling.mobile.core.network.PropertyUpdateRequest
import ir.divarfiling.mobile.core.network.AiSummarizePropertyRequest
import ir.divarfiling.mobile.core.network.ListingPublicShareUpdateRequest
import ir.divarfiling.mobile.core.network.SavedFilterCreateRequest
import ir.divarfiling.mobile.core.network.SavedFilterDto
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.DashboardRepository
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.DealsRepository
import ir.divarfiling.mobile.data.repository.ExportRepository
import ir.divarfiling.mobile.data.repository.ExportSecurityRepository
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DealsUiState(
    val deals: List<DealDto> = emptyList(),
    val pipelineColumns: List<DealPipelineColumnDto> = emptyList(),
    val savedFilters: List<SavedFilterDto> = emptyList(),
    val activeSavedFilterId: Long? = null,
    val stages: List<String> = CrmConstants.DEAL_STAGES,
    val stageDefs: List<DealStageDefDto> = emptyList(),
    val stagePresets: List<DealStagePresetDto> = emptyList(),
    val stageCounts: Map<String, Int> = emptyMap(),
    val stageColors: List<String> = emptyList(),
    val stageIcons: List<String> = emptyList(),
    val selectedStage: String? = null,
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val page: Int = 1,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val showStagesEditor: Boolean = false,
    val editorDefs: List<DealStageDefDto> = emptyList(),
    val editorExpandedId: String? = null,
    val isSavingStages: Boolean = false,
    val createTitle: String = "",
    val createCustomerId: Long? = null,
    val createStage: String = "سرنخ",
    val createAmount: String = "",
    val createNotes: String = "",
    val createPropertyId: Long? = null,
    val createCommissionRate: String = "",
    val contactPicker: List<ContactDto> = emptyList(),
    val propertyPicker: List<PropertyDto> = emptyList(),
    val isSubmittingCreate: Boolean = false,
    val showSaveFilterDialog: Boolean = false,
    val saveFilterName: String = "",
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
)

@HiltViewModel
class DealsViewModel @Inject constructor(
    private val repository: DealsRepository,
    private val crmRepository: CrmRepository,
    private val extrasRepository: WorkspaceExtrasRepository,
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DealsUiState())
    val uiState: StateFlow<DealsUiState> = _uiState.asStateFlow()
    private var currentPage = 1

    init {
        viewModelScope.launch {
            sessionStore.currentUser.collect { user ->
                _uiState.update {
                    it.copy(userName = user?.fullName?.substringBefore(" ") ?: "کاربر")
                }
            }
        }
        viewModelScope.launch {
            when (val result = dashboardRepository.getDashboard()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(notificationBadgeCount = result.data.notificationsUnread) }
                is ApiResult.Error -> Unit
            }
        }
        loadSavedFilters()
        load()
    }

    fun loadSavedFilters() {
        viewModelScope.launch {
            when (val result = extrasRepository.getSavedFilters(entity = "deals", includeNewCount = true)) {
                is ApiResult.Success -> _uiState.update { it.copy(savedFilters = result.data) }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun load(refreshing: Boolean = false) {
        viewModelScope.launch {
            currentPage = 1
            _uiState.update {
                it.copy(isLoading = !refreshing && it.deals.isEmpty(), isRefreshing = refreshing, error = null)
            }
            repository.getStages().let { result ->
                if (result is ApiResult.Success) {
                    _uiState.update {
                        it.copy(
                            stages = result.data.stages.ifEmpty { it.stages },
                            stageDefs = result.data.definitions,
                            stagePresets = result.data.presets,
                            stageCounts = result.data.counts,
                            stageColors = result.data.colors,
                            stageIcons = result.data.icons,
                            createStage = result.data.stages.firstOrNull() ?: it.createStage,
                        )
                    }
                }
            }
            repository.getPipeline().let { result ->
                if (result is ApiResult.Success) {
                    _uiState.update { it.copy(pipelineColumns = result.data.columns) }
                }
            }
            when (val result = repository.getDeals(
                query = _uiState.value.query,
                stage = _uiState.value.selectedStage,
                page = 1,
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        deals = result.data.items,
                        page = 1,
                        hasMore = result.data.hasMore,
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, isLoadingMore = false, error = result.message)
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore || _uiState.value.isLoading) return
        currentPage += 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            when (val result = repository.getDeals(
                query = _uiState.value.query,
                stage = _uiState.value.selectedStage,
                page = currentPage,
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        deals = it.deals + result.data.items,
                        page = currentPage,
                        hasMore = result.data.hasMore,
                        isLoadingMore = false,
                    )
                }
                is ApiResult.Error -> {
                    currentPage -= 1
                    _uiState.update { it.copy(isLoadingMore = false, error = result.message) }
                }
            }
        }
    }

    fun refresh() {
        currentPage = 1
        load(refreshing = true)
        loadSavedFilters()
    }

    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v, activeSavedFilterId = null) }
    fun search() = load()
    fun selectStage(stage: String) {
        _uiState.update {
            it.copy(
                selectedStage = if (it.selectedStage == stage) null else stage,
                activeSavedFilterId = null,
            )
        }
        load()
    }

    fun clearStageFilter() {
        if (_uiState.value.selectedStage != null) {
            _uiState.update { it.copy(selectedStage = null, activeSavedFilterId = null) }
            load()
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun applySavedFilter(filter: SavedFilterDto) {
        val params = filter.resolvedParams
        _uiState.update {
            it.copy(
                query = params["q"].orEmpty(),
                selectedStage = params["stage"]?.ifBlank { null },
                activeSavedFilterId = filter.id,
            )
        }
        load()
    }

    fun openSaveFilterDialog() = _uiState.update { it.copy(showSaveFilterDialog = true, saveFilterName = "") }

    fun dismissSaveFilterDialog() = _uiState.update { it.copy(showSaveFilterDialog = false) }

    fun onSaveFilterNameChange(value: String) = _uiState.update { it.copy(saveFilterName = value) }

    fun saveCurrentFilter() {
        val state = _uiState.value
        val name = state.saveFilterName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "نام فیلتر الزامی است") }
            return
        }
        val params = buildMap {
            if (state.query.isNotBlank()) put("q", state.query.trim())
            state.selectedStage?.takeIf { it.isNotBlank() }?.let { put("stage", it) }
        }
        if (params.isEmpty()) {
            _uiState.update { it.copy(error = "برای ذخیره، حداقل یک فیلتر لازم است") }
            return
        }
        viewModelScope.launch {
            when (
                val result = extrasRepository.createSavedFilter(
                    SavedFilterCreateRequest(
                        name = name,
                        scope = "deals",
                        params = params,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showSaveFilterDialog = false,
                            activeSavedFilterId = result.data.id,
                            error = null,
                        )
                    }
                    loadSavedFilters()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun pinSavedFilter(id: Long) {
        viewModelScope.launch {
            when (extrasRepository.pinSavedFilter(id)) {
                is ApiResult.Success -> loadSavedFilters()
                is ApiResult.Error -> Unit
            }
        }
    }

    fun deleteSavedFilter(id: Long) {
        viewModelScope.launch {
            when (extrasRepository.deleteSavedFilter(id)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(activeSavedFilterId = it.activeSavedFilterId.takeUnless { active -> active == id })
                    }
                    loadSavedFilters()
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun toggleCreate(show: Boolean) {
        _uiState.update {
            it.copy(
                showCreateDialog = show,
                createTitle = if (show) it.createTitle else "",
                createAmount = if (show) it.createAmount else "",
                createNotes = if (show) it.createNotes else "",
                createCustomerId = if (show) it.createCustomerId else null,
                createPropertyId = if (show) it.createPropertyId else null,
                createCommissionRate = if (show) it.createCommissionRate else "",
                createStage = if (show) it.createStage else (it.stages.firstOrNull() ?: "سرنخ"),
            )
        }
        if (show) {
            loadContactsForPicker()
            loadPropertiesForPicker()
        }
    }

    fun onCreateTitleChange(v: String) = _uiState.update { it.copy(createTitle = v) }
    fun onCreateCustomerSelect(id: Long) = _uiState.update { it.copy(createCustomerId = id) }
    fun onCreatePropertySelect(id: Long?) = _uiState.update { it.copy(createPropertyId = id) }
    fun onCreateStageChange(v: String) = _uiState.update { it.copy(createStage = v) }
    fun onCreateAmountChange(v: String) = _uiState.update { it.copy(createAmount = v) }
    fun onCreateCommissionRateChange(v: String) = _uiState.update { it.copy(createCommissionRate = v) }
    fun onCreateNotesChange(v: String) = _uiState.update { it.copy(createNotes = v) }

    private fun loadContactsForPicker() {
        viewModelScope.launch {
            when (val result = crmRepository.getContacts(page = 1, pageSize = 100)) {
                is ApiResult.Success -> {
                    val items = result.data.items
                    _uiState.update {
                        it.copy(
                            contactPicker = items,
                            createCustomerId = it.createCustomerId ?: items.firstOrNull()?.id,
                        )
                    }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    private fun loadPropertiesForPicker() {
        viewModelScope.launch {
            when (val result = repository.getProperties(page = 1, pageSize = 100)) {
                is ApiResult.Success -> _uiState.update { it.copy(propertyPicker = result.data.items) }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun submitCreate() {
        val state = _uiState.value
        val customerId = state.createCustomerId
        val title = state.createTitle.trim()
        if (customerId == null) {
            _uiState.update { it.copy(error = "مخاطب را انتخاب کنید") }
            return
        }
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "عنوان معامله الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingCreate = true, error = null) }
            when (
                val result = repository.createDeal(
                    DealCreateRequest(
                        customerId = customerId,
                        title = title,
                        stage = state.createStage.ifBlank { "سرنخ" },
                        amount = state.createAmount.trim().toLongOrNull(),
                        propertyId = state.createPropertyId,
                        notes = state.createNotes.trim(),
                        commissionRate = state.createCommissionRate.trim().toDoubleOrNull(),
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showCreateDialog = false,
                            createTitle = "",
                            createCustomerId = null,
                            createAmount = "",
                            createNotes = "",
                            createPropertyId = null,
                            createCommissionRate = "",
                            createStage = "سرنخ",
                            isSubmittingCreate = false,
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingCreate = false, error = result.message)
                }
            }
        }
    }

    fun openStagesEditor() {
        val defs = _uiState.value.stageDefs.ifEmpty {
            _uiState.value.stages.mapIndexed { index, name ->
                DealStageDefDto(id = "stage-$index", name = name)
            }
        }
        _uiState.update {
            it.copy(showStagesEditor = true, editorDefs = defs, editorExpandedId = null)
        }
    }

    fun dismissStagesEditor() = _uiState.update { it.copy(showStagesEditor = false) }

    fun expandEditorStage(id: String?) = _uiState.update { it.copy(editorExpandedId = id) }

    private fun updateEditorDef(id: String, transform: (DealStageDefDto) -> DealStageDefDto) {
        _uiState.update { state ->
            state.copy(editorDefs = state.editorDefs.map { if (it.id == id) transform(it) else it })
        }
    }

    fun onEditorNameChange(id: String, name: String) = updateEditorDef(id) { it.copy(name = name) }
    fun onEditorDescriptionChange(id: String, value: String) = updateEditorDef(id) { it.copy(description = value) }
    fun onEditorProbabilityChange(id: String, value: Int) = updateEditorDef(id) { it.copy(probability = value.coerceIn(0, 100)) }
    fun onEditorRotDaysChange(id: String, value: Int) = updateEditorDef(id) { it.copy(rotDays = value.coerceIn(0, 90)) }
    fun onEditorColorChange(id: String, color: String) = updateEditorDef(id) { it.copy(color = color) }
    fun onEditorIconChange(id: String, icon: String) = updateEditorDef(id) { it.copy(icon = icon) }

    fun moveEditorStage(id: String, delta: Int) {
        _uiState.update { state ->
            val list = state.editorDefs.toMutableList()
            val idx = list.indexOfFirst { it.id == id }
            val target = idx + delta
            if (idx < 0 || target < 0 || target > list.lastIndex) return@update state
            if (list[idx].locked || list[target].locked) return@update state
            val item = list.removeAt(idx)
            list.add(target, item)
            state.copy(editorDefs = list)
        }
    }

    fun removeEditorStage(id: String) {
        _uiState.update { state ->
            val remaining = state.editorDefs.filter { it.id != id }
            val activeCount = remaining.count { !it.locked }
            if (activeCount < 1) return@update state
            state.copy(editorDefs = remaining, editorExpandedId = state.editorExpandedId.takeUnless { it == id })
        }
    }

    fun addEditorStage() {
        _uiState.update { state ->
            if (state.editorDefs.size >= 14) return@update state
            val insertAt = state.editorDefs.indexOfFirst { it.locked }.takeIf { it >= 0 } ?: state.editorDefs.size
            val color = state.stageColors.getOrNull(insertAt) ?: "#6366f1"
            val def = DealStageDefDto(
                id = "stage-new-${System.currentTimeMillis()}",
                name = "مرحله ${insertAt + 1}",
                color = color,
                bg = color,
                icon = "fa-handshake",
                probability = 50,
                rotDays = 5,
                kind = "active",
            )
            val list = state.editorDefs.toMutableList()
            list.add(insertAt, def)
            state.copy(editorDefs = list, editorExpandedId = def.id)
        }
    }

    fun applyEditorPreset(presetId: String) {
        val preset = _uiState.value.stagePresets.firstOrNull { it.id == presetId } ?: return
        _uiState.update {
            it.copy(editorDefs = preset.definitions, editorExpandedId = null)
        }
    }

    fun saveStages() {
        val defs = _uiState.value.editorDefs
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingStages = true, error = null) }
            when (val result = repository.saveStages(DealStagesSaveRequest(definitions = defs))) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingStages = false,
                            showStagesEditor = false,
                            stages = result.data.stages,
                            stageDefs = result.data.definitions,
                            stagePresets = result.data.presets,
                            stageCounts = result.data.counts,
                            error = null,
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingStages = false, error = result.message)
                }
            }
        }
    }

    fun resetStages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingStages = true, error = null) }
            when (val result = repository.saveStages(DealStagesSaveRequest(reset = true))) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingStages = false,
                            editorDefs = result.data.definitions,
                            stages = result.data.stages,
                            stageDefs = result.data.definitions,
                            editorExpandedId = null,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingStages = false, error = result.message)
                }
            }
        }
    }
}

data class DealDetailUiState(
    val deal: DealDto? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val stages: List<String> = CrmConstants.DEAL_STAGES,
    val stageDefs: List<DealStageDefDto> = emptyList(),
    val showEditSheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showLostReasonDialog: Boolean = false,
    val pendingStage: String? = null,
    val lostReasonInput: String = "",
    val editTitle: String = "",
    val editAmount: String = "",
    val editNotes: String = "",
    val editStage: String = "",
    val editCommissionRate: String = "",
    val editPropertyId: Long? = null,
    val propertyPicker: List<PropertyDto> = emptyList(),
    val showFinanceSheet: Boolean = false,
    val financeDealKind: String = "sale",
    val financeCommissionMode: String = "percent",
    val financeBuyerRate: String = "0.5",
    val financeSellerRate: String = "0.5",
    val financeBuyerAmount: String = "",
    val financeSellerAmount: String = "",
    val financeAdvisorPercent: String = "70",
    val financeAgencyPercent: String = "30",
    val financeCosts: String = "0",
    val financeReceived: String = "0",
    val financePayoutStatus: String = "unpaid",
    val financeNotes: String = "",
)

@HiltViewModel
class DealDetailViewModel @Inject constructor(
    private val repository: DealsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val dealId: Long = savedStateHandle.get<Long>("dealId") ?: 0L
    private val _uiState = MutableStateFlow(DealDetailUiState())
    val uiState: StateFlow<DealDetailUiState> = _uiState.asStateFlow()

    init {
        if (dealId > 0) load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.deal == null, error = null) }
            repository.getStages().let { r ->
                if (r is ApiResult.Success) {
                    _uiState.update {
                        it.copy(
                            stages = r.data.stages.ifEmpty { it.stages },
                            stageDefs = r.data.definitions,
                        )
                    }
                }
            }
            when (val result = repository.getDeal(dealId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            deal = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            editTitle = result.data.title,
                            editAmount = result.data.amount?.toString().orEmpty(),
                            editNotes = result.data.notes.orEmpty(),
                            editStage = result.data.stage.orEmpty(),
                            editCommissionRate = result.data.commissionRate?.toString().orEmpty(),
                            editPropertyId = result.data.propertyId,
                        )
                    }
                    hydrateFinance(result.data)
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

    fun requestStageChange(stage: String) {
        if (stage == "از دست رفته") {
            _uiState.update {
                it.copy(
                    showLostReasonDialog = true,
                    pendingStage = stage,
                    lostReasonInput = it.deal?.lostReason.orEmpty(),
                )
            }
        } else {
            applyStageChange(stage)
        }
    }

    fun onLostReasonChange(value: String) = _uiState.update { it.copy(lostReasonInput = value) }

    fun dismissLostReasonDialog() = _uiState.update {
        it.copy(showLostReasonDialog = false, pendingStage = null, lostReasonInput = "")
    }

    fun confirmLostReason() {
        val stage = _uiState.value.pendingStage ?: return
        applyStageChange(stage, _uiState.value.lostReasonInput.trim().ifBlank { null })
        _uiState.update { it.copy(showLostReasonDialog = false, pendingStage = null) }
    }

    private fun applyStageChange(stage: String, lostReason: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (repository.updateDealStage(dealId, stage, lostReason)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "مرحله به‌روز شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = it.error) }
            }
        }
    }

    fun changeStage(stage: String) = requestStageChange(stage)

    fun toggleChecklistItem(itemId: String, done: Boolean) {
        viewModelScope.launch {
            when (val result = repository.toggleDealChecklist(dealId, itemId, done)) {
                is ApiResult.Success -> _uiState.update { state ->
                    state.copy(
                        deal = state.deal?.copy(checklist = result.data.checklist),
                        successMessage = if (result.data.progress?.complete == true) {
                            "چک‌لیست تکمیل شد"
                        } else {
                            null
                        },
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun deleteDeal(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.deleteDeal(dealId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, showDeleteDialog = false) }
                    onDeleted()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message, showDeleteDialog = false)
                }
            }
        }
    }

    fun toggleDeleteDialog(show: Boolean) = _uiState.update { it.copy(showDeleteDialog = show) }

    fun saveEdit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val state = _uiState.value
            when (repository.updateDeal(
                dealId,
                DealUpdateRequest(
                    title = state.editTitle.trim(),
                    amount = state.editAmount.trim().toLongOrNull(),
                    notes = state.editNotes,
                    stage = state.editStage.takeIf { it.isNotBlank() },
                    commissionRate = state.editCommissionRate.trim().toDoubleOrNull(),
                    propertyId = state.editPropertyId,
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, showEditSheet = false, successMessage = "ذخیره شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = it.error) }
            }
        }
    }

    fun toggleEditSheet(show: Boolean) {
        _uiState.update { it.copy(showEditSheet = show) }
        if (show) loadPropertiesForPicker()
    }

    private fun loadPropertiesForPicker() {
        viewModelScope.launch {
            when (val result = repository.getProperties(page = 1, pageSize = 100)) {
                is ApiResult.Success -> _uiState.update { it.copy(propertyPicker = result.data.items) }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun onEditTitleChange(v: String) = _uiState.update { it.copy(editTitle = v) }
    fun onEditAmountChange(v: String) = _uiState.update { it.copy(editAmount = v) }
    fun onEditNotesChange(v: String) = _uiState.update { it.copy(editNotes = v) }
    fun onEditStageChange(v: String) = _uiState.update { it.copy(editStage = v) }
    fun onEditCommissionRateChange(v: String) = _uiState.update { it.copy(editCommissionRate = v) }
    fun onEditPropertySelect(id: Long?) = _uiState.update { it.copy(editPropertyId = id) }
    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }

    private fun hydrateFinance(deal: DealDto) {
        _uiState.update {
            it.copy(
                financeDealKind = deal.dealKind ?: "sale",
                financeCommissionMode = deal.commissionMode ?: "percent",
                financeBuyerRate = deal.buyerCommissionRate?.toString() ?: "0.5",
                financeSellerRate = deal.sellerCommissionRate?.toString() ?: "0.5",
                financeBuyerAmount = deal.buyerCommissionAmount?.toString().orEmpty(),
                financeSellerAmount = deal.sellerCommissionAmount?.toString().orEmpty(),
                financeAdvisorPercent = deal.advisorSharePercent?.toString() ?: "70",
                financeAgencyPercent = deal.agencySharePercent?.toString() ?: "30",
                financeCosts = (deal.dealCosts ?: 0L).toString(),
                financeReceived = (deal.receivedAmount ?: 0L).toString(),
                financePayoutStatus = deal.payoutStatus ?: "unpaid",
                financeNotes = deal.payoutNotes.orEmpty(),
            )
        }
    }

    fun toggleFinanceSheet(show: Boolean) = _uiState.update { it.copy(showFinanceSheet = show) }
    fun onFinanceDealKindChange(v: String) = _uiState.update { it.copy(financeDealKind = v) }
    fun onFinanceModeChange(v: String) = _uiState.update { it.copy(financeCommissionMode = v) }
    fun onFinanceBuyerRateChange(v: String) = _uiState.update { it.copy(financeBuyerRate = v) }
    fun onFinanceSellerRateChange(v: String) = _uiState.update { it.copy(financeSellerRate = v) }
    fun onFinanceBuyerAmountChange(v: String) = _uiState.update { it.copy(financeBuyerAmount = v) }
    fun onFinanceSellerAmountChange(v: String) = _uiState.update { it.copy(financeSellerAmount = v) }
    fun onFinanceAdvisorPercentChange(v: String) = _uiState.update { it.copy(financeAdvisorPercent = v) }
    fun onFinanceAgencyPercentChange(v: String) = _uiState.update { it.copy(financeAgencyPercent = v) }
    fun onFinanceCostsChange(v: String) = _uiState.update { it.copy(financeCosts = v) }
    fun onFinanceReceivedChange(v: String) = _uiState.update { it.copy(financeReceived = v) }
    fun onFinancePayoutChange(v: String) = _uiState.update { it.copy(financePayoutStatus = v) }
    fun onFinanceNotesChange(v: String) = _uiState.update { it.copy(financeNotes = v) }

    fun saveFinance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val state = _uiState.value
            when (val result = repository.updateDealFinance(
                    dealId,
                    DealFinanceSaveRequest(
                        dealKind = state.financeDealKind,
                        commissionMode = state.financeCommissionMode,
                        buyerCommissionRate = state.financeBuyerRate.toDoubleOrNull(),
                        sellerCommissionRate = state.financeSellerRate.toDoubleOrNull(),
                        buyerCommissionAmount = state.financeBuyerAmount.toLongOrNull(),
                        sellerCommissionAmount = state.financeSellerAmount.toLongOrNull(),
                        advisorSharePercent = state.financeAdvisorPercent.toDoubleOrNull(),
                        agencySharePercent = state.financeAgencyPercent.toDoubleOrNull(),
                        dealCosts = state.financeCosts.toLongOrNull(),
                        receivedAmount = state.financeReceived.toLongOrNull(),
                        payoutStatus = state.financePayoutStatus,
                        payoutNotes = state.financeNotes,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, showFinanceSheet = false, successMessage = "تنظیمات مالی ذخیره شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
            }
        }
    }
}

data class DealFinanceUiState(
    val dashboard: DealFinanceDashboardData? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showSettings: Boolean = false,
    val editDefaults: DealFinanceDefaultsDto = DealFinanceDefaultsDto(),
)

@HiltViewModel
class DealFinanceViewModel @Inject constructor(
    private val repository: DealsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DealFinanceUiState(isLoading = true))
    val uiState: StateFlow<DealFinanceUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load(period: String? = null) {
        viewModelScope.launch {
            val selected = period ?: _uiState.value.dashboard?.period ?: "month"
            _uiState.update { it.copy(isLoading = it.dashboard == null, error = null) }
            when (val result = repository.getFinanceDashboard(selected)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        dashboard = result.data,
                        editDefaults = result.data.defaults,
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

    fun selectPeriod(period: String) = load(period)

    fun toggleSettings(show: Boolean) {
        val defaults = _uiState.value.dashboard?.defaults ?: DealFinanceDefaultsDto()
        _uiState.update { it.copy(showSettings = show, editDefaults = defaults) }
    }

    fun onSaleBuyerChange(v: String) = updateDefaults { it.copy(saleBuyerRate = v.toDoubleOrNull() ?: it.saleBuyerRate) }
    fun onSaleSellerChange(v: String) = updateDefaults { it.copy(saleSellerRate = v.toDoubleOrNull() ?: it.saleSellerRate) }
    fun onRentBuyerChange(v: String) = updateDefaults { it.copy(rentBuyerRate = v.toDoubleOrNull() ?: it.rentBuyerRate) }
    fun onRentSellerChange(v: String) = updateDefaults { it.copy(rentSellerRate = v.toDoubleOrNull() ?: it.rentSellerRate) }
    fun onDefaultAdvisorChange(v: String) = updateDefaults { it.copy(advisorSharePercent = v.toDoubleOrNull() ?: it.advisorSharePercent) }
    fun onDefaultAgencyChange(v: String) = updateDefaults { it.copy(agencySharePercent = v.toDoubleOrNull() ?: it.agencySharePercent) }

    private fun updateDefaults(block: (DealFinanceDefaultsDto) -> DealFinanceDefaultsDto) {
        _uiState.update { it.copy(editDefaults = block(it.editDefaults)) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.saveFinanceSettings(_uiState.value.editDefaults)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        showSettings = false,
                        editDefaults = result.data.defaults,
                        successMessage = "پیش‌فرض کمیسیون ذخیره شد",
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }
}

data class PropertiesUiState(
    val properties: List<PropertyDto> = emptyList(),
    val propertyCabinets: List<PropertyCabinetDto> = emptyList(),
    val propertyFolders: List<PropertyFolderDto> = emptyList(),
    val selectedCabinetId: String? = null,
    val selectedFolderId: Long? = null,
    val unassignedFolderCount: Int = 0,
    val savedFilters: List<SavedFilterDto> = emptyList(),
    val activeSavedFilterId: Long? = null,
    val query: String = "",
    val transactionStatus: String? = null,
    val dealMode: String? = null,
    val propertyType: String? = null,
    val city: String? = null,
    val cityQuery: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val createForm: PropertyFormState = PropertyFormState(),
    val isSubmittingCreate: Boolean = false,
    val isUploadingImages: Boolean = false,
    val isExporting: Boolean = false,
    val showExportSheet: Boolean = false,
    val showExportSecuritySheet: Boolean = false,
    val pendingExportFormat: ExportFormat? = null,
    val showSaveFilterDialog: Boolean = false,
    val saveFilterName: String = "",
    val showCreateFolderDialog: Boolean = false,
    val showManageFoldersSheet: Boolean = false,
    val showFolderFormSheet: Boolean = false,
    val showDeleteFolderDialog: Boolean = false,
    val showCabinetFormSheet: Boolean = false,
    val showManageCabinetsSheet: Boolean = false,
    val showDeleteCabinetDialog: Boolean = false,
    val editingFolder: PropertyFolderDto? = null,
    val editingCabinet: PropertyCabinetDto? = null,
    val folderPendingDelete: PropertyFolderDto? = null,
    val cabinetPendingDelete: PropertyCabinetDto? = null,
    val manageFoldersOrder: List<PropertyFolderDto> = emptyList(),
    val manageCabinetsOrder: List<PropertyCabinetDto> = emptyList(),
    val createFolderName: String = "",
    val folderFormName: String = "",
    val folderFormDescription: String = "",
    val folderFormColor: String = PropertyFolderConstants.DEFAULT_COLOR,
    val folderFormIcon: String = PropertyFolderConstants.DEFAULT_ICON,
    val folderFormPinned: Boolean = false,
    val cabinetFormName: String = "",
    val cabinetFormDescription: String = "",
    val cabinetFormColor: String = PropertyCabinetConstants.DEFAULT_COLOR,
    val cabinetFormIcon: String = PropertyCabinetConstants.DEFAULT_ICON,
    val cabinetFormPinned: Boolean = false,
    val folderFormCabinetId: Long? = null,
    val isSubmittingFolder: Boolean = false,
    val isSubmittingCabinet: Boolean = false,
    val isSavingFolderOrder: Boolean = false,
    val isSavingCabinetOrder: Boolean = false,
    val propertiesTotal: Int = 0,
    val exportMessage: String? = null,
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
    val showDuplicateDialog: Boolean = false,
    val duplicateTargetId: Long? = null,
    val isDuplicating: Boolean = false,
    val duplicateNavigateId: Long? = null,
)

object PropertyConstants {
    val DEAL_MODES = listOf("فروش", "رهن و اجاره", "اجاره", "پیش‌فروش")
    val PROPERTY_TYPES = listOf("آپارتمان", "ویلا", "کلنگی", "اداری", "مغازه", "زمین", "سایر")
    val TX_STATUSES = listOf("فعال", "در مذاکره", "قرارداد", "فروخته‌شده", "اجاره‌رفته", "بایگانی")
    val PUBLISH_STATUSES = listOf("پیش‌نویس", "منتشرشده", "مخفی", "بایگانی")
    val BUSINESS_TYPES = listOf("شخصی", "مشاور املاک", "بنگاه")
}

object PropertyFolderConstants {
    const val DEFAULT_COLOR = "#6366f1"
    const val DEFAULT_ICON = "fa-folder"
    val COLORS = listOf(
        "#6366f1", "#0ea5e9", "#10b981", "#f59e0b",
        "#ef4444", "#8b5cf6", "#ec4899", "#14b8a6",
    )
    val ICONS = listOf(
        "fa-folder",
        "fa-folder-open",
        "fa-building",
        "fa-city",
        "fa-key",
        "fa-star",
        "fa-briefcase",
        "fa-layer-group",
        "fa-house-chimney",
        "fa-landmark",
        "fa-map-pin",
    )
}

object PropertyCabinetConstants {
    const val DEFAULT_COLOR = "#6366f1"
    const val DEFAULT_ICON = "fa-folder"
    val COLORS = PropertyFolderConstants.COLORS
    val ICONS = PropertyFolderConstants.ICONS
}

@HiltViewModel
class PropertiesViewModel @Inject constructor(
    private val repository: DealsRepository,
    private val exportRepository: ExportRepository,
    private val exportSecurityRepository: ExportSecurityRepository,
    private val extrasRepository: WorkspaceExtrasRepository,
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PropertiesUiState())
    val uiState: StateFlow<PropertiesUiState> = _uiState.asStateFlow()
    private var currentPage = 1

    init {
        viewModelScope.launch {
            sessionStore.currentUser.collect { user ->
                _uiState.update {
                    it.copy(userName = user?.fullName?.substringBefore(" ") ?: "کاربر")
                }
            }
        }
        viewModelScope.launch {
            when (val result = dashboardRepository.getDashboard()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(notificationBadgeCount = result.data.notificationsUnread) }
                is ApiResult.Error -> Unit
            }
        }
        loadSavedFilters()
        loadCabinets()
        loadFolders()
        load()
    }

    fun loadCabinets() {
        viewModelScope.launch {
            when (val result = extrasRepository.getPropertyCabinets()) {
                is ApiResult.Success -> {
                    val unassigned = extrasRepository.getPropertyFolders("none")
                    val unassignedCount = if (unassigned is ApiResult.Success) unassigned.data.size else 0
                    _uiState.update {
                        it.copy(
                            propertyCabinets = result.data,
                            unassignedFolderCount = unassignedCount,
                        )
                    }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun loadFolders() {
        viewModelScope.launch {
            val cabinetFilter = _uiState.value.selectedCabinetId
            when (val result = extrasRepository.getPropertyFolders(cabinetFilter)) {
                is ApiResult.Success -> _uiState.update { it.copy(propertyFolders = result.data) }
                is ApiResult.Error -> Unit
            }
        }
    }

    private fun folderIdParam(): Long? = _uiState.value.selectedFolderId

    private fun cabinetIdParam(): String? {
        if (_uiState.value.selectedFolderId != null) return null
        return _uiState.value.selectedCabinetId
    }

    fun loadSavedFilters() {
        viewModelScope.launch {
            when (val result = extrasRepository.getSavedFilters(entity = "properties", includeNewCount = true)) {
                is ApiResult.Success -> _uiState.update { it.copy(savedFilters = result.data) }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun load(refreshing: Boolean = false) {
        viewModelScope.launch {
            currentPage = 1
            _uiState.update {
                it.copy(isLoading = !refreshing && it.properties.isEmpty(), isRefreshing = refreshing)
            }
            when (val result = repository.getProperties(
                query = _uiState.value.query,
                dealMode = _uiState.value.dealMode,
                propertyType = _uiState.value.propertyType,
                city = _uiState.value.city,
                transactionStatus = _uiState.value.transactionStatus,
                folderId = folderIdParam(),
                cabinetId = cabinetIdParam(),
                page = if (refreshing) 1 else currentPage,
            )) {
                is ApiResult.Success -> {
                    val page = result.data.page
                    currentPage = page
                    _uiState.update {
                        val items = if (refreshing || page == 1) {
                            result.data.items
                        } else {
                            it.properties + result.data.items
                        }
                        it.copy(
                            properties = items,
                            propertiesTotal = result.data.total,
                            hasMore = result.data.hasMore,
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, isLoadingMore = false, error = result.message)
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore || _uiState.value.isLoading) return
        currentPage += 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            when (val result = repository.getProperties(
                query = _uiState.value.query,
                dealMode = _uiState.value.dealMode,
                propertyType = _uiState.value.propertyType,
                city = _uiState.value.city,
                transactionStatus = _uiState.value.transactionStatus,
                folderId = folderIdParam(),
                cabinetId = cabinetIdParam(),
                page = currentPage,
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        properties = it.properties + result.data.items,
                        hasMore = result.data.hasMore,
                        isLoadingMore = false,
                    )
                }
                is ApiResult.Error -> {
                    currentPage -= 1
                    _uiState.update { it.copy(isLoadingMore = false, error = result.message) }
                }
            }
        }
    }

    fun refresh() {
        currentPage = 1
        load(refreshing = true)
        loadSavedFilters()
        loadFolders()
    }
    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v, activeSavedFilterId = null) }
    fun onTransactionStatusChange(status: String?) = _uiState.update {
        it.copy(transactionStatus = status, activeSavedFilterId = null)
    }
    fun onDealModeChange(mode: String?) = _uiState.update { it.copy(dealMode = mode, activeSavedFilterId = null) }
    fun onPropertyTypeChange(type: String?) = _uiState.update { it.copy(propertyType = type, activeSavedFilterId = null) }
    fun onCityQueryChange(value: String) = _uiState.update {
        it.copy(cityQuery = value, city = value.trim().ifBlank { null }, activeSavedFilterId = null)
    }
    fun search() = load()
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                query = "",
                transactionStatus = null,
                dealMode = null,
                propertyType = null,
                city = null,
                cityQuery = "",
                activeSavedFilterId = null,
                selectedFolderId = null,
            )
        }
        load()
    }

    fun selectPropertyFolder(folder: PropertyFolderDto) {
        _uiState.update {
            it.copy(
                selectedFolderId = folder.id,
                selectedCabinetId = folder.cabinet?.id?.toString() ?: folder.cabinetId?.toString(),
                activeSavedFilterId = null,
            )
        }
        load()
    }

    fun clearPropertyFolderFilter() {
        _uiState.update { it.copy(selectedFolderId = null) }
        load()
    }

    fun selectPropertyCabinet(cabinet: PropertyCabinetDto) {
        _uiState.update {
            it.copy(
                selectedCabinetId = cabinet.id.toString(),
                selectedFolderId = null,
                activeSavedFilterId = null,
            )
        }
        loadFolders()
        load()
    }

    fun clearPropertyCabinetFilter() {
        _uiState.update { it.copy(selectedCabinetId = null, selectedFolderId = null) }
        loadFolders()
        load()
    }

    fun selectUnassignedCabinet() {
        _uiState.update {
            it.copy(selectedCabinetId = "none", selectedFolderId = null, activeSavedFilterId = null)
        }
        loadFolders()
        load()
    }

    fun openCreateFolderDialog() {
        _uiState.update {
            it.copy(
                showFolderFormSheet = true,
                editingFolder = null,
                folderFormName = "",
                folderFormDescription = "",
                folderFormColor = PropertyFolderConstants.DEFAULT_COLOR,
                folderFormIcon = PropertyFolderConstants.DEFAULT_ICON,
                folderFormPinned = false,
                folderFormCabinetId = _uiState.value.selectedCabinetId?.toLongOrNull(),
            )
        }
    }

    fun dismissCreateFolderDialog() = _uiState.update { it.copy(showCreateFolderDialog = false, createFolderName = "") }

    fun openManageFoldersSheet() {
        _uiState.update {
            it.copy(
                showManageFoldersSheet = true,
                manageFoldersOrder = it.propertyFolders,
            )
        }
    }

    fun dismissManageFoldersSheet() = _uiState.update { it.copy(showManageFoldersSheet = false) }

    fun openEditFolder(folder: PropertyFolderDto) {
        _uiState.update {
            it.copy(
                showManageFoldersSheet = false,
                showFolderFormSheet = true,
                editingFolder = folder,
                folderFormName = folder.name,
                folderFormDescription = folder.description,
                folderFormColor = folder.color,
                folderFormIcon = folder.icon.ifBlank { PropertyFolderConstants.DEFAULT_ICON },
                folderFormPinned = folder.isPinned,
                folderFormCabinetId = folder.cabinet?.id ?: folder.cabinetId,
            )
        }
    }

    fun dismissFolderFormSheet() = _uiState.update {
        it.copy(showFolderFormSheet = false, editingFolder = null)
    }

    fun onFolderFormNameChange(value: String) = _uiState.update { it.copy(folderFormName = value) }

    fun onFolderFormDescriptionChange(value: String) = _uiState.update { it.copy(folderFormDescription = value) }

    fun onFolderFormColorChange(value: String) = _uiState.update { it.copy(folderFormColor = value) }

    fun onFolderFormIconChange(value: String) = _uiState.update { it.copy(folderFormIcon = value) }

    fun onFolderFormPinnedChange(value: Boolean) = _uiState.update { it.copy(folderFormPinned = value) }

    fun onFolderFormCabinetChange(value: Long?) = _uiState.update { it.copy(folderFormCabinetId = value) }

    fun saveFolderForm() {
        val state = _uiState.value
        val name = state.folderFormName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "نام زونکن الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingFolder = true) }
            val editing = state.editingFolder
            val result = if (editing == null) {
                extrasRepository.createPropertyFolder(
                    PropertyFolderCreateRequest(
                        name = name,
                        description = state.folderFormDescription.trim(),
                        color = state.folderFormColor,
                        icon = state.folderFormIcon,
                        isPinned = state.folderFormPinned,
                        cabinetId = state.folderFormCabinetId,
                    ),
                )
            } else {
                extrasRepository.updatePropertyFolder(
                    editing.id,
                    PropertyFolderUpdateRequest(
                        name = name,
                        description = state.folderFormDescription.trim(),
                        color = state.folderFormColor,
                        icon = state.folderFormIcon,
                        isPinned = state.folderFormPinned,
                        cabinetId = state.folderFormCabinetId,
                    ),
                )
            }
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingFolder = false,
                            showFolderFormSheet = false,
                            editingFolder = null,
                            selectedFolderId = if (editing == null) result.data.id else it.selectedFolderId,
                            exportMessage = if (editing == null) "زونکن ساخته شد" else "زونکن به‌روز شد",
                        )
                    }
                    loadFolders()
                    loadCabinets()
                    if (editing == null) load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingFolder = false, error = result.message)
                }
            }
        }
    }

    fun requestDeleteFolder(folder: PropertyFolderDto) {
        _uiState.update {
            it.copy(
                folderPendingDelete = folder,
                showDeleteFolderDialog = true,
                showManageFoldersSheet = false,
                showFolderFormSheet = false,
            )
        }
    }

    fun dismissDeleteFolderDialog() = _uiState.update {
        it.copy(showDeleteFolderDialog = false, folderPendingDelete = null)
    }

    fun confirmDeleteFolder() {
        val folder = _uiState.value.folderPendingDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingFolder = true) }
            when (val deleteResult = extrasRepository.deletePropertyFolder(folder.id)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingFolder = false,
                            showDeleteFolderDialog = false,
                            folderPendingDelete = null,
                            selectedFolderId = it.selectedFolderId.takeUnless { active -> active == folder.id },
                            exportMessage = "زونکن حذف شد",
                        )
                    }
                    loadFolders()
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingFolder = false, error = deleteResult.message)
                }
            }
        }
    }

    fun toggleFolderPin(folder: PropertyFolderDto) {
        viewModelScope.launch {
            when (
                val pinResult = extrasRepository.updatePropertyFolder(
                    folder.id,
                    PropertyFolderUpdateRequest(isPinned = !folder.isPinned),
                )
            ) {
                is ApiResult.Success -> {
                    loadFolders()
                    _uiState.update {
                        it.copy(
                            manageFoldersOrder = it.manageFoldersOrder.map { row ->
                                if (row.id == folder.id) row.copy(isPinned = !folder.isPinned) else row
                            },
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = pinResult.message) }
            }
        }
    }

    fun moveManageFolderUp(folderId: Long) {
        _uiState.update { state ->
            val list = state.manageFoldersOrder.toMutableList()
            val idx = list.indexOfFirst { it.id == folderId }
            if (idx <= 0) return@update state
            val item = list.removeAt(idx)
            list.add(idx - 1, item)
            state.copy(manageFoldersOrder = list)
        }
    }

    fun moveManageFolderDown(folderId: Long) {
        _uiState.update { state ->
            val list = state.manageFoldersOrder.toMutableList()
            val idx = list.indexOfFirst { it.id == folderId }
            if (idx < 0 || idx >= list.lastIndex) return@update state
            val item = list.removeAt(idx)
            list.add(idx + 1, item)
            state.copy(manageFoldersOrder = list)
        }
    }

    fun saveManageFolderOrder() {
        val ids = _uiState.value.manageFoldersOrder.map { it.id }
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingFolderOrder = true) }
            when (val result = extrasRepository.reorderPropertyFolders(ids)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingFolderOrder = false,
                            showManageFoldersSheet = false,
                            propertyFolders = result.data,
                            exportMessage = "ترتیب زونکن‌ها ذخیره شد",
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingFolderOrder = false, error = result.message)
                }
            }
        }
    }

    fun openCreateCabinetDialog() {
        _uiState.update {
            it.copy(
                showCabinetFormSheet = true,
                editingCabinet = null,
                cabinetFormName = "",
                cabinetFormDescription = "",
                cabinetFormColor = PropertyCabinetConstants.DEFAULT_COLOR,
                cabinetFormIcon = PropertyCabinetConstants.DEFAULT_ICON,
                cabinetFormPinned = false,
            )
        }
    }

    fun openManageCabinetsSheet() {
        _uiState.update {
            it.copy(
                showManageCabinetsSheet = true,
                manageCabinetsOrder = it.propertyCabinets,
            )
        }
    }

    fun dismissManageCabinetsSheet() = _uiState.update { it.copy(showManageCabinetsSheet = false) }

    fun openEditCabinet(cabinet: PropertyCabinetDto) {
        _uiState.update {
            it.copy(
                showManageCabinetsSheet = false,
                showCabinetFormSheet = true,
                editingCabinet = cabinet,
                cabinetFormName = cabinet.name,
                cabinetFormDescription = cabinet.description,
                cabinetFormColor = cabinet.color,
                cabinetFormIcon = cabinet.icon.ifBlank { PropertyCabinetConstants.DEFAULT_ICON },
                cabinetFormPinned = cabinet.isPinned,
            )
        }
    }

    fun dismissCabinetFormSheet() = _uiState.update {
        it.copy(showCabinetFormSheet = false, editingCabinet = null)
    }

    fun onCabinetFormNameChange(value: String) = _uiState.update { it.copy(cabinetFormName = value) }

    fun onCabinetFormDescriptionChange(value: String) = _uiState.update { it.copy(cabinetFormDescription = value) }

    fun onCabinetFormColorChange(value: String) = _uiState.update { it.copy(cabinetFormColor = value) }

    fun onCabinetFormIconChange(value: String) = _uiState.update { it.copy(cabinetFormIcon = value) }

    fun onCabinetFormPinnedChange(value: Boolean) = _uiState.update { it.copy(cabinetFormPinned = value) }

    fun saveCabinetForm() {
        val state = _uiState.value
        val name = state.cabinetFormName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "نام کمد الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingCabinet = true) }
            val editing = state.editingCabinet
            val result = if (editing == null) {
                extrasRepository.createPropertyCabinet(
                    PropertyCabinetCreateRequest(
                        name = name,
                        description = state.cabinetFormDescription.trim(),
                        color = state.cabinetFormColor,
                        icon = state.cabinetFormIcon,
                        isPinned = state.cabinetFormPinned,
                    ),
                )
            } else {
                extrasRepository.updatePropertyCabinet(
                    editing.id,
                    PropertyCabinetUpdateRequest(
                        name = name,
                        description = state.cabinetFormDescription.trim(),
                        color = state.cabinetFormColor,
                        icon = state.cabinetFormIcon,
                        isPinned = state.cabinetFormPinned,
                    ),
                )
            }
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingCabinet = false,
                            showCabinetFormSheet = false,
                            editingCabinet = null,
                            selectedCabinetId = if (editing == null) result.data.id.toString() else it.selectedCabinetId,
                            selectedFolderId = if (editing == null) null else it.selectedFolderId,
                            exportMessage = if (editing == null) "کمد ساخته شد" else "کمد به‌روز شد",
                        )
                    }
                    loadCabinets()
                    loadFolders()
                    if (editing == null) load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingCabinet = false, error = result.message)
                }
            }
        }
    }

    fun requestDeleteCabinet(cabinet: PropertyCabinetDto) {
        _uiState.update {
            it.copy(
                cabinetPendingDelete = cabinet,
                showDeleteCabinetDialog = true,
                showManageCabinetsSheet = false,
                showCabinetFormSheet = false,
            )
        }
    }

    fun dismissDeleteCabinetDialog() = _uiState.update {
        it.copy(showDeleteCabinetDialog = false, cabinetPendingDelete = null)
    }

    fun confirmDeleteCabinet() {
        val cabinet = _uiState.value.cabinetPendingDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingCabinet = true) }
            when (val deleteResult = extrasRepository.deletePropertyCabinet(cabinet.id)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingCabinet = false,
                            showDeleteCabinetDialog = false,
                            cabinetPendingDelete = null,
                            selectedCabinetId = it.selectedCabinetId.takeUnless { active -> active == cabinet.id.toString() },
                            exportMessage = "کمد حذف شد",
                        )
                    }
                    loadCabinets()
                    loadFolders()
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingCabinet = false, error = deleteResult.message)
                }
            }
        }
    }

    fun toggleCabinetPin(cabinet: PropertyCabinetDto) {
        viewModelScope.launch {
            when (
                val pinResult = extrasRepository.updatePropertyCabinet(
                    cabinet.id,
                    PropertyCabinetUpdateRequest(isPinned = !cabinet.isPinned),
                )
            ) {
                is ApiResult.Success -> {
                    loadCabinets()
                    _uiState.update {
                        it.copy(
                            manageCabinetsOrder = it.manageCabinetsOrder.map { row ->
                                if (row.id == cabinet.id) row.copy(isPinned = !cabinet.isPinned) else row
                            },
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = pinResult.message) }
            }
        }
    }

    fun moveManageCabinetUp(cabinetId: Long) {
        _uiState.update { state ->
            val list = state.manageCabinetsOrder.toMutableList()
            val idx = list.indexOfFirst { it.id == cabinetId }
            if (idx <= 0) return@update state
            val item = list.removeAt(idx)
            list.add(idx - 1, item)
            state.copy(manageCabinetsOrder = list)
        }
    }

    fun moveManageCabinetDown(cabinetId: Long) {
        _uiState.update { state ->
            val list = state.manageCabinetsOrder.toMutableList()
            val idx = list.indexOfFirst { it.id == cabinetId }
            if (idx < 0 || idx >= list.lastIndex) return@update state
            val item = list.removeAt(idx)
            list.add(idx + 1, item)
            state.copy(manageCabinetsOrder = list)
        }
    }

    fun saveManageCabinetOrder() {
        val ids = _uiState.value.manageCabinetsOrder.map { it.id }
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingCabinetOrder = true) }
            when (val result = extrasRepository.reorderPropertyCabinets(ids)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingCabinetOrder = false,
                            showManageCabinetsSheet = false,
                            propertyCabinets = result.data,
                            exportMessage = "ترتیب کمدها ذخیره شد",
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingCabinetOrder = false, error = result.message)
                }
            }
        }
    }

    fun applySavedFilter(filter: SavedFilterDto) {
        val params = filter.resolvedParams
        _uiState.update {
            it.copy(
                query = params["q"].orEmpty(),
                transactionStatus = params["transaction_status"]?.ifBlank { null },
                dealMode = params["deal_mode"]?.ifBlank { null },
                propertyType = params["property_type"]?.ifBlank { null },
                city = params["city"]?.ifBlank { null },
                cityQuery = params["city"].orEmpty(),
                activeSavedFilterId = filter.id,
                selectedFolderId = params["folder_id"]?.toLongOrNull(),
            )
        }
        load()
    }

    fun openSaveFilterDialog() = _uiState.update { it.copy(showSaveFilterDialog = true, saveFilterName = "") }

    fun dismissSaveFilterDialog() = _uiState.update { it.copy(showSaveFilterDialog = false) }

    fun onSaveFilterNameChange(value: String) = _uiState.update { it.copy(saveFilterName = value) }

    fun saveCurrentFilter() {
        val state = _uiState.value
        val name = state.saveFilterName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "نام فیلتر الزامی است") }
            return
        }
        val params = buildMap {
            if (state.query.isNotBlank()) put("q", state.query.trim())
            state.transactionStatus?.takeIf { it.isNotBlank() }?.let { put("transaction_status", it) }
            state.dealMode?.takeIf { it.isNotBlank() }?.let { put("deal_mode", it) }
            state.propertyType?.takeIf { it.isNotBlank() }?.let { put("property_type", it) }
            state.city?.takeIf { it.isNotBlank() }?.let { put("city", it) }
            state.selectedFolderId?.let { put("folder_id", it.toString()) }
        }
        if (params.isEmpty()) {
            _uiState.update { it.copy(error = "برای ذخیره، حداقل یک فیلتر لازم است") }
            return
        }
        viewModelScope.launch {
            when (
                val result = extrasRepository.createSavedFilter(
                    SavedFilterCreateRequest(
                        name = name,
                        scope = "properties",
                        params = params,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showSaveFilterDialog = false,
                            activeSavedFilterId = result.data.id,
                            exportMessage = "فیلتر ذخیره شد",
                        )
                    }
                    loadSavedFilters()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun pinSavedFilter(id: Long) {
        viewModelScope.launch {
            when (extrasRepository.pinSavedFilter(id)) {
                is ApiResult.Success -> loadSavedFilters()
                is ApiResult.Error -> Unit
            }
        }
    }

    fun deleteSavedFilter(id: Long) {
        viewModelScope.launch {
            when (extrasRepository.deleteSavedFilter(id)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(activeSavedFilterId = it.activeSavedFilterId.takeUnless { active -> active == id })
                    }
                    loadSavedFilters()
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun toggleCreate(show: Boolean) {
        _uiState.update {
            it.copy(
                showCreateDialog = show,
                createForm = if (!show) PropertyFormState() else it.createForm,
            )
        }
    }

    fun onCreateFormChange(transform: (PropertyFormState) -> PropertyFormState) =
        _uiState.update { state -> state.copy(createForm = transform(state.createForm)) }

    fun onCreateFormReplace(form: PropertyFormState) = _uiState.update { it.copy(createForm = form) }

    fun onCreateGalleryImagesSelected(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uiState.update { state ->
            val form = state.createForm
            val remaining = 12 - form.images.size - form.pendingImageUris.size
            if (remaining <= 0) return@update state
            state.copy(
                createForm = form.copy(
                    pendingImageUris = form.pendingImageUris + uris.take(remaining).map { it.toString() },
                ),
            )
        }
    }

    fun submitCreate() {
        val form = _uiState.value.createForm
        if (form.title.trim().isBlank()) {
            _uiState.update { it.copy(error = "عنوان ملک الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingCreate = true, error = null) }
            when (val result = repository.createProperty(PropertyFormMappers.toCreateRequest(form))) {
                is ApiResult.Success -> {
                    val pending = form.pendingImageUris
                    if (pending.isNotEmpty()) {
                        _uiState.update { it.copy(isUploadingImages = true) }
                        val uploadError = uploadPendingPropertyImages(result.data.id, pending)
                        _uiState.update { it.copy(isUploadingImages = false) }
                        if (uploadError != null) {
                            _uiState.update { it.copy(error = uploadError) }
                        }
                    }
                    toggleCreate(false)
                    _uiState.update { it.copy(isSubmittingCreate = false) }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingCreate = false, error = result.message)
                }
            }
        }
    }

    private suspend fun uploadPendingPropertyImages(propertyId: Long, uris: List<String>): String? {
        var lastError: String? = null
        for (uriStr in uris) {
            when (val result = repository.uploadPropertyImage(propertyId, Uri.parse(uriStr))) {
                is ApiResult.Success -> Unit
                is ApiResult.Error -> lastError = result.message
            }
        }
        return lastError
    }

    fun createFromListing(
        title: String,
        city: String?,
        district: String?,
        dealMode: String,
        salePrice: Long?,
        deposit: Long?,
        rent: Long?,
        area: Double?,
        token: String,
        link: String?,
    ) {
        viewModelScope.launch {
            when (
                val result = repository.createProperty(
                    PropertyCreateRequest(
                        title = title,
                        dealMode = dealMode,
                        city = city.orEmpty(),
                        district = district.orEmpty(),
                        salePrice = salePrice,
                        deposit = deposit,
                        rent = rent,
                        area = area,
                        token = token,
                        link = link.orEmpty(),
                    ),
                )
            ) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    fun openExportSheet() = _uiState.update { it.copy(showExportSheet = true) }

    fun dismissExportSheet() = _uiState.update { it.copy(showExportSheet = false) }

    fun clearExportMessage() = _uiState.update { it.copy(exportMessage = null) }

    fun exportProperties(context: Context, format: ExportFormat) {
        if (!exportSecurityRepository.hasValidToken()) {
            _uiState.update {
                it.copy(
                    pendingExportFormat = format,
                    showExportSecuritySheet = true,
                    showExportSheet = false,
                )
            }
            return
        }
        performExportProperties(context, format)
    }

    fun dismissExportSecuritySheet() = _uiState.update {
        it.copy(showExportSecuritySheet = false, pendingExportFormat = null)
    }

    fun onExportSecurityCompleted(context: Context) {
        val format = _uiState.value.pendingExportFormat ?: return
        _uiState.update { it.copy(showExportSecuritySheet = false, pendingExportFormat = null) }
        performExportProperties(context, format)
    }

    private fun performExportProperties(context: Context, format: ExportFormat) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            when (val result = exportRepository.exportProperties(
                context = context,
                format = format,
                query = state.query,
                dealMode = state.dealMode,
                propertyType = state.propertyType,
                transactionStatus = state.transactionStatus,
                city = state.city,
            )) {
                is ApiResult.Success -> {
                    ExportShareHelper.shareFile(context, result.data, format.mimeType, "خروجی فایل‌های شخصی")
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            showExportSheet = false,
                            exportMessage = "فایل ${format.label} آماده شد",
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isExporting = false, error = result.message)
                }
            }
        }
    }

    fun requestDuplicateProperty(propertyId: Long) {
        _uiState.update { it.copy(showDuplicateDialog = true, duplicateTargetId = propertyId) }
    }

    fun dismissDuplicateDialog() {
        _uiState.update { it.copy(showDuplicateDialog = false, duplicateTargetId = null) }
    }

    fun confirmDuplicateProperty() {
        val id = _uiState.value.duplicateTargetId ?: return
        if (_uiState.value.isDuplicating) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDuplicating = true, error = null) }
            when (val result = repository.duplicateProperty(id)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isDuplicating = false,
                            showDuplicateDialog = false,
                            duplicateTargetId = null,
                            duplicateNavigateId = result.data.id,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isDuplicating = false, error = result.message)
                }
            }
        }
    }

    fun consumeDuplicateNavigate() {
        _uiState.update { it.copy(duplicateNavigateId = null) }
    }
}

data class PropertyDetailUiState(
    val detail: PropertyDetailData? = null,
    val selectedTab: PropertyDetailTab = PropertyDetailTab.OVERVIEW,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUploadingImages: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showEditSheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showLinkContactSheet: Boolean = false,
    val showLinkContactPicker: Boolean = false,
    val linkContactId: String = "",
    val linkContactName: String = "",
    val linkContactPhone: String = "",
    val linkContactRole: String = "مالک",
    val editForm: PropertyFormState = PropertyFormState(),
    val editFormBaseline: PropertyFormState = PropertyFormState(),
    val showDiscardEditDialog: Boolean = false,
    val inlineNotes: String = "",
    val showShareSheet: Boolean = false,
    val showPublicShareSettingsSheet: Boolean = false,
    val showContactMatchesSheet: Boolean = false,
    val contactMatchesData: PropertyContactMatchesData? = null,
    val contactMatchesLoading: Boolean = false,
    val contactSuggestionResult: ContactSuggestResponse? = null,
    val pendingSocialShare: PendingContactSocialShare? = null,
    val shareNote: String = "",
    val shareIncludeLink: Boolean = false,
    val shareIncludeAddress: Boolean = false,
    val shareIncludeNotes: Boolean = false,
    val shareIncludeAmenities: Boolean = true,
    val shareIncludePublicPage: Boolean = true,
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
    val showLocationMapPicker: Boolean = false,
    val nearbyPoisPayload: NearbyPoisPayloadDto? = null,
    val nearbyPoisLoading: Boolean = false,
    val aiSummary: String = "",
    val isSummarizing: Boolean = false,
    val aiIsFallback: Boolean = false,
    val aiModelLabel: String = "",
    val aiSummaryHighlights: List<String> = emptyList(),
    val aiNegotiationTip: String = "",
    val showDuplicateDialog: Boolean = false,
    val isDuplicating: Boolean = false,
    val duplicateNavigateId: Long? = null,
)

enum class PropertyDetailTab(val label: String) {
    OVERVIEW("پرونده"),
    CONTACTS("مخاطبین"),
    ACTIVITY("فعالیت‌ها"),
    SPECS("مشخصات"),
    NOTES("یادداشت"),
    DOCS("مدارک"),
}

@HiltViewModel
class PropertyDetailViewModel @Inject constructor(
    private val repository: DealsRepository,
    private val extrasRepository: WorkspaceExtrasRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val propertyId: Long = savedStateHandle.get<Long>("propertyId") ?: 0L
    private val openEditOnLoad: Boolean = savedStateHandle.get<Boolean>("openEdit") ?: false
    private var openEditHandled = false
    private val _uiState = MutableStateFlow(PropertyDetailUiState())
    val uiState: StateFlow<PropertyDetailUiState> = _uiState.asStateFlow()

    init { if (propertyId > 0) load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.detail == null) }
            when (val result = repository.getProperty(propertyId)) {
                is ApiResult.Success -> {
                    val p = result.data.property
                    val publicShare = result.data.publicShare
                    val form = PropertyFormState.fromProperty(p)
                    _uiState.update {
                        it.copy(
                            detail = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            inlineNotes = p.notes.orEmpty(),
                            editForm = form,
                            editFormBaseline = form,
                            nearbyPoisPayload = result.data.nearbyPoisCtx?.initialPayload,
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
                        )
                    }
                    if (openEditOnLoad && !openEditHandled && result.data.canEdit) {
                        openEditHandled = true
                        val form = PropertyFormState.fromProperty(p)
                        _uiState.update {
                            it.copy(showEditSheet = true, editForm = form, editFormBaseline = form)
                        }
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    fun selectTab(tab: PropertyDetailTab) = _uiState.update { it.copy(selectedTab = tab) }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    fun changeStatus(status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = repository.updatePropertyStatus(propertyId, status)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "وضعیت به‌روز شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun toggleLocationMapPicker(show: Boolean) = _uiState.update { it.copy(showLocationMapPicker = show) }

    fun updatePropertyLocation(latitude: Double, longitude: Double) {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, showLocationMapPicker = false) }
            when (val result = repository.updatePropertyLocation(propertyId, latitude, longitude)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            nearbyPoisPayload = null,
                            successMessage = "موقعیت ذخیره شد",
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

    fun fetchNearbyPois(refresh: Boolean = false) {
        if (_uiState.value.nearbyPoisLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(nearbyPoisLoading = true) }
            when (val result = repository.fetchPropertyNearbyPois(propertyId, refresh)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        nearbyPoisLoading = false,
                        nearbyPoisPayload = result.data,
                        successMessage = if (result.data.cached) "امکانات اطراف بارگذاری شد" else "امکانات اطراف ذخیره شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(nearbyPoisLoading = false, error = result.message)
                }
            }
        }
    }

    fun summarizeProperty() {
        if (_uiState.value.isSummarizing || propertyId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true, error = null) }
            when (val result = extrasRepository.aiSummarizeProperty(AiSummarizePropertyRequest(propertyId))) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSummarizing = false,
                        aiSummary = result.data.text,
                        aiIsFallback = result.data.isFallback,
                        aiModelLabel = result.data.modelLabel.orEmpty(),
                        aiSummaryHighlights = result.data.summaryDetail?.highlights.orEmpty(),
                        aiNegotiationTip = result.data.summaryDetail?.negotiationTip.orEmpty(),
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSummarizing = false, error = result.message)
                }
            }
        }
    }

    fun togglePropertyFolder(folderId: Long, add: Boolean) {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = extrasRepository.togglePropertyFolderMembership(folderId, propertyId, add)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = if (add) "به زونکن اضافه شد" else "از زونکن حذف شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun saveEdit() {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val form = _uiState.value.editForm
            when (val result = repository.updateProperty(propertyId, PropertyFormMappers.toUpdateRequest(form))) {
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

    fun deleteProperty(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.deleteProperty(propertyId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, showDeleteDialog = false) }
                    onDeleted()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message, showDeleteDialog = false)
                }
            }
        }
    }

    fun toggleDuplicateDialog(show: Boolean) {
        _uiState.update { it.copy(showDuplicateDialog = show) }
    }

    fun confirmDuplicateProperty() {
        if (_uiState.value.isDuplicating) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDuplicating = true, error = null) }
            when (val result = repository.duplicateProperty(propertyId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isDuplicating = false,
                            showDuplicateDialog = false,
                            duplicateNavigateId = result.data.id,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isDuplicating = false, error = result.message)
                }
            }
        }
    }

    fun consumeDuplicateNavigate() {
        _uiState.update { it.copy(duplicateNavigateId = null) }
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
        if (state.showEditSheet && isPropertyEditDirty(state)) {
            _uiState.update { it.copy(showEditSheet = false, showDiscardEditDialog = true) }
        } else {
            _uiState.update { it.copy(showEditSheet = false, showDiscardEditDialog = false) }
        }
    }

    fun cancelDiscardEdit() {
        _uiState.update { it.copy(showDiscardEditDialog = false, showEditSheet = true) }
    }

    fun confirmDiscardEdit() {
        _uiState.update { it.copy(showDiscardEditDialog = false, showEditSheet = false) }
        load()
    }

    private fun isPropertyEditDirty(state: PropertyDetailUiState): Boolean =
        state.editForm != state.editFormBaseline

    fun onEditFormChange(transform: (PropertyFormState) -> PropertyFormState) =
        _uiState.update { it.copy(editForm = transform(it.editForm)) }

    fun onEditFormReplace(form: PropertyFormState) = _uiState.update { it.copy(editForm = form) }

    fun onEditGalleryImagesSelected(uris: List<Uri>) {
        if (uris.isEmpty() || propertyId <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImages = true, error = null) }
            var images = _uiState.value.editForm.images.toMutableList()
            var lastError: String? = null
            val remaining = 12 - images.size
            for (uri in uris.take(remaining.coerceAtLeast(0))) {
                when (val result = repository.uploadPropertyImage(propertyId, uri)) {
                    is ApiResult.Success -> images = result.data.images.toMutableList()
                    is ApiResult.Error -> lastError = result.message
                }
            }
            _uiState.update {
                it.copy(
                    isUploadingImages = false,
                    editForm = it.editForm.copy(images = images),
                    error = lastError,
                    successMessage = if (lastError == null && uris.isNotEmpty()) "تصاویر آپلود شد" else it.successMessage,
                )
            }
        }
    }

    fun toggleDeleteDialog(show: Boolean) = _uiState.update { it.copy(showDeleteDialog = show) }
    fun toggleShareSheet(show: Boolean) = _uiState.update { it.copy(showShareSheet = show) }
    fun togglePublicShareSettingsSheet(show: Boolean) = _uiState.update { it.copy(showPublicShareSettingsSheet = show) }
    fun onShareNoteChange(value: String) = _uiState.update { it.copy(shareNote = value) }
    fun onShareIncludeLinkChange(value: Boolean) = _uiState.update { it.copy(shareIncludeLink = value) }
    fun onShareIncludeAddressChange(value: Boolean) = _uiState.update { it.copy(shareIncludeAddress = value) }
    fun onShareIncludeNotesChange(value: Boolean) = _uiState.update { it.copy(shareIncludeNotes = value) }
    fun onShareIncludeAmenitiesChange(value: Boolean) = _uiState.update { it.copy(shareIncludeAmenities = value) }
    fun onShareIncludePublicPageChange(value: Boolean) = _uiState.update { it.copy(shareIncludePublicPage = value) }
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

    fun propertyShareOptions(): DossierShareOptions {
        val state = _uiState.value
        val publicUrl = state.detail?.publicShare?.shareUrl.orEmpty()
        return DossierShareOptions(
            customNote = state.shareNote,
            includeDivarLink = state.shareIncludeLink,
            includeAddress = state.shareIncludeAddress,
            includeInternalNotes = state.shareIncludeNotes,
            includeAmenities = state.shareIncludeAmenities,
            includePublicPageLink = state.shareIncludePublicPage && publicUrl.isNotBlank(),
            publicPageUrl = publicUrl,
            footer = DossierShareOptions.PERSONAL_FOOTER,
        )
    }

    fun savePublicShareSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (
                val result = repository.updatePropertyPublicShare(
                    propertyId,
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
                        isSubmitting = false,
                        showPublicShareSettingsSheet = false,
                        detail = state.detail?.copy(publicShare = result.data),
                        successMessage = "تنظیمات صفحه عمومی ذخیره شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun toggleLinkContactSheet(show: Boolean) = _uiState.update {
        it.copy(
            showLinkContactSheet = show,
            showLinkContactPicker = false,
            linkContactId = if (!show) "" else it.linkContactId,
            linkContactName = if (!show) "" else it.linkContactName,
            linkContactPhone = if (!show) "" else it.linkContactPhone,
        )
    }
    fun onLinkContactNameChange(v: String) = _uiState.update {
        it.copy(linkContactName = v, linkContactId = "")
    }

    fun onLinkContactPhoneChange(v: String) = _uiState.update {
        it.copy(linkContactPhone = v, linkContactId = "")
    }

    fun clearLinkContactSelection() = _uiState.update {
        it.copy(linkContactId = "", linkContactName = "", linkContactPhone = "")
    }

    fun toggleLinkContactPicker(show: Boolean) = _uiState.update { it.copy(showLinkContactPicker = show) }
    fun onLinkContactSelected(contact: ir.divarfiling.mobile.core.network.ContactDto) = _uiState.update {
        it.copy(
            showLinkContactPicker = false,
            linkContactId = contact.id.toString(),
            linkContactName = contact.fullName,
            linkContactPhone = contact.phone.orEmpty(),
        )
    }
    fun onInlineNotesChange(v: String) = _uiState.update { it.copy(inlineNotes = v) }

    fun saveInlineNotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (
                val result = repository.updateProperty(
                    propertyId,
                    PropertyUpdateRequest(notes = _uiState.value.inlineNotes),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "یادداشت ذخیره شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun linkContact() {
        val state = _uiState.value
        val pickedId = state.linkContactId.trim().toLongOrNull()
        val name = state.linkContactName.trim()
        val phone = state.linkContactPhone.trim()
        if (pickedId == null && (name.isBlank() || phone.isBlank())) {
            _uiState.update {
                it.copy(error = "نام و شماره مالک را وارد کنید یا از مخاطبین انتخاب کنید")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (
                val result = repository.linkPropertyContact(
                    propertyId,
                    PropertyLinkContactRequest(
                        customerId = pickedId,
                        fullName = name,
                        phone = phone,
                        role = "مالک",
                        isPrimary = true,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showLinkContactSheet = false,
                            linkContactId = "",
                            linkContactName = "",
                            linkContactPhone = "",
                            successMessage = "مالک به ملک متصل شد",
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

    fun uploadDocument(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.uploadPropertyDocument(propertyId, uri)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "مدرک آپلود شد") }
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
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.deletePropertyDocument(propertyId, documentId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "مدرک حذف شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun toggleContactMatchesSheet(show: Boolean) {
        _uiState.update { it.copy(showContactMatchesSheet = show) }
        if (show && _uiState.value.contactMatchesData == null) {
            loadContactMatches(openSheet = true)
        }
    }

    fun loadContactMatches(openSheet: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    contactMatchesLoading = true,
                    showContactMatchesSheet = openSheet || it.showContactMatchesSheet,
                    error = null,
                )
            }
            when (val result = repository.getPropertyContactMatches(propertyId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(contactMatchesData = result.data, contactMatchesLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(contactMatchesLoading = false, error = result.message)
                }
            }
        }
    }

    fun suggestContactMatches(matches: List<PropertyContactMatchItemDto>, shareChannel: String? = null) {
        if (matches.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.suggestPropertyContacts(propertyId, matches.map { it.customerId })) {
                is ApiResult.Success -> {
                    val response = result.data
                    val primarySuggestion = response.suggestions.firstOrNull()
                    val shareItem = when {
                        matches.size == 1 -> {
                            val selectedId = matches.first().customerId
                            response.suggestions.firstOrNull { it.customerId == selectedId }
                                ?: primarySuggestion
                        }
                        else -> primarySuggestion
                    }
                    val shareText = suggestionMessage(shareItem ?: ContactSuggestionItemDto(), response.publicUrl)
                        .takeIf { it.isNotBlank() }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showContactMatchesSheet = false,
                            contactSuggestionResult = if (shareChannel == null) response else null,
                            pendingSocialShare = if (shareChannel != null && shareText != null && shareItem != null) {
                                PendingContactSocialShare(
                                    channel = shareChannel,
                                    message = shareText,
                                    phone = shareItem.phone ?: matches.firstOrNull()?.phone,
                                    socialLinks = shareItem.socialLinks,
                                )
                            } else {
                                null
                            },
                            successMessage = "${response.suggestedCount} مشتری پیشنهاد شد",
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

    fun clearPendingSocialShare() = _uiState.update { it.copy(pendingSocialShare = null) }

    fun dismissContactSuggestionResult() = _uiState.update { it.copy(contactSuggestionResult = null) }
    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }
}
