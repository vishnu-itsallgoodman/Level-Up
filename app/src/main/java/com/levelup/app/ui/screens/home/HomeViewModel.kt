package com.levelup.app.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelup.app.data.database.LevelUpDatabase
import com.levelup.app.data.database.UserProgressEntity
import com.levelup.app.data.repository.DailySnapshot
import com.levelup.app.data.repository.LevelUpRepository
import com.levelup.app.domain.model.*
import com.levelup.app.domain.usecase.*
import com.levelup.app.utils.DateUtils
import com.levelup.app.widget.updateAllWidgets
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val playerState: PlayerState = PlayerState(1, Rank.E, 0, 0, 100, 0, 0),
    val dailySnapshot: DailySnapshot? = null,
    val levelUpEvent: LevelUpEvent? = null,
    val isLoading: Boolean = true
)

data class LevelUpEvent(val fromLevel: Int, val toLevel: Int, val xpGained: Int)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LevelUpRepository,
    private val database: LevelUpDatabase,
    private val toggleWorkout: ToggleWorkoutUseCase,
    private val toggleHabit: ToggleHabitUseCase,
    private val checkDayCompletion: CheckDayCompletionUseCase,
    private val evaluateAchievements: EvaluateAchievementsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedWorkoutsIfEmpty()
            repository.seedHabitsIfEmpty()
            repository.seedAchievementsIfEmpty()
            repository.getOrCreateDailyRecord(DateUtils.today())
            repository.ensureWorkoutProgressForDate(DateUtils.today())
            repository.ensureHabitProgressForDate(DateUtils.today())

            combine(
                repository.observeUserProgress(),
                repository.observeWorkoutProgressForDate(DateUtils.today()),
                repository.observeHabitProgressForDate(DateUtils.today())
            ) { up, _, _ ->
                refreshState(up)
            }.collect()
        }
    }

    private suspend fun refreshState(upEntity: UserProgressEntity?) {
        val upRaw         = upEntity ?: repository.getUserProgress()
        val currentStreak = repository.recalculateStreak()
        val up            = if (upRaw.streak != currentStreak) {
            val updated = upRaw.copy(streak = currentStreak, longestStreak = maxOf(upRaw.longestStreak, currentStreak))
            repository.upsertUserProgress(updated)
            updated
        } else {
            upRaw
        }

        val level   = LevelConfig.levelForTotalXp(up.totalXp)
        val rank    = Rank.forLevel(level)
        val xpIn    = LevelConfig.xpWithinCurrentLevel(up.totalXp)
        val xpNext  = LevelConfig.xpForLevel(level)
        val snap    = repository.buildDailySnapshot(DateUtils.today())

        _uiState.update {
            it.copy(
                playerState = PlayerState(level, rank, up.totalXp, xpIn, xpNext, up.streak, up.longestStreak),
                dailySnapshot = snap,
                isLoading = false
            )
        }
    }

    fun onToggleWorkout(workoutId: Long) {
        viewModelScope.launch {
            val prevLevel = LevelConfig.levelForTotalXp(repository.getUserProgress().totalXp)
            val xpDelta   = toggleWorkout(DateUtils.today(), workoutId)
            checkDayCompletion(DateUtils.today())
            evaluateAchievements()
            updateAllWidgets(context, database)

            if (xpDelta > 0) {
                val newLevel = LevelConfig.levelForTotalXp(repository.getUserProgress().totalXp)
                if (newLevel > prevLevel) {
                    _uiState.update { it.copy(levelUpEvent = LevelUpEvent(prevLevel, newLevel, xpDelta)) }
                }
            }
        }
    }

    fun onToggleHabit(habitId: Long) {
        viewModelScope.launch {
            val prevLevel = LevelConfig.levelForTotalXp(repository.getUserProgress().totalXp)
            val xpDelta   = toggleHabit(DateUtils.today(), habitId)
            checkDayCompletion(DateUtils.today())
            evaluateAchievements()
            updateAllWidgets(context, database)

            if (xpDelta > 0) {
                val newLevel = LevelConfig.levelForTotalXp(repository.getUserProgress().totalXp)
                if (newLevel > prevLevel) {
                    _uiState.update { it.copy(levelUpEvent = LevelUpEvent(prevLevel, newLevel, xpDelta)) }
                }
            }
        }
    }

    fun dismissLevelUp() {
        _uiState.update { it.copy(levelUpEvent = null) }
    }
}
