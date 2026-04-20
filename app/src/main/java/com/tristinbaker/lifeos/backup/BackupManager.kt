package com.tristinbaker.lifeos.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object BackupManager {

    private val DB_NAMES = listOf(
        "lifeos_journal",
        "lifeos_notes",
        "lifeos_habittracker",
        "lifeos_mealtracker",
        "lifeos_sleeptracker",
        "medialogger",
        "lifeos_sports"
    )

    // Suffixes for WAL-mode SQLite databases
    private val DB_SUFFIXES = listOf("", "-shm", "-wal")

    private const val COVERS_DIR = "media_covers"
    private const val JOURNAL_IMAGES_DIR = "journal_images"
    private const val DATASTORE_DIR = "datastore"
    // DataStore files to include in the backup (by filename without path)
    private val DATASTORE_FILES = listOf("settings.preferences_pb")

    private const val AUTO_BACKUP_WORK_NAME = "lifeos_auto_backup"
    private const val AUTO_BACKUP_FILE_PREFIX = "lifeos_backup_auto"

    /** One-shot backup: writes a ZIP to a URI chosen via CreateDocument. */
    suspend fun backup(context: Context, outputUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val dbDir = context.getDatabasePath("placeholder").parentFile
                ?: error("Cannot locate database directory")

            var fileCount = 0
            val outputStream = context.contentResolver.openOutputStream(outputUri)
                ?: error("Cannot open output URI")

            outputStream.use { raw ->
                ZipOutputStream(BufferedOutputStream(raw)).use { zip ->
                    writeDbFilesToZip(dbDir, zip)
                    writeCoversToZip(context, zip)
                    writeJournalImagesToZip(context, zip)
                    writeDataStoreToZip(context, zip)
                    fileCount = DB_NAMES.sumOf { name ->
                        DB_SUFFIXES.count { suffix ->
                            File(dbDir, "$name.db$suffix").let { it.exists() && it.length() > 0 }
                        }
                    }
                }
            }

            fileCount
        }
    }

    /** Auto-backup: overwrites lifeos_backup_auto.zip in the user-chosen folder. */
    suspend fun backupToFolder(context: Context, folderUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val folder = DocumentFile.fromTreeUri(context, folderUri)
                ?: error("Cannot access backup folder")

            // Delete any previous auto-backup file so we overwrite cleanly
            folder.listFiles()
                .filter { it.name?.startsWith(AUTO_BACKUP_FILE_PREFIX) == true }
                .forEach { it.delete() }

            val backupFile = folder.createFile("application/zip", AUTO_BACKUP_FILE_PREFIX)
                ?: error("Cannot create backup file in folder")

            val dbDir = context.getDatabasePath("placeholder").parentFile
                ?: error("Cannot locate database directory")

            var fileCount = 0
            val outputStream = context.contentResolver.openOutputStream(backupFile.uri)
                ?: error("Cannot open output stream for backup file")

            outputStream.use { raw ->
                ZipOutputStream(BufferedOutputStream(raw)).use { zip ->
                    writeDbFilesToZip(dbDir, zip)
                    writeCoversToZip(context, zip)
                    writeJournalImagesToZip(context, zip)
                    writeDataStoreToZip(context, zip)
                    fileCount = DB_NAMES.sumOf { name ->
                        DB_SUFFIXES.count { suffix ->
                            File(dbDir, "$name.db$suffix").let { it.exists() && it.length() > 0 }
                        }
                    }
                }
            }

            fileCount
        }
    }

    private fun writeDbFilesToZip(dbDir: File, zip: ZipOutputStream) {
        for (name in DB_NAMES) {
            for (suffix in DB_SUFFIXES) {
                val file = File(dbDir, "$name.db$suffix")
                if (file.exists() && file.length() > 0) {
                    zip.putNextEntry(ZipEntry(file.name))
                    file.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
    }

    private fun writeCoversToZip(context: Context, zip: ZipOutputStream) {
        val coversDir = File(context.filesDir, COVERS_DIR)
        if (!coversDir.exists()) return
        coversDir.listFiles()?.forEach { file ->
            if (file.isFile && file.length() > 0) {
                zip.putNextEntry(ZipEntry("$COVERS_DIR/${file.name}"))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    private fun writeJournalImagesToZip(context: Context, zip: ZipOutputStream) {
        val imagesDir = File(context.filesDir, JOURNAL_IMAGES_DIR)
        if (!imagesDir.exists()) return
        imagesDir.listFiles()?.forEach { file ->
            if (file.isFile && file.length() > 0) {
                zip.putNextEntry(ZipEntry("$JOURNAL_IMAGES_DIR/${file.name}"))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    private fun writeDataStoreToZip(context: Context, zip: ZipOutputStream) {
        val dsDir = File(context.filesDir, DATASTORE_DIR)
        if (!dsDir.exists()) return
        DATASTORE_FILES.forEach { fileName ->
            val file = File(dsDir, fileName)
            if (file.exists() && file.length() > 0) {
                zip.putNextEntry(ZipEntry("$DATASTORE_DIR/$fileName"))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    fun scheduleAutoBackup(context: Context) {
        val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AUTO_BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelAutoBackup(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(AUTO_BACKUP_WORK_NAME)
    }
}
