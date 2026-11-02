package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.ForumBootstrapDto
import ir.divarfiling.mobile.core.network.ForumEditMessageRequest
import ir.divarfiling.mobile.core.network.ForumEditMessageResponseDto
import ir.divarfiling.mobile.core.network.ForumMessagesPageDto
import ir.divarfiling.mobile.core.network.ForumPinRequest
import ir.divarfiling.mobile.core.network.ForumPinResponseDto
import ir.divarfiling.mobile.core.network.ForumProfileRequest
import ir.divarfiling.mobile.core.network.ForumProfileResponseDto
import ir.divarfiling.mobile.core.network.ForumReactionRequest
import ir.divarfiling.mobile.core.network.ForumReactionResponseDto
import ir.divarfiling.mobile.core.network.ForumReadRequest
import ir.divarfiling.mobile.core.network.ForumReadResponseDto
import ir.divarfiling.mobile.core.network.ForumReportRequest
import ir.divarfiling.mobile.core.network.ForumSearchResultDto
import ir.divarfiling.mobile.core.network.ForumSendMessageRequest
import ir.divarfiling.mobile.core.network.ForumSendMessageResponseDto
import ir.divarfiling.mobile.core.network.ForumSyncDto
import ir.divarfiling.mobile.core.network.ForumUnreadDto
import ir.divarfiling.mobile.core.network.ForumViewsRequest
import ir.divarfiling.mobile.core.network.ForumViewsResponseDto
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.mapApiError
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toApiFailure
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForumRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    @Volatile
    var cachedUnreadCount: Int = 0

    suspend fun bootstrap(): ApiResult<ForumBootstrapDto> = envelope { api.forumBootstrap() }

    suspend fun loadMessages(
        beforeId: Long? = null,
        afterId: Long? = null,
    ): ApiResult<ForumMessagesPageDto> = envelope { api.forumMessages(beforeId = beforeId, afterId = afterId) }

    suspend fun sendMessage(request: ForumSendMessageRequest): ApiResult<ForumSendMessageResponseDto> =
        envelope { api.forumSendMessage(request) }

    suspend fun editMessage(messageId: Long, body: String): ApiResult<ForumEditMessageResponseDto> =
        envelope { api.forumEditMessage(messageId, ForumEditMessageRequest(body)) }

    suspend fun deleteMessage(messageId: Long): ApiResult<Unit> = envelopeUnit { api.forumDeleteMessage(messageId) }

    suspend fun setReaction(messageId: Long, emoji: String?): ApiResult<ForumReactionResponseDto> =
        envelope { api.forumSetReaction(messageId, ForumReactionRequest(emoji = emoji)) }

    suspend fun reportMessage(messageId: Long, reason: String, note: String): ApiResult<Unit> =
        envelopeUnit { api.forumReportMessage(messageId, ForumReportRequest(reason, note)) }

    suspend fun pinMessage(messageId: Long, pin: Boolean): ApiResult<ForumPinResponseDto> =
        envelope { api.forumPinMessage(messageId, ForumPinRequest(pin)) }

    suspend fun updateRead(messageId: Long): ApiResult<ForumReadResponseDto> {
        val result = envelope<ForumReadResponseDto> {
            api.forumUpdateRead(ForumReadRequest(messageId))
        }
        if (result is ApiResult.Success) cachedUnreadCount = result.data.unread
        return result
    }

    suspend fun recordViews(messageIds: List<Long>): ApiResult<ForumViewsResponseDto> =
        envelope { api.forumRecordViews(ForumViewsRequest(messageIds)) }

    suspend fun search(query: String, page: Int): ApiResult<ForumSearchResultDto> =
        envelope { api.forumSearch(query, page) }

    suspend fun updateProfile(request: ForumProfileRequest): ApiResult<ForumProfileResponseDto> =
        envelope { api.forumUpdateProfile(request) }

    suspend fun unread(): ApiResult<ForumUnreadDto> {
        val result = envelope<ForumUnreadDto> { api.forumUnread() }
        if (result is ApiResult.Success) cachedUnreadCount = result.data.unread
        return result
    }

    suspend fun syncChanges(since: String, anchorId: Long, cursor: Long = 0): ApiResult<ForumSyncDto> =
        envelope { api.forumSyncChanges(since = since, anchorId = anchorId, cursor = cursor) }

    private suspend inline fun <reified T> envelope(
        crossinline call: suspend () -> ir.divarfiling.mobile.core.network.ApiEnvelope,
    ): ApiResult<T> = try {
        val response = call()
        if (!response.ok) {
            ApiResult.Error(
                mapForumApiError(response.code, response.error, null),
                response.code,
            )
        } else {
            ApiResult.Success(response.requireData(json))
        }
    } catch (e: Exception) {
        ApiResult.Error(forumFailureMessage(e), forumFailureCode(e))
    }

    private suspend inline fun envelopeUnit(
        crossinline call: suspend () -> ir.divarfiling.mobile.core.network.ApiEnvelope,
    ): ApiResult<Unit> = try {
        val response = call()
        if (!response.ok) {
            ApiResult.Error(
                mapForumApiError(response.code, response.error, null),
                response.code,
            )
        } else {
            ApiResult.Success(Unit)
        }
    } catch (e: Exception) {
        ApiResult.Error(forumFailureMessage(e), forumFailureCode(e))
    }

    private fun forumFailureMessage(e: Exception): String {
        val failure = e.toApiFailure("خطای شبکه")
        return mapForumApiError(failure.code, failure.message, failure.httpCode)
    }

    private fun forumFailureCode(e: Exception): String? = e.toApiFailure("").code

    companion object {
        fun mapForumApiError(code: String?, serverMessage: String?, httpCode: Int?): String {
            val fromCode = when (code) {
                "NOT_FOUND", "not_found" -> serverMessage?.takeIf { it.isNotBlank() }
                    ?: "تالار گفتگو فعال نیست یا دسترسی ندارید."
                "banned" -> serverMessage ?: "ارسال پیام برای شما محدود شده است."
                "banned_read" -> serverMessage ?: "دسترسی شما به تالار محدود شده است."
                "forbidden" -> serverMessage ?: "دسترسی به این عملیات مجاز نیست."
                "duplicate" -> serverMessage ?: "این درخواست قبلاً ثبت شده است."
                "edit_window" -> serverMessage ?: "مهلت ویرایش پیام به پایان رسیده است."
                "rate_interval", "rate_minute", "rate_hour", "rate_unavailable" ->
                    serverMessage ?: "محدودیت ارسال. کمی بعد دوباره تلاش کنید."
                "pii_phone", "pii_email", "pii_card", "pii_url" ->
                    serverMessage ?: "متن پیام مجاز نیست."
                "reply_invalid" -> serverMessage ?: "پیام مورد پاسخ در دسترس نیست."
                "query" -> serverMessage ?: "عبارت جستجو خیلی کوتاه است."
                else -> null
            }
            if (!fromCode.isNullOrBlank()) return fromCode
            return mapApiError(code, serverMessage, httpCode, serverMessage ?: "خطا در ارتباط با تالار")
        }
    }
}
