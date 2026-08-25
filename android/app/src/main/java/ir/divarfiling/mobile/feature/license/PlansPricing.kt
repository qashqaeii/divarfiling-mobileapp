package ir.divarfiling.mobile.feature.license

import ir.divarfiling.mobile.core.network.ShopDiscountPreviewData
import ir.divarfiling.mobile.core.network.ShopPlanDto

fun ShopPlanDto.isAgencyPlan(): Boolean = allowBulkPurchase

fun ShopPlanDto.defaultQuantity(): Int = if (isAgencyPlan()) {
    (minQuantity).coerceAtLeast(1)
} else {
    1
}

fun ShopPlanDto.clampQuantity(raw: Int): Int {
    if (!isAgencyPlan()) return 1
    val minQ = minQuantity.coerceAtLeast(1)
    val maxQ = maxQuantity.coerceAtLeast(minQ)
    return raw.coerceIn(minQ, maxQ)
}

fun ShopPlanDto.unitFinalPrice(): Long = unitFinalPrice ?: finalPrice ?: 0L

fun ShopPlanDto.unitOriginalPrice(): Long = unitOriginalPrice ?: originalPrice ?: unitFinalPrice()

fun ShopPlanDto.totalFinalPrice(quantity: Int): Long = unitFinalPrice() * clampQuantity(quantity)

fun ShopPlanDto.totalOriginalPrice(quantity: Int): Long = unitOriginalPrice() * clampQuantity(quantity)

fun resolveCheckoutTotal(
    plan: ShopPlanDto,
    quantity: Int,
    discountPreview: ShopDiscountPreviewData?,
): Long = discountPreview?.finalPrice ?: plan.totalFinalPrice(quantity)
