package com.example.equa_notepad_plats.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.equa_notepad_plats.data.local.entities.BookEntity

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllBooks(): kotlinx.coroutines.flow.Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: Int): BookEntity?

    @Query("SELECT * FROM books WHERE remoteId = :remoteId")
    suspend fun getBookByRemoteId(remoteId: String): BookEntity?

    @Query("SELECT * FROM books WHERE isDirty = 1 OR remoteId IS NULL")
    suspend fun getDirtyBooks(): List<BookEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)
}