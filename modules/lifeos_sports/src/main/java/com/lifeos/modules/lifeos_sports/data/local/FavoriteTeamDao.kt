package com.lifeos.modules.lifeos_sports.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTeamDao {
    @Query("SELECT * FROM sports_favorite_teams")
    fun getAll(): Flow<List<FavoriteTeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: FavoriteTeamEntity)

    @Query("DELETE FROM sports_favorite_teams WHERE compositeId = :compositeId")
    suspend fun delete(compositeId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM sports_favorite_teams WHERE compositeId = :compositeId)")
    suspend fun isFavorite(compositeId: String): Boolean
}
