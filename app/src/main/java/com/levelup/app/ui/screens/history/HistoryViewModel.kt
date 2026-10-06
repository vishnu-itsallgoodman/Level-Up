package com.levelup.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelup.app.data.database.DailyRecordEntity
import com.levelup.app.data.database.UserProgressEntity
import com.levelup.app.data.database.WorkoutEntity
import com.levelup.app.data.repository.LevelUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HistoryUiState(
    val records: List<DailyRecordEntity> = emptyList(),
    val userProgress: UserProgressEntity? = null,
    val workouts: List<WorkoutEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: LevelUpRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.observeAllDailyRecords(),
        repository.observeUserProgress(),
        repository.observeWorkouts()
    ) { records, up, workouts ->
        HistoryUiState(records = records, userProgress = up, workouts = workouts, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())
}
