package com.example.equa_notepad_plats.Activities

import FormulaEdit
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import com.example.equa_notepad_plats.components.BookEdit
import com.example.equa_notepad_plats.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFormula(modifier: Modifier, onSubmited: ()->Unit){
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Text("New Formula") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
            )
        }
    ) { paddingValues ->
        Row(modifier.fillMaxSize().padding(paddingValues).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            FormulaEdit(modifier, isEdit = false)

        }
    }
}
@Preview
@Composable
fun NewFormulaPreview(){
    NewFormula(modifier = Modifier, onSubmited = {})
}

@Preview
@Composable
fun NewFormulaDarkPreview(){
    AppTheme(darkTheme = true) {
        NewFormula(modifier = Modifier, onSubmited = {})
    }
}