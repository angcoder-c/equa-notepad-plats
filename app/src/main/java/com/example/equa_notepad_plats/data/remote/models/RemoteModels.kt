// File: app/src/main/java/com/example/equa_notepad_plats/data/remote/models/RemoteModels.kt
package com.example.equa_notepad_plats.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteUser(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class RemoteBook(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val description: String,
    @SerialName("image_uri")
    val imageUri: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false
)

@Serializable
data class RemoteFormula(
    val id: String? = null,
    @SerialName("book_id")
    val bookId: String,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    @SerialName("formula_text")
    val formulaText: String,
    val description: String? = null,
    @SerialName("image_uri")
    val imageUri: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false
)

@Serializable
data class SyncRequest(
    @SerialName("last_sync")
    val lastSync: String? = null
)

@Serializable
data class SyncResponse(
    val books: List<RemoteBook>,
    val formulas: List<RemoteFormula>,
    @SerialName("sync_timestamp")
    val syncTimestamp: String
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)