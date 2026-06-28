package ir.divarfiling.mobile.core.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DatasetInsightsDeserializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun `decodes doughnut chart with array backgroundColor`() {
        val payload = """
            {
              "dataset": {
                "id": "00000000-0000-0000-0000-000000000001",
                "name": "test",
                "item_count": 10
              },
              "meta": {
                "row_count": 10,
                "clean_count": 10,
                "lite_mode": false,
                "geo_count": 0,
                "is_rent": false
              },
              "charts": [
                {
                  "type": "doughnut",
                  "title": "چند فایل ارزون، سر به سر و گرونه؟",
                  "labels": ["زیر بازار", "سر به سر", "بالای بازار"],
                  "datasets": [{
                    "data": [3, 4, 3],
                    "backgroundColor": [
                      "rgba(16,185,129,0.85)",
                      "rgba(59,130,246,0.75)",
                      "rgba(239,68,68,0.75)"
                    ]
                  }]
                }
              ],
              "l2": {
                "tabs": [{
                  "id": "overview",
                  "label": "نمای کلی",
                  "charts": [{
                    "type": "bar",
                    "title": "سطح قیمت",
                    "labels": ["p10", "p50", "p90"],
                    "datasets": [{
                      "label": "قیمت هر متر",
                      "data": [100, 200, 300],
                      "backgroundColor": "rgba(59,130,246,0.75)"
                    }]
                  }]
                }]
              }
            }
        """.trimIndent()

        val insights = json.decodeFromString(DatasetInsightsDto.serializer(), payload)

        val doughnutBg = insights.charts.first().datasets.first().backgroundColor
        assertNotNull(doughnutBg)
        assertTrue(doughnutBg!!.toString().contains("rgba(16,185,129,0.85)"))

        val barBg = insights.l2?.tabs?.first()?.charts?.first()?.datasets?.first()?.backgroundColor
        assertEquals("\"rgba(59,130,246,0.75)\"", barBg.toString())
    }
}
