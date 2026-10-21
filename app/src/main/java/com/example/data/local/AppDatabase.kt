package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AssetDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.StockTransactionDao
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WarehouseDao
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.AssetMovementEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockTransactionEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WarehouseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room Database for the Smart Warehouse offline-first architecture.
 * Serves as the single local source of truth for all warehouse operations.
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        SupplierEntity::class,
        WarehouseEntity::class,
        LocationEntity::class,
        ProductEntity::class,
        StockTransactionEntity::class,
        AssetEntity::class,
        AssetMovementEntity::class,
        SyncQueueEntity::class,
        AuditLogEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun assetDao(): AssetDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_warehouse.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabasePrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Seeds initial master catalog data upon initial creation to provide an immediate,
     * fully working warehouse environment.
     */
    private class DatabasePrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    DatabaseInitialDataSeeder.seedInitialData(database)
                }
            }
        }
    }
}
