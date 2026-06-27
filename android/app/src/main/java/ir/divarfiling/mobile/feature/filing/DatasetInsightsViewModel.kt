package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class DatasetInsightsUiState(
    val datasetId: String = "",
    val datasetName: String? = null,
    val listings: List<ListingDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val avgPrice: Long? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val avgArea: Int? = null,
    val mapPoints: Int = 0,
    val centerLat: Double? = null,
    val centerLng: Double? = null,
    val roomDistribution: Map<Int, Int> = emptyMap(),
)

@HiltViewModel
class DatasetInsightsViewModel @Inject constructor(
    private val repository: FilingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val datasetId: String = savedStateHandle.get<String>("datasetId").orEmpty()

    private val _uiState = MutableStateFlow(DatasetInsightsUiState(datasetId = datasetId))
    val uiState: StateFlow<DatasetInsightsUiState> = _uiState.asStateFlow()

    init {
        if (datasetId.isNotBlank()) load()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.listings.isEmpty(), error = null)
            }
            val all = mutableListOf<ListingDto>()
            var page = 1
            var hasMore = true
            var name: String? = _uiState.value.datasetName
            while (hasMore && page <= 5) {
                when (val result = repository.getListings(datasetId, page = page, pageSize = 100)) {
                    is ApiResult.Success -> {
                        if (page == 1 && result.data.items.isNotEmpty()) {
                            name = result.data.items.first().datasetName ?: name
                        }
                        all += result.data.items
                        hasMore = result.data.hasMore
                        page++
                    }
                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, isRefreshing = false, error = result.message)
                        }
                        return@launch
                    }
                }
            }
            _uiState.update {
                it.copy(
                    listings = all,
                    datasetName = name,
                    isLoading = false,
                    isRefreshing = false,
                    error = null,
                ).withStats()
            }
        }
    }

    private fun DatasetInsightsUiState.withStats(): DatasetInsightsUiState {
        val prices = listings.mapNotNull { it.price }.filter { it > 0 }
        val areas = listings.mapNotNull { it.area }.filter { it > 0 }
        val coords = listings.mapNotNull { l ->
            val lat = l.latitude
            val lng = l.longitude
            if (lat != null && lng != null) lat to lng else null
        }
        val rooms = listings.mapNotNull { it.rooms }
            .groupingBy { it }
            .eachCount()
        return copy(
            avgPrice = prices.takeIf { it.isNotEmpty() }?.average()?.roundToInt()?.toLong(),
            minPrice = prices.minOrNull(),
            maxPrice = prices.maxOrNull(),
            avgArea = areas.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
            mapPoints = coords.size,
            centerLat = coords.takeIf { it.isNotEmpty() }?.map { it.first }?.average(),
            centerLng = coords.takeIf { it.isNotEmpty() }?.map { it.second }?.average(),
            roomDistribution = rooms,
        )
    }
}
