package ir.divarfiling.mobile.feature.contracts

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContractAttachmentDto
import ir.divarfiling.mobile.core.network.ContractDetailDto
import ir.divarfiling.mobile.core.network.ContractListItemDto
import ir.divarfiling.mobile.core.network.ContractListStatsDto
import ir.divarfiling.mobile.core.network.ContractTimelineItemDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ContractsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class ContractSummaryTab {
    ALL,
    DRAFT,
    NEEDS_ACTION,
    AWAITING,
    FINALIZED,
}

data class ContractsHomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val locked: Boolean = false,
    val stats: ContractListStatsDto = ContractListStatsDto(),
    val items: List<ContractListItemDto> = emptyList(),
    val query: String = "",
    val selectedTab: ContractSummaryTab = ContractSummaryTab.ALL,
    val appliedFilters: ContractSheetFilters = ContractSheetFilters(),
    val draftFilters: ContractSheetFilters = ContractSheetFilters(),
    val showFilterSheet: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val isFilteredEmpty: Boolean = false,
)

@HiltViewModel
class ContractsHomeViewModel @Inject constructor(
    private val repository: ContractsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContractsHomeUiState())
    val uiState: StateFlow<ContractsHomeUiState> = _uiState.asStateFlow()

    init {
        load(refresh = true)
    }

    fun load(refresh: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = it.items.isEmpty() && !refresh,
                    isRefreshing = refresh && it.items.isNotEmpty(),
                    error = null,
                )
            }
            val needsAction = state.selectedTab == ContractSummaryTab.NEEDS_ACTION
            val status = when (state.selectedTab) {
                ContractSummaryTab.DRAFT -> "draft"
                ContractSummaryTab.FINALIZED -> "finalized"
                ContractSummaryTab.AWAITING -> "sent"
                else -> null
            }
            val filters = state.appliedFilters
            when (
                val result = repository.listContracts(
                    query = state.query,
                    status = if (needsAction) null else status,
                    type = filters.contractType,
                    dealLinked = filters.dealLinked,
                    dateFrom = filters.dateFrom,
                    dateTo = filters.dateTo,
                    sort = if (needsAction) "needs_action" else filters.sort,
                    needsAction = needsAction,
                    page = 1,
                )
            ) {
                is ApiResult.Success -> {
                    val (data, page) = result.data
                    val filteredEmpty = data.items.isEmpty() &&
                        (state.query.isNotBlank() || filters.activeCount() > 0 || state.selectedTab != ContractSummaryTab.ALL)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            locked = false,
                            stats = data.stats,
                            items = data.items,
                            page = page.page,
                            hasMore = page.hasMore,
                            isFilteredEmpty = filteredEmpty,
                        )
                    }
                }
                is ApiResult.Error -> {
                    val locked = result.code == "CONTRACTS_LICENSE_REQUIRED"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            locked = locked,
                            error = result.message,
                            items = if (locked) emptyList() else it.items,
                        )
                    }
                }
            }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun onTabSelected(tab: ContractSummaryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        load(refresh = true)
    }

    fun search() = load(refresh = true)

    fun openFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = true, draftFilters = it.appliedFilters) }
    }

    fun closeFilterSheet() = _uiState.update { it.copy(showFilterSheet = false) }

    fun onDraftFiltersChange(value: ContractSheetFilters) = _uiState.update { it.copy(draftFilters = value) }

    fun clearDraftFilters() = _uiState.update { it.copy(draftFilters = ContractSheetFilters()) }

    fun applyFilters() {
        _uiState.update {
            it.copy(
                appliedFilters = it.draftFilters,
                showFilterSheet = false,
                snackbarMessage = "فیلتر اعمال شد",
            )
        }
        load(refresh = true)
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
}

data class ContractDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val detail: ContractDetailDto? = null,
    val timeline: List<ContractTimelineItemDto> = emptyList(),
    val attachments: List<ContractAttachmentDto> = emptyList(),
    val error: String? = null,
    val snackbarMessage: String? = null,
    val snackbarError: String? = null,
    val webBridgeUrl: String? = null,
    val openPdf: Boolean = false,
    val isUploadingAttachment: Boolean = false,
    val attachmentBusyId: Long? = null,
)

@HiltViewModel
class ContractDetailViewModel @Inject constructor(
    private val repository: ContractsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contractId: Long = savedStateHandle.get<Long>("contractId") ?: 0L
    private val _uiState = MutableStateFlow(ContractDetailUiState())
    val uiState: StateFlow<ContractDetailUiState> = _uiState.asStateFlow()

    init {
        if (contractId > 0) refresh(initial = true)
    }

    fun refresh(initial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = initial && it.detail == null,
                    isRefreshing = !initial && it.detail != null,
                    error = if (initial) null else it.error,
                )
            }
            loadDetail(replaceError = initial)
        }
    }

    fun refreshInBackground() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, snackbarError = null) }
            loadDetail(replaceError = false)
        }
    }

    private suspend fun loadDetail(replaceError: Boolean) {
        when (val detail = repository.getDetail(contractId)) {
            is ApiResult.Success -> {
                val timeline = when (val t = repository.getTimeline(contractId)) {
                    is ApiResult.Success -> t.data
                    else -> _uiState.value.timeline
                }
                val attachments = when (val a = repository.getAttachments(contractId)) {
                    is ApiResult.Success -> a.data
                    else -> _uiState.value.attachments
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        detail = detail.data,
                        timeline = timeline,
                        attachments = attachments,
                        error = null,
                    )
                }
            }
            is ApiResult.Error -> _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = if (replaceError || it.detail == null) detail.message else it.error,
                    snackbarError = if (it.detail != null) detail.message else null,
                )
            }
        }
    }

    fun onPrimaryAction() {
        val detail = _uiState.value.detail ?: return
        val action = detail.nextAction
        when (action.nativeTarget) {
            "pdf" -> _uiState.update { it.copy(openPdf = true) }
            "bridge" -> openBridge(action.webPath.ifBlank { detail.workspacePaths.wizard })
            else -> openBridge(detail.workspacePaths.manage)
        }
    }

    fun openClausesEditor() {
        val path = _uiState.value.detail?.clausesEditPath ?: _uiState.value.detail?.workspacePaths?.wizard ?: return
        openBridge(path)
    }

    fun openBridge(path: String) {
        if (path.isBlank()) return
        viewModelScope.launch {
            when (val bridge = repository.createWorkspaceBridge(path)) {
                is ApiResult.Success -> _uiState.update { it.copy(webBridgeUrl = bridge.data.bridgeUrl) }
                is ApiResult.Error -> _uiState.update { it.copy(snackbarError = bridge.message) }
            }
        }
    }

    fun consumeWebBridge() = _uiState.update { it.copy(webBridgeUrl = null) }

    fun consumeOpenPdf() = _uiState.update { it.copy(openPdf = false) }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null, snackbarError = null) }

    fun uploadAttachment(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAttachment = true, snackbarError = null) }
            when (val result = repository.uploadAttachment(contractId, uri, title = "")) {
                is ApiResult.Success -> {
                    refreshInBackground()
                    _uiState.update { it.copy(snackbarMessage = "پیوست آپلود شد") }
                }
                is ApiResult.Error -> _uiState.update { it.copy(snackbarError = result.message) }
            }
            _uiState.update { it.copy(isUploadingAttachment = false) }
        }
    }

    fun showMessage(message: String) = _uiState.update { it.copy(snackbarMessage = message) }

    fun openAttachment(att: ContractAttachmentDto, onFile: (File, String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(attachmentBusyId = att.id) }
            val name = att.title.ifBlank { "attachment_${att.id}" }
            when (val result = repository.downloadAttachment(contractId, att.id, name)) {
                is ApiResult.Success -> onFile(result.data, att.mimeType.ifBlank { "application/octet-stream" })
                is ApiResult.Error -> _uiState.update { it.copy(snackbarError = result.message) }
            }
            _uiState.update { it.copy(attachmentBusyId = null) }
        }
    }
}

data class ContractCreateUiState(
    val contractType: String = "sale",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val webBridgeUrl: String? = null,
    val createdContractId: Long? = null,
)

@HiltViewModel
class ContractCreateViewModel @Inject constructor(
    private val repository: ContractsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val dealId: Long? = savedStateHandle.get<Long>("dealId")?.takeIf { it > 0 }
    private val _uiState = MutableStateFlow(ContractCreateUiState())
    val uiState: StateFlow<ContractCreateUiState> = _uiState.asStateFlow()

    fun onTypeSelected(type: String) = _uiState.update { it.copy(contractType = type) }

    fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            when (val created = repository.createContract(_uiState.value.contractType, dealId)) {
                is ApiResult.Success -> {
                    val path = created.data.workspacePaths.wizard.ifBlank { created.data.workspacePaths.manage }
                    when (val bridge = repository.createWorkspaceBridge(path)) {
                        is ApiResult.Success -> _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                webBridgeUrl = bridge.data.bridgeUrl,
                                createdContractId = created.data.id,
                            )
                        }
                        is ApiResult.Error -> _uiState.update {
                            it.copy(isSubmitting = false, error = bridge.message, createdContractId = created.data.id)
                        }
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = created.message)
                }
            }
        }
    }

    fun consumeWebBridge() = _uiState.update { it.copy(webBridgeUrl = null) }
}

data class ContractPdfUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val filePath: String? = null,
    val title: String = "PDF قرارداد",
    val pageCount: Int = 0,
    val snackbarMessage: String? = null,
)

@HiltViewModel
class ContractPdfViewModel @Inject constructor(
    private val repository: ContractsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contractId: Long = savedStateHandle.get<Long>("contractId") ?: 0L
    private val _uiState = MutableStateFlow(
        ContractPdfUiState(title = savedStateHandle.get<String>("title").orEmpty().ifBlank { "PDF قرارداد" }),
    )
    val uiState: StateFlow<ContractPdfUiState> = _uiState.asStateFlow()
    private var cachedFile: File? = null

    init {
        load()
    }

    fun load() {
        if (cachedFile?.exists() == true) {
            _uiState.update { it.copy(isLoading = false, filePath = cachedFile!!.absolutePath) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.downloadPdf(contractId)) {
                is ApiResult.Success -> {
                    cachedFile = result.data
                    _uiState.update {
                        it.copy(isLoading = false, filePath = result.data.absolutePath)
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun pdfFile(): File? = cachedFile

    fun onSaved() = _uiState.update { it.copy(snackbarMessage = "فایل قرارداد ذخیره شد") }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun setPageCount(count: Int) = _uiState.update { it.copy(pageCount = count) }
}
