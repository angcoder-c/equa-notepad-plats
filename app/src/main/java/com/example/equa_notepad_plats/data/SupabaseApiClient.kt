package com.example.equa_notepad_plats.data

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import android.util.Log
import com.example.equa_notepad_plats.BuildConfig
import com.example.equa_notepad_plats.data.remote.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.call.body

object SupabaseApiService {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
                isLenient = true
            })
        }
    }

    private const val BASE_URL = BuildConfig.SUPABASE_URL

    suspend fun registerRemoteUser(user: RemoteUser): Result<ApiResponse<RemoteUser>> {
        return try {
            val response = client.post("${BASE_URL}/functions/v1/register-user") {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                setBody(user)
            }

            if (response.status.value in 200..299) {
                val apiResponse = response.body<ApiResponse<RemoteUser>>()
                Result.success(apiResponse)
            } else {
                Result.failure(Exception("Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error registering user", e)
            Result.failure(e)
        }
    }

    suspend fun createRemoteBook(book: RemoteBook): Result<ApiResponse<RemoteBook>> {
        return try {
            val endpoint = "${BASE_URL}/functions/v1/register-book"

            Log.d("SupabaseApiService", "Creating book at: $endpoint")
            Log.d("SupabaseApiService", "Book data: userId=${book.userId}, name=${book.name}")

            val response = client.post(endpoint) {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                setBody(mapOf(
                    "userId" to book.userId,
                    "name" to book.name,
                    "description" to book.description,
                    "imageUri" to book.imageUri
                ))
            }

            Log.d("SupabaseApiService", "Response status: ${response.status}")

            if (response.status.value in 200..299) {
                val responseBody = response.bodyAsText()
                Log.d("SupabaseApiService", "Response body: $responseBody")

                val apiResponse = response.body<ApiResponse<RemoteBook>>()
                Result.success(apiResponse)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("SupabaseApiService", "Error creating book: $errorBody")
                Log.e("SupabaseApiService", "Status code: ${response.status.value}")
                Log.e("SupabaseApiService", "Status description: ${response.status.description}")

                Result.failure(Exception("Error creating book: ${response.status.value} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error creating book", e)
            Result.failure(e)
        }
    }

    suspend fun createRemoteFormula(formula: RemoteFormula): Result<ApiResponse<RemoteFormula>> {
        return try {
            val response = client.post("${BASE_URL}/functions/v1/register-formula") {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                setBody(mapOf(
                    "bookId" to formula.bookId,
                    "userId" to formula.userId,
                    "name" to formula.name,
                    "formulaText" to formula.formulaText,
                    "description" to formula.description,
                    "imageUri" to formula.imageUri
                ))
            }

            if (response.status.value in 200..299) {
                val apiResponse = response.body<ApiResponse<RemoteFormula>>()
                Result.success(apiResponse)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("SupabaseApiService", "Error creating formula: $errorBody")
                Result.failure(Exception("Error creating formula: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error creating formula", e)
            Result.failure(e)
        }
    }

    suspend fun syncData(request: SyncRequest): Result<ApiResponse<SyncResponse>> {
        return try {
            val response = client.post("${BASE_URL}/functions/v1/sync") {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                setBody(request)
            }

            if (response.status.value in 200..299) {
                val apiResponse = response.body<ApiResponse<SyncResponse>>()
                Result.success(apiResponse)
            } else {
                Result.failure(Exception("Error syncing: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error syncing data", e)
            Result.failure(e)
        }
    }
}