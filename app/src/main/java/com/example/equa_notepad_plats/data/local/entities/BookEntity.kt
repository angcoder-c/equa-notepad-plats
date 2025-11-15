package com.example.equa_notepad_plats.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val lastSyncedAt: Long? = null,
    val isDirty: Boolean = false
)
