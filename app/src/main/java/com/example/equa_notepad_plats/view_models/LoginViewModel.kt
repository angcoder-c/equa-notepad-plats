package com.example.equa_notepad_plats.view_models

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val email: String = "",
    val name: String = ""
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val WEB_CLIENT_ID = "web_client_id"

    fun signInWithGoogle(
        credentialManager: CredentialManager,
        context: Context,
        onSuccess: (email: String, name: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = "")

                val nonce = generateNonce()

                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .setNonce(nonce)
                    .build()

                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )

                handleCredentialResponse(result, onSuccess, onError)

            } catch (e: GetCredentialException) {
                Log.e("LoginViewModel", "Sign in failed, attempting sign up", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError("No se encontraron cuentas autorizadas. Intenta registrarte.")
            }
        }
    }

    fun signUpWithGoogle(
        credentialManager: CredentialManager,
        context: Context,
        onSuccess: (email: String, name: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = "")

                val nonce = generateNonce()

                val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setNonce(nonce)
                    .build()

                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )

                handleCredentialResponse(result, onSuccess, onError)

            } catch (e: GetCredentialException) {
                Log.e("LoginViewModel", "Sign up failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error durante el registro: ${e.message}"
                )
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    private suspend fun handleCredentialResponse(
        result: androidx.credentials.GetCredentialResponse,
        onSuccess: (email: String, name: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        try {
            val credential = result.credential

            when (credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)

                            val idToken = googleIdTokenCredential.idToken
                            val displayName = googleIdTokenCredential.displayName ?: "Usuario"
                            val email = googleIdTokenCredential.id

                            Log.d("LoginViewModel", "Authentication successful")
                            Log.d("LoginViewModel", "Email: $email")
                            Log.d("LoginViewModel", "Name: $displayName")

                            // TODO: valir idtoken
                            // val isValid = validateTokenOnServer(idToken)

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                email = email,
                                name = displayName
                            )

                            onSuccess(email, displayName)

                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e("LoginViewModel", "Invalid Google ID token", e)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Token inválido"
                            )
                            onError("Token inválido: ${e.message}")
                        }
                    } else {
                        val error = "Tipo de credencial no reconocido"
                        Log.e("LoginViewModel", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error
                        )
                        onError(error)
                    }
                }

                else -> {
                    val error = "Tipo de credencial inesperado"
                    Log.e("LoginViewModel", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error
                    )
                    onError(error)
                }
            }

        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error handling credential response", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Error: ${e.message}"
            )
            onError(e.message ?: "Error desconocido")
        }
    }

    private fun generateNonce(): String {
        return UUID.randomUUID().toString()
    }
}