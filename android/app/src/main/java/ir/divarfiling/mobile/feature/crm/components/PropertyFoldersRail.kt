package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.network.PropertyFolderDto

@Composable
fun PropertyFoldersRail(
    folders: List<PropertyFolderDto>,
    selectedFolderId: Long?,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onSelectFolder: (PropertyFolderDto) -> Unit,
    onCreateFolder: () -> Unit,
    onManageFolders: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "زونکن‌ها",
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextPrimary,
                )
                Text(
                    text = "دسته‌بندی سریع فایل‌های مجتمع و پروژه",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            Surface(
                onClick = onManageFolders,
                enabled = folders.isNotEmpty(),
                shape = AppShapes.Chip,
                color = DfColors.SurfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(DfIcons.SlidersHorizontal, contentDescription = null, tint = DfColors.TextSecondary, modifier = Modifier.size(14.dp))
                    Text("مدیریت", style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                }
            }
            Surface(
                onClick = onCreateFolder,
                shape = AppShapes.Chip,
                color = DfColors.PurpleContainer.copy(alpha = 0.55f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(DfIcons.Plus, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(14.dp))
                    Text("جدید", style = AppTypography.labelSmall, color = DfColors.Purple)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            FolderChip(
                label = "همه",
                count = totalCount,
                color = DfColors.TextSecondary,
                selected = selectedFolderId == null,
                onClick = onSelectAll,
            )
            folders.forEach { folder ->
                FolderChip(
                    label = folder.name,
                    count = folder.propertyCount,
                    color = parseFolderColor(folder.color),
                    selected = selectedFolderId == folder.id,
                    pinned = folder.isPinned,
                    onClick = { onSelectFolder(folder) },
                )
            }
        }
        if (folders.isEmpty()) {
            Text(
                text = "اولین زونکن را بسازید تا فایل‌های مجتمع را سریع‌تر پیدا کنید",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
    }
}

@Composable
private fun FolderChip(
    label: String,
    count: Int,
    color: Color,
    selected: Boolean,
    pinned: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
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
                modifier = Modifier.size(16.dp),
            )
            if (pinned) {
                Icon(
                    imageVector = DfIcons.Bookmark,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp),
                )
            }
            Column {
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = DfColors.TextPrimary,
                )
                Text(
                    text = "$count فایل",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }
    }
}
