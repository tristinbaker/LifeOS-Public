package com.lifeos.modules.lifeos_physicalmedia.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_physicalmedia.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PhysicalMediaDiModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhysicalMediaDatabase {
        return Room.databaseBuilder(
            context,
            PhysicalMediaDatabase::class.java,
            "physicalmedia_v1.db"
        ).addMigrations(PM_MIGRATION_1_2, PM_MIGRATION_2_3).build()
    }

    @Provides
    fun providePhysicalBookDao(db: PhysicalMediaDatabase): PhysicalBookDao = db.physicalBookDao()

    @Provides
    fun providePhysicalMovieDao(db: PhysicalMediaDatabase): PhysicalMovieDao = db.physicalMovieDao()

    @Provides
    fun providePhysicalGameDao(db: PhysicalMediaDatabase): PhysicalGameDao = db.physicalGameDao()
}
