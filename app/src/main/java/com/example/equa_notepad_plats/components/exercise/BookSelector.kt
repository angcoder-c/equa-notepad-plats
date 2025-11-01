package com.example.equa_notepad_plats.components.exercise

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.equa_notepad_plats.data.repositories.BookRepository
import com.example.equa_notepad_plats.view_models.PracticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSelector(
    bookId: String,
    repository: BookRepository,
    viewModel: PracticeViewModel = viewModel()
) {
    val booksFlow = remember {
        repository.getAllBooks()
    }
    val books by booksFlow.collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    val selectedBookId by viewModel.selectedBookId.collectAsState()

    val selectedBook = books.find {
        it.id == selectedBookId
    }

    LaunchedEffect(books) {
        if (books.isNotEmpty() && selectedBookId == null && bookId.isNotEmpty()) {
            val initialBook = books.find {
                it.id.toString() == bookId
            }
            if (initialBook != null) {
                Log.d("BookSelector", "Setting initial book: ${initialBook.name}")
                viewModel.setBookId(initialBook.id, initialBook)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Formulario para practicar:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedBook?.name ?: "Selecciona un formulario",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                label = { Text("Formulario") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                books.forEach { book ->
                    DropdownMenuItem(
                        text = {
                            Text(book.name)
                        },
                        onClick = {
                            viewModel.setBookId(book.id, book)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}