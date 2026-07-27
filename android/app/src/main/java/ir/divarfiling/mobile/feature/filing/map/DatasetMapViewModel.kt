package ir.divarfiling.mobile.feature.filing.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.DatasetMapData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatasetMapUiState(
    val datasetId: String = "",
    val mapData: DatasetMapData? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
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
            _uiState.update {
                it.copy(
                    isLoading = it.mapData == null && !it.isRefreshing,
                    error = null,
                )
            }
            when (val result = repository.getDatasetMap(datasetId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(mapData = result.data, isLoading = false, isRefreshing = false)
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
