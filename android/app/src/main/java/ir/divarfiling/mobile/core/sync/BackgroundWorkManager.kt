package ir.divarfiling.mobile.core.sync

import android.content.Context
import androidx.work.WorkManager
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.feature.extract.schedule.ExtractScheduleWorker
import ir.divarfiling.mobile.feature.extract.schedule.ScheduleWorkManager

object BackgroundWorkManager {
    fun register(context: Context) {
        SyncWorkManager.register(context)
        ScheduleWorkManager.registerPeriodicPolling(context)
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        workManager.cancelUniqueWork(ExtractScheduleWorker.PERIODIC_WORK_NAME)
    }

    suspend fun syncWithSession(context: Context, sessionStore: SessionStore) {
        if (sessionStore.hasValidSession()) {
            register(context)
        } else {
            cancel(context)
        }
    }
}
