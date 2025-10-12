package com.example.equa_notepad_plats.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PracticeUiState(
    val exercise: String = "",
    val isLoading: Boolean = false,
    val error: String = ""
)

class PracticeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    fun generateExerciseWithAI(formulaId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = ""
                )
                // SIMULADO
                delay(2000)

                // TODO: Reemplazar con llamada real a tu API de IA
                // val response = aiService.generateExercise(formulaId)
                val generatedExercise = generateMockExercise(formulaId)

                _uiState.value = _uiState.value.copy(
                    exercise = generatedExercise,
                    isLoading = false,
                    error = ""
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al generar ejercicio: ${e.message}"
                )
            }
        }
    }

    private fun generateMockExercise(formulaId: String): String {
        return """
            EJERCICIO
        """.trimIndent()
    }
}