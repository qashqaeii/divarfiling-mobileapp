package ir.divarfiling.mobile.core.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import android.content.Context
import androidx.room.Room

@Entity(tableName = "cached_contacts")
data class CachedContactEntity(
    @PrimaryKey val id: Long,
    val fullName: String,
    val phone: String?,
    val customerType: String?,
    val status: String?,
    val updatedAt: String?,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cached_datasets")
data class CachedDatasetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val itemCount: Int,
    val city: String?,
    val district: String?,
    val createdAt: String?,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cached_dashboard")
data class CachedDashboardEntity(
    @PrimaryKey val id: Int = 1,
    val payload: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val opId: String,
    val entity: String,
    val action: String,
    val payloadJson: String,
    val createdAt: Long,
    val retryCount: Int = 0,
)

@Dao
interface ContactCacheDao {
    @Query("SELECT * FROM cached_contacts ORDER BY fullName")
    suspend fun getAll(): List<CachedContactEntity>

    @Upsert
    suspend fun upsertAll(items: List<CachedContactEntity>)

    @Query("DELETE FROM cached_contacts")
    suspend fun clear()
}

@Dao
interface DatasetCacheDao {
    @Query("SELECT * FROM cached_datasets ORDER BY cachedAt DESC")
    suspend fun getAll(): List<CachedDatasetEntity>

    @Upsert
    suspend fun upsertAll(items: List<CachedDatasetEntity>)

    @Query("DELETE FROM cached_datasets")
    suspend fun clear()
}

@Dao
interface DashboardCacheDao {
    @Query("SELECT * FROM cached_dashboard WHERE id = 1 LIMIT 1")
    suspend fun getLatest(): CachedDashboardEntity?

    @Upsert
    suspend fun upsert(entity: CachedDashboardEntity)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC LIMIT 50")
    suspend fun getPending(): List<SyncQueueEntity>

    @Upsert
    suspend fun insert(entity: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE opId = :opId")
    suspend fun delete(opId: String)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE opId = :opId")
    suspend fun incrementRetry(opId: String)
}

@Database(
    entities = [
        CachedContactEntity::class,
        CachedDatasetEntity::class,
        CachedDashboardEntity::class,
        SyncQueueEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun contactCacheDao(): ContactCacheDao
    abstract fun datasetCacheDao(): DatasetCacheDao
    abstract fun dashboardCacheDao(): DashboardCacheDao
    abstract fun syncQueueDao(): SyncQueueDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "divar_filing_cache.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideContactCacheDao(db: AppDatabase): ContactCacheDao = db.contactCacheDao()

    @Provides
    fun provideDatasetCacheDao(db: AppDatabase): DatasetCacheDao = db.datasetCacheDao()

    @Provides
    fun provideDashboardCacheDao(db: AppDatabase): DashboardCacheDao = db.dashboardCacheDao()

    @Provides
    fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
}
