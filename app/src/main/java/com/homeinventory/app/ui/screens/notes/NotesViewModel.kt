package com.homeinventory.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeinventory.app.model.Note
import com.homeinventory.app.model.NoteEntity
import com.homeinventory.app.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    init {
        loadAllNotes()
    }

    private fun loadAllNotes() {
        viewModelScope.launch {
            notesRepository.getAllNotes()
                .distinctUntilChanged()
                .collect { notesList ->
                    _notes.value = notesList
                }
        }
    }

    fun getNoteById(id: String) {
        viewModelScope.launch {
            _selectedNote.value = notesRepository.getNoteById(id)
        }
    }

    fun clearSelectedNote() {
        _selectedNote.value = null
    }

    fun addNewNote(title: String, content: String) {
        viewModelScope.launch {
            if (notesRepository.canAddNote()) {
                val newNote = Note(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    createdDate = Date(),
                    lastModified = Date()
                )
                notesRepository.addNote(newNote)
            }
        }
    }

    fun updateNote(id: String, title: String, content: String) {
        viewModelScope.launch {
            notesRepository.updateNoteContent(id, title, content)
        }
    }
    
    fun deleteNoteById(id: String) {
        viewModelScope.launch {
            val noteToDelete = notesRepository.getNoteById(id)
            noteToDelete?.let {
                notesRepository.deleteNote(it)
                if (_selectedNote.value?.id == id) {
                    _selectedNote.value = null
                }
            }
        }
    }

    fun delete(note: Note) { // Changed parameter type to Note
        viewModelScope.launch {
            notesRepository.deleteNote(note)
        }
    }
}
