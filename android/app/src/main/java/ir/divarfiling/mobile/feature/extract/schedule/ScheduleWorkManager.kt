package ir.divarfiling.mobile.feature.extract.schedule

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ScheduleWorkManager {
    fun registerPeriodicPolling(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<ExtractScheduleWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(ExtractScheduleWorker.PERIODIC_WORK_NAME)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ExtractScheduleWorker.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun enqueueDueRuns(context: Context, scheduleId: Long? = null) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val input = scheduleId?.let {
            workDataOf(ExtractScheduleWorker.KEY_SCHEDULE_ID to it)
        } ?: workDataOf()
        val request = OneTimeWorkRequestBuilder<ExtractScheduleWorker>()
            .setConstraints(constraints)
            .setInputData(input)
            .build()
        val workName = scheduleId?.let { "extract_schedule_run_$it" } ?: "extract_schedule_run_all"
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
