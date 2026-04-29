package com.tristinbaker.lifeos.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tristinbaker.lifeos.UserPreferences
import com.tristinbaker.lifeos.WeatherData
import com.tristinbaker.lifeos.WeatherService
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
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showHomeSettingsDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }

    val autoBackupEnabled by BackupPreferences.autoBackupEnabled(context)
        .collectAsStateWithLifecycle(initialValue = false)
    val folderUriString by BackupPreferences.folderUri(context)
        .collectAsStateWithLifecycle(initialValue = null)
    val biometricEnabled by BackupPreferences.biometricEnabled(context)
        .collectAsStateWithLifecycle(initialValue = false)

    val userName by UserPreferences.userName(context).collectAsStateWithLifecycle(initialValue = "")
    val cityName by UserPreferences.cityName(context).collectAsStateWithLifecycle(initialValue = "")
    val latitude by UserPreferences.latitude(context).collectAsStateWithLifecycle(initialValue = null)
    val longitude by UserPreferences.longitude(context).collectAsStateWithLifecycle(initialValue = null)
    val showWeather by UserPreferences.showWeather(context).collectAsStateWithLifecycle(initialValue = false)
    val gridColumns by UserPreferences.gridColumns(context).collectAsStateWithLifecycle(initialValue = 2)

    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isLoadingWeather by remember { mutableStateOf(false) }

    LaunchedEffect(latitude, longitude, showWeather) {
        if (showWeather && latitude != null && longitude != null) {
            isLoadingWeather = true
            val result = WeatherService.getWeather(latitude!!, longitude!!)
            weatherData = result?.copy(cityName = cityName)
            isLoadingWeather = false
        } else {
            weatherData = null
        }
    }

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
                Text(
                    text = "LifeOS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    IconButton(onClick = { showHomeSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { showBiometricDialog = true }) {
                        Icon(
                            imageVector = if (biometricEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Biometric lock",
                            tint = if (biometricEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
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
            }

            Spacer(modifier = Modifier.height(8.dp))

            WeatherCard(
                userName = userName,
                weatherData = weatherData,
                isLoading = isLoadingWeather
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 8.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ModuleRegistry.modules) { module ->
                    ModuleCard(
                        module = module,
                        compact = gridColumns >= 3,
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

    if (showBiometricDialog) {
        val isCurrentlyEnabled = biometricEnabled
        val biometricAvailable = remember {
            BiometricManager.from(context).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ) == BiometricManager.BIOMETRIC_SUCCESS
        }

        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            title = { Text("App Lock") },
            text = {
                Column {
                    Text(
                        if (isCurrentlyEnabled)
                            "Biometric lock is currently enabled. Disable it?"
                        else
                            "Enable biometric lock to require fingerprint or face authentication when opening the app.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!biometricAvailable && !isCurrentlyEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No biometric hardware or credentials found on this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            BackupPreferences.setBiometricEnabled(context, !isCurrentlyEnabled)
                        }
                        showBiometricDialog = false
                    },
                    enabled = isCurrentlyEnabled || biometricAvailable
                ) {
                    Text(if (isCurrentlyEnabled) "Disable" else "Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricDialog = false }) { Text("Cancel") }
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

    if (showHomeSettingsDialog) {
        HomeSettingsDialog(
            currentName = userName,
            currentCity = cityName,
            currentShowWeather = showWeather,
            currentGridColumns = gridColumns,
            onDismiss = { showHomeSettingsDialog = false },
            onSave = { _, _, _ -> }
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
private fun WeatherCard(
    userName: String,
    weatherData: WeatherData?,
    isLoading: Boolean
) {
    val displayName = if (userName.isBlank()) "" else userName
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (displayName.isNotBlank()) {
                Text(
                    text = "Hello, $displayName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Loading weather...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            } else if (weatherData != null && weatherData.cityName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getWeatherIcon(weatherData.weatherCode),
                        contentDescription = weatherData.condition,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "It's currently ${weatherData.conditionPhrase} in ${weatherData.cityName} and ${weatherData.temperature}\u00b0.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "High: ${weatherData.highTemp}\u00b0  Low: ${weatherData.lowTemp}\u00b0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (weatherData.airQuality != null) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = getAqiGradient(weatherData.airQuality)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AQ: ${weatherData.airQuality}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "AQ: N/A",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else if (weatherData != null) {
                Text(
                    text = "Weather settings configured. Enter a city to see weather.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Tap the gear icon to set up weather.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun getWeatherIcon(weatherCode: Int): androidx.compose.ui.graphics.vector.ImageVector {
    return when (weatherCode) {
        0 -> Icons.Default.WbSunny
        1, 2, 3 -> Icons.Default.WbSunny
        45, 48 -> Icons.Default.Cloud
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> Icons.Default.WaterDrop
        71, 73, 75, 77, 85, 86 -> Icons.Default.AcUnit
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.WbSunny
    }
}

private fun getAqiGradient(aqi: Int): List<androidx.compose.ui.graphics.Color> {
    return when {
        aqi <= 20 -> listOf(androidx.compose.ui.graphics.Color(0xFF4CAF50), androidx.compose.ui.graphics.Color(0xFF8BC34A))
        aqi <= 40 -> listOf(androidx.compose.ui.graphics.Color(0xFF8BC34A), androidx.compose.ui.graphics.Color(0xFFCDDC39))
        aqi <= 60 -> listOf(androidx.compose.ui.graphics.Color(0xFFFFEB3B), androidx.compose.ui.graphics.Color(0xFFFFC107))
        aqi <= 80 -> listOf(androidx.compose.ui.graphics.Color(0xFFFFC107), androidx.compose.ui.graphics.Color(0xFFFF9800))
        aqi <= 100 -> listOf(androidx.compose.ui.graphics.Color(0xFFFF9800), androidx.compose.ui.graphics.Color(0xFFF44336))
        else -> listOf(androidx.compose.ui.graphics.Color(0xFFF44336), androidx.compose.ui.graphics.Color(0xFFB71C1C))
    }
}

@Composable
private fun HomeSettingsDialog(
    currentName: String,
    currentCity: String,
    currentShowWeather: Boolean,
    currentGridColumns: Int,
    onDismiss: () -> Unit,
    onSave: (name: String, city: String, showWeather: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var city by remember { mutableStateOf(currentCity) }
    var showWeather by remember { mutableStateOf(currentShowWeather) }
    var gridCols by remember { mutableStateOf(currentGridColumns) }
    var isGeocoding by remember { mutableStateOf(false) }
    var geocodeError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Home Settings") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your name") },
                    placeholder = { Text("friend") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { 
                        city = it
                        geocodeError = null
                    },
                    label = { Text("City") },
                    placeholder = { Text("Chattanooga") },
                    singleLine = true,
                    isError = geocodeError != null,
                    supportingText = geocodeError?.let { { Text(it) } },
                    trailingIcon = {
                        if (isGeocoding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show weather")
                    Switch(
                        checked = showWeather,
                        onCheckedChange = { showWeather = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Grid columns", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3, 4).forEach { cols ->
                        FilterChip(
                            selected = gridCols == cols,
                            onClick = { gridCols = cols },
                            label = { Text("$cols") }
                        )
                    }
                }

                if (geocodeError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = geocodeError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        if (city.isNotBlank()) {
                            isGeocoding = true
                            geocodeError = null
                            val result = WeatherService.geocodeCity(city)
                            isGeocoding = false
                            if (result != null) {
                                val (displayName, lat, lon) = result
                                UserPreferences.setLocation(context, displayName, lat, lon)
                                UserPreferences.setUserName(context, name)
                                UserPreferences.setShowWeather(context, showWeather)
                                UserPreferences.setGridColumns(context, gridCols)
                                onDismiss()
                            } else {
                                geocodeError = "Could not find city"
                            }
                        } else {
                            UserPreferences.setUserName(context, name)
                            UserPreferences.setShowWeather(context, false)
                            UserPreferences.setGridColumns(context, gridCols)
                            onDismiss()
                        }
                    }
                },
                enabled = !isGeocoding
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ModuleCard(
    module: LifeOSModule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
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
                        text = if (compact) module.shortDescription else module.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
