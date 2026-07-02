package ir.divarfiling.mobile.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.database.CachedContactEntity
import ir.divarfiling.mobile.core.database.ContactCacheDao
import ir.divarfiling.mobile.core.database.SyncQueueDao
import ir.divarfiling.mobile.core.database.SyncQueueEntity
import ir.divarfiling.mobile.core.network.ActivityCreateRequest
import ir.divarfiling.mobile.core.network.ActivityDto
import ir.divarfiling.mobile.core.network.ContactDetailData
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.ContactMatchesData
import ir.divarfiling.mobile.core.network.ContactSuggestRequest
import ir.divarfiling.mobile.core.network.ContactSuggestResponse
import ir.divarfiling.mobile.core.network.ContactUpdateRequest
import ir.divarfiling.mobile.core.network.CustomerDocumentDto
import ir.divarfiling.mobile.core.network.LinkListingRequest
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.NoteCreateRequest
import ir.divarfiling.mobile.core.network.PaginatedResult
import ir.divarfiling.mobile.core.network.PropertyMatchDto
import ir.divarfiling.mobile.core.network.QuickLeadRequest
import ir.divarfiling.mobile.core.network.ReminderCreateRequest
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.core.network.SendListingRequest
import ir.divarfiling.mobile.core.network.SyncOperation
import ir.divarfiling.mobile.core.network.SyncPushRequest
import ir.divarfiling.mobile.core.network.TodayActionRequest
import ir.divarfiling.mobile.core.network.TodayData
import ir.divarfiling.mobile.core.network.parseData
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrmRepository @Inject constructor(
    @ApplicationContext private val context: Context,
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

    suspend fun getContactMatches(contactId: Long): ApiResult<ContactMatchesData> {
        return try {
            val response = api.getContactMatches(contactId)
            if (!response.ok) return ApiResult.Error(response.error ?: "خطا در دریافت پیشنهادها")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun suggestContactMatches(
        contactId: Long,
        matches: List<PropertyMatchDto>,
        note: String? = null,
    ): ApiResult<ContactSuggestResponse> {
        return try {
            val response = api.suggestContactMatches(
                contactId,
                ContactSuggestRequest(matches = matches, note = note),
            )
            if (!response.ok) return ApiResult.Error(response.error ?: "ثبت پیشنهاد ناموفق")
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

    suspend fun sendListing(contactId: Long, request: SendListingRequest): ApiResult<Unit> {
        return try {
            val response = api.sendListing(contactId, request)
            if (!response.ok) return ApiResult.Error(response.error ?: "ارسال فایل ناموفق")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun uploadDocument(
        contactId: Long,
        uri: Uri,
        title: String = "",
        docType: String = "",
        note: String = "",
    ): ApiResult<CustomerDocumentDto> {
        return try {
            val resolver = context.contentResolver
            val mime = resolver.getType(uri) ?: "application/octet-stream"
            val fileName = title.ifBlank {
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
                } ?: "document"
            }
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return ApiResult.Error("خواندن فایل ناموفق بود")
            val requestFile = bytes.toRequestBody(mime.toMediaType())
            val filePart = MultipartBody.Part.createFormData("file", fileName, requestFile)
            val titleBody = fileName.toRequestBody("text/plain".toMediaType())
            val docTypeBody = docType.toRequestBody("text/plain".toMediaType()).takeIf { docType.isNotBlank() }
            val noteBody = note.toRequestBody("text/plain".toMediaType()).takeIf { note.isNotBlank() }
            val response = api.uploadContactDocument(
                contactId = contactId,
                title = titleBody,
                file = filePart,
                docType = docTypeBody,
                note = noteBody,
            )
            if (!response.ok) return ApiResult.Error(response.error ?: "آپلود مدرک ناموفق")
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun deleteDocument(contactId: Long, documentId: Long): ApiResult<Unit> {
        return try {
            val response = api.deleteContactDocument(contactId, documentId)
            if (!response.ok) return ApiResult.Error(response.error ?: "حذف مدرک ناموفق")
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای شبکه")
        }
    }

    suspend fun updateContact(contactId: Long, request: ContactUpdateRequest): ApiResult<ContactDto> {
        val payload = buildMap {
            put("contact_id", contactId.toString())
            request.fullName?.let { put("full_name", it) }
            request.phone?.let { put("phone", it) }
            request.status?.let { put("status", it) }
            request.customerType?.let { put("customer_type", it) }
            request.priority?.let { put("priority", it) }
            request.notes?.let { put("notes", it) }
            request.budget?.let { put("budget", it.toString()) }
        }
        return try {
            val response = api.updateContact(contactId, request)
            if (!response.ok) {
                enqueueSync("contact", "update", payload)
                return ApiResult.Error(response.error ?: "ویرایش ناموفق — در صف آفلاین")
            }
            ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            enqueueSync("contact", "update", payload)
            ApiResult.Error(e.message ?: "خطای شبکه — در صف آفلاین")
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
                enqueueSync("reminder", "complete", mapOf(
                    "reminder_id" to reminderId.toString(),
                    "note" to note,
                ))
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
