package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StockTransactionEntity
import com.example.domain.model.UserRole
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberOnContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen for Manager reviewing and approving pending Stock Transfers (Use Case 6 & Approvals).
 */
@Composable
fun ApprovalsScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val pendingTransfers by viewModel.pendingTransfers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isManagerOrAdmin = currentUser.role == UserRole.WAREHOUSE_MANAGER || currentUser.role == UserRole.ADMIN

    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("approvals_screen")
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
                    .background(AmberContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "Transfer Approvals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Pending internal relocations requiring managerial authorization",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }
        }

        if (pendingTransfers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No pending stock transfers awaiting approval.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(pendingTransfers, key = { it.transactionId }) { txn ->
                    val formattedDate = dateFormatter.format(Date(txn.timestamp))

                    MinimalCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = txn.productName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(AmberContainer)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PENDING APPROVAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberOnContainer
                                    )
                                }
                            }

                            Text(
                                text = "Transferring ${txn.quantity} units from ${txn.sourceLocationId} ➔ ${txn.destinationLocationId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )

                            Text(
                                text = "Requested by: ${txn.requestedBy.ifBlank { txn.performedByUserName }} • Ref: ${txn.referenceNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )

                            if (isManagerOrAdmin) {
                                MinimalButton(
                                    text = "Approve Transfer",
                                    icon = Icons.Default.Check,
                                    onClick = { viewModel.approveTransfer(txn.transactionId) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "Requires Warehouse Manager or Administrator credentials to approve.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
