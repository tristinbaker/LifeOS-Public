package com.lifeos.modules.lifeos_medialogger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Recreate media_items with rating as REAL
        database.execSQL("CREATE TABLE media_items_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, coverUrl TEXT, coverLocalPath TEXT, rating REAL, dateCompleted INTEGER, notes TEXT, type TEXT NOT NULL, platform TEXT, author TEXT, createdAt INTEGER NOT NULL DEFAULT 0)")
        database.execSQL("INSERT INTO media_items_new SELECT id, title, coverUrl, coverLocalPath, CAST(rating AS REAL), dateCompleted, notes, type, platform, author, createdAt FROM media_items")
        database.execSQL("DROP TABLE media_items")
        database.execSQL("ALTER TABLE media_items_new RENAME TO media_items")

        // Add author to manga_series
        database.execSQL("ALTER TABLE manga_series ADD COLUMN author TEXT")

        // Recreate manga_volumes with rating as REAL + new cover/notes columns
        database.execSQL("CREATE TABLE manga_volumes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, seriesId INTEGER NOT NULL, volumeNumber INTEGER NOT NULL, rating REAL, dateCompleted INTEGER, coverUrl TEXT, coverLocalPath TEXT, notes TEXT, createdAt INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(seriesId) REFERENCES manga_series(id) ON DELETE CASCADE)")
        database.execSQL("INSERT INTO manga_volumes_new (id, seriesId, volumeNumber, rating, dateCompleted, createdAt) SELECT id, seriesId, volumeNumber, CAST(rating AS REAL), dateCompleted, createdAt FROM manga_volumes")
        database.execSQL("DROP TABLE manga_volumes")
        database.execSQL("ALTER TABLE manga_volumes_new RENAME TO manga_volumes")
        database.execSQL("CREATE INDEX index_manga_volumes_seriesId ON manga_volumes(seriesId)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE manga_series ADD COLUMN dateCompleted INTEGER")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE media_items ADD COLUMN isRewatch INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE media_items ADD COLUMN hasPlatinum INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE media_items ADD COLUMN has100Percent INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        MediaItemEntity::class,
        MangaSeriesEntity::class,
        MangaVolumeEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MediaLoggerDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun mangaSeriesDao(): MangaSeriesDao
    abstract fun mangaVolumeDao(): MangaVolumeDao
}
