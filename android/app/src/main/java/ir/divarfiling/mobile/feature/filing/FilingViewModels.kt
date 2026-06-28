package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.data.repository.DashboardRepository
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
    val error: String? = null,
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
)

@HiltViewModel
class DatasetsViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
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
}

data class ListingsUiState(
    val listings: List<ListingDto> = emptyList(),
    val datasetName: String? = null,
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
class ListingsViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
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
                    val name = result.data.items.firstOrNull { it.id == datasetId }?.name
                    _uiState.update { it.copy(datasetName = name) }
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
