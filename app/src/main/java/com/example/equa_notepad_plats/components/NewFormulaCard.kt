import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormulaEdit(modifier: Modifier, isEdit: Boolean) {
    var name by remember { mutableStateOf("") }
    var formula by remember { mutableStateOf("") }
    // to do edit formula
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sección Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Name",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    if (isEdit) {
                        /*Se hace la llamada para obtener el nombre de la formula
                        * */
                    }
                    else{
                        Text("Enter name")
                    } },
                singleLine = true
            )
        }

        // Sección Formula
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Formula",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = formula,
                onValueChange = { formula = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    if (isEdit) {
                        /* Se hace la llamada para obtener la formula */
                    }
                    else{Text("Enter formula")
                    } },
                singleLine = true
            )
        }

        // Botón Submit
        Button(
            onClick = {
                // Aquí iría la lógica para guardar la formula

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Submit",
                fontSize = 18.sp
            )
        }
    }
}

// Preview para ver el diseño en Android Studio
@Preview(showBackground = true)
@Composable
fun FormulaScreenPreview() {
    MaterialTheme {
        FormulaEdit(modifier = Modifier, isEdit = false)
    }
}