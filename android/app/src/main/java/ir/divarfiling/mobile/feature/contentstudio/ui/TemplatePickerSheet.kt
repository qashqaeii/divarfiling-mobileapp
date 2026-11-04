package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
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
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "انتخاب قالب",
            subtitle = "پوستر حرفه‌ای برای شبکه‌های اجتماعی",
            icon = DfIcons.LayoutGrid,
            onClose = onDismiss,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    horizontal = AppSpacing.screenHorizontal,
                    vertical = AppSpacing.sm,
                ),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(templates, key = { it.templateId }) { item ->
                    val selected = item.templateId == selectedTemplateId
                    DfPremiumCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (selected) {
                                    Modifier.border(2.dp, DfThemeColors.primary(), AppShapes.Card)
                                } else {
                                    Modifier
                                },
                            ),
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
                                    .height(132.dp)
                                    .padding(bottom = AppSpacing.xs),
                            )
                            Text(
                                item.label.ifBlank { item.templateId },
                                style = AppTypography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                            if (item.description.isNotBlank()) {
                                Text(
                                    item.description,
                                    style = AppTypography.meta,
                                    color = DfThemeColors.textSecondary(),
                                    maxLines = 2,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
