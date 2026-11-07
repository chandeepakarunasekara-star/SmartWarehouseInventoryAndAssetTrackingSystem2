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
 * Seeds comprehensive initial enterprise data according to the Smart Warehouse specification,
 * including photos, user accounts, hierarchical bins, assets, and transactions.
 */
object DatabaseInitialDataSeeder {
    suspend fun updateDefaultUsers(database: AppDatabase) {
        val userDao = database.userDao()
        val defaultUsers = listOf(
            UserEntity(
                userId = "USR-001",
                username = "admin_sarah",
                fullName = "Chandeepa karunasekara",
                email = "chandeepa@smartwarehouse.net",
                role = UserRole.ADMIN,
                warehouseAssignedId = "WH-NORTH",
                isActive = true
            ),
            UserEntity(
                userId = "USR-002",
                username = "mgr_david",
                fullName = "Nimal Rathnayake",
                email = "nimal@smartwarehouse.net",
                role = UserRole.WAREHOUSE_MANAGER,
                warehouseAssignedId = "WH-NORTH",
                isActive = true
            ),
            UserEntity(
                userId = "USR-003",
                username = "emp_104",
                fullName = "Sarath pathirana",
                email = "sarath@smartwarehouse.net",
                role = UserRole.WAREHOUSE_EMPLOYEE,
                warehouseAssignedId = "WH-NORTH",
                isActive = true
            ),
            UserEntity(
                userId = "USR-004",
                username = "emp_alex",
                fullName = "Alex Rivera",
                email = "a.rivera@smartwarehouse.net",
                role = UserRole.WAREHOUSE_EMPLOYEE,
                warehouseAssignedId = "WH-CENTRAL",
                isActive = true
            )
        )
        userDao.insertUsers(defaultUsers)
    }

    suspend fun seedInitialData(database: AppDatabase) {
        val userDao = database.userDao()
        val warehouseDao = database.warehouseDao()
        val productDao = database.productDao()
        val assetDao = database.assetDao()
        val transactionDao = database.stockTransactionDao()

        // 1. Users with distinct role hierarchies
        updateDefaultUsers(database)

        // 2. Warehouses
        val warehouses = listOf(
            WarehouseEntity(
                warehouseId = "WH-NORTH",
                name = "Warehouse A (North Distribution)",
                code = "WH-A",
                address = "104 Logistics Blvd, Industrial Sector 4",
                totalCapacityUnits = 25000,
                managerName = "Nimal Rathnayake"
            ),
            WarehouseEntity(
                warehouseId = "WH-CENTRAL",
                name = "Warehouse B (Central Logistics)",
                code = "WH-B",
                address = "520 Parkway Interchange, Hub 7",
                totalCapacityUnits = 50000,
                managerName = "Elena Rostova"
            ),
            WarehouseEntity(
                warehouseId = "WH-SOUTH",
                name = "Warehouse C (South Terminal)",
                code = "WH-C",
                address = "88 Harbor Terminal Pier, Dock 12",
                totalCapacityUnits = 32000,
                managerName = "Kofi Mensah"
            )
        )
        warehouseDao.insertWarehouses(warehouses)

        // 3. Hierarchical Storage Locations: Warehouse -> Section -> Rack -> Shelf -> Bin
        val locations = listOf(
            LocationEntity("LOC-WHA-B-R03-S02", "WH-NORTH", "B", "03", "02", "BIN-B03-2", 600, 245),
            LocationEntity("LOC-WHA-B-R04-S02", "WH-NORTH", "B", "04", "02", "BIN-B04-2", 800, 500),
            LocationEntity("LOC-WHA-C-R01-S05", "WH-NORTH", "C", "01", "05", "BIN-C01-5", 750, 180),
            LocationEntity("LOC-WHA-A-R01-S01", "WH-NORTH", "A", "01", "01", "BIN-A01-1", 500, 320),
            LocationEntity("LOC-WHA-A-R02-S03", "WH-NORTH", "A", "02", "03", "BIN-A02-3", 500, 142),
            LocationEntity("LOC-WHA-D-R01-S01", "WH-NORTH", "D", "01", "01", "BIN-D01-1", 400, 50),
            LocationEntity("LOC-WHB-A-R01-S01", "WH-CENTRAL", "A", "01", "01", "BIN-CENT-01", 1000, 620)
        )
        warehouseDao.insertLocations(locations)

        // 4. Categories
        val categories = listOf(
            CategoryEntity("CAT-01", "Electronics & Computing", "ELEC", "Laptops, chargers, barcode terminals, accessories"),
            CategoryEntity("CAT-02", "Industrial Hardware", "IND-HW", "Fittings, casters, steel bearings, tools"),
            CategoryEntity("CAT-03", "Packaging & Dispatch", "PKG-MAT", "Label rolls, tape, corrugated shipping supplies"),
            CategoryEntity("CAT-04", "Heavy Batteries & Power", "BAT-NRG", "Forklift cells, charging packs, invertors"),
            CategoryEntity("CAT-05", "Chemicals & Solvents", "CHM-SOLV", "Industrial cleaners, lubricants, solvents")
        )
        warehouseDao.insertCategories(categories)

        // 5. Suppliers
        val suppliers = listOf(
            SupplierEntity("SUP-01", "ABC Suppliers Ltd", "Thomas Bradley", "orders@abcsuppliers.com", "+1-800-555-0100", "Chicago, IL"),
            SupplierEntity("SUP-02", "Apex Heavy Logistics", "Robert King", "sales@apexlogistics.com", "+1-800-555-0199", "Detroit, MI"),
            SupplierEntity("SUP-03", "Titan Industrial Supplies", "Maria Diaz", "orders@titansupply.com", "+1-800-555-0245", "Houston, TX")
        )
        warehouseDao.insertSuppliers(suppliers)

        // 6. Products Master Catalog with Photos & Specifications from PDF
        val products = listOf(
            ProductEntity(
                productId = "PRD-00125",
                sku = "CHG-100",
                barcode = "78912345601",
                qrCode = "PRD-CHG-100",
                name = "Laptop Charger 65W USB-C",
                description = "Universal fast-charging warehouse terminal and rugged laptop AC power supply.",
                categoryId = "CAT-01",
                supplierId = "SUP-01",
                unitPrice = 34.50,
                quantity = 45,
                minimumStockLevel = 20,
                expiryDate = "2030-12-31",
                locationId = "LOC-WHA-B-R03-S02",
                warehouseId = "WH-NORTH",
                imageUrl = "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=400&q=80"
            ),
            ProductEntity(
                productId = "PRD-00500",
                sku = "LAP-100",
                barcode = "78912345602",
                qrCode = "PRD-LAP-100",
                name = "Rugged Enterprise Laptop 15-inch",
                description = "Drop-resistant IP65 warehouse logistics supervisor terminal.",
                categoryId = "CAT-01",
                supplierId = "SUP-01",
                unitPrice = 850.00,
                quantity = 100, // Stock in shipment demo
                minimumStockLevel = 50,
                expiryDate = "2032-05-20",
                locationId = "LOC-WHA-B-R04-S02",
                warehouseId = "WH-NORTH",
                imageUrl = "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=400&q=80"
            ),
            ProductEntity(
                productId = "PRD-00012",
                sku = "USB-CBL-01",
                barcode = "78912345603",
                qrCode = "PRD-USB-CBL-01",
                name = "High-Durability USB Cable 2M",
                description = "Braided high-current data transfer and mobile scanner sync cables.",
                categoryId = "CAT-01",
                supplierId = "SUP-01",
                unitPrice = 8.50,
                quantity = 12, // LOW STOCK (12 < 20)
                minimumStockLevel = 20,
                expiryDate = "2031-10-15",
                locationId = "LOC-WHA-A-R01-S01",
                warehouseId = "WH-NORTH",
                imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=400&q=80"
            ),
            ProductEntity(
                productId = "PRD-00099",
                sku = "CHM-SOLV-X",
                barcode = "78912345604",
                qrCode = "PRD-CHM-SOLV-X",
                name = "Industrial Chemical Cleaner X",
                description = "Heavy degreasing formulation for industrial rollers and belts.",
                categoryId = "CAT-05",
                supplierId = "SUP-03",
                unitPrice = 62.00,
                quantity = 28,
                minimumStockLevel = 10,
                expiryDate = "2026-11-05", // Approaching Expiry!
                locationId = "LOC-WHA-D-R01-S01",
                warehouseId = "WH-NORTH",
                imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=400&q=80"
            ),
            ProductEntity(
                productId = "PRD-00201",
                sku = "LBL-THM-100",
                barcode = "78912345605",
                qrCode = "PRD-LBL-THM-100",
                name = "Thermal Shipping Label Rolls 4x6",
                description = "High-speed thermal transfer self-adhesive labels for package dispatch.",
                categoryId = "CAT-03",
                supplierId = "SUP-02",
                unitPrice = 24.00,
                quantity = 15, // LOW STOCK
                minimumStockLevel = 40,
                expiryDate = "2027-04-10",
                locationId = "LOC-WHA-A-R02-S03",
                warehouseId = "WH-NORTH",
                imageUrl = "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?auto=format&fit=crop&w=400&q=80"
            )
        )
        productDao.insertProducts(products)

        // 7. Physical Assets with Photos (from PDF page 9-10)
        val assets = listOf(
            AssetEntity(
                assetId = "AST-000245",
                assetCode = "AST-000245",
                name = "Electric Forklift Toyota 8FBE",
                category = "Heavy Equipment",
                serialNumber = "FL-9821",
                status = AssetStatus.AVAILABLE,
                warehouseId = "WH-NORTH",
                locationId = "LOC-WHA-A-R01-S01",
                assignedEmployeeName = "Sarath pathirana",
                condition = "Excellent",
                purchaseDate = "2023-03-15",
                lastInspectionDate = "2026-09-10",
                nfcTagId = "NFC-AST-000245",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80"
            ),
            AssetEntity(
                assetId = "AST-000104",
                assetCode = "AST-PJ-04",
                name = "Crown Hydraulic Pallet Truck 5500lb",
                category = "Pallet Trucks",
                serialNumber = "CR-PJ-1049",
                status = AssetStatus.IN_USE,
                warehouseId = "WH-NORTH",
                locationId = "LOC-WHA-B-R03-S02",
                assignedEmployeeName = "Sarath pathirana",
                condition = "Good",
                purchaseDate = "2024-01-20",
                lastInspectionDate = "2026-08-25",
                nfcTagId = "NFC-AST-PJ04",
                imageUrl = "https://images.unsplash.com/photo-1580674684081-7617fbf3d745?auto=format&fit=crop&w=400&q=80"
            ),
            AssetEntity(
                assetId = "AST-000301",
                assetCode = "AST-SC-12",
                name = "Zebra TC52 Handheld Barcode Scanner",
                category = "Scanners",
                serialNumber = "ZB-TC52-8819",
                status = AssetStatus.ASSIGNED,
                warehouseId = "WH-NORTH",
                locationId = "LOC-WHA-A-R02-S03",
                assignedEmployeeName = "Sarath pathirana",
                condition = "Calibrated",
                purchaseDate = "2025-05-11",
                lastInspectionDate = "2026-10-01",
                nfcTagId = "NFC-AST-SC12",
                imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80"
            ),
            AssetEntity(
                assetId = "AST-000405",
                assetCode = "AST-PR-02",
                name = "Zebra Industrial Label Printer ZT411",
                category = "Printers",
                serialNumber = "ZB-PR-41102",
                status = AssetStatus.AVAILABLE,
                warehouseId = "WH-NORTH",
                locationId = "LOC-WHA-A-R02-S03",
                assignedEmployeeName = "Unassigned",
                condition = "Good",
                purchaseDate = "2024-09-18",
                lastInspectionDate = "2026-09-30",
                nfcTagId = "NFC-AST-PR02",
                imageUrl = "https://images.unsplash.com/photo-1612815154858-60aa4c59eaa6?auto=format&fit=crop&w=400&q=80"
            )
        )
        assetDao.insertAssets(assets)

        // 8. Asset Movement History Timeline (as specified in PDF page 10)
        val movements = listOf(
            AssetMovementEntity(
                movementId = "MOV-001",
                assetId = "AST-000245",
                assetName = "Electric Forklift Toyota 8FBE",
                fromLocationId = "LOC-WHA-A-R01-S01",
                toLocationId = "LOC-WHA-B-R03-S02",
                previousEmployee = "Chandeepa karunasekara",
                newEmployee = "Sarath pathirana",
                movementType = "Shift Assignment",
                performedBy = "Nimal Rathnayake (Manager)",
                notes = "08:30 Shift Start: Transferred to Section B for pallet unloading."
            ),
            AssetMovementEntity(
                movementId = "MOV-002",
                assetId = "AST-000245",
                assetName = "Electric Forklift Toyota 8FBE",
                fromLocationId = "LOC-WHA-B-R03-S02",
                toLocationId = "LOC-WHA-C-R01-S05",
                previousEmployee = "Sarath pathirana",
                newEmployee = "Sarath pathirana",
                movementType = "Zone Relocation",
                performedBy = "Sarath pathirana",
                notes = "11:15 Relocated to Warehouse A / Section C for dispatch staging."
            )
        )
        movements.forEach { assetDao.insertMovement(it) }

        // 9. Initial Transactions
        val transactions = listOf(
            StockTransactionEntity(
                transactionId = "TXN-1001",
                productId = "PRD-00125",
                productName = "Laptop Charger 65W USB-C",
                transactionType = TransactionType.STOCK_IN,
                quantity = 100,
                sourceLocationId = "ABC Suppliers Delivery",
                destinationLocationId = "LOC-WHA-B-R03-S02",
                performedByUserId = "USR-003",
                performedByUserName = "Sarath pathirana",
                referenceNumber = "PO-ABC-2026",
                notes = "Shipment received and verified.",
                transferStatus = "COMPLETED",
                syncStatus = SyncStatus.SYNCED
            ),
            StockTransactionEntity(
                transactionId = "TXN-1002",
                productId = "PRD-00125",
                productName = "Laptop Charger 65W USB-C",
                transactionType = TransactionType.STOCK_OUT,
                quantity = 15,
                sourceLocationId = "LOC-WHA-B-R03-S02",
                destinationLocationId = "Dispatch Bay 3",
                performedByUserId = "USR-003",
                performedByUserName = "Sarath pathirana",
                referenceNumber = "DISP-8821",
                notes = "Dispatched to Retail Store #4.",
                transferStatus = "COMPLETED",
                syncStatus = SyncStatus.SYNCED
            ),
            StockTransactionEntity(
                transactionId = "TXN-1003",
                productId = "PRD-00125",
                productName = "Laptop Charger 65W USB-C",
                transactionType = TransactionType.STOCK_TRANSFER,
                quantity = 20,
                sourceLocationId = "LOC-WHA-B-R04-S02",
                destinationLocationId = "LOC-WHA-C-R01-S05",
                performedByUserId = "USR-002",
                performedByUserName = "Nimal Rathnayake (Manager)",
                referenceNumber = "XFR-2026-04",
                notes = "Transfer from Section B to Section C.",
                transferStatus = "PENDING", // PENDING TRANSFER APPROVAL
                requestedBy = "Sarath pathirana",
                approvedBy = "Nimal Rathnayake",
                syncStatus = SyncStatus.SYNCED
            )
        )
        transactionDao.insertTransactions(transactions)

        // 10. Notifications from PDF page 13
        val notifications = listOf(
            NotificationEntity(
                notificationId = "NTF-01",
                title = "⚠ Low Stock Detected",
                message = "USB Cable (SKU: USB-CBL-01) stock is at 12 units (Minimum: 20). Replenishment required.",
                type = "LOW_STOCK",
                referenceId = "PRD-00012"
            ),
            NotificationEntity(
                notificationId = "NTF-02",
                title = "⚠ Expiry Warning: Chemical X",
                message = "Industrial Chemical Cleaner X expires on 05/10/2026. Please inspect inventory lot.",
                type = "EXPIRY",
                referenceId = "PRD-00099"
            ),
            NotificationEntity(
                notificationId = "NTF-03",
                title = "Stock Transfer Approval Required",
                message = "Transfer XFR-2026-04 for 20 Laptop Chargers from Sec B to Sec C requires Manager approval.",
                type = "TRANSFER",
                referenceId = "TXN-1003"
            ),
            NotificationEntity(
                notificationId = "NTF-04",
                title = "Warehouse Announcement",
                message = "Annual inventory cycle count begins this Friday. Ensure all mobile scanners are calibrated.",
                type = "ANNOUNCEMENT",
                referenceId = "SYSTEM"
            )
        )
        userDao.insertNotifications(notifications)

        // 11. Initial Audit Log from PDF page 16-17
        userDao.insertAuditLog(
            AuditLogEntity(
                eventId = "EVT-001",
                userId = "USR-001",
                userName = "Admin01 (Chandeepa)",
                eventType = "PRODUCT_CREATED",
                entityType = "PRODUCT",
                entityId = "PRD-00125",
                details = "Product Laptop Charger created with SKU CHG-100."
            )
        )
    }
}
