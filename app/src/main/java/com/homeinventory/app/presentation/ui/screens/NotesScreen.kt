package com.homeinventory.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.homeinventory.app.data.model.Note
import com.homeinventory.app.presentation.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onNavigateToNoteDetail: (String) -> Unit = {},
    onNavigateToNewNote: () -> Unit = {}
) {
    val notes by viewModel.notes.collectAsState()
    var showingLimitAlert by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Notes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { 
                        if (notes.size >= 6) {
                            // Show error only when + is clicked at limit
                            showingLimitAlert = true
                        } else {
                            // Navigate directly to new note editor without creating empty note
                            onNavigateToNewNote()
                        }
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Note")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (notes.isEmpty()) {
                // Empty state
                EmptyNotesView(
                    onCreateFirstNote = {
                        onNavigateToNewNote()
                    }
                )
            } else {
                // Notes grid
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(notes) { index, note ->
                        NoteCard(
                            note = note,
                            noteNumber = index + 1,
                            onView = { onNavigateToNoteDetail(note.id) },
                            onEdit = { onNavigateToNoteDetail("${note.id}?edit=true") },
                            onDelete = { viewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }
    }
    
    // Limit Alert Dialog
    if (showingLimitAlert) {
        AlertDialog(
            onDismissRequest = { showingLimitAlert = false },
            title = { Text("Note Limit Reached") },
            text = { Text("You can only have up to 6 notes. Please delete one and try again.") },
            confirmButton = {
                TextButton(onClick = { showingLimitAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun EmptyNotesView(
    onCreateFirstNote: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Filled.Note,
            contentDescription = "No notes",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "No Notes Yet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Tap the + button to create your first quick note. You can have up to 6 notes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Button(
            onClick = onCreateFirstNote
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create First Note")
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    noteNumber: Int,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    var showDeleteAlert by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Fixed height for consistent sizing
            .clickable { onView() }, // Tap to view like iOS
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween // Changed to space between for fixed height
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
            ) {
                // Header with number and date (like iOS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp) // Slightly smaller
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = noteNumber.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = formatDate(note.lastModified),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Title
                Text(
                    text = note.title.ifEmpty { "Untitled Note" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (note.title.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1 // Limit to 1 line for consistency
                )
                
                // Content preview
                if (note.content.isNotEmpty()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall, // Smaller text
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3 // Limit lines for fixed height
                    )
                } else {
                    Text(
                        text = "No content",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
            // Action buttons (like iOS) - moved to bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(14.dp) // Smaller icon
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                IconButton(
                    onClick = { showDeleteAlert = true },
                    modifier = Modifier.size(28.dp) // Smaller button
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(14.dp), // Smaller icon
                        tint = Color.Red
                    )
                }
            }
        }
    }
    
    // Delete confirmation alert
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showDeleteAlert = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteAlert = false
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
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    val now = Date()
    val noteDate = Date(lastModified)
    
    return when {
        calendar.apply { time = noteDate }.get(Calendar.DAY_OF_YEAR) == 
        calendar.apply { time = now }.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(noteDate)
        }
        calendar.apply { time = noteDate }.get(Calendar.WEEK_OF_YEAR) == 
        calendar.apply { time = now }.get(Calendar.WEEK_OF_YEAR) -> {
            SimpleDateFormat("E", Locale.getDefault()).format(noteDate)
        }
        else -> formatter.format(noteDate)
    }
}
