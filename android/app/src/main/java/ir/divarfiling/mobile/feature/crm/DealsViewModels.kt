package ir.divarfiling.mobile.feature.crm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.DealCreateRequest
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealPipelineColumnDto
import ir.divarfiling.mobile.core.network.DealUpdateRequest
import ir.divarfiling.mobile.core.network.PropertyCreateRequest
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.PropertyUpdateRequest
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.DashboardRepository
import ir.divarfiling.mobile.data.repository.DealsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DealsUiState(
    val deals: List<DealDto> = emptyList(),
    val pipelineColumns: List<DealPipelineColumnDto> = emptyList(),
    val stages: List<String> = CrmConstants.DEAL_STAGES,
    val selectedStage: String? = null,
    val query: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val page: Int = 1,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val createTitle: String = "",
    val createCustomerId: String = "",
    val createAmount: String = "",
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
)

@HiltViewModel
class DealsViewModel @Inject constructor(
    private val repository: DealsRepository,
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DealsUiState())
    val uiState: StateFlow<DealsUiState> = _uiState.asStateFlow()

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
        load()
    }

    fun load(refreshing: Boolean = false) {
        viewModelScope.launch {
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
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    fun refresh() = load(refreshing = true)

    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v) }
    fun search() = load()
    fun selectStage(stage: String) {
        _uiState.update { it.copy(selectedStage = if (it.selectedStage == stage) null else stage) }
        load()
    }

    fun clearStageFilter() {
        if (_uiState.value.selectedStage != null) {
            _uiState.update { it.copy(selectedStage = null) }
            load()
        }
    }

    fun toggleCreate(show: Boolean) = _uiState.update { it.copy(showCreateDialog = show) }
    fun onCreateTitleChange(v: String) = _uiState.update { it.copy(createTitle = v) }
    fun onCreateCustomerIdChange(v: String) = _uiState.update { it.copy(createCustomerId = v) }
    fun onCreateAmountChange(v: String) = _uiState.update { it.copy(createAmount = v) }

    fun submitCreate() {
        val customerId = _uiState.value.createCustomerId.trim().toLongOrNull() ?: return
        viewModelScope.launch {
            when (repository.createDeal(
                DealCreateRequest(
                    customerId = customerId,
                    title = _uiState.value.createTitle.trim(),
                    amount = _uiState.value.createAmount.trim().toLongOrNull(),
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(showCreateDialog = false, createTitle = "", createCustomerId = "", createAmount = "")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = it.error) }
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
    val editTitle: String = "",
    val editAmount: String = "",
    val editNotes: String = "",
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

    fun changeStage(stage: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (repository.updateDealStage(dealId, stage)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "مرحله به‌روز شد") }
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
            when (repository.updateDeal(
                dealId,
                DealUpdateRequest(
                    title = state.editTitle.trim(),
                    amount = state.editAmount.trim().toLongOrNull(),
                    notes = state.editNotes,
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, showEditSheet = false, successMessage = "ذخیره شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun toggleEditSheet(show: Boolean) = _uiState.update { it.copy(showEditSheet = show) }
    fun onEditTitleChange(v: String) = _uiState.update { it.copy(editTitle = v) }
    fun onEditAmountChange(v: String) = _uiState.update { it.copy(editAmount = v) }
    fun onEditNotesChange(v: String) = _uiState.update { it.copy(editNotes = v) }
    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }
}

data class PropertiesUiState(
    val properties: List<PropertyDto> = emptyList(),
    val query: String = "",
    val transactionStatus: String? = null,
    val dealMode: String? = null,
    val propertyType: String? = null,
    val isLoading: Boolean = false,
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
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PropertiesUiState())
    val uiState: StateFlow<PropertiesUiState> = _uiState.asStateFlow()

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
        load()
    }

    fun load(refreshing: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = !refreshing && it.properties.isEmpty(), isRefreshing = refreshing)
            }
            when (val result = repository.getProperties(
                query = _uiState.value.query,
                dealMode = _uiState.value.dealMode,
                propertyType = _uiState.value.propertyType,
                transactionStatus = _uiState.value.transactionStatus,
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(properties = result.data.items, hasMore = result.data.hasMore, isLoading = false, isRefreshing = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    fun refresh() = load(refreshing = true)
    fun onQueryChange(v: String) = _uiState.update { it.copy(query = v) }
    fun onTransactionStatusChange(status: String?) = _uiState.update { it.copy(transactionStatus = status) }
    fun onDealModeChange(mode: String?) = _uiState.update { it.copy(dealMode = mode) }
    fun onPropertyTypeChange(type: String?) = _uiState.update { it.copy(propertyType = type) }
    fun search() = load()
    fun toggleCreate(show: Boolean) = _uiState.update { it.copy(showCreateDialog = show) }
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
        if (title.isBlank()) return
        val state = _uiState.value
        val isRent = state.createDealMode.contains("اجاره") || state.createDealMode.contains("رهن")
        viewModelScope.launch {
            when (repository.createProperty(
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
            )) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showCreateDialog = false,
                            createTitle = "",
                            createCity = "",
                            createDistrict = "",
                            createPrice = "",
                            createDeposit = "",
                            createRent = "",
                            createArea = "",
                            createNotes = "",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update { s -> s.copy(error = s.error) }
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
            when (repository.createProperty(
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
            )) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { s -> s.copy(error = s.error) }
            }
        }
    }
}

data class PropertyDetailUiState(
    val property: PropertyDto? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showEditSheet: Boolean = false,
    val editTitle: String = "",
    val editCity: String = "",
    val editPrice: String = "",
    val editNotes: String = "",
)

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
            _uiState.update { it.copy(isLoading = it.property == null) }
            when (val result = repository.getProperty(propertyId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        property = result.data,
                        isLoading = false,
                        isRefreshing = false,
                        editTitle = result.data.title,
                        editCity = result.data.city.orEmpty(),
                        editPrice = result.data.salePrice?.toString().orEmpty(),
                        editNotes = result.data.notes.orEmpty(),
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

    fun changeStatus(status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (repository.updatePropertyStatus(propertyId, status)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "وضعیت به‌روز شد") }
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
            when (repository.updateProperty(
                propertyId,
                PropertyUpdateRequest(
                    title = state.editTitle.trim(),
                    city = state.editCity.trim(),
                    salePrice = state.editPrice.trim().toLongOrNull(),
                    notes = state.editNotes,
                ),
            )) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, showEditSheet = false, successMessage = "ذخیره شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun toggleEditSheet(show: Boolean) = _uiState.update { it.copy(showEditSheet = show) }
    fun onEditTitleChange(v: String) = _uiState.update { it.copy(editTitle = v) }
    fun onEditCityChange(v: String) = _uiState.update { it.copy(editCity = v) }
    fun onEditPriceChange(v: String) = _uiState.update { it.copy(editPrice = v) }
    fun onEditNotesChange(v: String) = _uiState.update { it.copy(editNotes = v) }
    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }
}
