package com.example.equa_notepad_plats.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.ui.theme.AppTheme
import com.example.equa_notepad_plats.components.BookCard
import com.example.equa_notepad_plats.components.Header
import com.example.equa_notepad_plats.view_models.HomeViewModel

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold() { innerPadding ->
                AppTheme (darkTheme = isSystemInDarkTheme()) {
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onBookClick = { }
                    )
                }

            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onBookClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        Header("Formularios")
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(uiState.books) { book ->
                BookCard(
                    title = book.title,
                    description = book.description,
                    onClick = {
                        onBookClick(book.id)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AppTheme (darkTheme = false) {
        HomeScreen(
            onBookClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenDarkPreview() {
    AppTheme (darkTheme = true) {
        HomeScreen(
            onBookClick = {}
        )
    }
}