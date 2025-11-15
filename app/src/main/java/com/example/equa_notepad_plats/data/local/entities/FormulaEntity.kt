package com.example.equa_notepad_plats.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "formulas",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class FormulaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookId: Int,
    val name: String,
    val formulaText: String,
    val description: String? = null,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val lastSyncedAt: Long? = null,
    val isDirty: Boolean = false
)