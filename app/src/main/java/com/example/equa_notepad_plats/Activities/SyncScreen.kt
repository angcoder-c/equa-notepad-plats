package com.example.equa_notepad_plats.Activities

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.equa_notepad_plats.data.DatabaseProvider
import com.example.equa_notepad_plats.data.local.entities.BookEntity
import com.example.equa_notepad_plats.data.local.entities.FormulaEntity
import com.example.equa_notepad_plats.data.remote.SyncService
import com.example.equa_notepad_plats.view_models.SyncViewModel
import com.example.equa_notepad_plats.view_models.BookWithFormulas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)
    val syncService = SyncService()
    val viewModel = remember { SyncViewModel(database, syncService) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val books by viewModel.books.collectAsStateWithLifecycle()
    val selectedBook by viewModel.selectedBook.collectAsStateWithLifecycle()
    val selectedFormulas by viewModel.selectedFormulas.collectAsStateWithLifecycle()

    // Animation states
    val progressAnimation by animateFloatAsState(
        targetValue = if (uiState.isUploading) uiState.uploadProgress else if (uiState.isFullSyncing) uiState.syncProgress else 0f,
        animationSpec = tween(300),
        label = "progress"
    )

    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrEmpty()) {
            // Auto-clear error after 5 seconds
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.syncMessage) {
        if (!uiState.syncMessage.isNullOrEmpty()) {
            // Auto-clear sync message after 3 seconds if not actively syncing
            if (!uiState.isUploading && !uiState.isFullSyncing && !uiState.isDownloading) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearSyncMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sync Center",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Status Card
            StatusCard(
                uiState = uiState,
                progressAnimation = progressAnimation,
                onDismissError = { viewModel.clearError() },
                onDismissMessage = { viewModel.clearSyncMessage() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            QuickActionsSection(
                isLoading = uiState.isLoading || uiState.isUploading || uiState.isFullSyncing || uiState.isDownloading,
                onUploadAll = { viewModel.performFullSync() },
                onDownload = { viewModel.downloadFromRemote() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Book Selection
            BookSelectionSection(
                books = books,
                selectedBook = selectedBook,
                isLoading = uiState.isLoading,
                onBookSelected = { viewModel.selectBook(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Formula Selection (only show if book is selected)
            selectedBook?.let { book ->
                FormulaSelectionSection(
                    book = book,
                    formulas = viewModel.getFormulasForSelectedBook(),
                    selectedFormulas = selectedFormulas,
                    isAllSelected = viewModel.isAllSelected(),
                    selectedCount = viewModel.getSelectedFormulasCount(),
                    isUploading = uiState.isUploading,
                    onToggleFormula = { viewModel.toggleFormulaSelection(it) },
                    onSelectAll = { viewModel.selectAllFormulas() },
                    onDeselectAll = { viewModel.deselectAllFormulas() },
                    onUploadSelected = { viewModel.uploadSelectedFormulas() }
                )
            }

            if (selectedBook == null && !uiState.isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                EmptyStateCard()
            }
        }
    }
}

@Composable
private fun StatusCard(
    uiState: com.example.equa_notepad_plats.view_models.SyncUiState,
    progressAnimation: Float,
    onDismissError: () -> Unit,
    onDismissMessage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                uiState.error != null -> MaterialTheme.colorScheme.errorContainer
                uiState.isUploading || uiState.isFullSyncing -> MaterialTheme.colorScheme.primaryContainer
                uiState.syncMessage != null -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            uiState.error != null -> Icons.Default.Error
                            uiState.isUploading || uiState.isFullSyncing -> Icons.Default.CloudUpload
                            uiState.isDownloading -> Icons.Default.CloudDownload
                            uiState.syncMessage != null -> Icons.Default.CheckCircle
                            else -> Icons.Default.Sync
                        },
                        contentDescription = null,
                        tint = when {
                            uiState.error != null -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = when {
                            uiState.error != null -> "Error"
                            uiState.isUploading -> "Uploading..."
                            uiState.isFullSyncing -> "Full Sync..."
                            uiState.isDownloading -> "Downloading..."
                            uiState.syncMessage != null -> "Success"
                            else -> "Ready"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (uiState.error != null || (!uiState.isUploading && !uiState.isFullSyncing && uiState.syncMessage != null)) {
                    IconButton(
                        onClick = if (uiState.error != null) onDismissError else onDismissMessage,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Progress bar
            if (uiState.isUploading || uiState.isFullSyncing) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressAnimation },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(progressAnimation * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status message
            val message = uiState.error ?: uiState.syncMessage
            if (message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (uiState.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    isLoading: Boolean,
    onUploadAll: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "⚡ Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onUploadAll,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload All", fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onDownload,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download", fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookSelectionSection(
    books: List<BookWithFormulas>,
    selectedBook: BookEntity?,
    isLoading: Boolean,
    onBookSelected: (BookEntity?) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "📋 Select Formulario (Book)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = {
                    if (!isLoading) dropdownExpanded = !dropdownExpanded
                }
            ) {
                OutlinedTextField(
                    value = selectedBook?.name ?: "Select a book...",
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors()
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    if (books.isEmpty() && !isLoading) {
                        DropdownMenuItem(
                            text = { Text("No books available") },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        books.forEach { bookWithFormulas ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = bookWithFormulas.book.name,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${bookWithFormulas.formulas.size} formulas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    onBookSelected(bookWithFormulas.book)
                                    dropdownExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Book,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Loading books...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaSelectionSection(
    book: BookEntity,
    formulas: List<FormulaEntity>,
    selectedFormulas: Set<Int>,
    isAllSelected: Boolean,
    selectedCount: Int,
    isUploading: Boolean,
    onToggleFormula: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onUploadSelected: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with formula count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Functions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "☑️ Formulas in ${book.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "$selectedCount/${formulas.size}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Select All / Deselect All Button
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = if (isAllSelected) onDeselectAll else onSelectAll,
                    enabled = !isUploading && formulas.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        if (isAllSelected) Icons.Default.CheckBoxOutlineBlank else Icons.Default.CheckBox,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isAllSelected) "🔘 Deselect All" else "🔘 Select All",
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = onUploadSelected,
                    enabled = !isUploading && selectedCount > 0,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("⬆️ Upload Selected", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (formulas.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No formulas found in this book",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Formula List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(formulas) { formula ->
                        FormulaItem(
                            formula = formula,
                            isSelected = selectedFormulas.contains(formula.id),
                            isEnabled = !isUploading,
                            onToggle = { onToggleFormula(formula.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaItem(
    formula: FormulaEntity,
    isSelected: Boolean,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = isEnabled) { onToggle() }
                .padding(12.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { if (isEnabled) onToggle() },
                enabled = isEnabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formula.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!formula.description.isNullOrEmpty()) {
                    Text(
                        text = formula.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Sync status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = if (formula.remoteId != null) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (formula.remoteId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (formula.remoteId != null) "Synced" else "Local only",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (formula.remoteId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (formula.isDirty) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Modified",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Icon(
                Icons.Default.CloudSync,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to Sync Center!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select a book from the dropdown above to start syncing your formulas with the cloud.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}