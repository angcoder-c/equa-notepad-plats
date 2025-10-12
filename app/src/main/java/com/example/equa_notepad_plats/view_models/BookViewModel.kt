package com.example.equa_notepad_plats.view_models

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Formula(
    val id: String,
    val title: String,
    val formula: String
)

data class BookUiState(
    val bookTitle: String = "",
    val formulas: List<Formula> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class BookViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BookUiState())
    val uiState: StateFlow<BookUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: String) {
        _uiState.value = _uiState.value.copy(
            bookTitle = "Formulario 1",
            formulas = listOf(
                Formula("1", "Formula", "A=πr²"),
                Formula("2", "Formula", "f=m·a")
            )
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun addNewFormula() {}

    fun editFormula(id: String) {}

    fun shareFormula(id: String) {}

    fun deleteFormula(id: String) {
        val currentFormulas = _uiState.value.formulas
        _uiState.value = _uiState.value.copy(
            formulas = currentFormulas.filter { it.id != id }
        )
    }

    val filteredFormulas: StateFlow<List<Formula>>
        get() = MutableStateFlow(
            _uiState.value.formulas.filter {
                it.title.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                        it.formula.contains(_uiState.value.searchQuery, ignoreCase = true)
            }
        )
}