package com.levelup.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelup.app.data.database.WorkoutEntity
import com.levelup.app.data.repository.LevelUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: LevelUpRepository
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<WorkoutEntity>>(emptyList())
    val workouts: StateFlow<List<WorkoutEntity>> = _workouts.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedWorkoutsIfEmpty()
            repository.seedHabitsIfEmpty()
            repository.seedAchievementsIfEmpty()
            _workouts.value = repository.getWorkouts()
        }
    }

    fun updateTarget(id: Long, target: Int) {
        _workouts.value = _workouts.value.map {
            if (it.id == id) it.copy(target = target.coerceAtLeast(1)) else it
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _workouts.value.forEach { repository.upsertWorkout(it) }
            repository.completeOnboarding()
        }
    }
}
