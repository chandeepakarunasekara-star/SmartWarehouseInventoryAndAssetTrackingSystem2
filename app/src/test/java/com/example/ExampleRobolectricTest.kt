package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.AssetDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.AssetMovementEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.domain.model.AssetStatus
import com.example.domain.model.OperationType
import com.example.domain.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = db.productDao()
        assetDao = db.assetDao()
        syncQueueDao = db.syncQueueDao()
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
}
