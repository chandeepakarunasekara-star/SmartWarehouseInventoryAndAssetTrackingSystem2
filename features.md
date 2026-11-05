# Smart Warehouse Inventory and Asset Tracking System — Features & Use Cases

This document details all the features currently implemented in the **Smart Warehouse Android Application**, mapped directly to their respective use cases from the *Smart Warehouse Inventory and Asset Tracking System* specification document.

The application is built with **Kotlin**, **Jetpack Compose (Material 3)**, and an **Offline-First Room SQLite Database Architecture**, complemented by **ML Kit Vision Barcode Scanning**, **Bluetooth/NFC peripheral emulation**, and **Role-Based Access Control (RBAC)**.

---

## Architecture & Technology Stack Overview

| Component | Technology | Description |
|---|---|---|
| **User Interface** | Jetpack Compose + Material 3 | Emerald enterprise theme (`#032B1E`, `#084B34`, `#4ADE80`), predictive back navigation, bottom navigation bar, and responsive layout. |
| **Local Database** | Room SQLite (`smart_warehouse.db`) | 12 entities, 6 DAOs, reactive Kotlin `Flow` streams, database converters, and pre-population seeder (`DatabaseInitialDataSeeder`). |
| **State Management** | MVVM + Coroutines / StateFlow | Clean architecture with repository pattern (`WarehouseRepository`), single source of truth, and `WarehouseViewModel`. |
| **Barcode / QR Vision** | Google ML Kit Vision | Real-time camera barcode and QR detection with flash toggle, manual fallback, and audio-haptic feedback. |
| **Hardware Peripherals** | Bluetooth & NFC Managers | BLE peripheral pairing (Honeywell Voyager scanners, Zebra mobile printers, forklift scales) and ISO 14443-A NFC tag emulation. |
| **Data Visualization** | Custom D3 / Recharts-styled Canvas | Interactive summary bar chart with dynamic scaling, low-stock threshold reference lines, and category filter chips. |
| **Offline Synchronization** | Two-Way Sync Queue | SQLite-backed `sync_queue` table with idempotency keys, retry policies, and network status observers. |
| **Testing** | Robolectric Local JVM Suite | Automated CUJ verification across all entities, DAOs, flows, stock math, and security rules (`ExampleRobolectricTest.kt`). |

---

## Detailed Feature Matrix & Respective Use Cases

### 1. Authentication, Sessions & Role-Based Access Control (RBAC)
- **Use Case Mapping**: Section 1 (Overall Purpose & User Roles), Section 24 (User Administration)
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`, `WAREHOUSE_EMPLOYEE`
- **Implementation**:
  - `LoginScreen.kt`, `SignUpScreen.kt`, `UserEntity`, `UserDao`, `FirebaseAuthManager.kt`
  - Top bar live role switcher and quick modal dialog allowing testers to switch active permissions on the fly.
  - Granular permissions matrix enforced at the repository level:
    - **Administrator**: Full system access, user creation, warehouse creation, product catalog, system audit logs, and settings.
    - **Warehouse Manager**: Inventory approvals, stock transfers, asset assignment, reports, and alerts.
    - **Warehouse Employee**: Dedicated operational tools (scanner, stock-in, stock-out, bin finder, asset relocation).
- **Offline / Cloud Dual Auth**: Real email/password validation with offline cached session fallback when disconnected.

---

### 2. Interactive Inventory Dashboard & Recharts/D3 Stock Visualizer
- **Use Case Mapping**: Section 14 (Low Stock Monitoring), Section 19 (Analytics & Reports)
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `InventoryDashboardScreen.kt`, `RechartsSummaryBarChart.kt`
  - **Summary Bar Chart**: Custom Jetpack Compose Canvas rendering styled after Recharts / D3.js:
    - Animated vertical bars color-coded by stock health: **Emerald** (Adequate stock), **Coral** (Low stock alert), **Slate** (Out of stock).
    - Dynamic Y-axis gridlines and automated scale normalization based on max inventory.
    - Configurable **Safety Threshold Line** (default 15 units) drawn across bars with dashed styling.
    - Metric toggles: Switch visualization between **Current Units on Hand** and **Total Valuation ($)**.
    - Category filtering: Filter bars by *All*, *Low Stock Only*, *Electronics*, *Equipment*, or *Consumables*.
  - **KPI Summary Cards**: Real-time reactive cards for Total Valuation ($), Total Units, Low Stock Alert Count, and Out-of-Stock Count.
  - **Actionable Low-Stock Tray**: Direct "Restock +20" button with instant Room SQLite stock adjustment and ledger transaction generation.

---

### 3. Comprehensive Product Catalog Management & Barcode Registration
- **Use Case Mapping**: Section 2 (Product Catalog), Section 3 (Mobile Barcode / QR Scanning)
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`, `WAREHOUSE_EMPLOYEE`
- **Implementation**:
  - `ProductListScreen.kt`, `AddItemScreen.kt`, `ProductEntity`, `ProductDao`
  - Complete master data fields: Product ID (`PRD-xxxxx`), SKU, Barcode (EAN-13/Code128/UPC), QR Code, Name, Description, Category, Supplier ID, Unit Price, Quantity, Minimum Stock Level, Expiry Date (`YYYY-MM-DD`), Location ID, and Image URL.
  - **Barcode Scan Product Intake**:
    - Add new products by scanning barcode labels directly with the camera via ML Kit Vision inside `AddItemScreen`.
    - Integrated barcode scanner button on the SKU input field opening a real-time scanning viewfinder with quick-simulation test chips.
    - Seamless flow from `ScannerScreen`: Scanning an unregistered barcode automatically displays an **"Add as New Product with this Barcode"** button, navigating directly to `AddItemScreen` with the barcode pre-populated.
  - Real-time search by keyword, SKU, or barcode code.
  - Filtering by category (Electronics, Heavy Equipment, Consumables, Safety Gear).
  - Add and Edit item dialogs with validation against negative stock levels and duplicate barcodes.
  - Active/Deactivated product state toggling.

---

### 4. Barcode & QR Code Scanning (ML Kit & Peripherals)
- **Use Case Mapping**: Section 3 (Mobile Barcode / QR Scanning)
- **Target Roles**: `WAREHOUSE_EMPLOYEE`, `WAREHOUSE_MANAGER`, `ADMINISTRATOR`
- **Implementation**:
  - `ScannerScreen.kt`, `BarcodeScanner.kt`, `com.google.mlkit:barcode-scanning`
  - Camera-based real-time viewfinder overlay with animated scanning laser beam and reticle targeting.
  - Instant product & asset lookup upon scanning barcode or QR code:
    - Displays product name, SKU, current stock, assigned bin location, and supplier.
    - Quick action buttons: **[Stock In]**, **[Stock Out]**, **[Transfer]**, and **[History]**.
    - If code is unrecognized, offers an instant one-tap **[Add as New Product]** action pre-filling the barcode into the product registration form.
  - Manual code entry dialog for damaged labels or low-light situations.
  - Test Barcode shortcuts for emulator simulation (including existing items and unregistered codes).
  - Torch / flashlight toggle button and camera permission handling.

---

### 5. Stock-In / Receiving Goods Workflow
- **Use Case Mapping**: Section 4 (Receiving Stock), Section 23 (End-to-End Receiving Scenario)
- **Target Roles**: `WAREHOUSE_EMPLOYEE`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `StockInDialog` in `WarehouseDialogs.kt`, `WarehouseRepository.stockIn()`, `StockTransactionEntity`
  - Step-by-step workflow:
    1. Scan product barcode or select from catalog.
    2. Input received quantity (e.g. 100 units).
    3. Select destination storage bin/location.
    4. Provide Purchase Order (PO) or delivery bill reference number.
    5. Save directly to Room SQLite database (`products.quantity += qty`).
    6. Record double-entry transaction in `stock_transactions` with `STOCK_IN` type, employee ID, and timestamp.
    7. Queue idempotent operation in `sync_queue` for cloud synchronization.

---

### 6. Stock-Out / Dispatch Goods Workflow
- **Use Case Mapping**: Section 5 (Issuing Stock)
- **Target Roles**: `WAREHOUSE_EMPLOYEE`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `StockOutDialog` in `WarehouseDialogs.kt`, `WarehouseRepository.stockOut()`, `StockTransactionDao`
  - Safeguards and validation:
    - Verifies available stock before issuing goods; strictly prevents negative balances or overdrafts.
    - Records dispatch destination (e.g., "Dispatch Bay 2", "Sales Order #SO-8812").
    - Updates local Room stock instantly and logs immutable ledger entry with `STOCK_OUT` type.

---

### 7. Internal Stock Relocation & Managerial Approvals
- **Use Case Mapping**: Section 6 (Moving Stock Between Locations), Section 18 (Approvals)
- **Target Roles**: `WAREHOUSE_EMPLOYEE` (request), `WAREHOUSE_MANAGER` (approval/execution)
- **Implementation**:
  - `ApprovalsScreen.kt`, `StockTransferDialog` in `WarehouseDialogs.kt`
  - 5-stage relocation lifecycle: `PENDING` $\rightarrow$ `APPROVED` $\rightarrow$ `IN_TRANSIT` $\rightarrow$ `COMPLETED` (or `CANCELLED`).
  - Records source location, destination location, requested by employee, authorized manager, and cargo notes.
  - Approvals screen allows managers to approve or reject pending transfers with single-tap audit confirmation.

---

### 8. Find Product Location (Bin & Aisle Locator)
- **Use Case Mapping**: Section 7 (Finding Product Location)
- **Target Roles**: `WAREHOUSE_EMPLOYEE`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `FindProductScreen.kt`
  - Fast search by SKU, product name, or barcode.
  - Visual breadcrumb hierarchy card displaying:
    - **Warehouse Facility** (e.g., Central Distribution Hub)
    - **Section** (e.g., Section A)
    - **Rack** (e.g., Rack 04)
    - **Shelf** (e.g., Shelf 02)
    - **Bin / Storage Position** (e.g., Bin B-12)
  - Color-coded capacity occupancy gauge showing current bin utilization percentage.

---

### 9. Multi-Warehouse & Granular Storage Hierarchy
- **Use Case Mapping**: Section 8 (Storage Locations & Modeling)
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `WarehouseLocationScreen.kt`, `WarehouseEntity`, `LocationEntity`, `WarehouseDao`
  - Multi-facility management: Code, Name, Physical Address, Maximum Capacity Units, and Assigned Manager.
  - Granular child locations: `Section` $\rightarrow$ `Rack` $\rightarrow$ `Shelf` $\rightarrow$ `Bin`.
  - Add location dialog with capacity boundaries and occupancy tracking.
  - Helper getter `fullHierarchyPath` formatted cleanly (`Sec A • Rack 01 • Sh 02 • Bin 05`).

---

### 10. Physical Asset Tracking & Lifecycle Management
- **Use Case Mapping**: Section 9 (Assets vs Products)
- **Target Roles**: `WAREHOUSE_MANAGER`, `WAREHOUSE_EMPLOYEE`
- **Implementation**:
  - `AssetManagementScreen.kt`, `AssetEntity`, `AssetDao`
  - Manages enterprise capital equipment (Forklifts, Pallet Jacks, Handheld Terminals, Label Printers, Scales).
  - Fields: Asset ID (`AST-xxxxx`), Serial Number, Asset Code, Status (`AVAILABLE`, `ASSIGNED`, `IN_USE`, `UNDER_MAINTENANCE`, `DAMAGED`, `RETIRED`), Physical Condition, Purchase Date, Last Inspection Date, NFC Tag ID, and Photo URL.
  - Filter assets by status tabs and search by serial number or asset code.

---

### 11. Asset Custody & Employee Assignment
- **Use Case Mapping**: Section 10 (Assigning Assets to Employees)
- **Target Roles**: `WAREHOUSE_MANAGER`
- **Implementation**:
  - `AssetAssignmentDialog`, `AssetDao.updateAsset()`, `AssetMovementEntity`
  - Assigns physical asset responsibility to a designated employee.
  - Records assignment date, manager authorizing handover, and updates asset status to `ASSIGNED` / `IN_USE`.

---

### 12. Asset Movement Tracking & Location Auditing
- **Use Case Mapping**: Section 11 (Asset Movement History), Section 24 (Relocation Flow)
- **Target Roles**: `WAREHOUSE_EMPLOYEE`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `RecordAssetMovementDialog`, `AssetMovementEntity`, `AssetDao.observeMovementsForAsset()`
  - Logs physical movement between warehouse docks, maintenance bays, and staging zones.
  - Preserves immutable movement audit trail (timestamp, from-location, to-location, previous custodian, new custodian, and notes).

---

### 13. Advanced NFC Asset Tag Reading & Identification
- **Use Case Mapping**: Section 12 (NFC Asset Tagging)
- **Target Roles**: `WAREHOUSE_EMPLOYEE`
- **Implementation**:
  - `NfcTagManager.kt`, `HardwarePeripheralsScreen.kt`
  - Emulates ISO 14443-A / NTAG213 high-frequency tag reading.
  - One-tap asset identification: Tapping simulated or physical NFC tag instantly loads asset specifications, custody record, and maintenance logs.

---

### 14. Bluetooth Warehouse Peripherals & Telemetry
- **Use Case Mapping**: Section 13 (Bluetooth Peripherals)
- **Target Roles**: `WAREHOUSE_EMPLOYEE`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `BluetoothDeviceManager.kt`, `HardwarePeripheralsScreen.kt`
  - Integrated peripheral management:
    - Honeywell Voyager 1602g Barcode Scanners
    - Zebra ZQ520 Mobile Label Printers
    - Avery Weigh-Tronix Forklift Scales
  - Live peripheral connection toggling, battery percentage gauge, signal strength (dBm) indicator, and BLE scan trigger.
  - Peripherals run decoupled from core warehouse logic to guarantee offline operation even if a peripheral disconnects.

---

### 15. Real-Time Expiration Date Monitoring
- **Use Case Mapping**: Section 15 (Expiration Date Monitoring)
- **Target Roles**: `WAREHOUSE_MANAGER`, `ADMINISTRATOR`
- **Implementation**:
  - `ProductEntity.expiryDate`, `WarehouseViewModel.expiringProducts`, `ReportsScreen.kt`
  - Categorizes batches approaching shelf-life expiration within 30, 60, or 90 days.
  - Flags expiring materials on dashboards to ensure First-Expired, First-Out (FEFO) picking order.

---

### 16. In-App Notification Center & Alerts
- **Use Case Mapping**: Section 16 (Notification System)
- **Target Roles**: All Roles
- **Implementation**:
  - `NotificationEntity`, `NotificationsDialog` in `MainWarehouseLayout.kt`
  - Badged notification bell in the top application bar showing real-time unread counts.
  - System-generated alerts for:
    - Low-stock warnings when inventory drops below safety thresholds.
    - Approaching product expiration alerts.
    - Stock transfer requests requiring manager approval.
    - Background synchronization failures or retry events.
    - Facility notices and announcements.

---

### 17. Offline-First Architecture & SQLite Room Persistence
- **Use Case Mapping**: Section 17 (Offline First and Synchronization)
- **Target Roles**: All Roles (Transparent to user)
- **Implementation**:
  - `AppDatabase.kt` (Room Database version 1, exportSchema = false)
  - Zero-latency local SQLite reads and writes; the application operates with 100% feature parity with or without active internet connectivity.
  - Initial database seeder (`DatabaseInitialDataSeeder`) automatically seeds warehouses, categories, locations, suppliers, products, physical assets, and transactions on first launch.

---

### 18. Two-Way Offline Synchronization Queue & Idempotency
- **Use Case Mapping**: Section 17 & 18 (Sync Queue & Server Validation)
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `SyncQueueScreen.kt`, `SyncQueueEntity`, `SyncQueueDao`
  - Changes made offline are queued with unique idempotency keys (`TXN_xxxxx_IN`, `ASSET_MOV_xxxxx`).
  - Sync queue monitor screen allows reviewing pending payloads, sync status (`PENDING`, `IN_PROGRESS`, `SYNCED`, `FAILED`), and manually triggering sync dispatch.
  - Top app bar dynamic chip indicates live network connection status (`Online` / `Offline`) and count of pending changes.

---

### 19. Inventory Reports, Financial Valuation & Analytics
- **Use Case Mapping**: Section 19 (Reports and Analytics)
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `ReportsScreen.kt`
  - Financial total inventory valuation ($) calculated dynamically across all warehouse locations.
  - Category breakdown and stock turnover distribution.
  - Stock movement transaction report breakdown (`STOCK_IN` vs `STOCK_OUT` vs `TRANSFERS`).
  - Asset status breakdown (Available vs In Use vs Maintenance).

---

### 20. Double-Entry Stock Transaction Ledger
- **Use Case Mapping**: Section 20 (History and Traceability)
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `TransactionHistoryScreen.kt`, `StockTransactionEntity`, `StockTransactionDao`
  - Immutable audit trail recording every inventory change:
    - Transaction ID, Product Name, Transaction Type (`STOCK_IN`, `STOCK_OUT`, `STOCK_TRANSFER`), Quantity, Source Location, Destination Location, Actor Name/ID, PO/Invoice Reference Number, Timestamp, and Approval Notes.
  - Filter ledger entries by transaction type and search by reference number or product name.

---

### 21. System Administrative Compliance Audit Logs
- **Use Case Mapping**: Section 21 (Audit Logs)
- **Target Roles**: `ADMINISTRATOR`
- **Implementation**:
  - `AuditLogScreen.kt`, `AuditLogEntity`, `UserDao.observeAuditLogs()`
  - High-security governance log tracking all administrative actions:
    - Events logged: `LOGIN`, `PRODUCT_CREATED`, `PRODUCT_UPDATED`, `STOCK_IN`, `STOCK_OUT`, `STOCK_TRANSFER`, `ASSET_MOVED`, `USER_CREATED`, `ROLE_CHANGED`, `SYNC_FAILED`.
    - Captures User ID, Full Name, Event Type, Entity Type, Entity ID, and detailed description.

---

### 22. User Administration & Access Control
- **Use Case Mapping**: Section 22 (User Management)
- **Target Roles**: `ADMINISTRATOR`
- **Implementation**:
  - `UserManagementScreen.kt`, `UserEntity`, `UserDao`
  - Create new warehouse users with specified usernames, full names, corporate emails, and initial roles.
  - Assign warehouse facility affiliation.
  - Activate or deactivate user accounts with immediate revocation of privileges.
  - View user profile and activity timestamp.

---

### 23. Suppliers & Vendor Catalog Management
- **Use Case Mapping**: Section 2 (Product Catalog Suppliers), Section 28 (Suppliers Management)
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `SuppliersScreen.kt`, `SupplierEntity`, `WarehouseDao`
  - Maintain vendor directory: Company Name, Contact Person, Business Email, Phone Number, and Physical Address.
  - Quick action buttons to launch dialer (`tel:`) and email client (`mailto:`) directly from the contact card.
  - Add Supplier modal dialog with Room SQLite persistence.
  - Link suppliers directly to catalog items (`supplierId`).

---

### 24. Facility CCTV Monitoring & Telemetry Visualizer
- **Use Case Mapping**: Facility Security & Real-Time Monitoring
- **Target Roles**: `ADMINISTRATOR`, `WAREHOUSE_MANAGER`
- **Implementation**:
  - `WarehouseVideoClipPlayer.kt`, integrated into `DashboardScreen.kt`
  - High-performance, offline-ready Jetpack Compose Canvas animated CCTV player.
  - Replaces fragile external HTTP video streams with an animated security feed featuring:
    - Rotating warehouse radar sweep and radar pings.
    - Horizontal scanning laser beams and surveillance scanlines.
    - Live facility HUD telemetry (Active Nodes, Camera Feed ID `CAM-04A [BAY-2]`, live timecode timestamp, and 60 FPS motion indicators).
    - Zero external network dependencies, preventing playback crashes or HTTP 403 errors.

---

## Verification & Test Results

The application's Room database and business logic are covered by an automated test suite in `ExampleRobolectricTest.kt`. All tests execute locally on the JVM without requiring an Android emulator.

| Test Case | Description | Result |
|---|---|---|
| `test product dao insert query update and search` | Validates product insertion, SKU lookup, barcode query, keyword search, stock quantity updates, Flow observation, and deletion. | **PASSED** |
| `test asset dao insert observe by status and record movement` | Tests equipment creation, NFC tag lookup, status filtering, assignment to employee, and movement history recording. | **PASSED** |
| `test offline sync queue persistence` | Validates sync queue item creation, idempotency key uniqueness, pending item querying, and status updates to `SYNCED`. | **PASSED** |
| `test warehouse and location hierarchy and suppliers` | Tests warehouse registration, multi-tier location hierarchy path generation (`Sec • Rack • Sh • Bin`), and supplier CRUD. | **PASSED** |
| `test stock transaction double entry ledger` | Tests insertion of stock-in transactions, double-entry ledger balance recording, and pending transfer queries. | **PASSED** |
| `test user authentication and compliance audit logging` | Verifies user creation, username lookup, role checks, and administrative audit event recording. | **PASSED** |
| `test database master data seeder` | Ensures `DatabaseInitialDataSeeder` initializes products, warehouses, assets, locations, and suppliers properly on first launch. | **PASSED** |
| `test warehouse repository end to end stock operations` | Tests high-level `stockIn()` and `stockOut()` repository methods, balance recalculations, and transaction ledger synchronization. | **PASSED** |
| `test firebase auth email and password validation` | Tests corporate email validation and password complexity rules. | **PASSED** |
| `test ml kit barcode options builder` | Validates ML Kit barcode format configurations. | **PASSED** |

**Execution Result**: `gradle :app:testDebugUnitTest` $\rightarrow$ **BUILD SUCCESSFUL in 20s** (33 tasks).
**Build Status**: `compile_applet` $\rightarrow$ **Build succeeded - the applet is compiled**.
