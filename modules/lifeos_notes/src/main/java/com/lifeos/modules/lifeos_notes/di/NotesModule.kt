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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotesModule {

    @Provides
    @Singleton
    fun provideNotesDatabase(@ApplicationContext context: Context): NotesDatabase {
        return Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            "lifeos_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NotesDatabase): NoteDao {
        return database.noteDao()
    }
}
