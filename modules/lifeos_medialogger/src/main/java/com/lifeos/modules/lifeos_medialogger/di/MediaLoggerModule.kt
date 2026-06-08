package com.lifeos.modules.lifeos_medialogger.di

import android.content.Context
import androidx.room.Room
import com.lifeos.core.InsightProvider
import com.lifeos.modules.lifeos_medialogger.data.local.*
import com.lifeos.modules.lifeos_medialogger.report.MediaRecommendationInsightProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaLoggerModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MediaLoggerDatabase {
        return Room.databaseBuilder(
            context,
            MediaLoggerDatabase::class.java,
            "medialogger.db"
        ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7).build()
    }

    @Provides
    fun provideMediaItemDao(database: MediaLoggerDatabase): MediaItemDao = database.mediaItemDao()

    @Provides
    fun provideMangaSeriesDao(database: MediaLoggerDatabase): MangaSeriesDao = database.mangaSeriesDao()

    @Provides
    fun provideMangaVolumeDao(database: MediaLoggerDatabase): MangaVolumeDao = database.mangaVolumeDao()

    @Provides
    @IntoSet
    @Singleton
    fun provideMediaRecommendationInsightProvider(mediaItemDao: MediaItemDao): InsightProvider =
        MediaRecommendationInsightProvider(mediaItemDao)
}
