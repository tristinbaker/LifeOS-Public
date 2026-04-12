package com.lifeos.modules.lifeos_mealtracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserSettings(
    val dailyCalorieGoal: Int = 2000,
    val goalWeight: Double? = null,
    val weeklyWeightGoalRate: Double = 1.0,
    val targetProtein: Int? = null,
    val targetCarbs: Int? = null,
    val targetFat: Int? = null,
    val shamePhotoUri: String? = null,
    val shameMessages: List<String> = defaultShameMessages,
    val motivationalMessages: List<String> = defaultMotivationalMessages,
    val isDarkMode: Boolean = true,
    val hourlyShameNotifications: Boolean = false,
    val motivationalMode: Boolean = false,
    val currentStreak: Int = 0
)

val defaultShameMessages = listOf(
    "You just ate [X] calories like it was nothing. Get your goddamn shit together.",
    "That's [X] calories of pure self-destruction. You're a fucking disaster.",
    "Were you raised in a barn? No manners, no self-control, just shoving garbage in your face.",
    "Congratulations, you absolute waste of space. You've set your progress back by a week with that pathetic excuse for a meal.",
    "[X] calories over your limit. That's the calorie equivalent of saying 'I don't give a fuck about my goals.'",
    "You look at that food and your brain turns off completely, doesn't it? The only thing running your life is your stomach.",
    "Every time you do this, a kitten dies. No wait, that's deforestation. But your dignity? Yeah, that's dead too.",
    "That 'cheat meal' was you cheating on yourself with a goddamn refrigerator. Disgusting.",
    "You don't have a 'food problem.' You have a 'no self-discipline and zero accountability' problem. Fucking figure it out.",
    "You just shoved [X] calories down your throat like a goddamn garbage disposal. Is that what you are? A human dump?",
    "Your willpower is so pathetic that a slice of pizza has more control over you than your own brain.",
    "Some people have dreams. You have a drawer full of diet plans you'll never follow. Typical.",
    "You just proved that you care more about temporary taste sensations than long-term results. Selfish and stupid.",
    "That was not a snack. That was a cry for help that nobody asked for.",
    "You ate like you had no goals, no future, and no self-respect. Wake the fuck up.",
    "I bet you justify this shit to yourself every single time. 'I'll work it off.' Spoiler: you won't.",
    "Your body is a temple? Nah, more like a landfill. And you keep adding to the garbage pile.",
    "You just ate [X] calories like it owed you money. Spoiler: it doesn't. Your pants do, though.",
    "Every time you exceed your limit, a fairy dies. Too bad it's not you.",
    "That's the eating pattern of someone who doesn't give a shit about themselves. And it shows.",
    "You know what's worse than going over your calorie limit? Your pathetic excuse for self-control.",
    "Did you think the calories wouldn't count? Did you think you were special? You're not. They always count, you dipshit.",
    "You just treated your body like a trash can. No pride, no dignity, just 'me want food now.'",
    "That meal was a choice. You chose to fuck up. Own it, you lazy bastard.",
    "Your discipline is a joke. Your excuses are a tragedy. Your portions are a crime scene."
)

val defaultMotivationalMessages = listOf(
    "You're on a [X] day streak! Your future self is fucking thrilled.",
    "Holy shit, [X] days under goal? You're actually doing this. Unbelievable.",
    "Keep going. Every day under goal is a middle finger to everyone who doubted you.",
    "You know what's harder than staying disciplined? Being a slave to your impulses. You're winning.",
    "[X] days strong. You're building something here. Don't fuck it up.",
    "Your commitment is showing. And it looks goddamn good on you.",
    "You showed up today. You logged your food. You stayed under goal. That's all you can do. And you did it.",
    "Some people talk about getting healthy. You're actually doing it. While they're scrolling, you're winning.",
    "The discipline you're building now? It's going to pay off in ways you can't even imagine yet.",
    "You're not perfect. You're persistent. And that's better.",
    "Every meal logged is a choice. Every day under goal is a victory. You're collecting victories.",
    "You could be eating garbage right now. Instead you're here, tracking, winning.",
    "[X] days. That's not luck. That's commitment. Own it.",
    "Your future self is going to look back at this streak and thank you. Or curse you. Depends on what you do next.",
    "You know what breaks streaks? Self-doubt. Laziness. Giving up. Don't be that person.",
    "This is what showing up looks like. Day after day. You're a machine.",
    "Goals don't care about your feelings. They only care about what you do. And you're doing it.",
    "You're proving to yourself that you can do hard things. That's worth more than any six-pack.",
    "[X] days of not being a fatass. You've officially become a person with self-control. Who knew?",
    "The hunger you resist today? It's the pride you'll feel tomorrow. Keep going."
)

val extraShameOnStreakBreakMessages = listOf(
    "You made it [X] DAYS and threw it all away in ONE meal. Incredible. Truly.",
    "[X] days of discipline. Ruined. Over. Done. Because you couldn't say no to ONE thing.",
    "You were on a [X] day streak. A [X] DAY STREAK. And you just... ended it. With one stupid meal. Unbelievable.",
    "Think about [X] days. All that hard work. All that dedication. Gone. In an instant. Because of you.",
    "You had [X] days. TWENTY. THIRTY. Whatever. And you decided 'nah, I want to feel like shit about myself.' Congratulations.",
    "A [X] day streak means you PROVED you could do it. Which makes this failure so much worse. You KNEW better.",
    "Here's the thing about streaks: they're easy to start. They're hard to keep. And they're DEVASTATING to break. You just learned that.",
    "You didn't just go over your goal. You ended [X] days of WINNING. Congratulations on being your own worst enemy.",
    "The worst part? You were CRUSHING it. [X] days. That's not a small number. That's a LIFESTYLE. Until you decided to end it.",
    "I've seen a lot of self-sabotage in my time. But a [X] day streak ending in one meal? That's elite-level self-destruction."
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
        val GOAL_WEIGHT = doublePreferencesKey("goal_weight")
        val WEEKLY_WEIGHT_GOAL_RATE = doublePreferencesKey("weekly_weight_goal_rate")
        val TARGET_PROTEIN = intPreferencesKey("target_protein")
        val TARGET_CARBS = intPreferencesKey("target_carbs")
        val TARGET_FAT = intPreferencesKey("target_fat")
        val SHAME_PHOTO_URI = stringPreferencesKey("shame_photo_uri")
        val SHAME_MESSAGES = stringPreferencesKey("shame_messages")
        val MOTIVATIONAL_MESSAGES = stringPreferencesKey("motivational_messages")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val HOURLY_SHAME_NOTIFICATIONS = booleanPreferencesKey("hourly_shame_notifications")
        val MOTIVATIONAL_MODE = booleanPreferencesKey("motivational_mode")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val shameMessagesString = preferences[PreferencesKeys.SHAME_MESSAGES]
        val shameMessages = if (shameMessagesString != null) {
            shameMessagesString.split("|||").filter { it.isNotBlank() }
        } else {
            defaultShameMessages
        }

        val motivationalMessagesString = preferences[PreferencesKeys.MOTIVATIONAL_MESSAGES]
        val motivationalMessages = if (motivationalMessagesString != null) {
            motivationalMessagesString.split("|||").filter { it.isNotBlank() }
        } else {
            defaultMotivationalMessages
        }

        UserSettings(
            dailyCalorieGoal = preferences[PreferencesKeys.DAILY_CALORIE_GOAL] ?: 2000,
            goalWeight = preferences[PreferencesKeys.GOAL_WEIGHT],
            weeklyWeightGoalRate = preferences[PreferencesKeys.WEEKLY_WEIGHT_GOAL_RATE] ?: 1.0,
            targetProtein = preferences[PreferencesKeys.TARGET_PROTEIN],
            targetCarbs = preferences[PreferencesKeys.TARGET_CARBS],
            targetFat = preferences[PreferencesKeys.TARGET_FAT],
            shamePhotoUri = preferences[PreferencesKeys.SHAME_PHOTO_URI],
            shameMessages = shameMessages,
            motivationalMessages = motivationalMessages,
            isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: true,
            hourlyShameNotifications = preferences[PreferencesKeys.HOURLY_SHAME_NOTIFICATIONS] ?: false,
            motivationalMode = preferences[PreferencesKeys.MOTIVATIONAL_MODE] ?: false,
            currentStreak = preferences[PreferencesKeys.CURRENT_STREAK] ?: 0
        )
    }

    suspend fun updateDailyCalorieGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_CALORIE_GOAL] = goal
        }
    }

    suspend fun updateGoalWeight(goal: Double?) {
        context.dataStore.edit { preferences ->
            if (goal != null) {
                preferences[PreferencesKeys.GOAL_WEIGHT] = goal
            } else {
                preferences.remove(PreferencesKeys.GOAL_WEIGHT)
            }
        }
    }

    suspend fun updateWeeklyWeightGoalRate(rate: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_WEIGHT_GOAL_RATE] = rate
        }
    }

    suspend fun updateMacroGoals(protein: Int?, carbs: Int?, fat: Int?) {
        context.dataStore.edit { preferences ->
            if (protein != null) preferences[PreferencesKeys.TARGET_PROTEIN] = protein
            else preferences.remove(PreferencesKeys.TARGET_PROTEIN)
            if (carbs != null) preferences[PreferencesKeys.TARGET_CARBS] = carbs
            else preferences.remove(PreferencesKeys.TARGET_CARBS)
            if (fat != null) preferences[PreferencesKeys.TARGET_FAT] = fat
            else preferences.remove(PreferencesKeys.TARGET_FAT)
        }
    }

    suspend fun updateShamePhotoUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[PreferencesKeys.SHAME_PHOTO_URI] = uri
            } else {
                preferences.remove(PreferencesKeys.SHAME_PHOTO_URI)
            }
        }
    }

    suspend fun updateShameMessages(messages: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHAME_MESSAGES] = messages.joinToString("|||")
        }
    }

    suspend fun updateMotivationalMessages(messages: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOTIVATIONAL_MESSAGES] = messages.joinToString("|||")
        }
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun updateHourlyShameNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HOURLY_SHAME_NOTIFICATIONS] = enabled
        }
    }

    suspend fun updateMotivationalMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOTIVATIONAL_MODE] = enabled
        }
    }

    suspend fun updateCurrentStreak(streak: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_STREAK] = streak
        }
    }
}
