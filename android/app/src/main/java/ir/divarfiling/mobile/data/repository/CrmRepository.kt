package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.database.CachedContactEntity
import ir.divarfiling.mobile.core.database.ContactCacheDao
import ir.divarfiling.mobile.core.database.SyncQueueDao
import ir.divarfiling.mobile.core.database.SyncQueueEntity
import ir.divarfiling.mobile.core.network.ActivityCreateRequest
import ir.divarfiling.mobile.core.network.ActivityDto
import ir.divarfiling.mobile.core.network.ContactDetailData
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.ContactUpdateRequest
import ir.divarfiling.mobile.core.network.LinkListingRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.NoteCreateRequest
import ir.divarfiling.mobile.core.network.PaginatedResult
import ir.divarfiling.mobile.core.network.QuickLeadRequest
import ir.divarfiling.mobile.core.network.ReminderCreateRequest
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.core.network.SyncOperation
import ir.divarfiling.mobile.core.network.SyncPushRequest
import ir.divarfiling.mobile.core.network.TodayActionRequest
import ir.divarfiling.mobile.core.network.TodayData
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrmRepository @Inject constructor(
    private val api: MobileApi,
    private val contactCache: ContactCacheDao,
    private val syncQueue: SyncQueueDao,
    private val json: Json,
) {
    suspend fun getContacts(
        query: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): ApiResult<PaginatedResult<ContactDto>> {
        return try {
            val response = api.getContacts(query = query?.ifBlank { null }, page = page, pageSize = pageSize)
            if (!response.ok) {
                if (page == 1) {
                    val cached = contactCache.getAll().map { it.toDto() }
                    if (cached.isNotEmpty()) {
                        return ApiResult.Success(PaginatedResult(cached, 1, cached.size, false))
                    }
                }
                return ApiResult.Error(response.error ?: "خطا در دریافت مخاطبین")
            }
            val list = response.data?.let {
                json.decodeFromJsonElement(ListSerializer(ContactDto.serializer()), it)
            }.orEmpty()
            if (page == 1) contactCache.upsertAll(list.map { it.toEntity() })
            val total = response.meta?.total ?: list.size
            val hasMore = page * pageSize < total
            ApiResult.Success(PaginatedResult(list, page, total, hasMore))
        } catch (e: Exception) {
            if (page == 1) {
                val cached = contactCache.getAll().map { it.toDto() }
                if (cached.isNotEmpty()) {
                    return ApiResult.Success(PaginatedResult(cached, 1, cached.size, false))
                }
            }
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getContactDetail(contactId: Long): ApiResult<ContactDetailData> {
        return try {
            val response = api.getContact(contactId)
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در دریافت مخاطب")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun quickLead(fullName: String, phone: String): ApiResult<ContactDto> {
        return try {
            val response = api.quickLead(QuickLeadRequest(fullName, phone))
            if (!response.ok) return ApiResult.Error(response.error ?: "ثبت سرنخ ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun createActivity(
        contactId: Long,
        type: String,
        content: String,
        title: String = "",
    ): ApiResult<ActivityDto> {
        val request = ActivityCreateRequest(activityType = type, content = content, title = title)
        return try {
            val response = api.createActivity(contactId, request)
            if (!response.ok) {
                enqueueSync("activity", "create", mapOf(
                    "contact_id" to contactId.toString(),
                    "activity_type" to type,
                    "content" to content,
                    "title" to title,
                ))
                return ApiResult.Error(response.error ?: "ثبت فعالیت ناموفق — در صف آفلاین")
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            enqueueSync("activity", "create", mapOf(
                "contact_id" to contactId.toString(),
                "activity_type" to type,
                "content" to content,
                "title" to title,
            ))
            ApiResult.Error(e.message ?: "خطای شبکه — در صف آفلاین")
        }
    }

    suspend fun createNote(contactId: Long, content: String): ApiResult<ActivityDto> {
        return try {
            val response = api.createNote(contactId, NoteCreateRequest(content))
            if (!response.ok) {
                enqueueSync("note", "create", mapOf(
                    "contact_id" to contactId.toString(),
                    "content" to content,
                ))
                return ApiResult.Error(response.error ?: "ثبت یادداشت ناموفق")
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            enqueueSync("note", "create", mapOf("contact_id" to contactId.toString(), "content" to content))
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun createReminder(
        contactId: Long,
        title: String,
        dueAt: String,
        note: String = "",
    ): ApiResult<ReminderDto> {
        return try {
            val response = api.createReminder(contactId, ReminderCreateRequest(title, dueAt, note))
            if (!response.ok) return ApiResult.Error(response.error ?: "ثبت یادآور ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun linkListing(contactId: Long, request: LinkListingRequest): ApiResult<Unit> {
        return try {
            val response = api.linkListing(contactId, request)
            if (!response.ok) return ApiResult.Error(response.error ?: "لینک آگهی ناموفق")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun updateContact(contactId: Long, request: ContactUpdateRequest): ApiResult<ContactDto> {
        return try {
            val response = api.updateContact(contactId, request)
            if (!response.ok) return ApiResult.Error(response.error ?: "ویرایش ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun getToday(): ApiResult<TodayData> {
        return try {
            val response = api.getToday()
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در دریافت کارهای امروز")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun completeTodayTask(
        contactId: Long? = null,
        reminderId: Long? = null,
        note: String = "",
    ): ApiResult<Unit> {
        val request = TodayActionRequest(
            contactId = contactId,
            reminderId = reminderId,
            action = "complete",
            note = note,
        )
        return try {
            val response = api.todayAction(request)
            if (!response.ok) {
                if (reminderId != null) {
                    enqueueSync("reminder", "complete", mapOf(
                        "reminder_id" to reminderId.toString(),
                        "note" to note,
                    ))
                }
                return ApiResult.Error(response.error ?: "عملیات ناموفق")
            }
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            if (reminderId != null) {
                enqueueSync("reminder", "complete", mapOf("reminder_id" to reminderId, "note" to note))
            }
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun postponeTodayTask(
        contactId: Long? = null,
        reminderId: Long? = null,
        days: Int = 1,
    ): ApiResult<Unit> {
        val request = TodayActionRequest(
            contactId = contactId,
            reminderId = reminderId,
            action = "postpone",
            days = days,
        )
        return try {
            val response = api.todayAction(request)
            if (!response.ok) return ApiResult.Error(response.error ?: "تعویق ناموفق")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun flushSyncQueue(): Int {
        val pending = syncQueue.getPending()
        if (pending.isEmpty()) return 0
        val operations = pending.map { item ->
            val payloadElement = json.parseToJsonElement(item.payloadJson)
            val payloadObj = payloadElement as? kotlinx.serialization.json.JsonObject
                ?: kotlinx.serialization.json.JsonObject(emptyMap())
            SyncOperation(
                opId = item.opId,
                entity = item.entity,
                action = item.action,
                payload = payloadObj,
            )
        }
        return try {
            val response = api.syncPush(SyncPushRequest(operations))
            if (response.ok) {
                pending.forEach { syncQueue.delete(it.opId) }
                pending.size
            } else 0
        } catch (_: Exception) {
            0
        }
    }

    private suspend fun enqueueSync(entity: String, action: String, payload: Map<String, String>) {
        val jsonPayload = json.encodeToString(
            kotlinx.serialization.serializer<Map<String, String>>(),
            payload,
        )
        syncQueue.insert(
            SyncQueueEntity(
                opId = UUID.randomUUID().toString(),
                entity = entity,
                action = action,
                payloadJson = jsonPayload,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    private fun ContactDto.toEntity() = CachedContactEntity(
        id = id,
        fullName = fullName,
        phone = phone,
        customerType = customerType,
        status = status,
        updatedAt = updatedAt,
    )

    private fun CachedContactEntity.toDto() = ContactDto(
        id = id,
        fullName = fullName,
        phone = phone,
        customerType = customerType,
        status = status,
        updatedAt = updatedAt,
    )
}
