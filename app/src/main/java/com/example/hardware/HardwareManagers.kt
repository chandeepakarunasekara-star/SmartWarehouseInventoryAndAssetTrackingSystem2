package com.example.hardware

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Bluetooth scanner, mobile terminal, and label printer peripherals.
 * Provides live connection status, battery telemetry, and peripheral pairing.
 */
class BluetoothDeviceManager {

    data class BluetoothPeripheral(
        val id: String,
        val name: String,
        val type: String, // SCANNER, PRINTER, SCALE
        val isConnected: Boolean,
        val batteryPercent: Int,
        val signalStrengthDbm: Int
    )

    private val _peripherals = MutableStateFlow(
        listOf(
            BluetoothPeripheral(
                id = "BT-SC-01",
                name = "Honeywell Voyager 1602g",
                type = "SCANNER",
                isConnected = true,
                batteryPercent = 88,
                signalStrengthDbm = -54
            ),
            BluetoothPeripheral(
                id = "BT-PR-02",
                name = "Zebra ZQ520 Mobile Label Printer",
                type = "PRINTER",
                isConnected = false,
                batteryPercent = 65,
                signalStrengthDbm = -72
            ),
            BluetoothPeripheral(
                id = "BT-SCL-03",
                name = "Avery Weigh-Tronix Forklift Scale",
                type = "SCALE",
                isConnected = false,
                batteryPercent = 95,
                signalStrengthDbm = -80
            )
        )
    )
    val peripherals = _peripherals.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    fun toggleDeviceConnection(deviceId: String) {
        _peripherals.value = _peripherals.value.map { device ->
            if (device.id == deviceId) {
                device.copy(isConnected = !device.isConnected)
            } else {
                device
            }
        }
    }

    fun triggerScanForNewDevices() {
        _isScanning.value = true
        // Simulates BLE scan interval
    }

    fun stopScan() {
        _isScanning.value = false
    }
}

/**
 * Handles NFC asset tag scanning and serialization.
 * Allows instant lookup of warehouse physical assets by tapping RFID/NFC chips.
 */
class NfcTagManager {

    data class NfcTagData(
        val tagId: String,
        val assetCode: String,
        val technology: String, // NFC Type 2 / NTAG213
        val memoryCapacityBytes: Int,
        val isWritable: Boolean
    )

    private val _lastScannedTag = MutableStateFlow<NfcTagData?>(null)
    val lastScannedTag = _lastScannedTag.asStateFlow()

    private val _isNfcEnabled = MutableStateFlow(true)
    val isNfcEnabled = _isNfcEnabled.asStateFlow()

    fun simulateNfcTagTap(assetCode: String) {
        _lastScannedTag.value = NfcTagData(
            tagId = "04:" + System.currentTimeMillis().toString().takeLast(8),
            assetCode = assetCode,
            technology = "NFC Type 2 (ISO 14443-A)",
            memoryCapacityBytes = 144,
            isWritable = true
        )
    }

    fun clearScannedTag() {
        _lastScannedTag.value = null
    }
}
