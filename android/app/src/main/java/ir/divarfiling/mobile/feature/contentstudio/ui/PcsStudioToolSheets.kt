package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.feature.contentstudio.PcsStudioToolTab
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsContentResolver
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsSceneDocument
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioBrand
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioProperty
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsTextStyle
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsCanvasInteractionMode
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsLayerEditorRules

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcsStudioToolSheets(
    activeTool: PcsStudioToolTab?,
    onDismiss: () -> Unit,
    property: PcsStudioProperty?,
    brand: PcsStudioBrand?,
    layers: List<PcsLayer>,
    layerOrder: List<String>,
    selectedLayerId: String?,
    selectedLayer: PcsLayer?,
    headline: String,
    aiLoading: Boolean,
    aiSuggestions: List<String>,
    canEdit: Boolean,
    snapEnabled: Boolean,
    baseScene: PcsSceneDocument?,
    onHeadlineChange: (String) -> Unit,
    onLayerSelect: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onReorder: (String, Int) -> Unit,
    onImageIndex: (Int) -> Unit,
    onRunAi: () -> Unit,
    onRunAiAction: (String) -> Unit,
    onApplyAi: (String) -> Unit,
    onOpenTemplates: () -> Unit,
    onInteractionMode: (PcsCanvasInteractionMode) -> Unit,
    onToggleSnap: () -> Unit,
    onRestoreLinkedText: () -> Unit,
    onResetLayer: () -> Unit,
    onLayerTextChange: (String) -> Unit,
    onLayerStyleChange: (PcsTextStyle) -> Unit,
    onLayerOpacityChange: (Float) -> Unit,
    onCropChange: (Float?, Float?, Float?) -> Unit,
    onObjectFitChange: (String) -> Unit,
) {
    if (activeTool == null || activeTool == PcsStudioToolTab.Templates) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedLayer = selectedLayerId?.let { id -> layers.find { it.id == id } }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when (activeTool) {
            PcsStudioToolTab.Images -> ImageSheet(
                property = property,
                selectedLayer = selectedLayer,
                canEdit = canEdit,
                onImageIndex = onImageIndex,
                onCropChange = onCropChange,
                onObjectFitChange = onObjectFitChange,
                onLayerOpacityChange = onLayerOpacityChange,
                onLayerStyleChange = onLayerStyleChange,
            )
            PcsStudioToolTab.Text -> TextSheet(
                headline = headline,
                property = property,
                selectedLayer = selectedLayer,
                canEdit = canEdit,
                onHeadlineChange = onHeadlineChange,
                onInteractionMode = onInteractionMode,
                onLayerTextChange = onLayerTextChange,
                onLayerStyleChange = onLayerStyleChange,
                onRestoreLinkedText = onRestoreLinkedText,
            )
            PcsStudioToolTab.Layers -> LayersSheet(
                layers = layers,
                layerOrder = layerOrder,
                selectedLayerId = selectedLayerId,
                canEdit = canEdit,
                snapEnabled = snapEnabled,
                onSelect = onLayerSelect,
                onToggleVisibility = onToggleVisibility,
                onToggleLock = onToggleLock,
                onReorder = onReorder,
                onToggleSnap = onToggleSnap,
                onResetLayer = onResetLayer,
            )
            PcsStudioToolTab.Brand -> BrandSheet(brand)
            PcsStudioToolTab.Ai -> AiSheet(aiLoading, aiSuggestions, canEdit, onRunAi, onRunAiAction, onApplyAi)
            else -> Unit
        }
    }
}

@Composable
private fun TextSheet(
    headline: String,
    property: PcsStudioProperty?,
    selectedLayer: PcsLayer?,
    canEdit: Boolean,
    onHeadlineChange: (String) -> Unit,
    onInteractionMode: (PcsCanvasInteractionMode) -> Unit,
    onLayerTextChange: (String) -> Unit,
    onLayerStyleChange: (PcsTextStyle) -> Unit,
    onRestoreLinkedText: () -> Unit,
) {
    Column(Modifier.padding(AppSpacing.screenHorizontal).padding(bottom = AppSpacing.lg)) {
        Text("ویرایش متن")
        OutlinedTextField(
            value = headline,
            onValueChange = { if (canEdit) onHeadlineChange(it) },
            modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
            label = { Text("عنوان پوستر") },
            enabled = canEdit,
        )
        selectedLayer?.takeIf { it.type in setOf("text", "bound_text") }?.let { layer ->
            val resolved = property?.let {
                PcsContentResolver.resolveLayerText(layer, it, headline, "")
            }.orEmpty()
            var layerText by remember(layer.id, layer.text, resolved) {
                mutableStateOf(layer.text ?: resolved)
            }
            OutlinedTextField(
                value = layerText,
                onValueChange = {
                    layerText = it
                    if (canEdit) onLayerTextChange(it)
                },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
                label = { Text(layer.name ?: "متن لایه") },
                enabled = canEdit,
            )
            Text(
                "حالت اتصال: ${layer.binding?.mode ?: "linked"}",
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
            DfSecondaryButton(
                text = "بازگشت به متن متصل",
                onClick = onRestoreLinkedText,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xs),
            )
            val style = layer.style
            var fontSize by remember(layer.id) { mutableFloatStateOf(style?.fontSize ?: 32f) }
            Text("اندازه فونت: ${fontSize.toInt()}")
            Slider(
                value = fontSize,
                onValueChange = {
                    fontSize = it
                    if (canEdit) onLayerStyleChange(PcsTextStyle(fontSize = it))
                },
                valueRange = 12f..96f,
                enabled = canEdit,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                DfSecondaryButton(
                    text = "RTL",
                    onClick = { if (canEdit) onLayerStyleChange(PcsTextStyle(direction = "rtl")) },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f),
                )
                DfSecondaryButton(
                    text = "LTR",
                    onClick = { if (canEdit) onLayerStyleChange(PcsTextStyle(direction = "ltr")) },
                    enabled = canEdit,
                    modifier = Modifier.weight(1f),
                )
            }
        } ?: Text("یک لایه متن را روی بوم انتخاب کنید.", modifier = Modifier.padding(top = AppSpacing.sm))
        TextButton(onClick = { onInteractionMode(PcsCanvasInteractionMode.EditLayer) }) {
            Text("حالت ویرایش لایه")
        }
    }
}

@Composable
private fun ImageSheet(
    property: PcsStudioProperty?,
    selectedLayer: PcsLayer?,
    canEdit: Boolean,
    onImageIndex: (Int) -> Unit,
    onCropChange: (Float?, Float?, Float?) -> Unit,
    onObjectFitChange: (String) -> Unit,
    onLayerOpacityChange: (Float) -> Unit,
    onLayerStyleChange: (PcsTextStyle) -> Unit,
) {
    val images = property?.images.orEmpty()
    Column(Modifier.padding(AppSpacing.screenHorizontal).padding(bottom = AppSpacing.lg)) {
        Text("تصاویر و Crop")
        if (selectedLayer == null || selectedLayer.type !in setOf("image", "decor", "logo")) {
            Text("یک لایه تصویر را انتخاب کنید.")
            return@Column
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            items(images.size) { index ->
                TextButton(onClick = { if (canEdit) onImageIndex(index) }, enabled = canEdit) {
                    AsyncImage(
                        model = images[index],
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        val style = selectedLayer.style
        var panX by remember(selectedLayer.id) { mutableFloatStateOf(style?.cropPanX ?: 50f) }
        var panY by remember(selectedLayer.id) { mutableFloatStateOf(style?.cropPanY ?: 50f) }
        var zoom by remember(selectedLayer.id) { mutableFloatStateOf(style?.cropZoom ?: 1f) }
        Text("Pan X")
        Slider(panX, { panX = it; if (canEdit) onCropChange(it, null, null) }, 0f..100f, enabled = canEdit)
        Text("Pan Y")
        Slider(panY, { panY = it; if (canEdit) onCropChange(null, it, null) }, 0f..100f, enabled = canEdit)
        Text("Zoom")
        Slider(zoom, { zoom = it; if (canEdit) onCropChange(null, null, it) }, 0.5f..3f, enabled = canEdit)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            DfSecondaryButton(
                text = "Cover",
                onClick = { if (canEdit) onObjectFitChange("cover") },
                modifier = Modifier.weight(1f),
                enabled = canEdit,
            )
            DfSecondaryButton(
                text = "Contain",
                onClick = { if (canEdit) onObjectFitChange("contain") },
                modifier = Modifier.weight(1f),
                enabled = canEdit,
            )
        }
        DfSecondaryButton(
            text = "Reset Crop",
            onClick = {
                panX = 50f
                panY = 50f
                zoom = 1f
                if (canEdit) onCropChange(50f, 50f, 1f)
            },
            enabled = canEdit,
            modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xs),
        )
        var brightness by remember(selectedLayer.id) { mutableFloatStateOf(style?.brightness ?: 100f) }
        Text("Brightness")
        Slider(
            brightness,
            {
                brightness = it
                if (canEdit) onLayerStyleChange(PcsTextStyle(brightness = it))
            },
            40f..160f,
            enabled = canEdit,
        )
        var opacity by remember(selectedLayer.id) { mutableFloatStateOf(selectedLayer.opacity) }
        Text("Opacity")
        Slider(opacity, { opacity = it; if (canEdit) onLayerOpacityChange(it) }, 0f..1f, enabled = canEdit)
    }
}

@Composable
private fun LayersSheet(
    layers: List<PcsLayer>,
    layerOrder: List<String>,
    selectedLayerId: String?,
    canEdit: Boolean,
    snapEnabled: Boolean,
    onSelect: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onReorder: (String, Int) -> Unit,
    onToggleSnap: () -> Unit,
    onResetLayer: () -> Unit,
) {
    val order = layerOrder.ifEmpty { layers.map { it.id } }
    Column(Modifier.padding(AppSpacing.screenHorizontal).padding(bottom = AppSpacing.lg)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("لایه‌ها")
            Row {
                Text("Snap")
                Switch(checked = snapEnabled, onCheckedChange = { onToggleSnap() })
            }
        }
        LazyColumn(modifier = Modifier.height(320.dp)) {
            items(order) { id ->
                val layer = layers.find { it.id == id } ?: return@items
                val protected = PcsLayerEditorRules.isStructureProtected(layer)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { onSelect(id) }) {
                        Text(
                            buildString {
                                if (selectedLayerId == id) append("● ")
                                append(layer.name ?: layer.type)
                                append(" · ")
                                append(layer.type)
                                if (!layer.visible) append(" · مخفی")
                                if (layer.locked) append(" · قفل")
                            },
                        )
                    }
                    Row {
                        TextButton(
                            onClick = { if (canEdit) onReorder(id, 1) },
                            enabled = canEdit && PcsLayerEditorRules.canReorder(layer),
                        ) { Text("↑") }
                        TextButton(
                            onClick = { if (canEdit) onReorder(id, -1) },
                            enabled = canEdit && PcsLayerEditorRules.canReorder(layer),
                        ) { Text("↓") }
                        TextButton(
                            onClick = { if (canEdit) onToggleVisibility(id) },
                            enabled = canEdit && PcsLayerEditorRules.canToggleVisibility(layer),
                        ) {
                            Text(if (layer.visible) "مخفی" else "نمایش")
                        }
                        TextButton(
                            onClick = { if (canEdit) onToggleLock(id) },
                            enabled = canEdit && PcsLayerEditorRules.canToggleLock(layer) && !protected,
                        ) {
                            Text(if (layer.locked) "باز" else "قفل")
                        }
                    }
                }
            }
        }
        DfSecondaryButton(
            text = "بازنشانی لایه به قالب",
            onClick = onResetLayer,
            enabled = canEdit && selectedLayerId != null,
            modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
        )
    }
}

@Composable
private fun BrandSheet(brand: PcsStudioBrand?) {
    Column(Modifier.padding(AppSpacing.screenHorizontal).padding(bottom = AppSpacing.lg)) {
        Text("برند")
        Text("نام: ${brand?.consultantName.orEmpty().ifBlank { brand?.brandName.orEmpty() }}")
        Text("تلفن: ${brand?.contactPhone.orEmpty()}")
        Text("نمایش بر اساس تنظیمات workspace و visibility سرور.")
    }
}

@Composable
private fun AiSheet(
    loading: Boolean,
    suggestions: List<String>,
    canEdit: Boolean,
    onRunHeadlines: () -> Unit,
    onRunAction: (String) -> Unit,
    onApply: (String) -> Unit,
) {
    Column(Modifier.padding(AppSpacing.screenHorizontal).padding(bottom = AppSpacing.lg)) {
        Text("هوش مصنوعی")
        DfPrimaryButton(
            text = if (loading) "…" else "عناوین پوستر",
            onClick = onRunHeadlines,
            enabled = canEdit && !loading,
            modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        )
        listOf(
            "divar-titles" to "عناوین دیوار",
            "divar-body" to "متن دیوار",
            "social" to "بسته شبکه‌های اجتماعی",
        ).forEach { (action, label) ->
            DfPrimaryButton(
                text = label,
                onClick = { onRunAction(action) },
                enabled = canEdit && !loading,
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
            )
        }
        suggestions.forEach { s ->
            TextButton(onClick = { onApply(s) }, modifier = Modifier.fillMaxWidth()) {
                Text(s)
            }
        }
    }
}
