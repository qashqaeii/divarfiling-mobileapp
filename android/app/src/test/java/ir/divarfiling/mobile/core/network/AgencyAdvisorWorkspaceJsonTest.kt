package ir.divarfiling.mobile.core.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AgencyAdvisorWorkspaceJsonTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun deserialize_filingTab_datasets_withUuidIds() {
        val raw = """
            {
              "tab": "filing",
              "member_id": 12,
              "datasets": [
                {
                  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "name": "زونکن شمال",
                  "listing_count": 42,
                  "updated_at_display": "۱۴۰۴/۰۱/۰۱"
                }
              ],
              "total": 1,
              "page": 1,
              "num_pages": 1
            }
        """.trimIndent()
        val payload = json.decodeFromString(AgencyAdvisorWorkspacePayload.serializer(), raw)
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", payload.datasets.single().id)
        assertEquals("زونکن شمال", payload.datasets.single().name)
    }

    @Test
    fun deserialize_filingTab_listings_withUuidDatasetId() {
        val raw = """
            {
              "tab": "filing",
              "dataset_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
              "listings": [
                {
                  "id": 99,
                  "token": "abc123",
                  "title": "آگهی نمونه",
                  "dataset_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
                }
              ],
              "total": 1,
              "page": 1,
              "num_pages": 1
            }
        """.trimIndent()
        val payload = json.decodeFromString(AgencyAdvisorWorkspacePayload.serializer(), raw)
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", payload.listings.single().datasetId)
    }
}
