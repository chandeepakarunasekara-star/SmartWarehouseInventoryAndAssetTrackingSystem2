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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SyncQueueEntity
import com.example.domain.model.SyncStatus
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.components.MinimalOutlinedButton
import com.example.ui.theme.MutedAmber
import com.example.ui.theme.MutedAmberContainer
import com.example.ui.theme.MutedCoral
import com.example.ui.theme.MutedCoralContainer
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

/**
 * Offline Synchronization & SyncQueue management console.
 * Demonstrates offline-first Room persistence, idempotency keys,
 * and reliable background server synchronization.
 */
@Composable
fun SyncQueueScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val syncQueue by viewModel.syncQueue.collectAsState()
    val isOnline by viewModel.isNetworkConnected.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val pendingCount = syncQueue.count { it.status == SyncStatus.PENDING || it.status == SyncStatus.FAILED }
    val syncedCount = syncQueue.count { it.status == SyncStatus.SYNCED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("sync_queue_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Network Simulation Controls & Offline Toggle
        item {
            MinimalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) SageTeal else MutedAmber
                            )
                            Text(
                                text = if (isOnline) "Network Connected" else "Offline Simulation Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isOnline) {
                                "Online: Transactions queue and sync to Spring Boot REST API."
                            } else {
                                "Offline: Operations write locally to Room and wait in SyncQueue."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }

                    Switch(
                        checked = isOnline,
                        onCheckedChange = { viewModel.toggleNetworkConnection() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SageTeal,
                            checkedTrackColor = SageTealContainer
                        ),
                        modifier = Modifier.testTag("network_simulation_toggle")
                    )
                }
            }
        }

        // 2. Sync Actions & Metrics Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MinimalButton(
                    text = if (isSyncing) "Syncing..." else "Sync Now ($pendingCount)",
                    icon = Icons.Default.Sync,
                    enabled = isOnline && !isSyncing && pendingCount > 0,
                    onClick = { viewModel.triggerSyncNow() },
                    modifier = Modifier.weight(1f),
                    testTag = "trigger_sync_button"
                )

                MinimalOutlinedButton(
                    text = "Clear History",
                    icon = Icons.Default.DeleteSweep,
                    enabled = syncedCount > 0,
                    onClick = { viewModel.clearCompletedSyncs() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Conflict Handling & Idempotency Security Note
        item {
            MinimalCard {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SageTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Idempotency & Conflict Strategy",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Every queue item carries an unforgeable idempotency key. If network drops mid-request and retries, the server verifies the key and prevents duplicate stock additions or double-deductions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }
            }
        }

        // 4. Queue Items
        item {
            Text(
                text = "Sync Queue Items (${syncQueue.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }

        if (syncQueue.isEmpty()) {
            item {
                MinimalCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = SageTeal,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sync queue is empty. All local Room operations are synchronized.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }
                }
            }
        } else {
            items(syncQueue, key = { it.syncId }) { item ->
                SyncQueueRow(item)
            }
        }
    }
}

@Composable
private fun SyncQueueRow(item: SyncQueueEntity) {
    val (statusBg, statusTextColor) = when (item.status) {
        SyncStatus.SYNCED -> Pair(SageTealContainer, SageTealOnContainer)
        SyncStatus.PENDING -> Pair(MutedAmberContainer, MutedAmber)
        SyncStatus.IN_PROGRESS -> Pair(Slate200, Slate700)
        SyncStatus.FAILED -> Pair(MutedCoralContainer, MutedCoral)
    }

    MinimalCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.operationType.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• ${item.entityType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
            }

            Text(
                text = "Idempotency: ${item.idempotencyKey}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Slate600
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .padding(8.dp)
            ) {
                Text(
                    text = item.payloadJson,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = Slate700,
                    maxLines = 2
                )
            }

            if (item.errorMessage.isNotBlank()) {
                Text(
                    text = "Retry error: ${item.errorMessage}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedCoral
                )
            }
        }
    }
}
