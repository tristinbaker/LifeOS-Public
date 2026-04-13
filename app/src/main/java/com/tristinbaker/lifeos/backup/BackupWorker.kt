package com.tristinbaker.lifeos.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val folderUriString = BackupPreferences.folderUri(applicationContext).first()
            ?: return Result.failure()

        val folderUri = android.net.Uri.parse(folderUriString)
        val result = BackupManager.backupToFolder(applicationContext, folderUri)

        return if (result.isSuccess) Result.success() else Result.retry()
    }
}
