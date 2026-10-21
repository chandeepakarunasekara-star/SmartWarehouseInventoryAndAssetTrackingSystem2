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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.WarehouseEntity
import com.example.ui.components.MinimalCard
import com.example.ui.theme.SageTeal
import com.example.ui.theme.SageTealContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Screen displaying the Warehouse hierarchy:
 * Warehouse -> Section -> Rack -> Shelf -> Bin Location.
 * Enables inspecting what inventory products and physical assets reside in each bin.
 */
@Composable
fun WarehouseLocationScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val warehouses by viewModel.warehouses.collectAsState()
    val locations by viewModel.allLocations.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val assets by viewModel.assets.collectAsState()

    var selectedWarehouseId by remember { mutableStateOf(warehouses.firstOrNull()?.warehouseId ?: "WH-NORTH") }
    var selectedLocation by remember { mutableStateOf<LocationEntity?>(null) }

    val filteredLocations = locations.filter { it.warehouseId == selectedWarehouseId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("warehouses_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Warehouse Facility Switcher
        Text(
            text = "Distribution Facilities",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(warehouses) { wh ->
                val isSelected = wh.warehouseId == selectedWarehouseId
                WarehouseFacilityCard(
                    warehouse = wh,
                    isSelected = isSelected,
                    onClick = {
                        selectedWarehouseId = wh.warehouseId
                        selectedLocation = null
                    }
                )
            }
        }

        // 2. Storage Locations Grid List (Section -> Rack -> Shelf -> Bin)
        Text(
            text = "Hierarchical Storage Locations (${filteredLocations.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredLocations) { loc ->
                val isLocSelected = selectedLocation?.locationId == loc.locationId
                val occupyingProducts = products.filter { it.locationId == loc.locationId }
                val occupyingAssets = assets.filter { it.locationId == loc.locationId }
                val occupancyRate = (loc.currentOccupancy.toFloat() / loc.maxCapacity.toFloat()).coerceIn(0f, 1f)

                MinimalCard(onClick = { selectedLocation = if (isLocSelected) null else loc }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isLocSelected) SageTealContainer else Slate100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridOn,
                                        contentDescription = null,
                                        tint = if (isLocSelected) SageTeal else Slate600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = loc.binCode,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Section ${loc.section} • Rack ${loc.rack} • Shelf ${loc.shelf}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                }
                            }

                            Text(
                                text = "${loc.currentOccupancy} / ${loc.maxCapacity} units",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                        }

                        // Utilization bar
                        LinearProgressIndicator(
                            progress = { occupancyRate },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SageTeal,
                            trackColor = Slate200
                        )

                        // Contents Breakdown
                        if (isLocSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate100)
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Current Contents in ${loc.binCode}:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )

                                    if (occupyingProducts.isEmpty() && occupyingAssets.isEmpty()) {
                                        Text(
                                            text = "Bin is currently empty. Ready for Stock In.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500
                                        )
                                    }

                                    occupyingProducts.forEach { p ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• ${p.name} (${p.sku})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate800
                                            )
                                            Text(
                                                text = "${p.quantity} units",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SageTeal
                                            )
                                        }
                                    }

                                    occupyingAssets.forEach { a ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• [Asset] ${a.name} (${a.assetCode})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate800
                                            )
                                            Text(
                                                text = a.status.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate600
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WarehouseFacilityCard(
    warehouse: WarehouseEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SageTealContainer else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) SageTeal else MaterialTheme.colorScheme.outline
        ),
        onClick = onClick,
        modifier = Modifier.size(width = 210.dp, height = 110.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = warehouse.code,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SageTeal
                )
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = if (isSelected) SageTeal else Slate400,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = warehouse.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 1
            )

            Text(
                text = "Capacity: ${warehouse.totalCapacityUnits} units",
                style = MaterialTheme.typography.labelSmall,
                color = Slate600
            )
        }
    }
}
