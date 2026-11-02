package ir.divarfiling.mobile.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForumBootstrapDto(
    val member: ForumMemberDto,
    val limits: ForumLimitsDto,
    @SerialName("report_reasons") val reportReasons: List<ForumReportReasonDto> = emptyList(),
    @SerialName("pinned_notices") val pinnedNotices: List<ForumPinnedNoticeDto> = emptyList(),
    @SerialName("pinned_message") val pinnedMessage: ForumMessageDto? = null,
    val unread: Int = 0,
    @SerialName("can_moderate") val canModerate: Boolean = false,
)

@Serializable
data class ForumMemberDto(
    @SerialName("public_id") val publicId: String,
    @SerialName("display_label") val displayLabel: String,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("welcome_seen") val welcomeSeen: Boolean = false,
)

@Serializable
data class ForumLimitsDto(
    @SerialName("max_body") val maxBody: Int = 2000,
)

@Serializable
data class ForumReportReasonDto(
    val id: String,
    val label: String,
)

@Serializable
data class ForumPinnedNoticeDto(
    val body: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class ForumMessageDto(
    val id: Long,
    @SerialName("sender_public_id") val senderPublicId: String,
    @SerialName("sender_label") val senderLabel: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("sender_is_moderator") val senderIsModerator: Boolean = false,
    val body: String = "",
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("is_edited") val isEdited: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("edited_at") val editedAt: String? = null,
    @SerialName("reply_to") val replyTo: ForumReplyPreviewDto? = null,
    val reactions: List<ForumReactionDto> = emptyList(),
    @SerialName("is_mine") val isMine: Boolean = false,
    @SerialName("my_reaction") val myReaction: String = "",
)

@Serializable
data class ForumReplyPreviewDto(
    val id: Long,
    val unavailable: Boolean = false,
    @SerialName("sender_label") val senderLabel: String = "",
    @SerialName("body_preview") val bodyPreview: String? = null,
)

@Serializable
data class ForumReactionDto(
    val emoji: String,
    val count: Int,
)

@Serializable
data class ForumMessagesPageDto(
    val messages: List<ForumMessageDto> = emptyList(),
    @SerialName("next_before_id") val nextBeforeId: Long? = null,
    @SerialName("has_more_after") val hasMoreAfter: Boolean = false,
)

@Serializable
data class ForumSendMessageResponseDto(
    val message: ForumMessageDto,
)

@Serializable
data class ForumEditMessageResponseDto(
    val message: ForumMessageDto,
)

@Serializable
data class ForumSyncDto(
    val messages: List<ForumMessageDto> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_cursor") val nextCursor: Long? = null,
)

@Serializable
data class ForumSearchResultDto(
    val messages: List<ForumMessageDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
)

@Serializable
data class ForumUnreadDto(
    val unread: Int = 0,
)

@Serializable
data class ForumReadResponseDto(
    val unread: Int = 0,
)

@Serializable
data class ForumViewsResponseDto(
    val views: Map<String, Int> = emptyMap(),
)

@Serializable
data class ForumReactionResponseDto(
    @SerialName("message_id") val messageId: Long,
    val reactions: List<ForumReactionDto> = emptyList(),
)

@Serializable
data class ForumPinResponseDto(
    @SerialName("pinned_message") val pinnedMessage: ForumMessageDto? = null,
)

@Serializable
data class ForumProfileResponseDto(
    @SerialName("display_label") val displayLabel: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("welcome_seen") val welcomeSeen: Boolean? = null,
)

@Serializable
data class ForumSendMessageRequest(
    val body: String,
    @SerialName("reply_to_id") val replyToId: Long? = null,
    @SerialName("client_message_id") val clientMessageId: String? = null,
)

@Serializable
data class ForumEditMessageRequest(
    val body: String,
)

@Serializable
data class ForumReactionRequest(
    val emoji: String? = null,
)

@Serializable
data class ForumReportRequest(
    val reason: String,
    val note: String = "",
)

@Serializable
data class ForumPinRequest(
    val pin: Boolean = true,
)

@Serializable
data class ForumReadRequest(
    @SerialName("message_id") val messageId: Long,
)

@Serializable
data class ForumViewsRequest(
    @SerialName("message_ids") val messageIds: List<Long>,
)

@Serializable
data class ForumProfileRequest(
    @SerialName("welcome_ack") val welcomeAck: Boolean? = null,
    @SerialName("display_name") val displayName: String? = null,
)
