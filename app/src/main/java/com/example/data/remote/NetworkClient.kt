package com.example.data.remote

import com.example.data.remote.model.ServerHealthCheckResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Network configuration and client provider for Spring Boot REST API integration.
 * Supports configurable API Base URL and live connectivity ping verification.
 */
object NetworkClient {

    private var currentBaseUrl: String = "http://10.0.2.2:8080/api/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var cachedRetrofit: Retrofit? = null
    private var cachedService: WarehouseApiService? = null

    @Synchronized
    fun getApiService(baseUrl: String = currentBaseUrl): WarehouseApiService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (cachedService != null && currentBaseUrl == sanitizedUrl) {
            return cachedService!!
        }

        currentBaseUrl = sanitizedUrl
        val retrofit = Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        cachedRetrofit = retrofit
        val service = retrofit.create(WarehouseApiService::class.java)
        cachedService = service
        return service
    }

    /**
     * Pings remote server to test reachability and latency.
     */
    suspend fun pingServer(targetUrl: String): ServerHealthCheckResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val formattedUrl = if (targetUrl.endsWith("/")) targetUrl else "$targetUrl/"
        val requestUrl = "${formattedUrl}health"

        try {
            val request = Request.Builder()
                .url(requestUrl)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime
                ServerHealthCheckResult(
                    isReachable = response.isSuccessful || response.code in 200..499,
                    statusCode = response.code,
                    latencyMs = duration,
                    endpointUrl = requestUrl,
                    message = "Server responded with HTTP ${response.code} in ${duration}ms"
                )
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            ServerHealthCheckResult(
                isReachable = false,
                statusCode = 0,
                latencyMs = duration,
                endpointUrl = requestUrl,
                message = "Connection failed: ${e.localizedMessage ?: "Host unreachable"}"
            )
        }
    }
}
