package ir.divarfiling.mobile.feature.filing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.DatasetInsightsDto
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
    val insights: DatasetInsightsDto? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedLevel: Int = 0,
    val selectedL2Tab: Int = 0,
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

    fun selectLevel(level: Int) {
        _uiState.update { it.copy(selectedLevel = level, selectedL2Tab = 0) }
    }

    fun selectL2Tab(index: Int) {
        _uiState.update { it.copy(selectedL2Tab = index) }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.insights == null, error = null)
            }
            when (val result = repository.getDatasetInsights(datasetId)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            insights = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.message,
                        )
                    }
                }
            }
        }
    }
}
