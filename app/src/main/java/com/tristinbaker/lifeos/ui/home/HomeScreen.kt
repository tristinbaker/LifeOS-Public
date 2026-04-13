package com.tristinbaker.lifeos.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.core.LifeOSModule
import com.lifeos.core.ModuleRegistry
import com.tristinbaker.lifeos.backup.BackupManager
import com.tristinbaker.lifeos.backup.BackupPreferences
import com.tristinbaker.lifeos.backup.RestoreManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onModuleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBackupDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }

    val autoBackupEnabled by BackupPreferences.autoBackupEnabled(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val folderUriString by BackupPreferences.folderUri(context)
        .collectAsStateWithLifecycle(initialValue = null)

    val folderName = remember(folderUriString) {
        folderUriString?.let { DocumentFile.fromTreeUri(context, android.net.Uri.parse(it))?.name }
    }

    // One-shot manual backup
    val dateTag = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val manualBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                showBackupDialog = false
                val result = BackupManager.backup(context, uri)
                snackbarHostState.showSnackbar(
                    if (result.isSuccess) "Backup saved (${result.getOrDefault(0)} files)"
                    else "Backup failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    // Folder picker for auto-backup destination
    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Persist access so the worker can write to this folder across reboots
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            scope.launch {
                BackupPreferences.setFolderUri(context, uri.toString())
                if (!autoBackupEnabled) {
                    BackupPreferences.setAutoBackupEnabled(context, true)
                    BackupManager.scheduleAutoBackup(context)
                }
            }
        }
    }

    // File picker for selecting a backup ZIP to restore
    val restorePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirm = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LifeOS",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Your personal life command center",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                IconButton(onClick = { showBackupDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = "Backup",
                        tint = if (autoBackupEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ModuleRegistry.modules) { module ->
                    ModuleCard(
                        module = module,
                        onClick = { onModuleClick(module.id) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showBackupDialog) {
        BackupDialog(
            autoBackupEnabled = autoBackupEnabled,
            folderName = folderName,
            onDismiss = { showBackupDialog = false },
            onBackupNow = { manualBackupLauncher.launch("lifeos_backup_$dateTag.zip") },
            onAutoBackupToggled = { enable ->
                scope.launch {
                    if (enable) {
                        if (folderUriString != null) {
                            BackupPreferences.setAutoBackupEnabled(context, true)
                            BackupManager.scheduleAutoBackup(context)
                        } else {
                            folderPickerLauncher.launch(null)
                        }
                    } else {
                        BackupPreferences.setAutoBackupEnabled(context, false)
                        BackupManager.cancelAutoBackup(context)
                    }
                }
            },
            onChangeFolder = { folderPickerLauncher.launch(null) },
            onImportBackup = {
                showBackupDialog = false
                restorePickerLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
            }
        )
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isRestoring) showRestoreConfirm = false },
            title = { Text("Restore backup?") },
            text = {
                Text("This will overwrite all current data with the selected backup and restart the app. This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingRestoreUri ?: return@Button
                        isRestoring = true
                        scope.launch {
                            val result = RestoreManager.restore(context, uri)
                            if (result.isSuccess) {
                                RestoreManager.restartApp(context)
                            } else {
                                isRestoring = false
                                showRestoreConfirm = false
                                snackbarHostState.showSnackbar(
                                    "Restore failed: ${result.exceptionOrNull()?.message}"
                                )
                            }
                        }
                    },
                    enabled = !isRestoring
                ) {
                    Text(if (isRestoring) "Restoring…" else "Restore & Restart")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreConfirm = false },
                    enabled = !isRestoring
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BackupDialog(
    autoBackupEnabled: Boolean,
    folderName: String?,
    onDismiss: () -> Unit,
    onBackupNow: () -> Unit,
    onAutoBackupToggled: (Boolean) -> Unit,
    onChangeFolder: () -> Unit,
    onImportBackup: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Backup") },
        text = {
            Column {
                Button(
                    onClick = onBackupNow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Backup Now")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onImportBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Import Backup…")
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Text(
                    "Auto-Backup",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Overwrites the same file daily in the chosen folder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable daily backup")
                    Switch(
                        checked = autoBackupEnabled,
                        onCheckedChange = onAutoBackupToggled
                    )
                }

                if (autoBackupEnabled || folderName != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = folderName ?: "No folder selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (folderName != null)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(onClick = onChangeFolder) {
                            Text(if (folderName == null) "Select folder" else "Change")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun ModuleCard(
    module: LifeOSModule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = module.icon,
                    contentDescription = module.name,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }
            }
        }
    }
}
