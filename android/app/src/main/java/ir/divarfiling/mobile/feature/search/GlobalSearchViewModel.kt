package ir.divarfiling.mobile.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.GlobalSearchGroupDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.SearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GlobalSearchUiState(
    val query: String = "",
    val groups: List<GlobalSearchGroupDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val minQueryLength: Int = 2,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val repository: SearchRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GlobalSearchUiState())
    val uiState: StateFlow<GlobalSearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        queryFlow
            .debounce(350)
            .distinctUntilChanged()
            .onEach { q -> runSearch(q, refreshing = false) }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value, error = null) }
        queryFlow.value = value.trim()
    }

    fun refresh() {
        runSearch(_uiState.value.query, refreshing = true)
    }

    private fun runSearch(query: String, refreshing: Boolean) {
        viewModelScope.launch {
            if (query.length < _uiState.value.minQueryLength) {
                _uiState.update {
                    it.copy(groups = emptyList(), isLoading = false, isRefreshing = false, error = null)
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = !refreshing && it.groups.isEmpty(),
                    isRefreshing = refreshing,
                    error = null,
                )
            }
            when (val result = repository.globalSearch(query)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        groups = result.data.groups,
                        minQueryLength = result.data.minQueryLength,
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
}
