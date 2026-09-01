package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    folderMetaFor: (PropertyFolderDto) -> String = { "" },
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 0.dp else AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(if (compact) AppSpacing.xxs else AppSpacing.sm),
    ) {
        if (compact) {
            FilingTierHeader(
                label = "زونکن",
                icon = DfIcons.Folder,
                showManage = folders.isNotEmpty(),
                onManage = onManageFolders,
                onAdd = onCreateFolder,
            )
        } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                ZonkanBrandIcon(size = 32.dp)
                Column {
                    Text(
                        text = "زونکن‌ها",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        text = "دسته‌بندی سریع فایل‌های مجتمع، پروژه یا محله",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                if (folders.isNotEmpty()) {
                    ZonkanActionChip(
                        icon = DfIcons.SlidersHorizontal,
                        label = "مدیریت",
                        tint = DfColors.TextSecondary,
                        onClick = onManageFolders,
                    )
                }
                ZonkanActionChip(
                    icon = DfIcons.Plus,
                    label = "جدید",
                    tint = DfColors.Purple,
                    containerColor = DfColors.PurpleContainer.copy(alpha = 0.55f),
                    onClick = onCreateFolder,
                )
            }
        }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(if (compact) AppSpacing.xs else AppSpacing.sm),
        ) {
            if (compact) {
                ZonkanCompactFolderCard(
                    name = "همه",
                    meta = "$totalCount فایل",
                    color = Color(0xFF64748B),
                    icon = "fa-layer-group",
                    selected = selectedFolderId == null,
                    pinned = false,
                    onClick = onSelectAll,
                )
                folders.forEach { folder ->
                    val suffix = folderMetaFor(folder)
                    ZonkanCompactFolderCard(
                        name = folder.name,
                        meta = buildString {
                            append("${folder.propertyCount} فایل")
                            if (suffix.isNotBlank()) append(' ').append(suffix.trim())
                        },
                        color = parseFolderColor(folder.color),
                        icon = folder.icon,
                        selected = selectedFolderId == folder.id,
                        pinned = folder.isPinned,
                        onClick = { onSelectFolder(folder) },
                    )
                }
                if (folders.isEmpty()) {
                    ZonkanCompactCreateCard(
                        title = "زونکن جدید",
                        subtitle = "مجتمع، پروژه…",
                        onClick = onCreateFolder,
                    )
                }
            } else {
            ZonkanFolderCard(
                name = "همه فایل‌ها",
                count = totalCount,
                color = Color(0xFF64748B),
                icon = "fa-layer-group",
                selected = selectedFolderId == null,
                pinned = false,
                onClick = onSelectAll,
            )
            folders.forEach { folder ->
                val suffix = folderMetaFor(folder)
                ZonkanFolderCard(
                    name = folder.name,
                    count = folder.propertyCount,
                    color = parseFolderColor(folder.color),
                    icon = folder.icon,
                    selected = selectedFolderId == folder.id,
                    pinned = folder.isPinned,
                    onClick = { onSelectFolder(folder) },
                    metaLabel = buildString {
                        append("${folder.propertyCount} فایل")
                        if (suffix.isNotBlank()) append(' ').append(suffix.trim())
                    },
                )
            }
            if (folders.isEmpty()) {
                ZonkanEmptyCreateCard(onClick = onCreateFolder)
            }
            }
        }

        if (folders.isEmpty() && !compact) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    DfIcons.Lightbulb,
                    contentDescription = null,
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "با زونکن، فایل‌های یک مجتمع یا محله را در یک کلیک فیلتر کنید — مثل «برج سپهر» یا «اجاره شمال تهران».",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }
    }
}

@Composable
internal fun ZonkanActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    containerColor: Color = DfColors.SurfaceVariant,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.Chip,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Text(label, style = AppTypography.labelSmall, color = tint, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ZonkanFolderCard(
    name: String,
    count: Int,
    color: Color,
    icon: String,
    selected: Boolean,
    pinned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    metaLabel: String? = null,
) {
    val shape = AppShapes.CardSmall
    Box(
        modifier = modifier
            .widthIn(min = 128.dp, max = 160.dp)
            .zonkanFolderGlassSurface(color = color, selected = selected, shape = shape)
            .clickable(onClick = onClick),
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.15f),
                                color.copy(alpha = 0.88f),
                                color.copy(alpha = 0.15f),
                            ),
                        ),
                    ),
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PropertyFolderIconBadge(
                    faIcon = icon,
                    color = color,
                    size = 34.dp,
                    iconSize = 16.dp,
                )
                if (pinned) {
                    Icon(
                        DfIcons.Pin,
                        contentDescription = "سنجاق‌شده",
                        tint = color,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(
                text = name,
                style = AppTypography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (selected) color.copy(alpha = 0.92f) else DfColors.TextPrimary,
            )
            Text(
                text = metaLabel ?: "$count فایل",
                style = AppTypography.labelSmall,
                color = if (selected) color.copy(alpha = 0.72f) else DfColors.TextMuted,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun ZonkanEmptyCreateCard(onClick: () -> Unit) {
    val accent = DfColors.Purple
    val shape = AppShapes.CardSmall
    Box(
        modifier = Modifier
            .widthIn(min = 128.dp, max = 160.dp)
            .zonkanFolderGlassSurface(color = accent, selected = true, shape = shape)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            PropertyFolderIconBadge(
                faIcon = "fa-folder-open",
                color = accent,
                size = 34.dp,
                iconSize = 16.dp,
            )
            Text(
                text = "اولین زونکن",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = "برای مجتمع، پروژه یا محله",
                style = AppTypography.labelSmall,
                color = accent.copy(alpha = 0.72f),
            )
        }
    }
}
