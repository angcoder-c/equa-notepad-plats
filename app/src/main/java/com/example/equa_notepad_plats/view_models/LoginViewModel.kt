package com.example.equa_notepad_plats.view_models

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.equa_notepad_plats.BuildConfig
import com.example.equa_notepad_plats.data.SupabaseApiService
import com.example.equa_notepad_plats.data.local.entities.UserEntity
import com.example.equa_notepad_plats.data.remote.RemoteUser
import com.example.equa_notepad_plats.data.repositories.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken

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

    // verifica si ya hay un usuario guardado
    private fun checkExistingUser() {
        viewModelScope.launch {
            try {
                // usuario guardado localmente
                val localUser = repository.getUser()
                if (localUser != null) {
                    _uiState.value = LoginUiState.Success(localUser)
                    return@launch
                }

                // sesion activa en Supabase
                val session = supabaseClient.auth.currentSessionOrNull()
                if (session != null) {
                    handleSupabaseSession()
                }
            } catch (e: Exception) {}
        }
    }

    // crea un nonce hasheado para Google Sign-In
    // https://www.youtube.com/watch?v=ZgYvexniGDA
    private fun createNonce(): Pair<String, String> {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it ->
            str + "%02x".format(it)
        }
        return Pair(rawNonce, hashedNonce)
    }

    // inicia el proceso de google sign in
    // https://www.youtube.com/watch?v=ZgYvexniGDA
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val (rawNonce, hashedNonce) = createNonce()

                // google id
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .setNonce(hashedNonce)
                    .setAutoSelectEnabled(true) // auto-seleccionar cuenta
                    .setFilterByAuthorizedAccounts(false) // mostrar todas las cuentas
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)

                // obtener credenciales
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(result.credential.data)

                val googleIdToken = googleIdTokenCredential.idToken

                // autenticacion con supabase usando el token de google
                supabaseClient.auth.signInWith(IDToken) {
                    idToken = googleIdToken
                    provider = Google
                    nonce = rawNonce
                }

                handleSupabaseSession()

            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                Log.e("LoginViewModel", "GetCredentialException: ${e.message}", e)

                val errorMessage = when (e) {
                    is androidx.credentials.exceptions.NoCredentialException ->
                        "No hay cuentas de Google disponibles. Por favor, agrega una cuenta de Google en tu dispositivo."
                    is androidx.credentials.exceptions.GetCredentialCancellationException ->
                        "Inicio de sesión cancelado"
                    else ->
                        "Error de credenciales: ${e.message}"
                }

                _uiState.value = LoginUiState.Error(errorMessage)

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Google Sign-In error: ${e.message}", e)
                _uiState.value = LoginUiState.Error(
                    "Error al iniciar sesión: ${e.message ?: "Error desconocido"}"
                )
            }
        }
    }

    // procesa la sesion actual de supabase y guarda el usuario localmente
    private suspend fun handleSupabaseSession() {
        try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val supabaseUser = session?.user

            if (supabaseUser != null) {
                val metadata = supabaseUser.userMetadata

                val name = metadata?.get("full_name")?.toString()
                    ?: metadata?.get("name")?.toString()
                    ?: supabaseUser.email?.substringBefore("@")
                    ?: "Usuario"

                val photoUrl = metadata?.get("avatar_url")?.toString()
                    ?: metadata?.get("picture")?.toString()

                val user = UserEntity(
                    id = supabaseUser.id,
                    name = name,
                    email = supabaseUser.email ?: "",
                    photoUrl = photoUrl,
                    isGuest = false
                )
                repository.insertUser(user)

                val remoteUser = RemoteUser(
                    id = supabaseUser.id,
                    name = name,
                    email = user.email,
                    photoUrl = photoUrl,
                    isGuest = false
                )

                val apiResult = SupabaseApiService.registerRemoteUser(remoteUser)

                if (apiResult==null) {
                    Log.e("LoginViewModel", "WARN: Remote user registration failed")
                }

                _uiState.value = LoginUiState.Success(user)

            } else {
                _uiState.value = LoginUiState.Error("No se pudo obtener la sesión del usuario")
            }
        } catch (e: Exception) {
            _uiState.value = LoginUiState.Error("Error al procesar sesión: ${e.message}")
        }
    }

    // entrar como invitado
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
                    "Error en modo invitado: ${e.message}"
                )
            }
        }
    }

    // reinicia el estado de la UI
    fun resetState() {
        _uiState.value = LoginUiState.Initial
    }

    // logout
    fun signOut() {
        viewModelScope.launch {
            try {
                // cerrar sesión en Supabase
                supabaseClient.auth.signOut()

                // eliminar usuario de la base de datos local
                repository.deleteUser()

                // resetear estado
                _uiState.value = LoginUiState.Initial
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    "Error al cerrar sesión: ${e.message}"
                )
            }
        }
    }
}