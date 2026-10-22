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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole
import com.example.ui.components.CreateUserDialog
import com.example.ui.components.MinimalCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MutedAmber
import com.example.ui.theme.MutedCoral
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Administrator User Management Console (Use Case 23).
 * Manage employee accounts, assign roles, and activate/deactivate user permissions.
 */
@Composable
fun UserManagementScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()
    var showCreateUserDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("user_management_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateUserDialog = true },
                shape = RoundedCornerShape(16.dp),
                containerColor = IndigoPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_user_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IndigoPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "User Administration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Assign roles, set active status, and configure warehouse assignments",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }

            // User List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(users, key = { it.userId }) { user ->
                    UserItemCard(
                        user = user,
                        onToggleActive = { viewModel.toggleUserActive(user.userId, user.isActive) }
                    )
                }
            }
        }
    }

    if (showCreateUserDialog) {
        CreateUserDialog(
            warehouses = warehouses,
            onDismiss = { showCreateUserDialog = false },
            onConfirm = { name, uname, email, role, whId ->
                viewModel.createUser(name, uname, email, role, whId)
                showCreateUserDialog = false
            }
        )
    }
}

@Composable
private fun UserItemCard(
    user: UserEntity,
    onToggleActive: () -> Unit
) {
    val (roleIcon, roleColor) = when (user.role) {
        UserRole.ADMIN -> Pair(Icons.Default.AdminPanelSettings, IndigoPrimary)
        UserRole.WAREHOUSE_MANAGER -> Pair(Icons.Default.SupervisorAccount, MutedAmber)
        UserRole.WAREHOUSE_EMPLOYEE -> Pair(Icons.Default.Engineering, EmeraldPrimary)
    }

    MinimalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = roleIcon,
                        contentDescription = null,
                        tint = roleColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "${user.email} • ID: ${user.userId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(roleColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = user.role.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = roleColor
                            )
                        }

                        Text(
                            text = if (user.isActive) "Active" else "Deactivated",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (user.isActive) EmeraldPrimary else MutedCoral
                        )
                    }
                }
            }

            Switch(
                checked = user.isActive,
                onCheckedChange = { onToggleActive() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = EmeraldPrimary,
                    checkedTrackColor = EmeraldPrimary.copy(alpha = 0.3f)
                )
            )
        }
    }
}
