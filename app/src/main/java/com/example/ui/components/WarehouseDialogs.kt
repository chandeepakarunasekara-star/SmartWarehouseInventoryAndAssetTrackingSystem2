package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LocationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.WarehouseEntity
import com.example.domain.model.AssetStatus
import com.example.domain.model.UserRole
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MutedCoral
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletPrimary

/**
 * Dialog for recording incoming inventory stock receipts (Stock In - Use Case 4 & 26).
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
    var quantityText by remember { mutableStateOf("100") }
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
                        text = "Receive Stock (Stock In)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Product selector
                Column {
                    Text("Select Product to Receive", style = MaterialTheme.typography.labelMedium, color = Slate700)
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
                    label = { Text("Quantity Delivered (Units)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stock_in_quantity_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Destination Location
                Column {
                    Text("Target Storage Location (Bin)", style = MaterialTheme.typography.labelMedium, color = Slate700)
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
                    label = { Text("PO / Supplier Delivery Reference") },
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
                        text = "Confirm Receipt",
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
 * Dialog for issuing goods or stock-out dispatch with inventory validation (Use Case 5).
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
    var quantityText by remember { mutableStateOf("10") }
    var destinationText by remember { mutableStateOf("Dispatch Bay 3") }
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
                        text = "Issue Stock (Stock Out)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
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
                    label = { Text("Destination / Customer Order / Bay") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Reference
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("Dispatch Order Reference") },
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
                                errorMessage = "Cannot deduct $qty! Only ${prod.quantity} available in stock."
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
 * Dialog for relocating stock across locations or warehouses (Use Case 6).
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
    var quantityText by remember { mutableStateOf("20") }
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
                        text = "Transfer Stock (Use Case 6)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
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

                // Target Storage Location
                Column {
                    Text("Target Storage Location (Bin)", style = MaterialTheme.typography.labelMedium, color = Slate700)
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
                        text = "Request Transfer",
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
 * Dialog for registering a new Product in the master catalog with Photo / Image URL support.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddProductDialog(
    categories: List<CategoryEntity>,
    suppliers: List<SupplierEntity>,
    locations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (sku: String, barcode: String, name: String, desc: String, catId: String, supId: String, price: Double, qty: Int, minStock: Int, locId: String, whId: String, imageUrl: String, expiryDate: String) -> Unit
) {
    var sku by remember { mutableStateOf("PRD-" + System.currentTimeMillis().toString().takeLast(4)) }
    var barcode by remember { mutableStateOf("789" + System.currentTimeMillis().toString().takeLast(8)) }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("45.00") }
    var qtyText by remember { mutableStateOf("50") }
    var minStockText by remember { mutableStateOf("20") }
    var expiryDate by remember { mutableStateOf("2030-12-31") }
    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=400&q=80") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    val samplePhotos = listOf(
        Pair("Charger", "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=400&q=80"),
        Pair("Laptop", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=400&q=80"),
        Pair("Cables", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=400&q=80"),
        Pair("Labels", "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?auto=format&fit=crop&w=400&q=80"),
        Pair("Chemical", "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=400&q=80"),
        Pair("Battery", "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?auto=format&fit=crop&w=400&q=80"),
        Pair("Safety Gear", "https://images.unsplash.com/photo-1578894381163-e72c17f2d45f?auto=format&fit=crop&w=400&q=80")
    )

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
                        text = "Add Product to Catalog",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Photo Preview and Device Picker Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate100)
                            .border(1.dp, Slate200, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Product image preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Slate400)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Warehouse Item Photo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate700)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MinimalButton(
                                text = "Gallery Photo",
                                icon = Icons.Default.AddPhotoAlternate,
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).height(38.dp)
                            )
                            MinimalOutlinedButton(
                                text = "Camera",
                                icon = Icons.Default.CameraAlt,
                                onClick = {
                                    imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80"
                                },
                                modifier = Modifier.weight(1f).height(38.dp)
                            )
                        }
                    }
                }

                Column {
                    Text("Or Choose Photo Preset:", style = MaterialTheme.typography.labelSmall, color = Slate600)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        samplePhotos.forEach { (label, url) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (imageUrl == url) EmeraldPrimary else Slate100)
                                    .clickable { imageUrl = url }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (imageUrl == url) MaterialTheme.colorScheme.onPrimary else Slate700
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Photo URL or Device URI") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name (e.g. Laptop Charger)") },
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
                        label = { Text("Initial Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Min Stock Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Unit Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

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
                        text = "Save Product",
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
                            val locId = selectedLoc?.locationId ?: "LOC-WHA-B-R03-S02"
                            val whId = selectedLoc?.warehouseId ?: "WH-NORTH"

                            onConfirm(sku, barcode, name, desc, catId, supId, price, qty, minStock, locId, whId, imageUrl, expiryDate)
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
 * Dialog for registering physical tools and equipment assets with photo presets (Use Case 9).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterAssetDialog(
    warehouses: List<WarehouseEntity>,
    locations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, category: String, serial: String, status: AssetStatus, whId: String, locId: String, emp: String, imageUrl: String) -> Unit
) {
    var code by remember { mutableStateOf("AST-000" + System.currentTimeMillis().toString().takeLast(3)) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Heavy Equipment") }
    var serial by remember { mutableStateOf("FL-" + System.currentTimeMillis().toString().takeLast(4)) }
    var assignedEmployee by remember { mutableStateOf("Employee 104") }
    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80") }

    val assetPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    val assetPresets = listOf(
        Pair("Forklift", "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80"),
        Pair("Pallet Truck", "https://images.unsplash.com/photo-1580674684081-7617fbf3d745?auto=format&fit=crop&w=400&q=80"),
        Pair("Scanner", "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80"),
        Pair("Printer", "https://images.unsplash.com/photo-1612815154858-60aa4c59eaa6?auto=format&fit=crop&w=400&q=80"),
        Pair("Platform Scale", "https://images.unsplash.com/photo-1532634922-8fe0b757fb13?auto=format&fit=crop&w=400&q=80"),
        Pair("Hand Truck", "https://images.unsplash.com/photo-1586528116493-a029325540fa?auto=format&fit=crop&w=400&q=80")
    )

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
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Asset photo preview and picker buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate100)
                            .border(1.dp, Slate200, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Asset photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Slate400)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Equipment Photo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate700)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MinimalButton(
                                text = "Gallery Photo",
                                icon = Icons.Default.AddPhotoAlternate,
                                onClick = {
                                    assetPhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f).height(38.dp)
                            )
                            MinimalOutlinedButton(
                                text = "Camera",
                                icon = Icons.Default.CameraAlt,
                                onClick = {
                                    imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80"
                                },
                                modifier = Modifier.weight(1f).height(38.dp)
                            )
                        }
                    }
                }

                Column {
                    Text("Or Select Equipment Preset:", style = MaterialTheme.typography.labelSmall, color = Slate600)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        assetPresets.forEach { (label, url) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (imageUrl == url) VioletPrimary else Slate100)
                                    .clickable { imageUrl = url }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (imageUrl == url) MaterialTheme.colorScheme.onPrimary else Slate700
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Photo URL or Device URI") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name (e.g. Electric Forklift)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_asset_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Asset ID Code") },
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
                    label = { Text("Assigned Employee") },
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
                            val locId = selectedLoc?.locationId ?: "LOC-WHA-A-R01-S01"
                            val whId = selectedLoc?.warehouseId ?: "WH-NORTH"
                            val status = if (assignedEmployee.isNotBlank()) AssetStatus.ASSIGNED else AssetStatus.AVAILABLE
                            onConfirm(code, name, category, serial, status, whId, locId, assignedEmployee, imageUrl)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "save_new_asset_button"
                    )
                }
            }
        }
    }
}

/**
 * Dialog for Administrator creating user accounts (Use Case 23).
 */
@Composable
fun CreateUserDialog(
    warehouses: List<WarehouseEntity>,
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, username: String, email: String, role: UserRole, warehouseId: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.WAREHOUSE_EMPLOYEE) }
    var selectedWarehouse by remember { mutableStateOf(warehouses.firstOrNull()) }
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
                        text = "Create User Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name (e.g. Elena Rostova)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Corporate Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Role selection chips
                Column {
                    Text("Assigned Role", style = MaterialTheme.typography.labelMedium, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedRole == role) IndigoPrimary else Slate100)
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (role) {
                                        UserRole.ADMIN -> "Admin"
                                        UserRole.WAREHOUSE_MANAGER -> "Manager"
                                        UserRole.WAREHOUSE_EMPLOYEE -> "Employee"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRole == role) MaterialTheme.colorScheme.onPrimary else Slate700
                                )
                            }
                        }
                    }
                }

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
                        text = "Create User",
                        onClick = {
                            if (fullName.isBlank() || username.isBlank()) {
                                errorMessage = "Please enter full name and username."
                                return@MinimalButton
                            }
                            val whId = selectedWarehouse?.warehouseId ?: "WH-NORTH"
                            onConfirm(fullName, username, email.ifBlank { "$username@smartwarehouse.net" }, selectedRole, whId)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
