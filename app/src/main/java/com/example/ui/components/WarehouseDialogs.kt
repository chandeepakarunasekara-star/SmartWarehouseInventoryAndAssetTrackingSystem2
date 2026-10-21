package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.AssetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.WarehouseEntity
import com.example.domain.model.AssetStatus
import com.example.ui.theme.MutedCoral
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700

/**
 * Dialog for recording incoming inventory stock receipts (Stock In).
 */
@Composable
fun StockInDialog(
    products: List<ProductEntity>,
    locations: List<LocationEntity>,
    preselectedProductId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (productId: String, quantity: Int, locationId: String, reference: String, notes: String) -> Unit
) {
    var selectedProduct by remember {
        mutableStateOf(products.find { it.productId == preselectedProductId } ?: products.firstOrNull())
    }
    var quantityText by remember { mutableStateOf("10") }
    var selectedLocation by remember {
        mutableStateOf(locations.find { it.locationId == selectedProduct?.locationId } ?: locations.firstOrNull())
    }
    var referenceText by remember { mutableStateOf("PO-" + System.currentTimeMillis().toString().takeLast(5)) }
    var notesText by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var locationDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock In Receipt",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Product selector
                Column {
                    Text("Select Product", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedProduct?.let { "${it.name} (${it.sku})" } ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { productDropdownExpanded = true }
                                .testTag("stock_in_product_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} (Current: ${prod.quantity})") },
                                    onClick = {
                                        selectedProduct = prod
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Quantity
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity to Receive") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stock_in_quantity_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Destination Location
                Column {
                    Text("Storage Location (Bin)", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedLocation?.fullHierarchyPath ?: "Select Location",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { locationDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = locationDropdownExpanded,
                            onDismissRequest = { locationDropdownExpanded = false }
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text("${loc.warehouseId} • ${loc.fullHierarchyPath}") },
                                    onClick = {
                                        selectedLocation = loc
                                        locationDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Reference
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("PO / Reference Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MutedCoral, fontSize = 12.sp)
                }

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
                        text = "Confirm Stock In",
                        onClick = {
                            val prod = selectedProduct
                            val loc = selectedLocation
                            val qty = quantityText.toIntOrNull()
                            if (prod == null) {
                                errorMessage = "Please select a product."
                                return@MinimalButton
                            }
                            if (qty == null || qty <= 0) {
                                errorMessage = "Quantity must be greater than zero."
                                return@MinimalButton
                            }
                            if (loc == null) {
                                errorMessage = "Please select a destination location."
                                return@MinimalButton
                            }
                            onConfirm(prod.productId, qty, loc.locationId, referenceText, notesText)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "confirm_stock_in_button"
                    )
                }
            }
        }
    }
}

/**
 * Dialog for issuing goods or stock-out dispatch with inventory validation.
 */
@Composable
fun StockOutDialog(
    products: List<ProductEntity>,
    preselectedProductId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (productId: String, quantity: Int, destination: String, reference: String, notes: String) -> Unit
) {
    var selectedProduct by remember {
        mutableStateOf(products.find { it.productId == preselectedProductId } ?: products.firstOrNull())
    }
    var quantityText by remember { mutableStateOf("1") }
    var destinationText by remember { mutableStateOf("Dispatch Bay 2") }
    var referenceText by remember { mutableStateOf("DISP-" + System.currentTimeMillis().toString().takeLast(5)) }
    var notesText by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock Out Dispatch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Product selector
                Column {
                    Text("Select Product", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedProduct?.let { "${it.name} (Available: ${it.quantity})" } ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { productDropdownExpanded = true }
                                .testTag("stock_out_product_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} (Stock: ${prod.quantity})") },
                                    onClick = {
                                        selectedProduct = prod
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Available stock note
                selectedProduct?.let {
                    Text(
                        text = "Currently Available: ${it.quantity} units (Threshold: ${it.minimumStockLevel})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (it.quantity <= it.minimumStockLevel) MutedCoral else Slate500
                    )
                }

                // Quantity
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity to Deduct") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stock_out_quantity_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Destination/Recipient
                OutlinedTextField(
                    value = destinationText,
                    onValueChange = { destinationText = it },
                    label = { Text("Destination / Recipient") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Reference
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("Work Order / Dispatch Ref") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MutedCoral, fontSize = 12.sp)
                }

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
                        text = "Confirm Stock Out",
                        onClick = {
                            val prod = selectedProduct
                            val qty = quantityText.toIntOrNull()
                            if (prod == null) {
                                errorMessage = "Please select a product."
                                return@MinimalButton
                            }
                            if (qty == null || qty <= 0) {
                                errorMessage = "Quantity must be greater than zero."
                                return@MinimalButton
                            }
                            if (qty > prod.quantity) {
                                errorMessage = "Cannot deduct $qty! Only ${prod.quantity} available."
                                return@MinimalButton
                            }
                            onConfirm(prod.productId, qty, destinationText, referenceText, notesText)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "confirm_stock_out_button"
                    )
                }
            }
        }
    }
}

/**
 * Dialog for relocating stock across locations or warehouses.
 */
@Composable
fun StockTransferDialog(
    products: List<ProductEntity>,
    warehouses: List<WarehouseEntity>,
    locations: List<LocationEntity>,
    preselectedProductId: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (productId: String, quantity: Int, destinationWarehouseId: String, destinationLocationId: String, ref: String, notes: String) -> Unit
) {
    var selectedProduct by remember {
        mutableStateOf(products.find { it.productId == preselectedProductId } ?: products.firstOrNull())
    }
    var quantityText by remember { mutableStateOf("5") }
    var selectedWarehouse by remember { mutableStateOf(warehouses.firstOrNull()) }
    var selectedLocation by remember { mutableStateOf(locations.firstOrNull()) }
    var referenceText by remember { mutableStateOf("XFR-" + System.currentTimeMillis().toString().takeLast(5)) }
    var notesText by remember { mutableStateOf("") }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var locDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock Relocation Transfer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Product selector
                Column {
                    Text("Select Product", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedProduct?.let { "${it.name} (${it.quantity} avail)" } ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { productDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} (Stock: ${prod.quantity})") },
                                    onClick = {
                                        selectedProduct = prod
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Transfer quantity
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Transfer Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Destination Location
                Column {
                    Text("Target Storage Location", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        OutlinedTextField(
                            value = selectedLocation?.fullHierarchyPath ?: "Select Location",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { locDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = locDropdownExpanded,
                            onDismissRequest = { locDropdownExpanded = false }
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text("${loc.warehouseId} • ${loc.fullHierarchyPath}") },
                                    onClick = {
                                        selectedLocation = loc
                                        locDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Reference
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("Transfer Authorization Ref") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MutedCoral, fontSize = 12.sp)
                }

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
                        text = "Execute Transfer",
                        onClick = {
                            val prod = selectedProduct
                            val loc = selectedLocation
                            val whId = selectedWarehouse?.warehouseId ?: "WH-NORTH"
                            val qty = quantityText.toIntOrNull()
                            if (prod == null) {
                                errorMessage = "Please select a product."
                                return@MinimalButton
                            }
                            if (qty == null || qty <= 0) {
                                errorMessage = "Quantity must be greater than zero."
                                return@MinimalButton
                            }
                            if (qty > prod.quantity) {
                                errorMessage = "Cannot transfer $qty! Only ${prod.quantity} available."
                                return@MinimalButton
                            }
                            if (loc == null) {
                                errorMessage = "Please select a destination location."
                                return@MinimalButton
                            }
                            onConfirm(prod.productId, qty, whId, loc.locationId, referenceText, notesText)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "confirm_transfer_button"
                    )
                }
            }
        }
    }
}

/**
 * Dialog for registering a new Product in the master catalog.
 */
@Composable
fun AddProductDialog(
    categories: List<CategoryEntity>,
    suppliers: List<SupplierEntity>,
    locations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (sku: String, barcode: String, name: String, desc: String, catId: String, supId: String, price: Double, qty: Int, minStock: Int, locId: String, whId: String) -> Unit
) {
    var sku by remember { mutableStateOf("PRD-" + System.currentTimeMillis().toString().takeLast(4)) }
    var barcode by remember { mutableStateOf("789" + System.currentTimeMillis().toString().takeLast(8)) }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("49.99") }
    var qtyText by remember { mutableStateOf("50") }
    var minStockText by remember { mutableStateOf("15") }
    var selectedCat by remember { mutableStateOf(categories.firstOrNull()) }
    var selectedSup by remember { mutableStateOf(suppliers.firstOrNull()) }
    var selectedLoc by remember { mutableStateOf(locations.firstOrNull()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New Product Catalog",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_product_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU Code") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Initial Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Min Stock Level") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Unit Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MutedCoral, fontSize = 12.sp)
                }

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
                        text = "Create Product",
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Product name cannot be empty."
                                return@MinimalButton
                            }
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            val qty = qtyText.toIntOrNull() ?: 0
                            val minStock = minStockText.toIntOrNull() ?: 10
                            val catId = selectedCat?.categoryId ?: "CAT-01"
                            val supId = selectedSup?.supplierId ?: "SUP-01"
                            val locId = selectedLoc?.locationId ?: "LOC-WH1-A11"
                            val whId = selectedLoc?.warehouseId ?: "WH-NORTH"

                            onConfirm(sku, barcode, name, desc, catId, supId, price, qty, minStock, locId, whId)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "save_new_product_button"
                    )
                }
            }
        }
    }
}

/**
 * Dialog for registering physical tools and equipment assets.
 */
@Composable
fun RegisterAssetDialog(
    warehouses: List<WarehouseEntity>,
    locations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, category: String, serial: String, status: AssetStatus, whId: String, locId: String, emp: String) -> Unit
) {
    var code by remember { mutableStateOf("AST-EQ-" + System.currentTimeMillis().toString().takeLast(3)) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Material Handling") }
    var serial by remember { mutableStateOf("SN-" + System.currentTimeMillis().toString().takeLast(6)) }
    var assignedEmployee by remember { mutableStateOf("") }
    var selectedLoc by remember { mutableStateOf(locations.firstOrNull()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Register Physical Asset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name (e.g. Scissor Lift 2000)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_asset_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Asset Tag Code") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = serial,
                    onValueChange = { serial = it },
                    label = { Text("Serial Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = assignedEmployee,
                    onValueChange = { assignedEmployee = it },
                    label = { Text("Assigned Employee (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MutedCoral, fontSize = 12.sp)
                }

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
                        text = "Register Asset",
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Asset name cannot be blank."
                                return@MinimalButton
                            }
                            val locId = selectedLoc?.locationId ?: "LOC-WH1-A11"
                            val whId = selectedLoc?.warehouseId ?: "WH-NORTH"
                            val status = if (assignedEmployee.isNotBlank()) AssetStatus.ASSIGNED else AssetStatus.AVAILABLE
                            onConfirm(code, name, category, serial, status, whId, locId, assignedEmployee)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "save_new_asset_button"
                    )
                }
            }
        }
    }
}
