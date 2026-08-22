package ir.divarfiling.mobile.feature.crm

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.DossierShareOptions
import ir.divarfiling.mobile.core.export.ExportFormat
import ir.divarfiling.mobile.core.export.ExportShareHelper
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.ContactSuggestResponse
import ir.divarfiling.mobile.core.network.DealCreateRequest
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealPipelineColumnDto
import ir.divarfiling.mobile.core.network.DealUpdateRequest
import ir.divarfiling.mobile.core.network.PropertyContactMatchItemDto
import ir.divarfiling.mobile.core.network.PropertyContactMatchesData
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.PropertyDetailData
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.PropertyLinkContactRequest
import ir.divarfiling.mobile.core.network.PropertyUpdateRequest
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
    val selectedStage: String? = null,
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val page: Int = 1,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
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
                    _uiState.update { it.copy(stages = result.data) }
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
                createStage = if (show) it.createStage else "سرنخ",
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
}

data class DealDetailUiState(
    val deal: DealDto? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val stages: List<String> = CrmConstants.DEAL_STAGES,
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
                if (r is ApiResult.Success) _uiState.update { it.copy(stages = r.data) }
            }
            when (val result = repository.getDeal(dealId)) {
                is ApiResult.Success -> _uiState.update {
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
}

data class PropertiesUiState(
    val properties: List<PropertyDto> = emptyList(),
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
    val createTitle: String = "",
    val createCity: String = "",
    val createDistrict: String = "",
    val createDealMode: String = PropertyConstants.DEAL_MODES.first(),
    val createPropertyType: String = PropertyConstants.PROPERTY_TYPES.first(),
    val createPrice: String = "",
    val createDeposit: String = "",
    val createRent: String = "",
    val createArea: String = "",
    val createNotes: String = "",
    val isSubmittingCreate: Boolean = false,
    val isExporting: Boolean = false,
    val showExportSheet: Boolean = false,
    val showSaveFilterDialog: Boolean = false,
    val saveFilterName: String = "",
    val exportMessage: String? = null,
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
)

object PropertyConstants {
    val DEAL_MODES = listOf("فروش", "رهن و اجاره", "اجاره", "پیش‌فروش")
    val PROPERTY_TYPES = listOf("آپارتمان", "ویلا", "کلنگی", "اداری", "مغازه", "زمین", "سایر")
    val TX_STATUSES = listOf("فعال", "در مذاکره", "قرارداد", "فروخته‌شده", "اجاره‌رفته", "بایگانی")
}

@HiltViewModel
class PropertiesViewModel @Inject constructor(
    private val repository: DealsRepository,
    private val exportRepository: ExportRepository,
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
        load()
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
            )
        }
        load()
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
                createTitle = if (!show) "" else it.createTitle,
                createCity = if (!show) "" else it.createCity,
                createDistrict = if (!show) "" else it.createDistrict,
                createPrice = if (!show) "" else it.createPrice,
                createDeposit = if (!show) "" else it.createDeposit,
                createRent = if (!show) "" else it.createRent,
                createArea = if (!show) "" else it.createArea,
                createNotes = if (!show) "" else it.createNotes,
                createDealMode = if (!show) PropertyConstants.DEAL_MODES.first() else it.createDealMode,
                createPropertyType = if (!show) PropertyConstants.PROPERTY_TYPES.first() else it.createPropertyType,
            )
        }
    }
    fun onCreateTitleChange(v: String) = _uiState.update { it.copy(createTitle = v) }
    fun onCreateCityChange(v: String) = _uiState.update { it.copy(createCity = v) }
    fun onCreateDistrictChange(v: String) = _uiState.update { it.copy(createDistrict = v) }
    fun onCreateDealModeChange(v: String) = _uiState.update { it.copy(createDealMode = v) }
    fun onCreatePropertyTypeChange(v: String) = _uiState.update { it.copy(createPropertyType = v) }
    fun onCreatePriceChange(v: String) = _uiState.update { it.copy(createPrice = v) }
    fun onCreateDepositChange(v: String) = _uiState.update { it.copy(createDeposit = v) }
    fun onCreateRentChange(v: String) = _uiState.update { it.copy(createRent = v) }
    fun onCreateAreaChange(v: String) = _uiState.update { it.copy(createArea = v) }
    fun onCreateNotesChange(v: String) = _uiState.update { it.copy(createNotes = v) }

    fun submitCreate() {
        val title = _uiState.value.createTitle.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "عنوان ملک الزامی است") }
            return
        }
        val state = _uiState.value
        val isRent = state.createDealMode.contains("اجاره") || state.createDealMode.contains("رهن")
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingCreate = true, error = null) }
            when (
                val result = repository.createProperty(
                    PropertyCreateRequest(
                        title = title,
                        dealMode = state.createDealMode,
                        propertyType = state.createPropertyType,
                        city = state.createCity.trim(),
                        district = state.createDistrict.trim(),
                        salePrice = if (!isRent) state.createPrice.trim().toLongOrNull() else null,
                        deposit = if (isRent) state.createDeposit.trim().toLongOrNull() else null,
                        rent = if (isRent) state.createRent.trim().toLongOrNull() else null,
                        area = state.createArea.trim().toDoubleOrNull(),
                        notes = state.createNotes.trim(),
                    ),
                )
            ) {
                is ApiResult.Success -> {
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
}

data class PropertyDetailUiState(
    val detail: PropertyDetailData? = null,
    val selectedTab: PropertyDetailTab = PropertyDetailTab.OVERVIEW,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
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
    val editTitle: String = "",
    val editCity: String = "",
    val editDistrict: String = "",
    val editNeighborhood: String = "",
    val editDealMode: String = PropertyConstants.DEAL_MODES.first(),
    val editPropertyType: String = PropertyConstants.PROPERTY_TYPES.first(),
    val editTransactionStatus: String = CrmConstants.PROPERTY_TX_STATUSES.first(),
    val editArea: String = "",
    val editRooms: String = "",
    val editPrice: String = "",
    val editDeposit: String = "",
    val editRent: String = "",
    val editAddress: String = "",
    val editNotes: String = "",
    val editFloor: String = "",
    val editBuildYear: String = "",
    val editAmenities: String = "",
    val editHasParking: Boolean = false,
    val editHasStorage: Boolean = false,
    val editHasElevator: Boolean = false,
    val editIsVacant: Boolean = false,
    val editOwnerName: String = "",
    val editOwnerPhone: String = "",
    val showDiscardEditDialog: Boolean = false,
    val inlineNotes: String = "",
    val showShareSheet: Boolean = false,
    val showPublicShareSettingsSheet: Boolean = false,
    val showContactMatchesSheet: Boolean = false,
    val contactMatchesData: PropertyContactMatchesData? = null,
    val contactMatchesLoading: Boolean = false,
    val contactSuggestionResult: ContactSuggestResponse? = null,
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
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val propertyId: Long = savedStateHandle.get<Long>("propertyId") ?: 0L
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
                    _uiState.update {
                        it.copy(
                            detail = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            inlineNotes = p.notes.orEmpty(),
                            editTitle = p.title,
                            editCity = p.city.orEmpty(),
                            editDistrict = p.district.orEmpty(),
                            editNeighborhood = p.neighborhood.orEmpty(),
                            editDealMode = p.dealMode ?: PropertyConstants.DEAL_MODES.first(),
                            editPropertyType = p.propertyType ?: PropertyConstants.PROPERTY_TYPES.first(),
                            editTransactionStatus = p.transactionStatus ?: CrmConstants.PROPERTY_TX_STATUSES.first(),
                            editArea = p.area?.let { v -> if (v % 1.0 == 0.0) v.toInt().toString() else v.toString() }.orEmpty(),
                            editRooms = p.rooms.orEmpty(),
                            editPrice = p.salePrice?.toString().orEmpty(),
                            editDeposit = p.deposit?.toString().orEmpty(),
                            editRent = p.rent?.toString().orEmpty(),
                            editAddress = p.address.orEmpty(),
                            editNotes = p.notes.orEmpty(),
                            editFloor = p.floor?.toString().orEmpty(),
                            editBuildYear = p.buildYear?.toString().orEmpty(),
                            editAmenities = p.amenities.orEmpty(),
                            editHasParking = p.hasParking,
                            editHasStorage = p.hasStorage,
                            editHasElevator = p.hasElevator,
                            editIsVacant = p.isVacant,
                            editOwnerName = p.ownerName.orEmpty(),
                            editOwnerPhone = p.phone.orEmpty(),
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

    fun saveEdit() {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val state = _uiState.value
            val isRent = state.editDealMode.contains("اجاره") || state.editDealMode.contains("رهن")
            when (
                val result = repository.updateProperty(
                    propertyId,
                    PropertyUpdateRequest(
                        title = state.editTitle.trim(),
                        dealMode = state.editDealMode,
                        transactionStatus = state.editTransactionStatus,
                        propertyType = state.editPropertyType,
                        city = state.editCity.trim(),
                        district = state.editDistrict.trim(),
                        neighborhood = state.editNeighborhood.trim(),
                        area = state.editArea.trim().toDoubleOrNull(),
                        rooms = state.editRooms.trim(),
                        salePrice = if (isRent) null else state.editPrice.trim().toLongOrNull(),
                        deposit = if (isRent) state.editDeposit.trim().toLongOrNull() else null,
                        rent = if (isRent) state.editRent.trim().toLongOrNull() else null,
                        address = state.editAddress.trim(),
                        notes = state.editNotes,
                        floor = state.editFloor.trim().toIntOrNull(),
                        buildYear = state.editBuildYear.trim().toIntOrNull(),
                        amenities = state.editAmenities,
                        hasParking = state.editHasParking,
                        hasStorage = state.editHasStorage,
                        hasElevator = state.editHasElevator,
                        isVacant = state.editIsVacant,
                        ownerName = state.editOwnerName.trim(),
                        ownerPhone = state.editOwnerPhone.trim(),
                    ),
                )
            ) {
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

    private fun isPropertyEditDirty(state: PropertyDetailUiState): Boolean {
        val p = state.detail?.property ?: return false
        val area = p.area?.let { v -> if (v % 1.0 == 0.0) v.toInt().toString() else v.toString() }.orEmpty()
        return state.editTitle != p.title ||
            state.editCity != p.city.orEmpty() ||
            state.editDistrict != p.district.orEmpty() ||
            state.editNeighborhood != p.neighborhood.orEmpty() ||
            state.editDealMode != (p.dealMode ?: PropertyConstants.DEAL_MODES.first()) ||
            state.editPropertyType != (p.propertyType ?: PropertyConstants.PROPERTY_TYPES.first()) ||
            state.editTransactionStatus != (p.transactionStatus ?: CrmConstants.PROPERTY_TX_STATUSES.first()) ||
            state.editArea != area ||
            state.editRooms != p.rooms.orEmpty() ||
            state.editPrice != p.salePrice?.toString().orEmpty() ||
            state.editDeposit != p.deposit?.toString().orEmpty() ||
            state.editRent != p.rent?.toString().orEmpty() ||
            state.editAddress != p.address.orEmpty() ||
            state.editNotes != p.notes.orEmpty() ||
            state.editFloor != p.floor?.toString().orEmpty() ||
            state.editBuildYear != p.buildYear?.toString().orEmpty() ||
            state.editAmenities != p.amenities.orEmpty() ||
            state.editHasParking != p.hasParking ||
            state.editHasStorage != p.hasStorage ||
            state.editHasElevator != p.hasElevator ||
            state.editIsVacant != p.isVacant ||
            state.editOwnerName != p.ownerName.orEmpty() ||
            state.editOwnerPhone != p.phone.orEmpty()
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
    fun onLinkContactIdChange(v: String) = _uiState.update { it.copy(linkContactId = v) }
    fun onLinkContactRoleChange(v: String) = _uiState.update { it.copy(linkContactRole = v) }
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
        val customerId = _uiState.value.linkContactId.trim().toLongOrNull()
        if (customerId == null) {
            _uiState.update { it.copy(error = "شناسه مخاطب نامعتبر است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (
                val result = repository.linkPropertyContact(
                    propertyId,
                    PropertyLinkContactRequest(
                        customerId = customerId,
                        role = _uiState.value.linkContactRole,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, showLinkContactSheet = false, successMessage = "مخاطب پیوند شد")
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

    fun suggestContactMatches(matches: List<PropertyContactMatchItemDto>) {
        if (matches.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = repository.suggestPropertyContacts(propertyId, matches.map { it.customerId })) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showContactMatchesSheet = false,
                            contactSuggestionResult = result.data,
                            successMessage = "${result.data.suggestedCount} مشتری پیشنهاد شد",
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

    fun onEditTitleChange(v: String) = _uiState.update { it.copy(editTitle = v) }
    fun onEditCityChange(v: String) = _uiState.update { it.copy(editCity = v) }
    fun onEditDistrictChange(v: String) = _uiState.update { it.copy(editDistrict = v) }
    fun onEditNeighborhoodChange(v: String) = _uiState.update { it.copy(editNeighborhood = v) }
    fun onEditDealModeChange(v: String) = _uiState.update { it.copy(editDealMode = v) }
    fun onEditPropertyTypeChange(v: String) = _uiState.update { it.copy(editPropertyType = v) }
    fun onEditTransactionStatusChange(v: String) = _uiState.update { it.copy(editTransactionStatus = v) }
    fun onEditAreaChange(v: String) = _uiState.update { it.copy(editArea = v) }
    fun onEditRoomsChange(v: String) = _uiState.update { it.copy(editRooms = v) }
    fun onEditPriceChange(v: String) = _uiState.update { it.copy(editPrice = v) }
    fun onEditDepositChange(v: String) = _uiState.update { it.copy(editDeposit = v) }
    fun onEditRentChange(v: String) = _uiState.update { it.copy(editRent = v) }
    fun onEditAddressChange(v: String) = _uiState.update { it.copy(editAddress = v) }
    fun onEditNotesChange(v: String) = _uiState.update { it.copy(editNotes = v) }
    fun onEditFloorChange(v: String) = _uiState.update { it.copy(editFloor = v) }
    fun onEditBuildYearChange(v: String) = _uiState.update { it.copy(editBuildYear = v) }
    fun onEditAmenitiesChange(v: String) = _uiState.update { it.copy(editAmenities = v) }
    fun onEditParkingChange(v: Boolean) = _uiState.update { it.copy(editHasParking = v) }
    fun onEditStorageChange(v: Boolean) = _uiState.update { it.copy(editHasStorage = v) }
    fun onEditElevatorChange(v: Boolean) = _uiState.update { it.copy(editHasElevator = v) }
    fun onEditVacantChange(v: Boolean) = _uiState.update { it.copy(editIsVacant = v) }
    fun onEditOwnerNameChange(v: String) = _uiState.update { it.copy(editOwnerName = v) }
    fun onEditOwnerPhoneChange(v: String) = _uiState.update { it.copy(editOwnerPhone = PhoneNormalizer.normalize(v)) }
    fun dismissContactSuggestionResult() = _uiState.update { it.copy(contactSuggestionResult = null) }
    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }
}
