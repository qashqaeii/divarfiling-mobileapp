package ir.divarfiling.mobile.feature.forum

import ir.divarfiling.mobile.core.network.ForumMessageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ForumChatTimelineTest {

    @Test
    fun clustersConsecutiveMessagesFromSameSender() {
        val messages = listOf(
            msg(1, "a", isMine = false),
            msg(2, "a", isMine = false),
        )
        val items = ForumChatTimeline.build(messages, contextMessageId = null)
        val messageItems = items.filterIsInstance<ForumChatListItem.Message>()
        assertEquals(2, messageItems.size)
        assertTrue(messageItems[0].showSenderName)
        assertTrue(!messageItems[1].showSenderName)
        assertTrue(!messageItems[0].showAvatar)
        assertTrue(messageItems[1].showAvatar)
    }

    @Test
    fun insertsDaySeparatorBetweenDays() {
        val messages = listOf(
            msg(1, "a", createdAt = "2026-01-01T10:00:00Z"),
            msg(2, "b", createdAt = "2026-01-02T10:00:00Z"),
        )
        val items = ForumChatTimeline.build(messages, null)
        assertTrue(items.any { it is ForumChatListItem.DaySeparator })
    }

    private fun msg(
        id: Long,
        sender: String,
        isMine: Boolean = true,
        createdAt: String = "2026-03-04T12:00:00Z",
    ) = ForumMessageDto(
        id = id,
        senderPublicId = sender,
        senderLabel = sender,
        createdAt = createdAt,
        isMine = isMine,
    )
}
