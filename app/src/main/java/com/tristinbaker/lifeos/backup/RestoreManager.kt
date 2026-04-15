package com.tristinbaker.lifeos.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tristinbaker.lifeos.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream

object RestoreManager {

    suspend fun restore(context: Context, backupUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val dbDir = context.getDatabasePath("placeholder").parentFile
                ?: error("Cannot locate database directory")

            // Remove all WAL/SHM files first so they don't conflict with restored data
            dbDir.listFiles()
                ?.filter { it.name.endsWith("-wal") || it.name.endsWith("-shm") }
                ?.forEach { it.delete() }

            val inputStream = context.contentResolver.openInputStream(backupUri)
                ?: error("Cannot open backup file")

            val coversDir = File(context.filesDir, "media_covers")
            val journalImagesDir = File(context.filesDir, "journal_images")
            val dataStoreDir = File(context.filesDir, "datastore")

            inputStream.use { raw ->
                ZipInputStream(raw.buffered()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val entryName = entry.name
                            val destFile = when {
                                entryName.startsWith("media_covers/") -> {
                                    coversDir.mkdirs()
                                    File(coversDir, File(entryName).name)
                                }
                                entryName.startsWith("journal_images/") -> {
                                    journalImagesDir.mkdirs()
                                    File(journalImagesDir, File(entryName).name)
                                }
                                entryName.startsWith("datastore/") -> {
                                    dataStoreDir.mkdirs()
                                    File(dataStoreDir, File(entryName).name)
                                }
                                else -> {
                                    // DB files: strip any directory component and place in dbDir
                                    val name = File(entryName).name
                                    if (name.isEmpty()) { zip.closeEntry(); entry = zip.nextEntry; continue }
                                    File(dbDir, name)
                                }
                            }
                            destFile.outputStream().use { out -> zip.copyTo(out) }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        }
    }

    /** Restart the app so Room opens fresh connections to the restored database files. */
    fun restartApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
