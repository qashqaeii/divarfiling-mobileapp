package ir.divarfiling.mobile



import android.app.Application

import dagger.hilt.android.HiltAndroidApp

import ir.divarfiling.mobile.core.sync.SyncWorkManager
import ir.divarfiling.mobile.feature.extract.schedule.ScheduleWorkManager

@HiltAndroidApp
class DivarFilingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ScheduleWorkManager.registerPeriodicPolling(this)
        SyncWorkManager.register(this)
    }
}

