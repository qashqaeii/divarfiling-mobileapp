package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.DealStageDefDto
import ir.divarfiling.mobile.core.network.DealStagePresetDto

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DealStagesEditorSheet(
    definitions: List<DealStageDefDto>,
    presets: List<DealStagePresetDto>,
    counts: Map<String, Int>,
    colors: List<String>,
    icons: List<String>,
    expandedId: String?,
    isSaving: Boolean,
    onPreset: (String) -> Unit,
    onAdd: () -> Unit,
    onReset: () -> Unit,
    onExpand: (String?) -> Unit,
    onNameChange: (String, String) -> Unit,
    onDescriptionChange: (String, String) -> Unit,
    onProbabilityChange: (String, Int) -> Unit,
    onRotDaysChange: (String, Int) -> Unit,
    onColorChange: (String, String) -> Unit,
    onIconChange: (String, String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onRemove: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = colors.ifEmpty {
        listOf(
            "#475569", "#1d4ed8", "#0ea5e9", "#0d9488", "#15803d",
            "#b45309", "#be185d", "#4338ca", "#7c3aed", "#b91c1c",
        )
    }
    val iconKeys = icons.ifEmpty {
        listOf(
            "fa-seedling", "fa-lightbulb", "fa-phone", "fa-comments", "fa-handshake",
            "fa-door-open", "fa-building", "fa-file-signature", "fa-file-contract",
            "fa-key", "fa-trophy", "fa-circle-xmark",
        )
    }

    DfSheetScaffold(
        title = "طراحی قیف فروش",
        subtitle = "مراحل، رنگ، احتمال بستن و مهلت پیگیری را مثل میزکار وب تنظیم کنید",
        icon = DfIcons.SlidersHorizontal,
        onClose = onDismiss,
        bodyHeightFraction = 0.9f,
        footer = {
            DfSheetActions(
                primaryText = if (isSaving) "در حال ذخیره…" else "ذخیره قیف",
                onPrimary = onSave,
                primaryEnabled = !isSaving && definitions.size >= 3,
                isSubmitting = isSaving,
                onSecondary = onDismiss,
            )
        },
    ) {
        if (presets.isNotEmpty()) {
            DfSheetSection(title = "قالب‌های آماده") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    presets.forEach { preset ->
                        Surface(
                            onClick = { onPreset(preset.id) },
                            enabled = !isSaving,
                            shape = AppShapes.Chip,
                            color = DfColors.PurpleContainer.copy(alpha = 0.55f),
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = preset.name,
                                    style = AppTypography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DfColors.Purple,
                                )
                                Text(
                                    text = "${preset.stageNames.ifEmpty { preset.definitions.map { it.name } }.size} مرحله",
                                    style = AppTypography.labelSmall,
                                    color = DfColors.TextMuted,
                                )
                            }
                        }
                    }
                }
            }
        }

        DfSheetSection(title = "پیش‌نمایش قیف") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
            ) {
                definitions.forEach { def ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(DealUiUtils.hexColor(def.color)),
                    )
                }
            }
        }

        DfSheetSection(title = "مراحل") {
            definitions.forEachIndexed { index, def ->
                DealStageEditorCard(
                    def = def,
                    count = counts[def.name] ?: 0,
                    expanded = expandedId == def.id,
                    canMoveUp = !def.locked && index > 0 && !definitions.getOrNull(index - 1)?.locked.orFalse(),
                    canMoveDown = !def.locked && index < definitions.lastIndex &&
                        !definitions.getOrNull(index + 1)?.locked.orFalse(),
                    palette = palette,
                    icons = iconKeys,
                    enabled = !isSaving,
                    onExpand = { onExpand(if (expandedId == def.id) null else def.id) },
                    onNameChange = { onNameChange(def.id, it) },
                    onDescriptionChange = { onDescriptionChange(def.id, it) },
                    onProbabilityChange = { onProbabilityChange(def.id, it) },
                    onRotDaysChange = { onRotDaysChange(def.id, it) },
                    onColorChange = { onColorChange(def.id, it) },
                    onIconChange = { onIconChange(def.id, it) },
                    onMoveUp = { onMoveUp(def.id) },
                    onMoveDown = { onMoveDown(def.id) },
                    onRemove = { onRemove(def.id) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DfPrimaryButton(
                text = "مرحله جدید",
                onClick = onAdd,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            )
            DfSecondaryButton(
                text = "پیش‌فرض",
                onClick = onReset,
                enabled = !isSaving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DealStageEditorCard(
    def: DealStageDefDto,
    count: Int,
    expanded: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    palette: List<String>,
    icons: List<String>,
    enabled: Boolean,
    onExpand: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onProbabilityChange: (Int) -> Unit,
    onRotDaysChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    val accent = DealUiUtils.hexColor(def.color)
    val bg = DealUiUtils.hexColor(def.bg, DfColors.SurfaceVariant)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = if (expanded) bg.copy(alpha = 0.45f) else DfColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(AppShapes.IconContainer)
                        .background(bg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = DealUiUtils.stageIconVector(def.icon),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (def.locked) {
                        Text(
                            text = def.name,
                            style = AppTypography.bodyDescription,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                        )
                    } else {
                        OutlinedTextField(
                            value = def.name,
                            onValueChange = onNameChange,
                            singleLine = true,
                            enabled = enabled,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("نام مرحله") },
                        )
                    }
                    Text(
                        text = "$count معامله · احتمال ${def.probability}٪" +
                            if (def.rotDays > 0) " · کهنگی ${def.rotDays} روز" else "",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onExpand, enabled = enabled) {
                    Icon(
                        imageVector = if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                        contentDescription = "جزئیات",
                        tint = DfColors.TextSecondary,
                    )
                }
            }
            if (!def.locked) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onMoveUp, enabled = enabled && canMoveUp) {
                        Icon(DfIcons.ChevronUp, contentDescription = "بالا")
                    }
                    IconButton(onClick = onMoveDown, enabled = enabled && canMoveDown) {
                        Icon(DfIcons.ChevronDown, contentDescription = "پایین")
                    }
                    IconButton(onClick = onRemove, enabled = enabled) {
                        Icon(DfIcons.Trash, contentDescription = "حذف", tint = DfColors.OverdueAccent)
                    }
                }
            }
            if (expanded) {
                OutlinedTextField(
                    value = def.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    label = { Text("توضیح کوتاه") },
                    minLines = 2,
                )
                if (!def.locked) {
                    Text(
                        text = "احتمال بستن ${def.probability}٪",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                    )
                    Slider(
                        value = def.probability.toFloat(),
                        onValueChange = { onProbabilityChange(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = enabled,
                    )
                    OutlinedTextField(
                        value = def.rotDays.toString(),
                        onValueChange = { onRotDaysChange(it.filter(Char::isDigit).toIntOrNull() ?: 0) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        label = { Text("مهلت کهنگی (روز)") },
                        singleLine = true,
                    )
                }
                Text("رنگ", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { hex ->
                        val selected = hex.equals(def.color, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DealUiUtils.hexColor(hex))
                                .then(
                                    if (selected) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier,
                                )
                                .clickableIf(enabled) { onColorChange(hex) },
                        )
                    }
                }
                Text("آیکون", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    icons.forEach { icon ->
                        val selected = icon == def.icon
                        Surface(
                            onClick = { onIconChange(icon) },
                            enabled = enabled,
                            shape = AppShapes.Chip,
                            color = if (selected) bg else DfColors.SurfaceVariant,
                        ) {
                            Icon(
                                imageVector = DealUiUtils.stageIconVector(icon),
                                contentDescription = icon,
                                tint = if (selected) accent else DfColors.TextSecondary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Boolean?.orFalse(): Boolean = this == true

private fun Modifier.clickableIf(enabled: Boolean, onClick: () -> Unit): Modifier {
    if (!enabled) return this
    return this.clickable(onClick = onClick)
}
