package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.PropertyFolderDto

private fun parseFolderColor(hex: String): Color {
    val cleaned = hex.trim().removePrefix("#")
    if (cleaned.length != 6) return DfColors.Purple
    return try {
        Color(
            red = cleaned.substring(0, 2).toInt(16) / 255f,
            green = cleaned.substring(2, 4).toInt(16) / 255f,
            blue = cleaned.substring(4, 6).toInt(16) / 255f,
        )
    } catch (_: Exception) {
        DfColors.Purple
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyFolderPicker(
    property: PropertyDto,
    availableFolders: List<PropertyFolderDto>,
    isSubmitting: Boolean,
    onToggle: (folderId: Long, add: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (availableFolders.isEmpty()) return

    val assignedIds = property.folders.map { it.id }.toSet()

    DfPremiumCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = DfIcons.Folder,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(18.dp),
                )
                Column {
                    Text(
                        text = "زونکن‌ها",
                        style = AppTypography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        text = "این فایل در کدام مجموعه‌ها قرار دارد؟",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                availableFolders.forEach { folder ->
                    val selected = folder.id in assignedIds
                    val color = parseFolderColor(folder.color)
                    Surface(
                        onClick = {
                            if (!isSubmitting) onToggle(folder.id, !selected)
                        },
                        shape = AppShapes.Chip,
                        color = if (selected) color.copy(alpha = 0.14f) else DfColors.SurfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (selected) color.copy(alpha = 0.55f) else DfColors.OutlineSubtle,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = DfIcons.Folder,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = folder.name,
                                style = AppTypography.labelSmall,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = DfColors.TextPrimary,
                            )
                            if (selected) {
                                Icon(
                                    imageVector = DfIcons.Check,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
