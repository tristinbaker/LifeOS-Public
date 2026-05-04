package com.lifeos.modules.lifeos_mealtracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import com.lifeos.modules.lifeos_mealtracker.BuildConfig
import com.lifeos.modules.lifeos_mealtracker.data.local.AppDatabase
import com.lifeos.modules.lifeos_mealtracker.data.local.MealEntryDao
import com.lifeos.modules.lifeos_mealtracker.data.local.SavedMealDao
import com.lifeos.modules.lifeos_mealtracker.data.local.StoredItemDao
import com.lifeos.modules.lifeos_mealtracker.data.local.WeightEntryDao
import com.lifeos.modules.lifeos_mealtracker.data.remote.FatSecretApiClient
import com.lifeos.core.ReportDataProvider
import com.lifeos.modules.lifeos_mealtracker.data.repository.MealRepository
import com.lifeos.modules.lifeos_mealtracker.data.repository.WeightRepository
import com.lifeos.modules.lifeos_mealtracker.report.MealReportProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val oldDb = context.getDatabasePath("lifeos_db")
        if (oldDb.exists()) {
            oldDb.delete()
            File("${oldDb.path}-shm").delete()
            File("${oldDb.path}-wal").delete()
        }
        val migration2to3 = object : Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `lifeos_mealtracker_stored_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `caloriesPerUnit` INTEGER NOT NULL,
                        `proteinPerUnit` INTEGER NOT NULL,
                        `carbsPerUnit` INTEGER NOT NULL,
                        `fatPerUnit` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        val migration3to4 = object : Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `lifeos_mealtracker_stored_items` ADD COLUMN `barcode` TEXT")
            }
        }
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lifeos_mealtracker.db"
        )
            .addMigrations(migration2to3, migration3to4)
            .build()
    }

    @Provides
    @Singleton
    fun provideMealEntryDao(database: AppDatabase): MealEntryDao {
        return database.mealEntryDao()
    }

    @Provides
    @Singleton
    fun provideSavedMealDao(database: AppDatabase): SavedMealDao {
        return database.savedMealDao()
    }

    @Provides
    @Singleton
    fun provideWeightEntryDao(database: AppDatabase): WeightEntryDao {
        return database.weightEntryDao()
    }

    @Provides
    @Singleton
    fun provideStoredItemDao(database: AppDatabase): StoredItemDao {
        return database.storedItemDao()
    }

    @Provides
    @Singleton
    fun provideFatSecretApiClient(): FatSecretApiClient =
        FatSecretApiClient(
            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build(),
            clientId = BuildConfig.FATSECRET_CLIENT_ID,
            clientSecret = BuildConfig.FATSECRET_CLIENT_SECRET
        )

    @Provides
    @IntoSet
    @Singleton
    fun provideMealReportProvider(mealRepo: MealRepository, weightRepo: WeightRepository): ReportDataProvider =
        MealReportProvider(mealRepo, weightRepo)
}
