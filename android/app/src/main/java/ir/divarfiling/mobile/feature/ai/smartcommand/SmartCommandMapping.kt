package ir.divarfiling.mobile.feature.ai.smartcommand

import ir.divarfiling.mobile.core.network.NlCommandParseResult
import ir.divarfiling.mobile.core.network.SmartCommandProfileDto
import ir.divarfiling.mobile.core.network.SmartCommandProfilesDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

enum class SmartCommandProfileKey(val apiKey: String) {
    TODAY("today"),
    CONTACTS("contacts"),
    PROPERTIES("properties"),
    GLOBAL("today"),
    ;

    fun profile(profiles: SmartCommandProfilesDto): SmartCommandProfileDto = when (this) {
        TODAY, GLOBAL -> profiles.today
        CONTACTS -> profiles.contacts
        PROPERTIES -> profiles.properties
    }
}

enum class SmartCommandPhase {
    Idle,
    Parsing,
    Preview,
    ResolvingContact,
    Confirming,
    Success,
    Error,
}

enum class SmartCommandBlockReason {
    AiDisabled,
    NoLicense,
    FeatureDisabled,
    QuotaExhausted,
}

data class ReminderPreviewEdit(
    val contactName: String = "",
    val title: String = "",
    val note: String = "",
    val dateText: String = "",
    val timeText: String = "",
    val dueAtDisplay: String = "",
)

data class ContactPreviewEdit(
    val name: String = "",
    val phone: String = "",
    val customerType: String = "",
    val companyName: String = "",
    val notes: String = "",
    val areas: String = "",
)

data class PropertyPreviewEdit(
    val propertyType: String = "",
    val dealMode: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val area: String = "",
    val rooms: String = "",
    val salePrice: String = "",
    val deposit: String = "",
    val monthlyRent: String = "",
    val notes: String = "",
)

object SmartCommandMapping {
    const val MAX_INPUT_CHARS = 1500

    const val INTENT_UNKNOWN = "unknown"
    const val INTENT_REMINDER = "create_reminder"
    const val INTENT_CONTACT = "create_contact"
    const val INTENT_PROPERTY = "create_personal_property"

    fun unknownUserMessage(examples: List<String>): String {
        val hint = examples.take(2).joinToString("\n") { "• $it" }
        return buildString {
            append("متوجه نشدم چه کاری باید انجام بدم. درخواستت رو کمی واضح‌تر بگو یا بنویس.")
            if (hint.isNotBlank()) {
                append("\n\n")
                append(hint)
            }
        }
    }

    fun previewString(preview: JsonObject?, key: String): String {
        val el = preview?.get(key) ?: return ""
        return el.jsonPrimitive.contentOrNull?.trim().orEmpty()
    }

    fun reminderFromPreview(preview: JsonObject?): ReminderPreviewEdit {
        if (preview == null) return ReminderPreviewEdit()
        return ReminderPreviewEdit(
            contactName = previewString(preview, "contact_name"),
            title = previewString(preview, "title"),
            note = previewString(preview, "note"),
            dateText = previewString(preview, "date_text"),
            timeText = previewString(preview, "time_text"),
            dueAtDisplay = previewString(preview, "due_at_display"),
        )
    }

    fun contactFromPreview(preview: JsonObject?): ContactPreviewEdit {
        if (preview == null) return ContactPreviewEdit()
        val name = previewString(preview, "name").ifBlank {
            firstSectionLine(preview, 0, 0)
        }
        return ContactPreviewEdit(
            name = name,
            phone = previewString(preview, "phone_display").ifBlank { firstSectionLine(preview, 0, 1) },
            customerType = previewString(preview, "customer_type"),
            companyName = firstMatchingLine(preview, "شرکت"),
            notes = previewString(preview, "notes"),
            areas = previewString(preview, "areas_display"),
        )
    }

    fun propertyFromPreview(preview: JsonObject?): PropertyPreviewEdit {
        if (preview == null) return PropertyPreviewEdit()
        return PropertyPreviewEdit(
            propertyType = previewString(preview, "type_deal_display").substringBefore(" · ").trim(),
            dealMode = previewString(preview, "type_deal_display").substringAfter(" · ", "").trim(),
            neighborhood = previewString(preview, "location_display"),
            city = "",
            area = previewString(preview, "specs_display").substringBefore(" · ").replace(" متر", "").trim(),
            rooms = previewString(preview, "specs_display").substringAfter(" · ", "").replace(" خواب", "").trim(),
            salePrice = previewString(preview, "sale_price_display"),
            deposit = previewString(preview, "deposit_display"),
            monthlyRent = previewString(preview, "rent_display"),
            notes = previewString(preview, "notes"),
        )
    }

    fun needsContactResolution(result: NlCommandParseResult): Boolean =
        result.intent == INTENT_REMINDER &&
            result.contactCandidates.isNotEmpty() &&
            result.ambiguousFields.contains("contact_name")

    fun quotaHint(remaining: Int?, limit: Int?): String? {
        if (remaining == null || limit == null || limit <= 0) return null
        return "$remaining از $limit فرمان امروز باقی مانده"
    }

    fun buildResumeCorrection(intent: String, original: String, edited: String, fieldLabel: String): String? {
        val o = original.trim()
        val e = edited.trim()
        if (o == e || e.isBlank()) return null
        return when (intent) {
            INTENT_REMINDER -> when (fieldLabel) {
                "title" -> "عنوانش $e باشه"
                "note" -> "یادداشت: $e"
                "date" -> "تاریخ $e"
                "time" -> "ساعت $e"
                "contact" -> "مخاطب $e"
                else -> "$fieldLabel: $e"
            }
            INTENT_CONTACT -> when (fieldLabel) {
                "name" -> "نام مخاطب $e"
                "phone" -> "شماره $e"
                "type" -> "نوع مخاطب $e"
                "company" -> "شرکت $e"
                "areas" -> "منطقه $e"
                "notes" -> "توضیحات: $e"
                else -> "$fieldLabel: $e"
            }
            INTENT_PROPERTY -> when (fieldLabel) {
                "price" -> "قیمت $e"
                "area" -> "متراژ $e متر"
                "neighborhood" -> "منطقه $e"
                "city" -> "شهر $e"
                "type" -> "نوع ملک $e"
                "deal" -> "نوع معامله $e"
                "rooms" -> "$e اتاق"
                "deposit" -> "ودیعه $e"
                "rent" -> "اجاره $e"
                "notes" -> "توضیحات: $e"
                else -> "$fieldLabel: $e"
            }
            else -> null
        }
    }

    fun mergeCorrections(parts: List<String>): String? {
        val cleaned = parts.map { it.trim() }.filter { it.isNotBlank() }
        if (cleaned.isEmpty()) return null
        return cleaned.joinToString("، ")
    }

    fun mapApiError(code: String?, message: String, messages: ir.divarfiling.mobile.core.network.AiCapabilitiesMessages): Pair<SmartCommandBlockReason?, String> {
        return when (code?.uppercase()) {
            "QUOTA_EXCEEDED" -> SmartCommandBlockReason.QuotaExhausted to messages.quotaExhausted.ifBlank { message }
            "FORBIDDEN" -> {
                if (message.contains("لایسنس")) {
                    SmartCommandBlockReason.NoLicense to messages.noLicense.ifBlank { message }
                } else {
                    SmartCommandBlockReason.FeatureDisabled to message
                }
            }
            "SERVICE_UNAVAILABLE" -> SmartCommandBlockReason.AiDisabled to messages.aiDisabled.ifBlank { message }
            else -> null to message
        }
    }

    private fun firstSectionLine(preview: JsonObject, sectionIndex: Int, lineIndex: Int): String {
        val sections = preview["sections"]?.jsonArray ?: return ""
        val section = sections.getOrNull(sectionIndex)?.jsonObject ?: return ""
        val lines = section["lines"]?.jsonArray ?: return ""
        return lines.getOrNull(lineIndex)?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    }

    private fun firstMatchingLine(preview: JsonObject, prefix: String): String {
        val sections = preview["sections"]?.jsonArray ?: return ""
        for (i in 0 until sections.size) {
            val lines = sections[i].jsonObject["lines"]?.jsonArray ?: continue
            for (j in 0 until lines.size) {
                val line = lines[j].jsonPrimitive.contentOrNull.orEmpty()
                if (line.contains(prefix)) return line.substringAfter(prefix).trim().trim(':', ' ')
            }
        }
        return ""
    }
}
