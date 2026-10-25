package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.SupplierEntity
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Suppliers Management screen (PDF Section 2, Page 1 & 20).
 * Allows warehouse administrators to register, view, search, and manage vendors/suppliers
 * with direct Room SQLite database persistence and product linkage.
 */
@Composable
fun SuppliersScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        if (searchQuery.isBlank()) {
            suppliers
        } else {
            suppliers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.contactPerson.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("suppliers_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSupplierDialog = true },
                containerColor = Color(0xFF084B34),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_supplier")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAF9))
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header
            item {
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
                            .testTag("suppliers_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Suppliers Management",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${suppliers.size} Registered Vendors • Room SQLite",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Admin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF084B34)
                        )
                    }
                }
            }

            // 2. Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by supplier name, contact, email...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF084B34),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_suppliers_input")
                )
            }

            // 3. Suppliers List
            if (filteredSuppliers.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No suppliers found matching '$searchQuery'.",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredSuppliers, key = { it.supplierId }) { supplier ->
                    val suppliedProductsCount = products.count { it.supplierId == supplier.supplierId }
                    SupplierCard(
                        supplier = supplier,
                        suppliedProductsCount = suppliedProductsCount,
                        onDelete = { viewModel.deleteSupplier(supplier.supplierId) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { showAddSupplierDialog = false },
            onConfirm = { name, contact, email, phone, address ->
                viewModel.createSupplier(name, contact, email, phone, address)
                showAddSupplierDialog = false
            }
        )
    }
}

@Composable
private fun SupplierCard(
    supplier: SupplierEntity,
    suppliedProductsCount: Int,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplier_card_${supplier.supplierId}")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Color(0xFF084B34),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = supplier.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "ID: ${supplier.supplierId}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_supplier_${supplier.supplierId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            // Contact & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = supplier.contactPerson.ifBlank { "Primary Rep" },
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Color(0xFF084B34),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$suppliedProductsCount items supplied",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF084B34)
                    )
                }
            }

            // Address
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = supplier.address.ifBlank { "Main Logistics Facility" },
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Actions row: Call / Email
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${supplier.phone}")
                        }
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF084B34), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = supplier.phone.ifBlank { "Call" }, fontSize = 11.sp, color = Color(0xFF084B34), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${supplier.email}")
                        }
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF084B34), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Email", fontSize = 11.sp, color = Color(0xFF084B34), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, contact: String, email: String, phone: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("add_supplier_dialog")
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
                        text = "Register New Supplier",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Company Name *") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("supplier_name_input")
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Person") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Physical Warehouse Address") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { err ->
                    Text(text = err, color = Color(0xFFEF4444), fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Company name is required."
                            return@Button
                        }
                        onConfirm(name, contact, email, phone, address)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF084B34)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_add_supplier")
                ) {
                    Text("Save to Room SQLite", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
