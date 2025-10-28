package com.example.equa_notepad_plats.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FormulaFormState(
    val name: String = "",
    val formulaText: String = "",
    val description: String = "",
    val imageUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

class FormulaViewModel(
    private val repository: FormulaRepository,
    private val bookId: Int,
    private val formulaId: Int? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormulaFormState())
    val uiState: StateFlow<FormulaFormState> = _uiState.asStateFlow()

    init {
        if (formulaId != null) {
            loadFormula(formulaId)
        }
    }

    private fun loadFormula(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val formula = repository.getFormulaById(id)
                if (formula != null) {
                    _uiState.value = _uiState.value.copy(
                        name = formula.name,
                        formulaText = formula.formulaText,
                        description = formula.description ?: "",
                        imageUri = formula.imageUri,
                        isLoading = false
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

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateFormulaText(text: String) {
        _uiState.value = _uiState.value.copy(formulaText = text)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateImageUri(uri: String?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    fun saveFormula() {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.name.isBlank()) {
                _uiState.value = state.copy(
                    error = "El nombre es requerido"
                )
                return@launch
            }

            if (state.formulaText.isBlank()) {
                _uiState.value = state.copy(
                    error = "La fórmula es requerida"
                )
                return@launch
            }

            _uiState.value = state.copy(isLoading = true)

            try {
                if (formulaId != null) {
                    // Actualizar fórmula existente
                    val formula = FormulaEntity(
                        id = formulaId,
                        bookId = bookId,
                        name = state.name,
                        formulaText = state.formulaText,
                        description = state.description.ifBlank { null },
                        imageUri = state.imageUri
                    )
                    repository.updateFormula(formula)
                } else {
                    // Crear nueva fórmula
                    val formula = FormulaEntity(
                        bookId = bookId,
                        name = state.name,
                        formulaText = state.formulaText,
                        description = state.description.ifBlank { null },
                        imageUri = state.imageUri
                    )
                    repository.insertFormula(formula)
                }

                _uiState.value = state.copy(
                    isLoading = false,
                    isSaved = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Error al guardar: ${e.message}"
                )
            }
        }
    }
}