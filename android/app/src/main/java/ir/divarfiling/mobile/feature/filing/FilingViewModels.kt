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
    val query: String = "",
    val priceMin: Long? = null,
    val priceMax: Long? = null,
    val areaMin: Int? = null,
    val areaMax: Int? = null,
    val rooms: Int? = null,
)

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
    private val exportRepository: ExportRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListingsUiState())
    val uiState: StateFlow<ListingsUiState> = _uiState.asStateFlow()

    private val routeDatasetId: String? = savedStateHandle.get<String>("datasetId")

    init {
        routeDatasetId?.let { resolveDatasetName(it) }
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
            when (val result = filingRepository.getListings(
                datasetId,
                query = state.query,
                page = page,
                priceMin = state.priceMin,
                priceMax = state.priceMax,
                areaMin = state.areaMin,
                areaMax = state.areaMax,
                rooms = state.rooms,
            )) {
                is ApiResult.Success -> {
                    val merged = if (reset) result.data.items else state.listings + result.data.items
                    val sorted = ListingAdvertiserUtils.sortPersonalFirst(merged)
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

    fun loadMore(datasetId: String) {
        if (!_uiState.value.hasMore || _uiState.value.isLoadingMore) return
        _uiState.update { it.copy(page = it.page + 1) }
        load(datasetId, reset = false)
    }

    fun refresh(datasetId: String) {
        _uiState.update { it.copy(page = 1) }
        load(datasetId, reset = true)
    }

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }

    fun applyFilters(
        datasetId: String,
        priceMin: Long?,
        priceMax: Long?,
        areaMin: Int?,
        areaMax: Int?,
        rooms: Int?,
    ) {
        _uiState.update {
            it.copy(
                priceMin = priceMin,
                priceMax = priceMax,
                areaMin = areaMin,
                areaMax = areaMax,
                rooms = rooms,
                page = 1,
            )
        }
        load(datasetId, reset = true)
    }

    fun clearFilters(datasetId: String) {
        applyFilters(datasetId, null, null, null, null, null)
    }

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
    val priceMin: Long? = null,
    val priceMax: Long? = null,
    val areaMin: Int? = null,
    val areaMax: Int? = null,
    val rooms: Int? = null,
)

@HiltViewModel
class FilingSearchViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FilingSearchUiState())
    val uiState: StateFlow<FilingSearchUiState> = _uiState.asStateFlow()

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
                priceMin = state.priceMin,
                priceMax = state.priceMax,
                areaMin = state.areaMin,
                areaMax = state.areaMax,
                rooms = state.rooms,
            )) {
                is ApiResult.Success -> {
                    val merged = if (reset) result.data.items else state.listings + result.data.items
                    val sorted = ListingAdvertiserUtils.sortPersonalFirst(merged)
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
    }

    fun applyFilters(priceMin: Long?, priceMax: Long?, areaMin: Int?, areaMax: Int?, rooms: Int?) {
        _uiState.update {
            it.copy(priceMin = priceMin, priceMax = priceMax, areaMin = areaMin, areaMax = areaMax, rooms = rooms, page = 1)
        }
        search(reset = true)
    }

    fun clearFilters() {
        applyFilters(null, null, null, null, null)
    }

    fun setInitialQuery(query: String) {
        if (query.isNotBlank() && _uiState.value.query.isBlank()) {
            _uiState.update { it.copy(query = query) }
            search(reset = true)
        }
    }
}
