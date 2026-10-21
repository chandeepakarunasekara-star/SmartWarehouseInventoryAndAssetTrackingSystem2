package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
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
import com.example.ui.components.MinimalCard
import com.example.ui.components.MetricCounterCard
import com.example.ui.components.StockInDialog
import com.example.ui.components.StockOutDialog
import com.example.ui.components.StockStatusBadge
import com.example.ui.components.StockTransferDialog
import com.example.ui.theme.MutedAmber
import com.example.ui.theme.MutedAmberContainer
import com.example.ui.theme.MutedCoral
import com.example.ui.theme.SageTeal
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dashboard screen providing real-time warehouse inventory metrics,
 * low stock alerts, quick transactional triggers, and recent movement logs.
 */
@Composable
fun DashboardScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
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
        // 1. Metric Overview Cards (2x2 Grid Layout)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCounterCard(
                        title = "Catalog SKUs",
                        value = "${products.size}",
                        subtitle = "$totalStockUnits total units",
                        icon = Icons.Default.Inventory,
                        accentColor = SageTeal,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(WarehouseScreen.Products) }
                    )
                    MetricCounterCard(
                        title = "Stock Alerts",
                        value = "${lowStockProducts.size}",
                        subtitle = "Needs replenishment",
                        icon = Icons.Default.WarningAmber,
                        accentColor = if (lowStockProducts.isNotEmpty()) MutedCoral else SageTeal,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectedStockFilter.value = StockLevelStatus.LOW_STOCK
                            viewModel.navigateTo(WarehouseScreen.Products)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCounterCard(
                        title = "Active Assets",
                        value = "${assets.size}",
                        subtitle = "Tracked equipment",
                        icon = Icons.Default.Build,
                        accentColor = Slate700,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(WarehouseScreen.Assets) }
                    )
                    MetricCounterCard(
                        title = "Sync Queue",
                        value = "$pendingSyncCount",
                        subtitle = if (pendingSyncCount == 0) "All changes synced" else "Pending upload",
                        icon = Icons.Default.CompareArrows,
                        accentColor = if (pendingSyncCount > 0) MutedAmber else SageTeal,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(WarehouseScreen.SyncQueue) }
                    )
                }
            }
        }

        // 2. Quick Operations Action Bar
        item {
            Column {
                Text(
                    text = "Quick Operations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        label = "Scan",
                        icon = Icons.Default.QrCodeScanner,
                        color = SageTeal,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_scan_action",
                        onClick = { viewModel.navigateTo(WarehouseScreen.Scanner) }
                    )
                    QuickActionButton(
                        label = "Stock In",
                        icon = Icons.Default.Add,
                        color = SageTeal,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_stock_in_action",
                        onClick = { showStockInDialog = true }
                    )
                    QuickActionButton(
                        label = "Stock Out",
                        icon = Icons.Default.Remove,
                        color = MutedCoral,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_stock_out_action",
                        onClick = { showStockOutDialog = true }
                    )
                    QuickActionButton(
                        label = "Transfer",
                        icon = Icons.Default.CompareArrows,
                        color = Slate800,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_transfer_action",
                        onClick = { showTransferDialog = true }
                    )
                }
            }
        }

        // 3. Low Stock Warning Ribbon (if any products are low)
        if (lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MutedAmberContainer)
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
                                .background(MutedAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MutedAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${lowStockProducts.size} item(s) below reorder threshold",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                            Text(
                                text = "e.g., ${lowStockProducts.first().name} (${lowStockProducts.first().quantity} remaining)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }

                        IconButton(onClick = {
                            viewModel.selectedStockFilter.value = StockLevelStatus.LOW_STOCK
                            viewModel.navigateTo(WarehouseScreen.Products)
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "View alerts",
                                tint = Slate900
                            )
                        }
                    }
                }
            }
        }

        // 4. Recent Transactions Log
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = SageTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(4.dp)
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                MinimalCard {
                    Text(
                        text = "No recent transactions logged yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500
                    )
                }
            }
        } else {
            items(recentTransactions.take(6)) { txn ->
                RecentTransactionRow(txn)
            }
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
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.testTag(testTag),
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
                    .background(color.copy(alpha = 0.12f)),
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
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Slate900
            )
        }
    }
}

@Composable
private fun RecentTransactionRow(txn: StockTransactionEntity) {
    val (typeColor, typeSign) = when (txn.transactionType) {
        TransactionType.STOCK_IN -> Pair(SageTeal, "+")
        TransactionType.STOCK_OUT -> Pair(MutedCoral, "-")
        TransactionType.STOCK_TRANSFER -> Pair(Slate700, "⇄")
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = typeSign,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }

                Column {
                    Text(
                        text = txn.productName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${txn.referenceNumber} • by ${txn.performedByUserName}",
                        style = MaterialTheme.typography.bodySmall,
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
