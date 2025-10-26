package com.example.equa_notepad_plats.data.repositories

import com.example.equa_notepad_plats.data.local.AppDatabase
import com.example.equa_notepad_plats.data.local.entities.UserEntity

class UserRepository(
    private val database: AppDatabase
) {
    suspend fun getUser(): UserEntity? = database.userDao().getUser()

    suspend fun insertUser(user: UserEntity) = database.userDao().insertUser(user)

    suspend fun deleteUser() = database.userDao().deleteUser()
}