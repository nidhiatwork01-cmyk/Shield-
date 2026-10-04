package com.scamshield.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ScamShieldApiService {
    @POST("analyze")
    suspend fun analyze(@Body request: AnalyzeRequest): VerdictResponse

    @GET("health")
    suspend fun health(): HealthResponse
}

object ScamShieldApi {
    // Connects directly to your PC backend when phone and laptop are on same Wi-Fi
    private const val BASE_URL = "http://192.168.0.103:8000/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: ScamShieldApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScamShieldApiService::class.java)
    }
}
