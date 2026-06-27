package ir.divarfiling.mobile.feature.extract.schedule

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import ir.divarfiling.mobile.core.sync.WorkerSessionEntryPoint
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ExtractionRepository
import ir.divarfiling.mobile.data.repository.ExtractionScheduleRepository

class ExtractScheduleWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sessionEntry = EntryPointAccessors.fromApplication(
            applicationContext,
            WorkerSessionEntryPoint::class.java,
        )
        if (!sessionEntry.sessionStore().hasValidSession()) {
            return Result.success()
        }
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ExtractScheduleWorkerEntryPoint::class.java,
        )
        val scheduleRepository = entryPoint.scheduleRepository()
        val extractionRepository = entryPoint.extractionRepository()

        val explicitScheduleId = inputData.getLong(KEY_SCHEDULE_ID, -1L).takeIf { it > 0 }
        val scheduleIds = if (explicitScheduleId != null) {
            listOf(explicitScheduleId)
        } else {
            when (val due = scheduleRepository.getDueSchedules()) {
                is ApiResult.Success -> due.data.map { it.id }
                is ApiResult.Error -> return Result.retry()
            }
        }

        if (scheduleIds.isEmpty()) return Result.success()

        var hadFailure = false
        for (scheduleId in scheduleIds) {
            when (val start = scheduleRepository.startRun(scheduleId)) {
                is ApiResult.Success -> {
                    val filters = ExtractScheduleMapper.toExtractFilters(start.data.filters)
                    when (
                        val result = extractionRepository.runLightExtraction(
                            filters = filters,
                            onProgress = { _, _ -> },
                            isCancelled = { false },
                            runId = start.data.run.id,
                            scheduleId = scheduleId,
                        )
                    ) {
                        is ApiResult.Success -> Unit
                        is ApiResult.Error -> {
                            hadFailure = true
                            scheduleRepository.failRun(start.data.run.id, result.message)
                        }
                    }
                }
                is ApiResult.Error -> hadFailure = true
            }
        }
        return if (hadFailure) Result.retry() else Result.success()
    }

    companion object {
        const val KEY_SCHEDULE_ID = "schedule_id"
        const val PERIODIC_WORK_NAME = "extract_schedule_poll"
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ExtractScheduleWorkerEntryPoint {
    fun scheduleRepository(): ExtractionScheduleRepository
    fun extractionRepository(): ExtractionRepository
}
