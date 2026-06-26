package ir.divarfiling.mobile.feature.filing

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
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = filingRepository.getDatasets()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(datasets = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}

data class ListingsUiState(
    val listings: List<ListingDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
)

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val filingRepository: FilingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListingsUiState())
    val uiState: StateFlow<ListingsUiState> = _uiState.asStateFlow()

    fun load(datasetId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = filingRepository.getListings(datasetId, _uiState.value.query)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(listings = result.data, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun onQueryChange(q: String) = _uiState.update { it.copy(query = q) }
}
