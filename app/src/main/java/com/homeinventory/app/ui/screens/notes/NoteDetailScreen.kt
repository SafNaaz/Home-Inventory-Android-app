package com.homeinventory.app.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

private class CharacterLimitTransformation(private val limit: Int) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val truncatedText = if (originalText.length > limit) {
            originalText.substring(0, limit)
        } else {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset.coerceIn(0, truncatedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return offset.coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(truncatedText), offsetMapping)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    noteId: String?,
    startInEditMode: Boolean,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val selectedNote by viewModel.selectedNote.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isEditMode by remember { mutableStateOf(startInEditMode || noteId == "new") }
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedNote?.let { note ->
                            viewModel.delete(note)
                            navController.popBackStack()
                        }
                        showDeleteDialog = false
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    LaunchedEffect(noteId) {
        if (noteId != null && noteId != "new") {
            viewModel.getNoteById(noteId)
        } else {
            viewModel.clearSelectedNote()
            title = ""
            content = ""
            isEditMode = true
        }
    }

    LaunchedEffect(selectedNote) {
        selectedNote?.let {
            title = it.title
            content = it.content
        }
    }

    val titleCharLimit = 50
    val contentCharLimit = 500

    val isTitleValid = title.isNotBlank() && title.length <= titleCharLimit
    val isContentValid = content.length <= contentCharLimit
    var isSaveInProgress by remember { mutableStateOf(false) }
    val isSaveEnabled = isTitleValid && isContentValid && !isSaveInProgress

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == "new") "New Note" else if (isEditMode) "Edit Note" else "Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId != null && noteId != "new") {
                        if (isEditMode) {
                            // In edit mode, show a delete button
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        } else {
                            // In read-only mode, show Edit and Delete buttons
                            IconButton(onClick = { isEditMode = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Title TextField
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= titleCharLimit) title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditMode,
                isError = !isTitleValid && title.isNotEmpty(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
            CharacterCount(
                count = title.length,
                limit = titleCharLimit,
                isError = !isTitleValid,
                isPristine = title.isEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content TextField
            OutlinedTextField(
                value = content,
                onValueChange = { if (it.length <= contentCharLimit) content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = isEditMode,
                isError = !isContentValid,
                visualTransformation = CharacterLimitTransformation(contentCharLimit),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
            CharacterCount(
                count = content.length,
                limit = contentCharLimit,
                isError = !isContentValid
            )

            if (isEditMode) {
                Spacer(modifier = Modifier.height(16.dp))
                // Save Button
                Button(
                    onClick = {
                        isSaveInProgress = true
                        val finalTitle = title.take(titleCharLimit)
                        val finalContent = content.take(contentCharLimit)

                        if (noteId == "new") {
                            viewModel.addNewNote(finalTitle, finalContent)
                        } else {
                            noteId?.let {
                                viewModel.updateNote(it, finalTitle, finalContent)
                            }
                        }
                        navController.popBackStack()
                    },
                    enabled = isSaveEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSaveInProgress) "Saving..." else "Save")
                }
            }
        }
    }
}

@Composable
private fun CharacterCount(
    count: Int,
    limit: Int,
    isError: Boolean,
    isPristine: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        val color = when {
            isPristine && isError -> MaterialTheme.colorScheme.onSurfaceVariant
            isError -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Text(
            text = "$count / $limit",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
