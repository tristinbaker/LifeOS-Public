package com.tristinbaker.lifeos.backup

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.backupDataStore: DataStore<Preferences> by preferencesDataStore(name = "backup_prefs")

object BackupPreferences {
    private val AUTO_ENABLED = booleanPreferencesKey("auto_backup_enabled")
    private val FOLDER_URI = stringPreferencesKey("backup_folder_uri")
    private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

    fun autoBackupEnabled(context: Context): Flow<Boolean> =
        context.backupDataStore.data.map { it[AUTO_ENABLED] ?: false }

    fun folderUri(context: Context): Flow<String?> =
        context.backupDataStore.data.map { it[FOLDER_URI] }

    fun biometricEnabled(context: Context): Flow<Boolean> =
        context.backupDataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }

    suspend fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
        context.backupDataStore.edit { it[AUTO_ENABLED] = enabled }
    }

    suspend fun setFolderUri(context: Context, uri: String) {
        context.backupDataStore.edit { it[FOLDER_URI] = uri }
    }

    suspend fun setBiometricEnabled(context: Context, enabled: Boolean) {
        context.backupDataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }
}
