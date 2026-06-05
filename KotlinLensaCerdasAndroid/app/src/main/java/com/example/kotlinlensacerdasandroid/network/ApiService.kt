package com.example.kotlinlensacerdasandroid.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// --- MODELS ---

data class LoginRequest(
    val google_id: String,
    val name: String,
    val email: String,
    val photo_url: String?
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)

data class UserData(
    val id: Int,
    val google_id: String,
    val name: String,
    val email: String,
    val photo_url: String?
)

data class SummarizeRequest(
    val user_id: Int,
    val title: String,
    val content: String,
    val style: String? = null,
    val length: String? = null
)

data class SummarizeResponse(
    val success: Boolean,
    val message: String,
    val data: SummaryData?
)

data class SummaryData(
    val id: Int,
    val title: String,
    val summary: String
)

data class HistoryResponse(
    val success: Boolean,
    val message: String?,
    val data: List<HistoryItem>?
)

data class HistoryItem(
    val id: Int,
    val user_id: Int,
    val title: String,
    val original_text: String,
    val summary_text: String,
    val created_at: String
)

data class UpdateRequest(
    val title: String,
    val summary_text: String
)

data class BaseResponse(
    val success: Boolean,
    val message: String
)

// --- API INTERFACE ---

interface ApiService {
    @POST("api/login")
    suspend fun loginWithGoogle(@Body request: LoginRequest): LoginResponse

    @POST("api/summarize")
    suspend fun createSummary(@Body request: SummarizeRequest): SummarizeResponse

    @GET("api/history")
    suspend fun getHistory(@Query("user_id") userId: Int): HistoryResponse

    @PUT("api/update/{id}")
    suspend fun updateSummary(@Path("id") id: Int, @Body request: UpdateRequest): BaseResponse

    @DELETE("api/delete/{id}")
    suspend fun deleteSummary(@Path("id") id: Int): BaseResponse
}

// --- RETROFIT CLIENT ---

object ApiClient {
    // Kalo pakai Emulator Android, localhost backend NodeJS bisa diakses via 10.0.2.2
    // Nanti jika sudah di-host di Vercel, tinggal ganti ke URL Vercel-nya
    private const val BASE_URL = "https://backend-lensacerdas.vercel.app/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
