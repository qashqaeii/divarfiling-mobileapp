package ir.divarfiling.mobile.feature.crm

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.export.ExportFormat
import ir.divarfiling.mobile.core.export.ExportShareHelper
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.TodayData
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.ContactUpdateRequest
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import ir.divarfiling.mobile.data.repository.DashboardRepository
import ir.divarfiling.mobile.data.repository.ExportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import ir.divarfiling.mobile.feature.crm.components.ContactEditBuilderState
import ir.divarfiling.mobile.feature.crm.components.ContactEditMoneyState
import ir.divarfiling.mobile.feature.crm.components.ContactEditPrefsState
import javax.inject.Inject

data class ContactsUiState(
    val contacts: List<ContactDto> = emptyList(),
    val query: String = "",
    val statusFilter: String? = null,
    val customerTypeFilter: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showQuickLead: Boolean = false,
    val leadName: String = "",
    val leadPhone: String = "",
    val leadCustomerType: String = "سرنخ",
    val leadSource: String = "موبایل",
    val leadNotes: String = "",
    val leadEmail: String = "",
    val leadCity: String = "",
    val leadDistrict: String = "",
    val leadJob: String = "",
    val leadCompanyName: String = "",
    val leadNationalId: String = "",
    val leadLeaseDeadline: String = "",
    val leadMoney: ContactEditMoneyState = ContactEditMoneyState(),
    val leadPrefs: ContactEditPrefsState = ContactEditPrefsState(),
    val leadBuilder: ContactEditBuilderState = ContactEditBuilderState(),
    val leadMatchingTolerance: Int = 20,
    val leadActiveChannels: Set<String> = emptySet(),
    val leadSocialLinks: Map<String, String> = emptyMap(),
    val leadDatasetNeighborhoods: List<String> = emptyList(),
    val isExporting: Boolean = false,
    val showExportSheet: Boolean = false,
    val exportMessage: String? = null,
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
    val savedFilters: List<ir.divarfiling.mobile.core.network.SavedFilterDto> = emptyList(),
    val activeSavedFilterId: Long? = null,
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
    private val exportRepository: ExportRepository,
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
    private val extrasRepository: ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val initialCustomerType = savedStateHandle.get<String>("customerType")
        ?.takeIf { it.isNotBlank() }
    private val _uiState = MutableStateFlow(
        ContactsUiState(
            customerTypeFilter = initialCustomerType,
            leadCustomerType = initialCustomerType ?: "سرنخ",
        ),
    )
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

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
        load(reset = true)
    }

    fun loadSavedFilters() {
        viewModelScope.launch {
            when (val result = extrasRepository.getSavedFilters(entity = "contacts")) {
                is ApiResult.Success -> _uiState.update { it.copy(savedFilters = result.data) }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun load(reset: Boolean = false) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.page
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = reset && it.contacts.isEmpty(),
                    isLoadingMore = !reset,
                    error = null,
                )
            }
            when (
                val result = crmRepository.getContacts(
                    query = state.query,
                    page = page,
                    status = state.statusFilter,
                    customerType = state.customerTypeFilter,
                )
            ) {
                is ApiResult.Success -> {
                    val merged = if (reset) result.data.items else _uiState.value.contacts + result.data.items
                    _uiState.update {
                        it.copy(
                            contacts = merged,
                            page = page,
                            hasMore = result.data.hasMore,
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        page = if (!reset && it.page > 1) it.page - 1 else it.page,
                        error = result.message,
                    )
                }
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore || _uiState.value.isLoading) return
        _uiState.update { it.copy(page = it.page + 1) }
        load(reset = false)
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, page = 1) }
        load(reset = true)
        loadSavedFilters()
    }

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }
    fun onStatusFilterChange(status: String?) = _uiState.update {
        it.copy(statusFilter = status, activeSavedFilterId = null)
    }

    fun onCustomerTypeFilterChange(type: String?) = _uiState.update {
        it.copy(customerTypeFilter = type, activeSavedFilterId = null)
    }

    fun applySavedFilter(filter: ir.divarfiling.mobile.core.network.SavedFilterDto) {
        val params = filter.resolvedParams
        _uiState.update {
            it.copy(
                query = params["q"].orEmpty(),
                statusFilter = params["status"]?.ifBlank { null },
                customerTypeFilter = params["customer_type"]?.ifBlank { null },
                activeSavedFilterId = filter.id,
                page = 1,
            )
        }
        load(reset = true)
    }

    fun saveCurrentAsFilter(name: String) {
        val state = _uiState.value
        val params = buildMap {
            if (state.query.isNotBlank()) put("q", state.query.trim())
            state.statusFilter?.takeIf { it.isNotBlank() }?.let { put("status", it) }
            state.customerTypeFilter?.takeIf { it.isNotBlank() }?.let { put("customer_type", it) }
        }
        if (params.isEmpty() || name.isBlank()) {
            _uiState.update { it.copy(error = "برای ذخیره، حداقل یک معیار و نام لازم است") }
            return
        }
        viewModelScope.launch {
            when (
                val result = extrasRepository.createSavedFilter(
                    ir.divarfiling.mobile.core.network.SavedFilterCreateRequest(
                        name = name.trim(),
                        scope = "contacts",
                        params = params,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "فیلتر ذخیره شد", activeSavedFilterId = result.data.id)
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

    fun search() {
        _uiState.update { it.copy(page = 1) }
        load(reset = true)
    }

    fun toggleQuickLead(show: Boolean, customerType: String? = null) = _uiState.update {
        val defaultType = initialCustomerType ?: "سرنخ"
        val resolvedType = when {
            !show -> defaultType
            !customerType.isNullOrBlank() -> customerType
            else -> it.leadCustomerType.ifBlank { defaultType }
        }
        val resolvedSource = if (show) it.leadSource.ifBlank { "موبایل" } else "موبایل"
        it.copy(
            showQuickLead = show,
            leadName = if (show) it.leadName else "",
            leadPhone = if (show) it.leadPhone else "",
            leadCustomerType = resolvedType,
            leadSource = resolvedSource,
            leadNotes = if (show) it.leadNotes else "",
            leadEmail = if (show) it.leadEmail else "",
            leadCity = if (show) it.leadCity else "",
            leadDistrict = if (show) it.leadDistrict else "",
            leadJob = if (show) it.leadJob else "",
            leadCompanyName = if (show) it.leadCompanyName else "",
            leadNationalId = if (show) it.leadNationalId else "",
            leadLeaseDeadline = if (show) it.leadLeaseDeadline else "",
            leadMoney = if (show) it.leadMoney else ContactEditMoneyState(),
            leadPrefs = if (show) it.leadPrefs else ContactEditPrefsState(),
            leadBuilder = if (show) it.leadBuilder else ContactEditBuilderState(),
            leadMatchingTolerance = if (show) it.leadMatchingTolerance else 20,
            leadActiveChannels = if (show) {
                it.leadActiveChannels.ifEmpty {
                    CrmContactChannels.defaultActiveChannelsForSource(resolvedSource)
                }
            } else {
                emptySet()
            },
            leadSocialLinks = if (show) it.leadSocialLinks else emptyMap(),
        ).also { if (show) loadLeadNeighborhoods() }
    }

    private fun loadLeadNeighborhoods() {
        if (_uiState.value.leadDatasetNeighborhoods.isNotEmpty()) return
        viewModelScope.launch {
            when (val result = crmRepository.getNeighborhoods()) {
                is ApiResult.Success -> _uiState.update { it.copy(leadDatasetNeighborhoods = result.data) }
                is ApiResult.Error -> Unit
            }
        }
    }
    fun onLeadNameChange(v: String) = _uiState.update { it.copy(leadName = v) }
    fun onLeadPhoneChange(v: String) = _uiState.update { state ->
        state.copy(
            leadPhone = v,
            leadSocialLinks = CrmContactChannels.syncPhoneDerivedSocialLinks(
                activeChannels = state.leadActiveChannels,
                phone = v,
                currentLinks = state.leadSocialLinks,
            ),
        )
    }
    fun onLeadCustomerTypeChange(v: String) = _uiState.update { it.copy(leadCustomerType = v) }
    fun onLeadSourceChange(v: String) = _uiState.update {
        it.copy(
            leadSource = v,
            leadActiveChannels = CrmContactChannels.defaultActiveChannelsForSource(v),
        )
    }
    fun onLeadNotesChange(v: String) = _uiState.update { it.copy(leadNotes = v) }
    fun onLeadEmailChange(v: String) = _uiState.update { it.copy(leadEmail = v) }
    fun onLeadCityChange(v: String) = _uiState.update { it.copy(leadCity = v) }
    fun onLeadDistrictChange(v: String) = _uiState.update { it.copy(leadDistrict = v) }
    fun onLeadJobChange(v: String) = _uiState.update { it.copy(leadJob = v) }
    fun onLeadCompanyNameChange(v: String) = _uiState.update { it.copy(leadCompanyName = v) }
    fun onLeadNationalIdChange(v: String) = _uiState.update { it.copy(leadNationalId = v) }
    fun onLeadLeaseDeadlineChange(v: String) = _uiState.update { it.copy(leadLeaseDeadline = v) }

    fun onLeadBudgetMinChange(v: String) = _uiState.update { it.copy(leadMoney = it.leadMoney.copy(budgetMin = v)) }
    fun onLeadBudgetMaxChange(v: String) = _uiState.update { it.copy(leadMoney = it.leadMoney.copy(budgetMax = v)) }
    fun onLeadDepositMinChange(v: String) = _uiState.update { it.copy(leadMoney = it.leadMoney.copy(depositMin = v)) }
    fun onLeadDepositMaxChange(v: String) = _uiState.update { it.copy(leadMoney = it.leadMoney.copy(depositMax = v)) }
    fun onLeadRentMinChange(v: String) = _uiState.update { it.copy(leadMoney = it.leadMoney.copy(rentMin = v)) }
    fun onLeadRentMaxChange(v: String) = _uiState.update { it.copy(leadMoney = it.leadMoney.copy(rentMax = v)) }

    fun onLeadPropertyTypeChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(propertyType = v)) }
    fun onLeadRoomsChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(rooms = v)) }
    fun onLeadRoomsMinChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(roomsMin = v)) }
    fun onLeadRoomsMaxChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(roomsMax = v)) }
    fun onLeadMinAreaChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(minArea = v)) }
    fun onLeadMaxAreaChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(maxArea = v)) }
    fun onLeadAreasChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(areas = v)) }
    fun onLeadYearMinChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(yearMin = v)) }
    fun onLeadYearMaxChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(yearMax = v)) }
    fun onLeadFloorMinChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(floorMin = v)) }
    fun onLeadFloorMaxChange(v: String) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(floorMax = v)) }
    fun onLeadWantParkingChange(v: Boolean) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(wantParking = v)) }
    fun onLeadWantStorageChange(v: Boolean) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(wantStorage = v)) }
    fun onLeadWantElevatorChange(v: Boolean) = _uiState.update { it.copy(leadPrefs = it.leadPrefs.copy(wantElevator = v)) }

    fun onLeadBuilderBuyBudgetMinChange(v: String) =
        _uiState.update { it.copy(leadBuilder = it.leadBuilder.copy(buyBudgetMin = v)) }
    fun onLeadBuilderBuyBudgetMaxChange(v: String) =
        _uiState.update { it.copy(leadBuilder = it.leadBuilder.copy(buyBudgetMax = v)) }
    fun onLeadBuilderBuyMinAreaChange(v: String) =
        _uiState.update { it.copy(leadBuilder = it.leadBuilder.copy(buyMinArea = v)) }
    fun onLeadBuilderBuyMaxAreaChange(v: String) =
        _uiState.update { it.copy(leadBuilder = it.leadBuilder.copy(buyMaxArea = v)) }
    fun onLeadBuilderBuyAreasChange(v: String) =
        _uiState.update { it.copy(leadBuilder = it.leadBuilder.copy(buyAreas = v)) }
    fun onLeadBuilderBuyTypesChange(v: String) =
        _uiState.update { it.copy(leadBuilder = it.leadBuilder.copy(buyPropertyTypes = v)) }

    fun onLeadMatchingToleranceChange(v: Int) = _uiState.update { it.copy(leadMatchingTolerance = v) }

    fun onLeadToggleChannel(key: String, active: Boolean) = _uiState.update { state ->
        val next = state.leadActiveChannels.toMutableSet()
        if (active) next.add(key) else next.remove(key)
        val nextLinks = state.leadSocialLinks.toMutableMap()
        if (active) {
            CrmContactChannels.autoSocialHandle(key, state.leadPhone)?.let { nextLinks[key] = it }
        } else if (CrmContactChannels.usesPhoneForSocialHandle(key)) {
            nextLinks.remove(key)
        }
        state.copy(leadActiveChannels = next, leadSocialLinks = nextLinks)
    }

    fun onLeadSocialLinkChange(key: String, value: String) = _uiState.update { state ->
        val next = state.leadSocialLinks.toMutableMap()
        if (value.isBlank()) next.remove(key) else next[key] = value
        state.copy(leadSocialLinks = next)
    }

    private fun parseMoneyInput(raw: String): Long? = FormatUtils.parseLocalizedLong(raw)

    private fun buildLeadUpdateRequest(state: ContactsUiState): ContactUpdateRequest {
        val money = state.leadMoney
        val prefs = state.leadPrefs
        val builder = state.leadBuilder
        val type = state.leadCustomerType.ifBlank { initialCustomerType ?: "سرنخ" }
        val socialLinks = state.leadSocialLinks.filterValues { it.isNotBlank() }
        return ContactUpdateRequest(
            customerType = type,
            source = state.leadSource.ifBlank { "موبایل" },
            notes = state.leadNotes.trim().ifBlank { null },
            email = state.leadEmail.trim(),
            city = state.leadCity.trim().ifBlank { prefs.city.trim() },
            district = state.leadDistrict.trim().ifBlank { prefs.district.trim() },
            job = state.leadJob.trim(),
            companyName = state.leadCompanyName.trim(),
            nationalId = state.leadNationalId.trim(),
            leaseDeadline = state.leadLeaseDeadline.trim(),
            budgetMin = parseMoneyInput(money.budgetMin),
            budgetMax = parseMoneyInput(money.budgetMax),
            depositMin = parseMoneyInput(money.depositMin),
            depositMax = parseMoneyInput(money.depositMax),
            rentMin = parseMoneyInput(money.rentMin),
            rentMax = parseMoneyInput(money.rentMax),
            propertyType = prefs.propertyType.ifBlank { if (type == "سازنده") "آپارتمان" else "" },
            rooms = prefs.rooms,
            minArea = parseMoneyInput(prefs.minArea)?.toInt(),
            maxArea = parseMoneyInput(prefs.maxArea)?.toInt(),
            areas = prefs.areas,
            builderBuyBudgetMin = parseMoneyInput(builder.buyBudgetMin),
            builderBuyBudgetMax = parseMoneyInput(builder.buyBudgetMax),
            builderBuyMinArea = parseMoneyInput(builder.buyMinArea)?.toInt(),
            builderBuyMaxArea = parseMoneyInput(builder.buyMaxArea)?.toInt(),
            builderBuyAreas = builder.buyAreas,
            builderBuyPropertyTypes = builder.buyPropertyTypes,
            roomsMin = parseMoneyInput(prefs.roomsMin)?.toInt(),
            roomsMax = parseMoneyInput(prefs.roomsMax)?.toInt(),
            yearMin = parseMoneyInput(prefs.yearMin)?.toInt(),
            yearMax = parseMoneyInput(prefs.yearMax)?.toInt(),
            floorMin = parseMoneyInput(prefs.floorMin)?.toInt(),
            floorMax = parseMoneyInput(prefs.floorMax)?.toInt(),
            wantParking = prefs.wantParking,
            wantStorage = prefs.wantStorage,
            wantElevator = prefs.wantElevator,
            matchingTolerancePercent = state.leadMatchingTolerance,
            socialLinks = socialLinks.ifEmpty { null },
            activeChannels = state.leadActiveChannels.filter { it in CrmContactChannels.allKeys }.toList()
                .ifEmpty { null },
        )
    }

    private fun resetLeadForm(defaultType: String) = _uiState.update {
        it.copy(
            showQuickLead = false,
            leadName = "",
            leadPhone = "",
            leadCustomerType = defaultType,
            leadSource = "موبایل",
            leadNotes = "",
            leadEmail = "",
            leadCity = "",
            leadDistrict = "",
            leadJob = "",
            leadCompanyName = "",
            leadNationalId = "",
            leadLeaseDeadline = "",
            leadMoney = ContactEditMoneyState(),
            leadPrefs = ContactEditPrefsState(),
            leadBuilder = ContactEditBuilderState(),
            leadMatchingTolerance = 20,
            leadActiveChannels = emptySet(),
            leadSocialLinks = emptyMap(),
            leadDatasetNeighborhoods = it.leadDatasetNeighborhoods,
            isSubmitting = false,
        )
    }

    fun submitQuickLead() {
        val state = _uiState.value
        if (state.leadName.isBlank() || state.leadPhone.isBlank()) {
            _uiState.update { it.copy(error = "نام و تلفن الزامی است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val result = crmRepository.quickLead(state.leadName, state.leadPhone)) {
                is ApiResult.Success -> {
                    val defaultType = initialCustomerType ?: "سرنخ"
                    crmRepository.updateContact(
                        result.data.id,
                        buildLeadUpdateRequest(state),
                    )
                    resetLeadForm(defaultType)
                    refresh()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun openExportSheet() = _uiState.update { it.copy(showExportSheet = true) }

    fun dismissExportSheet() = _uiState.update { it.copy(showExportSheet = false) }

    fun clearExportMessage() = _uiState.update { it.copy(exportMessage = null) }

    fun exportContacts(context: Context, format: ExportFormat) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            when (val result = exportRepository.exportContacts(
                context = context,
                format = format,
                query = state.query,
                status = state.statusFilter,
            )) {
                is ApiResult.Success -> {
                    ExportShareHelper.shareFile(context, result.data, format.mimeType, "خروجی مخاطبین")
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

data class TodayUiState(
    val data: TodayData? = null,
    val query: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isActionRunning: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showFilterSheet: Boolean = false,
    val showNewTaskSheet: Boolean = false,
    val contactPicker: List<ContactDto> = emptyList(),
    val newTaskContactId: Long? = null,
    val newTaskTitle: String = "",
    val newTaskDueMillis: Long = System.currentTimeMillis() + 3_600_000L,
    val isSubmittingTask: Boolean = false,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.data == null && !it.isRefreshing, error = null)
            }
            when (val result = crmRepository.getToday()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(data = result.data, isLoading = false, isRefreshing = false)
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

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun toggleFilterSheet(show: Boolean) = _uiState.update { it.copy(showFilterSheet = show) }

    fun toggleNewTaskSheet(show: Boolean) {
        _uiState.update {
            it.copy(
                showNewTaskSheet = show,
                newTaskTitle = if (show) it.newTaskTitle else "",
                newTaskContactId = if (show) it.newTaskContactId else null,
            )
        }
        if (show) loadContactsForPicker()
    }

    fun onNewTaskContactSelect(id: Long) = _uiState.update { it.copy(newTaskContactId = id) }
    fun onNewTaskTitleChange(v: String) = _uiState.update { it.copy(newTaskTitle = v) }
    fun onNewTaskDueChange(millis: Long) = _uiState.update { it.copy(newTaskDueMillis = millis) }

    private fun loadContactsForPicker() {
        viewModelScope.launch {
            when (val result = crmRepository.getContacts(page = 1, pageSize = 100)) {
                is ApiResult.Success -> {
                    val items = result.data.items
                    _uiState.update {
                        it.copy(
                            contactPicker = items,
                            newTaskContactId = it.newTaskContactId ?: items.firstOrNull()?.id,
                        )
                    }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun submitNewTask() {
        val state = _uiState.value
        val contactId = state.newTaskContactId
        val title = state.newTaskTitle.trim()
        if (contactId == null || title.isBlank()) {
            _uiState.update { it.copy(error = "مخاطب و عنوان الزامی است") }
            return
        }
        val dueAt = Instant.ofEpochMilli(state.newTaskDueMillis).toString()
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingTask = true, error = null) }
            when (val result = crmRepository.createReminder(contactId, title, dueAt)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingTask = false,
                            showNewTaskSheet = false,
                            newTaskTitle = "",
                            successMessage = "کار جدید ثبت شد",
                        )
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmittingTask = false, error = result.message)
                }
            }
        }
    }

    fun completeTask(contactId: Long?, reminderId: Long?, note: String = "") {
        if (contactId == null && reminderId == null) {
            _uiState.update { it.copy(error = "این کار قابل تکمیل نیست") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, error = null) }
            when (val result = crmRepository.completeTodayTask(contactId, reminderId, note)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isActionRunning = false, successMessage = "انجام شد") }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isActionRunning = false, error = result.message)
                }
            }
        }
    }

    fun postponeTask(contactId: Long?, reminderId: Long?, days: Int = 1) {
        if (contactId == null && reminderId == null) {
            _uiState.update { it.copy(error = "این کار قابل تعویق نیست") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, error = null) }
            when (val result = crmRepository.postponeTodayTask(contactId, reminderId, days)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isActionRunning = false, successMessage = "به ${days} روز بعد موکول شد")
                    }
                    load()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isActionRunning = false, error = result.message)
                }
            }
        }
    }

    fun logCallActivity(contactId: Long) {
        viewModelScope.launch {
            crmRepository.createActivity(contactId, "تماس", "تماس از صفحه امروز", "تماس")
        }
    }

    fun logWhatsAppActivity(contactId: Long) {
        viewModelScope.launch {
            crmRepository.createActivity(contactId, "واتساپ", "پیام واتساپ از صفحه امروز", "واتساپ")
        }
    }
}
