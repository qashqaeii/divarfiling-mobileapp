package ir.divarfiling.mobile.feature.contentstudio

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContentStudioManifestData
import ir.divarfiling.mobile.core.network.ContentStudioSessionData
import ir.divarfiling.mobile.core.network.ContentStudioTemplateCatalogItem
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ContentStudioRepository
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsAndroidCanvasRenderer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsBrandFooter
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsImageAssetLoader
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsManifestValidator
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsRenderInput
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioBrand
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioProperty
import ir.divarfiling.mobile.feature.contentstudio.draft.ContentStudioDraftPersistence
import ir.divarfiling.mobile.feature.contentstudio.export.PcsPngExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject

data class ContentStudioUiState(
    val loading: Boolean = true,
    val assetsLoading: Boolean = false,
    val saving: Boolean = false,
    val exporting: Boolean = false,
    val error: String? = null,
    val errorCode: String? = null,
    val successMessage: String? = null,
    val session: ContentStudioSessionData? = null,
    val property: PcsStudioProperty? = null,
    val brand: PcsStudioBrand? = null,
    val templates: List<ContentStudioTemplateCatalogItem> = emptyList(),
    val selectedTemplateId: String = "pearlescent",
    val selectedFormat: String = "story",
    val manifest: ContentStudioManifestData? = null,
    val scene: PcsSceneDocument? = null,
    val previewBitmap: Bitmap? = null,
    val projectTitle: String = "",
    val manifestVersion: String = "",
    val posterConfHeadline: String = "",
    val brandFooter: String = "",
    val selectionDirty: Boolean = false,
    val showTemplatePicker: Boolean = false,
    val canSaveSelection: Boolean = false,
)

@HiltViewModel
class ContentStudioViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ContentStudioRepository,
    private val assetLoader: PcsImageAssetLoader,
    private val canvasRenderer: PcsAndroidCanvasRenderer,
    private val pngExporter: PcsPngExporter,
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
            _uiState.update { it.copy(loading = true, error = null, errorCode = null, successMessage = null) }
            when (val result = repository.loadSession(propertyId)) {
                is ApiResult.Error -> _uiState.update {
                    it.copy(loading = false, error = result.message, errorCode = result.code)
                }
                is ApiResult.Success -> applySession(result.data)
            }
        }
    }

    private suspend fun applySession(session: ContentStudioSessionData) {
        val property = session.property?.let { el ->
            runCatching { json.decodeFromJsonElement(PcsStudioProperty.serializer(), el) }.getOrNull()
        }
        val brand = (session.brandEffective ?: session.brand)?.let { el ->
            runCatching { json.decodeFromJsonElement(PcsStudioBrand.serializer(), el) }.getOrNull()
        }
        val templates = session.templatesCatalog.orEmpty()
        if (templates.isEmpty()) {
            _uiState.update {
                it.copy(
                    loading = false,
                    error = "کاتالوگ قالب‌ها در پاسخ Session نیست",
                    session = session,
                )
            }
            return
        }
        val (savedTemplate, savedFormat) = ContentStudioDraftPersistence.readSelection(session.draft)
        val templateId = savedTemplate?.takeIf { id -> templates.any { it.templateId == id } }
            ?: templates.first().templateId
        val format = savedFormat ?: "story"
        val posterHeadline = readPosterHeadline(session.draft, templateId)
        val brandFooter = PcsBrandFooter.resolve(brand)
        _uiState.update {
            it.copy(
                loading = false,
                session = session,
                property = property,
                brand = brand,
                templates = templates,
                selectedTemplateId = templateId,
                selectedFormat = format,
                projectTitle = property?.title.orEmpty(),
                manifestVersion = templates.firstOrNull()?.manifestVersion.orEmpty(),
                posterConfHeadline = posterHeadline,
                brandFooter = brandFooter,
                selectionDirty = false,
                canSaveSelection = false,
            )
        }
        loadManifest(templateId, format)
    }

    fun selectTemplate(templateId: String) {
        _uiState.update {
            it.copy(
                selectedTemplateId = templateId,
                selectionDirty = true,
                canSaveSelection = true,
                posterConfHeadline = readPosterHeadline(it.session?.draft, templateId),
            )
        }
        loadManifest(templateId, _uiState.value.selectedFormat)
    }

    fun selectFormat(format: String) {
        _uiState.update {
            it.copy(
                selectedFormat = format,
                selectionDirty = true,
                canSaveSelection = true,
            )
        }
        loadManifest(_uiState.value.selectedTemplateId, format)
    }

    fun toggleTemplatePicker(open: Boolean) {
        _uiState.update { it.copy(showTemplatePicker = open) }
    }

    private fun loadManifest(templateId: String, format: String) {
        viewModelScope.launch {
            val expected = _uiState.value.manifestVersion
            when (val result = repository.loadManifest(propertyId, templateId, format, expected)) {
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        error = result.message,
                        errorCode = result.code,
                        manifest = null,
                        scene = null,
                        previewBitmap = null,
                    )
                }
                is ApiResult.Success -> {
                    val manifest = result.data
                    PcsManifestValidator.validateManifestVersion(manifest.manifestVersion)?.let { msg ->
                        _uiState.update { it.copy(error = msg, scene = null, previewBitmap = null) }
                        return@launch
                    }
                    val scene = manifest.scene?.let { el ->
                        runCatching { json.decodeFromJsonElement(PcsSceneDocument.serializer(), el) }.getOrNull()
                    }
                    PcsManifestValidator.validateScene(scene)?.let { msg ->
                        _uiState.update { it.copy(error = msg, scene = null, previewBitmap = null) }
                        return@launch
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
                    renderPreview()
                }
            }
        }
    }

    private fun renderPreview() {
        viewModelScope.launch {
            val state = _uiState.value
            val scene = state.scene ?: return@launch
            val property = state.property ?: return@launch
            val manifest = state.manifest ?: return@launch
            _uiState.update { it.copy(assetsLoading = true, error = null) }
            val assets = assetLoader.load(property, manifest.overlayUrl)
            if (assets.hasCriticalFailure) {
                _uiState.update {
                    it.copy(
                        assetsLoading = false,
                        error = "بارگذاری تصاویر یا Overlay ناموفق بود",
                        previewBitmap = null,
                    )
                }
                return@launch
            }
            val input = PcsRenderInput(
                scene = scene,
                property = property,
                assets = assets,
                posterConfHeadline = state.posterConfHeadline,
                brandFooter = state.brandFooter,
                canvasBackground = manifest.background,
            )
            val w = manifest.canvas.width.coerceAtLeast(1)
            val h = manifest.canvas.height.coerceAtLeast(1)
            val rendered = withContext(Dispatchers.Default) {
                canvasRenderer.render(input, w, h)
            }
            rendered.onSuccess { bmp ->
                _uiState.update { it.copy(assetsLoading = false, previewBitmap = bmp) }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(assetsLoading = false, error = err.message, previewBitmap = null)
                }
            }
        }
    }

    fun saveSelection() {
        val state = _uiState.value
        if (!state.canSaveSelection) return
        val existing = state.session?.draft
        val payload = ContentStudioDraftPersistence.mergeSelection(
            existing,
            state.selectedTemplateId,
            state.selectedFormat,
        )
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            when (val result = repository.saveDraft(propertyId, payload)) {
                is ApiResult.Error -> _uiState.update {
                    it.copy(saving = false, error = result.message)
                }
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        saving = false,
                        selectionDirty = false,
                        canSaveSelection = false,
                        successMessage = "انتخاب قالب ذخیره شد",
                    )
                }
            }
        }
    }

    fun exportPng() {
        val bmp = _uiState.value.previewBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(exporting = true, error = null) }
            val name = "divarfiling-${_uiState.value.selectedTemplateId}-${_uiState.value.selectedFormat}.png"
            val result = withContext(Dispatchers.IO) { pngExporter.saveToGallery(bmp, name) }
            result.onSuccess {
                _uiState.update { it.copy(exporting = false, successMessage = "PNG در گالری ذخیره شد") }
            }.onFailure { err ->
                _uiState.update { it.copy(exporting = false, error = err.message ?: "خطا در خروجی PNG") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }

    private fun readPosterHeadline(draft: JsonElement?, templateId: String): String {
        val posters = (draft as? JsonObject)?.get("posters") as? JsonObject ?: return ""
        val conf = posters[templateId] as? JsonObject ?: return ""
        return (conf["headline"] as? JsonPrimitive)?.content.orEmpty()
    }
}
