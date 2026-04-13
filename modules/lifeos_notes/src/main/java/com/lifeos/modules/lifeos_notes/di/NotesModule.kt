package com.lifeos.modules.lifeos_notes.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_notes.data.local.NoteDao
import com.lifeos.modules.lifeos_notes.data.local.NotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotesModule {

    @Provides
    @Singleton
    fun provideNotesDatabase(@ApplicationContext context: Context): NotesDatabase {
        // Migrate from the old shared "lifeos_db" filename to a module-specific one
        val oldDb = context.getDatabasePath("lifeos_db")
        val newDb = context.getDatabasePath("lifeos_notes.db")
        if (oldDb.exists() && !newDb.exists()) {
            oldDb.renameTo(newDb)
            File("${oldDb.path}-shm").takeIf { it.exists() }?.renameTo(File("${newDb.path}-shm"))
            File("${oldDb.path}-wal").takeIf { it.exists() }?.renameTo(File("${newDb.path}-wal"))
        }
        return Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            "lifeos_notes.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NotesDatabase): NoteDao {
        return database.noteDao()
    }
}
