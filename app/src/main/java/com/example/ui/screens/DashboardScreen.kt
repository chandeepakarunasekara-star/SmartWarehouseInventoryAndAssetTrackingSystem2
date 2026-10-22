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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StockTransactionEntity
import com.example.domain.model.StockLevelStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserRole
import com.example.ui.components.MetricCounterCard
import com.example.ui.components.MinimalCard
import com.example.ui.components.StockInDialog
import com.example.ui.components.StockOutDialog
import com.example.ui.components.StockTransferDialog
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberOnContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletContainer
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptive, role-tailored Dashboard screen (Use Cases 1, 14, 15, 19).
 * Dynamically customizes metrics and action items based on user role:
 * - Administrator
 * - Warehouse Manager
 * - Warehouse Employee
 */
@Composable
fun DashboardScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val expiringProducts by viewModel.expiringProducts.collectAsState()
    val pendingTransfers by viewModel.pendingTransfers.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val syncQueue by viewModel.syncQueue.collectAsState()
    val locations by viewModel.allLocations.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()

    var showStockInDialog by remember { mutableStateOf(false) }
    var showStockOutDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    val totalStockUnits = products.sumOf { it.quantity }
    val pendingSyncCount = syncQueue.count { it.status.name == "PENDING" || it.status.name == "FAILED" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Role Welcome & Profile Banner
        item {
            val (roleBannerBg, roleIcon, roleColor) = when (currentUser.role) {
                UserRole.ADMIN -> Triple(IndigoContainer, Icons.Default.AdminPanelSettings, IndigoPrimary)
                UserRole.WAREHOUSE_MANAGER -> Triple(AmberContainer, Icons.Default.SupervisorAccount, AmberPrimary)
                UserRole.WAREHOUSE_EMPLOYEE -> Triple(EmeraldContainer, Icons.Default.Engineering, EmeraldPrimary)
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = roleBannerBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = null,
                            tint = roleColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Terminal Mode: ${currentUser.role.name.replace("_", " ")}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = roleColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .clickable { viewModel.navigateTo(WarehouseScreen.Settings) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Switch Role",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = roleColor
                        )
                    }
                }
            }
        }

        // 2. Overview Metrics (Products, Assets, Stock, Low Stock, Expiring, Transfers)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCounterCard(
                        title = "Catalog Products",
                        value = "${products.size}",
                        subtitle = "$totalStockUnits units in stock",
                        icon = Icons.Default.Inventory,
                        accentColor = IndigoPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(WarehouseScreen.Products) }
                    )
                    MetricCounterCard(
                        title = "Physical Assets",
                        value = "${assets.size}",
                        subtitle = "Tracked equipment",
                        icon = Icons.Default.Build,
                        accentColor = VioletPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(WarehouseScreen.Assets) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCounterCard(
                        title = "Low Stock Alerts",
                        value = "${lowStockProducts.size}",
                        subtitle = "Needs reordering",
                        icon = Icons.Default.WarningAmber,
                        accentColor = if (lowStockProducts.isNotEmpty()) RosePrimary else EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectedStockFilter.value = StockLevelStatus.LOW_STOCK
                            viewModel.navigateTo(WarehouseScreen.Products)
                        }
                    )
                    MetricCounterCard(
                        title = "Expiring Items",
                        value = "${expiringProducts.size}",
                        subtitle = "Approaching date",
                        icon = Icons.Default.HourglassBottom,
                        accentColor = AmberPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(WarehouseScreen.Reports) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCounterCard(
                        title = "Pending Transfers",
                        value = "${pendingTransfers.size}",
                        subtitle = if (pendingTransfers.isEmpty()) "All approved" else "Needs manager review",
                        icon = Icons.Default.CompareArrows,
                        accentColor = if (pendingTransfers.isNotEmpty()) AmberPrimary else IndigoPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(WarehouseScreen.Approvals) }
                    )
                    MetricCounterCard(
                        title = "Sync Queue",
                        value = "$pendingSyncCount",
                        subtitle = if (pendingSyncCount == 0) "Synced to MySQL" else "Pending sync",
                        icon = Icons.Default.EventNote,
                        accentColor = if (pendingSyncCount > 0) AmberPrimary else EmeraldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(WarehouseScreen.SyncQueue) }
                    )
                }
            }
        }

        // 3. Role-Specific Action Grid (matching PDF page 3!)
        item {
            Text(
                text = when (currentUser.role) {
                    UserRole.ADMIN -> "Administrator Master Controls"
                    UserRole.WAREHOUSE_MANAGER -> "Manager Operations & Approvals"
                    UserRole.WAREHOUSE_EMPLOYEE -> "Warehouse Floor Actions"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(6.dp))

            when (currentUser.role) {
                UserRole.WAREHOUSE_EMPLOYEE -> {
                    // Employee: Scan, Stock In/Out, Assets, Locations (Find Items), History
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionTile("Scan Code", Icons.Default.QrCodeScanner, IndigoPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Scanner)
                            }
                            ActionTile("Stock In", Icons.Default.Add, EmeraldPrimary, Modifier.weight(1f)) {
                                showStockInDialog = true
                            }
                            ActionTile("Stock Out", Icons.Default.Remove, RosePrimary, Modifier.weight(1f)) {
                                showStockOutDialog = true
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionTile("Find Item", Icons.Default.LocationOn, CyanPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.FindProduct)
                            }
                            ActionTile("Move Asset", Icons.Default.Build, VioletPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Assets)
                            }
                            ActionTile("History", Icons.Default.History, Slate700, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Transactions)
                            }
                        }
                    }
                }
                UserRole.WAREHOUSE_MANAGER -> {
                    // Manager: Inventory, Transfers, Assets, Employees, Reports, Approvals
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionTile("Approvals (${pendingTransfers.size})", Icons.Default.Verified, AmberPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Approvals)
                            }
                            ActionTile("Transfer Stock", Icons.Default.CompareArrows, IndigoPrimary, Modifier.weight(1f)) {
                                showTransferDialog = true
                            }
                            ActionTile("Inventory", Icons.Default.Inventory, EmeraldPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Products)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionTile("Assets", Icons.Default.Build, VioletPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Assets)
                            }
                            ActionTile("Reports", Icons.Default.Assessment, CyanPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Reports)
                            }
                            ActionTile("Locations", Icons.Default.Business, Slate700, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Warehouses)
                            }
                        }
                    }
                }
                UserRole.ADMIN -> {
                    // Admin: Users, Products, Warehouses, Inventory, Assets, Reports, Settings
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionTile("Users Admin", Icons.Default.Group, IndigoPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.UserManagement)
                            }
                            ActionTile("Catalog", Icons.Default.Inventory, EmeraldPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Products)
                            }
                            ActionTile("Warehouses", Icons.Default.Business, CyanPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Warehouses)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActionTile("Assets", Icons.Default.Build, VioletPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Assets)
                            }
                            ActionTile("Reports", Icons.Default.Assessment, AmberPrimary, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Reports)
                            }
                            ActionTile("Settings", Icons.Default.AdminPanelSettings, Slate700, Modifier.weight(1f)) {
                                viewModel.navigateTo(WarehouseScreen.Settings)
                            }
                        }
                    }
                }
            }
        }

        // 4. Low Stock Warning Card
        if (lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${lowStockProducts.size} item(s) below safety stock level",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "${lowStockProducts.first().name} (${lowStockProducts.first().quantity} remaining)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }

                        IconButton(onClick = {
                            viewModel.selectedStockFilter.value = StockLevelStatus.LOW_STOCK
                            viewModel.navigateTo(WarehouseScreen.Products)
                        }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "View alerts", tint = Slate900)
                        }
                    }
                }
            }
        }

        // 5. Recent Activity Timeline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions Log",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "View Ledger",
                    style = MaterialTheme.typography.labelSmall,
                    color = IndigoPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(WarehouseScreen.Transactions) }
                        .padding(4.dp)
                )
            }
        }

        items(recentTransactions.take(5), key = { it.transactionId }) { txn ->
            RecentTransactionTile(txn)
        }
    }

    // Dialogs
    if (showStockInDialog) {
        StockInDialog(
            products = products,
            locations = locations,
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
            onDismiss = { showTransferDialog = false },
            onConfirm = { prodId, qty, destWh, destLoc, ref, notes ->
                viewModel.executeStockTransfer(prodId, qty, destWh, destLoc, ref, notes)
                showTransferDialog = false
            }
        )
    }
}

@Composable
private fun ActionTile(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecentTransactionTile(txn: StockTransactionEntity) {
    val (typeColor, typeSign) = when (txn.transactionType) {
        TransactionType.STOCK_IN -> Pair(EmeraldPrimary, "+")
        TransactionType.STOCK_OUT -> Pair(RosePrimary, "-")
        TransactionType.STOCK_TRANSFER -> Pair(IndigoPrimary, "⇄")
    }

    val dateFormatter = remember { SimpleDateFormat("HH:mm • dd MMM", Locale.getDefault()) }
    val formattedTime = remember(txn.timestamp) { dateFormatter.format(Date(txn.timestamp)) }

    MinimalCard {
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(typeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = typeSign,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }

                Column {
                    Text(
                        text = txn.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "${txn.referenceNumber} • by ${txn.performedByUserName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$typeSign${txn.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        }
    }
}
