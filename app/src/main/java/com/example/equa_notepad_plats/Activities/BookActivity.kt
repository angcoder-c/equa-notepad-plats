package com.example.equa_notepad_plats.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

class BookActivity : ComponentActivity() {
    private lateinit var viewModel: BookViewModel
    private var bookId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bookId = intent.getIntExtra("bookId", -1)

        val database = DatabaseProvider.getDatabase(applicationContext)
        val repository = FormulaRepository(database)
        val bookRepository = BookRepository(database)
        viewModel = BookViewModel(repository, bookRepository, bookId)

        setContent {
            AppTheme (darkTheme = isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavHost(
                        navController = navController,
                        startDestination = BookRoute
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    viewModel: BookViewModel = BookViewModel(
        FormulaRepository (
            DatabaseProvider
                .getDatabase(
                    LocalContext.current
                )
        ),
        BookRepository (
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
    onFormulaClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // header
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.book?.name ?: "Formulario 1",
                        style = MaterialTheme.typography.headlineSmall
                    )
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
                    IconButton(onClick = onPracticeClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
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
            // cargando...
            if (uiState.isLoading && uiState.formulas.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.formulas.isEmpty()) {
                // sin formulas
                EmptyFormulasState(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // lista de formulas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.formulas) { formula ->
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

// preview ligth
@Preview(showBackground = true)
@Composable
fun BookScreenPreview (){
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
fun BookScreenDarkPreview (){
    AppTheme (darkTheme = true) {
        BookScreen(
            onBackClick = {},
            onNewFormulaClick = {},
            onPracticeClick = {},
            onFormulaClick = {}
        )
    }
}