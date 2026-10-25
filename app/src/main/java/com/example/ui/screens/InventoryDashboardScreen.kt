package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.ChartMetric
import com.example.ui.components.RechartsSummaryBarChart
import com.example.ui.components.StockInDialog
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Inventory Dashboard screen that visualizes current stock levels and low-stock alerts
 * using a custom D3 / Recharts-styled summary bar chart, backed by the fully functional Room SQLite database.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryDashboardScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val locations by viewModel.allLocations.collectAsState()

    var selectedMetric by remember { mutableStateOf(ChartMetric.QUANTITY) }
    var selectedFilterCategory by remember { mutableStateOf("All") }
    var safetyThreshold by remember { mutableIntStateOf(15) }
    var showStockInDialogForProduct by remember { mutableStateOf<ProductEntity?>(null) }

    // Calculated metrics
    val totalValuation = remember(products) { products.sumOf { it.unitPrice * it.quantity } }
    val totalUnits = remember(products) { products.sumOf { it.quantity } }
    val outOfStockCount = remember(products) { products.count { it.quantity == 0 } }
    val lowStockCount = remember(products, safetyThreshold) {
        products.count { it.quantity in 1..it.minimumStockLevel }
    }

    // Filtered list for chart visualization
    val chartProducts = remember(products, selectedFilterCategory) {
        when (selectedFilterCategory) {
            "All" -> products
            "Low Stock Only" -> products.filter { it.quantity <= it.minimumStockLevel }
            "Electronics" -> products.filter { it.categoryId.contains("ELEC", ignoreCase = true) }
            "Equipment" -> products.filter { it.categoryId.contains("EQUIP", ignoreCase = true) }
            "Consumables" -> products.filter { it.categoryId.contains("CONS", ignoreCase = true) }
            else -> products
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .padding(16.dp)
            .testTag("inventory_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            .testTag("inventory_dashboard_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }

                    Column {
                        Text(
                            text = "Inventory Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Room SQLite Stock & Alert Analytics",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Add Item Shortcut Button
                Button(
                    onClick = { viewModel.navigateTo(WarehouseScreen.AddItem) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF084B34)),
                    modifier = Modifier.testTag("dashboard_add_item_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // 2. Room SQLite Status Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF032B1E), Color(0xFF084B34))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Room Database Active",
                                color = Color(0xFF4ADE80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${products.size} Products Registered • Offline-First Persistence",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "File: smart_warehouse.db • Instant SQLite queries and reactive Room flows",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Room SQLite",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Top KPI Metric Cards (2x2 Grid)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Valuation
                MetricCard(
                    title = "Total Valuation",
                    value = "$${String.format("%,.0f", totalValuation)}",
                    subtitle = "Inventory Assets",
                    icon = Icons.Default.MonetizationOn,
                    iconColor = Color(0xFF084B34),
                    containerColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                )

                // Total Units
                MetricCard(
                    title = "Total Stock",
                    value = "${String.format("%,d", totalUnits)}",
                    subtitle = "Units on hand",
                    icon = Icons.Default.Inventory,
                    iconColor = Color(0xFF0284C7),
                    containerColor = Color(0xFFE0F2FE),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Low Stock Alerts
                MetricCard(
                    title = "Low Stock Alerts",
                    value = "$lowStockCount Items",
                    subtitle = "Below safety min",
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFD97706),
                    containerColor = Color(0xFFFEF3C7),
                    modifier = Modifier.weight(1f)
                )

                // Out of Stock
                MetricCard(
                    title = "Out of Stock",
                    value = "$outOfStockCount Items",
                    subtitle = "Zero inventory",
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFDC2626),
                    containerColor = Color(0xFFFEE2E2),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Metric Switcher & Category Filter Controls
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric Switcher: Units vs Valuation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chart Display Mode:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            MetricPill(
                                label = "Quantity (Units)",
                                selected = selectedMetric == ChartMetric.QUANTITY,
                                onClick = { selectedMetric = ChartMetric.QUANTITY }
                            )
                            MetricPill(
                                label = "Valuation ($)",
                                selected = selectedMetric == ChartMetric.VALUATION,
                                onClick = { selectedMetric = ChartMetric.VALUATION }
                            )
                        }
                    }

                    // Category Filter Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Filter by Category:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All", "Low Stock Only", "Electronics", "Equipment", "Consumables").forEach { category ->
                                val isSelected = selectedFilterCategory == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Color(0xFF084B34) else Color(0xFFF1F5F9))
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF084B34) else Color(0xFFE2E8F0),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { selectedFilterCategory = category }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Custom D3 / Recharts Interactive Summary Bar Chart
        item {
            RechartsSummaryBarChart(
                products = chartProducts,
                metric = selectedMetric,
                safetyThreshold = safetyThreshold,
                onProductSelected = { prod ->
                    // Optionally open quick stock dialog
                }
            )
        }

        // 6. Low-Stock Alerts Action Center
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Low-Stock Alerts & Restock Action",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${lowStockProducts.size} Requiring Attention",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }
            }
        }

        // Render low-stock items or empty state
        if (lowStockProducts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF084B34),
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "All Inventory Healthy",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "No items currently breach safety threshold levels.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        } else {
            items(lowStockProducts.size) { index ->
                val prod = lowStockProducts[index]
                LowStockAlertCard(
                    product = prod,
                    onQuickRestock = {
                        viewModel.quickRestockProduct(prod.productId, 25)
                    },
                    onCustomStockIn = {
                        showStockInDialogForProduct = prod
                    }
                )
            }
        }

        // 7. Live Room Database Stock Adjuster (Interactive Real-Time Test)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Test Room SQLite Real-Time Chart Reactivity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Tap buttons below to execute instant Room database transactions. The D3/Recharts bar chart updates automatically via Room Flow.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    products.take(3).forEach { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Stock: ${prod.quantity} units • Min: ${prod.minimumStockLevel}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Deduct stock button
                                Button(
                                    onClick = {
                                        if (prod.quantity > 5) {
                                            viewModel.executeStockOut(
                                                productId = prod.productId,
                                                quantity = 5,
                                                destinationNotes = "Dispatch Bay",
                                                referenceNumber = "TEST-OUT",
                                                notes = "Test chart reaction"
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }

                                // Add stock button
                                Button(
                                    onClick = {
                                        viewModel.quickRestockProduct(prod.productId, 10)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9)),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF084B34))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal Stock In Dialog if requested
    showStockInDialogForProduct?.let { prod ->
        StockInDialog(
            products = products,
            locations = locations,
            preselectedProductId = prod.productId,
            onDismiss = { showStockInDialogForProduct = null },
            onConfirm = { prodId, qty, locId, ref, notes ->
                viewModel.executeStockIn(prodId, qty, locId, ref, notes)
                showStockInDialogForProduct = null
            }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color(0xFF084B34) else Color(0xFF64748B)
        )
    }
}

@Composable
private fun LowStockAlertCard(
    product: ProductEntity,
    onQuickRestock: () -> Unit,
    onCustomStockIn: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("low_stock_card_${product.productId}")
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product Image Thumbnail
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Text(
                    text = "SKU: ${product.sku} • Bin: ${product.locationId}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Current: ${product.quantity} units",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.quantity == 0) Color(0xFFEF4444) else Color(0xFFD97706)
                    )
                    Text(
                        text = "(Min: ${product.minimumStockLevel})",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Quick Restock Button
            Button(
                onClick = onQuickRestock,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF084B34)),
                modifier = Modifier.testTag("quick_restock_${product.productId}")
            ) {
                Text(
                    text = "Restock (+25)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
