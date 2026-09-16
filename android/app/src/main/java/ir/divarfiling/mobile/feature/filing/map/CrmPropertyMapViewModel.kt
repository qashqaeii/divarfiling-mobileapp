package ir.divarfiling.mobile.feature.filing.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.CrmPropertyMapData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.DealsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrmPropertyMapUiState(
    val mapData: CrmPropertyMapData? = null,
    val mapViewport: MapViewportState? = null,
    val selectedPropertyId: Long? = null,
    val isInitialLoading: Boolean = false,
    val isMapLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,
)

@HiltViewModel
class CrmPropertyMapViewModel @Inject constructor(
    private val repository: DealsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CrmPropertyMapUiState())
    val uiState: StateFlow<CrmPropertyMapUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null
    private var loadJob: Job? = null
    private var loadGeneration = 0
    private var lastFetchedViewport: MapViewportState? = null

    fun onViewportChanged(viewport: MapViewportState) {
        _uiState.update { it.copy(mapViewport = viewport) }
        if (!ViewportLoadGate.shouldFetch(lastFetchedViewport, viewport)) return
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(450)
            val latest = _uiState.value.mapViewport ?: return@launch
            if (!ViewportLoadGate.shouldFetch(lastFetchedViewport, latest)) return@launch
            fetchForViewport(latest, refreshing = false)
        }
    }

    fun refresh() {
        val viewport = _uiState.value.mapViewport
        if (viewport == null) {
            _uiState.update { it.copy(isRefreshing = true) }
            return
        }
        fetchForViewport(viewport, refreshing = true)
    }

    fun retryAfterError() {
        _uiState.update { it.copy(snackbarMessage = null) }
        val viewport = _uiState.value.mapViewport ?: return
        fetchForViewport(viewport, refreshing = false)
    }

    fun selectProperty(propertyId: Long?) {
        _uiState.update { it.copy(selectedPropertyId = propertyId) }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private fun fetchForViewport(viewport: MapViewportState, refreshing: Boolean) {
        loadJob?.cancel()
        val requestId = ++loadGeneration
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInitialLoading = it.mapData == null && !refreshing,
                    isMapLoading = it.mapData != null || refreshing,
                    isRefreshing = refreshing,
                    error = if (it.mapData == null) null else it.error,
                )
            }
            val params = viewport.toQueryParams()
            when (val result = repository.getCrmPropertyMap(params)) {
                is ApiResult.Success -> {
                    if (requestId != loadGeneration) return@launch
                    lastFetchedViewport = viewport
                    _uiState.update {
                        it.copy(
                            mapData = result.data,
                            isInitialLoading = false,
                            isMapLoading = false,
                            isRefreshing = false,
                            error = null,
                        )
                    }
                }
                is ApiResult.Error -> {
                    if (requestId != loadGeneration) return@launch
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isMapLoading = false,
                            isRefreshing = false,
                            error = if (it.mapData == null) result.message else it.error,
                            snackbarMessage = if (it.mapData != null) result.message else it.snackbarMessage,
                        )
                    }
                }
            }
        }
    }
}
