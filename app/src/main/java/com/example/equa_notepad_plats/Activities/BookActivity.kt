package com.example.equa_notepad_plats.Activities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.equa_notepad_plats.components.FormulaCard
import com.example.equa_notepad_plats.view_models.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    viewModel: BookViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.bookTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { searchExpanded = !searchExpanded }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addNewFormula() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Form")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (searchExpanded) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Inlined search text") },
                    singleLine = true
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.formulas) { formula ->
                    FormulaCard(
                        title = formula.title,
                        formula = formula.formula,
                        onEdit = { viewModel.editFormula(formula.id) },
                        onShare = { viewModel.shareFormula(formula.id) },
                        onDelete = { viewModel.deleteFormula(formula.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookScreenPreview() {
    MaterialTheme {
        BookScreen(
            bookId = "1",
            onNavigateBack = {}
        )
    }
}