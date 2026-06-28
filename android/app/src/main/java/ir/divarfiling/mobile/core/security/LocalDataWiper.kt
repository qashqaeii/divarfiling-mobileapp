package ir.divarfiling.mobile.core.security

import ir.divarfiling.mobile.core.database.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataWiper @Inject constructor(
    private val database: AppDatabase,
) {
    suspend fun wipeUserData() {
        database.contactCacheDao().clear()
        database.datasetCacheDao().clear()
        database.dealCacheDao().clear()
        database.propertyCacheDao().clear()
        database.reminderCacheDao().clear()
        database.dashboardCacheDao().clear()
        database.syncQueueDao().clear()
    }
}
