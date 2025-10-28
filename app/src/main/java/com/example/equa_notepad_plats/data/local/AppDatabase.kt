package com.example.equa_notepad_plats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.equa_notepad_plats.data.local.entities.*
import com.example.equa_notepad_plats.data.local.dao.*

@Database(
    entities = [BookEntity::class, FormulaEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun formulaDao(): FormulaDao
    abstract fun userDao(): UserDao
}