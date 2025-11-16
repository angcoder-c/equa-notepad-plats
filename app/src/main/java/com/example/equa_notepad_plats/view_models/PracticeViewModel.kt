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

    private val randomExercises = listOf(
        "¿En qué puedo ayudarte?",
        "Resuelve la siguiente ecuación: 2x + 5 = 15. ¿Cuál es el valor de x?",
        "Si el área de un rectángulo es 24 cm² y su ancho es 4 cm, ¿cuál es su largo?",
        "Calcula la derivada de f(x) = 3x² + 2x - 1",
        "¿Cuánto es √(64) + 3²?",
        "Si tienes 5 manzanas y comes 2, luego compras el doble de las que tienes, ¿cuántas manzanas tienes al final?",
        "Resuelve el sistema de ecuaciones:\n2x + y = 7\nx - y = 2",
        "¿Cuál es el perímetro de un círculo con radio 5 cm? (usa π = 3.14)",
        "Si un auto viaja a 60 km/h durante 2.5 horas, ¿qué distancia recorre?",
        "Factoriza la expresión: x² - 9",
        "¿Cuál es el 15% de 80?",
        "Si lanzas una moneda 3 veces, ¿cuál es la probabilidad de obtener al menos una cara?"
    )

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