package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AssetStatus
import com.example.domain.model.OperationType
import com.example.domain.model.SyncStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserRole

/**
 * Stores authenticated employee and manager profiles locally for offline authorization.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val warehouseAssignedId: String,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)

/**
 * Categorizes inventory items (e.g. Raw Materials, Finished Goods, Electronics, Hardware).
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val categoryId: String,
    val name: String,
    val code: String,
    val description: String
)

/**
 * Tracks third-party vendors and suppliers providing inventory stock.
 */
@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey
    val supplierId: String,
    val name: String,
    val contactPerson: String,
    val email: String,
    val phone: String,
    val address: String
)

/**
 * Represents physical warehouse facilities managed by the enterprise.
 */
@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey
    val warehouseId: String,
    val name: String,
    val code: String,
    val address: String,
    val totalCapacityUnits: Int,
    val managerName: String
)

/**
 * Represents granular hierarchical storage locations:
 * Warehouse -> Section -> Rack -> Shelf -> Storage Location (Bin/Aisle).
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey
    val locationId: String,
    val warehouseId: String,
    val section: String,
    val rack: String,
    val shelf: String,
    val binCode: String,
    val maxCapacity: Int,
    val currentOccupancy: Int = 0
) {
    val fullHierarchyPath: String
        get() = "Sec $section • Rack $rack • Sh $shelf • Bin $binCode"
}

/**
 * Master catalog item record for goods stored across warehouses.
 * Includes barcode/QR mapping, stock levels, safety thresholds, and expiration dates.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val productId: String,
    val sku: String,
    val barcode: String,
    val qrCode: String,
    val name: String,
    val description: String,
    val categoryId: String,
    val supplierId: String,
    val unitPrice: Double,
    val quantity: Int,
    val minimumStockLevel: Int,
    val expiryDate: String, // YYYY-MM-DD
    val locationId: String,
    val warehouseId: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Immutable ledger of every physical stock addition, deduction, or warehouse transfer.
 * Ensures an auditable chain of custody.
 */
@Entity(tableName = "stock_transactions")
data class StockTransactionEntity(
    @PrimaryKey
    val transactionId: String,
    val productId: String,
    val productName: String,
    val transactionType: TransactionType,
    val quantity: Int,
    val sourceLocationId: String,
    val destinationLocationId: String,
    val performedByUserId: String,
    val performedByUserName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceNumber: String,
    val notes: String = "",
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

/**
 * Tracks high-value physical equipment (forklifts, scanners, pallet jacks, testing tools)
 * distinct from consumable inventory stock.
 */
@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey
    val assetId: String,
    val assetCode: String,
    val name: String,
    val category: String,
    val serialNumber: String,
    val status: AssetStatus,
    val warehouseId: String,
    val locationId: String,
    val assignedEmployeeName: String = "",
    val condition: String = "Good",
    val purchaseDate: String,
    val lastInspectionDate: String,
    val nfcTagId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Historical record of asset movements and reassignments between employees or warehouse zones.
 */
@Entity(tableName = "asset_movements")
data class AssetMovementEntity(
    @PrimaryKey
    val movementId: String,
    val assetId: String,
    val assetName: String,
    val fromLocationId: String,
    val toLocationId: String,
    val previousEmployee: String,
    val newEmployee: String,
    val movementType: String,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

/**
 * Offline Synchronization Queue table.
 * Holds operations initiated while offline or pending backend confirmation.
 * Contains idempotency tokens and retry counters to prevent duplicate server actions.
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey
    val syncId: String,
    val entityType: String,
    val entityId: String,
    val operationType: OperationType,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: SyncStatus = SyncStatus.PENDING,
    val lastAttemptAt: Long = 0L,
    val errorMessage: String = "",
    val idempotencyKey: String
)

/**
 * System audit log for compliance, tracking user actions across products, stock, and assets.
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey
    val eventId: String,
    val userId: String,
    val userName: String,
    val eventType: String,
    val entityType: String,
    val entityId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)

/**
 * Local operational alerts and notification items (Low stock warnings, expiry notices).
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val notificationId: String,
    val title: String,
    val message: String,
    val type: String, // LOW_STOCK, EXPIRY, TRANSFER, SYSTEM
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val referenceId: String = ""
)
