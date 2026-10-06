package com.levelup.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelup.app.data.database.HabitEntity
import com.levelup.app.data.database.UserProgressEntity
import com.levelup.app.data.database.WorkoutEntity
import com.levelup.app.data.repository.LevelUpRepository
import com.levelup.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val workouts: List<WorkoutEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: LevelUpRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.observeWorkouts(),
        repository.observeHabits()
    ) { workouts, habits ->
        SettingsUiState(workouts = workouts, habits = habits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun upsertWorkout(workout: WorkoutEntity) {
        viewModelScope.launch { repository.upsertWorkout(workout) }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch { repository.deleteWorkout(id) }
    }

    fun toggleWorkoutEnabled(workout: WorkoutEntity) {
        viewModelScope.launch { repository.upsertWorkout(workout.copy(enabled = !workout.enabled)) }
    }

    fun upsertHabit(habit: HabitEntity) {
        viewModelScope.launch { repository.upsertHabit(habit) }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch { repository.deleteHabit(id) }
    }

    fun toggleHabitEnabled(habit: HabitEntity) {
        viewModelScope.launch { repository.upsertHabit(habit.copy(enabled = !habit.enabled)) }
    }

    fun resetTodayProgress() {
        viewModelScope.launch {
            repository.resetTodayProgress(DateUtils.today())
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.upsertUserProgress(UserProgressEntity())
        }
    }
}
