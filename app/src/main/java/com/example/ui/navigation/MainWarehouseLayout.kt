package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.NotificationEntity
import com.example.domain.model.UserRole
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.components.SyncStatusChip
import com.example.ui.screens.AddItemScreen
import com.example.ui.screens.ApprovalsScreen
import com.example.ui.screens.AssetManagementScreen
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FindProductScreen
import com.example.ui.screens.HardwarePeripheralsScreen
import com.example.ui.screens.InventoryDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProductListScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SuppliersScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.SyncQueueScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.screens.WarehouseLocationScreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoOnContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MutedCoral
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel
import kotlinx.coroutines.launch

/**
 * Main application navigation shell.
 * Implements edge-to-edge window insets, top action bar with sync status,
 * minimalist bottom navigation bar with rounded indicators, and more-menu bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWarehouseLayout(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isOnline by viewModel.isNetworkConnected.collectAsState()
    val syncQueue by viewModel.syncQueue.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val pendingSyncCount = syncQueue.count { it.status.name == "PENDING" || it.status.name == "FAILED" }
    val unreadNotifications = notifications.count { !it.isRead }

    val snackbarHostState = remember { SnackbarHostState() }
    var showMoreMenuSheet by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    // Predictive back navigation handling
    BackHandler(enabled = currentScreen !is WarehouseScreen.Dashboard && currentScreen !is WarehouseScreen.Login && currentScreen !is WarehouseScreen.SignUp) {
        viewModel.navigateBack()
    }

    // If current screen is Login or SignUp, display the respective interface
    if (currentScreen is WarehouseScreen.Login) {
        LoginScreen(viewModel = viewModel)
        return
    }

    if (currentScreen is WarehouseScreen.SignUp) {
        SignUpScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (currentScreen) {
                                is WarehouseScreen.Dashboard -> "Smart Warehouse"
                                is WarehouseScreen.AddItem -> "Add Warehouse Item"
                                is WarehouseScreen.Products -> "Product Catalog"
                                is WarehouseScreen.Scanner -> "Barcode & QR Scanner"
                                is WarehouseScreen.Assets -> "Asset Management"
                                is WarehouseScreen.Warehouses -> "Locations & Bins"
                                is WarehouseScreen.Transactions -> "Transaction Ledger"
                                is WarehouseScreen.SyncQueue -> "Offline Sync Queue"
                                is WarehouseScreen.Hardware -> "Hardware Peripherals"
                                is WarehouseScreen.Reports -> "Reports & Analytics"
                                is WarehouseScreen.UserManagement -> "User Administration"
                                is WarehouseScreen.Approvals -> "Transfer Approvals"
                                is WarehouseScreen.FindProduct -> "Find Product Location"
                                is WarehouseScreen.Settings -> "System Settings"
                                else -> "Smart Warehouse"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 12.dp)) {
                        SyncStatusChip(
                            isOnline = isOnline,
                            pendingSyncCount = pendingSyncCount,
                            onClick = { viewModel.navigateTo(WarehouseScreen.SyncQueue) }
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(
                                        containerColor = MutedCoral,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadNotifications")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Slate700
                            )
                        }
                    }

                    // Logout trigger
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.padding(end = 4.dp).testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Slate700
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                NavigationBarItem(
                    selected = currentScreen is WarehouseScreen.Dashboard,
                    onClick = { viewModel.navigateTo(WarehouseScreen.Dashboard) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFE8F5E9),
                        selectedIconColor = Color(0xFF084B34),
                        selectedTextColor = Color(0xFF084B34)
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )

                NavigationBarItem(
                    selected = currentScreen is WarehouseScreen.Products,
                    onClick = { viewModel.navigateTo(WarehouseScreen.Products) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Products") },
                    label = { Text("Catalog", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFE8F5E9),
                        selectedIconColor = Color(0xFF084B34),
                        selectedTextColor = Color(0xFF084B34)
                    ),
                    modifier = Modifier.testTag("nav_products")
                )

                NavigationBarItem(
                    selected = currentScreen is WarehouseScreen.AddItem,
                    onClick = { viewModel.navigateTo(WarehouseScreen.AddItem) },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add Item") },
                    label = { Text("Add Item", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFE8F5E9),
                        selectedIconColor = Color(0xFF084B34),
                        selectedTextColor = Color(0xFF084B34)
                    ),
                    modifier = Modifier.testTag("nav_add_item")
                )

                NavigationBarItem(
                    selected = currentScreen is WarehouseScreen.Scanner,
                    onClick = { viewModel.navigateTo(WarehouseScreen.Scanner) },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan") },
                    label = { Text("Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFE8F5E9),
                        selectedIconColor = Color(0xFF084B34),
                        selectedTextColor = Color(0xFF084B34)
                    ),
                    modifier = Modifier.testTag("nav_scanner")
                )

                NavigationBarItem(
                    selected = showMoreMenuSheet ||
                        currentScreen is WarehouseScreen.Assets ||
                        currentScreen is WarehouseScreen.Warehouses ||
                        currentScreen is WarehouseScreen.Transactions ||
                        currentScreen is WarehouseScreen.SyncQueue ||
                        currentScreen is WarehouseScreen.Hardware ||
                        currentScreen is WarehouseScreen.Reports ||
                        currentScreen is WarehouseScreen.UserManagement ||
                        currentScreen is WarehouseScreen.Approvals ||
                        currentScreen is WarehouseScreen.FindProduct ||
                        currentScreen is WarehouseScreen.Settings,
                    onClick = { showMoreMenuSheet = true },
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                    label = { Text("More", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFFE8F5E9),
                        selectedIconColor = Color(0xFF084B34),
                        selectedTextColor = Color(0xFF084B34)
                    ),
                    modifier = Modifier.testTag("nav_more")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                is WarehouseScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
                is WarehouseScreen.InventoryDashboard -> InventoryDashboardScreen(viewModel = viewModel)
                is WarehouseScreen.AddItem -> AddItemScreen(viewModel = viewModel)
                is WarehouseScreen.Suppliers -> SuppliersScreen(viewModel = viewModel)
                is WarehouseScreen.AuditLog -> AuditLogScreen(viewModel = viewModel)
                is WarehouseScreen.Products -> ProductListScreen(viewModel = viewModel)
                is WarehouseScreen.Scanner -> ScannerScreen(viewModel = viewModel)
                is WarehouseScreen.Assets -> AssetManagementScreen(viewModel = viewModel)
                is WarehouseScreen.Warehouses -> WarehouseLocationScreen(viewModel = viewModel)
                is WarehouseScreen.Transactions -> TransactionHistoryScreen(viewModel = viewModel)
                is WarehouseScreen.SyncQueue -> SyncQueueScreen(viewModel = viewModel)
                is WarehouseScreen.Hardware -> HardwarePeripheralsScreen(viewModel = viewModel)
                is WarehouseScreen.Reports -> ReportsScreen(viewModel = viewModel)
                is WarehouseScreen.UserManagement -> UserManagementScreen(viewModel = viewModel)
                is WarehouseScreen.Approvals -> ApprovalsScreen(viewModel = viewModel)
                is WarehouseScreen.FindProduct -> FindProductScreen(viewModel = viewModel)
                is WarehouseScreen.Settings -> SettingsScreen(viewModel = viewModel)
                else -> DashboardScreen(viewModel = viewModel)
            }
        }
    }

    // More Modules Bottom Sheet
    if (showMoreMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreMenuSheet = false },
            sheetState = rememberModalBottomSheetState(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Warehouse Management Modules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                MoreMenuItem(
                    title = "Inventory Dashboard (Recharts / D3)",
                    subtitle = "Interactive summary bar chart of stock levels & low-stock alerts",
                    icon = Icons.Default.Assessment,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.InventoryDashboard)
                    }
                )

                MoreMenuItem(
                    title = "Find Product Location (Bin Finder)",
                    subtitle = "Search any SKU or name to get Section, Rack, Shelf, and Bin",
                    icon = Icons.Default.LocationOn,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.FindProduct)
                    }
                )

                MoreMenuItem(
                    title = "Transfer Approvals",
                    subtitle = "Managerial authorization for internal warehouse relocations",
                    icon = Icons.Default.Verified,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.Approvals)
                    }
                )

                MoreMenuItem(
                    title = "User Administration",
                    subtitle = "Admin controls: create users, assign roles, activate/deactivate",
                    icon = Icons.Default.Group,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.UserManagement)
                    }
                )

                MoreMenuItem(
                    title = "Locations & Bins",
                    subtitle = "Warehouse hierarchy: Section -> Rack -> Shelf -> Bin",
                    icon = Icons.Default.Business,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.Warehouses)
                    }
                )

                MoreMenuItem(
                    title = "Transaction Ledger",
                    subtitle = "Immutable stock in, stock out, and transfer logs",
                    icon = Icons.Default.History,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.Transactions)
                    }
                )

                MoreMenuItem(
                    title = "Offline Sync Queue",
                    subtitle = "Inspect and dispatch offline queued operations with idempotency",
                    icon = Icons.Default.CloudSync,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.SyncQueue)
                    }
                )

                MoreMenuItem(
                    title = "Hardware & Bluetooth",
                    subtitle = "Connect Bluetooth barcode scanners, printers, and NFC tags",
                    icon = Icons.Default.Bluetooth,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.Hardware)
                    }
                )

                MoreMenuItem(
                    title = "Reports & Analytics",
                    subtitle = "Financial valuation, stock health, and category turnover",
                    icon = Icons.Default.Assessment,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.Reports)
                    }
                )

                MoreMenuItem(
                    title = "Suppliers Management",
                    subtitle = "Manage vendor contacts, procurement catalog, and linkage",
                    icon = Icons.Default.Business,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.Suppliers)
                    }
                )

                MoreMenuItem(
                    title = "System Audit Logs",
                    subtitle = "Immutable record of logins, stock ops, and administrative changes",
                    icon = Icons.Default.History,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.AuditLog)
                    }
                )

                MoreMenuItem(
                    title = "Settings & Profiles",
                    subtitle = "Role switcher, permissions matrix, backend REST URL",
                    icon = Icons.Default.Settings,
                    onClick = {
                        showMoreMenuSheet = false
                        viewModel.navigateTo(WarehouseScreen.Settings)
                    }
                )
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = notifications,
            onDismiss = { showNotificationsDialog = false },
            onItemClick = { notif ->
                viewModel.markNotificationRead(notif.notificationId)
            }
        )
    }
}

@Composable
private fun MoreMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IndigoContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }
        }
    }
}

@Composable
private fun NotificationsDialog(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onItemClick: (NotificationEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Warehouse Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (notifications.isEmpty()) {
                    Text(
                        text = "No operational notifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.notificationId }) { notif ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (notif.isRead) Slate100 else EmeraldContainer.copy(alpha = 0.5f))
                                    .clickable { onItemClick(notif) }
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate700
                                    )
                                }
                            }
                        }
                    }
                }

                MinimalButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
