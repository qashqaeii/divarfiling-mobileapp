package ir.divarfiling.mobile.feature.ai

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.AiDraftMessageRequest
import ir.divarfiling.mobile.core.network.AiQuotaData
import ir.divarfiling.mobile.core.network.AiSummarizeListingRequest
import ir.divarfiling.mobile.core.network.AiTextResult
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.WorkspaceExtrasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AiMode {
    Draft,
    Summarize,
}

data class AiToneOption(
    val value: String,
    val label: String,
)

data class AiAssistantUiState(
    val quota: AiQuotaData? = null,
    val contactId: String = "",
    val contactLabel: String = "",
    val contactPhone: String = "",
    val listingToken: String = "",
    val listingLabel: String = "",
    val showContactPicker: Boolean = false,
    val showListingPicker: Boolean = false,
    val tone: String = "رسمی",
    val notes: String = "",
    val mode: AiMode = AiMode.Draft,
    val contextLabel: String? = null,
    val draftText: String = "",
    val summaryText: String = "",
    val draftIsFallback: Boolean = false,
    val summaryIsFallback: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val repository: WorkspaceExtrasRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val initialContactId = savedStateHandle.get<String>("contactId").orEmpty()
    private val initialListingToken = savedStateHandle.get<String>("listingToken").orEmpty()
    private val initialMode = savedStateHandle.get<String>("mode").orEmpty()

    private val _uiState = MutableStateFlow(
        AiAssistantUiState(
            contactId = initialContactId,
            listingToken = initialListingToken,
            contactLabel = if (initialContactId.isNotBlank()) "مخاطب انتخاب‌شده" else "",
            listingLabel = if (initialListingToken.isNotBlank()) "آگهی انتخاب‌شده" else "",
            mode = when (initialMode.lowercase()) {
                "summarize", "summary" -> AiMode.Summarize
                else -> AiMode.Draft
            },
            contextLabel = buildContextLabel(
                if (initialContactId.isNotBlank()) "مخاطب انتخاب‌شده" else "",
                if (initialListingToken.isNotBlank()) "آگهی انتخاب‌شده" else "",
                initialContactId,
                initialListingToken,
            ),
        ),
    )
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    init { loadQuota() }

    fun loadQuota() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.quota == null && !it.isRefreshing, error = null) }
            when (val result = repository.getAiQuota()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(quota = result.data, isLoading = false, isRefreshing = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = result.message)
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadQuota()
    }

    fun onContactIdChange(value: String) = _uiState.update {
        it.copy(contactId = value, contextLabel = buildContextLabel(it.contactLabel, it.listingLabel, value, it.listingToken))
    }

    fun selectContact(id: Long, name: String, phone: String? = null) = _uiState.update {
        it.copy(
            contactId = id.toString(),
            contactLabel = name,
            contactPhone = phone.orEmpty(),
            showContactPicker = false,
            contextLabel = buildContextLabel(name, it.listingLabel, id.toString(), it.listingToken),
        )
    }

    fun selectListing(token: String, title: String) = _uiState.update {
        it.copy(
            listingToken = token,
            listingLabel = title,
            showListingPicker = false,
            contextLabel = buildContextLabel(it.contactLabel, title, it.contactId, token),
        )
    }

    fun toggleContactPicker(show: Boolean) = _uiState.update { it.copy(showContactPicker = show) }
    fun toggleListingPicker(show: Boolean) = _uiState.update { it.copy(showListingPicker = show) }

    fun onListingTokenChange(value: String) = _uiState.update {
        it.copy(listingToken = value, contextLabel = buildContextLabel(it.contactLabel, it.listingLabel, it.contactId, value))
    }

    fun onToneChange(value: String) = _uiState.update { it.copy(tone = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }
    fun onModeChange(mode: AiMode) = _uiState.update { it.copy(mode = mode) }

    fun generateDraft() {
        val state = _uiState.value
        if (state.contactId.isBlank() && state.listingToken.isBlank()) {
            _uiState.update {
                it.copy(error = "ابتدا مخاطب یا آگهی را از فهرست انتخاب کنید", successMessage = null)
            }
            return
        }
        if (state.quota?.remaining == 0 && state.quota.enabled) {
            _uiState.update {
                it.copy(
                    error = "سهمیه روزانه AI تمام شده است. می‌توانید بعداً دوباره تلاش کنید.",
                    successMessage = null,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, error = null, successMessage = null, mode = AiMode.Draft)
            }
            when (
                val result = repository.aiDraftMessage(
                    AiDraftMessageRequest(
                        contactId = state.contactId.trim().toLongOrNull(),
                        listingToken = state.listingToken.trim().ifBlank { null },
                        tone = state.tone.ifBlank { "رسمی" },
                        notes = state.notes.trim().ifBlank { null },
                    ),
                )
            ) {
                is ApiResult.Success -> applyTextResult(
                    result = result.data,
                    draft = true,
                    successMessage = if (result.data.isFallback || result.data.source == "fallback") {
                        "پیش‌نویس آماده شد (نسخه جایگزین)"
                    } else {
                        "پیش‌نویس آماده شد"
                    },
                )
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun summarizeListing() {
        val token = _uiState.value.listingToken.trim()
        if (token.isBlank()) {
            _uiState.update { it.copy(error = "برای خلاصه‌سازی، یک آگهی از فایلینگ انتخاب کنید", successMessage = null) }
            return
        }
        if (_uiState.value.quota?.remaining == 0 && _uiState.value.quota?.enabled == true) {
            _uiState.update {
                it.copy(
                    error = "سهمیه روزانه AI تمام شده است. می‌توانید بعداً دوباره تلاش کنید.",
                    successMessage = null,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, error = null, successMessage = null, mode = AiMode.Summarize)
            }
            when (val result = repository.aiSummarizeListing(AiSummarizeListingRequest(listingToken = token))) {
                is ApiResult.Success -> applyTextResult(
                    result = result.data,
                    draft = false,
                    successMessage = if (result.data.isFallback || result.data.source == "fallback") {
                        "خلاصه آگهی آماده شد (نسخه جایگزین)"
                    } else {
                        "خلاصه آگهی آماده شد"
                    },
                )
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
            }
        }
    }

    fun regenerate() {
        when (_uiState.value.mode) {
            AiMode.Draft -> generateDraft()
            AiMode.Summarize -> summarizeListing()
        }
    }

    fun runPrimaryAction() {
        when (_uiState.value.mode) {
            AiMode.Draft -> generateDraft()
            AiMode.Summarize -> summarizeListing()
        }
    }

    fun onDraftTextChange(value: String) = _uiState.update { it.copy(draftText = value) }
    fun onSummaryTextChange(value: String) = _uiState.update { it.copy(summaryText = value) }
    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
    fun showMessage(message: String) = _uiState.update { it.copy(successMessage = message, error = null) }

    private fun applyTextResult(result: AiTextResult, draft: Boolean, successMessage: String) {
        _uiState.update { state ->
            val updatedQuota = result.quotaRemaining?.let { remaining ->
                state.quota?.copy(remaining = remaining)
                    ?: AiQuotaData(remaining = remaining, enabled = true)
            } ?: state.quota
            if (draft) {
                state.copy(
                    isSubmitting = false,
                    draftText = result.text,
                    draftIsFallback = result.isFallback || result.source == "fallback",
                    successMessage = successMessage,
                    quota = updatedQuota,
                )
            } else {
                state.copy(
                    isSubmitting = false,
                    summaryText = result.text,
                    summaryIsFallback = result.isFallback || result.source == "fallback",
                    successMessage = successMessage,
                    quota = updatedQuota,
                )
            }
        }
    }

    companion object {
        val toneOptions = listOf(
            AiToneOption("رسمی", "رسمی و حرفه‌ای"),
            AiToneOption("صمیمی", "صمیمی و گرم"),
            AiToneOption("کوتاه", "کوتاه و مستقیم"),
            AiToneOption("مشاورانه", "مشاورانه"),
            AiToneOption("پیگیری", "پیگیری ملایم"),
        )

        private fun buildContextLabel(
            contactLabel: String,
            listingLabel: String,
            contactId: String = "",
            listingToken: String = "",
        ): String? {
            val contactPart = contactLabel.trim().ifBlank {
                contactId.trim().takeIf { it.isNotEmpty() }?.let { "مخاطب انتخاب‌شده" }
            }.takeIf { !it.isNullOrBlank() }
            val listingPart = listingLabel.trim().ifBlank {
                listingToken.trim().takeIf { it.isNotEmpty() }?.let { "آگهی انتخاب‌شده" }
            }.takeIf { !it.isNullOrBlank() }
            return listOfNotNull(contactPart, listingPart).joinToString(" · ").ifBlank { null }
        }
    }
}

