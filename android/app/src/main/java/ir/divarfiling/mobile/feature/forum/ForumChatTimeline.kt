package ir.divarfiling.mobile.feature.forum

import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.network.ForumMessageDto
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

sealed interface ForumChatListItem {
    val stableKey: String

    data class DaySeparator(val label: String, override val stableKey: String) : ForumChatListItem

    data class Message(
        val message: ForumMessageDto,
        val showSenderName: Boolean,
        val showAvatar: Boolean,
        val isContextTarget: Boolean,
        override val stableKey: String = "m-${message.id}",
    ) : ForumChatListItem
}

object ForumChatTimeline {

    private val zone: ZoneId = ZoneId.of("Asia/Tehran")

    fun build(messages: List<ForumMessageDto>, contextMessageId: Long?): List<ForumChatListItem> {
        if (messages.isEmpty()) return emptyList()
        val out = ArrayList<ForumChatListItem>(messages.size + 8)
        var lastDayKey: String? = null
        messages.forEachIndexed { index, message ->
            val dayKey = dayKey(message.createdAt)
            if (dayKey != null && dayKey != lastDayKey) {
                out.add(
                    ForumChatListItem.DaySeparator(
                        label = dayLabel(message.createdAt),
                        stableKey = "d-$dayKey",
                    ),
                )
                lastDayKey = dayKey
            }
            val prev = messages.getOrNull(index - 1)
            val next = messages.getOrNull(index + 1)
            val clusterWithPrev = prev != null && sameCluster(prev, message)
            val clusterWithNext = next != null && sameCluster(message, next)
            val showSender = !message.isMine && !message.isDeleted && !clusterWithPrev
            val showAvatar = !message.isMine && !message.isDeleted && !clusterWithNext
            out.add(
                ForumChatListItem.Message(
                    message = message,
                    showSenderName = showSender,
                    showAvatar = showAvatar,
                    isContextTarget = contextMessageId == message.id,
                ),
            )
        }
        return out
    }

    private fun sameCluster(a: ForumMessageDto, b: ForumMessageDto): Boolean {
        if (a.senderPublicId != b.senderPublicId) return false
        if (a.isDeleted || b.isDeleted) return false
        val ta = parseInstant(a.createdAt) ?: return false
        val tb = parseInstant(b.createdAt) ?: return false
        return ChronoUnit.MINUTES.between(ta, tb) <= 12
    }

    private fun dayKey(raw: String): String? {
        val instant = parseInstant(raw) ?: return null
        val local = instant.atZone(zone).toLocalDate()
        return local.toString()
    }

    fun dayLabel(raw: String): String {
        val instant = parseInstant(raw) ?: return "—"
        val now = Instant.now().atZone(zone).toLocalDate()
        val then = instant.atZone(zone).toLocalDate()
        val days = ChronoUnit.DAYS.between(then, now)
        return when {
            days == 0L -> "امروز"
            days == 1L -> "دیروز"
            else -> DateUtils.formatJalaliDate(raw) ?: DateUtils.formatRelativeTimeAgo(raw)
        }
    }

    fun messageClock(raw: String): String {
        val instant = parseInstant(raw) ?: return "—"
        val millis = instant.toEpochMilli()
        return DateUtils.fromPersianDigits(DateUtils.formatTimeFromMillis(millis, zone))
    }

    private fun parseInstant(raw: String): Instant? =
        runCatching { Instant.parse(raw) }.getOrNull()
}
