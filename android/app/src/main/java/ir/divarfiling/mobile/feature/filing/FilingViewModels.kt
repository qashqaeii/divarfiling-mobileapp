package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.export.ExportFormat
import ir.divarfiling.mobile.core.export.ExportShareHelper
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.network.SavedFilterCreateRequest
import ir.divarfiling.mobile.core.network.SavedFilterDto
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.data.repository.DashboardRepository
import ir.divarfiling.mobile.data.repository.ExportRepository
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatasetsUiState(
    val datasets: List<DatasetDto> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isExporting: Boolean = false,
    val showExportSheet: Boolean = false,
    val exportTarget: DatasetDto? = null,
    val exportMessage: String? = null,
    val showDeleteSheet: Boolean = false,
    val deleteTarget: DatasetDto? = null,
    val isDeleting: Boolean = false,
    val deleteMessage: String? = null,
    val error: String? = null,
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
)

@HiltViewModel
class DatasetsViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
    private val exportRepository: ExportRepository,
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DatasetsUiState())
    val uiState: StateFlow<DatasetsUiState> = _uiState.asStateFlow()

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
        load(reset = true)
    }

    fun load(reset: Boolean = false) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.page
            _uiState.update {
                it.copy(
                    isLoading = reset && it.datasets.isEmpty(),
                    isLoadingMore = !reset,
                    error = null,
                )
            }
            when (val result = filingRepository.getDatasets(page = page)) {
                is ApiResult.Success -> {
                    val merged = if (reset) result.data.items else _uiState.value.datasets + result.data.items
                    _uiState.update {
                        it.copy(
                            datasets = merged,
                            page = page,
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
        if (!_uiState.value.hasMore || _uiState.value.isLoadingMore) return
        _uiState.update { it.copy(page = it.page + 1) }
        load(reset = false)
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, page = 1) }
        load(reset = true)
    }

    fun openExportSheet(dataset: DatasetDto) {
        _uiState.update { it.copy(showExportSheet = true, exportTarget = dataset) }
    }

    fun dismissExportSheet() {
        _uiState.update { it.copy(showExportSheet = false, exportTarget = null) }
    }

    fun clearExportMessage() = _uiState.update { it.copy(exportMessage = null) }

    fun openDeleteSheet(dataset: DatasetDto) {
        _uiState.update { it.copy(showDeleteSheet = true, deleteTarget = dataset, error = null) }
    }

    fun dismissDeleteSheet() {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(showDeleteSheet = false, deleteTarget = null) }
    }

    fun clearDeleteMessage() = _uiState.update { it.copy(deleteMessage = null) }

    fun confirmDeleteDataset() {
        val target = _uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            when (val result = filingRepository.deleteDataset(target.id)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            datasets = it.datasets.filter { ds -> ds.id != target.id },
                            isDeleting = false,
                            showDeleteSheet = false,
                            deleteTarget = null,
                            deleteMessage = "فایل «${target.name}» حذف شد",
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isDeleting = false, error = result.message)
                }
            }
        }
    }

    fun exportDataset(context: Context, format: ExportFormat) {
        val target = _uiState.value.exportTarget ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null) }
            when (val result = exportRepository.exportDataset(context, target.id, target.name, format)) {
                is ApiResult.Success -> {
                    ExportShareHelper.shareFile(context, result.data, format.mimeType, "خروجی ${target.name}")
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            showExportSheet = false,
                            exportTarget = null,
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

data class ListingsUiState(
    val listings: List<ListingDto> = emptyList(),
    val datasetId: String? = null,
    val datasetName: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isExporting: Boolean = false,
    val showExportSheet: Boolean = false,
    val exportMessage: String? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val query: String = "",
    val filters: ListingFilterState = ListingFilterState(),
    val neighborhoods: List<String> = emptyList(),
    val savedFilters: List<SavedFilterDto> = emptyList(),
    val activeSavedFilterId: Long? = null,
    val showSaveFilterDialog: Boolean = false,
    val saveFilterName: String = "",
)

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
    private val exportRepository: ExportRepository,
    private val extrasRepository: WorkspaceExtrasRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListingsUiState())
    val uiState: StateFlow<ListingsUiState> = _uiState.asStateFlow()

    private val routeDatasetId: String? = savedStateHandle.get<String>("datasetId")

    init {
        routeDatasetId?.let { resolveDatasetName(it) }
        loadSavedFilters()
    }

    private fun resolveDatasetName(datasetId: String) {
        viewModelScope.launch {
            when (val result = filingRepository.getDatasets()) {
                is ApiResult.Success -> {
                    val dataset = result.data.items.firstOrNull { it.id == datasetId }
                    _uiState.update { it.copy(datasetId = datasetId, datasetName = dataset?.name) }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun loadSavedFilters() {
        viewModelScope.launch {
            when (val result = extrasRepository.getSavedFilters(entity = "listings", includeNewCount = true)) {
                is ApiResult.Success -> _uiState.update { it.copy(savedFilters = result.data) }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun load(datasetId: String, reset: Boolean = true) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.page
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    datasetId = datasetId,
                    isLoading = reset && it.listings.isEmpty(),
                    isLoadingMore = !reset,
                    isRefreshing = reset && it.listings.isNotEmpty(),
                    error = null,
                )
            }
            val filterMap = state.filters.toQueryMap(datasetId = datasetId)
            when (val result = filingRepository.getListings(
                datasetId,
                query = state.query,
                page = page,
                filters = filterMap,
            )) {
                is ApiResult.Success -> {
                    val merged = if (reset) result.data.items else state.listings + result.data.items
                    val sorted = if (state.filters.sort.isBlank()) {
                        ListingAdvertiserUtils.sortPersonalFirst(merged)
                    } else {
                        merged
                    }
                    _uiState.update {
                        it.copy(
                            listings = sorted,
                            page = page,
                            hasMore = result.data.hasMore,
                            neighborhoods = result.data.neighborhoods.ifEmpty { it.neighborhoods },
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

    fun loadMore(datasetId: String) {
        if (!_uiState.value.hasMore || _uiState.value.isLoadingMore) return
        _uiState.update { it.copy(page = it.page + 1) }
        load(datasetId, reset = false)
    }

    fun refresh(datasetId: String) {
        _uiState.update { it.copy(page = 1) }
        load(datasetId, reset = true)
        loadSavedFilters()
    }

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }

    fun applyFilters(datasetId: String, filters: ListingFilterState) {
        _uiState.update {
            it.copy(filters = filters, page = 1, activeSavedFilterId = null)
        }
        load(datasetId, reset = true)
    }

    fun clearFilters(datasetId: String) {
        applyFilters(datasetId, ListingFilterState())
    }

    fun applySavedFilter(datasetId: String, filter: SavedFilterDto) {
        val params = filter.resolvedParams.toMutableMap()
        val q = params.remove("q").orEmpty()
        _uiState.update {
            it.copy(
                query = q.ifBlank { it.query },
                filters = ListingFilterState.fromParams(params),
                activeSavedFilterId = filter.id,
                page = 1,
            )
        }
        load(datasetId, reset = true)
    }

    fun openSaveFilterDialog() = _uiState.update {
        it.copy(showSaveFilterDialog = true, saveFilterName = "")
    }

    fun dismissSaveFilterDialog() = _uiState.update { it.copy(showSaveFilterDialog = false) }

    fun onSaveFilterNameChange(v: String) = _uiState.update { it.copy(saveFilterName = v) }

    fun saveCurrentFilter(datasetId: String) {
        val state = _uiState.value
        val name = state.saveFilterName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "نام فیلتر الزامی است") }
            return
        }
        val params = state.filters.toQueryMap(datasetId = datasetId).toMutableMap()
        if (state.query.isNotBlank()) params["q"] = state.query.trim()
        viewModelScope.launch {
            when (
                val result = extrasRepository.createSavedFilter(
                    SavedFilterCreateRequest(
                        name = name,
                        scope = "listings",
                        params = params,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showSaveFilterDialog = false,
                            successMessage = "فیلتر ذخیره شد",
                            activeSavedFilterId = result.data.id,
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

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null, exportMessage = null) }

    fun openExportSheet() = _uiState.update { it.copy(showExportSheet = true) }

    fun dismissExportSheet() = _uiState.update { it.copy(showExportSheet = false) }

    fun clearExportMessage() = _uiState.update { it.copy(exportMessage = null) }

    fun exportDataset(context: Context, format: ExportFormat) {
        val datasetId = _uiState.value.datasetId ?: return
        val datasetName = _uiState.value.datasetName ?: "dataset"
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            when (val result = exportRepository.exportDataset(context, datasetId, datasetName, format)) {
                is ApiResult.Success -> {
                    ExportShareHelper.shareFile(context, result.data, format.mimeType, "خروجی $datasetName")
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

data class FilingSearchUiState(
    val listings: List<ListingDto> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val filters: ListingFilterState = ListingFilterState(),
    val savedFilters: List<SavedFilterDto> = emptyList(),
    val activeSavedFilterId: Long? = null,
    val showSaveFilterDialog: Boolean = false,
    val saveFilterName: String = "",
)

@HiltViewModel
class FilingSearchViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
    private val extrasRepository: WorkspaceExtrasRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FilingSearchUiState())
    val uiState: StateFlow<FilingSearchUiState> = _uiState.asStateFlow()

    init {
        loadSavedFilters()
    }

    fun loadSavedFilters() {
        viewModelScope.launch {
            when (val result = extrasRepository.getSavedFilters(entity = "listings", includeNewCount = true)) {
                is ApiResult.Success -> _uiState.update { it.copy(savedFilters = result.data) }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }

    fun search(reset: Boolean = true) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.query.isBlank()) {
                _uiState.update { it.copy(listings = emptyList(), hasMore = false, error = null) }
                return@launch
            }
            val page = if (reset) 1 else state.page
            _uiState.update {
                it.copy(
                    isLoading = reset && it.listings.isEmpty(),
                    isLoadingMore = !reset,
                    isRefreshing = reset && it.listings.isNotEmpty(),
                    error = null,
                )
            }
            when (val result = filingRepository.searchListings(
                query = state.query,
                page = page,
                filters = state.filters.toQueryMap(),
            )) {
                is ApiResult.Success -> {
                    val merged = if (reset) result.data.items else state.listings + result.data.items
                    val sorted = if (state.filters.sort.isBlank()) {
                        ListingAdvertiserUtils.sortPersonalFirst(merged)
                    } else {
                        merged
                    }
                    _uiState.update {
                        it.copy(
                            listings = sorted,
                            page = page,
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
        if (!_uiState.value.hasMore || _uiState.value.isLoadingMore || _uiState.value.query.isBlank()) return
        _uiState.update { it.copy(page = it.page + 1) }
        search(reset = false)
    }

    fun refresh() {
        _uiState.update { it.copy(page = 1) }
        search(reset = true)
        loadSavedFilters()
    }

    fun applyFilters(filters: ListingFilterState) {
        _uiState.update { it.copy(filters = filters, page = 1, activeSavedFilterId = null) }
        search(reset = true)
    }

    fun clearFilters() {
        applyFilters(ListingFilterState())
    }

    fun setInitialQuery(query: String) {
        if (query.isNotBlank() && _uiState.value.query.isBlank()) {
            _uiState.update { it.copy(query = query) }
            search(reset = true)
        }
    }

    fun applySavedFilter(filter: SavedFilterDto) {
        val params = filter.resolvedParams.toMutableMap()
        val q = params.remove("q").orEmpty()
        _uiState.update {
            it.copy(
                query = q.ifBlank { it.query },
                filters = ListingFilterState.fromParams(params),
                activeSavedFilterId = filter.id,
                page = 1,
            )
        }
        search(reset = true)
    }

    fun openSaveFilterDialog() = _uiState.update { it.copy(showSaveFilterDialog = true, saveFilterName = "") }
    fun dismissSaveFilterDialog() = _uiState.update { it.copy(showSaveFilterDialog = false) }
    fun onSaveFilterNameChange(v: String) = _uiState.update { it.copy(saveFilterName = v) }

    fun saveCurrentFilter() {
        val state = _uiState.value
        val name = state.saveFilterName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "نام فیلتر الزامی است") }
            return
        }
        val params = state.filters.toQueryMap().toMutableMap()
        if (state.query.isNotBlank()) params["q"] = state.query.trim()
        viewModelScope.launch {
            when (
                val result = extrasRepository.createSavedFilter(
                    SavedFilterCreateRequest(
                        name = name,
                        scope = "listings",
                        params = params,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showSaveFilterDialog = false,
                            activeSavedFilterId = result.data.id,
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
}
