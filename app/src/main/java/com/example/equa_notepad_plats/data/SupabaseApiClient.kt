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

    // ==================== USER ENDPOINTS ====================

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

    // ==================== BOOK ENDPOINTS ====================

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

    suspend fun updateRemoteBook(book: RemoteBook): Result<ApiResponse<RemoteBook>> {
        return try {
            val endpoint = "${BASE_URL}/functions/v1/update-book"

            Log.d("SupabaseApiService", "Updating book at: $endpoint")

            val response = client.put(endpoint) {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                setBody(mapOf(
                    "id" to book.id,
                    "userId" to book.userId,
                    "name" to book.name,
                    "description" to book.description,
                    "imageUri" to book.imageUri
                ))
            }

            if (response.status.value in 200..299) {
                val apiResponse = response.body<ApiResponse<RemoteBook>>()
                Result.success(apiResponse)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("SupabaseApiService", "Error updating book: $errorBody")
                Result.failure(Exception("Error updating book: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error updating book", e)
            Result.failure(e)
        }
    }

    suspend fun deleteRemoteBook(bookId: String): Result<ApiResponse<Unit>> {
        return try {
            val endpoint = "${BASE_URL}/functions/v1/delete-book"

            Log.d("SupabaseApiService", "Deleting book: $bookId")

            val response = client.delete(endpoint) {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                setBody(mapOf("id" to bookId))
            }

            if (response.status.value in 200..299) {
                val apiResponse = response.body<ApiResponse<Unit>>()
                Result.success(apiResponse)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("SupabaseApiService", "Error deleting book: $errorBody")
                Result.failure(Exception("Error deleting book: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error deleting book", e)
            Result.failure(e)
        }
    }

    // ==================== FORMULA ENDPOINTS ====================

    suspend fun createRemoteFormula(formula: RemoteFormula): Result<ApiResponse<RemoteFormula>> {
        return try {
            val endpoint = "${BASE_URL}/functions/v1/register-formula"

            Log.d("SupabaseApiService", "Creating formula at: $endpoint")
            Log.d("SupabaseApiService", "Formula data: bookId=${formula.bookId}, name=${formula.name}")

            val response = client.post(endpoint) {
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

            Log.d("SupabaseApiService", "Response status: ${response.status}")

            if (response.status.value in 200..299) {
                val responseBody = response.bodyAsText()
                Log.d("SupabaseApiService", "Response body: $responseBody")

                val apiResponse = response.body<ApiResponse<RemoteFormula>>()
                Result.success(apiResponse)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("SupabaseApiService", "Error creating formula: $errorBody")
                Log.e("SupabaseApiService", "Status code: ${response.status.value}")

                Result.failure(Exception("Error creating formula: ${response.status.value} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error creating formula", e)
            Result.failure(e)
        }
    }

    suspend fun updateRemoteFormula(formula: RemoteFormula): Result<ApiResponse<RemoteFormula>> {
        return try {
            val endpoint = "${BASE_URL}/functions/v1/update-formula"

            Log.d("SupabaseApiService", "Updating formula at: $endpoint")

            val response = client.put(endpoint) {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                setBody(mapOf(
                    "id" to formula.id,
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
                Log.e("SupabaseApiService", "Error updating formula: $errorBody")
                Result.failure(Exception("Error updating formula: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error updating formula", e)
            Result.failure(e)
        }
    }

    suspend fun deleteRemoteFormula(formulaId: String): Result<ApiResponse<Unit>> {
        return try {
            val endpoint = "${BASE_URL}/functions/v1/delete-formula"

            Log.d("SupabaseApiService", "Deleting formula: $formulaId")

            val response = client.delete(endpoint) {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                setBody(mapOf("id" to formulaId))
            }

            if (response.status.value in 200..299) {
                val apiResponse = response.body<ApiResponse<Unit>>()
                Result.success(apiResponse)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("SupabaseApiService", "Error deleting formula: $errorBody")
                Result.failure(Exception("Error deleting formula: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error deleting formula", e)
            Result.failure(e)
        }
    }

    suspend fun getFormulasByBookId(bookId: Int): Result<ApiResponse<List<RemoteFormula>>> {
        return try {
            val endpoint = "${BASE_URL}/functions/v1/get-formulas"

            Log.d("SupabaseApiService", "Getting formulas for book: $bookId")

            val response = client.get(endpoint) {
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_API_KEY}")
                parameter("bookId", bookId)
            }

            if (response.status.value in 200..299) {
                val apiResponse = response.body<ApiResponse<List<RemoteFormula>>>()
                Result.success(apiResponse)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("SupabaseApiService", "Error getting formulas: $errorBody")
                Result.failure(Exception("Error getting formulas: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error getting formulas", e)
            Result.failure(e)
        }
    }

    // ==================== SYNC ENDPOINTS ====================

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