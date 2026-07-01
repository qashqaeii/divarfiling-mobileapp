package ir.divarfiling.mobile.feature.extract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.license.ExtractLightLimits
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.datastore.ExtractPreferences
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.places.PlaceOption
import ir.divarfiling.mobile.core.places.PlaceSearchResult
import ir.divarfiling.mobile.core.places.PlacesRepository
import ir.divarfiling.mobile.core.network.ExtractionUploadData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.AuthRepository
import ir.divarfiling.mobile.data.repository.DashboardRepository
import ir.divarfiling.mobile.data.repository.ExtractGateResult
import ir.divarfiling.mobile.data.repository.ExtractionRepository
import ir.divarfiling.mobile.data.repository.ExtractionScheduleRepository
import ir.divarfiling.mobile.feature.extract.divar.ExtractAdvancedFilters
import ir.divarfiling.mobile.feature.extract.divar.ExtractFilters
import ir.divarfiling.mobile.feature.extract.divar.OutputNameHint
import ir.divarfiling.mobile.core.notifications.DfNotificationHelper
import ir.divarfiling.mobile.feature.extract.schedule.ScheduleNotificationCopy
import ir.divarfiling.mobile.feature.extract.schedule.ScheduleWorkManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExtractUiState(
    val license: LicenseState = LicenseState(),
    val transactionType: String = ExtractCategories.transactionTypes.first().label,
    val subcategoryLabel: String = ExtractCategories.transactionTypes.first().subcategories.first().label,
    val provinceName: String = "",
    val cityId: String = "1",
    val cityName: String = "تهران",
    val districtId: String = "",
    val sort: String = "sort_date",
    val maxItems: Int = 50,
    val advertiserFilter: String = "all",
    val showAdvanced: Boolean = false,
    val priceMin: String = "",
    val priceMax: String = "",
    val depositMin: String = "",
    val depositMax: String = "",
    val rentMin: String = "",
    val rentMax: String = "",
    val areaMin: String = "",
    val areaMax: String = "",
    val yearMin: String = "",
    val yearMax: String = "",
    val rooms: String = "",
    val provinces: List<String> = emptyList(),
    val cities: List<PlaceOption> = emptyList(),
    val districts: List<PlaceOption> = emptyList(),
    val isRunning: Boolean = false,
    val progressCurrent: Int = 0,
    val progressTotal: Int = 0,
    val message: String? = null,
    val error: String? = null,
    val lastDatasetId: String? = null,
    val lastUploadStats: ExtractionUploadData? = null,
    val remainingToday: Int? = null,
    val extractionsToday: Int? = null,
    val extractionsDailyLimit: Int? = null,
    val canExtractNow: Boolean = true,
    val scheduleIntervalHours: Double = 6.0,
    val gateMessage: String? = null,
    val placeQuery: String = "",
    val searchQuery: String = "",
    val placeSuggestions: List<PlaceSearchResult> = emptyList(),
    val userName: String = "",
    val notificationBadgeCount: Int = 0,
    val lastExtractionDurationMinutes: Double? = null,
    val lastSuccessfulIngestedCount: Int? = null,
    val averageExtractionDurationMinutes: Double? = null,
    val lastExtractionAtMs: Long? = null,
) {
    val categorySlug: String
        get() = ExtractCategories.slugFor(transactionType, subcategoryLabel).orEmpty()

    val isRent: Boolean
        get() = ExtractCategories.isRentCategory(categorySlug)
}

@HiltViewModel
class ExtractViewModel @Inject constructor(
    private val extractionRepository: ExtractionRepository,
    private val scheduleRepository: ExtractionScheduleRepository,
    private val placesRepository: PlacesRepository,
    private val extractPreferences: ExtractPreferences,
    private val sessionStore: SessionStore,
    private val dashboardRepository: DashboardRepository,
    authRepository: AuthRepository,
    private val notificationHelper: DfNotificationHelper,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExtractUiState())
    val uiState: StateFlow<ExtractUiState> = _uiState.asStateFlow()

    private var job: Job? = null
    @Volatile private var cancelled = false
    private var extractionStartedAt: Long? = null

    init {
        viewModelScope.launch {
            sessionStore.currentUser.collect { user ->
                _uiState.update {
                    it.copy(userName = user?.fullName?.substringBefore(" ") ?: "کاربر")
                }
            }
        }
        viewModelScope.launch {
            when (val result = dashboardRepository.getDashboard()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(notificationBadgeCount = result.data.notificationsUnread) }
                is ApiResult.Error -> Unit
            }
        }
        viewModelScope.launch {
            authRepository.licenseState.collect { license ->
                _uiState.update { state ->
                    val gate = extractionRepository.gateFromLicense(license)
                    state.copy(
                        license = license,
                        gateMessage = if (gate is ExtractGateResult.Denied) gate.message else null,
                    )
                }
            }
        }
        viewModelScope.launch { loadPlaces() }
        viewModelScope.launch {
            val savedInterval = extractPreferences.getScheduleIntervalHours()
            val sessionStats = extractPreferences.getSessionStats()
            _uiState.update {
                it.copy(
                    scheduleIntervalHours = savedInterval,
                    lastSuccessfulIngestedCount = sessionStats.lastSuccessfulCount.takeIf { c -> c > 0 },
                    averageExtractionDurationMinutes = sessionStats.averageDurationMinutes
                        .takeIf { avg -> avg > 0 },
                    lastExtractionAtMs = sessionStats.lastExtractionAtMs.takeIf { ts -> ts > 0 },
                )
            }
        }
        viewModelScope.launch {
            extractionRepository.getLimits()?.let { limits ->
                _uiState.update {
                    it.copy(
                        maxItems = it.maxItems.coerceAtMost(limits.maxItems),
                        remainingToday = limits.remainingToday,
                        extractionsToday = limits.extractionsToday,
                        extractionsDailyLimit = limits.extractionsDailyLimit,
                        canExtractNow = limits.canExtractNow,
                    )
                }
            }
        }
        refreshGate()
    }

    private suspend fun loadPlaces() {
        placesRepository.ensureLoaded()
        val provinces = placesRepository.provinceNames()
        val tehranProvince = placesRepository.provinceForCity("1")?.name
            ?: provinces.firstOrNull().orEmpty()
        val cities = placesRepository.citiesForProvince(tehranProvince)
        _uiState.update {
            it.copy(
                provinces = provinces,
                provinceName = tehranProvince,
                cities = cities,
                cityId = "1",
                cityName = "تهران",
                districts = placesRepository.districtsForCity("1"),
            )
        }
    }

    fun refreshGate() {
        viewModelScope.launch {
            when (val gate = extractionRepository.checkExtractGate()) {
                is ExtractGateResult.Denied ->
                    _uiState.update { it.copy(gateMessage = gate.message) }
                ExtractGateResult.Allowed ->
                    _uiState.update { it.copy(gateMessage = null) }
            }
        }
    }

    fun onTransactionTypeChange(value: String) {
        val subs = ExtractCategories.transactionTypes.firstOrNull { it.label == value }
            ?.subcategories.orEmpty()
        _uiState.update {
            it.copy(
                transactionType = value,
                subcategoryLabel = subs.firstOrNull()?.label ?: it.subcategoryLabel,
            )
        }
    }

    fun onSubcategoryChange(value: String) = _uiState.update { it.copy(subcategoryLabel = value) }

    fun onProvinceChange(value: String) {
        val cities = placesRepository.citiesForProvince(value)
        val firstCity = cities.firstOrNull()
        _uiState.update {
            it.copy(
                provinceName = value,
                cities = cities,
                cityId = firstCity?.id ?: it.cityId,
                cityName = firstCity?.name ?: it.cityName,
                districtId = "",
                districts = firstCity?.let { c -> placesRepository.districtsForCity(c.id) }.orEmpty(),
            )
        }
    }

    fun onCityChange(city: PlaceOption) {
        _uiState.update {
            it.copy(
                cityId = city.id,
                cityName = city.name,
                districtId = "",
                districts = placesRepository.districtsForCity(city.id),
            )
        }
    }

    fun onDistrictChange(districtId: String) = _uiState.update { it.copy(districtId = districtId) }
    fun onSortChange(value: String) = _uiState.update { it.copy(sort = value) }
    fun onAdvertiserFilterChange(value: String) = _uiState.update { it.copy(advertiserFilter = value) }
    fun onMaxItemsChange(value: Int) = _uiState.update {
        it.copy(maxItems = value.coerceIn(0, ExtractLightLimits.MAX_ITEMS))
    }
    fun toggleAdvanced() = _uiState.update { it.copy(showAdvanced = !it.showAdvanced) }

    fun onPriceMinChange(v: String) = _uiState.update { it.copy(priceMin = v) }
    fun onPriceMaxChange(v: String) = _uiState.update { it.copy(priceMax = v) }
    fun onDepositMinChange(v: String) = _uiState.update { it.copy(depositMin = v) }
    fun onDepositMaxChange(v: String) = _uiState.update { it.copy(depositMax = v) }
    fun onRentMinChange(v: String) = _uiState.update { it.copy(rentMin = v) }
    fun onRentMaxChange(v: String) = _uiState.update { it.copy(rentMax = v) }
    fun onAreaMinChange(v: String) = _uiState.update { it.copy(areaMin = v) }
    fun onAreaMaxChange(v: String) = _uiState.update { it.copy(areaMax = v) }
    fun onYearMinChange(v: String) = _uiState.update { it.copy(yearMin = v) }
    fun onYearMaxChange(v: String) = _uiState.update { it.copy(yearMax = v) }
    fun onRoomsChange(v: String) = _uiState.update { it.copy(rooms = v) }
    fun onSearchQueryChange(v: String) = _uiState.update { it.copy(searchQuery = v) }

    fun onScheduleIntervalSelect(hours: Double) {
        _uiState.update { it.copy(scheduleIntervalHours = hours) }
        viewModelScope.launch { extractPreferences.setScheduleIntervalHours(hours) }
    }

    fun onPlaceQueryChange(query: String) {
        _uiState.update { it.copy(placeQuery = query) }
    }

    fun onPlaceSearchDebounced(query: String) {
        val suggestions = if (query.trim().length >= 2) {
            placesRepository.searchPlaces(query)
        } else {
            emptyList()
        }
        _uiState.update { it.copy(placeSuggestions = suggestions) }
    }

    fun onPlaceSuggestionSelect(result: PlaceSearchResult) {
        val resolved = placesRepository.resolvePlace(result.matchedText)
            ?: return
        val selection = placesRepository.applyResolved(resolved)
        _uiState.update {
            it.copy(
                provinceName = selection.provinceName,
                cityId = selection.cityId,
                cityName = selection.cityName,
                districtId = selection.districtId,
                cities = selection.cities,
                districts = selection.districts,
                placeQuery = result.matchedText,
                placeSuggestions = emptyList(),
            )
        }
    }

    fun createSchedule() {
        val state = _uiState.value
        if (!state.license.canUseLightExtract) return
        viewModelScope.launch {
            when (val result = scheduleRepository.createSchedule(
                filters = buildFiltersFromState(state),
                intervalHours = state.scheduleIntervalHours,
            )) {
                is ApiResult.Success -> {
                    ScheduleWorkManager.registerPeriodicPolling(appContext)
                    ScheduleWorkManager.enqueueDueRuns(appContext, result.data.id)
                    val (title, body) = ScheduleNotificationCopy.createdMessage(result.data)
                    notificationHelper.showNotification(
                        id = ScheduleNotificationCopy.NOTIFICATION_ID_BASE + result.data.id.toInt(),
                        title = title,
                        body = body,
                        deepLink = ScheduleNotificationCopy.DEEP_LINK,
                    )
                    _uiState.update { it.copy(message = body) }
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
            }
        }
    }

    private fun buildFiltersFromState(state: ExtractUiState): ExtractFilters {
        val slug = state.categorySlug
        val districtIds = state.districtId.takeIf { it.isNotBlank() }?.let { listOf(it) }.orEmpty()
        val selectedDistrict = state.districtId.takeIf { it.isNotBlank() }
            ?.let { id -> state.districts.firstOrNull { it.id == id } }
        val districtNames = selectedDistrict?.name?.let { listOf(it) }.orEmpty()
        val districtSlugs = selectedDistrict?.slug?.takeIf { it.isNotBlank() }?.let { listOf(it) }.orEmpty()
        val citySlug = state.cities.firstOrNull { it.id == state.cityId }?.slug?.takeIf { it.isNotBlank() }
        val rooms = state.rooms.split(',', '،').map { it.trim() }.filter { it.isNotEmpty() }
        return ExtractFilters(
            cityId = state.cityId,
            cityName = state.cityName,
            provinceName = state.provinceName.takeIf { it.isNotBlank() },
            districtIds = districtIds,
            districtNames = districtNames,
            districtSlugs = districtSlugs,
            citySlug = citySlug,
            category = slug,
            categoryLabel = state.subcategoryLabel,
            transactionTypeLabel = state.transactionType,
            sort = state.sort,
            maxItems = state.maxItems,
            outputNameHint = null,
            searchQuery = state.searchQuery.trim().takeIf { it.isNotEmpty() },
            advanced = ExtractAdvancedFilters(
                priceMin = state.priceMin.toLongOrNull(),
                priceMax = state.priceMax.toLongOrNull(),
                depositMin = state.depositMin.toLongOrNull(),
                depositMax = state.depositMax.toLongOrNull(),
                rentMin = state.rentMin.toLongOrNull(),
                rentMax = state.rentMax.toLongOrNull(),
                areaMin = state.areaMin.toIntOrNull(),
                areaMax = state.areaMax.toIntOrNull(),
                yearMin = state.yearMin.toIntOrNull(),
                yearMax = state.yearMax.toIntOrNull(),
                rooms = rooms,
                advertiserFilter = state.advertiserFilter,
            ),
        ).let { base -> base.copy(outputNameHint = OutputNameHint.build(base)) }
    }

    fun startExtraction() {
        val state = _uiState.value
        if (!state.license.canUseLightExtract) {
            _uiState.update {
                it.copy(error = state.gateMessage ?: "لایسنس فعال برای استخراج نیاز است")
            }
            return
        }
        val slug = state.categorySlug
        if (slug.isBlank()) {
            _uiState.update { it.copy(error = "دسته‌بندی نامعتبر است") }
            return
        }
        cancelled = false
        job?.cancel()
        job = viewModelScope.launch {
            extractionStartedAt = System.currentTimeMillis()
            _uiState.update {
                it.copy(isRunning = true, error = null, message = null, progressCurrent = 0, progressTotal = 0)
            }
            val filters = buildFiltersFromState(state)
            when (
                val result = extractionRepository.runLightExtraction(
                    filters = filters,
                    onProgress = { current, total ->
                        _uiState.update { it.copy(progressCurrent = current, progressTotal = total) }
                    },
                    isCancelled = { cancelled },
                )
            ) {
                is ApiResult.Success -> {
                    val stats = result.data
                    val mergeNote = if (stats.datasetMerged) " (ادغام با فایلینگ موجود)" else ""
                    val durationMs = extractionStartedAt?.let { started ->
                        (System.currentTimeMillis() - started).coerceAtLeast(0)
                    } ?: 0L
                    val durationMinutes = durationMs / 60_000.0
                    if (stats.ingestedCount > 0) {
                        extractPreferences.recordSuccessfulExtraction(
                            ingestedCount = stats.ingestedCount,
                            durationMinutes = durationMinutes.coerceAtLeast(0.05),
                        )
                    }
                    val sessionStats = extractPreferences.getSessionStats()
                    _uiState.update {
                        it.copy(
                            isRunning = false,
                            message = "آپلود موفق — ${stats.ingestedCount} آگهی پردازش شد$mergeNote",
                            lastDatasetId = stats.datasetId,
                            lastUploadStats = stats,
                            lastExtractionDurationMinutes = durationMinutes.takeIf { it > 0 },
                            lastSuccessfulIngestedCount = sessionStats.lastSuccessfulCount
                                .takeIf { count -> count > 0 },
                            averageExtractionDurationMinutes = sessionStats.averageDurationMinutes
                                .takeIf { avg -> avg > 0 },
                            lastExtractionAtMs = sessionStats.lastExtractionAtMs.takeIf { ts -> ts > 0 },
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isRunning = false, error = result.message)
                }
            }
        }
    }

    fun cancel() {
        cancelled = true
        _uiState.update { it.copy(isRunning = false, message = "لغو شد") }
    }
}
