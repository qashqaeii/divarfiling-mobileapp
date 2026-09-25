package ir.divarfiling.mobile.core.network

import ir.divarfiling.mobile.feature.ai.smartcommand.SmartCommandStateLogic
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCapabilitiesJsonTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun deserialize_nlCommand_featureRemaining_doesNotFalseBlock() {
        val raw = """
            {
              "ai_enabled": true,
              "has_license": true,
              "can_use_smart_command": true,
              "nl_command": {
                "feature": "nl_command",
                "feature_limit": 50,
                "feature_remaining": 42,
                "daily_remaining": 100
              }
            }
        """.trimIndent()
        val caps = json.decodeFromString(AiCapabilitiesData.serializer(), raw)
        assertEquals(42, caps.nlCommand?.featureRemaining)
        assertNull(SmartCommandStateLogic.evaluateBlock(caps))
    }

    @Test
    fun deserialize_nlCommand_withRemainingKey() {
        val raw = """
            {
              "ai_enabled": true,
              "has_license": true,
              "can_use_smart_command": true,
              "nl_command": { "remaining": 7, "limit": 50, "enabled": true }
            }
        """.trimIndent()
        val caps = json.decodeFromString(AiCapabilitiesData.serializer(), raw)
        assertEquals(7, caps.nlCommand?.remaining)
        assertTrue(SmartCommandStateLogic.smartCommandRemaining(caps)!! > 0)
    }
}
