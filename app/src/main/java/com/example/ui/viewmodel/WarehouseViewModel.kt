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
 * Navigation routes supported across the Smart Warehouse application,
 * matching all 28 use cases from the specifications.
 */
sealed class WarehouseScreen {
    data object Login : WarehouseScreen()
    data object Dashboard : WarehouseScreen()
    data object Products : WarehouseScreen()
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
    val nfcManager: NfcTagManager = NfcTagManager()
) : ViewModel() {

    // Current navigation screen
    private val _currentScreen = MutableStateFlow<WarehouseScreen>(WarehouseScreen.Dashboard)
    val currentScreen = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<WarehouseScreen>(WarehouseScreen.Dashboard)

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

    fun logout() {
        navigateTo(WarehouseScreen.Login)
        showFeedback("Logged out successfully.")
    }

    fun toggleNetworkConnection() {
        repository.setNetworkConnected(!isNetworkConnected.value)
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

    fun showFeedback(message: String) {
        statusMessage.value = message
    }

    fun dismissFeedback() {
        statusMessage.value = null
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
                showFeedback("Stock-In successful! Added $quantity units.")
                _allLocations.value = repository.getAllLocations()
            }.onFailure { e ->
                showFeedback("Stock-In failed: ${e.message}")
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
    private val repository: WarehouseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WarehouseViewModel::class.java)) {
            return WarehouseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
