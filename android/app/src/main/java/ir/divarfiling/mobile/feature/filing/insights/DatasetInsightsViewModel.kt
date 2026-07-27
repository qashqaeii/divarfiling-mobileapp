package ir.divarfiling.mobile.feature.filing.insights

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.DatasetInsightsData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.FilingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatasetInsightsUiState(
    val datasetId: String = "",
    val insights: DatasetInsightsData? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
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

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.insights == null && !it.isRefreshing,
                    error = null,
                )
            }
            when (val result = repository.getDatasetInsights(datasetId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(insights = result.data, isLoading = false, isRefreshing = false)
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
