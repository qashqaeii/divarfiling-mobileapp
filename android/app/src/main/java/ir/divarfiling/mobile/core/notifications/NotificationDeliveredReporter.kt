package ir.divarfiling.mobile.core.notifications

import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDeliveredReporter @Inject constructor(
    private val notificationRepository: NotificationRepository,
) {
    suspend fun reportReminderDelivered(reminderId: Long) {
        when (notificationRepository.reportLocalReminderDelivered(reminderId)) {
            is ApiResult.Success -> Unit
            is ApiResult.Error -> Unit
        }
    }
}
