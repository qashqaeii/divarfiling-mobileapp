package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.ContactTeamAssignRequest
import ir.divarfiling.mobile.core.network.ContactTeamTransferRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.TeamActionRequest
import ir.divarfiling.mobile.core.network.TeamAnnouncementDetailPayload
import ir.divarfiling.mobile.core.network.TeamAnnouncementDto
import ir.divarfiling.mobile.core.network.TeamAnnouncementsPayload
import ir.divarfiling.mobile.core.network.TeamAssignLeadsRequest
import ir.divarfiling.mobile.core.network.TeamLeadsPayload
import ir.divarfiling.mobile.core.network.TeamMembersPayload
import ir.divarfiling.mobile.core.network.TeamOverviewDto
import ir.divarfiling.mobile.core.network.TeamPanelNotificationsPayload
import ir.divarfiling.mobile.core.network.TeamReplyRequest
import ir.divarfiling.mobile.core.network.TeamSendMessageRequest
import ir.divarfiling.mobile.core.network.TeamThreadDetailDto
import ir.divarfiling.mobile.core.network.TeamThreadStateRequest
import ir.divarfiling.mobile.core.network.TeamThreadsPayload
import ir.divarfiling.mobile.core.network.TeamUnreadDto
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toUserMessage
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    suspend fun getOverview(): ApiResult<TeamOverviewDto> = single { api.getTeamOverview() }

    suspend fun getMembers(excludeSelf: Boolean = false): ApiResult<TeamMembersPayload> =
        single { api.getTeamMembers(excludeSelf = if (excludeSelf) 1 else null) }

    suspend fun getUnread(): ApiResult<TeamUnreadDto> = single { api.getTeamUnread() }

    suspend fun getMessages(folder: String = "inbox", page: Int = 1): ApiResult<TeamThreadsPayload> =
        single { api.getTeamMessages(folder = folder, page = page) }

    suspend fun sendMessage(request: TeamSendMessageRequest): ApiResult<TeamThreadDetailDto> =
        single { api.sendTeamMessage(request) }

    suspend fun getThread(threadId: Long): ApiResult<TeamThreadDetailDto> =
        single { api.getTeamThread(threadId) }

    suspend fun reply(threadId: Long, body: String): ApiResult<TeamThreadDetailDto> =
        single { api.replyTeamThread(threadId, TeamReplyRequest(body = body)) }

    suspend fun patchThread(
        threadId: Long,
        isStarred: Boolean? = null,
        isArchived: Boolean? = null,
    ): ApiResult<Unit> = try {
        val response = api.patchTeamThread(
            threadId,
            TeamThreadStateRequest(isStarred = isStarred, isArchived = isArchived),
        )
        if (!response.ok) ApiResult.Error(response.error ?: "به‌روزرسانی ناموفق")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun getAnnouncements(
        unreadOnly: Boolean = false,
        importantOnly: Boolean = false,
    ): ApiResult<TeamAnnouncementsPayload> = single {
        api.getTeamAnnouncements(
            unread = if (unreadOnly) 1 else null,
            important = if (importantOnly) 1 else null,
        )
    }

    suspend fun getAnnouncement(id: Long): ApiResult<TeamAnnouncementDto> = try {
        val response = api.getTeamAnnouncement(id)
        if (!response.ok) ApiResult.Error(response.error ?: "اطلاعیه یافت نشد")
        else {
            val payload = response.requireData<TeamAnnouncementDetailPayload>(json)
            val item = payload.announcement
            if (item == null) ApiResult.Error("اطلاعیه یافت نشد")
            else ApiResult.Success(item)
        }
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun markAnnouncementRead(id: Long): ApiResult<Unit> = try {
        val response = api.markTeamAnnouncementRead(id)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun getPanelNotifications(unreadOnly: Boolean = false): ApiResult<TeamPanelNotificationsPayload> =
        single { api.getTeamPanelNotifications(unread = if (unreadOnly) 1 else null) }

    suspend fun markPanelReadAll(): ApiResult<Unit> = try {
        val response = api.markTeamPanelNotifications(TeamActionRequest(action = "read_all"))
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun markPanelRead(notificationId: Long): ApiResult<Unit> = try {
        val response = api.markTeamPanelNotifications(
            TeamActionRequest(action = "read", notificationId = notificationId),
        )
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun getLeadInbox(): ApiResult<TeamLeadsPayload> = single { api.getTeamLeadInbox() }

    suspend fun assignLeads(memberId: Long, customerIds: List<Long>): ApiResult<Unit> = try {
        val response = api.assignTeamLeads(TeamAssignLeadsRequest(memberId, customerIds))
        if (!response.ok) ApiResult.Error(response.error ?: "تخصیص ناموفق")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun assignContact(contactId: Long, memberId: Long): ApiResult<Unit> = try {
        val response = api.assignContact(contactId, ContactTeamAssignRequest(memberId))
        if (!response.ok) ApiResult.Error(response.error ?: "تخصیص ناموفق")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun transferContact(contactId: Long, memberId: Long, note: String?): ApiResult<Unit> = try {
        val response = api.transferContact(contactId, ContactTeamTransferRequest(memberId, note))
        if (!response.ok) ApiResult.Error(response.error ?: "انتقال ناموفق")
        else ApiResult.Success(Unit)
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    private suspend inline fun <reified T> single(
        crossinline call: suspend () -> ir.divarfiling.mobile.core.network.ApiEnvelope,
    ): ApiResult<T> = try {
        val response = call()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا")
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }
}
