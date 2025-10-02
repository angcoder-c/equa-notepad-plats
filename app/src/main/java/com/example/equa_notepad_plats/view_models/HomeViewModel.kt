package com.example.equa_notepad_plats.view_models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Book(
    val id: String,
    val title: String,
    val description: String
)

data class HomeUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        // formularios de preba
        _uiState.value = _uiState.value.copy(
            books = listOf(
                Book("1", "Formulario 1", "Descripcion"),
                Book("2", "Formulario 2", "Descripcion"),
                Book("3", "Formulario 3", "Descripcion")
            )
        )
    }

    fun addNewBook() {}
}