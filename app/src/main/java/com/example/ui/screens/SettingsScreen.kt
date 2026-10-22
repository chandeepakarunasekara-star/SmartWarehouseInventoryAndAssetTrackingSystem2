package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserRole
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.components.MinimalOutlinedButton
import com.example.ui.theme.SageTeal
import com.example.ui.theme.SageTealContainer
import com.example.ui.theme.SageTealOnContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Settings & User Profile screen.
 * Displays active employee identity, role permission matrix switcher,
 * and backend Spring Boot REST endpoint configuration.
 */
@Composable
fun SettingsScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var backendApiUrl by remember { mutableStateOf("http://10.0.2.2:8080/api") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Authenticated User Profile
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SageTealContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = SageTeal,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column {
                        Text(
                            text = currentUser.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(SageTealContainer)
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Role: ${currentUser.role.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageTealOnContainer
                            )
                        }
                    }
                }
            }
        }

        // 2. Role Switcher for Evaluation
        item {
            MinimalCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Demo Role Switcher",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Switch roles instantly to test role-based permissions throughout the app:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleChoiceChip(
                            label = "Admin",
                            role = UserRole.ADMIN,
                            isSelected = currentUser.role == UserRole.ADMIN,
                            onClick = { viewModel.switchUserRole(UserRole.ADMIN) },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChoiceChip(
                            label = "Manager",
                            role = UserRole.WAREHOUSE_MANAGER,
                            isSelected = currentUser.role == UserRole.WAREHOUSE_MANAGER,
                            onClick = { viewModel.switchUserRole(UserRole.WAREHOUSE_MANAGER) },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChoiceChip(
                            label = "Operator",
                            role = UserRole.WAREHOUSE_EMPLOYEE,
                            isSelected = currentUser.role == UserRole.WAREHOUSE_EMPLOYEE,
                            onClick = { viewModel.switchUserRole(UserRole.WAREHOUSE_EMPLOYEE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Role Permissions Matrix Card
        item {
            MinimalCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SageTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Role Authorization Matrix",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    PermissionRow("ADMIN", "Product Catalog, Warehouses, Hierarchy, System Audit Logs, Analytics")
                    PermissionRow("WAREHOUSE_MANAGER", "Inventory Transfers, Asset Allocation, Reports, Stock Adjustments")
                    PermissionRow("WAREHOUSE_EMPLOYEE", "Barcode Scanner, Stock In Receipts, Stock Out Dispatches, Asset Relocation")
                }
            }
        }

        // 4. Remote Data Source (Spring Boot REST Server & Cloud Sync)
        item {
            val remoteUrlState by viewModel.remoteApiUrl.collectAsState()
            val pingResult by viewModel.remotePingResult.collectAsState()
            val isTesting by viewModel.isTestingConnection.collectAsState()

            MinimalCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = SageTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Remote Data Source (REST API / Cloud)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Text(
                        text = "Configured endpoint for real-time synchronization, central database transactions, and user authentication:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    OutlinedTextField(
                        value = backendApiUrl,
                        onValueChange = { backendApiUrl = it },
                        label = { Text("Spring Boot / Remote API Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MinimalButton(
                            text = "Save Endpoint",
                            onClick = { viewModel.updateRemoteApiUrl(backendApiUrl) },
                            modifier = Modifier.weight(1f)
                        )
                        MinimalOutlinedButton(
                            text = if (isTesting) "Pinging..." else "Test Connection",
                            onClick = { viewModel.testRemoteConnection() },
                            enabled = !isTesting,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (pingResult != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (pingResult!!.contains("Online")) SageTealContainer else Slate100)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = pingResult!!,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (pingResult!!.contains("Online")) SageTealOnContainer else Slate700
                            )
                        }
                    }
                }
            }
        }

        // 5. Local Database Information
        item {
            MinimalCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = SageTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Storage & Database Engine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    Text(
                        text = "• Database: Room SQLite (smart_warehouse.db)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                    Text(
                        text = "• Local Storage: D:\\MyProjects\\smart-warehouse-inventory (Architecture compliant)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                    Text(
                        text = "• Offline Strategy: Transactional SyncQueue with Idempotency Validation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleChoiceChip(
    label: String,
    role: UserRole,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SageTealContainer else Slate100)
            .border(
                1.dp,
                if (isSelected) SageTeal else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) SageTealOnContainer else Slate700
        )
    }
}

@Composable
private fun PermissionRow(role: String, permissions: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SageTeal
        )
        Text(
            text = permissions,
            style = MaterialTheme.typography.bodySmall,
            color = Slate600
        )
    }
}
