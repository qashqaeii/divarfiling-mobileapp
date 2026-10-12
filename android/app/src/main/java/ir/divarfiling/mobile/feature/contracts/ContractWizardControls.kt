package ir.divarfiling.mobile.feature.contracts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfJalaliDateField
import ir.divarfiling.mobile.core.design.components.DfMoneyField
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class WizardChoiceOption(val value: String, val label: String)

val WIZARD_COMMON_AREAS = listOf(
    "راهرو", "راه‌پله", "آسانسور", "پارکینگ", "لابی", "حیاط", "پشت‌بام", "انباری مشترک", "سایر",
)

val PAYMENT_METHOD_OPTIONS = listOf(
    WizardChoiceOption("cash", "نقدی"),
    WizardChoiceOption("card", "کارت به کارت"),
    WizardChoiceOption("transfer", "انتقال بانکی"),
    WizardChoiceOption("cheque", "چک"),
    WizardChoiceOption("other", "سایر"),
)

val USAGE_TYPE_OPTIONS = listOf(
    WizardChoiceOption("residential", "مسکونی"),
    WizardChoiceOption("office", "اداری"),
    WizardChoiceOption("commercial", "تجاری"),
    WizardChoiceOption("other", "سایر"),
)

val DEPOSIT_MODE_OPTIONS = listOf(
    WizardChoiceOption("new", "ودیعه جدید"),
    WizardChoiceOption("retained", "ودیعه قبلی باقی می‌ماند"),
)

val PERSON_TYPE_OPTIONS = listOf(
    WizardChoiceOption("natural", "حقیقی"),
    WizardChoiceOption("legal", "حقوقی"),
)

val LEASE_COST_LABELS = mapOf(
    "water" to "آب",
    "electricity" to "برق",
    "gas" to "گاز",
    "phone" to "تلفن",
    "charge" to "شارژ",
    "minor_repairs" to "تعمیرات جزئی",
    "major_repairs" to "تعمیرات اساسی",
    "property_tax" to "مالیات",
)

val LEASE_PENALTY_LABELS = mapOf(
    "landlord_delivery_delay" to "تأخیر تحویل موجر",
    "tenant_vacate_delay" to "تأخیر تخلیه مستأجر",
    "deposit_return_delay" to "تأخیر استرداد ودیعه",
)

val SALE_COST_LABELS = mapOf(
    "transfer_costs" to "هزینه انتقال",
    "notary_costs" to "هزینه دفترخانه",
    "pre_delivery_costs" to "قبل از تحویل",
    "post_delivery_costs" to "بعد از تحویل",
    "other_costs" to "سایر",
)

val SALE_PENALTY_LABELS = mapOf(
    "payment_delay" to "تأخیر پرداخت",
    "delivery_delay" to "تأخیر تحویل",
    "transfer_delay" to "تأخیر اقدام برای انتقال رسمی",
)

fun JsonObject?.propertyChoices(key: String): List<String> {
    val prop = this?.get("property")?.jsonObject ?: return emptyList()
    return prop.choiceValues(key)
}

fun JsonObject?.choiceValues(key: String): List<String> {
    val arr = this?.get(key)?.jsonArray ?: return emptyList()
    return arr.mapNotNull { el ->
        when {
            el.jsonPrimitive.isString -> el.jsonPrimitive.content
            else -> el.jsonObject["value"]?.jsonPrimitive?.contentOrNull
                ?: el.jsonObject["label"]?.jsonPrimitive?.contentOrNull
        }
    }.filter { it.isNotBlank() }
}

fun JsonObject?.labeledOptions(key: String): List<WizardChoiceOption> {
    val arr = this?.get(key)?.jsonArray ?: return emptyList()
    return arr.mapNotNull { el ->
        when {
            el.jsonPrimitive.isString -> WizardChoiceOption(el.jsonPrimitive.content, el.jsonPrimitive.content)
            else -> {
                val o = el.jsonObject
                val v = o["value"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val l = o["label"]?.jsonPrimitive?.contentOrNull ?: v
                if (v.isBlank()) null else WizardChoiceOption(v, l)
            }
        }
    }
}

@Composable
fun WizardMoneyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
) {
    DfMoneyField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun WizardJalaliField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
) {
    DfJalaliDateField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun WizardChoiceChips(
    label: String,
    options: List<WizardChoiceOption>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
) {
    if (options.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            options.forEach { opt ->
                DfSoftChip(
                    text = opt.label,
                    selected = selected == opt.value,
                    onClick = { if (enabled) onSelect(opt.value) },
                )
            }
        }
    }
}

@Composable
fun WizardStringChoiceChips(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean = true,
) {
    if (options.isEmpty()) {
        WizardField(label, selected, onSelect, enabled = enabled)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            options.forEach { opt ->
                DfSoftChip(
                    text = opt,
                    selected = selected == opt,
                    onClick = { if (enabled) onSelect(opt) },
                )
            }
        }
    }
}

@Composable
fun WizardPenaltyBlock(
    title: String,
    penalty: WizardPenaltyDraft,
    onChange: (WizardPenaltyDraft) -> Unit,
    enabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        WizardToggle(
            label = title,
            checked = penalty.enabled,
            onCheckedChange = { onChange(penalty.copy(enabled = it)) },
            enabled = enabled,
        )
        if (penalty.enabled) {
            WizardMoneyField(
                label = "مبلغ جریمه روزانه",
                value = penalty.amountPerDay,
                onValueChange = { onChange(penalty.copy(amountPerDay = it)) },
                enabled = enabled,
            )
        }
    }
}
