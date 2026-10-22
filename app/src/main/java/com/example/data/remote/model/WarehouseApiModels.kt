package com.example.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer objects (DTOs) for remote Spring Boot REST API and Firebase sync operations.
 */

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String? = null,
    @Json(name = "role") val role: String
)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "token") val token: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "username") val username: String,
    @Json(name = "fullName") val fullName: String,
    @Json(name = "role") val role: String,
    @Json(name = "warehouseId") val warehouseId: String
)

@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "productId") val productId: String,
    @Json(name = "sku") val sku: String,
    @Json(name = "barcode") val barcode: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "categoryId") val categoryId: String,
    @Json(name = "supplierId") val supplierId: String,
    @Json(name = "unitPrice") val unitPrice: Double,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "minimumStockLevel") val minimumStockLevel: Int,
    @Json(name = "expiryDate") val expiryDate: String,
    @Json(name = "locationId") val locationId: String,
    @Json(name = "warehouseId") val warehouseId: String,
    @Json(name = "imageUrl") val imageUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class StockOperationRequestDto(
    @Json(name = "productId") val productId: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "locationId") val locationId: String,
    @Json(name = "referenceNumber") val referenceNumber: String,
    @Json(name = "notes") val notes: String = "",
    @Json(name = "performedByUserId") val performedByUserId: String,
    @Json(name = "idempotencyKey") val idempotencyKey: String
)

@JsonClass(generateAdapter = true)
data class StockTransferRequestDto(
    @Json(name = "productId") val productId: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "sourceLocationId") val sourceLocationId: String,
    @Json(name = "destinationWarehouseId") val destinationWarehouseId: String,
    @Json(name = "destinationLocationId") val destinationLocationId: String,
    @Json(name = "referenceNumber") val referenceNumber: String,
    @Json(name = "notes") val notes: String = "",
    @Json(name = "requestedByUserId") val requestedByUserId: String,
    @Json(name = "idempotencyKey") val idempotencyKey: String
)

@JsonClass(generateAdapter = true)
data class AssetDto(
    @Json(name = "assetId") val assetId: String,
    @Json(name = "assetCode") val assetCode: String,
    @Json(name = "name") val name: String,
    @Json(name = "category") val category: String,
    @Json(name = "serialNumber") val serialNumber: String,
    @Json(name = "status") val status: String,
    @Json(name = "warehouseId") val warehouseId: String,
    @Json(name = "locationId") val locationId: String,
    @Json(name = "assignedEmployeeName") val assignedEmployeeName: String = "",
    @Json(name = "imageUrl") val imageUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class SyncBatchRequestDto(
    @Json(name = "deviceId") val deviceId: String,
    @Json(name = "items") val items: List<SyncItemPayloadDto>
)

@JsonClass(generateAdapter = true)
data class SyncItemPayloadDto(
    @Json(name = "syncId") val syncId: String,
    @Json(name = "entityType") val entityType: String,
    @Json(name = "entityId") val entityId: String,
    @Json(name = "operationType") val operationType: String,
    @Json(name = "payloadJson") val payloadJson: String,
    @Json(name = "idempotencyKey") val idempotencyKey: String
)

@JsonClass(generateAdapter = true)
data class SyncBatchResponseDto(
    @Json(name = "success") val success: Boolean,
    @Json(name = "processedCount") val processedCount: Int,
    @Json(name = "results") val results: List<SyncResultItemDto> = emptyList(),
    @Json(name = "serverTimestamp") val serverTimestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class SyncResultItemDto(
    @Json(name = "syncId") val syncId: String,
    @Json(name = "status") val status: String, // SUCCESS, CONFLICT, ERROR
    @Json(name = "message") val message: String = ""
)

data class ServerHealthCheckResult(
    val isReachable: Boolean,
    val statusCode: Int = 0,
    val latencyMs: Long = 0L,
    val endpointUrl: String = "",
    val message: String = ""
)
