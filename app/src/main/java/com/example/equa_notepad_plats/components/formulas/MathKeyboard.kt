import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class MathSymbol(
    val display: String,
    val value: String,
    val needsInput: Boolean = false,
    val inputType: InputType = InputType.NONE
)

enum class InputType {
    NONE,
    FRACTION,
    SQUARE_ROOT,
    NTH_ROOT,
    POWER,
    SUBSCRIPT,
    INTEGRAL_BOUNDS
}

enum class MathCategory {
    BASIC, OPERATORS, GREEK, ADVANCED, SPECIAL
}

@Composable
fun MathKeyboard(
    onSymbolClick: (MathSymbol) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(MathCategory.BASIC) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        // Header with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Math Keyboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "Close")
            }
        }

        // Category tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategory.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            MathCategory.values().forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { Text(category.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(getSymbolsForCategory(selectedCategory)) { symbol ->
                MathSymbolButton(
                    symbol = symbol,
                    onClick = { onSymbolClick(symbol) }
                )
            }
        }
    }
}

@Composable
fun MathSymbolButton(
    symbol: MathSymbol,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = symbol.display,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FractionInputDialog(
    onConfirm: (numerator: String, denominator: String) -> Unit,
    onDismiss: () -> Unit
) {
    var numerator by remember { mutableStateOf("") }
    var denominator by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Enter Fraction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = numerator,
                    onValueChange = { numerator = it },
                    label = { Text("Numerator") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface)

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = denominator,
                    onValueChange = { denominator = it },
                    label = { Text("Denominator") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(numerator, denominator) },
                        enabled = numerator.isNotBlank() && denominator.isNotBlank()
                    ) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}

@Composable
fun PowerInputDialog(
    title: String,
    label: String,
    onConfirm: (value: String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(value) },
                        enabled = value.isNotBlank()
                    ) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}

fun getSymbolsForCategory(category: MathCategory): List<MathSymbol> {
    return when (category) {
        MathCategory.BASIC -> listOf(
            MathSymbol("0", "0"),
            MathSymbol("1", "1"),
            MathSymbol("2", "2"),
            MathSymbol("3", "3"),
            MathSymbol("4", "4"),
            MathSymbol("5", "5"),
            MathSymbol("6", "6"),
            MathSymbol("7", "7"),
            MathSymbol("8", "8"),
            MathSymbol("9", "9"),
            MathSymbol("+", "+"),
            MathSymbol("-", "-"),
            MathSymbol("×", "×"),
            MathSymbol("÷", "÷"),
            MathSymbol("=", "="),
            MathSymbol("(", "("),
            MathSymbol(")", ")"),
            MathSymbol("[", "["),
            MathSymbol("]", "]"),
            MathSymbol(".", ".")
        )

        MathCategory.OPERATORS -> listOf(
            MathSymbol("±", "±"),
            MathSymbol("∓", "∓"),
            MathSymbol("·", "·"),
            MathSymbol("∘", "∘"),
            MathSymbol("√", "√", true, InputType.SQUARE_ROOT),
            MathSymbol("∛", "∛"),
            MathSymbol("∜", "∜"),
            MathSymbol("ⁿ√", "ⁿ√", true, InputType.NTH_ROOT),
            MathSymbol("xⁿ", "^", true, InputType.POWER),
            MathSymbol("x₂", "₂", true, InputType.SUBSCRIPT),
            MathSymbol("a/b", "/", true, InputType.FRACTION),
            MathSymbol("∞", "∞"),
            MathSymbol("∝", "∝"),
            MathSymbol("∴", "∴"),
            MathSymbol("∵", "∵"),
            MathSymbol("⊥", "⊥"),
            MathSymbol("∥", "∥"),
            MathSymbol("∠", "∠"),
            MathSymbol("°", "°"),
            MathSymbol("%", "%")
        )

        MathCategory.GREEK -> listOf(
            MathSymbol("α", "α"),
            MathSymbol("β", "β"),
            MathSymbol("γ", "γ"),
            MathSymbol("δ", "δ"),
            MathSymbol("ε", "ε"),
            MathSymbol("ζ", "ζ"),
            MathSymbol("η", "η"),
            MathSymbol("θ", "θ"),
            MathSymbol("ι", "ι"),
            MathSymbol("κ", "κ"),
            MathSymbol("λ", "λ"),
            MathSymbol("μ", "μ"),
            MathSymbol("ν", "ν"),
            MathSymbol("ξ", "ξ"),
            MathSymbol("π", "π"),
            MathSymbol("ρ", "ρ"),
            MathSymbol("σ", "σ"),
            MathSymbol("τ", "τ"),
            MathSymbol("φ", "φ"),
            MathSymbol("χ", "χ"),
            MathSymbol("ψ", "ψ"),
            MathSymbol("ω", "ω"),
            MathSymbol("Γ", "Γ"),
            MathSymbol("Δ", "Δ"),
            MathSymbol("Θ", "Θ"),
            MathSymbol("Λ", "Λ"),
            MathSymbol("Σ", "Σ"),
            MathSymbol("Φ", "Φ"),
            MathSymbol("Ψ", "Ψ"),
            MathSymbol("Ω", "Ω")
        )

        MathCategory.ADVANCED -> listOf(
            MathSymbol("∫", "∫", true, InputType.INTEGRAL_BOUNDS),
            MathSymbol("∬", "∬"),
            MathSymbol("∭", "∭"),
            MathSymbol("∮", "∮"),
            MathSymbol("∑", "∑"),
            MathSymbol("∏", "∏"),
            MathSymbol("∂", "∂"),
            MathSymbol("∇", "∇"),
            MathSymbol("lim", "lim"),
            MathSymbol("log", "log"),
            MathSymbol("ln", "ln"),
            MathSymbol("sin", "sin"),
            MathSymbol("cos", "cos"),
            MathSymbol("tan", "tan"),
            MathSymbol("cot", "cot"),
            MathSymbol("sec", "sec"),
            MathSymbol("csc", "csc"),
            MathSymbol("sinh", "sinh"),
            MathSymbol("cosh", "cosh"),
            MathSymbol("tanh", "tanh")
        )

        MathCategory.SPECIAL -> listOf(
            MathSymbol("≠", "≠"),
            MathSymbol("≈", "≈"),
            MathSymbol("≡", "≡"),
            MathSymbol("≤", "≤"),
            MathSymbol("≥", "≥"),
            MathSymbol("<", "<"),
            MathSymbol(">", ">"),
            MathSymbol("≪", "≪"),
            MathSymbol("≫", "≫"),
            MathSymbol("∈", "∈"),
            MathSymbol("∉", "∉"),
            MathSymbol("⊂", "⊂"),
            MathSymbol("⊃", "⊃"),
            MathSymbol("⊆", "⊆"),
            MathSymbol("⊇", "⊇"),
            MathSymbol("∪", "∪"),
            MathSymbol("∩", "∩"),
            MathSymbol("∅", "∅"),
            MathSymbol("ℕ", "ℕ"),
            MathSymbol("ℤ", "ℤ"),
            MathSymbol("ℚ", "ℚ"),
            MathSymbol("ℝ", "ℝ"),
            MathSymbol("ℂ", "ℂ"),
            MathSymbol("∀", "∀"),
            MathSymbol("∃", "∃"),
            MathSymbol("¬", "¬"),
            MathSymbol("∧", "∧"),
            MathSymbol("∨", "∨"),
            MathSymbol("⊕", "⊕"),
            MathSymbol("⊗", "⊗")
        )
    }
}

// Usage Example
@Composable
fun MathFormulaEditor(modifier: Modifier = Modifier) {
    var formula by remember { mutableStateOf("") }
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
            value = formula,
            onValueChange = { formula = it },
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
                            formula += symbol.value
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
                formula += "($num/$den)"
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
                formula += "^($value)"
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
                formula += if (currentInputType == InputType.NTH_ROOT) "ⁿ√($value)" else "√($value)"
                showRootDialog = false
            },
            onDismiss = { showRootDialog = false }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun MathKeyboardPreview() {
    MaterialTheme {
        Surface {

            Box(
                modifier = Modifier.fillMaxSize().padding(vertical = 90.dp),
                contentAlignment = Alignment.CenterEnd
            ) { MathFormulaEditor(modifier = Modifier.padding(20.dp))
            }
        }
    }
}