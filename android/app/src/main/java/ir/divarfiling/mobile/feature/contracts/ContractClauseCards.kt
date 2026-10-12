package ir.divarfiling.mobile.feature.contracts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextField
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private fun JsonObject.cardStr(key: String): String =
    get(key)?.jsonPrimitive?.contentOrNull.orEmpty()

fun JsonObject.clauseRenderedText(): String = cardStr("clause_rendered").ifBlank { cardStr("clause_preview") }

private fun JsonObject.cardBool(key: String): Boolean {
    val el = get(key) ?: return false
    return when {
        el.jsonPrimitive.isString -> el.jsonPrimitive.content in listOf("true", "1", "yes")
        else -> el.jsonPrimitive.contentOrNull?.toBooleanStrictOrNull() == true
    }
}

fun optionalCodesFromClauseSections(clauseUi: JsonObject?): Set<String> {
    val sections = clauseUi?.get("clause_sections")?.jsonArray ?: return emptySet()
    val codes = mutableSetOf<String>()
    sections.forEach { sectionEl ->
        sectionEl.jsonObject["cards"]?.jsonArray?.forEach { cardEl ->
            val card = cardEl.jsonObject
            if (card.cardStr("type") != "optional_clause") return@forEach
            if (!card.cardBool("enabled")) return@forEach
            val code = card.cardStr("clause_code").ifBlank { card.cardStr("id") }
            if (code.isNotBlank()) codes.add(code)
        }
    }
    return codes
}

fun mergeClauseDraftFromWizard(
    wizard: JsonObject,
    clauseUi: JsonObject?,
): WizardClausesDraft {
    val draft = clausesFromWizard(wizard)
    if (wizard.containsKey("clause_optional")) {
        return draft
    }
    val seeded = optionalCodesFromClauseSections(clauseUi)
    return if (seeded.isEmpty()) draft else draft.copy(selectedOptionalCodes = seeded)
}

fun renderClauseBodyPreview(template: String, inlineValues: Map<String, String>): String {
    if (template.isBlank()) return ""
    var text = template
    inlineValues.forEach { (key, value) ->
        if (value.isNotBlank()) {
            text = text.replace("{{$key}}", value)
        }
    }
    return text
}

private fun penaltyInlineValues(penalty: WizardPenaltyDraft): Map<String, String> {
    val amount = penalty.amountPerDay
    val unit = penalty.unit.ifBlank { "daily" }
    return mapOf(
        "amount" to amount,
        "penalty_amount" to amount,
        "penalty_unit" to unit,
        "unit" to unit,
    )
}

@Composable
fun WizardClauseSections(
    contractType: String,
    clauses: WizardClausesDraft,
    clauseSections: JsonArray?,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
    onToggleOptional: (String) -> Unit,
) {
    val sections = clauseSections
    if (sections == null || sections.isEmpty()) {
        Text(
            "بندهای پیشنهادی را متناسب با شرایط معامله انتخاب و بررسی کنید.",
            style = AppTypography.bodyDescription,
            color = DfThemeColors.textSecondary(),
        )
        return
    }
    sections.forEach { sectionEl ->
        val section = sectionEl.jsonObject
        val sectionTitle = section.cardStr("title")
        if (sectionTitle.isNotBlank()) {
            WizardSectionTitle(sectionTitle)
        }
        section["cards"]?.jsonArray?.forEach { cardEl ->
            WizardClauseCard(
                card = cardEl.jsonObject,
                contractType = contractType,
                clauses = clauses,
                canEdit = canEdit,
                onChange = onChange,
                onToggleOptional = onToggleOptional,
            )
        }
    }
}

@Composable
private fun WizardClauseCard(
    card: JsonObject,
    contractType: String,
    clauses: WizardClausesDraft,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
    onToggleOptional: (String) -> Unit,
) {
    when (card.cardStr("type")) {
        "usage_choice" -> ClauseUsageChoiceCard(card, clauses, canEdit, onChange)
        "choice" -> ClauseChoiceCard(card, clauses, canEdit, onChange)
        "cost_allocation" -> ClauseCostAllocationCard(card, clauses, canEdit, onChange, leaseMode = true)
        "sale_cost_allocation" -> ClauseCostAllocationCard(card, clauses, canEdit, onChange, leaseMode = false)
        "repairs_allocation" -> ClauseRepairsCard(card, clauses, canEdit, onChange)
        "penalty" -> ClausePenaltyCard(card, contractType, clauses, canEdit, onChange)
        "optional_clause" -> ClauseOptionalCard(card, clauses, canEdit, onToggleOptional)
        "info" -> ClauseInfoCard(card)
        "custom_agreements" -> ClauseCustomAgreementsCard(card, clauses, canEdit, onChange)
    }
}

@Composable
private fun ClauseCardShell(
    title: String,
    question: String = "",
    warning: String? = null,
    reviewBadge: String? = null,
    clausePreview: String = "",
    clauseRendered: String = "",
    previewInline: Map<String, String> = emptyMap(),
    content: @Composable () -> Unit,
) {
    val previewText = clauseRendered.ifBlank {
        renderClauseBodyPreview(clausePreview, previewInline)
    }
    var showPreview by remember(previewText) { mutableStateOf(false) }
    Surface(
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(
            Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontWeight = FontWeight.SemiBold, style = AppTypography.labelLarge, modifier = Modifier.weight(1f))
                if (!reviewBadge.isNullOrBlank()) {
                    Text(reviewBadge, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                }
            }
            if (question.isNotBlank()) {
                Text(question, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
            }
            content()
            if (!warning.isNullOrBlank()) {
                Text(warning, style = AppTypography.labelSmall, color = DfThemeColors.warning())
            }
            if (previewText.isNotBlank()) {
                TextButton(onClick = { showPreview = !showPreview }) {
                    Text(if (showPreview) "بستن متن قرارداد" else "مشاهده متن قرارداد")
                }
                AnimatedVisibility(showPreview) {
                    Text(
                        previewText,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ClauseUsageChoiceCard(
    card: JsonObject,
    clauses: WizardClausesDraft,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
) {
    val options = card["options"]?.jsonArray?.mapNotNull { el ->
        val o = el.jsonObject
        val v = o.cardStr("value")
        val l = o.cardStr("label")
        if (v.isBlank()) null else WizardChoiceOption(v, l.ifBlank { v })
    }.orEmpty()
    val selected = clauses.usageType.ifBlank { card.cardStr("value").ifBlank { "residential" } }
    ClauseCardShell(
        title = card.cardStr("title"),
        question = card.cardStr("question"),
        clausePreview = card.cardStr("clause_preview"),
        clauseRendered = card.cardStr("clause_rendered"),
    ) {
        WizardChoiceChips("نوع استفاده", options, selected, { onChange(clauses.copy(usageType = it)) }, canEdit)
        WizardField(
            "تعداد ساکن (اختیاری)",
            clauses.occupantsCount,
            { onChange(clauses.copy(occupantsCount = it)) },
            enabled = canEdit,
        )
        card.cardStr("summary").takeIf { it.isNotBlank() }?.let {
            Text(it, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        }
    }
}

@Composable
private fun ClauseChoiceCard(
    card: JsonObject,
    clauses: WizardClausesDraft,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
) {
    val cardId = card.cardStr("id")
    val options = card["options"]?.jsonArray?.mapNotNull { el ->
        val o = el.jsonObject
        val v = o.cardStr("value")
        val l = o.cardStr("label")
        if (v.isBlank()) null else WizardChoiceOption(v, l.ifBlank { v })
    }.orEmpty()
    val selected = when (cardId) {
        "transfer" -> clauses.transferChoice.ifBlank { card.cardStr("value") }
        "has_tenant" -> if (clauses.propertyHasTenant) "yes" else "no"
        else -> card.cardStr("value")
    }
    ClauseCardShell(
        title = card.cardStr("title"),
        question = card.cardStr("question"),
        warning = card.cardStr("warning").takeIf { it.isNotBlank() },
        reviewBadge = card.cardStr("review_badge").takeIf { it.isNotBlank() },
        clausePreview = card.cardStr("clause_preview"),
        clauseRendered = card.cardStr("clause_rendered"),
    ) {
        WizardChoiceChips(card.cardStr("title"), options, selected, { value ->
            when (cardId) {
                "transfer" -> onChange(clauses.copy(transferChoice = value))
                "has_tenant" -> onChange(clauses.copy(propertyHasTenant = value == "yes"))
                else -> Unit
            }
        }, canEdit)
    }
}

@Composable
private fun ClauseCostAllocationCard(
    card: JsonObject,
    clauses: WizardClausesDraft,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
    leaseMode: Boolean,
) {
    val rows = card["rows"]?.jsonArray.orEmpty()
    val options = if (leaseMode) {
        listOf(WizardChoiceOption("landlord", "موجر"), WizardChoiceOption("tenant", "مستأجر"))
    } else {
        listOf(
            WizardChoiceOption("seller", "فروشنده"),
            WizardChoiceOption("buyer", "خریدار"),
            WizardChoiceOption("shared", "مشترک"),
        )
    }
    ClauseCardShell(
        title = card.cardStr("title"),
        question = card.cardStr("question"),
        reviewBadge = card.cardStr("review_badge").takeIf { it.isNotBlank() },
        clausePreview = card.cardStr("clause_preview"),
        clauseRendered = card.cardStr("clause_rendered"),
    ) {
        rows.forEach { rowEl ->
            val row = rowEl.jsonObject
            val key = row.cardStr("key")
            val label = row.cardStr("label").ifBlank { key }
            val current = if (leaseMode) {
                clauses.leaseCosts[key].orEmpty().ifBlank { row.cardStr("value") }
            } else {
                clauses.saleCosts[key].orEmpty().ifBlank { row.cardStr("value") }
            }
            WizardCostRow(label, current, options, { picked ->
                if (leaseMode) {
                    onChange(clauses.copy(leaseCosts = clauses.leaseCosts + (key to picked)))
                } else {
                    onChange(clauses.copy(saleCosts = clauses.saleCosts + (key to picked)))
                }
            }, canEdit)
        }
    }
}

@Composable
private fun ClauseRepairsCard(
    card: JsonObject,
    clauses: WizardClausesDraft,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
) {
    val landlordTenant = listOf(
        WizardChoiceOption("landlord", "موجر"),
        WizardChoiceOption("tenant", "مستأجر"),
    )
    val major = clauses.leaseCosts["major_repairs"].orEmpty().ifBlank { card.cardStr("major_value") }
    val minor = clauses.leaseCosts["minor_repairs"].orEmpty().ifBlank { card.cardStr("minor_value") }
    ClauseCardShell(
        title = card.cardStr("title"),
        question = card.cardStr("question"),
        reviewBadge = card.cardStr("review_badge").takeIf { it.isNotBlank() },
        clausePreview = card.cardStr("clause_preview"),
        clauseRendered = card.cardStr("clause_rendered"),
    ) {
        WizardCostRow("تعمیرات اساسی", major, landlordTenant, {
            onChange(clauses.copy(leaseCosts = clauses.leaseCosts + ("major_repairs" to it)))
        }, canEdit)
        WizardCostRow("تعمیرات جزئی", minor, landlordTenant, {
            onChange(clauses.copy(leaseCosts = clauses.leaseCosts + ("minor_repairs" to it)))
        }, canEdit)
    }
}

@Composable
private fun ClausePenaltyCard(
    card: JsonObject,
    contractType: String,
    clauses: WizardClausesDraft,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
) {
    val penaltyId = card.cardStr("id")
    val penalties = if (contractType == "sale") clauses.salePenalties else clauses.leasePenalties
    val penalty = penalties[penaltyId] ?: WizardPenaltyDraft(
        enabled = card.cardBool("enabled"),
        amountPerDay = card.cardStr("amount"),
        unit = card.cardStr("unit").ifBlank { "daily" },
    )
    val unitOptions = listOf(
        WizardChoiceOption("daily", "روزانه"),
        WizardChoiceOption("fixed", "موردی"),
    )
    ClauseCardShell(
        title = card.cardStr("title"),
        warning = card.cardStr("warning").takeIf { it.isNotBlank() },
        reviewBadge = card.cardStr("review_badge").takeIf { it.isNotBlank() },
        clausePreview = card.cardStr("clause_preview"),
        clauseRendered = card.cardStr("clause_rendered"),
        previewInline = penaltyInlineValues(penalty),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("فعال", style = AppTypography.labelSmall)
            Switch(
                checked = penalty.enabled,
                onCheckedChange = { on ->
                    updatePenalty(clauses, contractType, penaltyId, penalty.copy(enabled = on), onChange)
                },
                enabled = canEdit,
            )
        }
        if (penalty.enabled) {
            WizardMoneyField(
                label = "مبلغ",
                value = penalty.amountPerDay,
                onValueChange = { amt ->
                    updatePenalty(clauses, contractType, penaltyId, penalty.copy(amountPerDay = amt), onChange)
                },
                enabled = canEdit,
            )
            WizardChoiceChips("واحد", unitOptions, penalty.unit.ifBlank { "daily" }, { unit ->
                updatePenalty(clauses, contractType, penaltyId, penalty.copy(unit = unit), onChange)
            }, canEdit)
        }
    }
}

private fun updatePenalty(
    clauses: WizardClausesDraft,
    contractType: String,
    key: String,
    penalty: WizardPenaltyDraft,
    onChange: (WizardClausesDraft) -> Unit,
) {
    if (contractType == "sale") {
        onChange(clauses.copy(salePenalties = clauses.salePenalties + (key to penalty)))
    } else {
        onChange(clauses.copy(leasePenalties = clauses.leasePenalties + (key to penalty)))
    }
}

@Composable
private fun ClauseOptionalCard(
    card: JsonObject,
    clauses: WizardClausesDraft,
    canEdit: Boolean,
    onToggleOptional: (String) -> Unit,
) {
    val code = card.cardStr("clause_code").ifBlank { card.cardStr("id") }
    val enabled = code.isNotBlank() && code in clauses.selectedOptionalCodes
    ClauseCardShell(
        title = card.cardStr("title"),
        question = card.cardStr("question"),
        reviewBadge = card.cardStr("review_badge").takeIf { it.isNotBlank() },
        clausePreview = card.cardStr("clause_preview"),
        clauseRendered = card.cardStr("clause_rendered"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = enabled,
                onCheckedChange = { if (canEdit && code.isNotBlank()) onToggleOptional(code) },
                enabled = canEdit,
            )
            Text("فعال‌سازی این بند", style = AppTypography.labelSmall)
        }
    }
}

@Composable
private fun ClauseInfoCard(card: JsonObject) {
    val critical = card.cardStr("level") == "critical"
    Surface(
        shape = AppShapes.CardSmall,
        color = if (critical) {
            DfThemeColors.warning().copy(alpha = 0.12f)
        } else {
            DfThemeColors.surfaceVariant().copy(alpha = 0.35f)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            card.cardStr("message").ifBlank { card.cardStr("title") },
            Modifier.padding(AppSpacing.sm),
            style = AppTypography.labelSmall,
            color = if (critical) DfThemeColors.warning() else DfThemeColors.textSecondary(),
        )
    }
}

@Composable
private fun ClauseCustomAgreementsCard(
    card: JsonObject,
    clauses: WizardClausesDraft,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
) {
    val fromCard = card["agreements"]?.jsonArray?.mapNotNull { el ->
        val o = el.jsonObject
        val body = o.cardStr("body").ifBlank { o.cardStr("body_text") }
        if (body.isBlank()) null else WizardCustomAgreementDraft(
            title = o.cardStr("title").ifBlank { "توافق اختصاصی" },
            body = body,
        )
    }.orEmpty()
    val agreements = clauses.customAgreements.ifEmpty { fromCard }.ifEmpty {
        if (clauses.customBody.isNotBlank()) {
            listOf(WizardCustomAgreementDraft(clauses.customTitle, clauses.customBody))
        } else {
            emptyList()
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        agreements.forEachIndexed { index, agreement ->
            Surface(shape = AppShapes.CardSmall, color = DfThemeColors.surfaceVariant().copy(alpha = 0.35f)) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    WizardField("عنوان", agreement.title, {
                        val list = agreements.toMutableList()
                        list[index] = agreement.copy(title = it)
                        syncCustomAgreements(clauses, list, onChange)
                    }, enabled = canEdit)
                    DfTextField(
                        value = agreement.body,
                        onValueChange = {
                            val list = agreements.toMutableList()
                            list[index] = agreement.copy(body = it)
                            syncCustomAgreements(clauses, list, onChange)
                        },
                        label = "متن توافق",
                        minLines = 3,
                        singleLine = false,
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (canEdit && agreements.size > 1) {
                        TextButton(onClick = {
                            syncCustomAgreements(clauses, agreements.filterIndexed { i, _ -> i != index }, onChange)
                        }) { Text("حذف") }
                    }
                }
            }
        }
        if (canEdit) {
            DfSecondaryButton(
                text = "افزودن توافق اختصاصی",
                onClick = {
                    syncCustomAgreements(
                        clauses,
                        agreements + WizardCustomAgreementDraft(title = "توافق اختصاصی"),
                        onChange,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun syncCustomAgreements(
    clauses: WizardClausesDraft,
    list: List<WizardCustomAgreementDraft>,
    onChange: (WizardClausesDraft) -> Unit,
) {
    onChange(
        clauses.copy(
            customAgreements = list,
            customTitle = list.firstOrNull()?.title.orEmpty(),
            customBody = list.firstOrNull()?.body.orEmpty(),
        ),
    )
}
