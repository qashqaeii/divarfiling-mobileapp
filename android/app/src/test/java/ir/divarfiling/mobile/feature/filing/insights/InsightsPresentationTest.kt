package ir.divarfiling.mobile.feature.filing.insights

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsightsPresentationTest {

    @Test
    fun presentSignals_parsesLabelValueObjects() {
        val snapshot = buildJsonObject {
            put(
                "signals",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("label", JsonPrimitive("فایل‌های زیر عرف بازار"))
                            put("value", JsonPrimitive("33 فایل"))
                            put("icon", JsonPrimitive("fa-arrow-trend-down"))
                        },
                    )
                },
            )
        }

        val (blocks, _) = presentSnapshot(snapshot)
        assertEquals(1, blocks.size)
        assertEquals("سیگنال‌ها", blocks.first().title)
        assertEquals("33 فایل", blocks.first().facts.first().value)
        assertFalse(blocks.first().facts.first().value.contains("label:"))
    }

    @Test
    fun presentOpportunity_formatsAmenitiesAndReasons() {
        val opportunity = buildJsonObject {
            put("listing_id", JsonPrimitive(60999))
            put("title", JsonPrimitive("اجاره آپارتمان"))
            put("area_fmt", JsonPrimitive("135 متر"))
            put("neighborhood", JsonPrimitive("باغبان نوین"))
            put("price_fmt", JsonPrimitive("1.4 میلیارد تومان"))
            put("expected_price_fmt", JsonPrimitive("1.9 میلیارد تومان"))
            put("value_diff_pct", JsonPrimitive(24.5))
            put("opportunity_score", JsonPrimitive(150))
            put("stars", JsonPrimitive("★★★★★"))
            put("rank", JsonPrimitive(1))
            put(
                "reasons",
                buildJsonArray {
                    add(JsonPrimitive("فول امکانات — ارزش بالاتر از میانه بازار"))
                    add(JsonPrimitive("24.5٪ زیر رهن کامل بازار"))
                },
            )
            put(
                "amenities",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("label", JsonPrimitive("آسانسور"))
                            put("state", JsonPrimitive("yes"))
                            put("tier", JsonPrimitive("core"))
                        },
                    )
                },
            )
            put("amenity_tier", JsonPrimitive("full"))
        }

        val block = presentElementList(listOf(opportunity)).single()
        assertEquals(InsightBlockKind.OPPORTUNITY, block.kind)
        assertEquals("135 متر", block.title)
        assertEquals("باغبان نوین", block.subtitle)
        assertTrue(block.body.contains("• فول امکانات"))
        assertEquals("24.5٪ زیر بازار", block.badge)
        assertEquals(1, block.amenities.size)
        assertEquals("آسانسور", block.amenities.first().label)
        assertTrue(block.facts.none { it.label == "listing id" })
        assertTrue(block.facts.none { it.value.contains("field:"))
    }

    @Test
    fun formatDisplayValue_fixesRtlNegativeSuffix() {
        val snapshot = buildJsonObject {
            put("value_score", JsonPrimitive("18.2-"))
        }
        val (_, kpis) = presentSnapshot(snapshot)
        assertEquals("-18.2", kpis.first().value)
    }
}
