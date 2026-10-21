package com.example.data.local

import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.AssetMovementEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockTransactionEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WarehouseEntity
import com.example.domain.model.AssetStatus
import com.example.domain.model.SyncStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserRole

/**
 * Seeds comprehensive initial enterprise data for demo, testing, and offline verification.
 */
object DatabaseInitialDataSeeder {
    suspend fun seedInitialData(database: AppDatabase) {
        val userDao = database.userDao()
        val warehouseDao = database.warehouseDao()
        val productDao = database.productDao()
        val assetDao = database.assetDao()
        val transactionDao = database.stockTransactionDao()

        // 1. Users
        val users = listOf(
            UserEntity(
                userId = "USR-001",
                username = "admin_sarah",
                fullName = "Sarah Jenkins",
                email = "s.jenkins@smartwarehouse.net",
                role = UserRole.ADMIN,
                warehouseAssignedId = "WH-NORTH"
            ),
            UserEntity(
                userId = "USR-002",
                username = "mgr_david",
                fullName = "David Vance",
                email = "d.vance@smartwarehouse.net",
                role = UserRole.WAREHOUSE_MANAGER,
                warehouseAssignedId = "WH-NORTH"
            ),
            UserEntity(
                userId = "USR-003",
                username = "emp_marcus",
                fullName = "Marcus Cole",
                email = "m.cole@smartwarehouse.net",
                role = UserRole.WAREHOUSE_EMPLOYEE,
                warehouseAssignedId = "WH-NORTH"
            )
        )
        userDao.insertUsers(users)

        // 2. Warehouses
        val warehouses = listOf(
            WarehouseEntity(
                warehouseId = "WH-NORTH",
                name = "North Distribution Center",
                code = "NDC-01",
                address = "104 Logistics Blvd, Industrial Zone A",
                totalCapacityUnits = 25000,
                managerName = "David Vance"
            ),
            WarehouseEntity(
                warehouseId = "WH-CENTRAL",
                name = "Central Logistics Hub",
                code = "CLH-02",
                address = "520 Parkway Interchange, Sector 7",
                totalCapacityUnits = 50000,
                managerName = "Elena Rostova"
            ),
            WarehouseEntity(
                warehouseId = "WH-SOUTH",
                name = "South Port Fulfillment Yard",
                code = "SPF-03",
                address = "88 Harbor Terminal Pier, Dock 12",
                totalCapacityUnits = 32000,
                managerName = "Kofi Mensah"
            )
        )
        warehouseDao.insertWarehouses(warehouses)

        // 3. Hierarchical Storage Locations
        val locations = listOf(
            LocationEntity("LOC-WH1-A11", "WH-NORTH", "A", "R01", "S01", "BIN-A01", 500, 320),
            LocationEntity("LOC-WH1-A12", "WH-NORTH", "A", "R01", "S02", "BIN-A02", 500, 180),
            LocationEntity("LOC-WH1-B21", "WH-NORTH", "B", "R02", "S01", "BIN-B01", 800, 640),
            LocationEntity("LOC-WH1-B22", "WH-NORTH", "B", "R02", "S02", "BIN-B02", 800, 210),
            LocationEntity("LOC-WH1-C31", "WH-NORTH", "C", "R03", "S01", "BIN-C01", 1200, 890),
            LocationEntity("LOC-WH1-D41", "WH-NORTH", "D", "R04", "S01", "BIN-D01", 600, 50),
            LocationEntity("LOC-WH2-A11", "WH-CENTRAL", "A", "R01", "S01", "BIN-01", 1000, 450),
            LocationEntity("LOC-WH2-B11", "WH-CENTRAL", "B", "R01", "S01", "BIN-02", 1000, 720)
        )
        warehouseDao.insertLocations(locations)

        // 4. Categories
        val categories = listOf(
            CategoryEntity("CAT-01", "Industrial Hardware", "IND-HW", "Heavy mechanical fittings, casters, fasteners"),
            CategoryEntity("CAT-02", "Electronics & IoT", "ELEC-IOT", "Scanners, RFID readers, telemetry sensors"),
            CategoryEntity("CAT-03", "Packaging & Materials", "PKG-MAT", "Label rolls, corrugated boxes, shrink wrap"),
            CategoryEntity("CAT-04", "Heavy Batteries & Energy", "BAT-NRG", "Forklift cells, charging packs, invertors"),
            CategoryEntity("CAT-05", "Safety & PPE", "PPE-SAF", "Hard hats, safety vests, eye protection")
        )
        warehouseDao.insertCategories(categories)

        // 5. Suppliers
        val suppliers = listOf(
            SupplierEntity("SUP-01", "Apex Heavy Logistics", "Robert King", "sales@apexlogistics.com", "+1-800-555-0199", "Detroit, MI"),
            SupplierEntity("SUP-02", "Titan Industrial Supplies", "Maria Diaz", "orders@titansupply.com", "+1-800-555-0245", "Chicago, IL"),
            SupplierEntity("SUP-03", "NexGen Microcircuits", "Kenji Sato", "b2b@nexgen-elec.com", "+1-800-555-0371", "San Jose, CA")
        )
        warehouseDao.insertSuppliers(suppliers)

        // 6. Products Master Catalog
        val products = listOf(
            ProductEntity(
                productId = "PRD-001",
                sku = "HD-WHL-882",
                barcode = "78912345601",
                qrCode = "PRD-HD-WHL-882",
                name = "Heavy-Duty Pallet Caster Wheels",
                description = "Polyurethane 8-inch steel bearing swivel wheels for heavy pallet transport.",
                categoryId = "CAT-01",
                supplierId = "SUP-01",
                unitPrice = 45.50,
                quantity = 142,
                minimumStockLevel = 40,
                expiryDate = "2029-12-31",
                locationId = "LOC-WH1-A11",
                warehouseId = "WH-NORTH"
            ),
            ProductEntity(
                productId = "PRD-002",
                sku = "BCD-TRM-401",
                barcode = "78912345602",
                qrCode = "PRD-BCD-TRM-401",
                name = "Industrial 2D Barcode Scanner Terminals",
                description = "Ruggedized Android handheld imager with long-range laser autofocus.",
                categoryId = "CAT-02",
                supplierId = "SUP-03",
                unitPrice = 420.00,
                quantity = 18,
                minimumStockLevel = 25, // Low stock triggered!
                expiryDate = "2031-06-15",
                locationId = "LOC-WH1-A12",
                warehouseId = "WH-NORTH"
            ),
            ProductEntity(
                productId = "PRD-003",
                sku = "LBL-THM-100",
                barcode = "78912345603",
                qrCode = "PRD-LBL-THM-100",
                name = "Thermal Shipping Label Rolls 4x6 (Pack 6)",
                description = "Direct thermal perforated permanent adhesive label rolls for dispatch.",
                categoryId = "CAT-03",
                supplierId = "SUP-02",
                unitPrice = 28.00,
                quantity = 8, // Very low stock
                minimumStockLevel = 30,
                expiryDate = "2027-04-10",
                locationId = "LOC-WH1-B21",
                warehouseId = "WH-NORTH"
            ),
            ProductEntity(
                productId = "PRD-004",
                sku = "BAT-LION-50",
                barcode = "78912345604",
                qrCode = "PRD-BAT-LION-50",
                name = "Lithium-Ion Forklift Battery 48V",
                description = "Heavy rapid-charge industrial cell pack with smart telemetry BMS module.",
                categoryId = "CAT-04",
                supplierId = "SUP-01",
                unitPrice = 2850.00,
                quantity = 12,
                minimumStockLevel = 5,
                expiryDate = "2032-01-01",
                locationId = "LOC-WH1-C31",
                warehouseId = "WH-NORTH"
            ),
            ProductEntity(
                productId = "PRD-005",
                sku = "RFID-TAG-500",
                barcode = "78912345605",
                qrCode = "PRD-RFID-TAG-500",
                name = "UHF Gen2 RFID Asset Tracking Tags",
                description = "Mount-on-metal durable RFID chips for pallet and container tracking.",
                categoryId = "CAT-02",
                supplierId = "SUP-03",
                unitPrice = 110.00,
                quantity = 85,
                minimumStockLevel = 20,
                expiryDate = "2035-12-31",
                locationId = "LOC-WH1-B22",
                warehouseId = "WH-NORTH"
            ),
            ProductEntity(
                productId = "PRD-006",
                sku = "PPE-HAT-YLW",
                barcode = "78912345606",
                qrCode = "PRD-PPE-HAT-YLW",
                name = "High-Visibility Safety Hard Hats (Yellow)",
                description = "ANSI Z89.1 certified ratchet suspension impact protective helmets.",
                categoryId = "CAT-05",
                supplierId = "SUP-02",
                unitPrice = 19.95,
                quantity = 210,
                minimumStockLevel = 50,
                expiryDate = "2028-11-20",
                locationId = "LOC-WH1-D41",
                warehouseId = "WH-NORTH"
            )
        )
        productDao.insertProducts(products)

        // 7. Physical Assets
        val assets = listOf(
            AssetEntity(
                assetId = "AST-001",
                assetCode = "AST-FL-01",
                name = "Toyota Electric Forklift 8FBE",
                category = "Heavy Equipment",
                serialNumber = "TY-8FBE-994821",
                status = AssetStatus.AVAILABLE,
                warehouseId = "WH-NORTH",
                locationId = "LOC-WH1-A11",
                assignedEmployeeName = "Unassigned",
                condition = "Excellent",
                purchaseDate = "2023-03-15",
                lastInspectionDate = "2026-09-10",
                nfcTagId = "NFC-AST-001"
            ),
            AssetEntity(
                assetId = "AST-002",
                assetCode = "AST-PJ-04",
                name = "Crown Hydraulic Pallet Jack 5500lb",
                category = "Material Handling",
                serialNumber = "CR-PJ55-10492",
                status = AssetStatus.IN_USE,
                warehouseId = "WH-NORTH",
                locationId = "LOC-WH1-B21",
                assignedEmployeeName = "Marcus Cole",
                condition = "Good",
                purchaseDate = "2024-01-20",
                lastInspectionDate = "2026-08-25",
                nfcTagId = "NFC-AST-002"
            ),
            AssetEntity(
                assetId = "AST-003",
                assetCode = "AST-SC-12",
                name = "Zebra TC52 Handheld Barcode Scanner",
                category = "IT & Scanners",
                serialNumber = "ZB-TC52-881923",
                status = AssetStatus.ASSIGNED,
                warehouseId = "WH-NORTH",
                locationId = "LOC-WH1-A12",
                assignedEmployeeName = "Marcus Cole",
                condition = "Good",
                purchaseDate = "2025-05-11",
                lastInspectionDate = "2026-10-01",
                nfcTagId = "NFC-AST-003"
            ),
            AssetEntity(
                assetId = "AST-004",
                assetCode = "AST-SCL-02",
                name = "Fairbanks Industrial Platform Scale 1000kg",
                category = "Inspection & Weighing",
                serialNumber = "FB-1000-44910",
                status = AssetStatus.UNDER_MAINTENANCE,
                warehouseId = "WH-NORTH",
                locationId = "LOC-WH1-D41",
                assignedEmployeeName = "Maintenance Crew",
                condition = "Calibration Pending",
                purchaseDate = "2022-11-04",
                lastInspectionDate = "2026-10-12",
                nfcTagId = "NFC-AST-004"
            )
        )
        assetDao.insertAssets(assets)

        // 8. Asset Movements
        val movements = listOf(
            AssetMovementEntity(
                movementId = "MOV-001",
                assetId = "AST-002",
                assetName = "Crown Hydraulic Pallet Jack 5500lb",
                fromLocationId = "LOC-WH1-A11",
                toLocationId = "LOC-WH1-B21",
                previousEmployee = "Sarah Jenkins",
                newEmployee = "Marcus Cole",
                movementType = "Shift Assignment",
                performedBy = "David Vance",
                notes = "Assigned for Section B freight unload."
            ),
            AssetMovementEntity(
                movementId = "MOV-002",
                assetId = "AST-004",
                assetName = "Fairbanks Industrial Platform Scale 1000kg",
                fromLocationId = "LOC-WH1-B22",
                toLocationId = "LOC-WH1-D41",
                previousEmployee = "Warehouse Floor",
                newEmployee = "Maintenance Crew",
                movementType = "Maintenance Relocation",
                performedBy = "David Vance",
                notes = "Scheduled bi-annual load sensor recalibration."
            )
        )
        movements.forEach { assetDao.insertMovement(it) }

        // 9. Initial Transactions
        val transactions = listOf(
            StockTransactionEntity(
                transactionId = "TXN-9001",
                productId = "PRD-001",
                productName = "Heavy-Duty Pallet Caster Wheels",
                transactionType = TransactionType.STOCK_IN,
                quantity = 50,
                sourceLocationId = "SUPPLIER-APEX",
                destinationLocationId = "LOC-WH1-A11",
                performedByUserId = "USR-003",
                performedByUserName = "Marcus Cole",
                referenceNumber = "PO-APEX-8841",
                notes = "Received standard container delivery, passed QA check.",
                syncStatus = SyncStatus.SYNCED
            ),
            StockTransactionEntity(
                transactionId = "TXN-9002",
                productId = "PRD-003",
                productName = "Thermal Shipping Label Rolls 4x6 (Pack 6)",
                transactionType = TransactionType.STOCK_OUT,
                quantity = 12,
                sourceLocationId = "LOC-WH1-B21",
                destinationLocationId = "DISPATCH-BAY-3",
                performedByUserId = "USR-003",
                performedByUserName = "Marcus Cole",
                referenceNumber = "DISP-2026-104",
                notes = "Dispatched to outbound packaging line 3.",
                syncStatus = SyncStatus.SYNCED
            ),
            StockTransactionEntity(
                transactionId = "TXN-9003",
                productId = "PRD-005",
                productName = "UHF Gen2 RFID Asset Tracking Tags",
                transactionType = TransactionType.STOCK_TRANSFER,
                quantity = 25,
                sourceLocationId = "LOC-WH1-B21",
                destinationLocationId = "LOC-WH1-B22",
                performedByUserId = "USR-002",
                performedByUserName = "David Vance",
                referenceNumber = "XFR-2026-089",
                notes = "Relocated to secure RFID bin.",
                syncStatus = SyncStatus.SYNCED
            )
        )
        transactionDao.insertTransactions(transactions)

        // 10. Notifications
        val notifications = listOf(
            NotificationEntity(
                notificationId = "NTF-01",
                title = "Critical Low Stock Alert",
                message = "Thermal Shipping Label Rolls (SKU: LBL-THM-100) has dropped to 8 units (Threshold: 30). Reorder recommended.",
                type = "LOW_STOCK",
                referenceId = "PRD-003"
            ),
            NotificationEntity(
                notificationId = "NTF-02",
                title = "Hardware Calibration Warning",
                message = "Platform Scale (AST-SCL-02) is currently under maintenance at Section D.",
                type = "ASSET_ALERT",
                referenceId = "AST-004"
            ),
            NotificationEntity(
                notificationId = "NTF-03",
                title = "Transfer Order Approved",
                message = "Stock transfer XFR-2026-089 for 25 RFID tags completed successfully.",
                type = "TRANSFER",
                referenceId = "TXN-9003"
            )
        )
        userDao.insertNotifications(notifications)

        // 11. Initial Audit Log
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = "EVT-001",
                userId = "USR-001",
                userName = "Sarah Jenkins",
                eventType = "SYSTEM_INITIALIZED",
                entityType = "DATABASE",
                entityId = "smart_warehouse.db",
                details = "Master catalog, warehouse hierarchy, and user roles initialized."
            )
        )
    }
}
