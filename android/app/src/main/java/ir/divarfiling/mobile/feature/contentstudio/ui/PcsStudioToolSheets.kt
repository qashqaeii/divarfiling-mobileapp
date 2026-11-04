package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.feature.contentstudio.PcsStudioToolTab
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsLayer
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioBrand
import ir.divarfiling.mobile.feature.contentstudio.canvas.PcsStudioProperty
import ir.divarfiling.mobile.feature.contentstudio.editor.PcsCanvasInteractionMode

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
    headline: String,
    aiLoading: Boolean,
    aiSuggestions: List<String>,
    canEdit: Boolean,
    onHeadlineChange: (String) -> Unit,
    onLayerSelect: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onReorder: (String, Int) -> Unit,
    onImageIndex: (Int) -> Unit,
    onRunAi: () -> Unit,
    onApplyAi: (String) -> Unit,
    onOpenTemplates: () -> Unit,
    onInteractionMode: (PcsCanvasInteractionMode) -> Unit,
) {
    if (activeTool == null || activeTool == PcsStudioToolTab.Templates) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        when (activeTool) {
            PcsStudioToolTab.Images -> ImageSheet(property, selectedLayerId, canEdit, onImageIndex)
            PcsStudioToolTab.Text -> TextSheet(headline, selectedLayerId, layers, canEdit, onHeadlineChange, onInteractionMode)
            PcsStudioToolTab.Layers -> LayersSheet(layers, layerOrder, selectedLayerId, canEdit, onLayerSelect, onToggleVisibility, onToggleLock, onReorder)
            PcsStudioToolTab.Brand -> BrandSheet(brand)
            PcsStudioToolTab.Ai -> AiSheet(aiLoading, aiSuggestions, canEdit, onRunAi, onApplyAi)
            else -> Unit
        }
    }
}

@Composable
private fun TextSheet(
    headline: String,
    selectedLayerId: String?,
    layers: List<PcsLayer>,
    canEdit: Boolean,
    onHeadlineChange: (String) -> Unit,
    onInteractionMode: (PcsCanvasInteractionMode) -> Unit,
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
        TextButton(onClick = { onInteractionMode(PcsCanvasInteractionMode.EditLayer) }) {
            Text("حالت ویرایش لایه روی بوم")
        }
        selectedLayerId?.let { id ->
            Text("لایه انتخاب‌شده: $id", modifier = Modifier.padding(top = AppSpacing.sm))
        }
    }
}

@Composable
private fun ImageSheet(
    property: PcsStudioProperty?,
    selectedLayerId: String?,
    canEdit: Boolean,
    onImageIndex: (Int) -> Unit,
) {
    val images = property?.images.orEmpty()
    Column(Modifier.padding(AppSpacing.screenHorizontal).padding(bottom = AppSpacing.lg)) {
        Text("تصاویر ملک")
        if (selectedLayerId == null) {
            Text("ابتدا یک لایه تصویر را روی بوم انتخاب کنید.")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            items(images.size) { index ->
                TextButton(
                    onClick = { if (canEdit) onImageIndex(index) },
                    enabled = canEdit && selectedLayerId != null,
                ) {
                    Text("اسلات تصویر ${index + 1}")
                }
            }
        }
    }
}

@Composable
private fun LayersSheet(
    layers: List<PcsLayer>,
    layerOrder: List<String>,
    selectedLayerId: String?,
    canEdit: Boolean,
    onSelect: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onReorder: (String, Int) -> Unit,
) {
    val order = layerOrder.ifEmpty { layers.map { it.id } }
    Column(Modifier.padding(AppSpacing.screenHorizontal).padding(bottom = AppSpacing.lg)) {
        Text("لایه‌ها")
        LazyColumn {
            items(order) { id ->
                val layer = layers.find { it.id == id } ?: return@items
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { onSelect(id) }) {
                        Text(
                            (if (selectedLayerId == id) "● " else "") +
                                (layer.name ?: layer.type) +
                                if (!layer.visible) " (مخفی)" else "",
                        )
                    }
                    Row {
                        TextButton(onClick = { if (canEdit) onReorder(id, 1) }, enabled = canEdit) { Text("↑") }
                        TextButton(onClick = { if (canEdit) onReorder(id, -1) }, enabled = canEdit) { Text("↓") }
                        TextButton(onClick = { if (canEdit) onToggleVisibility(id) }, enabled = canEdit) {
                            Text(if (layer.visible) "مخفی" else "نمایش")
                        }
                        TextButton(onClick = { if (canEdit) onToggleLock(id) }, enabled = canEdit) {
                            Text(if (layer.locked) "باز" else "قفل")
                        }
                    }
                }
            }
        }
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
    onRun: () -> Unit,
    onApply: (String) -> Unit,
) {
    Column(Modifier.padding(AppSpacing.screenHorizontal).padding(bottom = AppSpacing.lg)) {
        Text("پیشنهاد عنوان پوستر")
        DfPrimaryButton(
            text = if (loading) "در حال تولید…" else "دریافت پیشنهاد",
            onClick = onRun,
            enabled = canEdit && !loading,
            modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
        )
        suggestions.forEach { s ->
            TextButton(onClick = { onApply(s) }, modifier = Modifier.fillMaxWidth()) {
                Text(s)
            }
        }
    }
}
