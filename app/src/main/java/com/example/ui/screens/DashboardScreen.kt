package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.StockLevelStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserRole
import com.example.ui.components.StockInDialog
import com.example.ui.components.StockOutDialog
import com.example.ui.components.StockTransferDialog
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern Dashboard screen matching the design aesthetic of the reference screenshot.
 * Features:
 * - Brand header with logo, notification badge, and profile button
 * - Deep emerald gradient hero card with "Your inventory, now easier to track" & action button
 * - 4 Quick-Action squircle buttons (Scan, Add Item, Transfer, Find)
 * - Operations video clip player component
 * - Inventory item cards matching the "Nearby Stations" layout from the design
 * - "Quick Stock In" card replicating the "Top up your wallet" layout
 */
@Composable
fun DashboardScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val locations by viewModel.allLocations.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val expiringProducts by viewModel.expiringProducts.collectAsState()
    val pendingTransfers by viewModel.pendingTransfers.collectAsState()

    var showStockInDialog by remember { mutableStateOf(false) }
    var showStockOutDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }

    val unreadNotifications = notifications.count { !it.isRead }
    val displayedProducts = products.take(6)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAF8))
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Brand Header (matching FuelSafe top header in Screenshot 1)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF10B981), Color(0xFF047857))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "SmartWarehouse",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B3B2B)
                        )
                        Text(
                            text = "Scan. Track. Move.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Notification Bell
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .clickable { showNotificationsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(
                                        containerColor = Color(0xFFE11D48),
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
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // User Profile Avatar with Online indicator
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .border(1.5.dp, Color(0xFF10B981), CircleShape)
                            .clickable { showRoleDialog = true }
                            .testTag("dashboard_profile_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF084B34),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. Forest Emerald Hero Banner Card (Replicating Hero Banner from Screenshot 1)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF02261A),
                                Color(0xFF064E3B),
                                Color(0xFF0D684A)
                            )
                        )
                    )
                    .testTag("dashboard_hero_banner")
            ) {
                // Background visual asset
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=700&q=80",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp)),
                    alpha = 0.35f
                )

                // Content Overlay
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        val heroTitle = when (currentUser.role) {
                            UserRole.ADMIN -> "Enterprise Administration\n${currentUser.fullName}"
                            UserRole.WAREHOUSE_MANAGER -> "Operations Manager\n${currentUser.fullName}"
                            UserRole.WAREHOUSE_EMPLOYEE -> "Operational Terminal\n${currentUser.fullName}"
                        }
                        val heroSubtitle = when (currentUser.role) {
                            UserRole.ADMIN -> "System Governance • Users • Audits • Facilities"
                            UserRole.WAREHOUSE_MANAGER -> "Inventory Control • Approvals • Analytics"
                            UserRole.WAREHOUSE_EMPLOYEE -> "Scan Items • Stock In/Out • Find Bins"
                        }

                        Text(
                            text = heroTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = heroSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA7F3D0),
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (currentUser.role) {
                                UserRole.ADMIN -> {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color(0xFF4ADE80))
                                            .clickable { viewModel.navigateTo(WarehouseScreen.UserManagement) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("hero_admin_users_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("User Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF02261A))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .clickable { viewModel.navigateTo(WarehouseScreen.AuditLog) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("hero_admin_audit_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Audit Logs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                UserRole.WAREHOUSE_MANAGER -> {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color(0xFF4ADE80))
                                            .clickable { viewModel.navigateTo(WarehouseScreen.Approvals) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("hero_mgr_approvals_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Approvals (${pendingTransfers.size})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF02261A)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .clickable { viewModel.navigateTo(WarehouseScreen.InventoryDashboard) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("hero_mgr_charts_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Charts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                UserRole.WAREHOUSE_EMPLOYEE -> {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color(0xFF4ADE80))
                                            .clickable { viewModel.navigateTo(WarehouseScreen.Scanner) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("hero_emp_scan_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Scan Barcode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF02261A))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .clickable { viewModel.navigateTo(WarehouseScreen.FindProduct) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .testTag("hero_emp_find_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Find Bin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Terminal Visual Badge on right
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val roleBadgeColor = when (currentUser.role) {
                                UserRole.ADMIN -> Color(0xFF818CF8)
                                UserRole.WAREHOUSE_MANAGER -> Color(0xFFFBBF24)
                                UserRole.WAREHOUSE_EMPLOYEE -> Color(0xFF4ADE80)
                            }
                            Icon(
                                imageVector = when (currentUser.role) {
                                    UserRole.ADMIN -> Icons.Default.Assessment
                                    UserRole.WAREHOUSE_MANAGER -> Icons.Default.CompareArrows
                                    UserRole.WAREHOUSE_EMPLOYEE -> Icons.Default.QrCodeScanner
                                },
                                contentDescription = null,
                                tint = roleBadgeColor,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentUser.role.name.replace("WAREHOUSE_", ""),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${products.size} Items",
                                fontSize = 10.sp,
                                color = Color(0xFFA7F3D0)
                            )
                        }
                    }
                }
            }
        }

        // 3. Four Role-Specific Quick Action Buttons (PDF Section 1: Dashboard Adaptation)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                when (currentUser.role) {
                    UserRole.ADMIN -> {
                        QuickActionItem(
                            icon = Icons.Default.Person,
                            label = "User\nManagement",
                            onClick = { viewModel.navigateTo(WarehouseScreen.UserManagement) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_users"
                        )
                        QuickActionItem(
                            icon = Icons.Default.Assessment,
                            label = "Audit\nLogs",
                            onClick = { viewModel.navigateTo(WarehouseScreen.AuditLog) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_audit"
                        )
                        QuickActionItem(
                            icon = Icons.Default.LocationOn,
                            label = "Locations\n& Bins",
                            onClick = { viewModel.navigateTo(WarehouseScreen.Warehouses) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_warehouses"
                        )
                        QuickActionItem(
                            icon = Icons.Default.Inventory2,
                            label = "Suppliers\nCatalog",
                            onClick = { viewModel.navigateTo(WarehouseScreen.Suppliers) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_suppliers"
                        )
                    }
                    UserRole.WAREHOUSE_MANAGER -> {
                        QuickActionItem(
                            icon = Icons.Default.QrCodeScanner,
                            label = "Scan\nBarcode",
                            onClick = { viewModel.navigateTo(WarehouseScreen.Scanner) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_scan"
                        )
                        QuickActionItem(
                            icon = Icons.Default.CheckCircle,
                            label = "Transfer\nApprovals",
                            onClick = { viewModel.navigateTo(WarehouseScreen.Approvals) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_approvals"
                        )
                        QuickActionItem(
                            icon = Icons.Default.CompareArrows,
                            label = "Stock\nTransfer",
                            onClick = { showTransferDialog = true },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_transfer"
                        )
                        QuickActionItem(
                            icon = Icons.Default.Assessment,
                            label = "Analytics\nReports",
                            onClick = { viewModel.navigateTo(WarehouseScreen.Reports) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_reports"
                        )
                    }
                    UserRole.WAREHOUSE_EMPLOYEE -> {
                        QuickActionItem(
                            icon = Icons.Default.QrCodeScanner,
                            label = "Scan\nBarcode",
                            onClick = { viewModel.navigateTo(WarehouseScreen.Scanner) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_scan"
                        )
                        QuickActionItem(
                            icon = Icons.Default.LocationOn,
                            label = "Find Bin\nLocation",
                            onClick = { viewModel.navigateTo(WarehouseScreen.FindProduct) },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_find"
                        )
                        QuickActionItem(
                            icon = Icons.Default.AddBox,
                            label = "Receive\nStock In",
                            onClick = { showStockInDialog = true },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_stock_in"
                        )
                        QuickActionItem(
                            icon = Icons.Default.CompareArrows,
                            label = "Issue\nStock Out",
                            onClick = { showStockOutDialog = true },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_stock_out"
                        )
                    }
                }
            }
        }

        // 4b. Warehouse Dashboard Overview Card (PDF Section 21 / Use Case 19, Page 15)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("warehouse_status_overview_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF084B34))
                            )
                            Text(
                                text = "WAREHOUSE DASHBOARD",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PDF Spec Page 15",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF084B34)
                            )
                        }
                    }

                    // Top Row: Products & Assets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardMetricBox(
                            title = "Products",
                            value = "${products.size}",
                            subtitle = "Catalog Items",
                            onClick = { viewModel.navigateTo(WarehouseScreen.Products) },
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricBox(
                            title = "Assets",
                            value = "${assets.size}",
                            subtitle = "Physical Units",
                            onClick = { viewModel.navigateTo(WarehouseScreen.Assets) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Middle Section: Available Stock, Low Stock, Expiring Products, Pending Transfers
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Available Stock", fontSize = 13.sp, color = Color(0xFF475569))
                            Text(
                                "${products.sumOf { it.quantity }} units",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF084B34)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(WarehouseScreen.InventoryDashboard) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Low Stock", fontSize = 13.sp, color = Color(0xFF475569))
                                if (lowStockProducts.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Alert", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                    }
                                }
                            }
                            Text(
                                "${lowStockProducts.size} items",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lowStockProducts.isNotEmpty()) Color(0xFFD97706) else Color(0xFF64748B)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Expiring Products", fontSize = 13.sp, color = Color(0xFF475569))
                            Text(
                                "${expiringProducts.size} items",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (expiringProducts.isNotEmpty()) Color(0xFFE11D48) else Color(0xFF64748B)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(WarehouseScreen.Approvals) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pending Transfers", fontSize = 13.sp, color = Color(0xFF475569))
                            Text(
                                "${pendingTransfers.size} pending",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingTransfers.isNotEmpty()) Color(0xFF0284C7) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Bottom Section: Recent Activity (Stock In, Stock Out, Asset Transfer)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        val latestStockIn = transactions.firstOrNull { it.transactionType == TransactionType.STOCK_IN }
                        val latestStockOut = transactions.firstOrNull { it.transactionType == TransactionType.STOCK_OUT }
                        val latestTransfer = transactions.firstOrNull { it.transactionType == TransactionType.STOCK_TRANSFER }

                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                        DashboardActivityRow(
                            type = "Stock In",
                            details = latestStockIn?.let { "${it.productName} (+${it.quantity})" } ?: "Laptop Charger (+100)",
                            time = latestStockIn?.timestamp?.let { timeFormat.format(Date(it)) } ?: "10:32",
                            color = Color(0xFF084B34)
                        )
                        DashboardActivityRow(
                            type = "Stock Out",
                            details = latestStockOut?.let { "${it.productName} (-${it.quantity})" } ?: "USB Cable (-15)",
                            time = latestStockOut?.timestamp?.let { timeFormat.format(Date(it)) } ?: "10:10",
                            color = Color(0xFFD97706)
                        )
                        DashboardActivityRow(
                            type = "Asset Transfer",
                            details = latestTransfer?.let { "${it.productName} -> ${it.destinationLocationId}" } ?: "Forklift FL-9821 -> Sec C",
                            time = latestTransfer?.timestamp?.let { timeFormat.format(Date(it)) } ?: "09:45",
                            color = Color(0xFF6D28D9)
                        )
                    }
                }
            }
        }

        // 4c. Interactive Inventory Analytics & Visual Chart Card (D3 / Recharts Visualizer)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(WarehouseScreen.InventoryDashboard) }
                    .testTag("home_inventory_analytics_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF084B34)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Analytics Visualizer",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Inventory Analytics & Bar Chart",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Interactive Recharts / D3 stock visualizer with safety threshold and restock tray",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color(0xFF047857)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Analytics",
                        tint = Color(0xFF084B34),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 5. Active Warehouse Inventory Section (Replicating "Nearby Stations" layout from Screenshot 1)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Warehouse Inventory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "See All →",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF084B34),
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(WarehouseScreen.Products) }
                        .padding(4.dp)
                )
            }
        }

        // Product Cards
        items(displayedProducts, key = { it.productId }) { product ->
            val matchingLocation = locations.find { it.locationId == product.locationId }
            val locationText = matchingLocation?.fullHierarchyPath ?: "Bin: ${product.locationId}"

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(WarehouseScreen.Products) }
                    .testTag("dashboard_product_${product.productId}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Item Photo Thumbnail
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = product.imageUrl.ifBlank {
                                "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80"
                            },
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Item Info & Pills
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1
                            )

                            // Status Pill Badge (like "Open Now" in Screenshot 1)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (product.quantity <= product.minimumStockLevel) Color(0xFFFEF3C7) else Color(0xFFE8F5E9)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (product.quantity <= product.minimumStockLevel) "Low Stock" else "In Stock",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (product.quantity <= product.minimumStockLevel) Color(0xFF92400E) else Color(0xFF065F46)
                                )
                            }
                        }

                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            maxLines = 1
                        )

                        // Feature Pills (matching "QR Payment • Wallet Only" in Screenshot 1)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Barcode: ${product.sku}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${product.quantity} Units",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF065F46)
                                )
                            }
                        }
                    }

                    // Chevron Arrow
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 6. "Top up your wallet / Quick Stock-In" Banner Card (Matching bottom card in Screenshot 1)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStockInDialog = true }
                    .testTag("quick_stock_in_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddBox,
                                contentDescription = null,
                                tint = Color(0xFF065F46),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Quick Stock In",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Receive shipment or replenish bin in seconds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF084B34)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Operational Dialogs
    if (showStockInDialog) {
        StockInDialog(
            products = products,
            locations = locations,
            preselectedProductId = null,
            onDismiss = { showStockInDialog = false },
            onConfirm = { prodId, qty, locId, ref, notes ->
                viewModel.executeStockIn(prodId, qty, locId, ref, notes)
                showStockInDialog = false
            }
        )
    }

    if (showStockOutDialog) {
        StockOutDialog(
            products = products,
            preselectedProductId = null,
            onDismiss = { showStockOutDialog = false },
            onConfirm = { prodId, qty, dest, ref, notes ->
                viewModel.executeStockOut(prodId, qty, dest, ref, notes)
                showStockOutDialog = false
            }
        )
    }

    if (showTransferDialog) {
        StockTransferDialog(
            products = products,
            warehouses = warehouses,
            locations = locations,
            preselectedProductId = null,
            onDismiss = { showTransferDialog = false },
            onConfirm = { prodId, qty, destWhId, destLocId, ref, notes ->
                viewModel.executeStockTransfer(prodId, qty, destWhId, destLocId, ref, notes)
                showTransferDialog = false
            }
        )
    }

    if (showNotificationsDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showNotificationsDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                            text = "Operational Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { showNotificationsDialog = false }) {
                            Icon(Icons.Default.Check, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    if (notifications.isEmpty()) {
                        Text("No active alerts. All operations normal.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    } else {
                        notifications.take(5).forEach { notif ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                    Text(notif.message, fontSize = 11.sp, color = Color(0xFF475569))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRoleDialog) {
        RoleSwitcherDialog(
            currentUser = currentUser,
            onRoleSelected = { role ->
                viewModel.switchUserRole(role)
                showRoleDialog = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }
}

@Composable
private fun RoleSwitcherDialog(
    currentUser: com.example.data.local.entity.UserEntity?,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("role_switcher_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Switch User Role",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "PDF Use Case 1: Role-Based Authorization",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                currentUser?.let { user ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("Active: ${user.fullName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF084B34))
                            Text("Role: ${user.role.name} • Warehouse: ${user.warehouseAssignedId}", fontSize = 11.sp, color = Color(0xFF065F46))
                        }
                    }
                }

                RoleSelectCard(
                    title = "Administrator",
                    subtitle = "Sarah Jenkins • Full system, users, warehouses, audit logs",
                    isSelected = currentUser?.role == UserRole.ADMIN,
                    onClick = {
                        onRoleSelected(UserRole.ADMIN)
                        onDismiss()
                    }
                )

                RoleSelectCard(
                    title = "Warehouse Manager",
                    subtitle = "David Vance • Transfers, inventory approvals, reports",
                    isSelected = currentUser?.role == UserRole.WAREHOUSE_MANAGER,
                    onClick = {
                        onRoleSelected(UserRole.WAREHOUSE_MANAGER)
                        onDismiss()
                    }
                )

                RoleSelectCard(
                    title = "Warehouse Employee",
                    subtitle = "Marcus (Emp 104) • Scanning, receiving, stock in/out, bin finder",
                    isSelected = currentUser?.role == UserRole.WAREHOUSE_EMPLOYEE,
                    onClick = {
                        onRoleSelected(UserRole.WAREHOUSE_EMPLOYEE)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun RoleSelectCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF8FAFC))
            .border(
                1.5.dp,
                if (isSelected) Color(0xFF084B34) else Color(0xFFE2E8F0),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFF084B34) else Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun DashboardMetricBox(
    title: String,
    value: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F5F9))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF084B34))
            Text(subtitle, fontSize = 10.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun DashboardActivityRow(
    type: String,
    details: String,
    time: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$type: $details",
                fontSize = 12.sp,
                color = Color(0xFF334155),
                maxLines = 1
            )
        }
        Text(
            text = time,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
    }
}

/**
 * Clean squircle quick-action button matching Screenshot 1 (Scan QR, Top Up, Link Bank, Find Stations).
 */
@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F5E9))
                .border(1.dp, Color(0xFFD1E7DD), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF084B34),
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
