package com.example.equa_notepad_plats.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.SupabaseApiService
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import com.example.equa_notepad_plats.data.remote.RemoteFormula
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import com.example.equa_notepad_plats.utils.UserSyncHelper
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
    val isSyncing: Boolean = false,
    val syncStatus: FormulaSyncStatus = FormulaSyncStatus.IDLE,
    val syncMessage: String? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

enum class FormulaSyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

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

    /**
     * Saves formula locally only (marks as dirty for later sync)
     */
    fun saveFormula() {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.name.isBlank()) {
                _uiState.value = state.copy(error = "El nombre es requerido")
                return@launch
            }

            if (state.formulaText.isBlank()) {
                _uiState.value = state.copy(error = "La fórmula es requerida")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true)

            try {
                if (formulaId != null) {
                    // Update existing formula
                    val formula = FormulaEntity(
                        id = formulaId,
                        bookId = bookId,
                        name = state.name,
                        formulaText = state.formulaText,
                        description = state.description.ifBlank { null },
                        imageUri = state.imageUri,
                        isDirty = true // Mark as dirty for sync
                    )
                    repository.updateFormula(formula)
                } else {
                    // Create new formula
                    val formula = FormulaEntity(
                        bookId = bookId,
                        name = state.name,
                        formulaText = state.formulaText,
                        description = state.description.ifBlank { null },
                        imageUri = state.imageUri,
                        isDirty = true // Mark as dirty for sync
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

    /**
     * Saves formula and syncs immediately with Supabase
     */
    fun saveFormulaAndSync(
        userId: String,
        userName: String,
        userEmail: String,
        userPhotoUrl: String? = null,
        isGuest: Boolean = false
    ) {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.name.isBlank()) {
                _uiState.value = state.copy(error = "El nombre es requerido")
                return@launch
            }

            if (state.formulaText.isBlank()) {
                _uiState.value = state.copy(error = "La fórmula es requerida")
                return@launch
            }

            _uiState.value = state.copy(
                isLoading = true,
                isSyncing = true,
                syncMessage = "Verificando usuario..."
            )

            try {
                // Ensure user is synced
                val userSynced = UserSyncHelper.ensureUserSynced(
                    userId = userId,
                    name = userName,
                    email = userEmail,
                    photoUrl = userPhotoUrl,
                    isGuest = isGuest
                )

                if (!userSynced) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        isSyncing = false,
                        syncStatus = FormulaSyncStatus.ERROR,
                        syncMessage = "No se pudo sincronizar el usuario. La fórmula se guardó localmente.",
                        error = null
                    )
                    // Still save locally
                    saveFormula()
                    return@launch
                }

                _uiState.value = state.copy(
                    syncMessage = "Guardando fórmula..."
                )

                val currentTime = System.currentTimeMillis()

                // 1. Save locally first
                val localFormula = FormulaEntity(
                    id = formulaId ?: 0,
                    bookId = bookId,
                    name = state.name,
                    formulaText = state.formulaText,
                    description = state.description.ifBlank { null },
                    imageUri = state.imageUri,
                    createdAt = currentTime,
                    isDirty = true,
                    remoteId = null,
                    lastSyncedAt = null
                )

                val localId = if (formulaId != null) {
                    repository.updateFormula(localFormula)
                    formulaId
                } else {
                    repository.insertFormula(localFormula).toInt()
                }

                Log.d("FormulaViewModel", "Formula saved locally with ID: $localId")

                // 2. Sync with Supabase
                _uiState.value = state.copy(
                    syncMessage = "Sincronizando con servidor..."
                )

                val remoteFormula = RemoteFormula(
                    id = null,
                    bookId = bookId,
                    userId = userId,
                    name = state.name,
                    formulaText = state.formulaText,
                    description = state.description.ifBlank { null },
                    imageUri = state.imageUri,
                    createdAt = currentTime,
                    remoteId = null,
                    lastSyncedAt = null,
                    isDirty = false
                )

                val result = SupabaseApiService.createRemoteFormula(remoteFormula)

                result.fold(
                    onSuccess = { apiResponse ->
                        if (apiResponse.success && apiResponse.data != null) {
                            val remoteId = apiResponse.data.id?.toString()

                            // Update local formula with remoteId
                            val syncedFormula = localFormula.copy(
                                id = localId,
                                isDirty = false,
                                lastSyncedAt = System.currentTimeMillis(),
                                remoteId = remoteId
                            )
                            repository.updateFormula(syncedFormula)

                            Log.d("FormulaViewModel", "Formula synced successfully with remote ID: $remoteId")

                            _uiState.value = state.copy(
                                isLoading = false,
                                isSyncing = false,
                                isSaved = true,
                                syncStatus = FormulaSyncStatus.SUCCESS,
                                syncMessage = "Fórmula creada y sincronizada correctamente",
                                error = null
                            )
                        } else {
                            Log.e("FormulaViewModel", "API returned success=false: ${apiResponse.error}")
                            _uiState.value = state.copy(
                                isLoading = false,
                                isSyncing = false,
                                isSaved = true,
                                syncStatus = FormulaSyncStatus.ERROR,
                                syncMessage = "Fórmula creada localmente. Sincronización pendiente.",
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("FormulaViewModel", "Error syncing formula to remote", error)
                        _uiState.value = state.copy(
                            isLoading = false,
                            isSyncing = false,
                            isSaved = true,
                            syncStatus = FormulaSyncStatus.ERROR,
                            syncMessage = "Fórmula creada localmente. Sincronización pendiente.",
                            error = null
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    isSyncing = false,
                    syncStatus = FormulaSyncStatus.ERROR,
                    error = "Error al crear fórmula: ${e.message}"
                )
            }
        }
    }

    fun clearSyncStatus() {
        _uiState.value = _uiState.value.copy(
            syncStatus = FormulaSyncStatus.IDLE,
            syncMessage = null,
            error = null
        )
    }
}