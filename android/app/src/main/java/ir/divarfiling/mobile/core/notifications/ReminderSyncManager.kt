package ir.divarfiling.mobile.core.notifications

import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSyncManager @Inject constructor(
    private val crmRepository: CrmRepository,
    private val alarmScheduler: ReminderAlarmScheduler,
    private val localStore: ReminderLocalStore,
) {
    suspend fun rescheduleFromServer() {
        when (val result = crmRepository.getReminders(done = false)) {
            is ApiResult.Success -> applyReminders(result.data)
            is ApiResult.Error -> Unit
        }
    }

    suspend fun rescheduleFromCache() {
        alarmScheduler.rescheduleAllFromCache()
        rescheduleFromServer()
    }

    suspend fun scheduleReminder(dto: ReminderDto) {
        val id = dto.id ?: return
        if (dto.done) {
            cancelReminder(id)
            return
        }
        alarmScheduler.syncFromDto(dto)
        val dueAt = DateUtils.parseInstantMillis(dto.dueAt)
        if (dueAt != null && dueAt > System.currentTimeMillis()) {
            localStore.saveScheduled(id, dueAt)
        } else {
            localStore.remove(id)
        }
    }

    fun cancelReminder(reminderId: Long) {
        alarmScheduler.cancelReminder(reminderId)
        localStore.remove(reminderId)
    }

    suspend fun clearAll() {
        alarmScheduler.cancelAll()
        localStore.clear()
    }

    suspend fun applyReminders(reminders: List<ReminderDto>) {
        val activeIds = reminders.filter { !it.done }.mapNotNull { it.id }.toSet()
        localStore.allScheduled().keys.filter { it !in activeIds }.forEach(::cancelReminder)
        reminders.filter { !it.done }.forEach { scheduleReminder(it) }
    }

    suspend fun handleFcmSchedule(data: Map<String, String>) {
        val reminderId = data["reminder_id"]?.toLongOrNull() ?: return
        if (data["action"] == "cancel_reminder") {
            cancelReminder(reminderId)
            return
        }
        alarmScheduler.scheduleReminder(
            reminderId = reminderId,
            dueAtIso = data["due_at"],
            title = data["title"].orEmpty().ifBlank { "یادآور" },
            body = data["body"].orEmpty(),
            deepLink = data["deep_link"],
            notificationType = data["type"].orEmpty().ifBlank { "reminder_call" },
        )
        DateUtils.parseInstantMillis(data["due_at"])?.let { localStore.saveScheduled(reminderId, it) }
    }
}
