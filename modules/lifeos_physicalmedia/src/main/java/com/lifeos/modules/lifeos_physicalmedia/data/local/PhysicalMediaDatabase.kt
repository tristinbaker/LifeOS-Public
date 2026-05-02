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

val PM_MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE physical_movies ADD COLUMN isCollection INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE physical_games ADD COLUMN isCollection INTEGER NOT NULL DEFAULT 0")
        database.execSQL("""CREATE TABLE IF NOT EXISTS physical_movie_collection_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            movieId INTEGER NOT NULL,
            title TEXT NOT NULL,
            FOREIGN KEY(movieId) REFERENCES physical_movies(id) ON DELETE CASCADE)""")
        database.execSQL("CREATE INDEX index_movie_col_movieId ON physical_movie_collection_items(movieId)")
        database.execSQL("""CREATE TABLE IF NOT EXISTS physical_game_collection_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            gameId INTEGER NOT NULL,
            title TEXT NOT NULL,
            FOREIGN KEY(gameId) REFERENCES physical_games(id) ON DELETE CASCADE)""")
        database.execSQL("CREATE INDEX index_game_col_gameId ON physical_game_collection_items(gameId)")
    }
}

@Database(
    entities = [
        PhysicalBookEntity::class,
        PhysicalMovieEntity::class,
        PhysicalMovieCollectionItemEntity::class,
        PhysicalGameEntity::class,
        PhysicalGameCollectionItemEntity::class,
        PhysicalTvSeriesEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(PhysicalMediaConverters::class)
abstract class PhysicalMediaDatabase : RoomDatabase() {
    abstract fun physicalBookDao(): PhysicalBookDao
    abstract fun physicalMovieDao(): PhysicalMovieDao
    abstract fun physicalMovieCollectionItemDao(): PhysicalMovieCollectionItemDao
    abstract fun physicalGameDao(): PhysicalGameDao
    abstract fun physicalGameCollectionItemDao(): PhysicalGameCollectionItemDao
    abstract fun physicalTvSeriesDao(): PhysicalTvSeriesDao
}
