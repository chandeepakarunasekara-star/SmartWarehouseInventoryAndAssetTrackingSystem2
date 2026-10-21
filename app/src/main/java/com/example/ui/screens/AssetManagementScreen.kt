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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.AssetMovementEntity
import com.example.data.local.entity.LocationEntity
import com.example.domain.model.AssetStatus
import com.example.ui.components.AssetStatusBadge
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.components.MinimalOutlinedButton
import com.example.ui.components.RegisterAssetDialog
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen managing physical warehouse assets (forklifts, scanners, pallet jacks),
 * maintenance status, employee assignments, and location movements.
 */
@Composable
fun AssetManagementScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.assets.collectAsState()
    val movements by viewModel.repository.observeAllAssetMovements().collectAsState(initial = emptyList())
    val warehouses by viewModel.warehouses.collectAsState()
    val locations by viewModel.allLocations.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Assets, 1: Movement Logs
    var selectedStatusFilter by remember { mutableStateOf<AssetStatus?>(null) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var movingAssetTarget by remember { mutableStateOf<AssetEntity?>(null) }

    val filteredAssets = assets.filter { asset ->
        selectedStatusFilter == null || asset.status == selectedStatusFilter
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("assets_screen"),
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showRegisterDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("register_asset_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Register Asset")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs: Assets Catalog vs Movement Log
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SageTeal
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Equipment Assets (${assets.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Movement History", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                // Status Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        AssetFilterPill(
                            label = "All Status",
                            isSelected = selectedStatusFilter == null,
                            onClick = { selectedStatusFilter = null }
                        )
                    }
                    items(listOf(AssetStatus.AVAILABLE, AssetStatus.IN_USE, AssetStatus.ASSIGNED, AssetStatus.UNDER_MAINTENANCE)) { status ->
                        AssetFilterPill(
                            label = status.name.replace("_", " "),
                            isSelected = selectedStatusFilter == status,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == status) null else status
                            }
                        )
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAssets, key = { it.assetId }) { asset ->
                        val matchingLoc = locations.find { it.locationId == asset.locationId }

                        MinimalCard(modifier = Modifier.testTag("asset_card_${asset.assetCode}")) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = asset.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tag: ${asset.assetCode} • Serial: ${asset.serialNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }
                                    AssetStatusBadge(status = asset.status)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (asset.assignedEmployeeName.isNotBlank()) {
                                            "Assigned to: ${asset.assignedEmployeeName}"
                                        } else {
                                            "Unassigned • Available on warehouse floor"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = matchingLoc?.fullHierarchyPath ?: asset.locationId,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Condition: ${asset.condition}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                    MinimalOutlinedButton(
                                        text = "Move / Reassign",
                                        onClick = { movingAssetTarget = asset },
                                        modifier = Modifier.height(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Movement History List
                val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(movements) { mov ->
                        val formattedDate = remember(mov.timestamp) { dateFormatter.format(Date(mov.timestamp)) }

                        MinimalCard {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mov.assetName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mov.movementType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SageTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = "From ${mov.fromLocationId} ➔ To ${mov.toLocationId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )

                                if (mov.newEmployee.isNotBlank()) {
                                    Text(
                                        text = "Reassigned: ${mov.previousEmployee.ifBlank { "None" }} ➔ ${mov.newEmployee}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate600
                                    )
                                }

                                Text(
                                    text = "Authorized by ${mov.performedBy} • $formattedDate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Register Asset Dialog
    if (showRegisterDialog) {
        RegisterAssetDialog(
            warehouses = warehouses,
            locations = locations,
            onDismiss = { showRegisterDialog = false },
            onConfirm = { code, name, category, serial, status, whId, locId, emp ->
                viewModel.registerAsset(code, name, category, serial, status, whId, locId, emp)
                showRegisterDialog = false
            }
        )
    }

    // Move Asset Dialog
    movingAssetTarget?.let { asset ->
        MoveAssetDialog(
            asset = asset,
            locations = locations,
            onDismiss = { movingAssetTarget = null },
            onConfirm = { toLoc, newEmp, type, notes ->
                viewModel.executeAssetMovement(asset.assetId, toLoc, newEmp, type, notes)
                movingAssetTarget = null
            }
        )
    }
}

@Composable
private fun AssetFilterPill(
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
            .padding(horizontal = 14.dp, vertical = 6.dp),
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
private fun MoveAssetDialog(
    asset: AssetEntity,
    locations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (toLocationId: String, newEmployee: String, movementType: String, notes: String) -> Unit
) {
    var selectedLocation by remember { mutableStateOf(locations.firstOrNull()) }
    var employeeInput by remember { mutableStateOf(asset.assignedEmployeeName) }
    var movementType by remember { mutableStateOf("Relocation") }
    var notes by remember { mutableStateOf("") }

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
                        text = "Move Asset: ${asset.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = employeeInput,
                    onValueChange = { employeeInput = it },
                    label = { Text("Assignee Employee") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = movementType,
                    onValueChange = { movementType = it },
                    label = { Text("Movement Type (e.g. Relocation, Shift Assign)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MinimalOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    MinimalButton(
                        text = "Confirm Move",
                        onClick = {
                            val locId = selectedLocation?.locationId ?: "LOC-WH1-B21"
                            onConfirm(locId, employeeInput, movementType, notes)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
