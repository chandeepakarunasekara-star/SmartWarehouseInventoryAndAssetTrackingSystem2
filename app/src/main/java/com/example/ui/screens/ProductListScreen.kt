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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProductEntity
import com.example.domain.model.StockLevelStatus
import com.example.ui.components.AddProductDialog
import com.example.ui.components.MinimalCard
import com.example.ui.components.MinimalOutlinedButton
import com.example.ui.components.MinimalSearchBar
import com.example.ui.components.StockInDialog
import com.example.ui.components.StockOutDialog
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.SageTeal
import com.example.ui.theme.SageTealContainer
import com.example.ui.theme.SageTealOnContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Product Catalog management screen with search, category filtering,
 * stock status inspection, and direct stock adjustment triggers.
 */
@Composable
fun ProductListScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val locations by viewModel.allLocations.collectAsState()
    val searchQuery by viewModel.productSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedStockStatus by viewModel.selectedStockFilter.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var stockInTargetProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var stockOutTargetProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("products_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                MinimalSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.productSearchQuery.value = it }
                )
            }

            // 2. Category Filter Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChipPill(
                        label = "All Categories",
                        isSelected = selectedCategory == null,
                        onClick = { viewModel.selectedCategoryFilter.value = null }
                    )
                }
                items(categories) { cat ->
                    FilterChipPill(
                        label = cat.name,
                        isSelected = selectedCategory == cat.categoryId,
                        onClick = {
                            viewModel.selectedCategoryFilter.value =
                                if (selectedCategory == cat.categoryId) null else cat.categoryId
                        }
                    )
                }
            }

            // 3. Stock Level Filter Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChipPill(
                        label = "All Stock",
                        isSelected = selectedStockStatus == null,
                        onClick = { viewModel.selectedStockFilter.value = null }
                    )
                }
                item {
                    FilterChipPill(
                        label = "Low Stock Alerts",
                        isSelected = selectedStockStatus == StockLevelStatus.LOW_STOCK,
                        onClick = {
                            viewModel.selectedStockFilter.value =
                                if (selectedStockStatus == StockLevelStatus.LOW_STOCK) null else StockLevelStatus.LOW_STOCK
                        }
                    )
                }
                item {
                    FilterChipPill(
                        label = "In Stock",
                        isSelected = selectedStockStatus == StockLevelStatus.IN_STOCK,
                        onClick = {
                            viewModel.selectedStockFilter.value =
                                if (selectedStockStatus == StockLevelStatus.IN_STOCK) null else StockLevelStatus.IN_STOCK
                        }
                    )
                }
            }

            // 4. Products List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No products found matching filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate500
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.productId }) { product ->
                        val matchingLocation = locations.find { it.locationId == product.locationId }
                        val stockStatus = when {
                            product.quantity == 0 -> StockLevelStatus.OUT_OF_STOCK
                            product.quantity <= product.minimumStockLevel -> StockLevelStatus.LOW_STOCK
                            else -> StockLevelStatus.IN_STOCK
                        }

                        ProductItemCard(
                            product = product,
                            locationText = matchingLocation?.fullHierarchyPath ?: product.locationId,
                            stockStatus = stockStatus,
                            onStockIn = { stockInTargetProduct = product },
                            onStockOut = { stockOutTargetProduct = product }
                        )
                    }
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            categories = categories,
            suppliers = suppliers,
            locations = locations,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { sku, barcode, name, desc, catId, supId, price, qty, minStock, locId, whId ->
                viewModel.createProduct(sku, barcode, name, desc, catId, supId, price, qty, minStock, locId, whId)
                showAddProductDialog = false
            }
        )
    }

    // Stock In Dialog
    stockInTargetProduct?.let { prod ->
        StockInDialog(
            products = allProducts,
            locations = locations,
            preselectedProductId = prod.productId,
            onDismiss = { stockInTargetProduct = null },
            onConfirm = { prodId, qty, locId, ref, notes ->
                viewModel.executeStockIn(prodId, qty, locId, ref, notes)
                stockInTargetProduct = null
            }
        )
    }

    // Stock Out Dialog
    stockOutTargetProduct?.let { prod ->
        StockOutDialog(
            products = allProducts,
            preselectedProductId = prod.productId,
            onDismiss = { stockOutTargetProduct = null },
            onConfirm = { prodId, qty, dest, ref, notes ->
                viewModel.executeStockOut(prodId, qty, dest, ref, notes)
                stockOutTargetProduct = null
            }
        )
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) SageTealContainer else Slate100)
            .border(
                1.dp,
                if (isSelected) SageTeal else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) SageTealOnContainer else Slate700
        )
    }
}

@Composable
private fun ProductItemCard(
    product: ProductEntity,
    locationText: String,
    stockStatus: StockLevelStatus,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit
) {
    MinimalCard(modifier = Modifier.testTag("product_card_${product.sku}")) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top row: Name & Stock Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SKU: ${product.sku} • Barcode: ${product.barcode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
                StockStatusBadge(status = stockStatus)
            }

            // Description if available
            if (product.description.isNotBlank()) {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    maxLines = 2
                )
            }

            // Location hierarchy breadcrumb
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }

            // Bottom row: Quantity, Price, and Quick Stock In/Out Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${product.quantity} units",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", product.unitPrice)}/unit • Min: ${product.minimumStockLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MinimalOutlinedButton(
                        text = "+ In",
                        onClick = onStockIn,
                        modifier = Modifier.height(38.dp)
                    )
                    MinimalOutlinedButton(
                        text = "- Out",
                        onClick = onStockOut,
                        modifier = Modifier.height(38.dp)
                    )
                }
            }
        }
    }
}
