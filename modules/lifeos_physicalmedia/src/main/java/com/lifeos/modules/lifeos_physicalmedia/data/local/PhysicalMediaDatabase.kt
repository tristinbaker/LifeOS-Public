package com.lifeos.modules.lifeos_physicalmedia.data.local

import androidx.room.*

class PhysicalMediaConverters {
    @TypeConverter fun bookFormatToString(v: BookFormat): String = v.name
    @TypeConverter fun stringToBookFormat(v: String): BookFormat = BookFormat.valueOf(v)
    @TypeConverter fun movieFormatToString(v: MovieFormat): String = v.name
    @TypeConverter fun stringToMovieFormat(v: String): MovieFormat = MovieFormat.valueOf(v)
    @TypeConverter fun gameSystemToString(v: GameSystem): String = v.name
    @TypeConverter fun stringToGameSystem(v: String): GameSystem = GameSystem.valueOf(v)
}

val PM_MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE physical_books ADD COLUMN seriesName TEXT")
        database.execSQL("ALTER TABLE physical_books ADD COLUMN seriesNumber REAL")
    }
}

val PM_MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE physical_movies ADD COLUMN slipcover INTEGER NOT NULL DEFAULT 0")
    }
}

val PM_MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS physical_tv_series (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                format TEXT NOT NULL,
                completeSeries INTEGER NOT NULL DEFAULT 0,
                seriesName TEXT,
                seriesNumber REAL,
                coverUrl TEXT,
                coverLocalPath TEXT,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

val PM_MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE physical_movies ADD COLUMN boutiqueLabel TEXT")
        database.execSQL("ALTER TABLE physical_movies ADD COLUMN catalogNumber TEXT")
    }
}

@Database(
    entities = [PhysicalBookEntity::class, PhysicalMovieEntity::class, PhysicalGameEntity::class, PhysicalTvSeriesEntity::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(PhysicalMediaConverters::class)
abstract class PhysicalMediaDatabase : RoomDatabase() {
    abstract fun physicalBookDao(): PhysicalBookDao
    abstract fun physicalMovieDao(): PhysicalMovieDao
    abstract fun physicalGameDao(): PhysicalGameDao
    abstract fun physicalTvSeriesDao(): PhysicalTvSeriesDao
}
