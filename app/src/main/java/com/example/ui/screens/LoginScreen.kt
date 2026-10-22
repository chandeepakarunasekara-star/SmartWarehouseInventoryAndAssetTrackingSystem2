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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserRole
import com.example.ui.components.MinimalButton
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MutedAmber
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Login & User Authentication screen (Use Case 1).
 * Authenticates credentials, determines user role, and routes to role-specific dashboard.
 */
@Composable
fun LoginScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("emp_104") }
    var password by remember { mutableStateOf("warehouse123") }
    var selectedRole by remember { mutableStateOf(UserRole.WAREHOUSE_EMPLOYEE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(IndigoPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warehouse,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Smart Warehouse",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Text(
            text = "Inventory & Asset Tracking System",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Sign In to Terminal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username / Employee ID") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("login_username_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate500) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                )

                Text(
                    text = "Select Role to Authenticate:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleSelectPill(
                        label = "Employee",
                        role = UserRole.WAREHOUSE_EMPLOYEE,
                        isSelected = selectedRole == UserRole.WAREHOUSE_EMPLOYEE,
                        icon = Icons.Default.Engineering,
                        color = EmeraldPrimary,
                        onClick = {
                            selectedRole = UserRole.WAREHOUSE_EMPLOYEE
                            username = "emp_104"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    RoleSelectPill(
                        label = "Manager",
                        role = UserRole.WAREHOUSE_MANAGER,
                        isSelected = selectedRole == UserRole.WAREHOUSE_MANAGER,
                        icon = Icons.Default.SupervisorAccount,
                        color = AmberPrimary,
                        onClick = {
                            selectedRole = UserRole.WAREHOUSE_MANAGER
                            username = "mgr_david"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    RoleSelectPill(
                        label = "Admin",
                        role = UserRole.ADMIN,
                        isSelected = selectedRole == UserRole.ADMIN,
                        icon = Icons.Default.AdminPanelSettings,
                        color = IndigoPrimary,
                        onClick = {
                            selectedRole = UserRole.ADMIN
                            username = "admin_sarah"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                MinimalButton(
                    text = "Log In",
                    onClick = { viewModel.login(username, selectedRole) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "login_submit_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Info footer
        Text(
            text = "Role-based access enforced by Room DB & Spring Boot backend",
            style = MaterialTheme.typography.labelSmall,
            color = Slate500
        )
    }
}

@Composable
private fun RoleSelectPill(
    label: String,
    role: UserRole,
    isSelected: Boolean,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else Slate100)
            .border(
                1.5.dp,
                if (isSelected) color else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else Slate600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else Slate700
            )
        }
    }
}
