package com.example.equa_notepad_plats.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.ui.theme.AppTheme
import com.example.equa_notepad_plats.view_models.FormulaViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.equa_notepad_plats.AppNavHost
import com.example.equa_notepad_plats.FormulaDetailRoute
import com.example.equa_notepad_plats.LoginRoute

class FormulaActivity : ComponentActivity() {
    private lateinit var viewModel: FormulaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val bookId = intent.getIntExtra("bookId", -1)
        val formulaId = intent.getIntExtra("formulaId", -1)

        val database = DatabaseProvider.getDatabase(applicationContext)
        val repository = FormulaRepository(database)
        viewModel = FormulaViewModel(
            repository,
            bookId,
            if (formulaId != -1) formulaId else null
        )

        setContent {
            AppTheme (darkTheme = isSystemInDarkTheme()){
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavHost(
                        navController = navController,
                        startDestination = FormulaDetailRoute
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaScreen(
    viewModel: FormulaViewModel = FormulaViewModel(
        FormulaRepository(
            DatabaseProvider
                .getDatabase(
                    LocalContext.current
                )
        ),
        -1,
        null
    ),
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nueva formula",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // titulo de la formula
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = {
                    Text("Nombre")
                        },
                placeholder = {
                    Text("Ingresar nombre")
                              },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // text field de la formula TODO: escribir en latex
            OutlinedTextField(
                value = uiState.formulaText,
                onValueChange = { viewModel.updateFormulaText(it) },
                label = {
                    Text("Formula")
                        },
                placeholder = {
                    Text("Ingresar formula")
                              },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )

            // descripcion
            OutlinedTextField(
                value = uiState.description,
                onValueChange = {
                    viewModel.updateDescription(it)
                                },
                label = {
                    Text("Descripcion")
                        },
                placeholder = {
                    Text("Ingresar descripcion")
                              },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )

            // mensaje de error
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // create button
            Button(
                onClick = { viewModel.saveFormula() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        "Crear",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// preview ligth
@Preview(showBackground = true)
@Composable
fun FormulaScreenPreview () {
    AppTheme {
        FormulaScreen(
            onBackClick = {},
            onSaveSuccess = {}
        )
    }
}

// preview dark
@Preview
@Composable
fun FormulaScreenPreviewDark () {
    AppTheme(darkTheme = true) {
        FormulaScreen(
            onBackClick = {},
            onSaveSuccess = {}
        )
    }
}