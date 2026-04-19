package com.lifeos.modules.lifeos_habittracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.modules.lifeos_habittracker.data.local.HabitEntity
import com.lifeos.modules.lifeos_habittracker.data.local.HabitFrequency
import com.lifeos.modules.lifeos_habittracker.data.repository.HabitsRepository
import com.lifeos.modules.lifeos_habittracker.notification.HabitReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitWithStats(
    val habit: HabitEntity,
    val isCheckedInToday: Boolean,
    val streak: Int,
    val totalCheckIns: Int,
    val daysSinceStart: Int
)

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val repository: HabitsRepository,
    private val reminderScheduler: HabitReminderScheduler
) : ViewModel() {

    private val _habitsWithStats = MutableStateFlow<List<HabitWithStats>>(emptyList())
    val habitsWithStats: StateFlow<List<HabitWithStats>> = _habitsWithStats.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            repository.getAllHabits().collect { habits ->
                val stats = habits.map { habit ->
                    HabitWithStats(
                        habit = habit,
                        isCheckedInToday = repository.isCheckedInToday(habit.id),
                        streak = repository.getStreak(habit.id),
                        totalCheckIns = repository.getCheckInCount(habit.id),
                        daysSinceStart = repository.daysSinceStart(habit)
                    )
                }
                _habitsWithStats.value = stats
            }
        }
    }

    fun toggleCheckIn(habitId: Long) {
        viewModelScope.launch {
            repository.toggleCheckIn(habitId)
            loadHabits()
        }
    }

    fun saveHabit(
        id: Long?,
        name: String,
        description: String,
        frequency: HabitFrequency,
        daysOfWeek: String,
        timesPerWeek: Int,
        reminderEnabled: Boolean,
        reminderTime: Long?,
        reminderDays: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val savedId: Long = if (id != null && id > 0) {
                val existing = repository.getHabitById(id)
                val habit = HabitEntity(
                    id = id,
                    name = name,
                    description = description,
                    frequency = frequency,
                    daysOfWeek = daysOfWeek,
                    timesPerWeek = timesPerWeek,
                    reminderEnabled = reminderEnabled,
                    reminderTime = if (reminderEnabled) reminderTime else null,
                    reminderDays = if (reminderEnabled) reminderDays else "",
                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                )
                repository.updateHabit(habit)
                id
            } else {
                val habit = HabitEntity(
                    id = 0,
                    name = name,
                    description = description,
                    frequency = frequency,
                    daysOfWeek = daysOfWeek,
                    timesPerWeek = timesPerWeek,
                    reminderEnabled = reminderEnabled,
                    reminderTime = if (reminderEnabled) reminderTime else null,
                    reminderDays = if (reminderEnabled) reminderDays else ""
                )
                repository.insertHabit(habit)
            }

            if (reminderEnabled && reminderTime != null) {
                reminderScheduler.scheduleReminders(
                    habitId = savedId,
                    habitName = name,
                    reminderTimeMillis = reminderTime,
                    reminderDays = reminderDays
                )
            } else {
                reminderScheduler.cancelReminders(savedId)
            }

            onComplete()
        }
    }

    fun deleteHabit(habitId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            reminderScheduler.cancelReminders(habitId)
            repository.deleteHabit(habitId)
            onComplete()
        }
    }

    suspend fun getHabitById(habitId: Long): HabitEntity? {
        return repository.getHabitById(habitId)
    }
}
