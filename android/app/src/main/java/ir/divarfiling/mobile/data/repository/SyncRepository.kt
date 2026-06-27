package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.database.ContactCacheDao
import ir.divarfiling.mobile.core.database.CachedContactEntity
import ir.divarfiling.mobile.core.database.CachedDealEntity
import ir.divarfiling.mobile.core.database.CachedPropertyEntity
import ir.divarfiling.mobile.core.database.CachedReminderEntity
import ir.divarfiling.mobile.core.database.DealCacheDao
import ir.divarfiling.mobile.core.database.PropertyCacheDao
import ir.divarfiling.mobile.core.database.ReminderCacheDao
import ir.divarfiling.mobile.core.database.SyncQueueDao
import ir.divarfiling.mobile.core.database.SyncQueueEntity
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.core.network.SyncOperation
import ir.divarfiling.mobile.core.network.SyncPullData
import ir.divarfiling.mobile.core.network.SyncPushRequest
import ir.divarfiling.mobile.core.network.requireData
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class SyncResult(
    val pushedCount: Int = 0,
    val conflictCount: Int = 0,
    val contactsPulled: Int = 0,
    val dealsPulled: Int = 0,
    val propertiesPulled: Int = 0,
    val remindersPulled: Int = 0,
    val serverTime: String? = null,
)

@Singleton
class SyncRepository @Inject constructor(
    private val api: MobileApi,
    private val sessionStore: SessionStore,
    private val contactCache: ContactCacheDao,
    private val dealCache: DealCacheDao,
    private val propertyCache: PropertyCacheDao,
    private val reminderCache: ReminderCacheDao,
    private val syncQueue: SyncQueueDao,
    private val json: Json,
) {
    suspend fun getPendingCount(): Int = syncQueue.getPending().size

    suspend fun syncAll(): ApiResult<SyncResult> {
        return try {
            val pushed = pushPending()
            val since = sessionStore.getLastSyncAt()
            val response = api.syncPull(since = since)
            if (!response.ok) {
                return ApiResult.Error(response.error ?: "خطا در دریافت sync")
            }
            val data = response.requireData<SyncPullData>(json)
            mergePull(data)
            sessionStore.setLastSyncAt(data.serverTime)
            ApiResult.Success(
                SyncResult(
                    pushedCount = pushed.first,
                    conflictCount = pushed.second,
                    contactsPulled = data.contacts?.upserted?.size ?: 0,
                    dealsPulled = data.deals?.upserted?.size ?: 0,
                    propertiesPulled = data.properties?.upserted?.size ?: 0,
                    remindersPulled = data.reminders?.upserted?.size ?: 0,
                    serverTime = data.serverTime,
                ),
            )
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "خطای sync")
        }
    }

    private suspend fun mergePull(data: SyncPullData) {
        data.contacts?.let { batch ->
            if (batch.upserted.isNotEmpty()) {
                contactCache.upsertAll(batch.upserted.map { it.toEntity() })
            }
        }
        data.deals?.let { batch ->
            if (batch.upserted.isNotEmpty()) {
                dealCache.upsertAll(batch.upserted.map { it.toEntity() })
            }
        }
        data.properties?.let { batch ->
            if (batch.upserted.isNotEmpty()) {
                propertyCache.upsertAll(batch.upserted.map { it.toEntity() })
            }
            if (batch.deletedIds.isNotEmpty()) {
                propertyCache.deleteByIds(batch.deletedIds)
            }
        }
        data.reminders?.let { batch ->
            if (batch.upserted.isNotEmpty()) {
                reminderCache.upsertAll(batch.upserted.mapNotNull { it.toEntity() })
            }
        }
    }

    suspend fun pushPending(): Pair<Int, Int> {
        val pending = syncQueue.getPending()
        if (pending.isEmpty()) return 0 to 0
        val operations = pending.map { it.toSyncOperation(json) }
        return try {
            val response = api.syncPush(SyncPushRequest(operations))
            if (!response.ok) return 0 to 0
            val result = response.requireData<ir.divarfiling.mobile.core.network.SyncPushResultData>(json)
            val mappedIds = result.mapped.map { it.opId }.toSet()
            pending.filter { it.opId in mappedIds }.forEach { syncQueue.delete(it.opId) }
            result.conflicts.forEach { conflict ->
                syncQueue.incrementRetry(conflict.opId)
            }
            mappedIds.size to result.conflicts.size
        } catch (_: Exception) {
            0 to 0
        }
    }

    suspend fun enqueue(entity: String, action: String, payload: Map<String, String>) {
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

    private fun DealDto.toEntity() = CachedDealEntity(
        id = id,
        title = title,
        stage = stage,
        amount = amount,
        customerId = customerId,
        updatedAt = updatedAt,
    )

    private fun PropertyDto.toEntity() = CachedPropertyEntity(
        id = id,
        title = title,
        city = city,
        district = district,
        transactionStatus = transactionStatus,
        salePrice = salePrice,
        updatedAt = updatedAt,
    )

    private fun ReminderDto.toEntity(): CachedReminderEntity? {
        val reminderId = id ?: return null
        return CachedReminderEntity(
            id = reminderId,
            title = title,
            contactId = contactId,
            dueAt = dueAt,
            done = done,
        )
    }

    private fun SyncQueueEntity.toSyncOperation(json: Json): SyncOperation {
        val payloadElement = json.parseToJsonElement(payloadJson)
        val payloadObj = payloadElement as? kotlinx.serialization.json.JsonObject
            ?: kotlinx.serialization.json.JsonObject(emptyMap())
        return SyncOperation(
            opId = opId,
            entity = entity,
            action = action,
            payload = payloadObj,
        )
    }
}
