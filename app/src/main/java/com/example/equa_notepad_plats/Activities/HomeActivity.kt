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
    onProfileClick: () -> Unit,
    // Datos del usuario actual (debes obtenerlos de tu sistema de autenticación)
    currentUserId: String = "default_user_id", // TODO: Obtener del AuthManager o SharedPreferences
    currentUserName: String = "Usuario", // TODO: Obtener del perfil del usuario
    currentUserEmail: String = "usuario@ejemplo.com", // TODO: Obtener del perfil
    currentUserPhotoUrl: String? = null,
    isGuest: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // Mostrar snackbar para mensajes de sincronización
    val snackbarHostState = remember { SnackbarHostState() }

    // Observar cambios en syncMessage y error
    LaunchedEffect(uiState.syncMessage, uiState.error) {
        uiState.syncMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSyncStatus()
        }
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearSyncStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Formularios",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        // Indicador de sincronización
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // Botón para sincronizar libros pendientes
                    val dirtyBooksCount = uiState.books.count { it.isDirty && it.remoteId == null }
                    if (dirtyBooksCount > 0) {
                        IconButton(
                            onClick = {
                                viewModel.syncBooksToRemote(
                                    userId = currentUserId
                                )
                            },
                            enabled = !uiState.isSyncing
                        ) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text("$dirtyBooksCount")
                            }
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = "Sincronizar libros pendientes"
                            )
                        }
                    }

                    // Botón de perfil
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Perfil"
                        )
                    }
                }
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
            // Cargando...
            if (uiState.isLoading && uiState.books.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    if (uiState.syncMessage != null) {
                        Text(
                            text = uiState.syncMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Sin formularios
            else if (uiState.books.isEmpty()) {
                EmptyBooksState(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Lista de formularios
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.books,
                        key = { book -> book.id }
                    ) { book ->
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book.id) },
                            onDelete = { viewModel.deleteBook(book) }
                        )
                    }
                }
            }
        }

        // Dialog para crear nuevo libro
        if (showDialog) {
            NewBookDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, description ->
                    // Crear libro con sincronización automática
                    viewModel.createBookAndSync(
                        name = name,
                        description = description,
                        imageUri = null,
                        userId = currentUserId,
                        userName = currentUserName,
                        userEmail = currentUserEmail,
                        userPhotoUrl = currentUserPhotoUrl,
                        isGuest = isGuest
                    )
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
                onProfileClick = {},
                currentUserId = "preview_user",
                currentUserName = "Usuario Preview",
                currentUserEmail = "preview@ejemplo.com"
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
                onProfileClick = {},
                currentUserId = "preview_user",
                currentUserName = "Usuario Preview",
                currentUserEmail = "preview@ejemplo.com"
            )
        }
    }
}