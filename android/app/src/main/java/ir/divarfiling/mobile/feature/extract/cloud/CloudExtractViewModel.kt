package ir.divarfiling.mobile.feature.extract.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.CloudExtractionCreateRequest
import ir.divarfiling.mobile.core.network.CloudExtractionJobDto
import ir.divarfiling.mobile.core.places.PlaceOption
import ir.divarfiling.mobile.core.places.PlacesRepository
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import ir.divarfiling.mobile.feature.extract.ExtractCategories
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CloudExtractUiState(
    val jobs: List<CloudExtractionJobDto> = emptyList(),
    val cityId: String = "1",
    val cityName: String = "تهران",
    val category: String = "",
    val categoryLabel: String = ExtractCategories.transactionTypes.first().subcategories.first().label,
    val maxItems: String = "200",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val cities: List<PlaceOption> = emptyList(),
    val activeJobId: Long? = null,
)

@HiltViewModel
class CloudExtractViewModel @Inject constructor(
    private val repository: WorkspaceExtrasRepository,
    private val placesRepository: PlacesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CloudExtractUiState())
    val uiState: StateFlow<CloudExtractUiState> = _uiState.asStateFlow()
    private var pollJob: Job? = null

    init {
        val defaultCategory = ExtractCategories.slugFor(
            ExtractCategories.transactionTypes.first().label,
            ExtractCategories.transactionTypes.first().subcategories.first().label,
        ).orEmpty()
        _uiState.update { it.copy(category = defaultCategory) }
        viewModelScope.launch {
            placesRepository.ensureLoaded()
            val cities = placesRepository.citiesForProvince("تهران")
            _uiState.update { it.copy(cities = cities) }
        }
        loadJobs()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.jobs.isEmpty() && !it.isRefreshing, error = null) }
            when (val result = repository.listCloudExtractions()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(jobs = result.data, isLoading = false, isRefreshing = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadJobs()
    }

    fun onCityChange(city: PlaceOption) = _uiState.update {
        it.copy(cityId = city.id, cityName = city.name)
    }

    fun onCategoryLabelChange(label: String) {
        val slug = ExtractCategories.slugFor(ExtractCategories.transactionTypes.first().label, label).orEmpty()
        _uiState.update { it.copy(categoryLabel = label, category = slug) }
    }

    fun onMaxItemsChange(value: String) = _uiState.update { it.copy(maxItems = value.filter { ch -> ch.isDigit() }) }

    fun createJob() {
        val state = _uiState.value
        if (state.category.isBlank()) {
            _uiState.update { it.copy(error = "دسته‌بندی نامعتبر است") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (
                val result = repository.createCloudExtraction(
                    CloudExtractionCreateRequest(
                        cityId = state.cityId,
                        cityName = state.cityName,
                        category = state.category,
                        categoryLabel = state.categoryLabel,
                        maxItems = state.maxItems.toIntOrNull()?.coerceIn(10, 500) ?: 200,
                    ),
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, successMessage = "درخواست استخراج ثبت شد", activeJobId = result.data.id)
                    }
                    loadJobs()
                    startPolling(result.data.id)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun startPolling(jobId: Long) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            repeat(60) {
                delay(3_000)
                when (val result = repository.getCloudExtraction(jobId)) {
                    is ApiResult.Success -> {
                        val job = result.data
                        _uiState.update { state ->
                            state.copy(
                                jobs = state.jobs.map { if (it.id == job.id) job else it }
                                    .ifEmpty { listOf(job) },
                            )
                        }
                        if (job.status == "success" || job.status == "failed") {
                            loadJobs()
                            return@launch
                        }
                    }
                    is ApiResult.Error -> Unit
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
