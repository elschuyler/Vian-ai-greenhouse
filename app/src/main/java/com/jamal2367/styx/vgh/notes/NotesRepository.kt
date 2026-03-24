/*
 * Vian AI Greenhouse - Notes Repository
 * PRD v4.3 Section 7: Notes CRUD operations
 */

package com.jamal2367.styx.vgh.notes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesRepository(private val dao: NoteDao) {

    suspend fun getNotesByWorkspace(workspaceId: String): List<Note> {
        return withContext(Dispatchers.IO) {
            dao.getNotesByWorkspace(workspaceId).map { it.toNote() }
        }
    }

    suspend fun getAllNotes(): List<Note> {
        return withContext(Dispatchers.IO) {
            dao.getAllNotes().map { it.toNote() }
        }
    }

    suspend fun searchNotes(query: String): List<Note> {
        return withContext(Dispatchers.IO) {
            dao.searchNotes(query).map { it.toNote() }
        }
    }

    suspend fun searchNotesInWorkspace(workspaceId: String, query: String): List<Note> {
        return withContext(Dispatchers.IO) {
            dao.searchNotesInWorkspace(workspaceId, query).map { it.toNote() }
        }
    }

    suspend fun getStarredNotes(): List<Note> {
        return withContext(Dispatchers.IO) {
            dao.getStarredNotes().map { it.toNote() }
        }
    }

    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            dao.insertNote(note.toEntity())
        }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            dao.updateNote(note.toEntity())
        }
    }

    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            dao.deleteNote(note.toEntity())
        }
    }

    suspend fun deleteNotesByWorkspace(workspaceId: String) {
        withContext(Dispatchers.IO) {
            dao.deleteNotesByWorkspace(workspaceId)
        }
    }

    suspend fun getNoteCount(): Int {
        return withContext(Dispatchers.IO) {
            dao.getNoteCount()
        }
    }

    private fun NoteEntity.toNote(): Note {
        return Note(
            noteId = noteId,
            workspaceId = workspaceId,
            title = title,
            body = body,
            isStarred = isStarred,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tags = tags.split(",").filter { it.isNotBlank() }
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            noteId = noteId,
            workspaceId = workspaceId,
            title = title,
            body = body,
            isStarred = isStarred,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tags = tags.joinToString(",")
        )
    }
}
