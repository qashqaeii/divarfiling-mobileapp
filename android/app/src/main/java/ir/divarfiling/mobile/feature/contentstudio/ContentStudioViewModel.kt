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
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsTextStyle
import ir.divarfiling.mobile.feature.contentstudio.ai.PcsAiResponseParser
import ir.divarfiling.mobile.feature.contentstudio.draft.ContentStudioDraftPersistence
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsCanvasInteractionMode
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsSceneHistory
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsSceneMutator
import ir.divarfiling.mobile.feature.contentstudio.export.PcsPngExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject

enum class PcsStudioToolTab {
    Templates, Images, Text, Layers, Brand, Ai,
}

enum class PcsSaveStatus {
    Saved, Unsaved, Saving, Error,
}

data class ContentStudioUiState(
    val loading: Boolean = true,
    val assetsLoading: Boolean = false,
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
    val baseScene: PcsSceneDocument? = null,
    val editableScene: PcsSceneDocument? = null,
    val previewBitmap: Bitmap? = null,
    val projectTitle: String = "",
    val manifestVersion: String = "",
    val posterConfHeadline: String = "",
    val brandFooter: String = "",
    val saveStatus: PcsSaveStatus = PcsSaveStatus.Saved,
    val showTemplatePicker: Boolean = false,
    val canEditDraft: Boolean = false,
    val selectedLayerId: String? = null,
    val interactionMode: PcsCanvasInteractionMode = PcsCanvasInteractionMode.PanZoom,
    val activeTool: PcsStudioToolTab? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val aiLoading: Boolean = false,
    val aiSuggestions: List<String> = emptyList(),
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

    private val sceneHistory = PcsSceneHistory()
    private var renderGeneration = 0
    private var exportGeneration = 0
    private var renderJob: Job? = null
    private var loadNonce = 0
    private var autosaveJob: Job? = null
    private var saveRequestId = 0
    private var dragHistoryCaptured = false

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
                it.copy(loading = false, error = "کاتالوگ قالب‌ها در پاسخ Session نیست", session = session)
            }
            return
        }
        val (savedTemplate, savedFormat) = ContentStudioDraftPersistence.readSelection(session.draft)
        val templateId = savedTemplate?.takeIf { id -> templates.any { it.templateId == id } }
            ?: templates.first().templateId
        val format = savedFormat ?: "story"
        val posterHeadline = ContentStudioDraftPersistence.readPosterHeadline(session.draft, templateId)
        val brandFooter = PcsBrandFooter.resolve(brand)
        sceneHistory.clear()
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
                saveStatus = PcsSaveStatus.Saved,
                canEditDraft = session.canEditProperty,
                selectedLayerId = null,
                canUndo = false,
                canRedo = false,
            )
        }
        loadManifest(templateId, format, session.draft)
    }

    fun selectTemplate(templateId: String) {
        val headline = ContentStudioDraftPersistence.readPosterHeadline(_uiState.value.session?.draft, templateId)
        _uiState.update {
            it.copy(
                selectedTemplateId = templateId,
                posterConfHeadline = headline,
                showTemplatePicker = false,
                saveStatus = if (it.canEditDraft) PcsSaveStatus.Unsaved else it.saveStatus,
            )
        }
        if (_uiState.value.canEditDraft) {
            scheduleAutosave(ContentStudioDraftPersistence.buildSelectionPatch(templateId, _uiState.value.selectedFormat))
        }
        loadManifest(templateId, _uiState.value.selectedFormat, _uiState.value.session?.draft)
    }

    fun selectFormat(format: String) {
        if (format == _uiState.value.selectedFormat) return
        viewModelScope.launch {
            flushCurrentScenePatchIfNeeded()
            _uiState.update {
                it.copy(
                    selectedFormat = format,
                    saveStatus = if (it.canEditDraft) PcsSaveStatus.Unsaved else it.saveStatus,
                )
            }
            if (_uiState.value.canEditDraft) {
                scheduleAutosave(
                    ContentStudioDraftPersistence.buildSelectionPatch(
                        _uiState.value.selectedTemplateId,
                        format,
                    ),
                )
            }
            loadManifest(_uiState.value.selectedTemplateId, format, _uiState.value.session?.draft)
        }
    }

    private suspend fun flushCurrentScenePatchIfNeeded() {
        val state = _uiState.value
        if (!state.canEditDraft) return
        val scene = state.editableScene ?: return
        val patch = ContentStudioDraftPersistence.buildPosterPatch(
            templateId = state.selectedTemplateId,
            format = state.selectedFormat,
            headline = state.posterConfHeadline,
            scene = scene,
            existingDraft = state.session?.draft,
            json = json,
        )
        flushDraftPatch(patch)
    }

    fun toggleTemplatePicker(open: Boolean) {
        _uiState.update { it.copy(showTemplatePicker = open, activeTool = if (open) PcsStudioToolTab.Templates else it.activeTool) }
    }

    fun setActiveTool(tab: PcsStudioToolTab?) {
        _uiState.update { it.copy(activeTool = tab) }
    }

    fun setInteractionMode(mode: PcsCanvasInteractionMode) {
        _uiState.update { it.copy(interactionMode = mode) }
    }

    private fun loadManifest(templateId: String, format: String, draft: JsonElement?) {
        val nonce = ++loadNonce
        viewModelScope.launch {
            val expected = _uiState.value.manifestVersion
            when (val result = repository.loadManifest(propertyId, templateId, format, expected)) {
                is ApiResult.Error -> {
                    if (nonce != loadNonce) return@launch
                    _uiState.update {
                        it.copy(error = result.message, errorCode = result.code, manifest = null, baseScene = null, editableScene = null, previewBitmap = null)
                    }
                }
                is ApiResult.Success -> {
                    if (nonce != loadNonce) return@launch
                    val manifest = result.data
                    PcsManifestValidator.validateManifestVersion(manifest.manifestVersion)?.let { msg ->
                        _uiState.update { it.copy(error = msg, baseScene = null, editableScene = null, previewBitmap = null) }
                        return@launch
                    }
                    val baseScene = manifest.scene?.let { el ->
                        runCatching { json.decodeFromJsonElement(PcsSceneDocument.serializer(), el) }.getOrNull()
                    }
                    PcsManifestValidator.validateScene(baseScene)?.let { msg ->
                        _uiState.update { it.copy(error = msg, baseScene = null, editableScene = null, previewBitmap = null) }
                        return@launch
                    }
                    val savedScene = ContentStudioDraftPersistence.readPosterScene(
                        draft,
                        templateId,
                        format,
                        json,
                    )
                    val editable = PcsSceneMutator.mergeBaseWithDraft(baseScene ?: return@launch, savedScene)
                    _uiState.update {
                        it.copy(
                            manifest = manifest,
                            baseScene = baseScene,
                            editableScene = editable,
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
        val gen = ++renderGeneration
        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            val state = _uiState.value
            val scene = state.editableScene ?: return@launch
            val property = state.property ?: return@launch
            val manifest = state.manifest ?: return@launch
            _uiState.update { it.copy(assetsLoading = true, error = null) }
            val assets = assetLoader.load(
                property = property,
                overlayUrl = manifest.overlayUrl,
                brandLogoUrl = state.brand?.logoUrl,
                scene = scene,
            )
            if (gen != renderGeneration) return@launch
            if (assets.hasCriticalFailure) {
                _uiState.update { it.copy(assetsLoading = false, error = "بارگذاری تصاویر یا Overlay ناموفق بود", previewBitmap = null) }
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
            val previewW = (w / 2).coerceIn(270, w)
            val previewH = (h * previewW / w).coerceAtLeast(1)
            val rendered = withContext(Dispatchers.Default) {
                canvasRenderer.render(input, previewW, previewH)
            }
            if (gen != renderGeneration) return@launch
            rendered.onSuccess { bmp ->
                replacePreviewBitmap(bmp)
                _uiState.update { it.copy(assetsLoading = false) }
            }.onFailure { err ->
                _uiState.update { it.copy(assetsLoading = false, error = err.message, previewBitmap = null) }
            }
        }
    }

    private fun replacePreviewBitmap(bmp: Bitmap) {
        val old = _uiState.value.previewBitmap
        _uiState.update { it.copy(previewBitmap = bmp) }
        old?.takeIf { !it.isRecycled && it !== bmp }?.recycle()
    }

    private fun applySceneChange(newScene: PcsSceneDocument, markDirty: Boolean = true) {
        _uiState.value.editableScene?.let { sceneHistory.pushBeforeChange(it) }
        _uiState.update {
            it.copy(
                editableScene = newScene,
                canUndo = sceneHistory.canUndo(),
                canRedo = sceneHistory.canRedo(),
                saveStatus = if (markDirty && it.canEditDraft) PcsSaveStatus.Unsaved else it.saveStatus,
            )
        }
        renderPreview()
        if (markDirty && _uiState.value.canEditDraft) {
            scheduleFullAutosave()
        }
    }

    fun undo() {
        val current = _uiState.value.editableScene ?: return
        val prev = sceneHistory.undo(current) ?: return
        _uiState.update {
            it.copy(editableScene = prev, canUndo = sceneHistory.canUndo(), canRedo = sceneHistory.canRedo(), saveStatus = PcsSaveStatus.Unsaved)
        }
        renderPreview()
        scheduleFullAutosave()
    }

    fun redo() {
        val current = _uiState.value.editableScene ?: return
        val next = sceneHistory.redo(current) ?: return
        _uiState.update {
            it.copy(editableScene = next, canUndo = sceneHistory.canUndo(), canRedo = sceneHistory.canRedo(), saveStatus = PcsSaveStatus.Unsaved)
        }
        renderPreview()
        scheduleFullAutosave()
    }

    fun selectLayer(layerId: String) {
        _uiState.update { it.copy(selectedLayerId = layerId) }
    }

    fun selectLayerAt(sceneX: Float, sceneY: Float) {
        val scene = _uiState.value.editableScene ?: return
        val id = PcsSceneMutator.hitTestLayer(scene, sceneX, sceneY)
        _uiState.update { it.copy(selectedLayerId = id) }
    }

    fun beginLayerDrag() {
        if (!_uiState.value.canEditDraft) return
        val id = _uiState.value.selectedLayerId ?: return
        val layer = _uiState.value.editableScene?.layers?.find { it.id == id } ?: return
        if (layer.locked) return
        if (!dragHistoryCaptured) {
            _uiState.value.editableScene?.let { sceneHistory.pushBeforeChange(it) }
            dragHistoryCaptured = true
        }
    }

    fun moveSelectedLayer(dx: Float, dy: Float) {
        val id = _uiState.value.selectedLayerId ?: return
        val scene = _uiState.value.editableScene ?: return
        val layer = scene.layers.find { it.id == id } ?: return
        if (layer.locked || !_uiState.value.canEditDraft) return
        val moved = PcsSceneMutator.moveLayer(scene, id, dx, dy)
        _uiState.update {
            it.copy(
                editableScene = moved,
                saveStatus = PcsSaveStatus.Unsaved,
            )
        }
        renderPreview()
    }

    fun endLayerDrag() {
        if (!dragHistoryCaptured) return
        dragHistoryCaptured = false
        _uiState.update {
            it.copy(canUndo = sceneHistory.canUndo(), canRedo = sceneHistory.canRedo())
        }
        scheduleFullAutosave()
    }

    fun updateHeadline(text: String) {
        _uiState.update { it.copy(posterConfHeadline = text, saveStatus = PcsSaveStatus.Unsaved) }
        renderPreview()
        scheduleFullAutosave()
    }

    fun updateSelectedLayerText(text: String) {
        val id = _uiState.value.selectedLayerId ?: return
        val scene = _uiState.value.editableScene ?: return
        applySceneChange(PcsSceneMutator.setLayerTextDetached(scene, id, text))
    }

    fun toggleLayerVisibility(layerId: String) {
        val scene = _uiState.value.editableScene ?: return
        val layer = scene.layers.find { it.id == layerId } ?: return
        applySceneChange(PcsSceneMutator.setLayerVisible(scene, layerId, !layer.visible))
    }

    fun toggleLayerLock(layerId: String) {
        val scene = _uiState.value.editableScene ?: return
        val layer = scene.layers.find { it.id == layerId } ?: return
        applySceneChange(PcsSceneMutator.setLayerLocked(scene, layerId, !layer.locked))
    }

    fun reorderLayer(layerId: String, direction: Int) {
        val scene = _uiState.value.editableScene ?: return
        applySceneChange(PcsSceneMutator.reorderLayer(scene, layerId, direction))
    }

    fun setImageSlotForSelected(index: Int) {
        val id = _uiState.value.selectedLayerId ?: return
        val scene = _uiState.value.editableScene ?: return
        applySceneChange(PcsSceneMutator.setImageIndex(scene, id, index))
    }

    fun saveNow() {
        if (!_uiState.value.canEditDraft) return
        scheduleFullAutosave(immediate = true)
    }

    private fun scheduleAutosave(patch: JsonObject) {
        if (!_uiState.value.canEditDraft) return
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(1_500)
            flushDraftPatch(patch)
        }
    }

    private fun scheduleFullAutosave(immediate: Boolean = false) {
        if (!_uiState.value.canEditDraft) return
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            if (!immediate) delay(2_000)
            val state = _uiState.value
            val scene = state.editableScene ?: return@launch
            val patch = ContentStudioDraftPersistence.buildPosterPatch(
                templateId = state.selectedTemplateId,
                format = state.selectedFormat,
                headline = state.posterConfHeadline,
                scene = scene,
                existingDraft = state.session?.draft,
                json = json,
            )
            flushDraftPatch(patch)
        }
    }

    private suspend fun flushDraftPatch(patch: JsonObject) {
        val reqId = ++saveRequestId
        _uiState.update { it.copy(saveStatus = PcsSaveStatus.Saving) }
        when (val result = repository.saveDraft(propertyId, patch)) {
            is ApiResult.Error -> {
                if (reqId != saveRequestId) return
                _uiState.update { it.copy(saveStatus = PcsSaveStatus.Error, error = result.message) }
            }
            is ApiResult.Success -> {
                if (reqId != saveRequestId) return
                _uiState.update {
                    it.copy(
                        saveStatus = PcsSaveStatus.Saved,
                        session = it.session?.let { s -> s.copy(draft = result.data.draft) },
                        successMessage = null,
                    )
                }
            }
        }
    }

    fun exportPng() {
        val exportGen = ++exportGeneration
        viewModelScope.launch {
            val state = _uiState.value
            val scene = state.editableScene ?: return@launch
            val property = state.property ?: return@launch
            val manifest = state.manifest ?: return@launch
            _uiState.update { it.copy(exporting = true, error = null) }
            val assets = assetLoader.load(property, manifest.overlayUrl, state.brand?.logoUrl, scene)
            if (exportGen != exportGeneration) {
                _uiState.update { it.copy(exporting = false) }
                return@launch
            }
            if (assets.hasCriticalFailure) {
                _uiState.update { it.copy(exporting = false, error = "بارگذاری Asset برای خروجی ناموفق بود") }
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
            val rendered = withContext(Dispatchers.Default) { canvasRenderer.render(input, w, h) }
            rendered.onFailure { err ->
                _uiState.update { it.copy(exporting = false, error = err.message) }
                return@launch
            }
            val bmp = rendered.getOrThrow()
            val name = "divarfiling-${state.selectedTemplateId}-${state.selectedFormat}.png"
            val result = withContext(Dispatchers.IO) { pngExporter.saveToGallery(bmp, name) }
            bmp.recycle()
            result.onSuccess {
                _uiState.update { it.copy(exporting = false, successMessage = "PNG در گالری ذخیره شد") }
            }.onFailure { err ->
                _uiState.update { it.copy(exporting = false, error = err.message ?: "خطا در خروجی PNG") }
            }
        }
    }

    fun runAiAction(action: String) {
        if (!_uiState.value.canEditDraft) return
        viewModelScope.launch {
            _uiState.update { it.copy(aiLoading = true, aiSuggestions = emptyList(), error = null) }
            when (val result = repository.runAiAction(propertyId, action)) {
                is ApiResult.Error -> _uiState.update { it.copy(aiLoading = false, error = result.message) }
                is ApiResult.Success -> {
                    val list = when (action) {
                        "divar-body" -> listOf(PcsAiResponseParser.parseDivarBody(result.data)).filter { it.isNotBlank() }
                        "social" -> PcsAiResponseParser.parseSocialPack(result.data)
                        else -> PcsAiResponseParser.parseTitleSuggestions(result.data, action)
                    }
                    _uiState.update { it.copy(aiLoading = false, aiSuggestions = list) }
                }
            }
        }
    }

    fun runPosterHeadlineAi() = runAiAction("poster-headlines")

    fun applyAiHeadline(suggestion: String) {
        updateHeadline(suggestion)
        _uiState.update { it.copy(aiSuggestions = emptyList(), activeTool = null) }
    }

    fun hasUnsavedChanges(): Boolean = _uiState.value.saveStatus == PcsSaveStatus.Unsaved

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }

    override fun onCleared() {
        _uiState.value.previewBitmap?.takeIf { !it.isRecycled }?.recycle()
        super.onCleared()
    }
}
