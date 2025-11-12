package com.example.equa_notepad_plats.data.remote

import android.util.Log
import com.example.equa_notepad_plats.data.SupabaseClientProvider
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import com.example.equa_notepad_plats.data.remote.mappers.toRemote
import com.example.equa_notepad_plats.data.remote.models.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

class SyncService {
    companion object {
        private const val TAG = "SyncService"
        private const val TABLE_BOOKS = "books"
        private const val TABLE_FORMULAS = "formulas"
        private const val TABLE_USERS = "users"
    }

    private val client = SupabaseClientProvider.client
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ==================== AUTHENTICATION ====================

    private suspend fun getCurrentUserId(): String? {
        return try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID", e)
            null
        }
    }

    // ==================== INDIVIDUAL OPERATIONS ====================

    /**
     * Upload a single book to Supabase
     */
    suspend fun uploadBook(book: BookEntity): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val remoteBook = book.toRemote(userId, book.remoteId)

                val response = if (book.remoteId != null) {
                    // Update existing book
                    client.postgrest[TABLE_BOOKS]
                        .update(remoteBook) {
                            filter {
                                eq("id", book.remoteId)
                                eq("user_id", userId)
                            }
                        }
                        .decodeSingle<RemoteBook>()
                } else {
                    // Create new book
                    client.postgrest[TABLE_BOOKS]
                        .insert(remoteBook)
                        .decodeSingle<RemoteBook>()
                }

                Result.success(response.id ?: "")
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading book: ${book.name}", e)
                Result.failure(SyncException("Failed to upload book", e))
            }
        }
    }

    /**
     * Upload a single formula to Supabase
     */
    suspend fun uploadFormula(formula: FormulaEntity, remoteBookId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val remoteFormula = formula.toRemote(userId, remoteBookId, formula.remoteId)

                val response = if (formula.remoteId != null) {
                    // Update existing formula
                    client.postgrest[TABLE_FORMULAS]
                        .update(remoteFormula) {
                            filter {
                                eq("id", formula.remoteId)
                                eq("user_id", userId)
                            }
                        }
                        .decodeSingle<RemoteFormula>()
                } else {
                    // Create new formula
                    client.postgrest[TABLE_FORMULAS]
                        .insert(remoteFormula)
                        .decodeSingle<RemoteFormula>()
                }

                Result.success(response.id ?: "")
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading formula: ${formula.name}", e)
                Result.failure(SyncException("Failed to upload formula", e))
            }
        }
    }

    // ==================== BATCH OPERATIONS ====================

    /**
     * Batch upload multiple books
     */
    suspend fun batchUploadBooks(books: List<BookEntity>): Result<List<BatchUploadResult>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val results = mutableListOf<BatchUploadResult>()

                // Process in chunks to avoid overwhelming the API
                books.chunked(10).forEach { chunk ->
                    chunk.forEach { book ->
                        val result = uploadBook(book)
                        results.add(
                            BatchUploadResult(
                                localId = book.id,
                                remoteId = result.getOrNull(),
                                success = result.isSuccess,
                                error = result.exceptionOrNull()?.message
                            )
                        )
                    }
                }

                Result.success(results)
            } catch (e: Exception) {
                Log.e(TAG, "Error in batch upload books", e)
                Result.failure(SyncException("Batch upload failed", e))
            }
        }
    }

    /**
     * Batch upload multiple formulas
     */
    suspend fun batchUploadFormulas(
        formulas: List<FormulaEntity>,
        bookIdMapping: Map<Int, String>
    ): Result<List<BatchUploadResult>> {
        return withContext(Dispatchers.IO) {
            try {
                val results = mutableListOf<BatchUploadResult>()

                formulas.chunked(10).forEach { chunk ->
                    chunk.forEach { formula ->
                        val remoteBookId = bookIdMapping[formula.bookId]
                        if (remoteBookId != null) {
                            val result = uploadFormula(formula, remoteBookId)
                            results.add(
                                BatchUploadResult(
                                    localId = formula.id,
                                    remoteId = result.getOrNull(),
                                    success = result.isSuccess,
                                    error = result.exceptionOrNull()?.message
                                )
                            )
                        } else {
                            results.add(
                                BatchUploadResult(
                                    localId = formula.id,
                                    remoteId = null,
                                    success = false,
                                    error = "Parent book not synced"
                                )
                            )
                        }
                    }
                }

                Result.success(results)
            } catch (e: Exception) {
                Log.e(TAG, "Error in batch upload formulas", e)
                Result.failure(SyncException("Batch upload failed", e))
            }
        }
    }

    // ==================== FETCH OPERATIONS ====================

    /**
     * Fetch all user's books from Supabase
     */
    suspend fun fetchBooks(): Result<List<RemoteBook>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val books = client.postgrest[TABLE_BOOKS]
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("is_deleted", false)
                        }
                        order(column = "created_at", order = Order.DESCENDING)
                    }
                    .decodeList<RemoteBook>()

                Log.d(TAG, "Fetched ${books.size} books from remote")
                Result.success(books)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching books", e)
                Result.failure(SyncException("Failed to fetch books", e))
            }
        }
    }

    /**
     * Fetch formulas for specific books
     */
    suspend fun fetchFormulas(bookIds: List<String>): Result<List<RemoteFormula>> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val formulas = client.postgrest[TABLE_FORMULAS]
                    .select {
                        filter {
                            eq("user_id", userId)
                            isIn("book_id", bookIds)
                            eq("is_deleted", false)
                        }
                        order(column = "created_at", order = Order.DESCENDING)
                    }
                    .decodeList<RemoteFormula>()

                Log.d(TAG, "Fetched ${formulas.size} formulas from remote")
                Result.success(formulas)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching formulas", e)
                Result.failure(SyncException("Failed to fetch formulas", e))
            }
        }
    }

    /**
     * Fetch formulas for a specific book
     */
    suspend fun fetchFormulasForBook(remoteBookId: String): Result<List<RemoteFormula>> {
        return fetchFormulas(listOf(remoteBookId))
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update an existing book on remote
     */
    suspend fun updateBookOnRemote(book: BookEntity, remoteId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val remoteBook = book.toRemote(userId, remoteId)

                client.postgrest[TABLE_BOOKS]
                    .update(remoteBook) {
                        filter {
                            eq("id", remoteId)
                            eq("user_id", userId)
                        }
                    }

                Log.d(TAG, "Updated book: ${book.name}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating book", e)
                Result.failure(SyncException("Failed to update book", e))
            }
        }
    }

    /**
     * Update an existing formula on remote
     */
    suspend fun updateFormulaOnRemote(
        formula: FormulaEntity,
        remoteId: String,
        remoteBookId: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val remoteFormula = formula.toRemote(userId, remoteBookId, remoteId)

                client.postgrest[TABLE_FORMULAS]
                    .update(remoteFormula) {
                        filter {
                            eq("id", remoteId)
                            eq("user_id", userId)
                        }
                    }

                Log.d(TAG, "Updated formula: ${formula.name}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating formula", e)
                Result.failure(SyncException("Failed to update formula", e))
            }
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Soft delete a book (mark as deleted)
     */
    suspend fun deleteBook(remoteId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                client.postgrest[TABLE_BOOKS]
                    .update(mapOf("is_deleted" to true, "updated_at" to getCurrentISOTime())) {
                        filter {
                            eq("id", remoteId)
                            eq("user_id", userId)
                        }
                    }

                Log.d(TAG, "Deleted book: $remoteId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting book", e)
                Result.failure(SyncException("Failed to delete book", e))
            }
        }
    }

    /**
     * Soft delete a formula (mark as deleted)
     */
    suspend fun deleteFormula(remoteId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                client.postgrest[TABLE_FORMULAS]
                    .update(mapOf("is_deleted" to true, "updated_at" to getCurrentISOTime())) {
                        filter {
                            eq("id", remoteId)
                            eq("user_id", userId)
                        }
                    }

                Log.d(TAG, "Deleted formula: $remoteId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting formula", e)
                Result.failure(SyncException("Failed to delete formula", e))
            }
        }
    }

    // ==================== FULL SYNC CAPABILITY ====================

    /**
     * Perform a complete bidirectional sync
     */
    suspend fun performFullSync(
        localBooks: List<BookEntity>,
        localFormulas: List<FormulaEntity>,
        lastSyncTimestamp: String? = null
    ): Result<SyncResult> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting full sync...")

                // Step 1: Fetch all remote data
                val remoteBooksResult = fetchBooks()
                if (remoteBooksResult.isFailure) {
                    return@withContext Result.failure(remoteBooksResult.exceptionOrNull()!!)
                }
                val remoteBooks = remoteBooksResult.getOrThrow()

                val remoteFormulasResult = fetchFormulas(remoteBooks.mapNotNull { it.id })
                if (remoteFormulasResult.isFailure) {
                    return@withContext Result.failure(remoteFormulasResult.exceptionOrNull()!!)
                }
                val remoteFormulas = remoteFormulasResult.getOrThrow()

                // Step 2: Upload local changes (books first, then formulas)
                val bookUploadResults = mutableListOf<BatchUploadResult>()
                val formulaUploadResults = mutableListOf<BatchUploadResult>()

                // Upload dirty/new books
                val booksToUpload = localBooks.filter { it.isDirty || it.remoteId == null }
                if (booksToUpload.isNotEmpty()) {
                    val uploadResult = batchUploadBooks(booksToUpload)
                    bookUploadResults.addAll(uploadResult.getOrElse { emptyList() })
                }

                // Create mapping of local book IDs to remote IDs
                val bookIdMapping = mutableMapOf<Int, String>()
                localBooks.forEach { book ->
                    book.remoteId?.let { remoteId ->
                        bookIdMapping[book.id] = remoteId
                    }
                }
                // Add newly uploaded book mappings
                bookUploadResults.forEach { result ->
                    if (result.success && result.remoteId != null) {
                        bookIdMapping[result.localId] = result.remoteId
                    }
                }

                // Upload dirty/new formulas
                val formulasToUpload = localFormulas.filter { it.isDirty || it.remoteId == null }
                if (formulasToUpload.isNotEmpty()) {
                    val uploadResult = batchUploadFormulas(formulasToUpload, bookIdMapping)
                    formulaUploadResults.addAll(uploadResult.getOrElse { emptyList() })
                }

                Log.d(TAG, "Full sync completed successfully")
                Result.success(
                    SyncResult(
                        downloadedBooks = remoteBooks,
                        downloadedFormulas = remoteFormulas,
                        uploadedBooks = bookUploadResults,
                        uploadedFormulas = formulaUploadResults,
                        syncTimestamp = getCurrentISOTime()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error during full sync", e)
                Result.failure(SyncException("Full sync failed", e))
            }
        }
    }

    /**
     * Quick sync only recent changes
     */
    suspend fun performQuickSync(lastSyncTimestamp: String): Result<SyncResult> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                // Fetch only items updated since last sync
                val recentBooks = client.postgrest[TABLE_BOOKS]
                    .select {
                        filter {
                            eq("user_id", userId)
                            gte("updated_at", lastSyncTimestamp)
                        }
                    }
                    .decodeList<RemoteBook>()

                val recentFormulas = client.postgrest[TABLE_FORMULAS]
                    .select {
                        filter {
                            eq("user_id", userId)
                            gte("updated_at", lastSyncTimestamp)
                        }
                    }
                    .decodeList<RemoteFormula>()

                Result.success(
                    SyncResult(
                        downloadedBooks = recentBooks,
                        downloadedFormulas = recentFormulas,
                        uploadedBooks = emptyList(),
                        uploadedFormulas = emptyList(),
                        syncTimestamp = getCurrentISOTime()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error during quick sync", e)
                Result.failure(SyncException("Quick sync failed", e))
            }
        }
    }

    // ==================== OLD METHODS (for backward compatibility) ====================

    suspend fun syncBooksToRemote(books: List<BookEntity>): Result<List<String>> {
        val uploadResult = batchUploadBooks(books)
        return uploadResult.map { results ->
            results.mapNotNull { it.remoteId }
        }
    }

    suspend fun syncFormulasToRemote(
        formulas: List<FormulaEntity>,
        remoteBookId: String
    ): Result<List<String>> {
        val bookIdMapping = mapOf(formulas.first().bookId to remoteBookId)
        val uploadResult = batchUploadFormulas(formulas, bookIdMapping)
        return uploadResult.map { results ->
            results.mapNotNull { it.remoteId }
        }
    }

    // ==================== HELPER METHODS ====================

    private fun getCurrentISOTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }
}

// ==================== DATA CLASSES ====================

data class BatchUploadResult(
    val localId: Int,
    val remoteId: String?,
    val success: Boolean,
    val error: String?
)

data class SyncResult(
    val downloadedBooks: List<RemoteBook>,
    val downloadedFormulas: List<RemoteFormula>,
    val uploadedBooks: List<BatchUploadResult>,
    val uploadedFormulas: List<BatchUploadResult>,
    val syncTimestamp: String
)

class SyncException(message: String, cause: Throwable? = null) : Exception(message, cause)
