package com.example.domain.model

/**
 * Defines the user authorization roles in the smart warehouse system.
 * - ADMIN: Full access to users, warehouses, product master data, and analytics.
 * - WAREHOUSE_MANAGER: Access to inventory transactions, transfers, assets, and approvals.
 * - WAREHOUSE_EMPLOYEE: Dedicated scanning, stock-in, stock-out, and asset movement operations.
 */
enum class UserRole {
    ADMIN,
    WAREHOUSE_MANAGER,
    WAREHOUSE_EMPLOYEE
}

/**
 * Categorizes inventory transactions for accurate double-entry ledger tracking.
 */
enum class TransactionType {
    STOCK_IN,
    STOCK_OUT,
    STOCK_TRANSFER
}

/**
 * Tracks the lifecycle and maintenance availability of physical warehouse equipment and assets.
 */
enum class AssetStatus {
    AVAILABLE,
    ASSIGNED,
    IN_USE,
    UNDER_MAINTENANCE,
    DAMAGED,
    RETIRED
}

/**
 * Represents the offline synchronization lifecycle of a queued change.
 */
enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    SYNCED,
    FAILED
}

/**
 * Distinguishes the specific REST action required when synchronizing queued local changes.
 */
enum class OperationType {
    CREATE,
    UPDATE,
    DELETE,
    STOCK_IN,
    STOCK_OUT,
    TRANSFER,
    ASSET_MOVE
}

/**
 * Derived stock health status used for dashboard indicators and warnings.
 */
enum class StockLevelStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK,
    OVERSTOCKED
}
