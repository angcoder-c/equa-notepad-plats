package com.example.equa_notepad_plats.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.SupabaseApiService
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import com.example.equa_notepad_plats.data.remote.RemoteFormula
import com.example.equa_notepad_plats.data.repositories.BookRepository
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookUiState(
    val book: BookEntity? = null,
    val formulas: List<FormulaEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val error: String? = null
)

class BookViewModel(
    private val repository: FormulaRepository,
    private val repositoryBook: BookRepository,
    private val bookId: Int
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookUiState())
    val uiState: StateFlow<BookUiState> = _uiState.asStateFlow()

    init {
        loadBookAndFormulas()
    }

    private fun loadBookAndFormulas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get book info
                val book = repositoryBook.getBookById(bookId)

                // Get formulas for this book
                repository.getFormulasByBookId(bookId).collect { formulas ->
                    _uiState.value = _uiState.value.copy(
                        book = book,
                        formulas = formulas,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteFormula(formula: FormulaEntity) {
        viewModelScope.launch {
            try {
                repository.deleteFormula(formula)

                // If formula has remoteId, also delete from Supabase
                if (formula.remoteId != null) {
                    val result = SupabaseApiService.deleteRemoteFormula(formula.remoteId)
                    result.fold(
                        onSuccess = {
                            Log.d("BookViewModel", "Formula deleted from remote: ${formula.remoteId}")
                        },
                        onFailure = { error ->
                            Log.e("BookViewModel", "Error deleting remote formula", error)
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al eliminar formula: ${e.message}"
                )
            }
        }
    }

    /**
     * Syncs all dirty formulas (that haven't been synced to remote) for this book
     * Does nothing if user is guest
     */
    fun syncFormulasToRemote(userId: String, isGuest: Boolean = false) {
        if (isGuest) {
            Log.d("BookViewModel", "Sync skipped: user is guest")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                syncMessage = "Sincronizando fórmulas..."
            )

            try {
                val formulas = _uiState.value.formulas
                val dirtyFormulas = formulas.filter { it.isDirty && it.remoteId == null }

                if (dirtyFormulas.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        syncMessage = "No hay fórmulas pendientes de sincronizar"
                    )
                    return@launch
                }

                var successCount = 0
                var errorCount = 0

                dirtyFormulas.forEach { formula ->
                    val remoteFormula = RemoteFormula(
                        id = null,
                        bookId = formula.bookId,
                        userId = userId,
                        name = formula.name,
                        formulaText = formula.formulaText,
                        description = formula.description,
                        imageUri = formula.imageUri,
                        createdAt = formula.createdAt,
                        remoteId = null,
                        lastSyncedAt = null,
                        isDirty = false
                    )

                    val result = SupabaseApiService.createRemoteFormula(remoteFormula)

                    result.fold(
                        onSuccess = { apiResponse ->
                            if (apiResponse.success && apiResponse.data != null) {
                                val remoteId = apiResponse.data.id?.toString()

                                val updatedFormula = formula.copy(
                                    isDirty = false,
                                    lastSyncedAt = System.currentTimeMillis(),
                                    remoteId = remoteId
                                )
                                repository.updateFormula(updatedFormula)
                                successCount++
                                Log.d("BookViewModel", "Formula ${formula.id} synced successfully")
                            } else {
                                errorCount++
                                Log.e("BookViewModel", "API returned success=false: ${apiResponse.error}")
                            }
                        },
                        onFailure = { error ->
                            errorCount++
                            Log.e("BookViewModel", "Error syncing formula ${formula.id}", error)
                        }
                    )
                }

                val message = if (errorCount == 0) {
                    "Sincronizadas $successCount fórmulas exitosamente"
                } else {
                    "Sincronizadas: $successCount, Errores: $errorCount"
                }

                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncMessage = message
                )

            } catch (e: Exception) {
                Log.e("BookViewModel", "Error syncing formulas", e)
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncMessage = "Error al sincronizar fórmulas",
                    error = e.message
                )
            }
        }
    }

    fun clearSyncStatus() {
        _uiState.value = _uiState.value.copy(
            syncMessage = null,
            error = null
        )
    }
}