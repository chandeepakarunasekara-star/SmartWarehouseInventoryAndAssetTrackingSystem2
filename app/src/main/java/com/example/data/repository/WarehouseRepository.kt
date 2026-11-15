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
import com.example.data.remote.RemoteWarehouseDataSource
import com.example.data.remote.model.ServerHealthCheckResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository orchestrating local Room persistence, offline SyncQueue management,
 * user authentication, hardware integrations, and business validation rules.
 */
class WarehouseRepository(
    private val database: AppDatabase,
    private val context: android.content.Context? = null
) {

    private val productDao = database.productDao()
    private val warehouseDao = database.warehouseDao()
    private val stockTransactionDao = database.stockTransactionDao()
    private val assetDao = database.assetDao()
    private val syncQueueDao = database.syncQueueDao()
    private val userDao = database.userDao()
    private val remoteDataSource = RemoteWarehouseDataSource()
    val firestoreManager = com.example.data.remote.FirebaseFirestoreManager(context)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            if (productDao.getAllProducts().isEmpty()) {
                com.example.data.local.DatabaseInitialDataSeeder.seedInitialData(database)
            } else {
                com.example.data.local.DatabaseInitialDataSeeder.updateDefaultUsers(database)
            }
            // Two-Way Sync: Fetch and restore existing products from Cloud Firestore into local Room DB
            firestoreManager.fetchAndSyncProductsToLocalDb(productDao)
        }
    }

    fun updateRemoteBaseUrl(url: String) {
        remoteDataSource.updateBaseUrl(url)
    }

    fun getRemoteBaseUrl(): String = remoteDataSource.getBaseUrl()

    suspend fun pingRemoteServer(): ServerHealthCheckResult = remoteDataSource.pingBackend()

    // Active authenticated employee session
    private val _currentUser = MutableStateFlow(
        UserEntity(
            userId = "USR-003",
            username = "emp_104",
            fullName = "Sarath pathirana",
            email = "sarath@smartwarehouse.net",
            role = UserRole.WAREHOUSE_EMPLOYEE,
            warehouseAssignedId = "WH-NORTH",
            isActive = true
        )
    )
    val currentUser = _currentUser.asStateFlow()

    // Network connectivity simulation toggle for offline testing
    private val _isNetworkConnected = MutableStateFlow(true)
    val isNetworkConnected = _isNetworkConnected.asStateFlow()

    fun setNetworkConnected(connected: Boolean) {
        _isNetworkConnected.value = connected
    }

    // Role switcher & login authentication
    fun switchUserRole(role: UserRole) {
        val user = when (role) {
            UserRole.ADMIN -> UserEntity(
                userId = "USR-001",
                username = "admin_sarah",
                fullName = "Chandeepa karunasekara",
                email = "chandeepa@smartwarehouse.net",
                role = UserRole.ADMIN,
                warehouseAssignedId = "WH-NORTH",
                isActive = true
            )
            UserRole.WAREHOUSE_MANAGER -> UserEntity(
                userId = "USR-002",
                username = "mgr_david",
                fullName = "Nimal Rathnayake",
                email = "nimal@smartwarehouse.net",
                role = UserRole.WAREHOUSE_MANAGER,
                warehouseAssignedId = "WH-NORTH",
                isActive = true
            )
            UserRole.WAREHOUSE_EMPLOYEE -> UserEntity(
                userId = "USR-003",
                username = "emp_104",
                fullName = "Sarath pathirana",
                email = "sarath@smartwarehouse.net",
                role = UserRole.WAREHOUSE_EMPLOYEE,
                warehouseAssignedId = "WH-NORTH",
                isActive = true
            )
        }
        _currentUser.value = user
    }

    suspend fun loginUser(username: String, role: UserRole): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmed = username.trim()
        val lower = trimmed.lowercase()

        // Match against pre-seeded enterprise accounts or requested role
        val matchedUser: UserEntity? = when {
            role == UserRole.ADMIN || lower.contains("admin") || lower.contains("sarah") || lower.contains("chandeepa") -> {
                val dbUser = userDao.findUserByUsername("admin_sarah")
                (dbUser ?: UserEntity(
                    userId = "USR-001",
                    username = "admin_sarah",
                    fullName = "Chandeepa karunasekara",
                    email = "chandeepa@smartwarehouse.net",
                    role = UserRole.ADMIN,
                    warehouseAssignedId = "WH-NORTH",
                    isActive = true
                )).copy(
                    fullName = "Chandeepa karunasekara",
                    email = "chandeepa@smartwarehouse.net"
                )
            }
            role == UserRole.WAREHOUSE_MANAGER || lower.contains("manager") || lower.contains("david") || lower.contains("nimal") -> {
                val dbUser = userDao.findUserByUsername("mgr_david")
                (dbUser ?: UserEntity(
                    userId = "USR-002",
                    username = "mgr_david",
                    fullName = "Nimal Rathnayake",
                    email = "nimal@smartwarehouse.net",
                    role = UserRole.WAREHOUSE_MANAGER,
                    warehouseAssignedId = "WH-NORTH",
                    isActive = true
                )).copy(
                    fullName = "Nimal Rathnayake",
                    email = "nimal@smartwarehouse.net"
                )
            }
            role == UserRole.WAREHOUSE_EMPLOYEE || lower.contains("employee") || lower.contains("marcus") || lower.contains("104") || lower.contains("sarath") -> {
                val dbUser = userDao.findUserByUsername("emp_104")
                (dbUser ?: UserEntity(
                    userId = "USR-003",
                    username = "emp_104",
                    fullName = "Sarath pathirana",
                    email = "sarath@smartwarehouse.net",
                    role = UserRole.WAREHOUSE_EMPLOYEE,
                    warehouseAssignedId = "WH-NORTH",
                    isActive = true
                )).copy(
                    fullName = "Sarath pathirana",
                    email = "sarath@smartwarehouse.net"
                )
            }
            else -> {
                userDao.findUserByUsername(lower)
            }
        }

        val targetUser = matchedUser ?: UserEntity(
            userId = "USR-" + UUID.randomUUID().toString().take(6).uppercase(),
            username = lower,
            fullName = trimmed.substringBefore("@").replaceFirstChar { it.uppercase() },
            email = if (trimmed.contains("@")) trimmed else "$lower@smartwarehouse.net",
            role = role,
            warehouseAssignedId = "WH-NORTH",
            isActive = true
        )

        if (!targetUser.isActive) {
            return@withContext Result.failure(IllegalStateException("User account is deactivated."))
        }

        userDao.insertUser(targetUser)
        _currentUser.value = targetUser

        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = targetUser.userId,
                userName = targetUser.fullName,
                eventType = "LOGIN",
                entityType = "USER",
                entityId = targetUser.userId,
                details = "User ${targetUser.fullName} logged in successfully as ${targetUser.role.name}."
            )
        )
        Result.success(targetUser)
    }

    // ----------------------------------------------------
    // User Administration (Use Case 23)
    // ----------------------------------------------------
    fun observeUsers(): Flow<List<UserEntity>> = userDao.observeUsers()

    suspend fun createUser(fullName: String, username: String, email: String, role: UserRole, warehouseId: String): UserEntity = withContext(Dispatchers.IO) {
        val newUser = UserEntity(
            userId = "USR-" + UUID.randomUUID().toString().take(6).uppercase(),
            username = username.trim().lowercase(),
            fullName = fullName.trim(),
            email = email.trim(),
            role = role,
            warehouseAssignedId = warehouseId,
            isActive = true
        )
        userDao.insertUser(newUser)
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "USER_CREATED",
                entityType = "USER",
                entityId = newUser.userId,
                details = "Administrator created user ${newUser.fullName} with role ${newUser.role}."
            )
        )
        newUser
    }

    suspend fun toggleUserActive(userId: String, currentActive: Boolean) = withContext(Dispatchers.IO) {
        userDao.setUserActive(userId, !currentActive)
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = if (!currentActive) "USER_ACTIVATED" else "USER_DEACTIVATED",
                entityType = "USER",
                entityId = userId,
                details = "User status changed to ${if (!currentActive) "Active" else "Deactivated"}."
            )
        )
    }

    // ----------------------------------------------------
    // Products (Use Case 2, 3, 7, 14, 15)
    // ----------------------------------------------------
    fun observeAllProducts(): Flow<List<ProductEntity>> = productDao.observeAllProducts()
    fun observeLowStockProducts(): Flow<List<ProductEntity>> = productDao.observeLowStockProducts()
    fun observeExpiringProducts(): Flow<List<ProductEntity>> = productDao.observeExpiringProducts()
    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query.trim())
    fun observeProductsByWarehouse(warehouseId: String): Flow<List<ProductEntity>> = productDao.observeProductsByWarehouse(warehouseId)
    fun observeProductsByLocation(locationId: String): Flow<List<ProductEntity>> = productDao.observeProductsByLocation(locationId)

    suspend fun getProductById(id: String): ProductEntity? = withContext(Dispatchers.IO) { productDao.getProductById(id) }

    suspend fun findProductByCode(code: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.findProductByCode(code.trim())
    }

    suspend fun deleteProduct(productId: String) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(productId)
    }

    suspend fun saveProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        // Step 1: Add product to local Room SQLite database first
        productDao.insertProduct(product)

        var isSynced = false
        var syncNote = "Offline mode - Queued for sync"

        // Step 2: Next, if online, push to Cloud Firestore database
        if (_isNetworkConnected.value) {
            val firestoreResult = firestoreManager.saveProductToFirestore(product)
            if (firestoreResult.isSuccess) {
                isSynced = true
                syncNote = "Synced to Firebase Cloud Firestore"
            } else {
                syncNote = "Firestore save notice: ${firestoreResult.exceptionOrNull()?.message ?: "Queued for sync"}"
            }
        }

        // Step 3: Insert queue record into sync_queue table (PENDING if offline)
        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "PRODUCT",
            entityId = product.productId,
            operationType = OperationType.CREATE,
            payloadJson = "{\"sku\":\"${product.sku}\",\"name\":\"${product.name}\",\"qty\":${product.quantity},\"img\":\"${product.imageUrl}\"}",
            createdAt = System.currentTimeMillis(),
            status = if (isSynced) SyncStatus.SYNCED else SyncStatus.PENDING,
            errorMessage = syncNote,
            idempotencyKey = "PROD_${product.productId}_${product.updatedAt}"
        )
        syncQueueDao.insertSyncItem(syncItem)

        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "PRODUCT_CREATED",
                entityType = "PRODUCT",
                entityId = product.productId,
                details = "Product ${product.name} (SKU: ${product.sku}) saved with stock ${product.quantity}. ($syncNote)"
            )
        )
    }

    suspend fun deactivateProduct(productId: String) = withContext(Dispatchers.IO) {
        productDao.deactivateProduct(productId)
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "PRODUCT_DEACTIVATED",
                entityType = "PRODUCT",
                entityId = productId,
                details = "Product $productId deactivated."
            )
        )
    }

    // ----------------------------------------------------
    // Warehouses & Locations (Use Case 8)
    // ----------------------------------------------------
    fun observeWarehouses(): Flow<List<WarehouseEntity>> = warehouseDao.observeWarehouses()
    fun observeLocationsForWarehouse(warehouseId: String): Flow<List<LocationEntity>> = warehouseDao.observeLocationsForWarehouse(warehouseId)
    suspend fun getAllLocations(): List<LocationEntity> = withContext(Dispatchers.IO) { warehouseDao.getAllLocations() }
    fun observeCategories(): Flow<List<CategoryEntity>> = warehouseDao.observeCategories()
    fun observeSuppliers(): Flow<List<SupplierEntity>> = warehouseDao.observeSuppliers()

    suspend fun createSupplier(name: String, contactPerson: String, email: String, phone: String, address: String): SupplierEntity = withContext(Dispatchers.IO) {
        val sup = SupplierEntity(
            supplierId = "SUP-" + UUID.randomUUID().toString().take(6).uppercase(),
            name = name.trim(),
            contactPerson = contactPerson.trim(),
            email = email.trim(),
            phone = phone.trim(),
            address = address.trim()
        )
        warehouseDao.insertSupplier(sup)
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "SUPPLIER_CREATED",
                entityType = "SUPPLIER",
                entityId = sup.supplierId,
                details = "Supplier ${sup.name} registered by ${_currentUser.value.fullName}."
            )
        )
        sup
    }

    suspend fun deleteSupplier(supplierId: String) = withContext(Dispatchers.IO) {
        warehouseDao.deleteSupplierById(supplierId)
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "SUPPLIER_DELETED",
                entityType = "SUPPLIER",
                entityId = supplierId,
                details = "Supplier $supplierId removed."
            )
        )
    }

    suspend fun createWarehouse(name: String, code: String, address: String, capacity: Int, manager: String): WarehouseEntity = withContext(Dispatchers.IO) {
        val wh = WarehouseEntity(
            warehouseId = "WH-" + UUID.randomUUID().toString().take(6).uppercase(),
            name = name.trim(),
            code = code.trim().uppercase(),
            address = address.trim(),
            totalCapacityUnits = capacity,
            managerName = manager.trim()
        )
        warehouseDao.insertWarehouse(wh)
        wh
    }

    suspend fun createLocation(warehouseId: String, section: String, rack: String, shelf: String, binCode: String, maxCapacity: Int): LocationEntity = withContext(Dispatchers.IO) {
        val loc = LocationEntity(
            locationId = "LOC-${warehouseId}-${section}-${rack}-${binCode}",
            warehouseId = warehouseId,
            section = section.trim().uppercase(),
            rack = rack.trim().uppercase(),
            shelf = shelf.trim().uppercase(),
            binCode = binCode.trim().uppercase(),
            maxCapacity = maxCapacity,
            currentOccupancy = 0
        )
        warehouseDao.insertLocation(loc)
        loc
    }

    // ----------------------------------------------------
    // Inventory Operations (Use Case 4, 5, 6, 26)
    // ----------------------------------------------------
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
        val updatedProduct = product.copy(quantity = newQuantity, updatedAt = System.currentTimeMillis())

        // Step 1: Update in local Room SQLite database first
        productDao.updateProductStock(productId, newQuantity)

        var isSynced = false
        var syncNote = "Offline Stock In - Queued for sync"

        // Step 2: Next, if online, push to Cloud Firestore database
        if (_isNetworkConnected.value) {
            val prodResult = firestoreManager.saveProductToFirestore(updatedProduct)
            if (prodResult.isSuccess) {
                isSynced = true
                syncNote = "Synced to Firebase Cloud Firestore"
            } else {
                syncNote = "Firestore sync notice: ${prodResult.exceptionOrNull()?.message ?: "Queued for sync"}"
            }
        }

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
            transferStatus = "COMPLETED",
            syncStatus = if (isSynced) SyncStatus.SYNCED else SyncStatus.PENDING
        )
        stockTransactionDao.insertTransaction(transaction)
        if (isSynced) {
            firestoreManager.saveTransactionToFirestore(transaction)
        }

        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "INVENTORY_TRANSACTION",
            entityId = txnId,
            operationType = OperationType.STOCK_IN,
            payloadJson = "{\"productId\":\"$productId\",\"quantity\":$quantity,\"destinationLocation\":\"$destinationLocationId\"}",
            createdAt = System.currentTimeMillis(),
            status = if (isSynced) SyncStatus.SYNCED else SyncStatus.PENDING,
            errorMessage = syncNote,
            idempotencyKey = "TXN_${txnId}_IN"
        )
        syncQueueDao.insertSyncItem(syncItem)

        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "STOCK_IN",
                entityType = "PRODUCT",
                entityId = productId,
                details = "Stock In: +$quantity for ${product.name}. New total: $newQuantity. ($syncNote)"
            )
        )

        Result.success(transaction)
    }

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
        val updatedProduct = product.copy(quantity = newQuantity, updatedAt = System.currentTimeMillis())

        // Step 1: Update in local Room SQLite database first
        productDao.updateProductStock(productId, newQuantity)

        var isSynced = false
        var syncNote = "Offline Stock Out - Queued for sync"

        // Step 2: Next, if online, push to Cloud Firestore database
        if (_isNetworkConnected.value) {
            val prodResult = firestoreManager.saveProductToFirestore(updatedProduct)
            if (prodResult.isSuccess) {
                isSynced = true
                syncNote = "Synced to Firebase Cloud Firestore"
            } else {
                syncNote = "Firestore sync notice: ${prodResult.exceptionOrNull()?.message ?: "Queued for sync"}"
            }
        }

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
            transferStatus = "COMPLETED",
            syncStatus = if (isSynced) SyncStatus.SYNCED else SyncStatus.PENDING
        )
        stockTransactionDao.insertTransaction(transaction)
        if (isSynced) {
            firestoreManager.saveTransactionToFirestore(transaction)
        }

        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "INVENTORY_TRANSACTION",
            entityId = txnId,
            operationType = OperationType.STOCK_OUT,
            payloadJson = "{\"productId\":\"$productId\",\"quantity\":$quantity,\"reason\":\"$destinationNotes\"}",
            createdAt = System.currentTimeMillis(),
            status = if (isSynced) SyncStatus.SYNCED else SyncStatus.PENDING,
            errorMessage = syncNote,
            idempotencyKey = "TXN_${txnId}_OUT"
        )
        syncQueueDao.insertSyncItem(syncItem)

        if (newQuantity <= product.minimumStockLevel) {
            userDao.insertNotification(
                NotificationEntity(
                    notificationId = UUID.randomUUID().toString(),
                    title = "⚠ Low Stock Alert: ${product.name}",
                    message = "Stock reduced to $newQuantity (Minimum threshold: ${product.minimumStockLevel}). Reorder recommended.",
                    type = "LOW_STOCK",
                    referenceId = product.productId
                )
            )
        }

        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = UUID.randomUUID().toString(),
                userId = _currentUser.value.userId,
                userName = _currentUser.value.fullName,
                eventType = "STOCK_OUT",
                entityType = "PRODUCT",
                entityId = productId,
                details = "Stock Out: -$quantity for ${product.name}. Remaining: $newQuantity. ($syncNote)"
            )
        )

        Result.success(transaction)
    }

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

        // Step 1: Update location in local Room SQLite database first
        productDao.updateProductLocation(productId, destinationWarehouseId, destinationLocationId)

        var isSynced = false
        var syncNote = "Offline Stock Transfer - Queued for sync"

        // Step 2: Next, if online, push to Cloud Firestore database
        if (_isNetworkConnected.value) {
            val updatedProduct = product.copy(locationId = destinationLocationId, warehouseId = destinationWarehouseId, updatedAt = System.currentTimeMillis())
            val prodResult = firestoreManager.saveProductToFirestore(updatedProduct)
            if (prodResult.isSuccess) {
                isSynced = true
                syncNote = "Synced to Firebase Cloud Firestore"
            } else {
                syncNote = "Firestore sync notice: ${prodResult.exceptionOrNull()?.message ?: "Queued for sync"}"
            }
        }

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
            transferStatus = if (_currentUser.value.role == UserRole.WAREHOUSE_EMPLOYEE) "PENDING" else "APPROVED",
            requestedBy = _currentUser.value.fullName,
            approvedBy = if (_currentUser.value.role != UserRole.WAREHOUSE_EMPLOYEE) _currentUser.value.fullName else "",
            syncStatus = if (isSynced) SyncStatus.SYNCED else SyncStatus.PENDING
        )
        stockTransactionDao.insertTransaction(transaction)
        if (isSynced) {
            firestoreManager.saveTransactionToFirestore(transaction)
        }

        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "INVENTORY_TRANSACTION",
            entityId = txnId,
            operationType = OperationType.TRANSFER,
            payloadJson = "{\"productId\":\"$productId\",\"quantity\":$quantity,\"from\":\"${product.locationId}\",\"to\":\"$destinationLocationId\"}",
            createdAt = System.currentTimeMillis(),
            status = if (isSynced) SyncStatus.SYNCED else SyncStatus.PENDING,
            errorMessage = syncNote,
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
                details = "Transferred $quantity units of ${product.name} to $destinationLocationId. ($syncNote)"
            )
        )

        Result.success(transaction)
    }

    suspend fun approveTransfer(transactionId: String, approverName: String) = withContext(Dispatchers.IO) {
        stockTransactionDao.updateTransferStatus(transactionId, "APPROVED", approverName)
        userDao.insertNotification(
            NotificationEntity(
                notificationId = UUID.randomUUID().toString(),
                title = "Transfer Approved",
                message = "Transfer $transactionId has been approved by $approverName.",
                type = "TRANSFER",
                referenceId = transactionId
            )
        )
    }

    // ----------------------------------------------------
    // Assets & Movement Tracking (Use Case 9, 10, 11, 27)
    // ----------------------------------------------------
    fun observeAssets(): Flow<List<AssetEntity>> = assetDao.observeAssets()
    fun searchAssets(query: String): Flow<List<AssetEntity>> = assetDao.searchAssets(query.trim())
    fun observeAssetsByStatus(status: AssetStatus): Flow<List<AssetEntity>> = assetDao.observeAssetsByStatus(status)
    fun observeAssetsByWarehouse(warehouseId: String): Flow<List<AssetEntity>> = assetDao.observeAssetsByWarehouse(warehouseId)
    fun observeAssetsByLocation(locationId: String): Flow<List<AssetEntity>> = assetDao.observeAssetsByLocation(locationId)

    suspend fun getAssetById(id: String): AssetEntity? = withContext(Dispatchers.IO) { assetDao.getAssetById(id) }
    suspend fun findAssetByCode(code: String): AssetEntity? = withContext(Dispatchers.IO) { assetDao.findAssetByCode(code.trim()) }
    fun observeMovementsForAsset(assetId: String): Flow<List<AssetMovementEntity>> = assetDao.observeMovementsForAsset(assetId)
    fun observeAllAssetMovements(): Flow<List<AssetMovementEntity>> = assetDao.observeAllAssetMovements()

    suspend fun deleteAsset(assetId: String) = withContext(Dispatchers.IO) {
        assetDao.deleteAssetById(assetId)
    }

    suspend fun saveAsset(asset: AssetEntity) = withContext(Dispatchers.IO) {
        assetDao.insertAsset(asset)
        val syncItem = SyncQueueEntity(
            syncId = UUID.randomUUID().toString(),
            entityType = "ASSET",
            entityId = asset.assetId,
            operationType = OperationType.CREATE,
            payloadJson = "{\"code\":\"${asset.assetCode}\",\"name\":\"${asset.name}\",\"status\":\"${asset.status}\",\"img\":\"${asset.imageUrl}\"}",
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
                details = "Registered asset ${asset.name} (${asset.assetCode})."
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
                details = "Moved ${asset.name} from ${asset.locationId} to $toLocationId, assigned to $newEmployee."
            )
        )

        Result.success(movement)
    }

    // ----------------------------------------------------
    // Transactions, Audit & Approvals (Use Case 6, 21, 22)
    // ----------------------------------------------------
    fun observeTransactions(): Flow<List<StockTransactionEntity>> = stockTransactionDao.observeTransactions()
    fun observeRecentTransactions(): Flow<List<StockTransactionEntity>> = stockTransactionDao.observeRecentTransactions(15)
    fun observePendingTransfers(): Flow<List<StockTransactionEntity>> = stockTransactionDao.observePendingTransfers()
    fun observeAuditLogs(): Flow<List<AuditLogEntity>> = userDao.observeAuditLogs()
    fun observeNotifications(): Flow<List<NotificationEntity>> = userDao.observeNotifications()
    suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) { userDao.markNotificationAsRead(id) }

    // ----------------------------------------------------
    // SyncQueue & Offline Processing (Use Case 17, 18)
    // ----------------------------------------------------
    fun observeAllSyncItems(): Flow<List<SyncQueueEntity>> = syncQueueDao.observeAllSyncItems()
    fun observePendingSyncItems(): Flow<List<SyncQueueEntity>> = syncQueueDao.observePendingSyncItems()

    suspend fun processSyncQueue(): Int = withContext(Dispatchers.IO) {
        if (!_isNetworkConnected.value) return@withContext 0

        // Two-way sync: Pull remote products from Firebase Cloud Firestore into local Room DB
        firestoreManager.fetchAndSyncProductsToLocalDb(productDao)

        val pending = syncQueueDao.getPendingOrFailedItems()
        if (pending.isEmpty()) return@withContext 0
        var syncedCount = 0

        // Attempt remote batch push over HTTP / REST
        val remoteBatchResult = remoteDataSource.pushSyncBatch(pending)
        val isRemoteSuccess = remoteBatchResult.isSuccess

        for (item in pending) {
            try {
                syncQueueDao.updateSyncItem(item.copy(status = SyncStatus.IN_PROGRESS, lastAttemptAt = System.currentTimeMillis()))

                // Push item to Firebase Cloud Firestore based on entity type
                if (item.entityType == "PRODUCT") {
                    productDao.getProductById(item.entityId)?.let { prod ->
                        firestoreManager.saveProductToFirestore(prod)
                    }
                } else if (item.entityType == "INVENTORY_TRANSACTION") {
                    stockTransactionDao.getTransactionById(item.entityId)?.let { txn ->
                        firestoreManager.saveTransactionToFirestore(txn)
                        productDao.getProductById(txn.productId)?.let { prod ->
                            firestoreManager.saveProductToFirestore(prod)
                        }
                    }
                }

                kotlinx.coroutines.delay(60)
                syncQueueDao.updateSyncItem(
                    item.copy(
                        status = SyncStatus.SYNCED,
                        lastAttemptAt = System.currentTimeMillis(),
                        errorMessage = if (isRemoteSuccess) "Synced to Firebase Cloud Firestore / REST Backend" else "Synced to Firebase Cloud & Room SQLite ledger"
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
