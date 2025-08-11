package com.homeinventory.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.homeinventory.app.data.model.Note
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    startInEditMode: Boolean,
    onNavigateBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val note = if (noteId == "new") null else notes.find { it.id == noteId }
    
    var isEditMode by remember { mutableStateOf(startInEditMode || noteId == "new") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showDeleteAlert by remember { mutableStateOf(false) }
    
    // Character limits like iOS app
    val titleCharLimit = 50
    val contentCharLimit = 500
    
    // Initialize values when note is loaded
    LaunchedEffect(note) {
        if (note != null) {
            title = note.title
            content = note.content
        } else if (noteId == "new") {
            title = ""
            content = ""
            isEditMode = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when {
                            noteId == "new" -> "New Note"
                            isEditMode -> "Edit Note"
                            else -> "View Note"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId != "new" && note != null) {
                        if (isEditMode) {
                            // In edit mode, show delete button
                            IconButton(onClick = { showDeleteAlert = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        } else {
                            // In view mode, show edit and delete buttons
                            IconButton(onClick = { isEditMode = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteAlert = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isEditMode) {
                // Edit Mode
                OutlinedTextField(
                    value = title,
                    onValueChange = { newTitle -> 
                        if (newTitle.length <= titleCharLimit) {
                            title = newTitle
                        }
                    },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter note title...") },
                    supportingText = { 
                        Text(
                            text = "${title.length}/$titleCharLimit",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (title.length > titleCharLimit * 0.9) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    isError = title.length > titleCharLimit
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { newContent -> 
                        if (newContent.length <= contentCharLimit) {
                            content = newContent
                        }
                    },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("Start writing your note...") },
                    minLines = 8,
                    supportingText = { 
                        Text(
                            text = "${content.length}/$contentCharLimit",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (content.length > contentCharLimit * 0.9) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    isError = content.length > contentCharLimit
                )
                
                // Save Button
                Button(
                    onClick = {
                        // Trim content to limits just in case
                        val finalTitle = title.take(titleCharLimit)
                        val finalContent = content.take(contentCharLimit)
                        
                        if (noteId == "new") {
                            viewModel.addNote(finalTitle, finalContent)
                        } else if (note != null) {
                            viewModel.updateNote(note, finalTitle, finalContent)
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (title.isNotBlank() || content.isNotBlank()) && 
                             title.length <= titleCharLimit && 
                             content.length <= contentCharLimit
                ) {
                    Text("Save")
                }
            } else {
                // View Mode (like iOS)
                if (note != null) {
                    // Title Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TITLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = note.title.ifEmpty { "Untitled Note" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Divider()
                    
                    // Content Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CONTENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (note.content.isNotEmpty()) {
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Text(
                                text = "No content",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                    
                    Divider()
                    
                    // Metadata Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LAST MODIFIED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDate(note.lastModified),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation alert
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showDeleteAlert = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (note != null) {
                            viewModel.deleteNote(note)
                        }
                        showDeleteAlert = false
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlert = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDate(lastModified: Long): String {
    val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(Date(lastModified))
}
