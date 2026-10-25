package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitialDataSeeder
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
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockTransactionEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WarehouseEntity
import com.example.data.repository.WarehouseRepository
import com.example.domain.model.AssetStatus
import com.example.domain.model.OperationType
import com.example.domain.model.SyncStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao
    private lateinit var assetDao: AssetDao
    private lateinit var syncQueueDao: SyncQueueDao
    private lateinit var warehouseDao: WarehouseDao
    private lateinit var stockTransactionDao: StockTransactionDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = db.productDao()
        assetDao = db.assetDao()
        syncQueueDao = db.syncQueueDao()
        warehouseDao = db.warehouseDao()
        stockTransactionDao = db.stockTransactionDao()
        userDao = db.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Warehouse", appName)
    }

    @Test
    fun `test product dao insert query update and search`() = runBlocking {
        val item = ProductEntity(
            productId = "PRD-TEST-001",
            sku = "SKU-TEST-001",
            barcode = "BAR-112233",
            qrCode = "QR-112233",
            name = "Wireless Barcode Scanner Handheld",
            description = "Rugged industrial handheld scanner",
            categoryId = "CAT-ELEC",
            supplierId = "SUP-01",
            unitPrice = 149.99,
            quantity = 25,
            minimumStockLevel = 10,
            expiryDate = "2029-12-31",
            locationId = "LOC-WH1-A1",
            warehouseId = "WH-NORTH",
            imageUrl = "https://images.unsplash.com/photo-example"
        )

        // Insert
        productDao.insertProduct(item)

        // Query by ID
        val retrieved = productDao.getProductById("PRD-TEST-001")
        assertNotNull(retrieved)
        assertEquals("Wireless Barcode Scanner Handheld", retrieved?.name)
        assertEquals(25, retrieved?.quantity)

        // Query by barcode
        val byBarcode = productDao.findProductByCode("BAR-112233")
        assertNotNull(byBarcode)
        assertEquals("SKU-TEST-001", byBarcode?.sku)

        // Search by name keyword
        val searchResults = productDao.searchProducts("Handheld").first()
        assertEquals(1, searchResults.size)
        assertEquals("PRD-TEST-001", searchResults[0].productId)

        // Update stock
        productDao.updateProductStock("PRD-TEST-001", 30)
        val updated = productDao.getProductById("PRD-TEST-001")
        assertEquals(30, updated?.quantity)

        // Flow observation
        val allProducts = productDao.observeAllProducts().first()
        assertEquals(1, allProducts.size)

        // Delete
        productDao.deleteProductById("PRD-TEST-001")
        val deleted = productDao.getProductById("PRD-TEST-001")
        assertNull(deleted)
    }

    @Test
    fun `test asset dao insert observe by status and record movement`() = runBlocking {
        val asset = AssetEntity(
            assetId = "AST-TEST-001",
            assetCode = "FL-001",
            name = "Toyota Electric Forklift 3-Ton",
            category = "HEAVY_MACHINERY",
            serialNumber = "TY-FL-8921",
            status = AssetStatus.AVAILABLE,
            warehouseId = "WH-NORTH",
            locationId = "LOC-WH1-DOCK1",
            assignedEmployeeName = "",
            condition = "Excellent",
            purchaseDate = "2024-03-15",
            lastInspectionDate = "2026-09-01",
            nfcTagId = "NFC-FL-001",
            imageUrl = "https://images.unsplash.com/photo-forklift"
        )

        // Insert asset
        assetDao.insertAsset(asset)

        // Find by NFC tag ID
        val byNfc = assetDao.findAssetByCode("NFC-FL-001")
        assertNotNull(byNfc)
        assertEquals("Toyota Electric Forklift 3-Ton", byNfc?.name)

        // Observe by status
        val availableAssets = assetDao.observeAssetsByStatus(AssetStatus.AVAILABLE).first()
        assertEquals(1, availableAssets.size)
        assertEquals("AST-TEST-001", availableAssets[0].assetId)

        // Record movement
        val movement = AssetMovementEntity(
            movementId = "MOV-TEST-01",
            assetId = "AST-TEST-001",
            assetName = "Toyota Electric Forklift 3-Ton",
            fromLocationId = "LOC-WH1-DOCK1",
            toLocationId = "LOC-WH1-AISLE2",
            previousEmployee = "",
            newEmployee = "Alex Chen",
            movementType = "ASSIGNMENT",
            performedBy = "Warehouse Manager",
            notes = "Assigned for morning staging"
        )
        assetDao.insertMovement(movement)

        // Update asset assignment
        val updatedAsset = asset.copy(
            status = AssetStatus.ASSIGNED,
            assignedEmployeeName = "Alex Chen",
            locationId = "LOC-WH1-AISLE2"
        )
        assetDao.updateAsset(updatedAsset)

        val retrievedUpdated = assetDao.getAssetById("AST-TEST-001")
        assertEquals(AssetStatus.ASSIGNED, retrievedUpdated?.status)
        assertEquals("Alex Chen", retrievedUpdated?.assignedEmployeeName)

        // Observe movements
        val movements = assetDao.observeMovementsForAsset("AST-TEST-001").first()
        assertEquals(1, movements.size)
        assertEquals("Alex Chen", movements[0].newEmployee)
    }

    @Test
    fun `test offline sync queue persistence`() = runBlocking {
        val syncItem = SyncQueueEntity(
            syncId = "SYNC-001",
            entityType = "PRODUCT",
            entityId = "PRD-TEST-001",
            operationType = OperationType.STOCK_IN,
            payloadJson = "{\"productId\":\"PRD-TEST-001\",\"qty\":10}",
            idempotencyKey = "TXN_TEST_001_IN",
            status = SyncStatus.PENDING
        )

        syncQueueDao.insertSyncItem(syncItem)

        val pendingList = syncQueueDao.observePendingSyncItems().first()
        assertEquals(1, pendingList.size)
        assertEquals("TXN_TEST_001_IN", pendingList[0].idempotencyKey)

        // Update status to synced
        val syncedItem = syncItem.copy(status = SyncStatus.SYNCED)
        syncQueueDao.updateSyncItem(syncedItem)

        val remainingPending = syncQueueDao.observePendingSyncItems().first()
        assertTrue(remainingPending.isEmpty())
    }

    @Test
    fun `test warehouse and location hierarchy and suppliers`() = runBlocking {
        val warehouse = WarehouseEntity(
            warehouseId = "WH-TEST-01",
            name = "Central Distribution Hub",
            code = "CDH-01",
            address = "100 Industrial Pkwy, Chicago, IL",
            totalCapacityUnits = 50000,
            managerName = "David Vance"
        )
        warehouseDao.insertWarehouse(warehouse)

        val retrievedWh = warehouseDao.getWarehouseById("WH-TEST-01")
        assertNotNull(retrievedWh)
        assertEquals("Central Distribution Hub", retrievedWh?.name)

        val location = LocationEntity(
            locationId = "LOC-TEST-SEC1",
            warehouseId = "WH-TEST-01",
            section = "SEC-A",
            rack = "RCK-01",
            shelf = "SHF-02",
            binCode = "BIN-05",
            maxCapacity = 500,
            currentOccupancy = 120
        )
        warehouseDao.insertLocation(location)

        val locs = warehouseDao.observeLocationsForWarehouse("WH-TEST-01").first()
        assertEquals(1, locs.size)
        assertEquals("Sec SEC-A • Rack RCK-01 • Sh SHF-02 • Bin BIN-05", locs[0].fullHierarchyPath)

        // Test Supplier CRUD
        val supplier = SupplierEntity(
            supplierId = "SUP-TEST-01",
            name = "Apex Industrial Hardware",
            contactPerson = "Marcus Cole",
            email = "m.cole@apex.com",
            phone = "+1-555-0199",
            address = "Detroit, MI"
        )
        warehouseDao.insertSupplier(supplier)

        val suppliersList = warehouseDao.observeSuppliers().first()
        assertEquals(1, suppliersList.size)
        assertEquals("Apex Industrial Hardware", suppliersList[0].name)

        warehouseDao.deleteSupplierById("SUP-TEST-01")
        val afterDelete = warehouseDao.observeSuppliers().first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `test stock transaction double entry ledger`() = runBlocking {
        val txn = StockTransactionEntity(
            transactionId = "TXN-TEST-100",
            productId = "PRD-TEST-001",
            productName = "Heavy Duty Casters",
            transactionType = TransactionType.STOCK_IN,
            quantity = 50,
            sourceLocationId = "INCOMING-RECEIPT",
            destinationLocationId = "LOC-WH1-A1",
            performedByUserId = "USR-003",
            performedByUserName = "Marcus Cole",
            referenceNumber = "PO-99412",
            notes = "Delivered via Freight Bay",
            transferStatus = "COMPLETED",
            syncStatus = SyncStatus.SYNCED
        )
        stockTransactionDao.insertTransaction(txn)

        val txns = stockTransactionDao.observeTransactions().first()
        assertEquals(1, txns.size)
        assertEquals(50, txns[0].quantity)
        assertEquals(TransactionType.STOCK_IN, txns[0].transactionType)

        // Pending transfer
        val pendingTxn = txn.copy(
            transactionId = "TXN-TEST-101",
            transactionType = TransactionType.STOCK_TRANSFER,
            transferStatus = "PENDING"
        )
        stockTransactionDao.insertTransaction(pendingTxn)

        val pendingList = stockTransactionDao.observePendingTransfers().first()
        assertEquals(1, pendingList.size)
        assertEquals("TXN-TEST-101", pendingList[0].transactionId)
    }

    @Test
    fun `test user authentication and compliance audit logging`() = runBlocking {
        val user = UserEntity(
            userId = "USR-TEST-01",
            username = "admin_sarah",
            fullName = "Sarah Jenkins",
            email = "s.jenkins@smartwarehouse.net",
            role = UserRole.ADMIN,
            warehouseAssignedId = "WH-NORTH",
            isActive = true
        )
        userDao.insertUser(user)

        val found = userDao.findUserByUsername("admin_sarah")
        assertNotNull(found)
        assertEquals(UserRole.ADMIN, found?.role)

        // Audit Log
        val log = AuditLogEntity(
            eventId = "AUDIT-001",
            userId = "USR-TEST-01",
            userName = "Sarah Jenkins",
            eventType = "PRODUCT_CREATED",
            entityType = "PRODUCT",
            entityId = "PRD-TEST-001",
            details = "Product created with initial quantity 25"
        )
        userDao.insertAuditLog(log)

        val logs = userDao.observeAuditLogs().first()
        assertEquals(1, logs.size)
        assertEquals("PRODUCT_CREATED", logs[0].eventType)
    }

    @Test
    fun `test database master data seeder`() = runBlocking {
        DatabaseInitialDataSeeder.seedInitialData(db)

        val allProducts = productDao.getAllProducts()
        assertTrue(allProducts.isNotEmpty())

        val warehouses = warehouseDao.observeWarehouses().first()
        assertTrue(warehouses.isNotEmpty())

        val assets = assetDao.observeAssets().first()
        assertTrue(assets.isNotEmpty())

        val suppliers = warehouseDao.observeSuppliers().first()
        assertTrue(suppliers.isNotEmpty())

        val transactions = stockTransactionDao.observeTransactions().first()
        assertTrue(transactions.isNotEmpty())
    }

    @Test
    fun `test warehouse repository end to end stock operations`() = runBlocking {
        DatabaseInitialDataSeeder.seedInitialData(db)
        val repository = WarehouseRepository(db)

        val initialProducts = repository.observeAllProducts().first()
        val firstProduct = initialProducts.first()
        val initialQty = firstProduct.quantity

        // Perform stock in
        val result = repository.stockIn(
            productId = firstProduct.productId,
            quantity = 20,
            destinationLocationId = firstProduct.locationId,
            referenceNumber = "PO-TEST",
            notes = "Test restock"
        )
        assertTrue(result.isSuccess)

        val updatedProduct = productDao.getProductById(firstProduct.productId)
        assertEquals(initialQty + 20, updatedProduct?.quantity)

        // Perform stock out
        val outResult = repository.stockOut(
            productId = firstProduct.productId,
            quantity = 5,
            destinationNotes = "Dispatch Bay 2",
            referenceNumber = "DISP-001",
            notes = "Test dispatch"
        )
        assertTrue(outResult.isSuccess)

        val finalProduct = productDao.getProductById(firstProduct.productId)
        assertEquals(initialQty + 15, finalProduct?.quantity)
    }

    @Test
    fun `test firebase auth email and password validation`() {
        assertTrue(com.example.data.auth.FirebaseAuthManager.isValidEmail("employee@warehouse.com"))
        assertTrue(com.example.data.auth.FirebaseAuthManager.isValidEmail("admin_123@smartwarehouse.net"))

        assertFalse(com.example.data.auth.FirebaseAuthManager.isValidEmail(""))
        assertFalse(com.example.data.auth.FirebaseAuthManager.isValidEmail("invalid-email"))
        assertFalse(com.example.data.auth.FirebaseAuthManager.isValidEmail("@domain.com"))

        val (valid, _) = com.example.data.auth.FirebaseAuthManager.validatePassword("secret123")
        assertTrue(valid)

        val (invalid, errorMsg) = com.example.data.auth.FirebaseAuthManager.validatePassword("123")
        assertFalse(invalid)
        assertNotNull(errorMsg)
    }

    @Test
    fun `test ml kit barcode options builder`() {
        val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS)
            .build()
        assertNotNull(options)
    }
}
