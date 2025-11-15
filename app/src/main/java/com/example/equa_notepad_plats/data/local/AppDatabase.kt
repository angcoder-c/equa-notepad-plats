package com.example.equa_notepad_plats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.equa_notepad_plats.data.local.entities.*
import com.example.equa_notepad_plats.data.local.dao.*

@Database(
    entities = [BookEntity::class, FormulaEntity::class, UserEntity::class],
    version = 2, // Increment version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun formulaDao(): FormulaDao
    abstract fun userDao(): UserDao
}

// Add this in DatabaseProvider.kt
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns to books table
        database.execSQL(
            "ALTER TABLE books ADD COLUMN remoteId TEXT DEFAULT NULL"
        )
        database.execSQL(
            "ALTER TABLE books ADD COLUMN lastSyncedAt INTEGER DEFAULT NULL"
        )
        database.execSQL(
            "ALTER TABLE books ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0"
        )

        // Add new columns to formulas table
        database.execSQL(
            "ALTER TABLE formulas ADD COLUMN remoteId TEXT DEFAULT NULL"
        )
        database.execSQL(
            "ALTER TABLE formulas ADD COLUMN lastSyncedAt INTEGER DEFAULT NULL"
        )
        database.execSQL(
            "ALTER TABLE formulas ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0"
        )
    }
}