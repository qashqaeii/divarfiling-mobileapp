package ir.divarfiling.mobile.feature.contentstudio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsCanvasInteractionMode
import ir.divarfiling.mobile.feature.contentstudio.ui.PcsEditorPosterCanvas
import ir.divarfiling.mobile.feature.contentstudio.ui.PcsStudioToolSheets
import ir.divarfiling.mobile.feature.contentstudio.ui.PcsStudioToolbar
import ir.divarfiling.mobile.feature.contentstudio.ui.TemplatePickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentStudioScreen(
    onBack: () -> Unit,
    viewModel: ContentStudioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = state.saveStatus == PcsSaveStatus.Unsaved) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("تغییرات ذخیره نشده") },
            text = { Text("قبل از خروج تغییرات را ذخیره کنید یا بدون ذخیره خارج شوید.") },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; viewModel.saveNow() }) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false; onBack() }) {
                    Text("خروج")
                }
            },
        )
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { msg ->
            if (state.previewBitmap == null || !state.assetsLoading) {
                snackbar.showSnackbar(msg)
            }
        }
    }

    TemplatePickerSheet(
        visible = state.showTemplatePicker,
        templates = state.templates,
        selectedTemplateId = state.selectedTemplateId,
        onSelect = viewModel::selectTemplate,
        onDismiss = { viewModel.toggleTemplatePicker(false) },
    )

    PcsStudioToolSheets(
        activeTool = state.activeTool,
        onDismiss = { viewModel.setActiveTool(null) },
        property = state.property,
        brand = state.brand,
        layers = state.editableScene?.layers.orEmpty(),
        layerOrder = state.editableScene?.layerOrder.orEmpty(),
        selectedLayerId = state.selectedLayerId,
        headline = state.posterConfHeadline,
        aiLoading = state.aiLoading,
        aiSuggestions = state.aiSuggestions,
        canEdit = state.canEditDraft,
        onHeadlineChange = viewModel::updateHeadline,
        onLayerSelect = viewModel::selectLayer,
        onToggleVisibility = viewModel::toggleLayerVisibility,
        onToggleLock = viewModel::toggleLayerLock,
        onReorder = viewModel::reorderLayer,
        onImageIndex = viewModel::setImageSlotForSelected,
        onRunAi = viewModel::runPosterHeadlineAi,
        onRunAiAction = viewModel::runAiAction,
        onApplyAi = viewModel::applyAiHeadline,
        onOpenTemplates = { viewModel.toggleTemplatePicker(true) },
        onInteractionMode = viewModel::setInteractionMode,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("استودیوی تولید محتوا", style = AppTypography.sectionTitle)
                        Text(
                            state.projectTitle.ifBlank { "فایل شخصی" },
                            style = AppTypography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(DfIcons.ChevronLeft, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    Text(
                        when (state.saveStatus) {
                            PcsSaveStatus.Saving -> "در حال ذخیره…"
                            PcsSaveStatus.Unsaved -> "ذخیره نشده"
                            PcsSaveStatus.Error -> "خطای ذخیره"
                            PcsSaveStatus.Saved -> "ذخیره شد"
                        },
                        style = AppTypography.labelSmall,
                        modifier = Modifier.padding(end = AppSpacing.xs),
                    )
                    TextButton(onClick = viewModel::saveNow, enabled = state.canEditDraft && state.saveStatus == PcsSaveStatus.Unsaved) {
                        Text("ذخیره")
                    }
                    TextButton(onClick = viewModel::undo, enabled = state.canUndo) { Text("Undo") }
                    TextButton(onClick = viewModel::redo, enabled = state.canRedo) { Text("Redo") }
                    TextButton(
                        onClick = viewModel::exportPng,
                        enabled = state.editableScene != null && !state.exporting && !state.assetsLoading,
                    ) {
                        Text(if (state.exporting) "…" else "PNG")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = DfThemeColors.background(),
    ) { padding ->
        when {
            state.loading -> LoadingBlock(Modifier.fillMaxSize().padding(padding))
            state.errorCode == "MANIFEST_VERSION_MISMATCH" || state.errorCode == "MANIFEST_UNAVAILABLE" -> {
                DfEmptyState(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.screenHorizontal),
                    title = "Manifest در دسترس نیست",
                    subtitle = state.error ?: "نسخه قالب با سرور هماهنگ نیست.",
                    actionLabel = "تلاش مجدد",
                    onAction = viewModel::refresh,
                    variant = DfEmptyVariant.Offline,
                )
            }
            state.editableScene != null && state.property != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        listOf("story", "post", "square").forEach { fmt ->
                            DfPrimaryButton(
                                text = when (fmt) {
                                    "story" -> "استوری"
                                    "post" -> "پست"
                                    else -> "مربع"
                                },
                                onClick = { viewModel.selectFormat(fmt) },
                                modifier = Modifier.weight(1f),
                                enabled = state.selectedFormat != fmt,
                            )
                        }
                    }
                    if (state.assetsLoading && state.previewBitmap == null) {
                        LoadingBlock(Modifier.weight(1f))
                    } else {
                        PcsEditorPosterCanvas(
                            bitmap = state.previewBitmap,
                            sceneWidth = state.manifest?.canvas?.width ?: 1080,
                            sceneHeight = state.manifest?.canvas?.height ?: 1920,
                            interactionMode = state.interactionMode,
                            onTapScene = { x, y -> viewModel.selectLayerAt(x, y) },
                            onDragSceneStart = viewModel::beginLayerDrag,
                            onDragScene = { dx, dy -> viewModel.moveSelectedLayer(dx, dy) },
                            onDragSceneEnd = viewModel::endLayerDrag,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    PcsStudioToolbar(
                        activeTool = state.activeTool,
                        onToolSelected = { tab ->
                            when (tab) {
                                PcsStudioToolTab.Templates -> viewModel.toggleTemplatePicker(true)
                                else -> viewModel.setActiveTool(tab)
                            }
                        },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        DfSecondaryButton(
                            text = if (state.interactionMode == PcsCanvasInteractionMode.EditLayer) "جابجایی بوم" else "ویرایش لایه",
                            onClick = {
                                viewModel.setInteractionMode(
                                    if (state.interactionMode == PcsCanvasInteractionMode.EditLayer) {
                                        PcsCanvasInteractionMode.PanZoom
                                    } else {
                                        PcsCanvasInteractionMode.EditLayer
                                    },
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            state.error != null -> {
                DfEmptyState(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.screenHorizontal),
                    title = "خطا",
                    subtitle = state.error.orEmpty(),
                    actionLabel = "تلاش مجدد",
                    onAction = viewModel::refresh,
                    variant = DfEmptyVariant.Error,
                )
            }
            else -> {
                DfEmptyState(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    title = "صحنه بارگذاری نشد",
                    subtitle = "قالب یا Manifest نامعتبر است.",
                    actionLabel = "بارگذاری مجدد",
                    onAction = viewModel::refresh,
                )
            }
        }
    }
}

@Composable
private fun LoadingBlock(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text("در حال بارگذاری…", modifier = Modifier.padding(top = AppSpacing.md))
    }
}
