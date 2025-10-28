package com.example.equa_notepad_plats.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.repositories.BookRepository
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val books: List<BookEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val repository: BookRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getAllBooks().collect { books ->
                    _uiState.value = _uiState.value.copy(
                        books = books,
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

    fun createBook(name: String, description: String, imageUri: String? = null) {
        viewModelScope.launch {
            try {
                val book = BookEntity(
                    name = name,
                    description = description,
                    imageUri = imageUri
                )
                repository.insertBook(book)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al crear formulario: ${e.message}"
                )
            }
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            try {
                repository.deleteBook(book)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al eliminar formulario: ${e.message}"
                )
            }
        }
    }
}