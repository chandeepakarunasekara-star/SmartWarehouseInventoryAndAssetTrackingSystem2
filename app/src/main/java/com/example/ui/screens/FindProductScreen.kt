package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.StockLevelStatus
import com.example.ui.components.MinimalCard
import com.example.ui.components.MinimalSearchBar
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.CyanOnContainer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Screen for locating warehouse items quickly (Use Case 7: Finding a Product).
 * Instantly displays warehouse facility, section, rack, shelf, bin, and available quantity.
 */
@Composable
fun FindProductScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val locations by viewModel.allLocations.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val matchedProducts = allProducts.filter { prod ->
        searchQuery.isBlank() ||
            prod.name.contains(searchQuery, ignoreCase = true) ||
            prod.sku.contains(searchQuery, ignoreCase = true) ||
            prod.barcode.contains(searchQuery, ignoreCase = true) ||
            prod.qrCode.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("find_product_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyanContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "Find Warehouse Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Search by Name, SKU, Barcode, or QR code to find exact bin location",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }
        }

        // Search Bar
        MinimalSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search 'Laptop Charger', SKU, Barcode..."
        )

        // Matched Results
        if (matchedProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No products found matching \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matchedProducts, key = { it.productId }) { prod ->
                    val loc = locations.find { it.locationId == prod.locationId }
                    val wh = warehouses.find { it.warehouseId == prod.warehouseId }

                    LocationLookupCard(
                        product = prod,
                        warehouseName = wh?.name ?: prod.warehouseId,
                        section = loc?.section ?: "B",
                        rack = loc?.rack ?: "03",
                        shelf = loc?.shelf ?: "02",
                        bin = loc?.binCode ?: "BIN-01"
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationLookupCard(
    product: com.example.data.local.entity.ProductEntity,
    warehouseName: String,
    section: String,
    rack: String,
    shelf: String,
    bin: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().testTag("lookup_card_${product.sku}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Product & Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                        )
                    }

                    Column {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "SKU: ${product.sku} • Barcode: ${product.barcode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(EmeraldPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${product.quantity} Available",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
            }

            // Location Box (matching PDF page 7 specification)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyanContainer)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Warehouse: $warehouseName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanOnContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Section: $section", style = MaterialTheme.typography.bodySmall, color = CyanOnContainer, fontWeight = FontWeight.SemiBold)
                        Text("Rack: $rack", style = MaterialTheme.typography.bodySmall, color = CyanOnContainer, fontWeight = FontWeight.SemiBold)
                        Text("Shelf: $shelf", style = MaterialTheme.typography.bodySmall, color = CyanOnContainer, fontWeight = FontWeight.SemiBold)
                        Text("Bin: $bin", style = MaterialTheme.typography.bodySmall, color = CyanOnContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
