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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Sign Up Screen using Material3 theme with rounded corners.
 * Implements Firebase Auth registration logic with email/password validation
 * and navigation flow back to Login.
 */
@Composable
fun SignUpScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.WAREHOUSE_EMPLOYEE) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("signup_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Icon
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(EmeraldPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warehouse,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Text(
            text = "Join Smart Warehouse Tracking System",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Registration Card
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
                    text = "Operator Information",
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

                // Full Name Field
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = null
                    },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate500) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_name_input")
                )

                // Email Address Field
                val isEmailValid = email.isBlank() || FirebaseAuthManager.isValidEmail(email)
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Work Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Slate500) },
                    isError = !isEmailValid,
                    supportingText = if (!isEmailValid) {
                        { Text("Please enter a valid email (e.g. name@warehouse.com)", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_email_input")
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password (min 6 chars)") },
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
                        .testTag("signup_password_input")
                )

                // Confirm Password Field
                val passwordsMatch = confirmPassword.isEmpty() || confirmPassword == password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate500) },
                    isError = !passwordsMatch,
                    supportingText = if (!passwordsMatch) {
                        { Text("Passwords do not match", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_confirm_password_input")
                )

                // Role Selection
                Text(
                    text = "Assign Terminal Access Role:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SignUpRolePill(
                        label = "Employee",
                        role = UserRole.WAREHOUSE_EMPLOYEE,
                        isSelected = selectedRole == UserRole.WAREHOUSE_EMPLOYEE,
                        icon = Icons.Default.Engineering,
                        color = EmeraldPrimary,
                        onClick = { selectedRole = UserRole.WAREHOUSE_EMPLOYEE },
                        modifier = Modifier.weight(1f)
                    )
                    SignUpRolePill(
                        label = "Manager",
                        role = UserRole.WAREHOUSE_MANAGER,
                        isSelected = selectedRole == UserRole.WAREHOUSE_MANAGER,
                        icon = Icons.Default.SupervisorAccount,
                        color = AmberPrimary,
                        onClick = { selectedRole = UserRole.WAREHOUSE_MANAGER },
                        modifier = Modifier.weight(1f)
                    )
                    SignUpRolePill(
                        label = "Admin",
                        role = UserRole.ADMIN,
                        isSelected = selectedRole == UserRole.ADMIN,
                        icon = Icons.Default.AdminPanelSettings,
                        color = IndigoPrimary,
                        onClick = { selectedRole = UserRole.ADMIN },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sign Up Action Button
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(36.dp))
                    }
                } else {
                    MinimalButton(
                        text = "Create Account",
                        onClick = {
                            if (fullName.isBlank()) {
                                errorMessage = "Please enter your full name."
                                return@MinimalButton
                            }
                            if (!FirebaseAuthManager.isValidEmail(email)) {
                                errorMessage = "Please enter a valid work email address."
                                return@MinimalButton
                            }
                            val (validPass, passErr) = FirebaseAuthManager.validatePassword(password)
                            if (!validPass) {
                                errorMessage = passErr ?: "Password must be at least 6 characters."
                                return@MinimalButton
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match."
                                return@MinimalButton
                            }

                            isLoading = true
                            errorMessage = null
                            viewModel.firebaseSignUp(
                                email = email,
                                password = password,
                                fullName = fullName,
                                role = selectedRole
                            ) { success, err ->
                                isLoading = false
                                if (!success) {
                                    errorMessage = err ?: "Registration failed."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "signup_submit_button"
                    )
                }

                // Switch to Sign In Flow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    TextButton(
                        onClick = { viewModel.navigateTo(WarehouseScreen.Login) },
                        modifier = Modifier.testTag("switch_to_login_button")
                    ) {
                        Text(
                            text = "Sign In",
                            color = IndigoPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignUpRolePill(
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
