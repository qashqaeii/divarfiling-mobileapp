package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.network.PropertyCabinetDto
import ir.divarfiling.mobile.feature.crm.PropertyCabinetConstants

@Composable
fun PropertyCabinetsManageSheet(
    cabinets: List<PropertyCabinetDto>,
    isSavingOrder: Boolean,
    onMoveUp: (Long) -> Unit,
    onMoveDown: (Long) -> Unit,
    onPin: (PropertyCabinetDto) -> Unit,
    onEdit: (PropertyCabinetDto) -> Unit,
    onDelete: (PropertyCabinetDto) -> Unit,
    onSaveOrder: () -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ZonkanBrandIcon(size = 36.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "مدیریت کمدها",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                )
                Text(
                    text = "ترتیب، سنجاق، ویرایش یا حذف",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            itemsIndexed(cabinets, key = { _, item -> item.id }) { index, cabinet ->
                ManageCabinetRow(
                    cabinet = cabinet,
                    canMoveUp = index > 0,
                    canMoveDown = index < cabinets.lastIndex,
                    onMoveUp = { onMoveUp(cabinet.id) },
                    onMoveDown = { onMoveDown(cabinet.id) },
                    onPin = { onPin(cabinet) },
                    onEdit = { onEdit(cabinet) },
                    onDelete = { onDelete(cabinet) },
                )
            }
        }

        DfSecondaryButton(text = "کمد جدید", onClick = onCreateNew, modifier = Modifier.fillMaxWidth())
        DfPrimaryButton(
            text = if (isSavingOrder) "در حال ذخیره…" else "ذخیره ترتیب",
            onClick = onSaveOrder,
            enabled = !isSavingOrder && cabinets.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ManageCabinetRow(
    cabinet: PropertyCabinetDto,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onPin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = parseFolderColor(cabinet.color)
    Surface(
        shape = AppShapes.CardSmall,
        color = if (cabinet.isPinned) color.copy(alpha = 0.08f) else DfColors.SurfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(
            1.dp,
            if (cabinet.isPinned) color.copy(alpha = 0.35f) else DfColors.OutlineSubtle,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Icon(DfIcons.GripVertical, contentDescription = null, tint = DfColors.TextMuted, modifier = Modifier.size(16.dp))
            Column {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(28.dp)) {
                    Icon(DfIcons.ChevronUp, contentDescription = "بالا", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(28.dp)) {
                    Icon(DfIcons.ChevronDown, contentDescription = "پایین", modifier = Modifier.size(16.dp))
                }
            }
            PropertyFolderIconBadge(faIcon = cabinet.icon, color = color, size = 36.dp, iconSize = 16.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cabinet.name,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${cabinet.folderCount} زونکن · ${cabinet.propertyCount} فایل",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onPin) {
                Icon(
                    DfIcons.Pin,
                    contentDescription = "سنجاق",
                    tint = if (cabinet.isPinned) color else DfColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = onEdit) {
                Icon(DfIcons.Pencil, contentDescription = "ویرایش", tint = DfColors.TextSecondary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(DfIcons.Trash, contentDescription = "حذف", tint = DfColors.OverdueAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyCabinetFormSheet(
    title: String,
    name: String,
    description: String,
    selectedColor: String,
    selectedIcon: String,
    isPinned: Boolean,
    isSubmitting: Boolean,
    showDelete: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onPinnedChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val previewColor = parseFolderColor(selectedColor)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ZonkanBrandIcon(size = 36.dp)
            Text(title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold, color = DfColors.TextPrimary)
        }
        ZonkanFolderCard(
            name = name.trim().ifBlank { "نام کمد" },
            count = 0,
            color = previewColor,
            icon = selectedIcon,
            selected = true,
            pinned = isPinned,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            metaLabel = "پیش‌نمایش",
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("نام کمد") },
            placeholder = { Text("مثلاً فروش، اجاره، پروژه‌ها…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("توضیح (اختیاری)") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("سنجاق در ابتدای لیست", style = AppTypography.labelLarge)
            Switch(checked = isPinned, onCheckedChange = onPinnedChange)
        }
        Text("رنگ", style = AppTypography.labelLarge, color = DfColors.TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            PropertyCabinetConstants.COLORS.forEach { hex ->
                val c = parseFolderColor(hex)
                Surface(
                    onClick = { onColorChange(hex) },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = c,
                    border = BorderStroke(2.dp, if (hex == selectedColor) DfColors.TextPrimary else Color.Transparent),
                    modifier = Modifier.size(36.dp),
                ) {}
            }
        }
        Text("آیکن", style = AppTypography.labelLarge, color = DfColors.TextSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            PropertyCabinetConstants.ICONS.forEach { faIcon ->
                val isSelected = faIcon == selectedIcon
                Surface(
                    onClick = { onIconChange(faIcon) },
                    shape = AppShapes.Chip,
                    color = if (isSelected) previewColor.copy(alpha = 0.14f) else DfColors.SurfaceVariant,
                    border = BorderStroke(1.5.dp, if (isSelected) previewColor.copy(alpha = 0.6f) else DfColors.OutlineSubtle),
                ) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Icon(
                            imageVector = folderIconVector(faIcon),
                            contentDescription = faIcon,
                            tint = if (isSelected) previewColor else DfColors.TextSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
        if (showDelete) {
            DfSecondaryButton(text = "حذف کمد", onClick = onDelete, modifier = Modifier.fillMaxWidth())
        }
        DfPrimaryButton(
            text = if (isSubmitting) "در حال ذخیره…" else "ذخیره کمد",
            onClick = onSubmit,
            enabled = !isSubmitting && name.trim().isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
