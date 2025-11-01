package com.example.equa_notepad_plats.Activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.equa_notepad_plats.AppNavHost
import com.example.equa_notepad_plats.LoginRoute
import com.example.equa_notepad_plats.PracticeRoute
import com.example.equa_notepad_plats.components.exercise.BookSelector
import com.example.equa_notepad_plats.ui.theme.AppTheme
import com.example.equa_notepad_plats.view_models.PracticeViewModel
import com.example.equa_notepad_plats.view_models.ProfileViewModel
import com.example.equa_notepad_plats.view_models.BookViewModel
import com.example.equa_notepad_plats.components.exercise.ExerciseGeneratorCard
import com.example.equa_notepad_plats.components.exercise.ChatMessageComponent
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.data.repositories.BookRepository
import com.example.equa_notepad_plats.data.repositories.FormulaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    modifier: Modifier = Modifier,
    bookId: String,
    onBackClick: () -> Unit = {},
    viewModel: PracticeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedBookId by viewModel.selectedBookId.collectAsState()

    // header
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Generador de ejercicios",
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
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            BookSelector(
                bookId=bookId,
                repository = BookRepository(DatabaseProvider.getDatabase(LocalContext.current)),
                viewModel = viewModel
            )
            ExerciseGeneratorCard(
                onExerciseGeneratorClick = {
                    viewModel.generateExerciseWithAI(bookId)
                },
                onClearMessagesClick = {
                    viewModel.clearMessages()
                }
            )

            ChatMessageComponent(
                messages = uiState.messages,
                isLoading = uiState.isLoading
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PracticeScreenPreview() {
    AppTheme(darkTheme = false) {
        PracticeScreen(
            bookId = "1",
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PracticeScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        PracticeScreen(
            bookId = "1",
            onBackClick = {}
        )
    }
}