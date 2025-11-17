package com.example.equa_notepad_plats.Activities

import FractionInputDialog
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import com.example.equa_notepad_plats.data.repositories.FormulaRepository
import com.example.equa_notepad_plats.data.repositories.UserRepository
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.ui.theme.AppTheme
import com.example.equa_notepad_plats.view_models.FormulaViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.equa_notepad_plats.AppNavHost
import com.example.equa_notepad_plats.FormulaDetailRoute
import com.example.equa_notepad_plats.LoginRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaScreen(
    viewModel: FormulaViewModel = FormulaViewModel(
        repository = FormulaRepository(
            DatabaseProvider.getDatabase(LocalContext.current)
        ),
        bookId = -1,
        formulaId = null
    ),
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Determine if we're editing or creating
    val isEditing = viewModel.formulaId != null
    val screenTitle = if (isEditing) "Editar fórmula" else "Nueva fórmula"
    val saveButtonText = if (isEditing) "Actualizar" else "Crear"

    // Show success feedback
    LaunchedEffect(uiState.error, uiState.isSaved) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }

        if (uiState.isSaved) {
            val successMessage = if (isEditing) "¡Fórmula actualizada exitosamente!" else "¡Fórmula creada exitosamente!"
            snackbarHostState.showSnackbar(
                message = successMessage,
                duration = SnackbarDuration.Short
            )
            // Navigate back after showing success message
            kotlinx.coroutines.delay(1000)
            onSaveSuccess()
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
                            screenTitle,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        // Loading indicator
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        // Success indicator
                        if (uiState.isSaved && !uiState.isLoading) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Guardado",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        enabled = !uiState.isLoading
                    ) {
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
            // Formula name
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
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            )

            // Formula editor section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
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
                    // Section header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Editor de Fórmulas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Character count
                        Text(
                            text = "${uiState.formulaText.length} caracteres",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Formula display
                    OutlinedTextField(
                        value = uiState.formulaText,
                        onValueChange = { viewModel.updateFormulaText(it) },
                        label = { Text("Fórmula matemática") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        enabled = !uiState.isLoading,
                        placeholder = {
                            Text(
                                "Escribe tu fórmula aquí...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Keyboard toggle button
                    Button(
                        onClick = { showKeyboard = !showKeyboard },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showKeyboard) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (showKeyboard) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showKeyboard) "Ocultar teclado matemático" else "Mostrar teclado matemático")
                    }

                    // Math Keyboard
                    if (showKeyboard) {
                        MathKeyboard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.5f),
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
                        title = "Ingresar exponente",
                        label = "Potencia",
                        onConfirm = { value ->
                            viewModel.updateFormulaText(uiState.formulaText + "^($value)")
                            showPowerDialog = false
                        },
                        onDismiss = { showPowerDialog = false }
                    )
                }

                if (showRootDialog) {
                    PowerInputDialog(
                        title = if (currentInputType == InputType.NTH_ROOT) "Ingresar índice de raíz" else "Ingresar valor",
                        label = if (currentInputType == InputType.NTH_ROOT) "n" else "Valor",
                        onConfirm = { value ->
                            val newFormula = if (currentInputType == InputType.NTH_ROOT) "ⁿ√($value)" else "√($value)"
                            viewModel.updateFormulaText(uiState.formulaText + newFormula)
                            showRootDialog = false
                        },
                        onDismiss = { showRootDialog = false }
                    )
                }
            }

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = {
                    viewModel.updateDescription(it)
                },
                label = {
                    Text("Descripción")
                },
                placeholder = {
                    Text("Ingresar descripción (opcional)")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel button
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancelar")
                }

                // Save button
                Button(
                    onClick = {
                        viewModel.saveFormula()
                    },
                    modifier = Modifier.weight(2f),
                    enabled = !uiState.isLoading &&
                            uiState.name.isNotBlank() && uiState.formulaText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            saveButtonText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Help text
            if (uiState.name.isBlank() || uiState.formulaText.isBlank()) {
                Text(
                    text = "💡 Completa el nombre y la fórmula para habilitar el guardado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Preview light
@Preview(showBackground = true)
@Composable
fun FormulaScreenPreview() {
    AppTheme {
        FormulaScreen(
            onBackClick = {},
            onSaveSuccess = {}
        )
    }
}

// Preview dark
@Preview
@Composable
fun FormulaScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        FormulaScreen(
            onBackClick = {},
            onSaveSuccess = {}
        )
    }
}

// Preview for edit mode
@Preview(showBackground = true)
@Composable
fun FormulaScreenEditPreview() {
    AppTheme {
        FormulaScreen(
            viewModel = FormulaViewModel(
                repository = FormulaRepository(
                    DatabaseProvider.getDatabase(LocalContext.current)
                ),
                bookId = -1,
                formulaId = 1 // Simulate edit mode
            ),
            onBackClick = {},
            onSaveSuccess = {}
        )
    }
}