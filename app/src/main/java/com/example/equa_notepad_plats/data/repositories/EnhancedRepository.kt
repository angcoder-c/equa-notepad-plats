package com.example.equa_notepad_plats.data.repositories

import android.content.Context
import android.util.Log
import com.example.equa_notepad_plats.data.local.AppDatabase
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import com.example.equa_notepad_plats.data.remote.SyncService
import com.example.equa_notepad_plats.data.remote.mappers.toLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class EnhancedSyncRepository(
    private val database: AppDatabase,
    private val syncService: SyncService,
    private val context: Context
) {
    companion object {
        private const val TAG = "EnhancedSyncRepository"
        private const val PREFS_NAME = "sync_prefs"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Upload book with remoteId tracking
    suspend fun syncBook(book: BookEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (book.remoteId != null) {
                    // Update existing
                    syncService.updateBookOnRemote(book, book.remoteId)
                } else {
                    // Create new
                    val result = syncService.syncBooksToRemote(listOf(book))
                    result.onSuccess { ids ->
                        ids.firstOrNull()?.let { remoteId ->
                            // Update local entity with remoteId
                            val updatedBook = book.copy(
                                remoteId = remoteId,
                                lastSyncedAt = System.currentTimeMillis(),
                                isDirty = false
                            )
                            database.bookDao().updateBook(updatedBook)
                        }
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing book", e)
                Result.failure(e)
            }
        }
    }

    // Upload formula with remoteId tracking
    suspend fun syncFormula(formula: FormulaEntity): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Get the book to find its remoteId
                val book = database.bookDao().getBookById(formula.bookId)
                val remoteBookId = book?.remoteId
                    ?: return@withContext Result.failure(
                        Exception("Book not synced yet")
                    )

                if (formula.remoteId != null) {
                    // Update existing
                    syncService.updateFormulaOnRemote(
                        formula,
                        formula.remoteId,
                        remoteBookId
                    )
                } else {
                    // Create new
                    val result = syncService.syncFormulasToRemote(
                        listOf(formula),
                        remoteBookId
                    )
                    result.onSuccess { ids ->
                        ids.firstOrNull()?.let { remoteId ->
                            // Update local entity with remoteId
                            val updatedFormula = formula.copy(
                                remoteId = remoteId,
                                lastSyncedAt = System.currentTimeMillis(),
                                isDirty = false
                            )
                            database.formulaDao().updateFormula(updatedFormula)
                        }
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing formula", e)
                Result.failure(e)
            }
        }
    }

    // Sync only dirty (modified) items
    suspend fun syncDirtyItems(): Result<SyncStats> {
        return withContext(Dispatchers.IO) {
            try {
                var syncedBooks = 0
                var syncedFormulas = 0

                // Get all books
                val books = database.bookDao().getAllBooks().first()

                // Sync dirty books
                books.filter { it.isDirty || it.remoteId == null }.forEach { book ->
                    syncBook(book).onSuccess {
                        syncedBooks++
                    }
                }

                // Sync dirty formulas
                books.forEach { book ->
                    val formulas = database.formulaDao()
                        .getFormulasByBookId(book.id)
                        .first()

                    formulas.filter { it.isDirty || it.remoteId == null }.forEach { formula ->
                        syncFormula(formula).onSuccess {
                            syncedFormulas++
                        }
                    }
                }

                saveLastSyncTimestamp()

                Result.success(
                    SyncStats(
                        syncedBooks = syncedBooks,
                        syncedFormulas = syncedFormulas
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing dirty items", e)
                Result.failure(e)
            }
        }
    }

    private fun saveLastSyncTimestamp() {
        sharedPrefs.edit()
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }
}

data class SyncStats(
    val syncedBooks: Int,
    val syncedFormulas: Int
)