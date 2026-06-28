package ir.divarfiling.mobile.feature.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

fun smartToolIdFromKey(key: String): SmartToolId? =
    SmartToolId.entries.firstOrNull { it.key == key }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartToolCalculatorScreen(
    toolId: SmartToolId,
    onBack: () -> Unit,
) {
    val tool = smartToolsCatalog.first { it.id == toolId }
    var resultText by remember(toolId) { mutableStateOf<String?>(null) }

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                DfDetailPageHeader(
                    title = tool.title,
                    subtitle = tool.subtitle,
                    onBack = onBack,
                )
            }
            item {
                when (toolId) {
                    SmartToolId.RentCommission -> RentCommissionForm { resultText = it }
                    SmartToolId.DepositConvert -> DepositConvertForm { resultText = it }
                    SmartToolId.Compare -> CompareForm { resultText = it }
                    SmartToolId.AreaPrice -> AreaPriceForm { resultText = it }
                    SmartToolId.Discount -> DiscountForm { resultText = it }
                    SmartToolId.SalesCommission -> SalesCommissionForm { resultText = it }
                }
            }
            resultText?.let { text ->
                item {
                    DfPremiumCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            Text(
                                "نتیجه محاسبه",
                                style = AppTypography.sectionTitle,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = text,
                                style = AppTypography.bodyDescription,
                                color = DfColors.TextPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolFormCard(
    fields: List<ToolField>,
    buttonLabel: String,
    onCalculate: (Map<String, String>) -> Unit,
) {
    var values by remember(fields) { mutableStateOf(fields.associate { it.key to "" }) }
    DfPremiumCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            fields.forEach { field ->
                OutlinedTextField(
                    value = values[field.key].orEmpty(),
                    onValueChange = { values = values + (field.key to it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(field.label) },
                    placeholder = field.placeholder?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            DfPrimaryButton(
                text = buttonLabel,
                onClick = { onCalculate(values) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private data class ToolField(
    val key: String,
    val label: String,
    val placeholder: String? = null,
)

@Composable
private fun RentCommissionForm(onResult: (String) -> Unit) {
    ToolFormCard(
        fields = listOf(
            ToolField("rent", "اجاره ماهانه (تومان)"),
            ToolField("tenant", "سهم مستأجر (٪)", "۵۰"),
            ToolField("landlord", "سهم مالک (٪)", "۵۰"),
        ),
        buttonLabel = "محاسبه کمیسیون",
    ) { values ->
        val rent = values["rent"]?.toLongOrNull() ?: return@ToolFormCard
        val tenant = values["tenant"]?.toDoubleOrNull() ?: 50.0
        val landlord = values["landlord"]?.toDoubleOrNull() ?: 50.0
        val r = SmartToolsEngine.rentCommissionCalc(rent, tenant, landlord)
        onResult(
            buildString {
                appendLine("اجاره ماهانه: ${FormatUtils.formatPriceToman(r.monthlyRent)}")
                appendLine("کمیسیون مستأجر: ${FormatUtils.formatPriceToman(r.tenantCommission)}")
                appendLine("کمیسیون مالک: ${FormatUtils.formatPriceToman(r.landlordCommission)}")
                append("جمع کمیسیون: ${FormatUtils.formatPriceToman(r.totalCommission)}")
            },
        )
    }
}

@Composable
private fun DepositConvertForm(onResult: (String) -> Unit) {
    ToolFormCard(
        fields = listOf(
            ToolField("deposit", "رهن کامل (تومان) — یا خالی"),
            ToolField("rent", "اجاره ماهانه (تومان) — یا خالی"),
            ToolField("rate", "نرخ تبدیل", "۳۰"),
        ),
        buttonLabel = "تبدیل",
    ) { values ->
        val rate = values["rate"]?.toIntOrNull() ?: 30
        val deposit = values["deposit"]?.toLongOrNull()
        val rent = values["rent"]?.toLongOrNull()
        val r = SmartToolsEngine.depositRentConvert(deposit, rent, rate)
        if (r.fullDeposit == null && r.monthlyRent == null) return@ToolFormCard
        onResult(
            buildString {
                appendLine("نرخ تبدیل: ۱ به $rate")
                r.fullDeposit?.let { appendLine("رهن کامل: ${FormatUtils.formatPriceToman(it)}") }
                r.monthlyRent?.let { appendLine("اجاره ماهانه: ${FormatUtils.formatPriceToman(it)}") }
                val mixed = SmartToolsEngine.mixedRentCalc(deposit, rent, rate)
                mixed?.let {
                    appendLine("رهن کامل معادل: ${FormatUtils.formatPriceToman(it.fullDeposit)}")
                    append("اجاره کامل معادل: ${FormatUtils.formatPriceToman(it.fullRent)}")
                }
            },
        )
    }
}

@Composable
private fun CompareForm(onResult: (String) -> Unit) {
    ToolFormCard(
        fields = listOf(
            ToolField("a_price", "آگهی الف — قیمت/رهن"),
            ToolField("a_area", "آگهی الف — متراژ"),
            ToolField("b_price", "آگهی ب — قیمت/رهن"),
            ToolField("b_area", "آگهی ب — متراژ"),
            ToolField("rate", "نرخ رهن/اجاره (اجاره)", "۳۰"),
        ),
        buttonLabel = "مقایسه",
    ) { values ->
        val rate = values["rate"]?.toIntOrNull() ?: 30
        val isRent = false
        val a = CompareInput(
            label = "آگهی الف",
            price = values["a_price"]?.toLongOrNull(),
            area = values["a_area"]?.toDoubleOrNull(),
        )
        val b = CompareInput(
            label = "آگهی ب",
            price = values["b_price"]?.toLongOrNull(),
            area = values["b_area"]?.toDoubleOrNull(),
        )
        val r = SmartToolsEngine.compareProperties(a, b, isRent, rate)
        onResult(
            buildString {
                r.a?.let { appendLine("الف: ${FormatUtils.formatPriceToman(it.perSqm)} / متر") }
                r.b?.let { appendLine("ب: ${FormatUtils.formatPriceToman(it.perSqm)} / متر") }
                r.verdict?.let { append(it) }
            },
        )
    }
}

@Composable
private fun AreaPriceForm(onResult: (String) -> Unit) {
    ToolFormCard(
        fields = listOf(
            ToolField("pps", "قیمت هر متر — یا خالی"),
            ToolField("area", "متراژ — یا خالی"),
            ToolField("total", "قیمت کل — یا خالی"),
        ),
        buttonLabel = "محاسبه",
    ) { values ->
        val r = SmartToolsEngine.areaPriceCalc(
            pricePerSqm = values["pps"]?.toDoubleOrNull(),
            area = values["area"]?.toDoubleOrNull(),
            totalPrice = values["total"]?.toLongOrNull(),
        ) ?: return@ToolFormCard
        onResult(
            buildString {
                r.pricePerSqm?.let { appendLine("قیمت هر متر: ${FormatUtils.formatPriceToman(it.toLong())}") }
                r.area?.let { appendLine("متراژ: $it متر") }
                r.totalPrice?.let { append("قیمت کل: ${FormatUtils.formatPriceToman(it)}") }
            },
        )
    }
}

@Composable
private fun DiscountForm(onResult: (String) -> Unit) {
    ToolFormCard(
        fields = listOf(
            ToolField("price", "قیمت آگهی (تومان)"),
            ToolField("pct", "درصد تخفیف"),
        ),
        buttonLabel = "محاسبه تخفیف",
    ) { values ->
        val price = values["price"]?.toLongOrNull() ?: return@ToolFormCard
        val pct = values["pct"]?.toDoubleOrNull() ?: return@ToolFormCard
        val r = SmartToolsEngine.discountCalc(price, pct)
        onResult(
            buildString {
                appendLine("قیمت اولیه: ${FormatUtils.formatPriceToman(r.listPrice)}")
                appendLine("تخفیف ${r.discountPct}%: ${FormatUtils.formatPriceToman(r.savings)}")
                append("قیمت نهایی: ${FormatUtils.formatPriceToman(r.finalPrice)}")
            },
        )
    }
}

@Composable
private fun SalesCommissionForm(onResult: (String) -> Unit) {
    ToolFormCard(
        fields = listOf(
            ToolField("price", "قیمت معامله (تومان)"),
            ToolField("buyer", "کمیسیون خریدار (٪)", "۱"),
            ToolField("seller", "کمیسیون فروشنده (٪)", "۱"),
        ),
        buttonLabel = "محاسبه کمیسیون",
    ) { values ->
        val price = values["price"]?.toLongOrNull() ?: return@ToolFormCard
        val buyer = values["buyer"]?.toDoubleOrNull() ?: 1.0
        val seller = values["seller"]?.toDoubleOrNull() ?: 1.0
        val r = SmartToolsEngine.commissionCalc(price, buyer, seller)
        onResult(
            buildString {
                appendLine("کمیسیون خریدار: ${FormatUtils.formatPriceToman(r.buyerCommission)}")
                appendLine("کمیسیون فروشنده: ${FormatUtils.formatPriceToman(r.sellerCommission)}")
                append("جمع: ${FormatUtils.formatPriceToman(r.totalCommission)}")
            },
        )
    }
}
