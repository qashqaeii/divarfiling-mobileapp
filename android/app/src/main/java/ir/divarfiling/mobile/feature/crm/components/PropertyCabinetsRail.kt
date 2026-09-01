package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
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
import ir.divarfiling.mobile.core.network.PropertyCabinetDto

@Composable
fun PropertyCabinetsRail(
    cabinets: List<PropertyCabinetDto>,
    selectedCabinetId: String?,
    unassignedFolderCount: Int,
    onSelectAll: () -> Unit,
    onSelectCabinet: (PropertyCabinetDto) -> Unit,
    onSelectUnassigned: () -> Unit,
    onCreateCabinet: () -> Unit,
    onManageCabinets: () -> Unit = {},
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
                label = "کمد",
                icon = DfIcons.Briefcase,
                showManage = cabinets.isNotEmpty(),
                onManage = onManageCabinets,
                onAdd = onCreateCabinet,
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
                        text = "کمدها",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        text = "گروه‌بندی زونکن‌ها",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                if (cabinets.isNotEmpty()) {
                    ZonkanActionChip(
                        icon = DfIcons.SlidersHorizontal,
                        label = "مدیریت",
                        tint = DfColors.TextSecondary,
                        onClick = onManageCabinets,
                    )
                }
                ZonkanActionChip(
                    icon = DfIcons.Plus,
                    label = "جدید",
                    tint = DfColors.Purple,
                    containerColor = DfColors.PurpleContainer.copy(alpha = 0.55f),
                    onClick = onCreateCabinet,
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
                    meta = "همه فایل‌ها",
                    color = Color(0xFF475569),
                    icon = "fa-layer-group",
                    selected = selectedCabinetId == null,
                    pinned = false,
                    onClick = onSelectAll,
                )
                cabinets.forEach { cabinet ->
                    ZonkanCompactFolderCard(
                        name = cabinet.name,
                        meta = "${cabinet.folderCount} زونکن · ${cabinet.propertyCount} فایل",
                        color = parseFolderColor(cabinet.color),
                        icon = cabinet.icon,
                        selected = selectedCabinetId == cabinet.id.toString(),
                        pinned = cabinet.isPinned,
                        onClick = { onSelectCabinet(cabinet) },
                    )
                }
                if (unassignedFolderCount > 0) {
                    ZonkanCompactFolderCard(
                        name = "بدون کمد",
                        meta = "$unassignedFolderCount زونکن",
                        color = Color(0xFF94A3B8),
                        icon = "fa-folder-open",
                        selected = selectedCabinetId == "none",
                        pinned = false,
                        onClick = onSelectUnassigned,
                    )
                }
                if (cabinets.isEmpty()) {
                    ZonkanCompactCreateCard(
                        title = "کمد جدید",
                        subtitle = "فروش، اجاره…",
                        onClick = onCreateCabinet,
                    )
                }
            } else {
            ZonkanFolderCard(
                name = "همه کمدها",
                count = 0,
                color = Color(0xFF475569),
                icon = "fa-layer-group",
                selected = selectedCabinetId == null,
                pinned = false,
                onClick = onSelectAll,
                metaLabel = "همه فایل‌ها",
            )
            cabinets.forEach { cabinet ->
                ZonkanFolderCard(
                    name = cabinet.name,
                    count = cabinet.folderCount,
                    color = parseFolderColor(cabinet.color),
                    icon = cabinet.icon,
                    selected = selectedCabinetId == cabinet.id.toString(),
                    pinned = cabinet.isPinned,
                    onClick = { onSelectCabinet(cabinet) },
                    metaLabel = "${cabinet.folderCount} زونکن · ${cabinet.propertyCount} فایل",
                )
            }
            if (unassignedFolderCount > 0) {
                ZonkanFolderCard(
                    name = "بدون کمد",
                    count = unassignedFolderCount,
                    color = Color(0xFF94A3B8),
                    icon = "fa-folder-open",
                    selected = selectedCabinetId == "none",
                    pinned = false,
                    onClick = onSelectUnassigned,
                    metaLabel = "$unassignedFolderCount زونکن",
                )
            }
            if (cabinets.isEmpty()) {
                CabinetEmptyCreateCard(onClick = onCreateCabinet)
            }
            }
        }
    }
}

@Composable
private fun CabinetEmptyCreateCard(onClick: () -> Unit) {
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
                text = "اولین کمد",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                text = "مثل فروش یا اجاره",
                style = AppTypography.labelSmall,
                color = accent.copy(alpha = 0.72f),
            )
        }
    }
}
