package ir.divarfiling.mobile.feature.support

import androidx.compose.ui.graphics.Color
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.network.SupportTicketDto
import ir.divarfiling.mobile.core.network.SupportTicketStatsDto
import java.io.File
import java.util.Locale

object SupportAttachmentRules {
    const val MAX_BYTES = 10 * 1024 * 1024L
    private val allowedExtensions = setOf(
        "jpg", "jpeg", "png", "pdf", "doc", "docx", "xls", "xlsx", "txt", "zip",
    )

    fun validate(fileName: String, sizeBytes: Long): String? {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.US)
        if (ext.isBlank() || ext !in allowedExtensions) {
            val allowed = allowedExtensions.sorted().joinToString("، ")
            return "فرمت فایل مجاز نیست. پسوندهای مجاز: $allowed"
        }
        if (sizeBytes > MAX_BYTES) {
            return "حجم فایل نباید بیشتر از ${MAX_BYTES / (1024 * 1024)} مگابایت باشد."
        }
        return null
    }

    fun validate(file: File): String? = validate(file.name, file.length())
}

enum class SupportTicketFilter(val label: String) {
    All("همه"),
    Unread("پاسخ جدید"),
    Open("باز"),
    WaitingUser("منتظر شما"),
    Closed("بسته");

    fun apiStatusParam(): String? = when (this) {
        WaitingUser -> "waiting_user"
        Closed -> "closed"
        else -> null
    }

    fun count(stats: SupportTicketStatsDto, tickets: List<SupportTicketDto>): Int = when (this) {
        All -> stats.total
        Unread -> stats.unreadReplies
        Open -> stats.open
        WaitingUser -> stats.waitingUser
        Closed -> stats.closed
    }

    fun applyClientFilter(tickets: List<SupportTicketDto>): List<SupportTicketDto> = when (this) {
        All -> tickets
        Unread -> tickets.filter { it.userHasUnread }
        Open -> tickets.filter { it.status != TICKET_STATUS_CLOSED }
        WaitingUser -> tickets.filter { it.status == TICKET_STATUS_WAITING_USER }
        Closed -> tickets.filter { it.status == TICKET_STATUS_CLOSED }
    }
}

data class SupportTimelineStep(
    val key: String,
    val label: String,
    val step: Int,
)

const val TICKET_STATUS_OPEN = "open"
const val TICKET_STATUS_IN_REVIEW = "in_review"
const val TICKET_STATUS_ANSWERED = "answered"
const val TICKET_STATUS_WAITING_USER = "waiting_user"
const val TICKET_STATUS_CLOSED = "closed"

val supportTimelineSteps = listOf(
    SupportTimelineStep(TICKET_STATUS_OPEN, "ثبت شد", 1),
    SupportTimelineStep(TICKET_STATUS_IN_REVIEW, "در بررسی", 2),
    SupportTimelineStep(TICKET_STATUS_ANSWERED, "پاسخ داده شد", 3),
    SupportTimelineStep(TICKET_STATUS_WAITING_USER, "منتظر شما", 4),
    SupportTimelineStep(TICKET_STATUS_CLOSED, "بسته شده", 5),
)

fun ticketStatusStep(status: String): Int = when (status) {
    TICKET_STATUS_OPEN -> 1
    TICKET_STATUS_IN_REVIEW -> 2
    TICKET_STATUS_ANSWERED -> 3
    TICKET_STATUS_WAITING_USER -> 4
    TICKET_STATUS_CLOSED -> 5
    else -> 1
}

fun ticketStatusLabel(status: String): String = when (status) {
    TICKET_STATUS_OPEN -> "باز"
    TICKET_STATUS_IN_REVIEW -> "در حال بررسی"
    TICKET_STATUS_ANSWERED -> "پاسخ داده‌شده"
    TICKET_STATUS_WAITING_USER -> "منتظر شما"
    TICKET_STATUS_CLOSED -> "بسته"
    else -> status
}

fun ticketStatusColors(status: String): Pair<Color, Color> = when (status) {
    TICKET_STATUS_OPEN -> AppColors.BlueLight to AppColors.Blue
    TICKET_STATUS_IN_REVIEW -> AppColors.AmberLight to AppColors.Amber
    TICKET_STATUS_ANSWERED -> AppColors.GreenLight to AppColors.Green
    TICKET_STATUS_WAITING_USER -> AppColors.PurpleContainer to AppColors.PurpleDark
    TICKET_STATUS_CLOSED -> AppColors.LockedContainer to AppColors.OnLocked
    else -> AppColors.PurpleContainer to AppColors.PurpleDark
}

fun supportPriorityLabel(value: String): String = when (value) {
    "low" -> "کم"
    "normal" -> "معمولی"
    "high" -> "مهم"
    "urgent" -> "فوری"
    else -> value
}

fun supportPriorityColors(value: String): Pair<Color, Color> = when (value) {
    "low" -> AppColors.LockedContainer to AppColors.OnLocked
    "high" -> AppColors.AmberLight to AppColors.Amber
    "urgent" -> AppColors.RoseLight to AppColors.Rose
    else -> AppColors.PurpleContainer to AppColors.PurpleDark
}

fun supportCategoryLabel(value: String): String =
    supportCategoryOptions.firstOrNull { it.first == value }?.second ?: value

val supportCategoryOptions = listOf(
    "account" to "مشکل ورود یا حساب کاربری",
    "payment" to "مشکل پرداخت یا اشتراک",
    "license" to "مشکل لایسنس ربات",
    "scraper" to "مشکل ربات استخراج آگهی",
    "upload" to "مشکل آپلود فایل",
    "filing" to "مشکل فایلینگ و مجموعه‌ها",
    "crm" to "مشکل CRM و مشتریان",
    "feature" to "پیشنهاد یا درخواست قابلیت جدید",
    "other" to "سایر موارد",
)

val supportPriorityOptions = listOf(
    "low" to "کم",
    "normal" to "معمولی",
    "high" to "مهم",
    "urgent" to "فوری",
)

fun messageAuthorLabel(message: ir.divarfiling.mobile.core.network.SupportTicketMessageDto): String =
    when {
        message.isStaffReply -> message.authorName.ifBlank { "پشتیبانی دیوار فایلینگ" }
        message.authorName.isNotBlank() -> message.authorName
        else -> "شما"
    }
