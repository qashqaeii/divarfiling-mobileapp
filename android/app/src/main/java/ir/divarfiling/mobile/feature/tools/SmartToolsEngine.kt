package ir.divarfiling.mobile.feature.tools

import kotlin.math.roundToInt

object SmartToolsEngine {

    private const val STANDARD_DEPOSIT_RENT_RATIO = 30

    fun fullDepositEquiv(deposit: Long?, rent: Long?, ratio: Int = STANDARD_DEPOSIT_RENT_RATIO): Long? {
        val d = deposit ?: 0L
        val r = rent ?: 0L
        if (d == 0L && r == 0L) return null
        return d + r * ratio.coerceAtLeast(1)
    }

    fun depositRentConvert(
        fullDeposit: Long? = null,
        fullRent: Long? = null,
        rate: Int = STANDARD_DEPOSIT_RENT_RATIO,
    ): DepositRentConvertResult {
        if (fullDeposit != null && fullDeposit > 0 && rate > 0) {
            return DepositRentConvertResult(
                rate = rate,
                monthlyRent = fullDeposit / rate,
                fullDeposit = fullDeposit,
                from = "deposit",
            )
        }
        if (fullRent != null && fullRent > 0 && rate > 0) {
            return DepositRentConvertResult(
                rate = rate,
                monthlyRent = fullRent,
                fullDeposit = fullRent * rate,
                from = "rent",
            )
        }
        return DepositRentConvertResult(rate = rate)
    }

    fun mixedRentCalc(
        deposit: Long? = null,
        rent: Long? = null,
        rate: Int = STANDARD_DEPOSIT_RENT_RATIO,
    ): MixedRentResult? {
        if (rate <= 0) return null
        val d = deposit ?: 0L
        val r = rent ?: 0L
        if (d == 0L && r == 0L) return null
        val fullDep = fullDepositEquiv(d, r, rate) ?: return null
        return MixedRentResult(
            deposit = d,
            rent = r,
            rate = rate,
            fullDeposit = fullDep,
            fullRent = fullDep / rate,
        )
    }

    fun compareProperties(
        a: CompareInput,
        b: CompareInput,
        isRent: Boolean = false,
        rate: Int = STANDARD_DEPOSIT_RENT_RATIO,
    ): CompareResult {
        val rowA = listingCompareRow(a, isRent, rate)
        val rowB = listingCompareRow(b, isRent, rate)
        if (rowA == null || rowB == null) {
            return CompareResult(isRent = isRent, rate = rate, a = rowA, b = rowB)
        }
        val ppsA = rowA.perSqm
        val ppsB = rowB.perSqm
        if (ppsA <= 0 || ppsB <= 0) {
            return CompareResult(isRent = isRent, rate = rate, a = rowA, b = rowB)
        }
        val diffPct = ((ppsB - ppsA).toDouble() / ppsA * 100).roundToInt() / 10.0
        val (winner, verdict) = when {
            diffPct < -1 -> "b" to "${b.label.ifBlank { "آگهی ب" }} ارزان‌تر است"
            diffPct > 1 -> "a" to "${a.label.ifBlank { "آگهی الف" }} ارزان‌تر است"
            else -> "tie" to "تقریباً برابر"
        }
        return CompareResult(
            isRent = isRent,
            rate = rate,
            a = rowA,
            b = rowB,
            diffPct = diffPct,
            winner = winner,
            verdict = verdict,
            cheaperPerSqm = minOf(ppsA, ppsB),
        )
    }

    fun discountCalc(listPrice: Long, discountPct: Double): DiscountResult {
        val pct = discountPct.coerceIn(0.0, 100.0)
        val savings = (listPrice * pct / 100).toLong()
        return DiscountResult(
            listPrice = listPrice,
            discountPct = pct,
            savings = savings,
            finalPrice = listPrice - savings,
        )
    }

    fun rentCommissionCalc(
        monthlyRent: Long,
        tenantShare: Double = 50.0,
        landlordShare: Double = 50.0,
    ): RentCommissionResult {
        val tenant = (monthlyRent * tenantShare / 100).toLong()
        val landlord = (monthlyRent * landlordShare / 100).toLong()
        return RentCommissionResult(
            monthlyRent = monthlyRent,
            tenantCommission = tenant,
            landlordCommission = landlord,
            totalCommission = tenant + landlord,
            tenantShare = tenantShare,
            landlordShare = landlordShare,
        )
    }

    fun budgetCalc(
        budget: Long,
        pricePerSqm: Double? = null,
        area: Double? = null,
    ): BudgetResult {
        val result = BudgetResult(budget = budget)
        val pps = pricePerSqm?.takeIf { it > 0 }
        val ar = area?.takeIf { it > 0 }
        return when {
            budget > 0 && pps != null && ar == null -> result.copy(
                mode = "max_area",
                maxArea = (budget / pps * 10).roundToInt() / 10.0,
                pricePerSqm = pps,
            )
            budget > 0 && ar != null && pps == null -> result.copy(
                mode = "required_pps",
                requiredPps = (budget / ar).toLong(),
                area = ar,
            )
            budget > 0 && pps != null && ar != null -> {
                val total = (pps * ar).toLong()
                result.copy(
                    mode = "affordability",
                    totalPrice = total,
                    pricePerSqm = pps,
                    area = ar,
                    affordable = total <= budget,
                    shortfall = maxOf(0, total - budget),
                    surplus = maxOf(0, budget - total),
                )
            }
            else -> result
        }
    }

    fun pricingSuggest(
        area: Double,
        marketMedianPps: Double,
        adjustmentPct: Double = 0.0,
    ): PricingSuggestResult? {
        if (area <= 0 || marketMedianPps <= 0) return null
        val adj = 1 + adjustmentPct / 100
        val suggestedPps = marketMedianPps * adj
        return PricingSuggestResult(
            area = area,
            marketMedianPps = marketMedianPps,
            adjustmentPct = adjustmentPct,
            suggestedPps = suggestedPps.roundToInt().toLong(),
            suggestedTotal = (suggestedPps * area).toLong(),
        )
    }

    fun areaPriceCalc(
        pricePerSqm: Double? = null,
        area: Double? = null,
        totalPrice: Long? = null,
    ): AreaPriceResult? {
        val pps = pricePerSqm?.takeIf { it > 0 }
        val ar = area?.takeIf { it > 0 }
        val total = totalPrice?.takeIf { it > 0 }
        return when {
            pps != null && ar != null -> AreaPriceResult(
                totalPrice = (pps * ar).toLong(),
                pricePerSqm = pps,
                area = ar,
            )
            total != null && ar != null -> AreaPriceResult(
                pricePerSqm = (total / ar).toLong().toDouble(),
                totalPrice = total,
                area = ar,
            )
            total != null && pps != null -> AreaPriceResult(
                area = (total / pps * 10).roundToInt() / 10.0,
                totalPrice = total,
                pricePerSqm = pps,
            )
            else -> null
        }
    }

    fun commissionCalc(
        price: Long,
        buyerPct: Double = 1.0,
        sellerPct: Double = 1.0,
    ): SalesCommissionResult {
        val buyer = (price * buyerPct / 100).toLong()
        val seller = (price * sellerPct / 100).toLong()
        return SalesCommissionResult(
            price = price,
            buyerCommission = buyer,
            sellerCommission = seller,
            totalCommission = buyer + seller,
            buyerPct = buyerPct,
            sellerPct = sellerPct,
        )
    }

    private fun listingCompareRow(
        input: CompareInput,
        isRent: Boolean,
        rate: Int,
    ): CompareRow? {
        val area = input.area ?: return null
        if (area <= 0) return null
        return if (isRent) {
            val fd = fullDepositEquiv(input.deposit, input.rent, rate) ?: return null
            CompareRow(
                label = input.label,
                area = area,
                fullDeposit = fd,
                perSqm = (fd.toDouble() / area).roundToInt().toLong(),
                metric = "full_deposit",
            )
        } else {
            val price = input.price ?: return null
            CompareRow(
                label = input.label,
                area = area,
                price = price,
                perSqm = (price.toDouble() / area).roundToInt().toLong(),
                metric = "price",
            )
        }
    }
}

data class DepositRentConvertResult(
    val rate: Int,
    val monthlyRent: Long? = null,
    val fullDeposit: Long? = null,
    val from: String? = null,
)

data class MixedRentResult(
    val deposit: Long,
    val rent: Long,
    val rate: Int,
    val fullDeposit: Long,
    val fullRent: Long,
)

data class CompareInput(
    val label: String = "",
    val price: Long? = null,
    val deposit: Long? = null,
    val rent: Long? = null,
    val area: Double? = null,
)

data class CompareRow(
    val label: String,
    val area: Double,
    val price: Long? = null,
    val fullDeposit: Long? = null,
    val perSqm: Long,
    val metric: String,
)

data class CompareResult(
    val isRent: Boolean,
    val rate: Int,
    val a: CompareRow?,
    val b: CompareRow?,
    val diffPct: Double? = null,
    val winner: String? = null,
    val verdict: String? = null,
    val cheaperPerSqm: Long? = null,
)

data class DiscountResult(
    val listPrice: Long,
    val discountPct: Double,
    val savings: Long,
    val finalPrice: Long,
)

data class RentCommissionResult(
    val monthlyRent: Long,
    val tenantCommission: Long,
    val landlordCommission: Long,
    val totalCommission: Long,
    val tenantShare: Double,
    val landlordShare: Double,
)

data class BudgetResult(
    val budget: Long,
    val mode: String? = null,
    val maxArea: Double? = null,
    val pricePerSqm: Double? = null,
    val requiredPps: Long? = null,
    val area: Double? = null,
    val totalPrice: Long? = null,
    val affordable: Boolean? = null,
    val shortfall: Long? = null,
    val surplus: Long? = null,
)

data class PricingSuggestResult(
    val area: Double,
    val marketMedianPps: Double,
    val adjustmentPct: Double,
    val suggestedPps: Long,
    val suggestedTotal: Long,
)

data class AreaPriceResult(
    val pricePerSqm: Double? = null,
    val area: Double? = null,
    val totalPrice: Long? = null,
)

data class SalesCommissionResult(
    val price: Long,
    val buyerCommission: Long,
    val sellerCommission: Long,
    val totalCommission: Long,
    val buyerPct: Double,
    val sellerPct: Double,
)
