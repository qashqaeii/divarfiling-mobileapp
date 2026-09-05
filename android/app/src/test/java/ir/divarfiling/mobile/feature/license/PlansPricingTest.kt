package ir.divarfiling.mobile.feature.license

import ir.divarfiling.mobile.core.network.ShopDiscountPreviewData
import ir.divarfiling.mobile.core.network.ShopPlanDto
import org.junit.Assert.assertEquals
import org.junit.Test

class PlansPricingTest {

    private fun agencyPlan(
        id: Long = 5L,
        unitFinal: Long = 990_000L,
        min: Int = 2,
        max: Int = 25,
    ) = ShopPlanDto(
        id = id,
        name = "پلن آژانس — ماهانه",
        planType = "monthly",
        allowBulkPurchase = true,
        minQuantity = min,
        maxQuantity = max,
        unitFinalPrice = unitFinal,
        finalPrice = unitFinal,
    )

    @Test
    fun clampQuantity_respectsAgencyBounds() {
        val plan = agencyPlan()
        assertEquals(2, plan.clampQuantity(1))
        assertEquals(25, plan.clampQuantity(99))
        assertEquals(5, plan.clampQuantity(5))
    }

    @Test
    fun resolveCheckoutTotal_prefersPreviewFinalPrice() {
        val plan = agencyPlan()
        val preview = ShopDiscountPreviewData(finalPrice = 1_782_000L, quantity = 2)
        assertEquals(1_782_000L, resolveCheckoutTotal(plan, 2, preview))
    }

    @Test
    fun effectiveUnitPrice_usesPreviewWhenPresent() {
        val plan = agencyPlan(unitFinal = 990_000L)
        val preview = ShopDiscountPreviewData(effectiveUnitPrice = 891_000L)
        assertEquals(891_000L, preview.effectiveUnitPrice(plan))
    }

    @Test
    fun durationLabels_mapPlanTypes() {
        assertEquals("ماهانه", durationLabelFa("monthly"))
        assertEquals("۳۰ روز", durationMetaFa("monthly"))
        assertEquals(0, planTypeSortKey("monthly"))
        assertEquals(2, planTypeSortKey("yearly"))
    }
}
