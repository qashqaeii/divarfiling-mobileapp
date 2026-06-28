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

@Entity(tableName = "cached_deals")
data class CachedDealEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val stage: String?,
    val amount: Long?,
    val customerId: Long?,
    val updatedAt: String?,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cached_properties")
data class CachedPropertyEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val city: String?,
    val district: String?,
    val transactionStatus: String?,
    val salePrice: Long?,
    val updatedAt: String?,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "cached_reminders")
data class CachedReminderEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val contactId: Long?,
    val dueAt: String?,
    val done: Boolean,
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
interface DealCacheDao {
    @Query("SELECT * FROM cached_deals ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CachedDealEntity>

    @Upsert
    suspend fun upsertAll(items: List<CachedDealEntity>)

    @Query("DELETE FROM cached_deals")
    suspend fun clear()

    @Query("DELETE FROM cached_deals WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}

@Dao
interface PropertyCacheDao {
    @Query("SELECT * FROM cached_properties ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CachedPropertyEntity>

    @Upsert
    suspend fun upsertAll(items: List<CachedPropertyEntity>)

    @Query("DELETE FROM cached_properties")
    suspend fun clear()

    @Query("DELETE FROM cached_properties WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}

@Dao
interface ReminderCacheDao {
    @Query("SELECT * FROM cached_reminders WHERE done = 0 ORDER BY dueAt")
    suspend fun getActive(): List<CachedReminderEntity>

    @Upsert
    suspend fun upsertAll(items: List<CachedReminderEntity>)

    @Query("DELETE FROM cached_reminders")
    suspend fun clear()

    @Query("DELETE FROM cached_reminders WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}

@Dao
interface DashboardCacheDao {
    @Query("SELECT * FROM cached_dashboard WHERE id = 1 LIMIT 1")
    suspend fun getLatest(): CachedDashboardEntity?

    @Upsert
    suspend fun upsert(entity: CachedDashboardEntity)

    @Query("DELETE FROM cached_dashboard")
    suspend fun clear()
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

    @Query("DELETE FROM sync_queue")
    suspend fun clear()
}

@Database(
    entities = [
        CachedContactEntity::class,
        CachedDealEntity::class,
        CachedPropertyEntity::class,
        CachedReminderEntity::class,
        CachedDatasetEntity::class,
        CachedDashboardEntity::class,
        SyncQueueEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun contactCacheDao(): ContactCacheDao
    abstract fun dealCacheDao(): DealCacheDao
    abstract fun propertyCacheDao(): PropertyCacheDao
    abstract fun reminderCacheDao(): ReminderCacheDao
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
    fun provideDealCacheDao(db: AppDatabase): DealCacheDao = db.dealCacheDao()

    @Provides
    fun providePropertyCacheDao(db: AppDatabase): PropertyCacheDao = db.propertyCacheDao()

    @Provides
    fun provideReminderCacheDao(db: AppDatabase): ReminderCacheDao = db.reminderCacheDao()

    @Provides
    fun provideDatasetCacheDao(db: AppDatabase): DatasetCacheDao = db.datasetCacheDao()

    @Provides
    fun provideDashboardCacheDao(db: AppDatabase): DashboardCacheDao = db.dashboardCacheDao()

    @Provides
    fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
}
