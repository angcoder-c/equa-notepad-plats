package com.example.equa_notepad_plats.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val isGuest: Boolean = false
)