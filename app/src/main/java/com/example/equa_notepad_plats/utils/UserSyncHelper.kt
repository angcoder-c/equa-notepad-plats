package com.example.equa_notepad_plats.utils

import android.util.Log
import com.example.equa_notepad_plats.data.SupabaseApiService
import com.example.equa_notepad_plats.data.remote.RemoteUser

/**
 * Helper para asegurar que el usuario esté sincronizado con Supabase
 * antes de crear libros o fórmulas
 */
object UserSyncHelper {

    private var isUserSynced = false
    private var syncedUserId: String? = null

    /**
     * Sincroniza el usuario con Supabase si aún no está sincronizado
     *
     * @param userId ID del usuario local
     * @param name Nombre del usuario
     * @param email Email del usuario
     * @param photoUrl URL de foto (opcional)
     * @param isGuest Si es usuario invitado
     * @return true si el usuario existe o fue creado exitosamente
     */
    suspend fun ensureUserSynced(
        userId: String,
        name: String,
        email: String,
        photoUrl: String? = null,
        isGuest: Boolean = false
    ): Boolean {
        // Si ya está sincronizado este usuario, retornar true
        if (isUserSynced && syncedUserId == userId) {
            Log.d("UserSyncHelper", "User already synced: $userId")
            return true
        }

        Log.d("UserSyncHelper", "Attempting to sync user: $userId")

        val remoteUser = RemoteUser(
            id = userId,
            name = name,
            email = email,
            photoUrl = photoUrl,
            isGuest = isGuest
        )

        val result = SupabaseApiService.registerRemoteUser(remoteUser)

        return result.fold(
            onSuccess = { apiResponse ->
                if (apiResponse.success) {
                    isUserSynced = true
                    syncedUserId = userId
                    Log.d("UserSyncHelper", "User synced successfully: $userId")
                    true
                } else {
                    Log.e("UserSyncHelper", "Failed to sync user: ${apiResponse.error}")
                    false
                }
            },
            onFailure = { error ->
                Log.e("UserSyncHelper", "Error syncing user", error)
                false
            }
        )
    }

    /**
     * Resetea el estado de sincronización (útil al cerrar sesión)
     */
    fun reset() {
        isUserSynced = false
        syncedUserId = null
    }

    /**
     * Verifica si el usuario está sincronizado
     */
    fun isSynced(userId: String): Boolean {
        return isUserSynced && syncedUserId == userId
    }
}