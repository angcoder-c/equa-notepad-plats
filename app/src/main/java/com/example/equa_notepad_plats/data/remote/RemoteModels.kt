package com.example.equa_notepad_plats.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteUser(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("photo_url")
    val photoUrl: String? = null,
    @SerialName("is_guest")
    val isGuest: Boolean = false
)

@Serializable
data class RemoteBook(
    val id: Int? = null,

    @SerialName("user_id")
    val userId: String,

    val name: String,
    val description: String,

    @SerialName("image_uri")
    val imageUri: String? = null,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("remote_id")
    val remoteId: String? = null,

    @SerialName("last_synced_at")
    val lastSyncedAt: Long? = null,

    @SerialName("is_dirty")
    val isDirty: Boolean = false
)

@Serializable
data class RemoteFormula(
    val id: Int? = null,

    @SerialName("book_id")
    val bookId: Int,

    @SerialName("user_id")
    val userId: String,

    val name: String,

    @SerialName("formula_text")
    val formulaText: String,

    val description: String? = null,

    @SerialName("image_uri")
    val imageUri: String? = null,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("remote_id")
    val remoteId: String? = null,

    @SerialName("last_synced_at")
    val lastSyncedAt: Long? = null,

    @SerialName("is_dirty")
    val isDirty: Boolean = false
)

@Serializable
data class SyncRequest(
    @SerialName("last_sync")
    val lastSync: Long? = null
)

@Serializable
data class SyncResponse(
    val books: List<RemoteBook>,
    val formulas: List<RemoteFormula>,

    @SerialName("sync_timestamp")
    val syncTimestamp: Long
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)