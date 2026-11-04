package ir.divarfiling.mobile.feature.contentstudio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContentStudioManifestData
import ir.divarfiling.mobile.core.network.ContentStudioSessionData
import ir.divarfiling.mobile.core.network.ContentStudioTemplateCatalogItem
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ContentStudioRepository
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioProperty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class ContentStudioUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val errorCode: String? = null,
    val session: ContentStudioSessionData? = null,
    val property: PcsStudioProperty? = null,
    val templates: List<ContentStudioTemplateCatalogItem> = emptyList(),
    val selectedTemplateId: String = "pearlescent",
    val selectedFormat: String = "story",
    val manifest: ContentStudioManifestData? = null,
    val scene: PcsSceneDocument? = null,
    val projectTitle: String = "",
    val manifestVersion: String = "",
)

@HiltViewModel
class ContentStudioViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ContentStudioRepository,
    private val json: Json,
) : ViewModel() {

    private val propertyId: Long = checkNotNull(savedStateHandle.get<Long>("propertyId"))

    private val _uiState = MutableStateFlow(ContentStudioUiState())
    val uiState: StateFlow<ContentStudioUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, errorCode = null) }
            when (val result = repository.loadSession(propertyId)) {
                is ApiResult.Error -> _uiState.update {
                    it.copy(loading = false, error = result.message, errorCode = result.code)
                }
                is ApiResult.Success -> {
                    val session = result.data
                    val property = session.property?.let { el ->
                        runCatching { json.decodeFromJsonElement(PcsStudioProperty.serializer(), el) }.getOrNull()
                    }
                    val templates = session.templatesCatalog.orEmpty()
                    val templateId = templates.firstOrNull()?.templateId ?: "pearlescent"
                    _uiState.update {
                        it.copy(
                            loading = false,
                            session = session,
                            property = property,
                            templates = templates,
                            selectedTemplateId = templateId,
                            projectTitle = property?.title.orEmpty(),
                            manifestVersion = templates.firstOrNull()?.manifestVersion.orEmpty(),
                        )
                    }
                    loadManifest(templateId, _uiState.value.selectedFormat)
                }
            }
        }
    }

    fun selectTemplate(templateId: String) {
        _uiState.update { it.copy(selectedTemplateId = templateId) }
        loadManifest(templateId, _uiState.value.selectedFormat)
    }

    fun selectFormat(format: String) {
        _uiState.update { it.copy(selectedFormat = format) }
        loadManifest(_uiState.value.selectedTemplateId, format)
    }

    private fun loadManifest(templateId: String, format: String) {
        viewModelScope.launch {
            val expected = _uiState.value.manifestVersion
            when (
                val result = repository.loadManifest(propertyId, templateId, format, expected)
            ) {
                is ApiResult.Error -> _uiState.update {
                    it.copy(error = result.message, errorCode = result.code, manifest = null, scene = null)
                }
                is ApiResult.Success -> {
                    val manifest = result.data
                    val scene = manifest.scene?.let { el ->
                        runCatching { json.decodeFromJsonElement(PcsSceneDocument.serializer(), el) }.getOrNull()
                    }
                    _uiState.update {
                        it.copy(
                            manifest = manifest,
                            scene = scene,
                            manifestVersion = manifest.manifestVersion,
                            error = null,
                            errorCode = null,
                        )
                    }
                }
            }
        }
    }

    fun saveDraft() {
        val draft = _uiState.value.session?.draft ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            when (val result = repository.saveDraft(propertyId, draft)) {
                is ApiResult.Error -> _uiState.update { it.copy(saving = false, error = result.message) }
                is ApiResult.Success -> _uiState.update { it.copy(saving = false) }
            }
        }
    }
}
