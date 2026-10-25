package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * AddItemScreen composable with text fields for item name, SKU, and quantity,
 * styled to match the third screen in the reference design ("Add Funds / Form").
 * Includes an 'Add to Database' button that inserts directly into the Room database.
 */
@Composable
fun AddItemScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val locations by viewModel.allLocations.collectAsState()

    var itemName by remember { mutableStateOf("") }
    var itemSku by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("50") }
    var itemUnitPrice by remember { mutableStateOf("34.50") }
    var selectedLocation by remember { mutableStateOf("LOC-WH1-A1") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var itemImageUrl by remember {
        mutableStateOf("https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80")
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val categories = listOf("Electronics", "Equipment", "Consumables", "Storage Tools")
    val quickQuantities = listOf("10", "25", "50", "100", "250", "500")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            itemImageUrl = uri.toString()
        }
    }

    val samplePhotos = listOf(
        Pair("Industrial Forklift", "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=400&q=80"),
        Pair("Rugged Tablet", "https://images.unsplash.com/photo-1580674684081-7617fbf3d745?auto=format&fit=crop&w=400&q=80"),
        Pair("Laser Scanner", "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=400&q=80"),
        Pair("Pallet Jack", "https://images.unsplash.com/photo-1587293852726-70cdb56c2866?auto=format&fit=crop&w=400&q=80")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("add_item_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    .testTag("add_item_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A)
                )
            }

            Text(
                text = "Add Warehouse Item",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Room SQLite",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )
            }
        }

        // 2. Forest Emerald Balance Card (like top balance card in Screenshot 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF032B1E),
                            Color(0xFF084B34),
                            Color(0xFF0E6948)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Catalog Inventory Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA7F3D0)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${allProducts.size} SKUs Registered",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Success / Error Feedback Banners
        if (successMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDCFCE7))
                    .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = successMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14532D)
                    )
                }
            }
        }

        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFE4E6))
                    .border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9F1239)
                    )
                }
            }
        }

        // 3. Category Selector Tabs (like "Choose a top-up method" in Screenshot 3)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Choose a category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEachIndexed { index, cat ->
                    val isSelected = index == selectedCategoryIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF084B34) else Color.White)
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF084B34) else Color(0xFFE2E8F0),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedCategoryIndex = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 4. Input Fields Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = itemName,
                    onValueChange = {
                        itemName = it
                        errorMessage = null
                    },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Industrial Barcode Scanner Handheld") },
                    leadingIcon = {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF084B34))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF084B34),
                        focusedLabelColor = Color(0xFF084B34)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_item_name_input")
                )

                // SKU (Stock Keeping Unit)
                OutlinedTextField(
                    value = itemSku,
                    onValueChange = {
                        itemSku = it
                        errorMessage = null
                    },
                    label = { Text("SKU / Barcode *") },
                    placeholder = { Text("e.g. SKU-WHS-9021 or 78912345601") },
                    leadingIcon = {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF084B34))
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                itemSku = "SKU-" + System.currentTimeMillis().toString().takeLast(6)
                            }
                        ) {
                            Text("Auto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF084B34))
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF084B34),
                        focusedLabelColor = Color(0xFF084B34)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_item_sku_input")
                )

                // Quantity
                OutlinedTextField(
                    value = itemQuantity,
                    onValueChange = {
                        itemQuantity = it
                        errorMessage = null
                    },
                    label = { Text("Quantity in Stock *") },
                    placeholder = { Text("e.g. 50") },
                    leadingIcon = {
                        Icon(Icons.Default.Numbers, contentDescription = null, tint = Color(0xFF084B34))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF084B34),
                        focusedLabelColor = Color(0xFF084B34)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_item_quantity_input")
                )

                // Unit Price & Bin Location Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = itemUnitPrice,
                        onValueChange = { itemUnitPrice = it },
                        label = { Text("Unit Price ($)") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF084B34))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = { selectedLocation = it },
                        label = { Text("Storage Bin") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF084B34))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Quick Quantity Selector (like "Quick Amount: 50, 100, 200, 500" in Screenshot 3)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Quantity",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickQuantities.forEach { qty ->
                            val isSelected = itemQuantity == qty
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF084B34) else Color(0xFFF1F5F9))
                                    .clickable { itemQuantity = qty }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = qty,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Item Visual Photo Preview & Selection
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Item Visual / Photo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color(0xFF065F46),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery Photo", color = Color(0xFF065F46), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Current Photo Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = itemImageUrl,
                        contentDescription = "Selected item photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Sample Preset Thumbnails
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    samplePhotos.forEach { (presetName, url) ->
                        val isSelected = itemImageUrl == url
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    2.dp,
                                    if (isSelected) Color(0xFF084B34) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { itemImageUrl = url },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = presetName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // 6. Big Rounded Pill Button: 'Add to Database'
        // (Replicating the prominent green "Top Up Now" pill button from Screenshot 3)
        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF084B34))
            }
        } else {
            Button(
                onClick = {
                    if (itemName.isBlank()) {
                        errorMessage = "Please enter an item name."
                        return@Button
                    }
                    if (itemSku.isBlank()) {
                        errorMessage = "Please enter a valid SKU or barcode."
                        return@Button
                    }
                    val qty = itemQuantity.toIntOrNull()
                    if (qty == null || qty < 0) {
                        errorMessage = "Please enter a valid quantity (0 or greater)."
                        return@Button
                    }
                    val price = itemUnitPrice.toDoubleOrNull() ?: 29.99

                    isSubmitting = true
                    errorMessage = null
                    successMessage = null

                    viewModel.insertWarehouseItem(
                        name = itemName,
                        sku = itemSku,
                        quantity = qty,
                        unitPrice = price,
                        categoryId = "CAT-" + categories[selectedCategoryIndex].take(4).uppercase(),
                        locationId = selectedLocation,
                        imageUrl = itemImageUrl
                    ) { success, err ->
                        isSubmitting = false
                        if (success) {
                            successMessage = "Successfully inserted '$itemName' into Room Database!"
                            // Reset form
                            itemName = ""
                            itemSku = ""
                            itemQuantity = "50"
                        } else {
                            errorMessage = err ?: "Failed to insert into database."
                        }
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF084B34)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("add_item_submit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to Database",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
