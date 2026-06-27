package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.DatasetMapDto
import ir.divarfiling.mobile.core.network.MapMarkerDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MapDisplayMode(val id: String, val label: String) {
    Markers("markers", "آگهی‌دهنده"),
    Market("market", "بازار"),
    Value("value", "ارزش"),
    FullDeposit("full_deposit", "رهن کامل"),
}

enum class SellerFilter(val label: String) {
    All("همه"),
    Consultant("مشاور"),
    Personal("شخصی"),
}

data class DatasetMapUiState(
    val datasetId: String = "",
    val mapData: DatasetMapDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val displayMode: MapDisplayMode = MapDisplayMode.Markers,
    val sellerFilter: SellerFilter = SellerFilter.All,
    val underMarketOnly: Boolean = false,
    val priceMin: Long? = null,
    val priceMax: Long? = null,
    val areaMin: Int? = null,
    val areaMax: Int? = null,
    val selectedMarker: MapMarkerDto? = null,
    val filteredMarkers: List<MapMarkerDto> = emptyList(),
)

@HiltViewModel
class DatasetMapViewModel @Inject constructor(
    private val repository: FilingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val datasetId: String = savedStateHandle.get<String>("datasetId").orEmpty()

    private val _uiState = MutableStateFlow(DatasetMapUiState(datasetId = datasetId))
    val uiState: StateFlow<DatasetMapUiState> = _uiState.asStateFlow()

    init {
        if (datasetId.isNotBlank()) load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.mapData == null, error = null) }
            when (val result = repository.getDatasetMap(datasetId)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        val data = result.data
                        val defaultMode = if (data.config?.isRent == true) {
                            MapDisplayMode.FullDeposit
                        } else {
                            MapDisplayMode.Market
                        }
                        state.copy(
                            mapData = data,
                            isLoading = false,
                            displayMode = defaultMode,
                        ).withFilteredMarkers()
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun setDisplayMode(mode: MapDisplayMode) {
        _uiState.update { it.copy(displayMode = mode).withFilteredMarkers() }
    }

    fun setSellerFilter(filter: SellerFilter) {
        _uiState.update { it.copy(sellerFilter = filter).withFilteredMarkers() }
    }

    fun setUnderMarketOnly(enabled: Boolean) {
        _uiState.update { it.copy(underMarketOnly = enabled).withFilteredMarkers() }
    }

    fun setPriceRange(min: Long?, max: Long?) {
        _uiState.update { it.copy(priceMin = min, priceMax = max).withFilteredMarkers() }
    }

    fun setAreaRange(min: Int?, max: Int?) {
        _uiState.update { it.copy(areaMin = min, areaMax = max).withFilteredMarkers() }
    }

    fun selectMarker(marker: MapMarkerDto?) {
        _uiState.update { it.copy(selectedMarker = marker) }
    }

    private fun DatasetMapUiState.withFilteredMarkers(): DatasetMapUiState {
        val markers = mapData?.markers.orEmpty()
        val filtered = markers.filter { marker ->
            when (sellerFilter) {
                SellerFilter.All -> true
                SellerFilter.Consultant -> marker.isConsultant
                SellerFilter.Personal -> !marker.isConsultant
            }
        }.filter { marker ->
            if (!underMarketOnly) true else marker.marketTier == "under"
        }.filter { marker ->
            val value = marker.filterValue ?: marker.fullDeposit ?: 0L
            (priceMin == null || value >= priceMin) && (priceMax == null || value <= priceMax)
        }.filter { marker ->
            val area = marker.area ?: 0
            (areaMin == null || area >= areaMin) && (areaMax == null || area <= areaMax)
        }
        return copy(filteredMarkers = filtered)
    }
}
