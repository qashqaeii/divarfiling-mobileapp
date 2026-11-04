package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.network.ContentStudioTemplateCatalogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerSheet(
    visible: Boolean,
    templates: List<ContentStudioTemplateCatalogItem>,
    selectedTemplateId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "انتخاب قالب",
            style = AppTypography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(AppSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(templates, key = { it.templateId }) { item ->
                DfPremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onSelect(item.templateId)
                        onDismiss()
                    },
                ) {
                    Column(modifier = Modifier.padding(AppSpacing.sm)) {
                        AsyncImage(
                            model = item.overlayUrl,
                            contentDescription = item.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .padding(bottom = AppSpacing.xs),
                        )
                        Text(
                            item.label.ifBlank { item.templateId },
                            style = AppTypography.labelSmall,
                            fontWeight = if (item.templateId == selectedTemplateId) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
