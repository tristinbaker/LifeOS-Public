package com.lifeos.modules.lifeos_physicalmedia.data.repository

import com.lifeos.modules.lifeos_physicalmedia.data.local.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhysicalMediaRepository @Inject constructor(
    private val bookDao: PhysicalBookDao,
    private val movieDao: PhysicalMovieDao,
    private val movieCollectionItemDao: PhysicalMovieCollectionItemDao,
    private val gameDao: PhysicalGameDao,
    private val gameCollectionItemDao: PhysicalGameCollectionItemDao,
    private val tvSeriesDao: PhysicalTvSeriesDao
) {
    fun getBooks(): Flow<List<PhysicalBookEntity>> = bookDao.getAll()
    fun getMovies(): Flow<List<PhysicalMovieEntity>> = movieDao.getAll()
    fun getMovieCollectionItems(): Flow<List<PhysicalMovieCollectionItemEntity>> = movieCollectionItemDao.getAll()
    fun getGames(): Flow<List<PhysicalGameEntity>> = gameDao.getAll()
    fun getGameCollectionItems(): Flow<List<PhysicalGameCollectionItemEntity>> = gameCollectionItemDao.getAll()
    fun getTvSeries(): Flow<List<PhysicalTvSeriesEntity>> = tvSeriesDao.getAll()

    suspend fun getBookById(id: Long): PhysicalBookEntity? = bookDao.getById(id)
    suspend fun getMovieById(id: Long): PhysicalMovieEntity? = movieDao.getById(id)
    suspend fun getGameById(id: Long): PhysicalGameEntity? = gameDao.getById(id)
    suspend fun getTvSeriesById(id: Long): PhysicalTvSeriesEntity? = tvSeriesDao.getById(id)

    suspend fun insertBook(entity: PhysicalBookEntity): Long = bookDao.insert(entity)
    suspend fun insertMovie(entity: PhysicalMovieEntity): Long = movieDao.insert(entity)
    suspend fun insertGame(entity: PhysicalGameEntity): Long = gameDao.insert(entity)
    suspend fun insertTvSeries(entity: PhysicalTvSeriesEntity): Long = tvSeriesDao.insert(entity)

    suspend fun updateBook(entity: PhysicalBookEntity) = bookDao.update(entity)
    suspend fun updateMovie(entity: PhysicalMovieEntity) = movieDao.update(entity)
    suspend fun updateGame(entity: PhysicalGameEntity) = gameDao.update(entity)
    suspend fun updateTvSeries(entity: PhysicalTvSeriesEntity) = tvSeriesDao.update(entity)

    suspend fun deleteBook(id: Long) = bookDao.deleteById(id)
    suspend fun deleteMovie(id: Long) = movieDao.deleteById(id)
    suspend fun deleteGame(id: Long) = gameDao.deleteById(id)
    suspend fun deleteTvSeries(id: Long) = tvSeriesDao.deleteById(id)

    suspend fun replaceMovieCollectionItems(movieId: Long, titles: List<String>) {
        movieCollectionItemDao.deleteAllForMovie(movieId)
        movieCollectionItemDao.insertAll(titles.map { PhysicalMovieCollectionItemEntity(movieId = movieId, title = it) })
    }

    suspend fun replaceGameCollectionItems(gameId: Long, titles: List<String>) {
        gameCollectionItemDao.deleteAllForGame(gameId)
        gameCollectionItemDao.insertAll(titles.map { PhysicalGameCollectionItemEntity(gameId = gameId, title = it) })
    }
}
