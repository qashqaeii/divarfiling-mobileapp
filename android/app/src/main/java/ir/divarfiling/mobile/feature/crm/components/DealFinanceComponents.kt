package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.DealDto

private val KindOptions = listOf("فروش", "اجاره")
private val ModeOptions = listOf("درصد از ارزش", "مبلغ ثابت")
private val PayoutOptions = listOf("وصول نشده", "نیمه‌وصول", "وصول شده", "بخشوده")

fun dealKindLabel(kind: String?): String = if (kind == "rent") "اجاره" else "فروش"
fun dealKindValue(label: String): String = if (label == "اجاره") "rent" else "sale"
fun commissionModeLabel(mode: String?): String = if (mode == "fixed") "مبلغ ثابت" else "درصد از ارزش"
fun commissionModeValue(label: String): String = if (label == "مبلغ ثابت") "fixed" else "percent"
fun payoutLabel(status: String?): String = when (status) {
    "paid" -> "وصول شده"
    "partial" -> "نیمه‌وصول"
    "waived" -> "بخشوده"
    else -> "وصول نشده"
}
fun payoutValue(label: String): String = when (label) {
    "وصول شده" -> "paid"
    "نیمه‌وصول" -> "partial"
    "بخشوده" -> "waived"
    else -> "unpaid"
}

@Composable
fun DealFinanceCard(
    deal: DealDto,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val advisor = deal.advisorCommission ?: 0L
    val agency = deal.agencyCommission ?: 0L
    val total = (advisor + agency).coerceAtLeast(1L)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = "مالی معامله",
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.TextMuted,
            )
            Text(
                text = FormatUtils.formatPriceToman(deal.commissionAmount ?: 0L),
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
            Text(
                text = listOfNotNull(deal.dealKindLabel, deal.payoutStatusLabel).joinToString(" · "),
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.Chip)
                    .height(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(advisor.toFloat().coerceAtLeast(0.01f) / total.toFloat())
                        .height(10.dp)
                        .background(DfColors.Green),
                )
                Box(
                    modifier = Modifier
                        .weight(agency.toFloat().coerceAtLeast(0.01f) / total.toFloat())
                        .height(10.dp)
                        .background(DfColors.Purple),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FinanceMiniStat("سهم شما", advisor, DfColors.Green)
                FinanceMiniStat("سهم املاک", agency, DfColors.Purple)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FinanceMiniStat("خریدار", deal.buyerCommissionAmount ?: 0L, DfColors.TextSecondary)
                FinanceMiniStat("فروشنده", deal.sellerCommissionAmount ?: 0L, DfColors.TextSecondary)
            }
            deal.netAdvisor?.let { net ->
                Text(
                    text = "خالص شما: ${FormatUtils.formatPriceToman(net)}",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.Green,
                )
            }
            DfPrimaryButton(
                text = "تنظیمات کامل مالی",
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FinanceMiniStat(label: String, amount: Long, color: Color) {
    Column {
        Text(text = label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
        Text(
            text = FormatUtils.formatPriceShort(amount),
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealFinanceSheet(
    dealKind: String,
    commissionMode: String,
    buyerRate: String,
    sellerRate: String,
    buyerAmount: String,
    sellerAmount: String,
    advisorPercent: String,
    agencyPercent: String,
    costs: String,
    received: String,
    payoutStatus: String,
    notes: String,
    basis: Long,
    isSubmitting: Boolean,
    onDealKindChange: (String) -> Unit,
    onCommissionModeChange: (String) -> Unit,
    onBuyerRateChange: (String) -> Unit,
    onSellerRateChange: (String) -> Unit,
    onBuyerAmountChange: (String) -> Unit,
    onSellerAmountChange: (String) -> Unit,
    onAdvisorPercentChange: (String) -> Unit,
    onAgencyPercentChange: (String) -> Unit,
    onCostsChange: (String) -> Unit,
    onReceivedChange: (String) -> Unit,
    onPayoutChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val preview = remember(
        dealKind, commissionMode, buyerRate, sellerRate, buyerAmount, sellerAmount,
        advisorPercent, agencyPercent, costs, basis,
    ) {
        computeFinancePreview(
            basis = basis,
            mode = commissionMode,
            buyerRate = buyerRate.toDoubleOrNull() ?: 0.0,
            sellerRate = sellerRate.toDoubleOrNull() ?: 0.0,
            buyerAmount = buyerAmount.toLongOrNull() ?: 0L,
            sellerAmount = sellerAmount.toLongOrNull() ?: 0L,
            advisorPercent = advisorPercent.toDoubleOrNull() ?: 70.0,
            agencyPercent = agencyPercent.toDoubleOrNull() ?: 30.0,
            costs = costs.toLongOrNull() ?: 0L,
        )
    }

    DfSheetScaffold(
        title = "تنظیمات مالی معامله",
        subtitle = "کمیسیون طرفین، سهم مشاور و املاک، هزینه و وصول",
        icon = DfIcons.Coins,
        iconContainerColor = DfColors.GreenLight,
        iconTint = DfColors.Green,
        onClose = onDismiss,
        bodyHeightFraction = 0.92f,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره مالی",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "پیش‌نمایش") {
            FinancePreviewRow("کمیسیون کل", preview.total)
            FinancePreviewRow("سهم مشاور", preview.advisor)
            FinancePreviewRow("سهم املاک", preview.agency)
            FinancePreviewRow("خالص مشاور", preview.net)
        }
        DfSheetSection(title = "نحوه محاسبه") {
            DfDropdown(
                label = "نوع معامله",
                value = dealKindLabel(dealKind),
                options = KindOptions,
                enabled = !isSubmitting,
                onSelect = { onDealKindChange(dealKindValue(it)) },
            )
            DfDropdown(
                label = "مدل کمیسیون",
                value = commissionModeLabel(commissionMode),
                options = ModeOptions,
                enabled = !isSubmitting,
                onSelect = { onCommissionModeChange(commissionModeValue(it)) },
            )
        }
        DfSheetSection(title = "کمیسیون طرفین") {
            OutlinedTextField(
                value = buyerRate,
                onValueChange = onBuyerRateChange,
                label = { Text("نرخ خریدار / مستأجر (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = sellerRate,
                onValueChange = onSellerRateChange,
                label = { Text("نرخ فروشنده / مالک (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            if (commissionMode == "fixed") {
                OutlinedTextField(
                    value = buyerAmount,
                    onValueChange = onBuyerAmountChange,
                    label = { Text("مبلغ کمیسیون خریدار") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    value = sellerAmount,
                    onValueChange = onSellerAmountChange,
                    label = { Text("مبلغ کمیسیون فروشنده") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
            }
        }
        DfSheetSection(title = "تقسیم دفتر") {
            OutlinedTextField(
                value = advisorPercent,
                onValueChange = onAdvisorPercentChange,
                label = { Text("سهم مشاور (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = agencyPercent,
                onValueChange = onAgencyPercentChange,
                label = { Text("سهم املاک (٪)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
        }
        DfSheetSection(title = "وصول") {
            OutlinedTextField(
                value = costs,
                onValueChange = onCostsChange,
                label = { Text("هزینه‌های معامله") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            OutlinedTextField(
                value = received,
                onValueChange = onReceivedChange,
                label = { Text("مبلغ وصول‌شده") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting,
            )
            DfDropdown(
                label = "وضعیت وصول",
                value = payoutLabel(payoutStatus),
                options = PayoutOptions,
                enabled = !isSubmitting,
                onSelect = { onPayoutChange(payoutValue(it)) },
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("یادداشت مالی") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
private fun FinancePreviewRow(label: String, amount: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
        Text(
            text = FormatUtils.formatPriceToman(amount),
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
        )
    }
}

data class FinancePreview(
    val total: Long,
    val advisor: Long,
    val agency: Long,
    val net: Long,
)

fun computeFinancePreview(
    basis: Long,
    mode: String,
    buyerRate: Double,
    sellerRate: Double,
    buyerAmount: Long,
    sellerAmount: Long,
    advisorPercent: Double,
    agencyPercent: Double,
    costs: Long,
): FinancePreview {
    val buyer = if (mode == "fixed") buyerAmount else if (basis > 0) kotlin.math.round(basis * buyerRate / 100.0).toLong() else buyerAmount
    val seller = if (mode == "fixed") sellerAmount else if (basis > 0) kotlin.math.round(basis * sellerRate / 100.0).toLong() else sellerAmount
    val total = buyer + seller
    var adv = advisorPercent.coerceAtLeast(0.0)
    var ag = agencyPercent.coerceAtLeast(0.0)
    val share = adv + ag
    if (share <= 0) {
        adv = 70.0
        ag = 30.0
    } else if (kotlin.math.abs(share - 100.0) > 0.01) {
        adv = adv / share * 100.0
        ag = 100.0 - adv
    }
    val advisor = kotlin.math.round(total * adv / 100.0).toLong()
    val agency = (total - advisor).coerceAtLeast(0)
    return FinancePreview(total = total, advisor = advisor, agency = agency, net = advisor - costs)
}
