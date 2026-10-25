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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthManager
import com.example.domain.model.UserRole
import com.example.ui.components.MinimalButton
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Login & User Authentication screen (Use Case 1) with Material3 theme and rounded corners.
 * Authenticates credentials via Firebase Auth with email/password validation
 * and provides navigation flow to SignUpScreen.
 */
@Composable
fun LoginScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    var emailOrUsername by remember { mutableStateOf("employee@smartwarehouse.net") }
    var password by remember { mutableStateOf("warehouse123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.WAREHOUSE_EMPLOYEE) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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

                // Error Banner if present
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFE4E6))
                            .border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9F1239),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Email / Username Input
                val isEmailFormat = emailOrUsername.contains("@")
                val isEmailValid = !isEmailFormat || FirebaseAuthManager.isValidEmail(emailOrUsername)
                OutlinedTextField(
                    value = emailOrUsername,
                    onValueChange = {
                        emailOrUsername = it
                        errorMessage = null
                    },
                    label = { Text("Email Address / Employee ID") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isEmailFormat) Icons.Default.Email else Icons.Default.Person,
                            contentDescription = null,
                            tint = Slate500
                        )
                    },
                    isError = isEmailFormat && !isEmailValid,
                    supportingText = if (isEmailFormat && !isEmailValid) {
                        { Text("Please enter a valid email address", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_username_input")
                )

                // Password Input with Visibility Toggle
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate500) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Slate500
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
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
                            emailOrUsername = "employee@smartwarehouse.net"
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
                            emailOrUsername = "manager@smartwarehouse.net"
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
                            emailOrUsername = "admin@smartwarehouse.net"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sign In Button
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IndigoPrimary, modifier = Modifier.size(36.dp))
                    }
                } else {
                    MinimalButton(
                        text = "Sign In with Firebase Auth",
                        onClick = {
                            if (emailOrUsername.isBlank()) {
                                errorMessage = "Please enter an email or username."
                                return@MinimalButton
                            }
                            if (emailOrUsername.contains("@") && !FirebaseAuthManager.isValidEmail(emailOrUsername)) {
                                errorMessage = "Please enter a valid email address."
                                return@MinimalButton
                            }
                            val (passValid, passErr) = FirebaseAuthManager.validatePassword(password)
                            if (!passValid) {
                                errorMessage = passErr ?: "Password must be at least 6 characters."
                                return@MinimalButton
                            }

                            isLoading = true
                            errorMessage = null
                            val emailToAuth = if (emailOrUsername.contains("@")) {
                                emailOrUsername.trim()
                            } else {
                                "${emailOrUsername.trim().lowercase()}@smartwarehouse.net"
                            }

                            viewModel.firebaseLogin(
                                email = emailToAuth,
                                password = password,
                                role = selectedRole
                            ) { success, err ->
                                isLoading = false
                                if (!success) {
                                    errorMessage = err ?: "Sign in failed."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_submit_button"
                    )
                }

                // Switch to Sign Up Flow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    TextButton(
                        onClick = { viewModel.navigateTo(WarehouseScreen.SignUp) },
                        modifier = Modifier.testTag("switch_to_signup_button")
                    ) {
                        Text(
                            text = "Create Account",
                            color = IndigoPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Info footer
        Text(
            text = "Secured with Firebase Authentication & Local Room DB",
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
    color: Color,
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
