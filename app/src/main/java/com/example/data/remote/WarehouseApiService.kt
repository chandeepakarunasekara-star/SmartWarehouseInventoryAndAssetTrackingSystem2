package com.example.data.remote

import com.example.data.remote.model.AssetDto
import com.example.data.remote.model.LoginRequestDto
import com.example.data.remote.model.LoginResponseDto
import com.example.data.remote.model.ProductDto
import com.example.data.remote.model.StockOperationRequestDto
import com.example.data.remote.model.StockTransferRequestDto
import com.example.data.remote.model.SyncBatchRequestDto
import com.example.data.remote.model.SyncBatchResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit interface representing the Spring Boot Warehouse REST API.
 * Maps endpoints to backend microservices and MySQL data store.
 */
interface WarehouseApiService {

    @GET("health")
    suspend fun checkHealth(): Response<ResponseBody>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>

    @GET("products")
    suspend fun getProducts(): Response<List<ProductDto>>

    @POST("products")
    suspend fun createProduct(
        @Body product: ProductDto
    ): Response<ProductDto>

    @POST("inventory/stock-in")
    suspend fun executeStockIn(
        @Body request: StockOperationRequestDto
    ): Response<ResponseBody>

    @POST("inventory/stock-out")
    suspend fun executeStockOut(
        @Body request: StockOperationRequestDto
    ): Response<ResponseBody>

    @POST("inventory/transfer")
    suspend fun executeStockTransfer(
        @Body request: StockTransferRequestDto
    ): Response<ResponseBody>

    @GET("assets")
    suspend fun getAssets(): Response<List<AssetDto>>

    @POST("sync/batch")
    suspend fun syncBatch(
        @Body batchRequest: SyncBatchRequestDto
    ): Response<SyncBatchResponseDto>
}
