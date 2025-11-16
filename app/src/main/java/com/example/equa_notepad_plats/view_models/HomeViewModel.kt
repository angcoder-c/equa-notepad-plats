package com.example.equa_notepad_plats.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.repositories.BookRepository
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.data.SupabaseApiService
import com.example.equa_notepad_plats.data.remote.RemoteBook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.equa_notepad_plats.utils.UserSyncHelper

data class HomeUiState(
    val books: List<BookEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val syncStatus: BookSyncStatus = BookSyncStatus.IDLE,
    val syncMessage: String? = null
)

enum class BookSyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

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

    /**
     * Crea un libro guardándolo localmente en Room
     * y marcándolo como pendiente de sincronización
     */
    fun createBook(name: String, description: String, imageUri: String? = null) {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val book = BookEntity(
                    id = 0, // Room asignará el ID
                    name = name,
                    description = description,
                    imageUri = imageUri,
                    createdAt = currentTime,
                    isDirty = true, // Marcar como pendiente de sincronizar
                    remoteId = null,
                    lastSyncedAt = null
                )
                repository.insertBook(book)

                Log.d("HomeViewModel", "Book created locally: $name")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al crear libro: ${e.message}"
                )
            }
        }
    }

    /**
     * Crea un libro y lo sincroniza inmediatamente con Supabase
     * Requiere que el usuario esté autenticado y disponible en el repositorio
     */
    fun createBookAndSync(
        name: String,
        description: String,
        imageUri: String? = null,
        userId: String,
        userName: String,
        userEmail: String,
        userPhotoUrl: String? = null,
        isGuest: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isSyncing = true,
                syncMessage = "Verificando usuario..."
            )

            try {
                // Primero asegurar que el usuario existe en Supabase
                val userSynced = UserSyncHelper.ensureUserSynced(
                    userId = userId,
                    name = userName,
                    email = userEmail,
                    photoUrl = userPhotoUrl,
                    isGuest = isGuest
                )

                if (!userSynced) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSyncing = false,
                        syncStatus = BookSyncStatus.ERROR,
                        error = "No se pudo sincronizar el usuario. El libro se guardó localmente."
                    )
                    // Aún así crear el libro localmente
                    createBook(name, description, imageUri)
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    syncMessage = "Creando libro..."
                )

                val currentTime = System.currentTimeMillis()

                // 1. Guardar localmente primero
                val localBook = BookEntity(
                    id = 0,
                    name = name,
                    description = description,
                    imageUri = imageUri,
                    createdAt = currentTime,
                    isDirty = true,
                    remoteId = null,
                    lastSyncedAt = null
                )

                val localId = repository.insertBook(localBook).toInt()
                Log.d("HomeViewModel", "Book created locally with ID: $localId")

                // 2. Sincronizar con Supabase
                _uiState.value = _uiState.value.copy(
                    syncMessage = "Sincronizando con servidor..."
                )

                val remoteBook = RemoteBook(
                    id = null,
                    userId = userId,
                    name = name,
                    description = description,
                    imageUri = imageUri,
                    createdAt = currentTime,
                    remoteId = null,
                    lastSyncedAt = null,
                    isDirty = false
                )

                val result = SupabaseApiService.createRemoteBook(remoteBook)

                result.fold(
                    onSuccess = { apiResponse ->
                        if (apiResponse.success && apiResponse.data != null) {
                            // Extraer el ID remoto
                            val remoteId = apiResponse.data.id?.toString()

                            // Actualizar el libro local con el remoteId
                            val syncedBook = localBook.copy(
                                id = localId,
                                isDirty = false,
                                lastSyncedAt = System.currentTimeMillis(),
                                remoteId = remoteId
                            )
                            repository.updateBook(syncedBook)

                            Log.d("HomeViewModel", "Book synced successfully with remote ID: $remoteId")

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSyncing = false,
                                syncStatus = BookSyncStatus.SUCCESS,
                                syncMessage = "Libro creado y sincronizado correctamente",
                                error = null
                            )
                        } else {
                            // Guardado local exitoso pero sync falló
                            Log.e("HomeViewModel", "API returned success=false: ${apiResponse.error}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSyncing = false,
                                syncStatus = BookSyncStatus.ERROR,
                                syncMessage = "Libro creado localmente. Sincronización pendiente.",
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        // Guardado local exitoso pero sync falló
                        Log.e("HomeViewModel", "Error syncing book to remote", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSyncing = false,
                            syncStatus = BookSyncStatus.ERROR,
                            syncMessage = "Libro creado localmente. Sincronización pendiente.",
                            error = null
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSyncing = false,
                    syncStatus = BookSyncStatus.ERROR,
                    error = "Error al crear libro: ${e.message}"
                )
            }
        }
    }

    /**
     * Sincroniza todos los libros pendientes (isDirty=true) con Supabase
     */
    fun syncBooksToRemote(userId: String, isGuest: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                syncStatus = BookSyncStatus.SYNCING,
                syncMessage = "Sincronizando libros..."
            )

            try {
                val books = _uiState.value.books
                val dirtyBooks = books.filter { it.isDirty && it.remoteId == null }

                if (dirtyBooks.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        syncStatus = BookSyncStatus.SUCCESS,
                        syncMessage = "No hay libros pendientes de sincronizar"
                    )
                    return@launch
                }

                var successCount = 0
                var errorCount = 0

                dirtyBooks.forEach { book ->
                    val remoteBook = RemoteBook(
                        id = null,
                        userId = userId,
                        name = book.name,
                        description = book.description,
                        imageUri = book.imageUri,
                        createdAt = book.createdAt,
                        remoteId = null,
                        lastSyncedAt = null,
                        isDirty = false
                    )

                    val result = SupabaseApiService.createRemoteBook(remoteBook)

                    result.fold(
                        onSuccess = { apiResponse ->
                            if (apiResponse.success && apiResponse.data != null) {
                                val remoteId = apiResponse.data.id?.toString()

                                val updatedBook = book.copy(
                                    isDirty = false,
                                    lastSyncedAt = System.currentTimeMillis(),
                                    remoteId = remoteId
                                )
                                repository.updateBook(updatedBook)
                                successCount++
                                Log.d("HomeViewModel", "Book ${book.id} synced successfully")
                            } else {
                                errorCount++
                                Log.e("HomeViewModel", "API returned success=false: ${apiResponse.error}")
                            }
                        },
                        onFailure = { error ->
                            errorCount++
                            Log.e("HomeViewModel", "Error syncing book ${book.id}", error)
                        }
                    )
                }

                val finalStatus = if (errorCount == 0) BookSyncStatus.SUCCESS else BookSyncStatus.ERROR
                val message = if (errorCount == 0) {
                    "Sincronizados $successCount libros exitosamente"
                } else {
                    "Sincronizados: $successCount"
                }

                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncStatus = finalStatus,
                    syncMessage = message
                )

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error syncing books", e)
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncStatus = BookSyncStatus.ERROR,
                    syncMessage = "Libros sincronizados anteriormente"
                )
            }
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            try {
                repository.deleteBook(book)

                // TODO: Si el libro tiene remoteId, también eliminarlo de Supabase
                // Necesitarías crear un endpoint delete-book en tus edge functions
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al eliminar libro: ${e.message}"
                )
            }
        }
    }

    fun clearSyncStatus() {
        _uiState.value = _uiState.value.copy(
            syncStatus = BookSyncStatus.IDLE,
            syncMessage = null,
            error = null
        )
    }

    fun refreshBooks() {
        loadBooks()
    }
}