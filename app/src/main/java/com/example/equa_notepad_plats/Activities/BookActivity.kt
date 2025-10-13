package com.example.equa_notepad_plats.Activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.equa_notepad_plats.ui.theme.AppTheme
import com.example.equa_notepad_plats.components.FormulaCard
import com.example.equa_notepad_plats.components.HeaderBack
import com.example.equa_notepad_plats.view_models.BookViewModel

class BookActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold() { innerPadding ->
                AppTheme (darkTheme = isSystemInDarkTheme()){
                    BookScreen(
                        bookId = "1",
                        modifier = Modifier.padding(innerPadding),
                    )
                }

            }
        }
    }
}
@Composable
fun BookScreen(
    modifier: Modifier = Modifier,
    bookId: String,
    viewModel: BookViewModel = BookViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    viewModel.loadBook(bookId)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HeaderBack(uiState.bookTitle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn (
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
                items(
                    items = uiState.formulas,
                    key = { it.id }
                ) { formula ->
                    FormulaCard(
                        title = formula.title,
                        formula = formula.formula,
                        onEdit = { viewModel.editFormula(formula.id) },
                        onShare = { viewModel.shareFormula(formula.id) },
                        onDelete = { viewModel.deleteFormula(formula.id) }
                    )
                }
            }
            FloatingActionButton(
                onClick = { viewModel.addNewFormula() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar fórmula")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookScreenPreview() {
    AppTheme {
        BookScreen(
            bookId = "1"
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BookScreenDarkPreview() {
    AppTheme (darkTheme = true) {
        BookScreen(
            bookId = "1"
        )
    }
}