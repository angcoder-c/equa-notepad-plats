package com.example.equa_notepad_plats.Activities

import FractionInputDialog
import MathFormulaEditor
import MathKeyboard
import PowerInputDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.ui.theme.AppTheme
import com.example.equa_notepad_plats.view_models.FormulaViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
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
                onValueChange = {
                    viewModel.updateName(it)
                },
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
            Box(modifier = Modifier.size(500.dp))
            {
                var showKeyboard by remember { mutableStateOf(false) }
                var showFractionDialog by remember { mutableStateOf(false) }
                var showPowerDialog by remember { mutableStateOf(false) }
                var showRootDialog by remember { mutableStateOf(false) }
                var currentInputType by remember { mutableStateOf(InputType.NONE) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Formula display
                    OutlinedTextField(
                        value = uiState.formulaText,
                        onValueChange = { viewModel.updateFormulaText(it) },
                        label = { Text("Formula") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showKeyboard = !showKeyboard },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showKeyboard) "Hide Math Keyboard" else "Show Math Keyboard")
                    }

                    if (showKeyboard) {
                        MathKeyboard(modifier = Modifier.fillMaxWidth().weight(2f),
                            onSymbolClick = { symbol ->
                                when (symbol.inputType) {
                                    InputType.FRACTION -> {
                                        showFractionDialog = true
                                    }
                                    InputType.POWER -> {
                                        currentInputType = InputType.POWER
                                        showPowerDialog = true
                                    }
                                    InputType.SQUARE_ROOT, InputType.NTH_ROOT -> {
                                        currentInputType = symbol.inputType
                                        showRootDialog = true
                                    }
                                    else -> {
                                        viewModel.updateFormulaText(uiState.formulaText + symbol.value)
                                    }
                                }
                            },
                            onDismiss = { showKeyboard = false }
                        )
                    }
                }

                // Dialogs
                if (showFractionDialog) {
                    FractionInputDialog(
                        onConfirm = { num, den ->
                            viewModel.updateFormulaText(uiState.formulaText + "($num/$den)")
                            showFractionDialog = false
                        },
                        onDismiss = { showFractionDialog = false }
                    )
                }

                if (showPowerDialog) {
                    PowerInputDialog(
                        title = "Enter Exponent",
                        label = "Power",
                        onConfirm = { value ->
                            viewModel.updateFormulaText(uiState.formulaText + "^($value)")
                            showPowerDialog = false
                        },
                        onDismiss = { showPowerDialog = false }
                    )
                }

                if (showRootDialog) {
                    PowerInputDialog(
                        title = if (currentInputType == InputType.NTH_ROOT) "Enter Root Index" else "Enter Value",
                        label = if (currentInputType == InputType.NTH_ROOT) "n" else "Value",
                        onConfirm = { value ->
                            val newFormula = if (currentInputType == InputType.NTH_ROOT) "ⁿ√($value)" else "√($value)"
                            viewModel.updateFormulaText(uiState.formulaText + newFormula)
                            showRootDialog = false
                        },
                        onDismiss = { showRootDialog = false }
                    )
                }
            }
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