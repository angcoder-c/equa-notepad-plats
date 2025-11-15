package com.example.equa_notepad_plats.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.local.AppDatabase
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import com.example.equa_notepad_plats.data.remote.SyncService
import com.example.equa_notepad_plats.data.remote.SyncResult
import com.example.equa_notepad_plats.data.remote.BatchUploadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SyncViewModel(
    private val database: AppDatabase,
    private val syncService: SyncService
) : ViewModel() {

    companion object {
        private const val TAG = "SyncViewModel"
    }

    // UI State
    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    // Books and Formulas
    private val _books = MutableStateFlow<List<BookWithFormulas>>(emptyList())
    val books: StateFlow<List<BookWithFormulas>> = _books.asStateFlow()

    // Selected items for sync
    private val _selectedFormulas = MutableStateFlow<Set<Int>>(emptySet())
    val selectedFormulas: StateFlow<Set<Int>> = _selectedFormulas.asStateFlow()

    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()

    init {
        loadBooksAndFormulas()
    }

    // ==================== DATA LOADING ====================

    private fun loadBooksAndFormulas() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                database.bookDao().getAllBooks().collect { booksList ->
                    val booksWithFormulas = mutableListOf<BookWithFormulas>()

                    booksList.forEach { book ->
                        database.formulaDao().getFormulasByBookId(book.id)
                            .take(1) // Only take the first emission to avoid continuous updates
                            .collect { formulas ->
                                booksWithFormulas.add(
                                    BookWithFormulas(
                                        book = book,
                                        formulas = formulas
                                    )
                                )
                            }
                    }

                    _books.value = booksWithFormulas
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading books and formulas", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    // ==================== BOOK SELECTION ====================

    fun selectBook(book: BookEntity?) {
        _selectedBook.value = book
        _selectedFormulas.value = emptySet() // Clear formula selection when book changes
    }

    fun getFormulasForSelectedBook(): List<FormulaEntity> {
        val book = _selectedBook.value ?: return emptyList()
        return _books.value.find { it.book.id == book.id }?.formulas ?: emptyList()
    }

    // ==================== FORMULA SELECTION ====================

    fun toggleFormulaSelection(formulaId: Int) {
        val currentSelected = _selectedFormulas.value.toMutableSet()
        if (currentSelected.contains(formulaId)) {
            currentSelected.remove(formulaId)
        } else {
            currentSelected.add(formulaId)
        }
        _selectedFormulas.value = currentSelected
    }

    fun selectAllFormulas() {
        val allFormulas = getFormulasForSelectedBook()
        _selectedFormulas.value = allFormulas.map { it.id }.toSet()
    }

    fun deselectAllFormulas() {
        _selectedFormulas.value = emptySet()
    }

    fun isFormulaSelected(formulaId: Int): Boolean {
        return _selectedFormulas.value.contains(formulaId)
    }

    fun isAllSelected(): Boolean {
        val allFormulas = getFormulasForSelectedBook()
        return allFormulas.isNotEmpty() &&
                _selectedFormulas.value.size == allFormulas.size &&
                allFormulas.all { _selectedFormulas.value.contains(it.id) }
    }

    fun getSelectedFormulasCount(): Int = _selectedFormulas.value.size

    // ==================== SYNC OPERATIONS ====================

    fun uploadSelectedFormulas() {
        val selectedBook = _selectedBook.value
        if (selectedBook == null) {
            _uiState.update { it.copy(error = "Please select a book first") }
            return
        }

        if (_selectedFormulas.value.isEmpty()) {
            _uiState.update { it.copy(error = "Please select at least one formula") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isUploading = true,
                        uploadProgress = 0f,
                        error = null,
                        syncMessage = "Starting upload..."
                    )
                }

                // First ensure the book is uploaded
                var bookRemoteId = selectedBook.remoteId
                if (bookRemoteId == null) {
                    _uiState.update { it.copy(syncMessage = "Uploading book...") }
                    val bookResult = syncService.uploadBook(selectedBook)

                    if (bookResult.isFailure) {
                        throw Exception("Failed to upload book: ${bookResult.exceptionOrNull()?.message}")
                    }

                    bookRemoteId = bookResult.getOrThrow()

                    // Update local book with remote ID
                    val updatedBook = selectedBook.copy(
                        remoteId = bookRemoteId,
                        lastSyncedAt = System.currentTimeMillis(),
                        isDirty = false
                    )
                    database.bookDao().updateBook(updatedBook)
                    _selectedBook.value = updatedBook
                }

                // Upload selected formulas
                val selectedFormulaEntities = getFormulasForSelectedBook()
                    .filter { _selectedFormulas.value.contains(it.id) }

                if (selectedFormulaEntities.isEmpty()) {
                    throw Exception("No formulas found to upload")
                }

                _uiState.update { it.copy(syncMessage = "Uploading ${selectedFormulaEntities.size} formulas...") }

                val uploadResults = mutableListOf<BatchUploadResult>()
                selectedFormulaEntities.forEachIndexed { index, formula ->
                    val progress = (index + 1).toFloat() / selectedFormulaEntities.size
                    _uiState.update {
                        it.copy(
                            uploadProgress = progress,
                            syncMessage = "Uploading formula: ${formula.name}"
                        )
                    }

                    val result = syncService.uploadFormula(formula, bookRemoteId)
                    val uploadResult = BatchUploadResult(
                        localId = formula.id,
                        remoteId = result.getOrNull(),
                        success = result.isSuccess,
                        error = result.exceptionOrNull()?.message
                    )
                    uploadResults.add(uploadResult)

                    // Update local formula with remote ID if successful
                    if (result.isSuccess) {
                        val remoteId = result.getOrThrow()
                        val updatedFormula = formula.copy(
                            remoteId = remoteId,
                            lastSyncedAt = System.currentTimeMillis(),
                            isDirty = false
                        )
                        database.formulaDao().updateFormula(updatedFormula)
                    }
                }

                val successCount = uploadResults.count { it.success }
                val failureCount = uploadResults.count { !it.success }

                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 1f,
                        syncMessage = "Upload completed: $successCount successful, $failureCount failed",
                        lastSyncResult = SyncResult(
                            downloadedBooks = emptyList(),
                            downloadedFormulas = emptyList(),
                            uploadedBooks = emptyList(),
                            uploadedFormulas = uploadResults,
                            syncTimestamp = System.currentTimeMillis().toString()
                        )
                    )
                }

                // Reload data to reflect changes
                loadBooksAndFormulas()

            } catch (e: Exception) {
                Log.e(TAG, "Error uploading formulas", e)
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        error = "Upload failed: ${e.message}",
                        syncMessage = null
                    )
                }
            }
        }
    }

    fun uploadAllFormulas() {
        selectAllFormulas()
        uploadSelectedFormulas()
    }

    fun performFullSync() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isFullSyncing = true,
                        syncProgress = 0f,
                        error = null,
                        syncMessage = "Starting full sync..."
                    )
                }

                // Get all local data
                val allBooks = database.bookDao().getAllBooks().first()
                val allFormulas = mutableListOf<FormulaEntity>()

                allBooks.forEach { book ->
                    val bookFormulas = database.formulaDao().getFormulasByBookId(book.id).first()
                    allFormulas.addAll(bookFormulas)
                }

                _uiState.update {
                    it.copy(
                        syncProgress = 0.1f,
                        syncMessage = "Performing full sync..."
                    )
                }

                val syncResult = syncService.performFullSync(allBooks, allFormulas)

                if (syncResult.isFailure) {
                    throw Exception("Full sync failed: ${syncResult.exceptionOrNull()?.message}")
                }

                val result = syncResult.getOrThrow()

                _uiState.update {
                    it.copy(
                        syncProgress = 0.8f,
                        syncMessage = "Updating local database..."
                    )
                }

                // Update local database with remote IDs from successful uploads
                result.uploadedBooks.forEach { uploadResult ->
                    if (uploadResult.success && uploadResult.remoteId != null) {
                        val book = allBooks.find { it.id == uploadResult.localId }
                        if (book != null) {
                            val updatedBook = book.copy(
                                remoteId = uploadResult.remoteId,
                                lastSyncedAt = System.currentTimeMillis(),
                                isDirty = false
                            )
                            database.bookDao().updateBook(updatedBook)
                        }
                    }
                }

                result.uploadedFormulas.forEach { uploadResult ->
                    if (uploadResult.success && uploadResult.remoteId != null) {
                        val formula = allFormulas.find { it.id == uploadResult.localId }
                        if (formula != null) {
                            val updatedFormula = formula.copy(
                                remoteId = uploadResult.remoteId,
                                lastSyncedAt = System.currentTimeMillis(),
                                isDirty = false
                            )
                            database.formulaDao().updateFormula(updatedFormula)
                        }
                    }
                }

                val totalUploaded = result.uploadedBooks.size + result.uploadedFormulas.size
                val totalDownloaded = result.downloadedBooks.size + result.downloadedFormulas.size

                _uiState.update {
                    it.copy(
                        isFullSyncing = false,
                        syncProgress = 1f,
                        syncMessage = "Full sync completed: $totalUploaded uploaded, $totalDownloaded downloaded",
                        lastSyncResult = result
                    )
                }

                // Reload data to reflect changes
                loadBooksAndFormulas()

            } catch (e: Exception) {
                Log.e(TAG, "Error during full sync", e)
                _uiState.update {
                    it.copy(
                        isFullSyncing = false,
                        syncProgress = 0f,
                        error = "Full sync failed: ${e.message}",
                        syncMessage = null
                    )
                }
            }
        }
    }

    fun downloadFromRemote() {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isDownloading = true,
                        error = null,
                        syncMessage = "Downloading from remote..."
                    )
                }

                val booksResult = syncService.fetchBooks()
                if (booksResult.isFailure) {
                    throw Exception("Failed to fetch books: ${booksResult.exceptionOrNull()?.message}")
                }

                val remoteBooks = booksResult.getOrThrow()
                val remoteBookIds = remoteBooks.mapNotNull { it.id }

                val formulasResult = syncService.fetchFormulas(remoteBookIds)
                if (formulasResult.isFailure) {
                    throw Exception("Failed to fetch formulas: ${formulasResult.exceptionOrNull()?.message}")
                }

                val remoteFormulas = formulasResult.getOrThrow()

                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        syncMessage = "Downloaded ${remoteBooks.size} books and ${remoteFormulas.size} formulas"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error downloading from remote", e)
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        error = "Download failed: ${e.message}",
                        syncMessage = null
                    )
                }
            }
        }
    }

    // ==================== ERROR HANDLING ====================

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }
}

// ==================== DATA CLASSES ====================

data class BookWithFormulas(
    val book: BookEntity,
    val formulas: List<FormulaEntity>
)

data class SyncUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isDownloading: Boolean = false,
    val isFullSyncing: Boolean = false,
    val uploadProgress: Float = 0f,
    val syncProgress: Float = 0f,
    val error: String? = null,
    val syncMessage: String? = null,
    val lastSyncResult: SyncResult? = null
)