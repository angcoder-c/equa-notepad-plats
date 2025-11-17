package com.example.equa_notepad_plats.view_models

import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.data.repositories.BookRepository
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.equa_notepad_plats.data.OpenRouterClient

data class ChatMessage(
    val content: String,
    val isFromUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class PracticeUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)

class PracticeViewModel (
    private val repositoryFormula: FormulaRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PracticeUiState())
    private val _selectedBook = MutableStateFlow<BookEntity?>(null)
    private val _selectedBookId = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()
    val selectedBook: StateFlow<BookEntity?> = _selectedBook.asStateFlow()
    val selectedBookId: StateFlow<Int?> = _selectedBookId.asStateFlow()



    fun generateExerciseWithAI(bookId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = ""
                )
                if (_selectedBookId.value != null) {
                    repositoryFormula.getFormulasByBookId(_selectedBookId.value ?: 0).collect { formulas ->
                        val formulasText = formulas.joinToString(separator = "\n") {
                            "- ${it.name}: ${it.formulaText} :${it.description}"
                        }
                        val prompt = """
                        Eres un generador de ejercicios de matemáticas.
                        Usa solo las siguientes fórmulas para generar un ejercicio:
                        no uses latex para generar tu respuesta y inicia la respuesta con Ejercicio:
                        utiliza lenguaje natural para describir el ejercicio.
                        $formulasText
    
                        Genera un ejercicio adecuado para estudiantes.
                    """.trimIndent()
                        Log.d("generateExerciseWithAI", "prompt: $prompt")

                        val aiResponse = OpenRouterClient.ask(prompt)

                        val newMessage = ChatMessage(
                            content = aiResponse,
                            isFromUser = false
                        )

                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + newMessage,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al comunicar con la IA: ${e.message}"
                )
            }
        }
    }


    fun clearMessages() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }

    fun setBookId(id: Int, book: BookEntity?) {
        _selectedBook.value = book
        _selectedBookId.value = id
    }
}