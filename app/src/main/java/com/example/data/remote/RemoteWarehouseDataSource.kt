package com.example.data.remote

import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.data.remote.model.LoginRequestDto
import com.example.data.remote.model.LoginResponseDto
import com.example.data.remote.model.ProductDto
import com.example.data.remote.model.ServerHealthCheckResult
import com.example.data.remote.model.StockOperationRequestDto
import com.example.data.remote.model.SyncBatchRequestDto
import com.example.data.remote.model.SyncBatchResponseDto
import com.example.data.remote.model.SyncItemPayloadDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Remote Data Source orchestrating network communication with the Spring Boot REST API
 * and Firebase cloud backend. Handles resilient batch synchronization and remote endpoints.
 */
class RemoteWarehouseDataSource(
    private var baseUrl: String = "http://10.0.2.2:8080/api/"
) {

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
    }

    fun getBaseUrl(): String = baseUrl

    private fun api(): WarehouseApiService = NetworkClient.getApiService(baseUrl)

    suspend fun pingBackend(): ServerHealthCheckResult = NetworkClient.pingServer(baseUrl)

    suspend fun loginRemote(username: String, role: String): Result<LoginResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = api().login(LoginRequestDto(username = username, role = role))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushSyncBatch(items: List<SyncQueueEntity>): Result<SyncBatchResponseDto> = withContext(Dispatchers.IO) {
        try {
            val dtoList = items.map { item ->
                SyncItemPayloadDto(
                    syncId = item.syncId,
                    entityType = item.entityType,
                    entityId = item.entityId,
                    operationType = item.operationType.name,
                    payloadJson = item.payloadJson,
                    idempotencyKey = item.idempotencyKey
                )
            }
            val request = SyncBatchRequestDto(
                deviceId = "ANDROID-TERMINAL-01",
                items = dtoList
            )
            val response = api().syncBatch(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // If remote endpoint isn't running or returns non-200, return graceful error
                Result.failure(Exception("Remote sync returned HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncProductRemote(product: ProductEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dto = ProductDto(
                productId = product.productId,
                sku = product.sku,
                barcode = product.barcode,
                name = product.name,
                description = product.description,
                categoryId = product.categoryId,
                supplierId = product.supplierId,
                unitPrice = product.unitPrice,
                quantity = product.quantity,
                minimumStockLevel = product.minimumStockLevel,
                expiryDate = product.expiryDate,
                locationId = product.locationId,
                warehouseId = product.warehouseId,
                imageUrl = product.imageUrl
            )
            val response = api().createProduct(dto)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
