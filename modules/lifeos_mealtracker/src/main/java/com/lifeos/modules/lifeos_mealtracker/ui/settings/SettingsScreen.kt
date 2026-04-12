package com.lifeos.modules.lifeos_mealtracker.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lifeos.modules.lifeos_mealtracker.data.repository.defaultMotivationalMessages
import com.lifeos.modules.lifeos_mealtracker.data.repository.defaultShameMessages
import com.lifeos.modules.lifeos_mealtracker.data.repository.UserSettings
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ExportData(
    val dailyCalorieGoal: Int,
    val goalWeight: Double?,
    val weeklyWeightGoalRate: Double,
    val targetProtein: Int?,
    val targetCarbs: Int?,
    val targetFat: Int?,
    val shameMessages: List<String>,
    val motivationalMessages: List<String>,
    val motivationalMode: Boolean,
    val currentStreak: Int,
    val hourlyShameNotifications: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNotificationPermissionRequired: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var showCalorieGoalDialog by remember { mutableStateOf(false) }
    var showGoalWeightDialog by remember { mutableStateOf(false) }
    var showWeeklyRateDialog by remember { mutableStateOf(false) }
    var showMacroDialog by remember { mutableStateOf(false) }
    var showMessagesDialog by remember { mutableStateOf(false) }
    var showMotivationalMessagesDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateShamePhotoUri(uri)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportData(context, it, settings) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { importData(context, it, viewModel) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            SettingsSection(title = "Goals") {
                SettingsItem(
                    title = "Daily Calorie Goal",
                    subtitle = "${settings.dailyCalorieGoal} calories",
                    onClick = { showCalorieGoalDialog = true }
                )
                SettingsItem(
                    title = "Goal Weight",
                    subtitle = settings.goalWeight?.let { "$it lbs" } ?: "Not set",
                    onClick = { showGoalWeightDialog = true }
                )
                SettingsItem(
                    title = "Weekly Weight Goal Rate",
                    subtitle = "${settings.weeklyWeightGoalRate} lbs/week",
                    onClick = { showWeeklyRateDialog = true }
                )
                SettingsItem(
                    title = "Macro Goals",
                    subtitle = "P: ${settings.targetProtein ?: "—"}g | C: ${settings.targetCarbs ?: "—"}g | F: ${settings.targetFat ?: "—"}g",
                    onClick = { showMacroDialog = true }
                )
            }
        }

        item {
            SettingsSection(title = "Shame Photo") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (settings.shamePhotoUri != null) {
                        AsyncImage(
                            model = settings.shamePhotoUri,
                            contentDescription = "Shame photo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (settings.shamePhotoUri != null) "Change Photo" else "Set Shame Photo",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (settings.shamePhotoUri != null) "Tap to change" else "Tap to select from gallery",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (settings.shamePhotoUri != null) {
                    TextButton(
                        onClick = { viewModel.updateShamePhotoUri(null) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Berating Messages") {
                SettingsItem(
                    title = "Edit Messages",
                    subtitle = "${settings.shameMessages.size} messages",
                    onClick = { showMessagesDialog = true }
                )
            }
        }

        item {
            SettingsSection(title = "Shame Notifications") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Every 5 Minutes Shame Reminders",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Get notified every 5 minutes if over goal. Opens forced shame view.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.hourlyShameNotifications,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        viewModel.updateHourlyShameNotifications(true)
                                    } else {
                                        onNotificationPermissionRequired()
                                    }
                                } else {
                                    viewModel.updateHourlyShameNotifications(true)
                                }
                            } else {
                                viewModel.updateHourlyShameNotifications(false)
                            }
                        }
                    )
                }
            }
        }

        item {
            SettingsSection(title = "Motivation & Streaks") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Motivational Mode",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Show encouragement when under goal. Breaking a streak = extra shame.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.motivationalMode,
                        onCheckedChange = { viewModel.updateMotivationalMode(it) }
                    )
                }
                if (settings.currentStreak > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Streak",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${settings.currentStreak} days 🔥",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                SettingsItem(
                    title = "Edit Motivational Messages",
                    subtitle = "${settings.motivationalMessages.size} messages",
                    onClick = { showMotivationalMessagesDialog = true }
                )
            }
        }

        item {
            SettingsSection(title = "Appearance") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = settings.isDarkMode,
                        onCheckedChange = { viewModel.updateDarkMode(it) }
                    )
                }
            }
        }

        item {
            SettingsSection(title = "Data") {
                SettingsItem(
                    title = "Export Data",
                    subtitle = "Save your settings and messages to a file",
                    onClick = { exportLauncher.launch("brutal_meal_tracker_backup.json") }
                )
                SettingsItem(
                    title = "Import Data",
                    subtitle = "Restore from a backup file",
                    onClick = { importLauncher.launch("application/json") }
                )
            }
        }
    }

    if (showCalorieGoalDialog) {
        CalorieGoalDialog(
            currentGoal = settings.dailyCalorieGoal,
            onDismiss = { showCalorieGoalDialog = false },
            onConfirm = { goal ->
                viewModel.updateDailyCalorieGoal(goal)
                showCalorieGoalDialog = false
            }
        )
    }

    if (showGoalWeightDialog) {
        GoalWeightDialog(
            currentGoal = settings.goalWeight,
            onDismiss = { showGoalWeightDialog = false },
            onConfirm = { goal ->
                viewModel.updateGoalWeight(goal)
                showGoalWeightDialog = false
            }
        )
    }

    if (showWeeklyRateDialog) {
        WeeklyRateDialog(
            currentRate = settings.weeklyWeightGoalRate,
            onDismiss = { showWeeklyRateDialog = false },
            onConfirm = { rate ->
                viewModel.updateWeeklyWeightGoalRate(rate)
                showWeeklyRateDialog = false
            }
        )
    }

    if (showMacroDialog) {
        MacroGoalsDialog(
            currentProtein = settings.targetProtein,
            currentCarbs = settings.targetCarbs,
            currentFat = settings.targetFat,
            onDismiss = { showMacroDialog = false },
            onConfirm = { protein, carbs, fat ->
                viewModel.updateMacroGoals(protein, carbs, fat)
                showMacroDialog = false
            }
        )
    }

    if (showMessagesDialog) {
        MessagesDialog(
            currentMessages = settings.shameMessages,
            onDismiss = { showMessagesDialog = false },
            onConfirm = { messages ->
                viewModel.updateShameMessages(messages)
                showMessagesDialog = false
            }
        )
    }

    if (showMotivationalMessagesDialog) {
        MotivationalMessagesDialog(
            currentMessages = settings.motivationalMessages,
            onDismiss = { showMotivationalMessagesDialog = false },
            onConfirm = { messages ->
                viewModel.updateMotivationalMessages(messages)
                showMotivationalMessagesDialog = false
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CalorieGoalDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Calorie Goal") },
        text = {
            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it.filter { c -> c.isDigit() } },
                label = { Text("Calories") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { goalText.toIntOrNull()?.let { onConfirm(it) } }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GoalWeightDialog(
    currentGoal: Double?,
    onDismiss: () -> Unit,
    onConfirm: (Double?) -> Unit
) {
    var weightText by remember { mutableStateOf(currentGoal?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Goal Weight") },
        text = {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = weightText.toDoubleOrNull()
                    onConfirm(weight)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onConfirm(null) }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun WeeklyRateDialog(
    currentRate: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val options = listOf(0.25, 0.5, 1.0, 1.5, 2.0)
    var selectedRate by remember { mutableStateOf(currentRate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weekly Weight Goal") },
        text = {
            Column {
                options.forEach { rate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRate = rate }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRate == rate,
                            onClick = { selectedRate = rate }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${rate} lbs/week")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedRate) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MacroGoalsDialog(
    currentProtein: Int?,
    currentCarbs: Int?,
    currentFat: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int?, Int?, Int?) -> Unit
) {
    var proteinText by remember { mutableStateOf(currentProtein?.toString() ?: "") }
    var carbsText by remember { mutableStateOf(currentCarbs?.toString() ?: "") }
    var fatText by remember { mutableStateOf(currentFat?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Macro Goals") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = proteinText,
                    onValueChange = { proteinText = it.filter { c -> c.isDigit() } },
                    label = { Text("Protein (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = carbsText,
                    onValueChange = { carbsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Carbs (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fatText,
                    onValueChange = { fatText = it.filter { c -> c.isDigit() } },
                    label = { Text("Fat (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        proteinText.toIntOrNull(),
                        carbsText.toIntOrNull(),
                        fatText.toIntOrNull()
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MessagesDialog(
    currentMessages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var messages by remember { mutableStateOf(currentMessages.toMutableList()) }
    var newMessage by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf(-1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Berating Messages") },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(messages) { index, message ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    messages = messages.toMutableList().also { it.removeAt(index) }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        label = { Text("New message") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (newMessage.isNotBlank()) {
                                messages = messages.toMutableList().also { it.add(newMessage) }
                                newMessage = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        messages = defaultShameMessages.toMutableList()
                    }
                ) {
                    Text("Reset to Default")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(messages) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MotivationalMessagesDialog(
    currentMessages: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var messages by remember { mutableStateOf(currentMessages.toMutableList()) }
    var newMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Motivational Messages") },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(messages) { index, message ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    messages = messages.toMutableList().also { it.removeAt(index) }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        label = { Text("New message") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (newMessage.isNotBlank()) {
                                messages = messages.toMutableList().also { it.add(newMessage) }
                                newMessage = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        messages = defaultMotivationalMessages.toMutableList()
                    }
                ) {
                    Text("Reset to Default")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(messages) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun exportData(context: Context, uri: Uri, settings: UserSettings) {
    try {
        val exportData = ExportData(
            dailyCalorieGoal = settings.dailyCalorieGoal,
            goalWeight = settings.goalWeight,
            weeklyWeightGoalRate = settings.weeklyWeightGoalRate,
            targetProtein = settings.targetProtein,
            targetCarbs = settings.targetCarbs,
            targetFat = settings.targetFat,
            shameMessages = settings.shameMessages,
            motivationalMessages = settings.motivationalMessages,
            motivationalMode = settings.motivationalMode,
            currentStreak = settings.currentStreak,
            hourlyShameNotifications = settings.hourlyShameNotifications
        )
        val json = Json.encodeToString(exportData)
        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun importData(context: Context, uri: Uri, viewModel: SettingsViewModel) {
    try {
        val json = context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
        json?.let {
            val data = Json.decodeFromString<ExportData>(it)
            kotlinx.coroutines.MainScope().launch {
                viewModel.updateDailyCalorieGoal(data.dailyCalorieGoal)
                viewModel.updateGoalWeight(data.goalWeight)
                viewModel.updateWeeklyWeightGoalRate(data.weeklyWeightGoalRate)
                viewModel.updateMacroGoals(data.targetProtein, data.targetCarbs, data.targetFat)
                viewModel.updateShameMessages(data.shameMessages)
                viewModel.updateMotivationalMessages(data.motivationalMessages)
                viewModel.updateMotivationalMode(data.motivationalMode)
                viewModel.updateStreak(data.currentStreak)
                viewModel.updateHourlyShameNotifications(data.hourlyShameNotifications)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
