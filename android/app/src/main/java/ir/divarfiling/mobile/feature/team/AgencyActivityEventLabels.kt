package ir.divarfiling.mobile.feature.team

import ir.divarfiling.mobile.core.network.AgencyActivityDto
import ir.divarfiling.mobile.core.network.AgencyEventTypeOptionDto

private val EVENT_TYPE_FA = mapOf(
    "announcement.created" to "ثبت اطلاعیه",
    "announcement.updated" to "ویرایش اطلاعیه",
    "seat.assigned" to "تخصیص صندلی",
    "seat.released" to "آزادسازی صندلی",
    "member.invited" to "دعوت مشاور",
    "member.joined" to "عضویت مشاور",
    "member.removed" to "حذف عضو",
    "lead.assigned" to "تخصیص سرنخ",
    "lead.transferred" to "انتقال سرنخ",
    "space.published" to "انتشار در فضای آژانس",
    "space.unpublished" to "لغو انتشار",
    "space.copied" to "کپی از فضای آژانس",
    "deal.created" to "ثبت معامله",
    "deal.closed" to "بستن معامله",
    "contract.signed" to "امضای قرارداد",
)

fun AgencyActivityDto.localizedEventType(): String {
    val key = eventType.trim()
    if (key.isBlank()) return ""
    return EVENT_TYPE_FA[key]
        ?: EVENT_TYPE_FA[key.replace('.', '_')]
        ?: "فعالیت آژانس"
}

fun AgencyActivityDto.displayTitle(): String {
    val label = eventLabel.trim()
    val titled = title.trim()
    if (label.isNotBlank() && !label.contains('.') && label != titled) return label
    if (titled.isNotBlank() && !titled.contains('.')) {
        val duplicate = titled.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (duplicate.size >= 2 && duplicate.distinct().size == 1) return duplicate.first()
        return titled
    }
    return localizedEventType().ifBlank { "فعالیت آژانس" }
}

fun AgencyActivityDto.displaySubtitle(): String {
    val actor = actorName.trim()
    val typeFa = localizedEventType()
    return when {
        actor.isNotBlank() && typeFa.isNotBlank() && typeFa != "فعالیت آژانس" -> "$actor · $typeFa"
        actor.isNotBlank() -> actor
        else -> ""
    }
}

fun AgencyEventTypeOptionDto.localizedLabel(): String {
    val key = value.trim()
    if (label.isNotBlank() && !label.contains('.')) return label
    return EVENT_TYPE_FA[key] ?: label.ifBlank { "فعالیت آژانس" }
}

data class AgencyActivityEventVisual(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tintKey: AgencyActivityTint,
)

enum class AgencyActivityTint {
    Primary,
    Info,
    Success,
    Warning,
    Error,
}

fun activityEventVisual(eventType: String): AgencyActivityEventVisual {
    val key = eventType.trim().lowercase()
    return when {
        key.startsWith("announcement") -> AgencyActivityEventVisual(
            ir.divarfiling.mobile.core.design.DfIcons.Sparkles,
            AgencyActivityTint.Warning,
        )
        key.startsWith("seat") -> AgencyActivityEventVisual(
            ir.divarfiling.mobile.core.design.DfIcons.Lock,
            AgencyActivityTint.Info,
        )
        key.startsWith("member") || key.startsWith("lead") -> AgencyActivityEventVisual(
            ir.divarfiling.mobile.core.design.DfIcons.Users,
            AgencyActivityTint.Primary,
        )
        key.startsWith("space") -> AgencyActivityEventVisual(
            ir.divarfiling.mobile.core.design.DfIcons.Layers,
            AgencyActivityTint.Success,
        )
        key.startsWith("deal") || key.startsWith("contract") -> AgencyActivityEventVisual(
            ir.divarfiling.mobile.core.design.DfIcons.Handshake,
            AgencyActivityTint.Success,
        )
        else -> AgencyActivityEventVisual(
            ir.divarfiling.mobile.core.design.DfIcons.Clock,
            AgencyActivityTint.Primary,
        )
    }
}
