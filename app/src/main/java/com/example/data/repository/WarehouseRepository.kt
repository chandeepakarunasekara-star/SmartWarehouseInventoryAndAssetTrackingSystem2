package com.example.data.repository

import com.example.data.local.AppDatabase
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
import com.example.domain.model.OperationType
import com.example.domain.model.SyncStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository orchestrating local Room persistence, offline SyncQueue management,
 * and business validation rules for the Smart Warehouse system.
 */
class WarehouseRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val warehouseDao = database.warehouseDao()
    private val stockTransactionDao = database.stockTransactionDao()
    private val assetDao = database.assetDao()
    private val syncQueueDao = database.syncQueueDao()
    private val userDao = database.userDao()

    // Active authenticated employee session
    private val _currentUser = MutableStateFlow(
        UserEntity(
            userId = "USR-003",
            username = "emp_marcus",
            fullName = "Marcus Cole",
            email = "m.cole@smartwarehouse.net",
            role = UserRole.WAREHOUSE_EMPLOYEE,
            warehouseAssignedId = "WH-NORTH"
        )
    )
    val currentUser = _currentUser.asStateFlow()

    // Network connectivity simulation toggle for offline testing
    private val _isNetworkConnected = MutableStateFlow(true)
    val isNetworkConnected = _isNetworkConnected.asStateFlow()

    fun setNetworkConnected(connected: Boolean) {
        _isNetworkConnected.value = connected
    }

    // Quick switch session for testing authorization matrices in the app
    fun switchUserRole(role: UserRole) {
        val user = when (role) {
            UserRole.ADMIN -> UserEntity(
                userId = "USR-001",
                username = "admin_sarah",
                fullName = "Sarah Jenkins",
                email = "s.jenkins@smartwarehouse.net",
                role = UserRole.ADMIN,
                warehouseAssignedId = "WH-NORTH"
            )
            UserRole.WAREHOUSE_MANAGER -> UserEntity(
                userId = "USR-002",
                username = "mgr_david",
                fullName = "David Vance",
                email = "d.vance@smartwarehouse.net",
                role = UserRole.WAREHOUSE_MANAGER,
                warehouseAssignedId = "WH-NORTH"
            )
            UserRole.WAREHOUSE_EMPLOYEE -> UserEntity(
                userId = "USR-003",
                username = "emp_marcus",
                fullName = "Marcus Cole",
                email = "m.cole@smartwarehouse.net",
                role = UserRole.WAREHOUSE_EMPLOYEE,
                warehouseAssignedId = "WH-NORTH"
            )
        }
        _currentUser.value = user
    }

    // ----------------------------------------------------
    // Products
    // ----------------------------------------------------
    fun observeAllProducts(): Flow<List<ProductEntity>> = productDao.observeAllProducts()
    fun observeLowStockProducts(): Flow<List<ProductEntity>> = productDao.observeLowStockProducts()
    suspend fun getProductById(id: String): ProductEntity? = withContext(Dispatchers.IO) { productDao.getProductById(id) }

    suspend fun findProductByCode(code: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.findProductByCode(code.trim())
    }

    suspend fun saveProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)

        // Queue offline synchronization
        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "PRODUCT",
            entityId = product.productId,
            operationType = OperationType.CREATE,
            payloadJson = "{\"sku\":\"${product.sku}\",\"name\":\"${product.name}\",\"qty\":${product.quantity}}",
            idempotencyKey = "PROD_${product.productId}_${product.updatedAt}"
        )
        syncQueueDao.insertSyncItem(syncItem)

        // Audit log
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "PRODUCT_CREATED",
                entityType = "PRODUCT",
                entityId = product.productId,
                details = "Product ${product.name} (SKU: ${product.sku}) saved with initial stock ${product.quantity}"
            )
        )
    }

    // ----------------------------------------------------
    // Warehouses & Locations
    // ----------------------------------------------------
    fun observeWarehouses(): Flow<List<WarehouseEntity>> = warehouseDao.observeWarehouses()
    fun observeLocationsForWarehouse(warehouseId: String): Flow<List<LocationEntity>> = warehouseDao.observeLocationsForWarehouse(warehouseId)
    suspend fun getAllLocations(): List<LocationEntity> = withContext(Dispatchers.IO) { warehouseDao.getAllLocations() }
    fun observeCategories(): Flow<List<CategoryEntity>> = warehouseDao.observeCategories()
    fun observeSuppliers(): Flow<List<SupplierEntity>> = warehouseDao.observeSuppliers()

    // ----------------------------------------------------
    // Inventory Operations
    // ----------------------------------------------------
    /**
     * Executes a Stock-In receipt operation.
     * Updates local stock immediately, logs transaction, and enqueues sync operation.
     */
    suspend fun stockIn(
        productId: String,
        quantity: Int,
        destinationLocationId: String,
        referenceNumber: String,
        notes: String
    ): Result<StockTransactionEntity> = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Quantity must be greater than zero."))
        }
        val product = productDao.getProductById(productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Product not found: $productId"))

        val newQuantity = product.quantity + quantity
        productDao.updateProductStock(productId, newQuantity)

        val txnId = "TXN-" + System.currentTimeMillis().toString().takeLast(6)
        val transaction = StockTransactionEntity(
            transactionId = txnId,
            productId = product.productId,
            productName = product.name,
            transactionType = TransactionType.STOCK_IN,
            quantity = quantity,
            sourceLocationId = "INCOMING-RECEIPT",
            destinationLocationId = destinationLocationId,
            performedByUserId = _currentUser.value.userId,
            performedByUserName = _currentUser.value.fullName,
            referenceNumber = referenceNumber.ifBlank { "PO-$txnId" },
            notes = notes,
            syncStatus = if (_isNetworkConnected.value) SyncStatus.SYNCED else SyncStatus.PENDING
        )
        stockTransactionDao.insertTransaction(transaction)

        // Queue for server sync
        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "INVENTORY_TRANSACTION",
            entityId = txnId,
            operationType = OperationType.STOCK_IN,
            payloadJson = "{\"productId\":\"$productId\",\"quantity\":$quantity,\"destinationLocation\":\"$destinationLocationId\"}",
            idempotencyKey = "TXN_${txnId}_IN"
        )
        syncQueueDao.insertSyncItem(syncItem)

        // Audit log
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "STOCK_IN",
                entityType = "PRODUCT",
                entityId = productId,
                details = "Stock In: +$quantity for ${product.name}. New total: $newQuantity (Ref: $referenceNumber)"
            )
        )

        Result.success(transaction)
    }

    /**
     * Executes a Stock-Out deduction operation.
     * Strictly verifies stock availability to prevent negative inventory balances.
     */
    suspend fun stockOut(
        productId: String,
        quantity: Int,
        destinationNotes: String,
        referenceNumber: String,
        notes: String
    ): Result<StockTransactionEntity> = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Quantity must be greater than zero."))
        }
        val product = productDao.getProductById(productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Product not found: $productId"))

        if (product.quantity < quantity) {
            return@withContext Result.failure(
                IllegalStateException("Insufficient stock! Available: ${product.quantity}, requested: $quantity")
            )
        }

        val newQuantity = product.quantity - quantity
        productDao.updateProductStock(productId, newQuantity)

        val txnId = "TXN-" + System.currentTimeMillis().toString().takeLast(6)
        val transaction = StockTransactionEntity(
            transactionId = txnId,
            productId = product.productId,
            productName = product.name,
            transactionType = TransactionType.STOCK_OUT,
            quantity = quantity,
            sourceLocationId = product.locationId,
            destinationLocationId = destinationNotes.ifBlank { "DISPATCH" },
            performedByUserId = _currentUser.value.userId,
            performedByUserName = _currentUser.value.fullName,
            referenceNumber = referenceNumber.ifBlank { "DISP-$txnId" },
            notes = notes,
            syncStatus = if (_isNetworkConnected.value) SyncStatus.SYNCED else SyncStatus.PENDING
        )
        stockTransactionDao.insertTransaction(transaction)

        // Enqueue synchronization
        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "INVENTORY_TRANSACTION",
            entityId = txnId,
            operationType = OperationType.STOCK_OUT,
            payloadJson = "{\"productId\":\"$productId\",\"quantity\":$quantity,\"reason\":\"$destinationNotes\"}",
            idempotencyKey = "TXN_${txnId}_OUT"
        )
        syncQueueDao.insertSyncItem(syncItem)

        // Low stock notification trigger
        if (newQuantity <= product.minimumStockLevel) {
            userDao.insertNotification(
                NotificationEntity(
                    notificationId = UUID.randomUUID().toString(),
                    title = "Low Stock Alert: ${product.name}",
                    message = "Stock reduced to $newQuantity (Minimum threshold: ${product.minimumStockLevel}).",
                    type = "LOW_STOCK",
                    referenceId = product.productId
                )
            )
        }

        // Audit log
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "STOCK_OUT",
                entityType = "PRODUCT",
                entityId = productId,
                details = "Stock Out: -$quantity for ${product.name}. Remaining: $newQuantity (Ref: $referenceNumber)"
            )
        )

        Result.success(transaction)
    }

    /**
     * Executes an internal or cross-warehouse stock transfer.
     */
    suspend fun stockTransfer(
        productId: String,
        quantity: Int,
        destinationWarehouseId: String,
        destinationLocationId: String,
        referenceNumber: String,
        notes: String
    ): Result<StockTransactionEntity> = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Transfer quantity must be greater than zero."))
        }
        val product = productDao.getProductById(productId)
            ?: return@withContext Result.failure(IllegalArgumentException("Product not found: $productId"))

        if (product.quantity < quantity) {
            return@withContext Result.failure(
                IllegalStateException("Transfer exceeds available stock (${product.quantity})")
            )
        }

        // Update product location to new destination
        productDao.updateProductLocation(productId, destinationWarehouseId, destinationLocationId)

        val txnId = "TXN-" + System.currentTimeMillis().toString().takeLast(6)
        val transaction = StockTransactionEntity(
            transactionId = txnId,
            productId = product.productId,
            productName = product.name,
            transactionType = TransactionType.STOCK_TRANSFER,
            quantity = quantity,
            sourceLocationId = product.locationId,
            destinationLocationId = destinationLocationId,
            performedByUserId = _currentUser.value.userId,
            performedByUserName = _currentUser.value.fullName,
            referenceNumber = referenceNumber.ifBlank { "XFR-$txnId" },
            notes = notes,
            syncStatus = if (_isNetworkConnected.value) SyncStatus.SYNCED else SyncStatus.PENDING
        )
        stockTransactionDao.insertTransaction(transaction)

        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "INVENTORY_TRANSACTION",
            entityId = txnId,
            operationType = OperationType.TRANSFER,
            payloadJson = "{\"productId\":\"$productId\",\"quantity\":$quantity,\"from\":\"${product.locationId}\",\"to\":\"$destinationLocationId\"}",
            idempotencyKey = "TXN_${txnId}_XFR"
        )
        syncQueueDao.insertSyncItem(syncItem)

        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "STOCK_TRANSFER",
                entityType = "PRODUCT",
                entityId = productId,
                details = "Transferred $quantity units of ${product.name} to $destinationLocationId"
            )
        )

        Result.success(transaction)
    }

    // ----------------------------------------------------
    // Assets & Movement Tracking
    // ----------------------------------------------------
    fun observeAssets(): Flow<List<AssetEntity>> = assetDao.observeAssets()
    suspend fun getAssetById(id: String): AssetEntity? = withContext(Dispatchers.IO) { assetDao.getAssetById(id) }
    suspend fun findAssetByCode(code: String): AssetEntity? = withContext(Dispatchers.IO) { assetDao.findAssetByCode(code.trim()) }
    fun observeMovementsForAsset(assetId: String): Flow<List<AssetMovementEntity>> = assetDao.observeMovementsForAsset(assetId)
    fun observeAllAssetMovements(): Flow<List<AssetMovementEntity>> = assetDao.observeAllAssetMovements()

    suspend fun saveAsset(asset: AssetEntity) = withContext(Dispatchers.IO) {
        assetDao.insertAsset(asset)
        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "ASSET",
            entityId = asset.assetId,
            operationType = OperationType.CREATE,
            payloadJson = "{\"code\":\"${asset.assetCode}\",\"name\":\"${asset.name}\",\"status\":\"${asset.status}\"}",
            idempotencyKey = "AST_${asset.assetId}"
        )
        syncQueueDao.insertSyncItem(syncItem)
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "ASSET_CREATED",
                entityType = "ASSET",
                entityId = asset.assetId,
                details = "Registered asset ${asset.name} (${asset.assetCode})"
            )
        )
    }

    suspend fun recordAssetMovement(
        assetId: String,
        toLocationId: String,
        newEmployee: String,
        movementType: String,
        notes: String
    ): Result<AssetMovementEntity> = withContext(Dispatchers.IO) {
        val asset = assetDao.getAssetById(assetId)
            ?: return@withContext Result.failure(IllegalArgumentException("Asset not found: $assetId"))

        val movement = AssetMovementEntity(
            movementId = "MOV-" + System.currentTimeMillis().toString().takeLast(6),
            assetId = assetId,
            assetName = asset.name,
            fromLocationId = asset.locationId,
            toLocationId = toLocationId,
            previousEmployee = asset.assignedEmployeeName,
            newEmployee = newEmployee,
            movementType = movementType,
            performedBy = _currentUser.value.fullName,
            notes = notes
        )
        assetDao.insertMovement(movement)

        // Update asset location & assigned worker
        val updatedAsset = asset.copy(
            locationId = toLocationId,
            assignedEmployeeName = newEmployee,
            status = if (newEmployee.isNotBlank()) AssetStatus.ASSIGNED else AssetStatus.AVAILABLE
        )
        assetDao.updateAsset(updatedAsset)

        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "ASSET_MOVEMENT",
            entityId = movement.movementId,
            operationType = OperationType.ASSET_MOVE,
            payloadJson = "{\"assetId\":\"$assetId\",\"to\":\"$toLocationId\",\"emp\":\"$newEmployee\"}",
            idempotencyKey = "MOV_${movement.movementId}"
        )
        syncQueueDao.insertSyncItem(syncItem)

        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "ASSET_MOVED",
                entityType = "ASSET",
                entityId = assetId,
                details = "Moved ${asset.name} to $toLocationId, assigned to $newEmployee"
            )
        )

        Result.success(movement)
    }

    // ----------------------------------------------------
    // Transactions & Audit
    // ----------------------------------------------------
    fun observeTransactions(): Flow<List<StockTransactionEntity>> = stockTransactionDao.observeTransactions()
    fun observeRecentTransactions(): Flow<List<StockTransactionEntity>> = stockTransactionDao.observeRecentTransactions(15)
    fun observeAuditLogs(): Flow<List<AuditLogEntity>> = userDao.observeAuditLogs()
    fun observeNotifications(): Flow<List<NotificationEntity>> = userDao.observeNotifications()
    suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) { userDao.markNotificationAsRead(id) }

    // ----------------------------------------------------
    // SyncQueue & Offline Processing
    // ----------------------------------------------------
    fun observeAllSyncItems(): Flow<List<SyncQueueEntity>> = syncQueueDao.observeAllSyncItems()
    fun observePendingSyncItems(): Flow<List<SyncQueueEntity>> = syncQueueDao.observePendingSyncItems()

    /**
     * Processes pending operations in the SyncQueue.
     * Simulates REST communication with the backend, retries with backoff,
     * and sets status to SYNCED upon acknowledgement.
     */
    suspend fun processSyncQueue(): Int = withContext(Dispatchers.IO) {
        if (!_isNetworkConnected.value) return@withContext 0

        val pending = syncQueueDao.getPendingOrFailedItems()
        var syncedCount = 0

        for (item in pending) {
            try {
                // Mark in progress
                syncQueueDao.updateSyncItem(item.copy(status = SyncStatus.IN_PROGRESS, lastAttemptAt = System.currentTimeMillis()))

                // Simulate network latency & idempotent backend REST API dispatch
                kotlinx.coroutines.delay(150)

                // Mark synced
                syncQueueDao.updateSyncItem(
                    item.copy(
                        status = SyncStatus.SYNCED,
                        lastAttemptAt = System.currentTimeMillis(),
                        errorMessage = ""
                    )
                )
                syncedCount++
            } catch (e: Exception) {
                syncQueueDao.updateSyncItem(
                    item.copy(
                        status = SyncStatus.FAILED,
                        retryCount = item.retryCount + 1,
                        lastAttemptAt = System.currentTimeMillis(),
                        errorMessage = e.message ?: "Network timeout"
                    )
                )
            }
        }
        syncedCount
    }

    suspend fun clearCompletedSyncItems() = withContext(Dispatchers.IO) {
        syncQueueDao.clearCompletedSyncItems()
    }
}
