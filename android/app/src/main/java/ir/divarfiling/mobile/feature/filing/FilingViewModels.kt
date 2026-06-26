package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatasetsUiState(
    val datasets: List<DatasetDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DatasetsViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DatasetsUiState())
    val uiState: StateFlow<DatasetsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.datasets.isEmpty(), error = null) }
            when (val result = filingRepository.getDatasets()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(datasets = result.data, isLoading = false, isRefreshing = false)
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
}

data class ListingsUiState(
    val listings: List<ListingDto> = emptyList(),
    val datasetName: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val query: String = "",
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
                    val name = result.data.firstOrNull { it.id == datasetId }?.name
                    _uiState.update { it.copy(datasetName = name) }
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    fun load(datasetId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.listings.isEmpty() && !it.isRefreshing,
                    isRefreshing = it.listings.isNotEmpty(),
                    error = null,
                )
            }
            when (val result = filingRepository.getListings(datasetId, _uiState.value.query)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        listings = result.data,
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

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }
}
