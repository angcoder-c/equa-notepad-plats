package com.example.equa_notepad_plats.data.repositories

import com.example.equa_notepad_plats.data.local.AppDatabase
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val database: AppDatabase
) {
    fun getAllBooks(): Flow<List<BookEntity>> = database.bookDao().getAllBooks()

    suspend fun getBookById(bookId: Int): BookEntity? = database.bookDao().getBookById(bookId)

    suspend fun insertBook(book: BookEntity): Long = database.bookDao().insertBook(book)

    suspend fun updateBook(book: BookEntity) = database.bookDao().updateBook(book)

    suspend fun deleteBook(book: BookEntity) = database.bookDao().deleteBook(book)
}