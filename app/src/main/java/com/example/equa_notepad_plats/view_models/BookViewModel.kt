package com.example.equa_notepad_plats.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import com.example.equa_notepad_plats.data.repositories.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookUiState(
    val book: BookEntity? = null,
    val formulas: List<FormulaEntity> = emptyList(),
    val isLoading: Boolean = false,
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
                // informacion del formulario
                val book = repositoryBook.getBookById(bookId)

                // formulas del formulario
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al eliminar formula: ${e.message}"
                )
            }
        }
    }
}