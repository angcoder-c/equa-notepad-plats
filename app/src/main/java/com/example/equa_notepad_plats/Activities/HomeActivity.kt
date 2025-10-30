package com.example.equa_notepad_plats.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.equa_notepad_plats.AppNavHost
import com.example.equa_notepad_plats.HomeRoute
import com.example.equa_notepad_plats.LoginRoute
import com.example.equa_notepad_plats.data.repositories.BookRepository
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.ui.theme.AppTheme
import com.example.equa_notepad_plats.view_models.HomeViewModel
import com.example.equa_notepad_plats.components.books.NewBookDialog
import com.example.equa_notepad_plats.components.books.EmptyBooksState
import com.example.equa_notepad_plats.components.books.BookCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = HomeViewModel(
        BookRepository (
            DatabaseProvider
                .getDatabase(LocalContext.current)
        )
    ),
    onBookClick: (Int) -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember {
        mutableStateOf(false)
    }
    // header
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Formularios",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo libro")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // cargando..
            if (uiState.isLoading && uiState.books.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // sin formularios
            else if (uiState.books.isEmpty()) {
                EmptyBooksState(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.books) { book ->
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book.id) },
                            onDelete = { viewModel.deleteBook(book) }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            NewBookDialog(
                onDismiss = {
                    showDialog = false
                            },
                onConfirm = { name, description ->
                    viewModel.createBook(name, description)
                    showDialog = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewLight() {
    AppTheme(darkTheme = false) {
        Surface {
            HomeScreen(
                onBookClick = {},
                onProfileClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        Surface {
            HomeScreen(
                onBookClick = {},
                onProfileClick = {}
            )
        }
    }
}