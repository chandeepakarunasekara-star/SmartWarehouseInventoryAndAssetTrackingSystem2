package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Wifi
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
import com.example.hardware.BluetoothDeviceManager
import com.example.hardware.NfcTagManager
import com.example.ui.components.MinimalButton
import com.example.ui.components.MinimalCard
import com.example.ui.components.MinimalOutlinedButton
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
 * Screen for managing hardware warehouse peripherals:
 * Bluetooth ring/laser scanners, thermal mobile label printers,
 * digital forklift scales, and contactless NFC asset tags.
 */
@Composable
fun HardwarePeripheralsScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val peripherals by viewModel.bluetoothManager.peripherals.collectAsState()
    val isScanningBle by viewModel.bluetoothManager.isScanning.collectAsState()
    val nfcTag by viewModel.nfcManager.lastScannedTag.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("hardware_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Bluetooth Header & Scan Trigger
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bluetooth Peripherals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Warehouse scanners, printers, and weighing scales",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }

                MinimalButton(
                    text = if (isScanningBle) "Searching..." else "Scan BLE",
                    icon = Icons.Default.Bluetooth,
                    onClick = {
                        if (isScanningBle) viewModel.bluetoothManager.stopScan()
                        else viewModel.bluetoothManager.triggerScanForNewDevices()
                    }
                )
            }
        }

        // 2. Peripherals List
        items(peripherals) { device ->
            val icon = when (device.type) {
                "SCANNER" -> Icons.Default.QrCodeScanner
                "PRINTER" -> Icons.Default.Print
                "SCALE" -> Icons.Default.Scale
                else -> Icons.Default.Bluetooth
            }

            MinimalCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (device.isConnected) SageTealContainer else Slate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (device.isConnected) SageTeal else Slate600,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = device.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID: ${device.id} • ${device.type}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryFull,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${device.batteryPercent}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate600
                                    )
                                }
                                Text(
                                    text = "${device.signalStrengthDbm} dBm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    MinimalOutlinedButton(
                        text = if (device.isConnected) "Disconnect" else "Connect",
                        onClick = { viewModel.bluetoothManager.toggleDeviceConnection(device.id) },
                        modifier = Modifier.height(38.dp)
                    )
                }
            }
        }

        // 3. NFC Asset Tag Interface
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NFC Asset Tag Integration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Contactless RFID/NFC tag reader for high-value equipment identification",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )

                MinimalCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SageTealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = SageTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "NFC Sensor Ready",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap phone against asset tag to read identifier",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }
                        }

                        // Simulation button for emulator testing
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MinimalButton(
                                text = "Simulate Forklift Tag Tap",
                                onClick = {
                                    viewModel.nfcManager.simulateNfcTagTap("AST-FL-01")
                                    viewModel.handleScannedCode("AST-FL-01")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            MinimalOutlinedButton(
                                text = "Clear Tag",
                                onClick = { viewModel.nfcManager.clearScannedTag() },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Scanned Tag Result
                        nfcTag?.let { tag ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate100)
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "NFC Tag Detected: ${tag.tagId}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SageTeal
                                    )
                                    Text(
                                        text = "Asset Code Payload: ${tag.assetCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Protocol: ${tag.technology} • ${tag.memoryCapacityBytes} Bytes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
