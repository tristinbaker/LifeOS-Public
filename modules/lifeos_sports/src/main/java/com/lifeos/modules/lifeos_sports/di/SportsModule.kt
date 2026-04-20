package com.lifeos.modules.lifeos_sports.di

import android.content.Context
import androidx.room.Room
import com.lifeos.modules.lifeos_sports.data.local.FavoriteTeamDao
import com.lifeos.modules.lifeos_sports.data.local.SportsCacheDao
import com.lifeos.modules.lifeos_sports.data.local.SportsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SportsModule {

    @Provides
    @Singleton
    fun provideSportsDatabase(@ApplicationContext context: Context): SportsDatabase =
        Room.databaseBuilder(context, SportsDatabase::class.java, "lifeos_sports.db")
            .addMigrations(SportsDatabase.MIGRATION_2_3, SportsDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoriteTeamDao(db: SportsDatabase): FavoriteTeamDao =
        db.favoriteTeamDao()

    @Provides
    @Singleton
    fun provideSportsCacheDao(db: SportsDatabase): SportsCacheDao =
        db.sportsCacheDao()
}
