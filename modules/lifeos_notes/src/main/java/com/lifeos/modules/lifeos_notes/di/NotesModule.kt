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
        // Delete the old shared "lifeos_db" file — it was corrupted by a naming conflict
        // with the mealtracker module (both used "lifeos_db"). The schema can't be trusted.
        val oldDb = context.getDatabasePath("lifeos_db")
        if (oldDb.exists()) {
            oldDb.delete()
            File("${oldDb.path}-shm").delete()
            File("${oldDb.path}-wal").delete()
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
