package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AuditLogEntity
import com.example.ui.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * System Audit Logging Screen (PDF Use Case 22, Page 16-17).
 * Displays immutable administrative audit records for all critical events:
 * USER_CREATED, ROLE_CHANGED, PRODUCT_CREATED, PRODUCT_UPDATED, STOCK_IN, STOCK_OUT,
 * STOCK_TRANSFER, ASSET_MOVED, LOGIN, SYNC_FAILED.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuditLogScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filterOptions = listOf(
        "ALL" to "All Events",
        "STOCK" to "Stock Ops",
        "PRODUCT" to "Products",
        "ASSET" to "Assets",
        "USER" to "Users & Roles",
        "LOGIN" to "Logins",
        "SYNC" to "Sync"
    )

    val filteredLogs = remember(auditLogs, selectedFilter, searchQuery) {
        auditLogs.filter { log ->
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "STOCK" -> log.eventType.startsWith("STOCK_")
                "PRODUCT" -> log.eventType.startsWith("PRODUCT_")
                "ASSET" -> log.eventType.startsWith("ASSET_")
                "USER" -> log.eventType.startsWith("USER_") || log.eventType == "ROLE_CHANGED"
                "LOGIN" -> log.eventType == "LOGIN"
                "SYNC" -> log.eventType.startsWith("SYNC_")
                else -> true
            }

            val matchesSearch = searchQuery.isBlank() ||
                log.userName.contains(searchQuery, ignoreCase = true) ||
                log.eventType.contains(searchQuery, ignoreCase = true) ||
                log.entityId.contains(searchQuery, ignoreCase = true) ||
                log.details.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .padding(16.dp)
            .testTag("audit_log_screen"),
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
                        .testTag("audit_log_back_button")
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
                        text = "System Audit Logging",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Immutable Compliance Trail (Use Case 22)",
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
                        text = "${auditLogs.size} Events",
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
                placeholder = { Text("Search by user, action, ID, or details...") },
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
                    .testTag("search_audit_logs_input")
            )
        }

        // 3. Filter Chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterOptions.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF084B34) else Color.White)
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF084B34) else Color(0xFFE2E8F0),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedFilter = key }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("audit_filter_$key")
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                    }
                }
            }
        }

        // 4. Audit Log Items
        if (filteredLogs.isEmpty()) {
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
                            text = "No audit log entries matching criteria.",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.eventId }) { log ->
                AuditLogCard(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AuditLogCard(log: AuditLogEntity) {
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    val (badgeBg, badgeText) = when {
        log.eventType.startsWith("STOCK_IN") -> Color(0xFFE8F5E9) to Color(0xFF084B34)
        log.eventType.startsWith("STOCK_OUT") -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        log.eventType.startsWith("STOCK_TRANSFER") -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
        log.eventType.startsWith("PRODUCT_") -> Color(0xFFE0F2FE) to Color(0xFF0284C7)
        log.eventType.startsWith("ASSET_") -> Color(0xFFF3E8FF) to Color(0xFF7E22CE)
        log.eventType.startsWith("USER_") || log.eventType == "ROLE_CHANGED" -> Color(0xFFFCE7F3) to Color(0xFFBE185D)
        log.eventType == "LOGIN" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        log.eventType.contains("FAILED") -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_card_${log.eventId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = log.eventType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Text(
                text = log.details,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1E293B)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "User: ${log.userName} (${log.userId})",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                if (log.entityId.isNotBlank()) {
                    Text(
                        text = "Entity: ${log.entityType} ${log.entityId}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}
