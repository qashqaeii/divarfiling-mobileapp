package ir.divarfiling.mobile.feature.contracts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextField
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun HandoverWizardStepper(
    wizard: JsonObject?,
    currentStep: String,
    onStepClick: (String) -> Unit,
) {
    val steps = wizard?.get("display_steps")?.jsonArray?.mapNotNull { el ->
        val o = el as? kotlinx.serialization.json.JsonObject
        if (o != null) {
            o.get("key")?.jsonPrimitive?.contentOrNull to o.get("label")?.jsonPrimitive?.contentOrNull
        } else {
            val arr = el as? kotlinx.serialization.json.JsonArray
            arr?.getOrNull(0)?.jsonPrimitive?.contentOrNull to arr?.getOrNull(1)?.jsonPrimitive?.contentOrNull
        }
    }.orEmpty()
    val progress = wizard?.get("progress_pct")?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            steps.forEach { (key, label) ->
                if (key.isNullOrBlank()) return@forEach
                DfSoftChip(
                    text = label ?: key,
                    selected = key == currentStep,
                    onClick = { onStepClick(key) },
                )
            }
        }
    }
}

@Composable
fun HandoverBasicStepContent(
    state: HandoverWizardUiState,
    enabled: Boolean,
    onChange: (String?, String?, Int?, Int?, String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("مشخصات تحویل", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        DfTextField(
            value = state.handoverDate,
            onValueChange = { onChange(it, null, null, null, null) },
            label = "تاریخ تحویل (شمسی)",
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
        DfTextField(
            value = state.handoverTime,
            onValueChange = { onChange(null, it, null, null, null) },
            label = "ساعت (HH:MM)",
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
        PartyPickSection("تحویل‌دهنده", state.delivererCards, state.deliveredIdx, enabled) {
            onChange(null, null, it, null, null)
        }
        PartyPickSection("تحویل‌گیرنده", state.receiverCards, state.receivedIdx, enabled) {
            onChange(null, null, null, it, null)
        }
        if (state.deliveredIdx == state.receivedIdx && state.delivererCards.isNotEmpty()) {
            DfStatusBanner(
                message = "تحویل‌دهنده و گیرنده یک نفر انتخاب شده‌اند.",
                tone = DfStatusTone.Warning,
            )
        }
        DfTextField(
            value = state.notes,
            onValueChange = { onChange(null, null, null, null, it) },
            label = "یادداشت تحویل",
            enabled = enabled,
            minLines = 2,
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PartyPickSection(
    title: String,
    cards: List<HandoverPartyCard>,
    selectedIdx: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    Text(title, fontWeight = FontWeight.SemiBold)
    cards.forEach { card ->
        val selected = card.index == selectedIdx
        Surface(
            onClick = { if (enabled) onSelect(card.index) },
            shape = AppShapes.CardSmall,
            color = if (selected) DfThemeColors.primaryContainer().copy(alpha = 0.35f) else DfThemeColors.surface(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(AppSpacing.sm)) {
                Text(card.name.ifBlank { "—" }, fontWeight = FontWeight.Medium)
                Text(card.roleLabel, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                Text(card.mobile.ifBlank { "—" }, style = AppTypography.labelSmall)
            }
        }
    }
}

@Composable
fun HandoverKeysStepContent(
    state: HandoverWizardUiState,
    enabled: Boolean,
    onCountDelta: (Int, Int) -> Unit,
    onNotes: (Int, String) -> Unit,
    onCustomTypeChange: (String) -> Unit,
    onAddCustom: () -> Unit,
    onKeyTypeChange: (Int, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("کلیدها و دسترسی‌ها", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        Text(
            "تعداد هر مورد را مشخص کنید؛ صفر ذخیره نمی‌شود.",
            style = AppTypography.bodyDescription,
            color = DfThemeColors.textSecondary(),
        )
        state.keys.forEachIndexed { index, key ->
            DfCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    if (key.isCustom) {
                        DfTextField(
                            value = key.type,
                            onValueChange = { onKeyTypeChange(index, it) },
                            label = "نوع کلید",
                            enabled = enabled,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(key.type, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (enabled) onCountDelta(index, -1) }, enabled = enabled) {
                            Text("−", style = AppTypography.sectionTitle)
                        }
                        Text(key.count, modifier = Modifier.padding(horizontal = AppSpacing.md))
                        IconButton(onClick = { if (enabled) onCountDelta(index, 1) }, enabled = enabled) {
                            Text("+", style = AppTypography.sectionTitle)
                        }
                    }
                    DfTextField(
                        value = key.notes,
                        onValueChange = { onNotes(index, it) },
                        label = "یادداشت",
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
            DfTextField(
                value = state.customKeyType,
                onValueChange = onCustomTypeChange,
                label = "کلید سفارشی",
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            DfSecondaryButton(text = "افزودن", onClick = onAddCustom, enabled = enabled)
        }
    }
}

@Composable
fun HandoverMetersStepContent(
    state: HandoverWizardUiState,
    enabled: Boolean,
    onUpdate: (Int, String?, String?, String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("قرائت کنتور", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        state.meters.forEachIndexed { index, meter ->
            DfCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(meter.label, fontWeight = FontWeight.Bold)
                    DfTextField(
                        value = meter.reading,
                        onValueChange = { onUpdate(index, null, it, null) },
                        label = "عدد فعلی",
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DfTextField(
                        value = meter.number,
                        onValueChange = { onUpdate(index, it, null, null) },
                        label = "شماره کنتور",
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DfTextField(
                        value = meter.notes,
                        onValueChange = { onUpdate(index, null, null, it) },
                        label = "یادداشت",
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HandoverConditionsStepContent(
    state: HandoverWizardUiState,
    enabled: Boolean,
    onStatus: (Int, String) -> Unit,
    onNote: (Int, String) -> Unit,
    onMarkAllOk: () -> Unit,
    onReset: () -> Unit,
    onUploadPhoto: () -> Unit,
    photoCaption: String,
    onPhotoCaption: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("وضعیت ملک", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            DfSecondaryButton(text = "همه سالم", onClick = onMarkAllOk, enabled = enabled)
            TextButton(onClick = onReset, enabled = enabled) { Text("بازنشانی") }
        }
        state.conditions.forEachIndexed { index, cond ->
            DfCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(cond.item, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val statuses = state.conditionStatuses.ifEmpty {
                            listOf(
                                "ok" to "سالم",
                                "minor" to "جزئی",
                                "repair" to "تعمیر",
                                "damaged" to "آسیب",
                                "unchecked" to "بررسی نشده",
                            )
                        }
                        statuses.forEach { (value, label) ->
                            FilterChip(
                                selected = cond.status == value,
                                onClick = { if (enabled) onStatus(index, value) },
                                label = { Text(label, style = AppTypography.labelSmall) },
                                enabled = enabled,
                            )
                        }
                    }
                    DfTextField(
                        value = cond.note,
                        onValueChange = { onNote(index, it) },
                        label = "توضیح",
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        DfTextField(
            value = photoCaption,
            onValueChange = onPhotoCaption,
            label = "عنوان عکس جدید",
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
        DfSecondaryButton(
            text = if (state.isUploadingPhoto) "در حال آپلود…" else "انتخاب عکس",
            onClick = onUploadPhoto,
            enabled = enabled && !state.isUploadingPhoto,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HandoverEquipmentStepContent(
    state: HandoverWizardUiState,
    enabled: Boolean,
    onChip: (String) -> Unit,
    onUpdate: (Int, String?, String?, String?, String?) -> Unit,
    onAddRow: () -> Unit,
    onRemove: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("تجهیزات تحویلی", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.commonEquipment.forEach { name ->
                FilterChip(
                    selected = state.equipment.any { it.name == name },
                    onClick = { if (enabled) onChip(name) },
                    label = { Text(name, style = AppTypography.labelSmall) },
                    enabled = enabled,
                )
            }
        }
        state.equipment.forEachIndexed { index, eq ->
            DfCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    DfTextField(
                        value = eq.name,
                        onValueChange = { onUpdate(index, it, null, null, null) },
                        label = "نام",
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        DfTextField(
                            value = eq.count,
                            onValueChange = { onUpdate(index, null, it, null, null) },
                            label = "تعداد",
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                        )
                        val statusOptions = state.conditionStatuses.filter { it.first != "unchecked" }
                            .ifEmpty { listOf("ok" to "سالم", "minor" to "جزئی", "repair" to "تعمیر", "damaged" to "آسیب") }
                        Column(Modifier.weight(1f)) {
                            Text("وضعیت", style = AppTypography.labelSmall)
                            FlowRow {
                                statusOptions.forEach { (value, label) ->
                                    FilterChip(
                                        selected = eq.status == value,
                                        onClick = { if (enabled) onUpdate(index, null, null, value, null) },
                                        label = { Text(label, style = AppTypography.labelSmall) },
                                        enabled = enabled,
                                    )
                                }
                            }
                        }
                    }
                    DfTextField(
                        value = eq.notes,
                        onValueChange = { onUpdate(index, null, null, null, it) },
                        label = "یادداشت",
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(onClick = { onRemove(index) }, enabled = enabled) { Text("حذف") }
                }
            }
        }
        DfSecondaryButton(text = "افزودن تجهیز", onClick = onAddRow, enabled = enabled)
    }
}

@Composable
fun HandoverReviewStepContent(
    state: HandoverWizardUiState,
    enabled: Boolean,
    onEditStep: (String) -> Unit,
    onOpenPdf: () -> Unit,
    onAckChange: (Boolean) -> Unit,
) {
    val review = state.wizard?.get("review")?.jsonObject
    fun JsonObject.str(k: String) = get(k)?.jsonPrimitive?.contentOrNull.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("مرور نهایی", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        if (state.statusLabel.isNotBlank()) {
            Text("وضعیت: ${state.statusLabel}", color = DfThemeColors.textSecondary())
        }
        review?.let { r ->
            val readiness = r.str("readiness")
            val tone = when (readiness) {
                "ready" -> DfStatusTone.Success
                "blocked" -> DfStatusTone.Error
                else -> DfStatusTone.Warning
            }
            DfStatusBanner(
                message = r.str("readiness_message"),
                detail = r.str("readiness_detail").takeIf { it.isNotBlank() },
                tone = tone,
            )
            r.get("issues")?.jsonArray?.forEach { el ->
                val issue = el.jsonObject
                val msg = issue.str("message")
                val edit = issue.str("edit_step")
                if (msg.isNotBlank()) {
                    Surface(shape = AppShapes.CardSmall, color = DfThemeColors.surface()) {
                        Row(Modifier.padding(AppSpacing.sm), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(msg, style = AppTypography.labelSmall, modifier = Modifier.weight(1f))
                            if (edit.isNotBlank()) {
                                TextButton(onClick = { onEditStep(edit) }) { Text("اصلاح") }
                            }
                        }
                    }
                }
            }
            ReviewSection("تاریخ و طرفین", "basic", onEditStep) {
                Text("${r.str("date_display")} · ${r.str("time_display")}")
                r.get("delivered")?.jsonObject?.let { Text("دهنده: ${it.str("name")}") }
                r.get("received")?.jsonObject?.let { Text("گیرنده: ${it.str("name")}") }
            }
            ReviewSection("کلیدها", "keys", onEditStep) {
                r.get("keys")?.jsonArray?.forEach { el ->
                    val k = el.jsonObject
                    Text("• ${k.str("type")}: ${k.str("count")}", style = AppTypography.labelSmall)
                }
            }
            ReviewSection("کنتورها", "meters", onEditStep) {
                r.get("meters")?.jsonArray?.forEach { el ->
                    val m = el.jsonObject
                    Text("• ${m.str("label")}: ${m.str("reading")}", style = AppTypography.labelSmall)
                }
            }
            ReviewSection("تجهیزات", "equipment", onEditStep) {
                r.get("equipment")?.jsonArray?.forEach { el ->
                    val e = el.jsonObject
                    Text("• ${e.str("name")} × ${e.str("count")}", style = AppTypography.labelSmall)
                }
            }
        }
        if (state.pdfAvailable) {
            DfSecondaryButton(text = "مشاهده PDF صورت‌جلسه", onClick = onOpenPdf, modifier = Modifier.fillMaxWidth())
        }
        if (enabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.creatorAck, onCheckedChange = onAckChange, enabled = !state.sendDisabled)
                Text(
                    "اطلاعات را در محل بررسی کردم و صحت آن را تأیید می‌کنم.",
                    style = AppTypography.labelSmall,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReviewSection(
    title: String,
    step: String,
    onEdit: (String) -> Unit,
    content: @Composable () -> Unit,
) {
    DfCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.sm)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { onEdit(step) }) { Text("ویرایش") }
            }
            content()
        }
    }
}
