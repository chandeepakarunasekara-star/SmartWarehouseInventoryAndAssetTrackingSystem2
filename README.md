# Smart Warehouse Inventory & Asset Tracking System

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Room SQLite](https://img.shields.io/badge/Database-Room%20SQLite%20(Offline--First)-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Firebase](https://img.shields.io/badge/Cloud-Firebase%20Auth%20%26%20Storage%20%26%20Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

An enterprise-grade, offline-first Android application designed for modern warehouse operations, inventory control, equipment tracking, and multi-facility logistics. Built with **Kotlin**, **Jetpack Compose (Material 3 Colorful Minimalism)**, **Room SQLite Database**, **Firebase Cloud Infrastructure** (Auth, Cloud Storage, Firestore sync), **Google ML Kit Barcode Vision**, and **Bluetooth BLE / NFC Hardware Integration**.

---

## 📸 Key UI Highlights & Design System

The application features a **Colorful Minimalist Design System** built on top of Jetpack Compose Material 3:
- **Clean Geometry**: Universal rounded shapes across components (`26.dp` cards, `16.dp` fields, pill badges).
- **Vibrant Accent Palette**:
  - 🟢 **Emerald Mint (`#10B981`, `#084B34`)**: Stock-In receipts, healthy inventory, warehouse employee terminal.
  - 🟣 **Royal Indigo (`#4F46E5`, `#6366F1`)**: Product catalog, administrator console, system settings.
  - 🟪 **Electric Violet (`#7C3AED`, `#8B5CF6`)**: Physical assets, equipment assignment, NFC tag emulation.
  - 🟦 **Ocean Cyan (`#0891B2`, `#06B6D4`)**: Physical bin location finder, warehouses, racks, and shelves.
  - 🟠 **Vivid Amber (`#D97706`, `#F59E0B`)**: Low stock alerts, transfer approvals, warehouse manager console.
  - 🔴 **Radiant Rose (`#E11D48`, `#F43F5E`)**: Stock-Out dispatches, critical errors, negative stock prevention.

---

## 🚀 Architectural Overview & Tech Stack

```
 ┌───────────────────────────────────────────────────────────────────────────┐
 │                   Jetpack Compose UI (Material 3)                         │
 │  (Login, Dashboard, Catalog, Scanner, Assets, Bin Finder, Recharts)     │
 └──────────────────────────────────────┬────────────────────────────────────┘
                                        │ (StateFlow / UI Events)
 ┌──────────────────────────────────────▼────────────────────────────────────┐
 │                  WarehouseViewModel (MVVM Architecture)                   │
 └──────────────────────────────────────┬────────────────────────────────────┘
                                        │
 ┌──────────────────────────────────────▼────────────────────────────────────┐
 │                          WarehouseRepository                              │
 │            (Single Source of Truth & Business Logic Layer)               │
 └───────────┬──────────────────────────┬─────────────────────────┬──────────┘
             │                          │                         │
 ┌───────────▼───────────┐  ┌───────────▼───────────┐  ┌──────────▼──────────┐
 │   Room SQLite DB      │  ┌   Firebase Auth       │  │  REST API / Firestore │
 │  (12 Local Entities & │  │   Cloud Storage       │  │    Sync Queue Layer   │
 │   Offline DAOs)       │  │  (Media & Photos)     │  │  (Idempotency Keys)   │
 └───────────────────────┘  └───────────────────────┘  └─────────────────────┘
```

| Layer / Technology | Specification | Functionality |
|:---|:---|:---|
| **Language & Runtime** | Kotlin 2.0+ / Coroutines Flow | Asynchronous reactive state streaming, structured concurrency. |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative UI, dynamic role themes, responsive layout scaffold. |
| **Local Database** | Room SQLite (`smart_warehouse.db`) | 12 Entities, 6 DAOs, TypeConverters, pre-seeded data generator. |
| **Firebase Auth** | `com.google.firebase:firebase-auth` | Real-time user sign-in, password validation, offline cached identity. |
| **Firebase Storage** | Cloud Storage & Photo Picker | Item & asset image uploads, live camera capture, fullscreen viewer. |
| **Firebase Firestore** | Remote Cloud Data Store | Dual cloud backup and two-way synchronization queue layer. |
| **Barcode Scanning** | Google ML Kit Vision | Real-time camera barcode/QR code reader with targeting viewfinder. |
| **Data Visualization** | Custom Compose Canvas | Recharts/D3-styled interactive bar chart with low-stock threshold lines. |
| **Hardware Peripherals**| Bluetooth BLE & NFC | Laser ring scanners, mobile label printers, scale telemetry, NFC tags. |
| **Testing Engine** | Robolectric 4.14+ | Local JVM automated test suite covering DB DAOs, math, and auth flows. |

---

## 👥 Role-Based Access Control (RBAC)

The system enforces strict permission scoping across 3 operational profiles:

1. ⚙️ **Administrator (`ADMIN`)**:
   - Master system configuration, user administration, warehouse creation, category/supplier setup, complete audit logs, and settings.
2. 📋 **Warehouse Manager (`WAREHOUSE_MANAGER`)**:
   - Approval of inter-warehouse stock transfers, restock authorizations, asset assignments, financial reports, and analytics.
3. 📦 **Warehouse Employee (`WAREHOUSE_EMPLOYEE`)**:
   - Operational tasks: Barcode scanning, receiving (Stock-In), issuing (Stock-Out), location bin lookup, and physical asset relocation.

---

## 📁 Key File Structure & Functions

```
com.example
├── MainActivity.kt                      # Single activity hosting Jetpack Compose navigation scaffold
├── data
│   ├── auth
│   │   └── FirebaseAuthManager.kt        # Firebase Authentication implementation with offline fallback
│   ├── local
│   │   ├── AppDatabase.kt               # Room SQLite database configuration (12 entities)
│   │   ├── DatabaseConverters.kt        # Room TypeConverters for Enums and Timestamps
│   │   ├── DatabaseInitialDataSeeder.kt # Sample data generator for products, assets, and bins
│   │   ├── dao/Daos.kt                  # Data Access Objects (Product, Asset, StockTx, Warehouse, UserDao)
│   │   └── entity/Entities.kt          # SQLite Data Entities (ProductEntity, AssetEntity, UserEntity, etc.)
│   ├── remote
│   │   ├── NetworkClient.kt             # OkHttp/Retrofit HTTP client configuration
│   │   ├── RemoteWarehouseDataSource.kt # Spring Boot REST & Firebase cloud data source bridge
│   │   ├── WarehouseApiService.kt       # Retrofit REST API interface
│   │   └── model/WarehouseApiModels.kt  # Cloud DTO data structures
│   └── repository
│       └── WarehouseRepository.kt       # Repository implementing single source of truth & SyncQueue
├── domain
│   └── model/Enums.kt                   # Enumerations (UserRole, StockStatus, AssetStatus, etc.)
├── hardware
│   └── HardwareManagers.kt              # Bluetooth BLE device manager & NFC tag reader emulator
├── ui
│   ├── components
│   │   ├── BarcodeScanner.kt            # ML Kit camera viewfinder & barcode analyzer
│   │   ├── ImageViewerDialog.kt         # Fullscreen interactive photo viewer with zoom
│   │   ├── MinimalComponents.kt         # Minimalist UI buttons, search bars, and status badges
│   │   ├── RechartsSummaryBarChart.kt   # Custom Compose Canvas bar chart visualization
│   │   ├── WarehouseDialogs.kt          # Stock-In, Stock-Out, and Relocation input dialogs
│   │   └── WarehouseVideoClipPlayer.kt  # Industrial demonstration media clip player
│   ├── navigation
│   │   └── MainWarehouseLayout.kt       # Top bar, drawer, bottom navigation & screen routing
│   ├── screens
│   │   ├── AddItemScreen.kt             # New product registration with inline barcode scanning
│   │   ├── ApprovalsScreen.kt           # Managerial approval queue for pending transfers
│   │   ├── AssetManagementScreen.kt     # Equipment lifecycle tracking, employee assignment, photo upload
│   │   ├── AuditLogScreen.kt            # Immutable system audit trail log
│   │   ├── DashboardScreen.kt           # Role-customized landing dashboard with metrics
│   │   ├── FindProductScreen.kt         # Bin finder (warehouse -> section -> rack -> shelf -> bin)
│   │   ├── HardwarePeripheralsScreen.kt # Bluetooth BLE peripheral pairing & NFC tag manager
│   │   ├── InventoryDashboardScreen.kt  # Inventory health charts & valuation analytics
│   │   ├── LoginScreen.kt               # Colorful minimalist login screen with Firebase Auth
│   │   ├── ProductListScreen.kt         # Catalog list with search, filter, and detail view
│   │   ├── ReportsScreen.kt             # Financial reports & stock turnover analytics
│   │   ├── ScannerScreen.kt             # Real-time barcode/QR scanner with instant actions
│   │   ├── SettingsScreen.kt            # Server endpoint settings & system preferences
│   │   ├── SignUpScreen.kt              # Firebase user registration screen
│   │   ├── SuppliersScreen.kt           # Supplier catalog & vendor management
│   │   ├── SyncQueueScreen.kt           # Offline synchronization queue inspector
│   │   ├── TransactionHistoryScreen.kt  # Chronological stock movement ledger
│   │   ├── UserManagementScreen.kt      # Administrator user creation & role switcher
│   │   └── WarehouseLocationScreen.kt   # Facility bin hierarchy manager
│   ├── theme
│   │   ├── Color.kt                     # Minimalist colorful color palette
│   │   ├── Theme.kt                     # Material 3 light/dark theme schemes
│   │   └── Type.kt                      # Typography scale configuration
│   └── viewmodel
│       └── WarehouseViewModel.kt        # Master ViewModel handling screen state & coroutine scopes
```

---

## 🛠️ Build & Setup Instructions

### Prerequisites
- **Android Studio**: Ladybug / 2024.2.1 or newer
- **JDK**: Java 17 or Java 21
- **Android SDK**: API 35 (Android 15) target / API 24 minimum

### Building from Source
1. Open Android Studio and choose **File ➔ Open**, navigating to the project directory.
2. Allow Gradle to synchronize dependencies.
3. Build the project using Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run automated Robolectric unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
5. Deploy to an Android emulator or connected USB device:
   ```bash
   ./gradlew installDebug
   ```

---

## 📄 License & Documentation

Refer to `Appfeatures.md` for a comprehensive description of all 28 Use Cases, cloud integration specifications (Firebase Auth, Cloud Storage, Firestore), and file-by-file function references.
