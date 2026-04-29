package com.lifeos.modules.lifeos_journal.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryDao
import com.lifeos.modules.lifeos_journal.data.local.JournalEntryEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalImageDao
import com.lifeos.modules.lifeos_journal.data.local.JournalImageEntity
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsDao
import com.lifeos.modules.lifeos_journal.data.local.JournalSettingsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_IMAGE_PX = 1920
private const val JPEG_QUALITY = 80

@Singleton
class JournalRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val journalEntryDao: JournalEntryDao,
    private val journalSettingsDao: JournalSettingsDao,
    private val journalImageDao: JournalImageDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getAllEntries(): Flow<List<JournalEntryEntity>> = journalEntryDao.getAllEntries()

    suspend fun getEntryById(entryId: Long): JournalEntryEntity? = journalEntryDao.getEntryById(entryId)

    suspend fun getEntryForDate(date: LocalDate): JournalEntryEntity? =
        journalEntryDao.getEntryForDate(date.format(dateFormatter))

    suspend fun insertEntry(entry: JournalEntryEntity): Long = journalEntryDao.insertEntry(entry)

    suspend fun updateEntry(entry: JournalEntryEntity) = journalEntryDao.updateEntry(entry)

    suspend fun deleteEntry(entryId: Long) = journalEntryDao.deleteEntryById(entryId)

    suspend fun searchEntries(query: String): List<JournalEntryEntity> = journalEntryDao.searchEntries(query)

    fun getSettings(): Flow<JournalSettingsEntity?> = journalSettingsDao.getSettings()

    suspend fun getSettingsOnce(): JournalSettingsEntity? = journalSettingsDao.getSettingsOnce()

    suspend fun updateSettings(settings: JournalSettingsEntity) {
        journalSettingsDao.insertSettings(settings)
    }

    // Images

    fun getImagesForEntry(entryId: Long): Flow<List<JournalImageEntity>> =
        journalImageDao.getImagesForEntry(entryId)

    suspend fun saveImages(entryId: Long, uris: List<Uri>): List<JournalImageEntity> =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "journal_images").also { it.mkdirs() }
            uris.mapIndexed { index, uri ->
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val isGif = mimeType == "image/gif"
                val file = File(dir, "${System.currentTimeMillis()}_$index.${if (isGif) "gif" else "jpg"}")

                if (isGif) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                } else {
                    // Read EXIF orientation before decoding (decodeStream discards it)
                    val orientation = context.contentResolver.openInputStream(uri)?.use {
                        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    } ?: ExifInterface.ORIENTATION_NORMAL

                    val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    if (bitmap != null) {
                        compressToFile(bitmap, file)
                        preserveOrientation(file, orientation)
                    } else {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                }

                val entity = JournalImageEntity(
                    entryId = entryId,
                    localPath = file.absolutePath,
                    createdAt = System.currentTimeMillis()
                )
                val id = journalImageDao.insertImage(entity)
                entity.copy(id = id)
            }
        }

    suspend fun deleteImage(imageId: Long) {
        val path = journalImageDao.getLocalPath(imageId)
        path?.let { File(it).delete() }
        journalImageDao.deleteImage(imageId)
    }

    suspend fun deleteAllImagesForEntry(entryId: Long) {
        val images = journalImageDao.getImagesForEntryOnce(entryId)
        images.forEach { File(it.localPath).delete() }
        journalImageDao.deleteAllImagesForEntry(entryId)
    }

    suspend fun getWeeklyImages(weekStart: LocalDate, weekEnd: LocalDate): List<JournalImageEntity> {
        val entries = journalEntryDao.getEntriesBetween(
            weekStart.format(dateFormatter),
            weekEnd.format(dateFormatter)
        )
        return entries.flatMap { entry -> journalImageDao.getImagesForEntryOnce(entry.id) }
    }

    // One-time background migration: compress all existing journal images in-place.
    suspend fun compressExistingImages() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("journal_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("images_compressed_v3", false)) return@withContext

        val dir = File(context.filesDir, "journal_images")
        if (dir.exists()) {
            dir.listFiles()?.forEach { file ->
                if (file.extension.lowercase() == "gif") return@forEach

                val orientation = try {
                    ExifInterface(file.absolutePath).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                    )
                } catch (_: Exception) { ExifInterface.ORIENTATION_NORMAL }

                val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@forEach
                val tmp = File(file.parent, "${file.nameWithoutExtension}.tmp")
                try {
                    compressToFile(bitmap, tmp)
                    preserveOrientation(tmp, orientation)
                    tmp.renameTo(file)
                } catch (_: Exception) {
                    tmp.delete()
                }
            }
        }

        prefs.edit().putBoolean("images_compressed_v3", true).apply()
    }

    // Write the original EXIF orientation tag back onto the compressed output file so that
    // image viewers (Coil, gallery apps) continue to display it with the correct rotation.
    private fun preserveOrientation(file: File, orientation: Int) {
        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) return
        try {
            ExifInterface(file.absolutePath).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
        } catch (_: Exception) {}
    }

    // Scales to MAX_IMAGE_PX on the long edge, compresses to JPEG, and recycles src.
    private fun compressToFile(src: Bitmap, file: File) {
        val w = src.width
        val h = src.height
        val bitmap = if (w > MAX_IMAGE_PX || h > MAX_IMAGE_PX) {
            val scale = MAX_IMAGE_PX.toFloat() / maxOf(w, h)
            val scaled = Bitmap.createScaledBitmap(src, (w * scale).toInt(), (h * scale).toInt(), true)
            src.recycle()
            scaled
        } else {
            src
        }
        file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out) }
        bitmap.recycle()
    }
}

data class JournalStats(
    val streak: Int = 0,
    val avgWordCount: Int = 0,
    val totalEntries: Int = 0,
    val avgMood: Float = 0f
)
