package com.lifeos.modules.lifeos_physicalmedia.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.room.OnConflictStrategy

@Dao
interface PhysicalBookDao {
    @Query("SELECT * FROM physical_books ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PhysicalBookEntity>>

    @Query("SELECT * FROM physical_books WHERE id = :id")
    suspend fun getById(id: Long): PhysicalBookEntity?

    @Insert
    suspend fun insert(entity: PhysicalBookEntity): Long

    @Update
    suspend fun update(entity: PhysicalBookEntity)

    @Query("DELETE FROM physical_books WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface PhysicalMovieDao {
    @Query("SELECT * FROM physical_movies ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PhysicalMovieEntity>>

    @Query("SELECT * FROM physical_movies WHERE id = :id")
    suspend fun getById(id: Long): PhysicalMovieEntity?

    @Insert
    suspend fun insert(entity: PhysicalMovieEntity): Long

    @Update
    suspend fun update(entity: PhysicalMovieEntity)

    @Query("DELETE FROM physical_movies WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface PhysicalGameDao {
    @Query("SELECT * FROM physical_games ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PhysicalGameEntity>>

    @Query("SELECT * FROM physical_games WHERE id = :id")
    suspend fun getById(id: Long): PhysicalGameEntity?

    @Insert
    suspend fun insert(entity: PhysicalGameEntity): Long

    @Update
    suspend fun update(entity: PhysicalGameEntity)

    @Query("DELETE FROM physical_games WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface PhysicalMovieCollectionItemDao {
    @Query("SELECT * FROM physical_movie_collection_items")
    fun getAll(): Flow<List<PhysicalMovieCollectionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PhysicalMovieCollectionItemEntity>)

    @Query("DELETE FROM physical_movie_collection_items WHERE movieId = :movieId")
    suspend fun deleteAllForMovie(movieId: Long)
}

@Dao
interface PhysicalGameCollectionItemDao {
    @Query("SELECT * FROM physical_game_collection_items")
    fun getAll(): Flow<List<PhysicalGameCollectionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PhysicalGameCollectionItemEntity>)

    @Query("DELETE FROM physical_game_collection_items WHERE gameId = :gameId")
    suspend fun deleteAllForGame(gameId: Long)
}
