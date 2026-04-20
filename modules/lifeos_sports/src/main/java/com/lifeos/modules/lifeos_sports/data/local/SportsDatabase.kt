package com.lifeos.modules.lifeos_sports.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FavoriteTeamEntity::class, SportsCacheEntity::class],
    version = 4,
    exportSchema = false
)
abstract class SportsDatabase : RoomDatabase() {
    abstract fun favoriteTeamDao(): FavoriteTeamDao
    abstract fun sportsCacheDao(): SportsCacheDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS sports_cache (key TEXT NOT NULL PRIMARY KEY, data TEXT NOT NULL, cachedDate TEXT NOT NULL)"
                )
            }
        }
        // Clears cache table to remove any oversized raw-JSON rows (NCAAF standings > 2MB).
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS sports_cache")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS sports_cache (key TEXT NOT NULL PRIMARY KEY, data TEXT NOT NULL, cachedDate TEXT NOT NULL)"
                )
            }
        }
    }
}
