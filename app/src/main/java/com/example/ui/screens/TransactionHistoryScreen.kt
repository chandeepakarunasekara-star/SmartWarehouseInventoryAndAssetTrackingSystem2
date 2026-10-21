package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.entity.StockTransactionEntity
import com.example.domain.model.SyncStatus
import com.example.domain.model.TransactionType
import com.example.ui.components.MinimalCard
import com.example.ui.theme.MutedCoral
import com.example.ui.theme.SageTeal
import com.example.ui.theme.SageTealContainer
import com.example.ui.theme.SageTealOnContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Immutable transaction audit ledger screen.
 * Displays all completed and queued Stock-In, Stock-Out, and Transfer movements.
 */
@Composable
fun TransactionHistoryScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    var selectedTypeFilter by remember { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = transactions.filter {
        selectedTypeFilter == null || it.transactionType == selectedTypeFilter
    }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Inventory Transaction Ledger",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )

        // Filter Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                TxnFilterPill(
                    label = "All (${transactions.size})",
                    isSelected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null }
                )
            }
            items(TransactionType.entries) { type ->
                TxnFilterPill(
                    label = type.name.replace("_", " "),
                    isSelected = selectedTypeFilter == type,
                    onClick = {
                        selectedTypeFilter = if (selectedTypeFilter == type) null else type
                    }
                )
            }
        }

        // Ledger List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions found in this category.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredTransactions, key = { it.transactionId }) { txn ->
                    val (typeColor, typeSign) = when (txn.transactionType) {
                        TransactionType.STOCK_IN -> Pair(SageTeal, "+")
                        TransactionType.STOCK_OUT -> Pair(MutedCoral, "-")
                        TransactionType.STOCK_TRANSFER -> Pair(Slate700, "⇄")
                    }
                    val formattedDate = remember(txn.timestamp) { dateFormatter.format(Date(txn.timestamp)) }

                    MinimalCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(typeColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = typeSign,
                                            fontWeight = FontWeight.Bold,
                                            color = typeColor
                                        )
                                    }
                                    Text(
                                        text = txn.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "$typeSign${txn.quantity}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ref: ${txn.referenceNumber} • by ${txn.performedByUserName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                            }

                            Text(
                                text = "Path: ${txn.sourceLocationId} ➔ ${txn.destinationLocationId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )

                            if (txn.notes.isNotBlank()) {
                                Text(
                                    text = "Note: ${txn.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }

                            // Sync Status Tag
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (txn.syncStatus == SyncStatus.SYNCED) Icons.Default.Check else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (txn.syncStatus == SyncStatus.SYNCED) SageTeal else Slate400,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (txn.syncStatus == SyncStatus.SYNCED) "Backend Synced" else "Stored Locally (Pending Sync)",
                                    fontSize = 10.sp,
                                    color = if (txn.syncStatus == SyncStatus.SYNCED) SageTeal else Slate500
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TxnFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) SageTealContainer else Slate100)
            .border(
                1.dp,
                if (isSelected) SageTeal else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) SageTealOnContainer else Slate700
        )
    }
}
