package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.domain.model.AssetStatus
import com.example.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local Product and inventory catalog management.
 */
@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE productId = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :code OR barcode = :code OR qrCode = :code LIMIT 1")
    suspend fun findProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%') AND isActive = 1 ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE locationId = :locationId AND isActive = 1 ORDER BY name ASC")
    fun observeProductsByLocation(locationId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE warehouseId = :warehouseId AND isActive = 1 ORDER BY name ASC")
    fun observeProductsByWarehouse(warehouseId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE quantity <= minimumStockLevel AND isActive = 1")
    fun observeLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE expiryDate <= :thresholdDate AND isActive = 1 ORDER BY expiryDate ASC")
    fun observeExpiringProducts(thresholdDate: String = "2028-01-01"): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("UPDATE products SET quantity = :newQuantity, updatedAt = :updatedAt WHERE productId = :productId")
    suspend fun updateProductStock(productId: String, newQuantity: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET locationId = :newLocationId, warehouseId = :newWarehouseId, updatedAt = :updatedAt WHERE productId = :productId")
    suspend fun updateProductLocation(productId: String, newWarehouseId: String, newLocationId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET isActive = 0, updatedAt = :updatedAt WHERE productId = :productId")
    suspend fun deactivateProduct(productId: String, updatedAt: Long = System.currentTimeMillis())
}

/**
 * Data Access Object for Warehouses, Sections, Racks, Categories, and Suppliers.
 */
@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses ORDER BY name ASC")
    fun observeWarehouses(): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM warehouses WHERE warehouseId = :id")
    suspend fun getWarehouseById(id: String): WarehouseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: WarehouseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouses(warehouses: List<WarehouseEntity>)

    @Query("SELECT * FROM locations WHERE warehouseId = :warehouseId ORDER BY section, rack, shelf, binCode ASC")
    fun observeLocationsForWarehouse(warehouseId: String): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations")
    suspend fun getAllLocations(): List<LocationEntity>

    @Query("SELECT * FROM locations WHERE locationId = :id")
    suspend fun getLocationById(id: String): LocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun observeSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE supplierId = :id")
    suspend fun deleteSupplierById(id: String)
}

/**
 * Data Access Object for Stock In, Stock Out, and Stock Transfer immutable ledger.
 */
@Dao
interface StockTransactionDao {
    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC")
    fun observeTransactions(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentTransactions(limit: Int = 10): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE transactionType = 'STOCK_TRANSFER' AND transferStatus = 'PENDING' ORDER BY timestamp DESC")
    fun observePendingTransfers(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY timestamp DESC")
    fun observeTransactionsForProduct(productId: String): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE transactionId = :id")
    suspend fun getTransactionById(id: String): StockTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<StockTransactionEntity>)

    @Query("UPDATE stock_transactions SET transferStatus = :status, approvedBy = :approvedBy WHERE transactionId = :transactionId")
    suspend fun updateTransferStatus(transactionId: String, status: String, approvedBy: String)

    @Query("UPDATE stock_transactions SET syncStatus = :status WHERE transactionId = :transactionId")
    suspend fun updateTransactionSyncStatus(transactionId: String, status: SyncStatus)
}

/**
 * Data Access Object for physical equipment assets and movement history.
 */
@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY name ASC")
    fun observeAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE assetId = :id")
    suspend fun getAssetById(id: String): AssetEntity?

    @Query("SELECT * FROM assets WHERE assetCode = :code OR nfcTagId = :code OR serialNumber = :code LIMIT 1")
    suspend fun findAssetByCode(code: String): AssetEntity?

    @Query("SELECT * FROM assets WHERE (name LIKE '%' || :query || '%' OR assetCode LIKE '%' || :query || '%' OR serialNumber LIKE '%' || :query || '%' OR nfcTagId LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchAssets(query: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE status = :status ORDER BY name ASC")
    fun observeAssetsByStatus(status: AssetStatus): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE warehouseId = :warehouseId ORDER BY name ASC")
    fun observeAssetsByWarehouse(warehouseId: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE locationId = :locationId ORDER BY name ASC")
    fun observeAssetsByLocation(locationId: String): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE assetId = :assetId")
    suspend fun deleteAssetById(assetId: String)

    @Query("SELECT * FROM asset_movements WHERE assetId = :assetId ORDER BY timestamp DESC")
    fun observeMovementsForAsset(assetId: String): Flow<List<AssetMovementEntity>>

    @Query("SELECT * FROM asset_movements ORDER BY timestamp DESC")
    fun observeAllAssetMovements(): Flow<List<AssetMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: AssetMovementEntity)
}

/**
 * Data Access Object for offline operations queue and conflict resolution.
 */
@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = :status ORDER BY createdAt ASC")
    fun observePendingSyncItems(status: SyncStatus = SyncStatus.PENDING): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue ORDER BY createdAt DESC")
    fun observeAllSyncItems(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getPendingOrFailedItems(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncQueueEntity)

    @Update
    suspend fun updateSyncItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE syncId = :syncId")
    suspend fun deleteSyncItem(syncId: String)

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun clearCompletedSyncItems()
}

/**
 * Data Access Object for User sessions, User Administration, Audit Logs, and Operational Notifications.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun observeUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isActive = :isActive WHERE userId = :userId")
    suspend fun setUserActive(userId: String, isActive: Boolean)

    // Audit logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun observeAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun observeNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :id")
    suspend fun markNotificationAsRead(id: String)
}
