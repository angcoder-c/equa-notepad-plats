package com.example.equa_notepad_plats.Activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.equa_notepad_plats.ui.theme.AppTheme
import com.example.equa_notepad_plats.components.HeaderBack
import com.example.equa_notepad_plats.view_models.PracticeViewModel
import com.example.equa_notepad_plats.view_models.ProfileViewModel

class PracticeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bookId = intent.getStringExtra("bookId") ?: "1"
        val formulaId = intent.getStringExtra("formulaId") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val name = intent.getStringExtra("name") ?: ""

        setContent {
            AppTheme(darkTheme = isSystemInDarkTheme()) {
                Scaffold() { innerPadding ->
                    PracticeScreen(
                        bookId = bookId,
                        formulaId = formulaId,
                        email = email,
                        name = name,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeScreen(
    modifier: Modifier = Modifier,
    bookId: String,
    formulaId: String,
    email: String = "",
    name: String = "",
    viewModel: PracticeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderBack("Ejercicio")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Generador de ejercicio IA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // campo de texto
                    OutlinedTextField(
                        value = uiState.exercise,
                        onValueChange = { /* TODO */ },
                        label = {
                            Text("Ejercicio generado")
                                },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                    )

                    // errores
                    if (uiState.error.isNotEmpty()) {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }

                    // boton de generar ejercicio
                    Button(
                        onClick = { viewModel.generateExerciseWithAI(formulaId) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generando...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Generar"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generar con IA")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeScreenPreview() {
    AppTheme(darkTheme = false) {
        PracticeScreen(
            bookId = "1",
            formulaId = "1"
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PracticeScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        PracticeScreen(
            bookId = "1",
            formulaId = "1"
        )
    }
}