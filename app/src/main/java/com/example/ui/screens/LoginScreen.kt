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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warehouse
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberOnContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.CyanOnContainer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoOnContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletContainer
import com.example.ui.theme.VioletLight
import com.example.ui.theme.VioletOnContainer
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.WarehouseScreen
import com.example.ui.viewmodel.WarehouseViewModel

/**
 * Modern, colorful, minimalist Login & User Authentication screen (Use Case 1).
 * Features organized layout hierarchy, role selection pills without auto-filling credentials,
 * vibrant color palette accents, and Firebase Auth with email/password validation.
 */
@Composable
fun LoginScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    // Blank credential inputs by default - NO auto-filling
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.WAREHOUSE_EMPLOYEE) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Role-dependent vibrant primary accent color
    val activeRoleColor = when (selectedRole) {
        UserRole.ADMIN -> IndigoPrimary
        UserRole.WAREHOUSE_MANAGER -> AmberPrimary
        UserRole.WAREHOUSE_EMPLOYEE -> EmeraldLight
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Header Section with colorful gradient logo background
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // Glow effect behind logo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                activeRoleColor.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Modern Gradient Logo Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(IndigoPrimary, VioletPrimary, CyanPrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = "Smart Warehouse Logo",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        // Feature pill highlights
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderFeatureBadge(
                label = "Role Security",
                icon = Icons.Default.Shield,
                bgColor = CyanContainer,
                textColor = CyanOnContainer
            )
            HeaderFeatureBadge(
                label = "Offline Sync",
                icon = Icons.Default.CloudDone,
                bgColor = EmeraldContainer,
                textColor = EmeraldOnContainer
            )
            HeaderFeatureBadge(
                label = "Firebase Auth",
                icon = Icons.Default.AdminPanelSettings,
                bgColor = VioletContainer,
                textColor = VioletOnContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Organized Login Card with Multi-Color Accent Stripe
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Vibrant top multi-color accent bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    EmeraldLight,
                                    CyanPrimary,
                                    IndigoPrimary,
                                    VioletPrimary,
                                    AmberPrimary
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Sign In to Terminal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Enter your credentials to access warehouse operations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }

                    // Error Banner if present
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(RoseContainer)
                                .border(1.dp, RosePrimary, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = RosePrimary,
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
                        placeholder = { Text("e.g. employee@smartwarehouse.net", color = Slate500) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isEmailFormat) Icons.Default.Email else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (emailOrUsername.isNotEmpty()) activeRoleColor else Slate500
                            )
                        },
                        isError = isEmailFormat && !isEmailValid,
                        supportingText = if (isEmailFormat && !isEmailValid) {
                            { Text("Please enter a valid email address", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeRoleColor,
                            focusedLabelColor = activeRoleColor
                        ),
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
                        placeholder = { Text("Enter password", color = Slate500) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (password.isNotEmpty()) activeRoleColor else Slate500
                            )
                        },
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeRoleColor,
                            focusedLabelColor = activeRoleColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Role Selection Label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = activeRoleColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Select Access Role to Authenticate:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }

                    // Colorful Role Selection Pills (Purely for role choice - NO CREDENTIAL AUTOFILL)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorfulRoleSelectPill(
                            label = "Employee",
                            isSelected = selectedRole == UserRole.WAREHOUSE_EMPLOYEE,
                            icon = Icons.Default.Engineering,
                            accentColor = EmeraldLight,
                            containerBg = EmeraldContainer,
                            textColor = EmeraldOnContainer,
                            onClick = {
                                selectedRole = UserRole.WAREHOUSE_EMPLOYEE
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ColorfulRoleSelectPill(
                            label = "Manager",
                            isSelected = selectedRole == UserRole.WAREHOUSE_MANAGER,
                            icon = Icons.Default.SupervisorAccount,
                            accentColor = AmberPrimary,
                            containerBg = AmberContainer,
                            textColor = AmberOnContainer,
                            onClick = {
                                selectedRole = UserRole.WAREHOUSE_MANAGER
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ColorfulRoleSelectPill(
                            label = "Admin",
                            isSelected = selectedRole == UserRole.ADMIN,
                            icon = Icons.Default.AdminPanelSettings,
                            accentColor = IndigoPrimary,
                            containerBg = IndigoContainer,
                            textColor = IndigoOnContainer,
                            onClick = {
                                selectedRole = UserRole.ADMIN
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Colorful Dynamic Sign In Button
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = activeRoleColor, modifier = Modifier.size(36.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                if (emailOrUsername.isBlank()) {
                                    errorMessage = "Please enter an email or username."
                                    return@Button
                                }
                                if (emailOrUsername.contains("@") && !FirebaseAuthManager.isValidEmail(emailOrUsername)) {
                                    errorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                val (passValid, passErr) = FirebaseAuthManager.validatePassword(password)
                                if (!passValid) {
                                    errorMessage = passErr ?: "Password must be at least 6 characters."
                                    return@Button
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
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeRoleColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = when (selectedRole) {
                                        UserRole.ADMIN -> "Sign In as Administrator"
                                        UserRole.WAREHOUSE_MANAGER -> "Sign In as Warehouse Manager"
                                        UserRole.WAREHOUSE_EMPLOYEE -> "Sign In as Warehouse Employee"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Footer System Info
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterSystemChip(label = "Room Local DB", color = EmeraldLight)
            Text(text = "•", color = Slate500, fontSize = 12.sp)
            FooterSystemChip(label = "Firebase Auth", color = IndigoPrimary)
            Text(text = "•", color = Slate500, fontSize = 12.sp)
            FooterSystemChip(label = "NFC & Barcode Ready", color = VioletPrimary)
        }
    }
}

@Composable
private fun HeaderFeatureBadge(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun ColorfulRoleSelectPill(
    label: String,
    isSelected: Boolean,
    icon: ImageVector,
    accentColor: Color,
    containerBg: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) containerBg else Slate100)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) textColor else Slate600,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) textColor else Slate700
                )
            }
        }
    }
}

@Composable
private fun FooterSystemChip(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate600
        )
    }
}
