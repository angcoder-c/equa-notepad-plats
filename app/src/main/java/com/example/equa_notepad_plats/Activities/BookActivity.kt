package com.example.equa_notepad_plats.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.equa_notepad_plats.AppNavHost
import com.example.equa_notepad_plats.BookRoute
import com.example.equa_notepad_plats.LoginRoute
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import com.example.equa_notepad_plats.data.repositories.BookRepository
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import com.example.equa_notepad_plats.ui.theme.AppTheme
import com.example.equa_notepad_plats.view_models.BookViewModel
import com.example.equa_notepad_plats.components.formulas.EmptyFormulasState
import com.example.equa_notepad_plats.components.formulas.FormulaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    viewModel: BookViewModel = BookViewModel(
        FormulaRepository(
            DatabaseProvider
                .getDatabase(
                    LocalContext.current
                )
        ),
        BookRepository(
            DatabaseProvider
                .getDatabase(
                    LocalContext.current
                )
        ),
        -1
    ),
    onBackClick: () -> Unit,
    onPracticeClick: () -> Unit,
    onNewFormulaClick: () -> Unit,
    onFormulaClick: (Int) -> Unit,
    // User data for sync
    currentUserId: String = "default_user_id",
    isGuest: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show sync messages
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
                            uiState.book?.name ?: "",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        // Sync indicator
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Show sync button only if user is NOT a guest and there are dirty formulas
                    if (!isGuest) {
                        val dirtyFormulasCount = uiState.formulas.count { it.isDirty && it.remoteId == null }
                        if (dirtyFormulasCount > 0) {
                            IconButton(
                                onClick = {
                                    viewModel.syncFormulasToRemote(currentUserId, isGuest)
                                },
                                enabled = !uiState.isSyncing
                            ) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text("$dirtyFormulasCount")
                                }
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = "Sincronizar fórmulas pendientes"
                                )
                            }
                        }
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
                onClick = onNewFormulaClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva fórmula")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Loading state
            if (uiState.isLoading && uiState.formulas.isEmpty()) {
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
            } else if (uiState.formulas.isEmpty()) {
                // Empty state
                EmptyFormulasState(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Formula list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.formulas,
                        key = { formula -> formula.id }
                    ) { formula ->
                        FormulaCard(
                            formula = formula,
                            onClick = { onFormulaClick(formula.id) },
                            onShare = { /* TODO */ },
                            onDelete = { viewModel.deleteFormula(formula) }
                        )
                    }
                }
            }
        }
    }
}

// preview light
@Preview(showBackground = true)
@Composable
fun BookScreenPreview() {
    AppTheme {
        BookScreen(
            onBackClick = {},
            onNewFormulaClick = {},
            onPracticeClick = {},
            onFormulaClick = {}
        )
    }
}

// preview dark
@Preview(showBackground = true)
@Composable
fun BookScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        BookScreen(
            onBackClick = {},
            onNewFormulaClick = {},
            onPracticeClick = {},
            onFormulaClick = {}
        )
    }
}