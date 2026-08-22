package ir.divarfiling.mobile.core.notifications

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.divarfiling.mobile.data.repository.NotificationRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun reminderAlarmScheduler(): ReminderAlarmScheduler
    fun notificationRepository(): NotificationRepository
    fun dedupStore(): NotificationDedupStore
}
