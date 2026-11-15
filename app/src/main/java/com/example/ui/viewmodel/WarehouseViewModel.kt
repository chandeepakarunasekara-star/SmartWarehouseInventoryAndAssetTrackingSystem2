package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.WarehouseRepository
import com.example.domain.model.AssetStatus
import com.example.domain.model.StockLevelStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserRole
import com.example.hardware.BluetoothDeviceManager
import com.example.hardware.NfcTagManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Data class representing operation success pop-up info for Stock In, Stock Out, and Product Insertion.
 */
data class OperationSuccessInfo(
    val title: String,
    val message: String
)

/**
 * Navigation routes supported across the Smart Warehouse application,
 * matching all 28 use cases from the specifications.
 */
sealed class WarehouseScreen {
    data object Login : WarehouseScreen()
    data object SignUp : WarehouseScreen()
    data object Dashboard : WarehouseScreen()
    data object InventoryDashboard : WarehouseScreen()
    data object AddItem : WarehouseScreen()
    data object Products : WarehouseScreen()
    data object Suppliers : WarehouseScreen()
    data object AuditLog : WarehouseScreen()
    data object Scanner : WarehouseScreen()
    data object StockOperations : WarehouseScreen()
    data object Assets : WarehouseScreen()
    data object Warehouses : WarehouseScreen()
    data object Transactions : WarehouseScreen()
    data object SyncQueue : WarehouseScreen()
    data object Hardware : WarehouseScreen()
    data object Reports : WarehouseScreen()
    data object UserManagement : WarehouseScreen()
    data object Approvals : WarehouseScreen()
    data object FindProduct : WarehouseScreen()
    data object Settings : WarehouseScreen()
    data class ProductDetail(val productId: String) : WarehouseScreen()
}

/**
 * Main ViewModel orchestrating state between the UI, Room database, and hardware managers.
 */
class WarehouseViewModel(
    val repository: WarehouseRepository,
    val bluetoothManager: BluetoothDeviceManager = BluetoothDeviceManager(),
    val nfcManager: NfcTagManager = NfcTagManager(),
    val context: android.content.Context? = null
) : ViewModel() {

    val firebaseAuthManager: com.example.data.auth.FirebaseAuthManager? = context?.let {
        com.example.data.auth.FirebaseAuthManager(it)
    }

    // Current navigation screen (Login screen displayed first on app launch)
    private val _currentScreen = MutableStateFlow<WarehouseScreen>(WarehouseScreen.Login)
    val currentScreen = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<WarehouseScreen>(WarehouseScreen.Login)

    fun navigateTo(screen: WarehouseScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.size > 1) {
            screenBackStack.removeAt(screenBackStack.lastIndex)
            _currentScreen.value = screenBackStack.last()
            return true
        }
        return false
    }

    // Active User session
    val currentUser: StateFlow<UserEntity> = repository.currentUser
    val isNetworkConnected: StateFlow<Boolean> = repository.isNetworkConnected

    fun switchUserRole(role: UserRole) {
        repository.switchUserRole(role)
        showFeedback("Switched active profile to ${role.name}")
    }

    fun login(username: String, role: UserRole) {
        viewModelScope.launch {
            val result = repository.loginUser(username, role)
            result.onSuccess { user ->
                showFeedback("Welcome, ${user.fullName} (${user.role.name})")
                navigateTo(WarehouseScreen.Dashboard)
            }.onFailure { err ->
                showFeedback("Login failed: ${err.message}")
            }
        }
    }

    fun firebaseLogin(
        email: String,
        password: String,
        role: UserRole,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val authMgr = firebaseAuthManager ?: context?.let { com.example.data.auth.FirebaseAuthManager(it) }
            val authResult = authMgr?.signIn(email, password)
                ?: Result.success("LOCAL_AUTH_${email.hashCode()}")

            authResult.onSuccess { uid ->
                val username = email.substringBefore("@").ifBlank { "user_${uid.take(4)}" }
                val userResult = repository.loginUser(username, role)
                userResult.onSuccess { user ->
                    showFeedback("Signed in via Firebase Auth: ${user.fullName} (${user.role.name})")
                    navigateTo(WarehouseScreen.Dashboard)
                    onResult(true, null)
                }.onFailure { err ->
                    showFeedback("User sync error: ${err.message}")
                    onResult(false, err.message)
                }
            }.onFailure { err ->
                val errMsg = err.message ?: "Authentication failed."
                showFeedback(errMsg)
                onResult(false, errMsg)
            }
        }
    }

    fun firebaseSignUp(
        email: String,
        password: String,
        fullName: String,
        role: UserRole,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val authMgr = firebaseAuthManager ?: context?.let { com.example.data.auth.FirebaseAuthManager(it) }
            val authResult = authMgr?.signUp(email, password)
                ?: Result.success("LOCAL_AUTH_${email.hashCode()}")

            authResult.onSuccess { uid ->
                val username = email.substringBefore("@").ifBlank { "user_${uid.take(4)}" }
                val createdUser = repository.createUser(
                    fullName = fullName.ifBlank { username.replaceFirstChar { it.uppercase() } },
                    username = username,
                    email = email.trim(),
                    role = role,
                    warehouseId = "WH-NORTH"
                )
                repository.switchUserRole(role)
                showFeedback("Account created via Firebase Auth for ${createdUser.fullName}")
                navigateTo(WarehouseScreen.Dashboard)
                onResult(true, null)
            }.onFailure { err ->
                val errMsg = err.message ?: "Registration failed."
                showFeedback(errMsg)
                onResult(false, errMsg)
            }
        }
    }

    fun logout() {
        firebaseAuthManager?.signOut()
        screenBackStack.clear()
        screenBackStack.add(WarehouseScreen.Login)
        _currentScreen.value = WarehouseScreen.Login
        showFeedback("Logged out successfully.")
    }

    fun toggleNetworkConnection() {
        val newConnected = !isNetworkConnected.value
        repository.setNetworkConnected(newConnected)
        if (newConnected) {
            showFeedback("Back online: Syncing Room database changes to Firebase Cloud...")
            triggerSyncNow()
        } else {
            showFeedback("Offline mode active: All changes will save locally to Room SQLite.")
        }
    }

    // Master Catalog Data
    val allProducts: StateFlow<List<ProductEntity>> = repository.observeAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.observeLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiringProducts: StateFlow<List<ProductEntity>> = repository.observeExpiringProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val warehouses: StateFlow<List<WarehouseEntity>> = repository.observeWarehouses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<SupplierEntity>> = repository.observeSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assets: StateFlow<List<AssetEntity>> = repository.observeAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<StockTransactionEntity>> = repository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<StockTransactionEntity>> = repository.observeRecentTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransfers: StateFlow<List<StockTransactionEntity>> = repository.observePendingTransfers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.observeUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncQueue: StateFlow<List<SyncQueueEntity>> = repository.observeAllSyncItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.observeNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.observeAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Inserts a new Warehouse Item into the Room SQLite database.
     */
    fun insertWarehouseItem(
        name: String,
        sku: String,
        quantity: Int,
        unitPrice: Double = 29.99,
        categoryId: String = "CAT-ELEC",
        locationId: String = "LOC-WH1-A1",
        imageUrl: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                onComplete(false, "Item name cannot be empty.")
                return@launch
            }
            if (sku.isBlank()) {
                onComplete(false, "SKU cannot be empty.")
                return@launch
            }
            if (quantity < 0) {
                onComplete(false, "Quantity must be 0 or greater.")
                return@launch
            }

            val productId = "PRD-" + UUID.randomUUID().toString().take(8).uppercase()
            val newProduct = ProductEntity(
                productId = productId,
                sku = sku.trim(),
                barcode = sku.trim(),
                qrCode = "QR-${sku.trim()}",
                name = name.trim(),
                description = "Warehouse item added from AddItemScreen terminal.",
                categoryId = categoryId,
                supplierId = "SUP-01",
                unitPrice = unitPrice,
                quantity = quantity,
                minimumStockLevel = 10,
                expiryDate = "2029-12-31",
                locationId = locationId,
                warehouseId = "WH-NORTH",
                imageUrl = imageUrl.ifBlank {
                    "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80"
                }
            )

            try {
                repository.saveProduct(newProduct)
                val syncStatusMsg = if (isNetworkConnected.value) "Room database & Cloud Firestore synced." else "Room database updated (queued for offline sync)."
                showOperationSuccessPopup(
                    title = "New Product Inserted!",
                    message = "Product '${newProduct.name}' (SKU: ${newProduct.sku}, Quantity: ${newProduct.quantity}) inserted successfully. $syncStatusMsg"
                )
                showFeedback("Successfully added '${newProduct.name}' to Room Database!")
                onComplete(true, null)
            } catch (e: Exception) {
                val err = e.message ?: "Failed to insert item."
                showFeedback("Error: $err")
                onComplete(false, err)
            }
        }
    }

    // Locations cache for selection dropdowns
    private val _allLocations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val allLocations = _allLocations.asStateFlow()

    init {
        viewModelScope.launch {
            _allLocations.value = repository.getAllLocations()
        }
    }

    // Product search & filter states
    val productSearchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedStockFilter = MutableStateFlow<StockLevelStatus?>(null)

    // Filtered products flow
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        productSearchQuery,
        selectedCategoryFilter,
        selectedStockFilter
    ) { list, query, category, stockStatus ->
        list.filter { product ->
            val matchesQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.sku.contains(query, ignoreCase = true) ||
                product.barcode.contains(query, ignoreCase = true)

            val matchesCategory = category == null || product.categoryId == category

            val matchesStock = when (stockStatus) {
                StockLevelStatus.LOW_STOCK -> product.quantity in 1..product.minimumStockLevel
                StockLevelStatus.OUT_OF_STOCK -> product.quantity == 0
                StockLevelStatus.IN_STOCK -> product.quantity > product.minimumStockLevel
                else -> true
            }

            matchesQuery && matchesCategory && matchesStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scanner state
    val scannedCode = MutableStateFlow("")
    val scannedProductResult = MutableStateFlow<ProductEntity?>(null)
    val scannedAssetResult = MutableStateFlow<AssetEntity?>(null)
    val scanErrorMessage = MutableStateFlow<String?>(null)

    // Barcode passed from Scanner to AddItemScreen for quick registration
    val pendingScannedBarcode = MutableStateFlow<String?>(null)

    fun setPendingScannedBarcode(barcode: String) {
        pendingScannedBarcode.value = barcode.trim()
    }

    fun clearPendingScannedBarcode() {
        pendingScannedBarcode.value = null
    }

    fun handleScannedCode(code: String) {
        val clean = code.trim()
        scannedCode.value = clean
        scanErrorMessage.value = null

        viewModelScope.launch {
            val product = repository.findProductByCode(clean)
            if (product != null) {
                scannedProductResult.value = product
                scannedAssetResult.value = null
                return@launch
            }

            val asset = repository.findAssetByCode(clean)
            if (asset != null) {
                scannedAssetResult.value = asset
                scannedProductResult.value = null
                return@launch
            }

            scannedProductResult.value = null
            scannedAssetResult.value = null
            scanErrorMessage.value = "No product or asset found matching: $clean"
        }
    }

    fun clearScanResults() {
        scannedCode.value = ""
        scannedProductResult.value = null
        scannedAssetResult.value = null
        scanErrorMessage.value = null
    }

    // Status snackbar / feedback
    val statusMessage = MutableStateFlow<String?>(null)
    val operationSuccessPopup = MutableStateFlow<OperationSuccessInfo?>(null)

    fun showFeedback(message: String) {
        statusMessage.value = message
    }

    fun dismissFeedback() {
        statusMessage.value = null
    }

    fun showOperationSuccessPopup(title: String, message: String) {
        operationSuccessPopup.value = OperationSuccessInfo(title, message)
    }

    fun dismissOperationSuccessPopup() {
        operationSuccessPopup.value = null
    }

    // Stock In action
    fun executeStockIn(
        productId: String,
        quantity: Int,
        destinationLocationId: String,
        referenceNumber: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.stockIn(
                productId,
                quantity,
                destinationLocationId,
                referenceNumber,
                notes
            )
            result.onSuccess {
                val syncStatusMsg = if (isNetworkConnected.value) "Added to Room database & synced to Cloud Firestore." else "Added to Room database (queued for offline sync)."
                showOperationSuccessPopup(
                    title = "Stock-In Operation Completed!",
                    message = "Successfully received $quantity units into inventory. $syncStatusMsg"
                )
                showFeedback("Stock-In successful! Added $quantity units.")
                _allLocations.value = repository.getAllLocations()
            }.onFailure { e ->
                showFeedback("Stock-In failed: ${e.message}")
            }
        }
    }

    /**
     * Quick restock action for Inventory Dashboard alerts
     */
    fun quickRestockProduct(productId: String, quantity: Int = 25) {
        viewModelScope.launch {
            val matchingProd = repository.getProductById(productId)
            val destLoc = matchingProd?.locationId ?: "LOC-WH1-A1"
            val ref = "QUICK-RESTOCK-" + System.currentTimeMillis().toString().takeLast(5)
            val result = repository.stockIn(
                productId = productId,
                quantity = quantity,
                destinationLocationId = destLoc,
                referenceNumber = ref,
                notes = "Quick restock from Inventory Dashboard"
            )
            result.onSuccess {
                val syncStatusMsg = if (isNetworkConnected.value) "Room database & Cloud Firestore synced." else "Room database updated (queued for offline sync)."
                showOperationSuccessPopup(
                    title = "Stock-In Operation Completed!",
                    message = "Quick restock of +$quantity units for ${matchingProd?.name ?: "item"} completed successfully. $syncStatusMsg"
                )
                showFeedback("Restocked +$quantity units for ${matchingProd?.name ?: "item"}!")
            }.onFailure { e ->
                showFeedback("Restock failed: ${e.message}")
            }
        }
    }

    // Stock Out action
    fun executeStockOut(
        productId: String,
        quantity: Int,
        destinationNotes: String,
        referenceNumber: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.stockOut(
                productId,
                quantity,
                destinationNotes,
                referenceNumber,
                notes
            )
            result.onSuccess {
                val syncStatusMsg = if (isNetworkConnected.value) "Room database & Cloud Firestore synced." else "Room database updated (queued for offline sync)."
                showOperationSuccessPopup(
                    title = "Stock-Out Operation Completed!",
                    message = "Successfully dispatched $quantity units. $syncStatusMsg"
                )
                showFeedback("Stock-Out successful! Deducted $quantity units.")
            }.onFailure { e ->
                showFeedback("Stock-Out failed: ${e.message}")
            }
        }
    }

    // Stock Transfer action
    fun executeStockTransfer(
        productId: String,
        quantity: Int,
        destinationWarehouseId: String,
        destinationLocationId: String,
        referenceNumber: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.stockTransfer(
                productId,
                quantity,
                destinationWarehouseId,
                destinationLocationId,
                referenceNumber,
                notes
            )
            result.onSuccess {
                showFeedback("Transfer recorded! Destination: $destinationLocationId.")
            }.onFailure { e ->
                showFeedback("Transfer failed: ${e.message}")
            }
        }
    }

    // Transfer Approval action
    fun approveTransfer(transactionId: String) {
        viewModelScope.launch {
            repository.approveTransfer(transactionId, currentUser.value.fullName)
            showFeedback("Transfer $transactionId approved successfully.")
        }
    }

    // Add Product action with Photo support
    fun createProduct(
        sku: String,
        barcode: String,
        name: String,
        description: String,
        categoryId: String,
        supplierId: String,
        unitPrice: Double,
        quantity: Int,
        minStock: Int,
        locationId: String,
        warehouseId: String,
        imageUrl: String = "",
        expiryDate: String = "2030-12-31"
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                productId = "PRD-" + UUID.randomUUID().toString().take(6).uppercase(),
                sku = sku.trim(),
                barcode = barcode.trim(),
                qrCode = "PRD-${sku.trim()}",
                name = name.trim(),
                description = description.trim(),
                categoryId = categoryId,
                supplierId = supplierId,
                unitPrice = unitPrice,
                quantity = quantity,
                minimumStockLevel = minStock,
                expiryDate = expiryDate,
                locationId = locationId,
                warehouseId = warehouseId,
                imageUrl = imageUrl.trim()
            )
            repository.saveProduct(product)
            val syncStatusMsg = if (isNetworkConnected.value) "Room database & Cloud Firestore synced." else "Room database updated (queued for offline sync)."
            showOperationSuccessPopup(
                title = "New Product Inserted!",
                message = "Product '${product.name}' (SKU: ${product.sku}, Quantity: ${product.quantity}) inserted successfully. $syncStatusMsg"
            )
            showFeedback("Created product ${product.name} (SKU: ${product.sku})")
        }
    }

    fun deactivateProduct(productId: String) {
        viewModelScope.launch {
            repository.deactivateProduct(productId)
            showFeedback("Product deactivated.")
        }
    }

    // Asset Registration action with Photo support
    fun registerAsset(
        code: String,
        name: String,
        category: String,
        serialNumber: String,
        status: AssetStatus,
        warehouseId: String,
        locationId: String,
        assignedEmployee: String,
        imageUrl: String = ""
    ) {
        viewModelScope.launch {
            val asset = AssetEntity(
                assetId = "AST-" + UUID.randomUUID().toString().take(6).uppercase(),
                assetCode = code.trim().uppercase(),
                name = name.trim(),
                category = category,
                serialNumber = serialNumber.trim(),
                status = status,
                warehouseId = warehouseId,
                locationId = locationId,
                assignedEmployeeName = assignedEmployee.trim(),
                purchaseDate = "2026-01-15",
                lastInspectionDate = "2026-10-01",
                nfcTagId = "NFC-${code.trim()}",
                imageUrl = imageUrl.trim()
            )
            repository.saveAsset(asset)
            showFeedback("Registered physical asset: ${asset.name}")
        }
    }

    // Move Asset action
    fun executeAssetMovement(
        assetId: String,
        toLocationId: String,
        newEmployee: String,
        movementType: String,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.recordAssetMovement(
                assetId,
                toLocationId,
                newEmployee,
                movementType,
                notes
            )
            result.onSuccess {
                showFeedback("Asset moved to $toLocationId successfully.")
            }.onFailure { e ->
                showFeedback("Asset movement failed: ${e.message}")
            }
        }
    }

    // User Management actions (Use Case 23)
    fun createUser(fullName: String, username: String, email: String, role: UserRole, warehouseId: String) {
        viewModelScope.launch {
            val user = repository.createUser(fullName, username, email, role, warehouseId)
            showFeedback("Created user account ${user.fullName} (${user.role.name})")
        }
    }

    fun toggleUserActive(userId: String, currentActive: Boolean) {
        viewModelScope.launch {
            repository.toggleUserActive(userId, currentActive)
            showFeedback("User status updated.")
        }
    }

    // Warehouse & Location Creation
    fun createWarehouse(name: String, code: String, address: String, capacity: Int, manager: String) {
        viewModelScope.launch {
            val wh = repository.createWarehouse(name, code, address, capacity, manager)
            showFeedback("Created warehouse ${wh.name} (${wh.code})")
        }
    }

    fun createLocation(warehouseId: String, section: String, rack: String, shelf: String, binCode: String, maxCapacity: Int) {
        viewModelScope.launch {
            val loc = repository.createLocation(warehouseId, section, rack, shelf, binCode, maxCapacity)
            _allLocations.value = repository.getAllLocations()
            showFeedback("Created storage location ${loc.fullHierarchyPath}")
        }
    }

    // Supplier Management
    fun createSupplier(name: String, contactPerson: String, email: String, phone: String, address: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                showFeedback("Supplier name cannot be empty.")
                return@launch
            }
            val sup = repository.createSupplier(name, contactPerson, email, phone, address)
            showFeedback("Registered supplier: ${sup.name}")
        }
    }

    fun deleteSupplier(supplierId: String) {
        viewModelScope.launch {
            repository.deleteSupplier(supplierId)
            showFeedback("Supplier removed.")
        }
    }

    // Sync queue execution
    val isSyncing = MutableStateFlow(false)

    fun triggerSyncNow() {
        if (isSyncing.value) return
        if (!isNetworkConnected.value) {
            showFeedback("Cannot sync while offline. Enable network simulation first.")
            return
        }

        viewModelScope.launch {
            isSyncing.value = true
            val count = repository.processSyncQueue()
            isSyncing.value = false
            showFeedback("Sync complete. $count operations sent to server.")
        }
    }

    fun clearCompletedSyncs() {
        viewModelScope.launch {
            repository.clearCompletedSyncItems()
            showFeedback("Cleared completed sync history.")
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // Remote REST Server & Firebase Cloud configuration
    val remoteApiUrl = MutableStateFlow(repository.getRemoteBaseUrl())
    val remotePingResult = MutableStateFlow<String?>(null)
    val isTestingConnection = MutableStateFlow(false)

    fun updateRemoteApiUrl(url: String) {
        repository.updateRemoteBaseUrl(url)
        remoteApiUrl.value = url
        showFeedback("Saved Remote API URL: $url")
    }

    fun testRemoteConnection() {
        viewModelScope.launch {
            isTestingConnection.value = true
            val result = repository.pingRemoteServer()
            isTestingConnection.value = false
            val summary = if (result.isReachable) {
                "Online (${result.latencyMs}ms): ${result.message}"
            } else {
                "Unreachable (${result.latencyMs}ms): ${result.message}"
            }
            remotePingResult.value = summary
            showFeedback(summary)
        }
    }
}

/**
 * Factory for creating WarehouseViewModel with repository dependency.
 */
class WarehouseViewModelFactory(
    private val repository: WarehouseRepository,
    private val context: android.content.Context? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WarehouseViewModel::class.java)) {
            return WarehouseViewModel(repository = repository, context = context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
