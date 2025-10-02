package com.example.equa_notepad_plats.view_models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val name: String = "",
    val lastName: String = "",
    val role: String = "",
    val isLoading: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        _uiState.value = _uiState.value.copy(
            name = "Test name",
            lastName = "Test last name",
            role = "Test role"
        )
    }

    fun changePassword() {}

    fun updateProfile(name: String, lastName: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            lastName = lastName
        )
    }
}