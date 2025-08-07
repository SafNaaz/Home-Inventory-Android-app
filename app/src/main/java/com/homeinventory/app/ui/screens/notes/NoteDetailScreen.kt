package com.homeinventory.app.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.homeinventory.app.model.Note
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    noteId: String?,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val selectedNote by viewModel.selectedNote.collectAsState()

    // Use local state for text fields to ensure a smooth editing experience
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // This effect triggers loading the note from the database
    LaunchedEffect(noteId) {
        if (noteId != null && noteId != "new") {
            viewModel.getNoteById(noteId)
        } else {
            // It's a new note, so we clear the selection in the VM and reset local state
            viewModel.clearSelectedNote()
            title = ""
            content = ""
        }
    }

    // This effect updates the local state once the note is loaded from the VM
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
    val isSaveEnabled = isTitleValid && isContentValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == "new") "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId != null && noteId != "new") {
                        IconButton(onClick = {
                            viewModel.deleteNoteById(noteId)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
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
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = !isTitleValid && title.isNotEmpty(), // Show error if invalid and not empty
                singleLine = true
            )
            CharacterCount(
                count = title.length,
                limit = titleCharLimit,
                isError = !isTitleValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content TextField
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                isError = !isContentValid
            )
            CharacterCount(
                count = content.length,
                limit = contentCharLimit,
                isError = !isContentValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (noteId == "new") {
                        viewModel.addNewNote(title, content)
                    } else {
                        noteId?.let {
                            viewModel.updateNote(it, title, content)
                        }
                    }
                    navController.popBackStack()
                },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun CharacterCount(count: Int, limit: Int, isError: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "$count / $limit",
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
