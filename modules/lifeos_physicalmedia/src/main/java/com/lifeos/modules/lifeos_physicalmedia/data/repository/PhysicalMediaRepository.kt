package com.lifeos.modules.lifeos_physicalmedia.data.repository

import com.lifeos.modules.lifeos_physicalmedia.data.local.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhysicalMediaRepository @Inject constructor(
    private val bookDao: PhysicalBookDao,
    private val movieDao: PhysicalMovieDao,
    private val gameDao: PhysicalGameDao
) {
    fun getBooks(): Flow<List<PhysicalBookEntity>> = bookDao.getAll()
    fun getMovies(): Flow<List<PhysicalMovieEntity>> = movieDao.getAll()
    fun getGames(): Flow<List<PhysicalGameEntity>> = gameDao.getAll()

    suspend fun getBookById(id: Long): PhysicalBookEntity? = bookDao.getById(id)
    suspend fun getMovieById(id: Long): PhysicalMovieEntity? = movieDao.getById(id)
    suspend fun getGameById(id: Long): PhysicalGameEntity? = gameDao.getById(id)

    suspend fun insertBook(entity: PhysicalBookEntity): Long = bookDao.insert(entity)
    suspend fun insertMovie(entity: PhysicalMovieEntity): Long = movieDao.insert(entity)
    suspend fun insertGame(entity: PhysicalGameEntity): Long = gameDao.insert(entity)

    suspend fun updateBook(entity: PhysicalBookEntity) = bookDao.update(entity)
    suspend fun updateMovie(entity: PhysicalMovieEntity) = movieDao.update(entity)
    suspend fun updateGame(entity: PhysicalGameEntity) = gameDao.update(entity)

    suspend fun deleteBook(id: Long) = bookDao.deleteById(id)
    suspend fun deleteMovie(id: Long) = movieDao.deleteById(id)
    suspend fun deleteGame(id: Long) = gameDao.deleteById(id)
}
