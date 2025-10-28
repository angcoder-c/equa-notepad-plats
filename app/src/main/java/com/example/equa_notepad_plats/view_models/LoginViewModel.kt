package com.example.equa_notepad_plats.view_models

import android.app.Activity
import android.webkit.ConsoleMessage
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.data.repositories.UserRepository
import com.example.equa_notepad_plats.data.local.entities.UserEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: UserEntity) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val repository: UserRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingUser()
    }

    private fun checkExistingUser() {
        viewModelScope.launch {
            try {
                val user = repository.getUser()
                if (user != null) {
                    _uiState.value = LoginUiState.Success(user)
                }
            } catch (e: Exception) {
                // mantener estado inicial
            }
        }
    }

    fun signInWithGoogle(activity: Activity, webClientId: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val credentialManager = CredentialManager.create(activity)

                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashedNonce = digest.fold("") { str, it ->
                    str + "%02x".format(it)
                }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(
                    result.credential.data
                )

                val googleIdToken = googleIdTokenCredential.idToken

                supabaseClient.auth.signInWith(IDToken) {
                    idToken = googleIdToken
                    provider = Google
                    nonce = rawNonce
                }

                val session = supabaseClient.auth.currentSessionOrNull()
                val supabaseUser = session?.user

                if (supabaseUser != null) {
                    val user = UserEntity(
                        id = supabaseUser.id,
                        name = googleIdTokenCredential.displayName ?: "Usuario",
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                        isGuest = false
                    )

                    repository.insertUser(user)
                    _uiState.value = LoginUiState.Success(user)
                } else {
                    _uiState.value = LoginUiState.Error("No se pudo obtener la sesión del usuario")
                }

            } catch (e: GetCredentialException) {
                _uiState.value = LoginUiState.Error(
                    "Error al iniciar sesión: ${e.message}"
                )
            } catch (e: GoogleIdTokenParsingException) {
                _uiState.value = LoginUiState.Error(
                    "Error al procesar token de Google: ${e.message}"
                )
            } catch (e: RestException) {
                _uiState.value = LoginUiState.Error(
                    "Error de Supabase: ${e.message}"
                )
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val guestUser = UserEntity(
                    id = "guest_${System.currentTimeMillis()}",
                    name = "Invitado",
                    email = "guest@local.com",
                    photoUrl = null,
                    isGuest = true
                )

                repository.insertUser(guestUser)
                _uiState.value = LoginUiState.Success(guestUser)

            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    "Error modo invitado: ${e.message}"
                )
            }
        }
    }
}