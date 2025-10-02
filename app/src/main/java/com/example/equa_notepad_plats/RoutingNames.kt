package com.example.equa_notepad_plats

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    object Home : Route

    @Serializable
    data class Book(val bookId: String) : Route

    @Serializable
    object Profile : Route
}