package com.example.equa_notepad_plats.data

import android.content.Context
import androidx.room.Room
import com.example.equa_notepad_plats.data.local.*
import kotlinx.coroutines.flow.Flow

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "deltime_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}