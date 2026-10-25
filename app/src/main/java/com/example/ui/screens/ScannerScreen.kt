package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.StockLevelStatus
import com.example.ui.components.AssetStatusBadge
import com.example.ui.components.BarcodeScanner
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.components.MinimalOutlinedButton
import com.example.ui.components.StockInDialog
import com.example.ui.components.StockOutDialog
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.MutedCoral
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
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Camera & Hardware Barcode/QR scanner interface.
 * Supports direct camera barcode recognition, physical Bluetooth laser scanner input,
 * and test barcode shortcuts for immediate verification in the emulator environment.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScannerScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val scannedCode by viewModel.scannedCode.collectAsState()
    val productResult by viewModel.scannedProductResult.collectAsState()
    val assetResult by viewModel.scannedAssetResult.collectAsState()
    val scanErrorMessage by viewModel.scanErrorMessage.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val locations by viewModel.allLocations.collectAsState()

    var manualCodeInput by remember { mutableStateOf("") }
    var showStockInForScanned by remember { mutableStateOf(false) }
    var showStockOutForScanned by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("scanner_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Live CameraX + Google ML Kit Barcode Scanner
        BarcodeScanner(
            onBarcodeScanned = { code ->
                viewModel.handleScannedCode(code)
            },
            lastScannedBarcode = scannedCode,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        // 2. Emulator Simulation Shortcuts (Quick Barcode Taps)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Test Scanner Quick Barcodes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickScanChip("78912345601", "Casters (PRD)") { viewModel.handleScannedCode("78912345601") }
                QuickScanChip("78912345603", "Labels (Low)") { viewModel.handleScannedCode("78912345603") }
                QuickScanChip("78912345602", "Scanner Term.") { viewModel.handleScannedCode("78912345602") }
                QuickScanChip("AST-FL-01", "Forklift (AST)") { viewModel.handleScannedCode("AST-FL-01") }
                QuickScanChip("AST-PJ-04", "Pallet Jack") { viewModel.handleScannedCode("AST-PJ-04") }
            }
        }

        // 3. Manual SKU / Barcode Entry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualCodeInput,
                onValueChange = { manualCodeInput = it },
                placeholder = { Text("Or enter SKU / Barcode manually") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("manual_barcode_input")
            )
            MinimalButton(
                text = "Scan",
                onClick = {
                    if (manualCodeInput.isNotBlank()) {
                        viewModel.handleScannedCode(manualCodeInput)
                    }
                },
                testTag = "manual_scan_button"
            )
        }

        // 4. Scan Result Card: Product Found
        productResult?.let { prod ->
            val matchingLoc = locations.find { it.locationId == prod.locationId }
            val stockStatus = when {
                prod.quantity == 0 -> StockLevelStatus.OUT_OF_STOCK
                prod.quantity <= prod.minimumStockLevel -> StockLevelStatus.LOW_STOCK
                else -> StockLevelStatus.IN_STOCK
            }

            MinimalCard(modifier = Modifier.testTag("scanned_product_card")) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SageTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Product Recognized",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SageTeal
                            )
                        }
                        IconButton(onClick = { viewModel.clearScanResults() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }

                    Text(
                        text = prod.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SKU: ${prod.sku}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("Barcode: ${prod.barcode}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        }
                        StockStatusBadge(status = stockStatus)
                    }

                    Text(
                        text = "Current Stock: ${prod.quantity} units (Safety Min: ${prod.minimumStockLevel})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Storage Location: ${matchingLoc?.fullHierarchyPath ?: prod.locationId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MinimalButton(
                            text = "Stock In (+)",
                            onClick = { showStockInForScanned = true },
                            modifier = Modifier.weight(1f)
                        )
                        MinimalOutlinedButton(
                            text = "Stock Out (-)",
                            onClick = { showStockOutForScanned = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 5. Scan Result Card: Physical Asset Found
        assetResult?.let { asset ->
            MinimalCard(modifier = Modifier.testTag("scanned_asset_card")) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SageTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Equipment Asset Identified",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SageTeal
                            )
                        }
                        IconButton(onClick = { viewModel.clearScanResults() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }

                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Tag Code: ${asset.assetCode}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                            Text("Serial: ${asset.serialNumber}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        }
                        AssetStatusBadge(status = asset.status)
                    }

                    Text(
                        text = "Assigned Employee: ${asset.assignedEmployeeName.ifBlank { "Unassigned / Warehouse Floor" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    Text(
                        text = "Condition: ${asset.condition} • Inspected: ${asset.lastInspectionDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }
            }
        }

        // 6. Error message if code not recognized
        scanErrorMessage?.let { err ->
            MinimalCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MutedCoral,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Unregistered Barcode",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MutedCoral
                        )
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }
            }
        }
    }

    // Direct Stock In Dialog for Scanned Product
    if (showStockInForScanned && productResult != null) {
        StockInDialog(
            products = allProducts,
            locations = locations,
            preselectedProductId = productResult!!.productId,
            onDismiss = { showStockInForScanned = false },
            onConfirm = { prodId, qty, locId, ref, notes ->
                viewModel.executeStockIn(prodId, qty, locId, ref, notes)
                showStockInForScanned = false
                viewModel.handleScannedCode(productResult!!.barcode)
            }
        )
    }

    // Direct Stock Out Dialog for Scanned Product
    if (showStockOutForScanned && productResult != null) {
        StockOutDialog(
            products = allProducts,
            preselectedProductId = productResult!!.productId,
            onDismiss = { showStockOutForScanned = false },
            onConfirm = { prodId, qty, dest, ref, notes ->
                viewModel.executeStockOut(prodId, qty, dest, ref, notes)
                showStockOutForScanned = false
                viewModel.handleScannedCode(productResult!!.barcode)
            }
        )
    }
}

@Composable
private fun QuickScanChip(
    code: String,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Slate100)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("quick_chip_$code"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clip(RoundedCornerShape(50))
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                tint = SageTeal,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Slate800,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clip(RoundedCornerShape(50)).border(0.dp, Color.Transparent)
            )
        }
        // Transparent clickable overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(50))
                .background(Color.Transparent)
                .clickable { onClick() }
        )
    }
}
