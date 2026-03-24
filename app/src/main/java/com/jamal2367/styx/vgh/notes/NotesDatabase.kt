/*
 * Vian AI Greenhouse - Notes Database (Room)
 * PRD v4.3 Section 7: Notes System with search and organization
 */

package com.jamal2367.styx.vgh.notes

import androidx.room.*

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val noteId: String,
    val workspaceId: String,
    val title: String,
    val body: String,
    val isStarred: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: String // Comma-separated
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE workspaceId = :workspaceId ORDER BY isStarred DESC, updatedAt DESC")
    suspend fun getNotesByWorkspace(workspaceId: String): List<NoteEntity>

    @Query("SELECT * FROM notes ORDER BY isStarred DESC, updatedAt DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY isStarred DESC, updatedAt DESC")
    suspend fun searchNotes(query: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE workspaceId = :workspaceId AND (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') ORDER BY isStarred DESC, updatedAt DESC")
    suspend fun searchNotesInWorkspace(workspaceId: String, query: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE isStarred = 1 ORDER BY updatedAt DESC")
    suspend fun getStarredNotes(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE workspaceId = :workspaceId")
    suspend fun deleteNotesByWorkspace(workspaceId: String)

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNoteCount(): Int
}

@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        private var INSTANCE: NotesDatabase? = null

        fun getInstance(context: android.content.Context): NotesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "vgh_notes.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
