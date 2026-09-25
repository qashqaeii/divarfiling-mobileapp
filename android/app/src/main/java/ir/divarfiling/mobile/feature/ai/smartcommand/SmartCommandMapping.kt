package ir.divarfiling.mobile.feature.ai.smartcommand

import ir.divarfiling.mobile.core.network.NlCommandParseResult
import ir.divarfiling.mobile.core.network.SmartCommandExampleDto
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

data class WrongIntentState(
    val detectedIntent: String,
    val message: String,
    val navigation: WrongIntentDestination?,
)

enum class WrongIntentDestination {
    Today,
    Contacts,
    Properties,
}

data class SmartCommandPreviewLineUi(
    val label: String,
    val value: String,
    val isMissing: Boolean = false,
    val isWarning: Boolean = false,
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

    fun unknownUserMessage(examples: List<SmartCommandExampleDto>): String {
        val hint = examples.take(2).joinToString("\n") { "• ${it.commandText()}" }
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

    fun needsContactResolution(
        profile: SmartCommandProfileDto?,
        result: NlCommandParseResult,
    ): Boolean =
        profile?.enableContactResolve == true &&
            result.intent == INTENT_REMINDER &&
            result.contactCandidates.isNotEmpty() &&
            result.ambiguousFields.contains("contact_name")

    fun isWrongIntent(profile: SmartCommandProfileDto?, data: NlCommandParseResult): Boolean {
        val allowed = profile?.allowedIntent?.trim().orEmpty()
        if (allowed.isBlank() || data.intent.isBlank() || data.intent == INTENT_UNKNOWN) return false
        return data.intent != allowed
    }

    fun wrongIntentState(profile: SmartCommandProfileDto?, data: NlCommandParseResult): WrongIntentState? {
        if (!isWrongIntent(profile, data)) return null
        val nav = wrongIntentDestination(data.intent)
        val message = profile?.wrongIntentMessage?.trim().orEmpty()
            .ifBlank { "این فرمان در این بخش پشتیبانی نمی‌شود." }
        return WrongIntentState(
            detectedIntent = data.intent,
            message = message,
            navigation = nav,
        )
    }

    fun wrongIntentDestination(intent: String): WrongIntentDestination? = when (intent) {
        INTENT_REMINDER -> WrongIntentDestination.Today
        INTENT_CONTACT -> WrongIntentDestination.Contacts
        INTENT_PROPERTY -> WrongIntentDestination.Properties
        else -> null
    }

    fun wrongIntentCtaLabel(destination: WrongIntentDestination): String = when (destination) {
        WrongIntentDestination.Today -> "رفتن به کارهای امروز"
        WrongIntentDestination.Contacts -> "رفتن به مخاطبین"
        WrongIntentDestination.Properties -> "رفتن به فایل‌های شخصی"
    }

    fun profileExamplesForChips(examples: List<SmartCommandExampleDto>, max: Int = 3): List<SmartCommandExampleDto> =
        examples.filter { it.commandText().isNotBlank() }.take(max)

    fun defaultPlaceholder(profileKey: SmartCommandProfileKey): String = when (profileKey) {
        SmartCommandProfileKey.TODAY, SmartCommandProfileKey.GLOBAL ->
            "مثلاً: فردا ساعت ۱۰ یادم بنداز به احمدی زنگ بزنم"
        SmartCommandProfileKey.CONTACTS ->
            "مثلاً: احمدی، 0912...، خریدار پونک با بودجه تا ۱۲ میلیارد"
        SmartCommandProfileKey.PROPERTIES ->
            "مثلاً: پونک، ۱۲۰ متر، دو خواب، فروش ۱۲ میلیارد"
    }

    fun entrySubtitle(profile: SmartCommandProfileDto?, profileKey: SmartCommandProfileKey): String =
        profile?.subtitle?.trim().orEmpty().ifBlank {
            when (profileKey) {
                SmartCommandProfileKey.CONTACTS -> "نام، شماره و نیاز مشتری را در یک جمله وارد کنید"
                SmartCommandProfileKey.PROPERTIES -> "محله، متراژ و قیمت را در یک جمله بنویسید"
                SmartCommandProfileKey.TODAY, SmartCommandProfileKey.GLOBAL ->
                    "زمان و مخاطب را در یک جمله بنویسید یا بگویید"
            }
        }

    fun entryPlaceholder(profile: SmartCommandProfileDto?, profileKey: SmartCommandProfileKey): String =
        profile?.placeholder?.trim().orEmpty().ifBlank { defaultPlaceholder(profileKey) }

    fun previewLinesFromApi(preview: JsonObject?, missingFields: List<String>): List<SmartCommandPreviewLineUi> {
        val missing = missingFields.toSet()
        val lines = mutableListOf<SmartCommandPreviewLineUi>()
        val sections = preview?.get("sections")?.jsonArray
        if (sections != null) {
            for (i in 0 until sections.size) {
                val section = sections[i].jsonObject
                val title = section["title"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                val sectionLines = section["lines"]?.jsonArray
                if (sectionLines != null) {
                    for (j in 0 until sectionLines.size) {
                        val raw = sectionLines[j].jsonPrimitive.contentOrNull.orEmpty()
                        if (raw.isBlank()) continue
                        val (label, value) = splitPreviewLine(raw, title)
                        lines.add(
                            SmartCommandPreviewLineUi(
                                label = label,
                                value = value.ifBlank { "—" },
                            ),
                        )
                    }
                }
            }
        }
        if (lines.isNotEmpty()) return lines
        return fallbackPreviewLines(preview, missing)
    }

    fun humanizeMissingFields(fields: List<String>, intent: String): List<String> {
        if (fields.isEmpty()) return emptyList()
        return fields.mapNotNull { field -> humanizeMissingField(field, intent) }.distinct()
    }

    fun humanizeAmbiguousFields(fields: List<String>, intent: String): List<String> {
        if (fields.isEmpty()) return emptyList()
        return fields.mapNotNull { field ->
            when (field) {
                "contact_name" -> "چند مخاطب با این نام پیدا شد"
                "due_at" -> if (intent == INTENT_REMINDER) "زمان یادآوری نامعتبر یا در گذشته است" else null
                else -> null
            }
        }.distinct()
    }

    fun isIncompleteReminder(data: NlCommandParseResult): Boolean {
        if (data.intent != INTENT_REMINDER || data.canConfirm) return false
        if (data.contactNotFound) return false
        if (data.contactCandidates.isNotEmpty() && data.ambiguousFields.contains("contact_name")) return false
        if (data.missingFields.isNotEmpty()) return true
        return data.ambiguousFields.contains("due_at")
    }

    fun isContactNotFound(data: NlCommandParseResult): Boolean =
        data.intent == INTENT_REMINDER && !data.canConfirm && data.contactNotFound

    fun incompleteReminderMessage(data: NlCommandParseResult): String {
        val missing = data.missingFields
        val ambiguous = data.ambiguousFields
        when {
            ambiguous.contains("due_at") ->
                return data.hint ?: data.message ?: "زمان یادآوری در گذشته است یا نامعتبر است."
            missing.contains("date_text") || missing.contains("time_text") ->
                return data.hint ?: data.message ?: "زمان یادآوری مشخص نشده"
            data.hint?.contains("کامل") == true -> return "چی رو یادت بندازم؟"
            !data.hint.isNullOrBlank() -> return data.hint!!
            !data.message.isNullOrBlank() -> return data.message!!
            else -> return "چی رو یادت بندازم؟"
        }
    }

    @Deprecated("Use needsContactResolution(profile, result)", ReplaceWith("needsContactResolution(null, result)"))
    fun needsContactResolution(result: NlCommandParseResult): Boolean =
        needsContactResolution(null, result)

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

    private fun splitPreviewLine(raw: String, sectionTitle: String): Pair<String, String> {
        val colon = raw.indexOf(':')
        val dash = raw.indexOf('·')
        val sep = when {
            colon in 1 until raw.length -> colon
            dash in 1 until raw.length -> dash
            else -> -1
        }
        return if (sep > 0) {
            raw.substring(0, sep).trim() to raw.substring(sep + 1).trim()
        } else if (sectionTitle.isNotBlank()) {
            sectionTitle to raw.trim()
        } else {
            "" to raw.trim()
        }
    }

    private fun fallbackPreviewLines(preview: JsonObject?, missing: Set<String>): List<SmartCommandPreviewLineUi> {
        if (preview == null) return emptyList()
        val keys = listOf(
            "contact_name" to "مخاطب",
            "due_at_display" to "زمان",
            "title" to "موضوع",
            "name" to "نام",
            "phone_display" to "شماره",
            "type_deal_display" to "نوع ملک",
            "location_display" to "موقعیت",
            "specs_display" to "مشخصات",
            "sale_price_display" to "قیمت",
            "notes" to "یادداشت",
        )
        return keys.mapNotNull { (key, label) ->
            val value = previewString(preview, key)
            if (value.isBlank()) return@mapNotNull null
            SmartCommandPreviewLineUi(
                label = label,
                value = value,
                isMissing = missing.contains(key),
            )
        }
    }

    private fun humanizeMissingField(field: String, intent: String): String? = when (field) {
        "date_text", "time_text" -> "زمان یادآوری مشخص نشده"
        "contact_name" -> if (intent == INTENT_REMINDER) "مخاطب یادآوری مشخص نشده" else null
        "name" -> "نام مخاطب وارد نشده"
        "phone" -> "شماره تماس وارد نشده"
        "deal_mode", "area" -> "اطلاعات ملک ناقص است"
        else -> null
    }
}
