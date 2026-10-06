package com.levelup.app.domain.usecase

import com.levelup.app.data.database.HabitProgressEntity
import com.levelup.app.data.database.WorkoutProgressEntity
import com.levelup.app.data.repository.LevelUpRepository
import com.levelup.app.domain.model.LevelConfig
import com.levelup.app.utils.DateUtils
import javax.inject.Inject

/**
 * Toggles a workout quest's completion state for the given date.
 * XP is awarded/reversed on toggle. Lifetime quest count & lifetime amount
 * increment at most ONCE per quest per calendar date (idempotent).
 */
class ToggleWorkoutUseCase @Inject constructor(
    private val repository: LevelUpRepository
) {
    suspend operator fun invoke(
        date: String,
        workoutId: Long
    ): Int {
        val workout = repository.getWorkoutById(workoutId) ?: return 0

        val existing = repository.getWorkoutProgressForDate(date)
            .firstOrNull { it.workoutId == workoutId }
            ?: WorkoutProgressEntity(date = date, workoutId = workoutId)

        val reward = workout.xpReward.coerceAtMost(50)
        val nowCompleted = !existing.completed
        val wasEverCompleted = existing.everCompleted

        val xpDelta = when {
            nowCompleted && !existing.xpAwarded -> reward
            !nowCompleted && existing.xpAwarded -> -reward
            else -> 0
        }

        repository.upsertWorkoutProgress(
            existing.copy(
                completed = nowCompleted,
                xpAwarded = nowCompleted,
                everCompleted = wasEverCompleted || nowCompleted
            )
        )

        // Increment lifetime workout amount & quest count only on the FIRST completion for this date
        if (nowCompleted && !wasEverCompleted) {
            val newLifetime = maxOf(0, workout.lifetimeAmount + workout.target)
            repository.upsertWorkout(workout.copy(lifetimeAmount = newLifetime))
            repository.incrementQuestsCompleted()
        }

        if (xpDelta != 0) {
            repository.addXp(xpDelta)
        }

        return xpDelta
    }
}

/**
 * Toggles a habit's completion for the given date.
 * XP is awarded/reversed on toggle. Lifetime quest count increments
 * at most ONCE per habit per calendar date (idempotent).
 */
class ToggleHabitUseCase @Inject constructor(
    private val repository: LevelUpRepository
) {
    suspend operator fun invoke(date: String, habitId: Long): Int {
        val habits    = repository.getHabits()
        val habit     = habits.firstOrNull { it.id == habitId } ?: return 0
        val existing  = repository.getHabitProgressForDate(date)
            .firstOrNull { it.habitId == habitId }
            ?: HabitProgressEntity(date = date, habitId = habitId)

        val reward = habit.xpReward.coerceAtMost(50)
        val nowComplete = !existing.completed
        val wasEverCompleted = existing.everCompleted

        val xpDelta = when {
            nowComplete && !existing.xpAwarded -> reward
            !nowComplete && existing.xpAwarded -> -reward
            else -> 0
        }

        repository.upsertHabitProgress(
            existing.copy(
                completed = nowComplete,
                xpAwarded = nowComplete,
                everCompleted = wasEverCompleted || nowComplete
            )
        )

        // Increment lifetime quest count only on the FIRST completion for this date
        if (nowComplete && !wasEverCompleted) {
            repository.incrementQuestsCompleted()
        }

        if (xpDelta != 0) {
            repository.addXp(xpDelta)
        }

        return xpDelta
    }
}

/**
 * Checks whether all active quests for a date are done and marks the day complete.
 * Also handles streak updates and idempotent lifetime days completed count.
 */
class CheckDayCompletionUseCase @Inject constructor(
    private val repository: LevelUpRepository
) {
    suspend operator fun invoke(date: String) {
        val snapshot = repository.buildDailySnapshot(date)
        val totalActive = snapshot.workouts.size + snapshot.habits.size
        val allDone  = totalActive > 0 &&
                       snapshot.workouts.all { it.xpAwarded } &&
                       snapshot.habits.all { it.xpAwarded }

        val record   = repository.getOrCreateDailyRecord(date)
        val wasAlreadyComplete = record.dayCompleted
        val wasEverCompleted = record.everCompleted

        if (allDone && !wasAlreadyComplete) {
            repository.upsertDailyRecord(
                record.copy(
                    dayCompleted = true,
                    everCompleted = true
                )
            )

            if (!wasEverCompleted) {
                repository.incrementDaysCompleted()
            }

            // Award day-completion bonus XP
            repository.addXp(com.levelup.app.domain.model.XpConfig.XP_DAILY_COMPLETE_BONUS)

            // Recalculate streak
            val newStreak = repository.recalculateStreak()
            val up = repository.getUserProgress()
            val longest = maxOf(up.longestStreak, newStreak)

            repository.upsertUserProgress(
                up.copy(
                    streak = newStreak,
                    longestStreak = longest,
                    lastStreakDate = date
                )
            )
        } else if (!allDone && wasAlreadyComplete) {
            // An item was unchecked — mark day incomplete and remove day-completion bonus XP
            repository.upsertDailyRecord(record.copy(dayCompleted = false))
            repository.addXp(-com.levelup.app.domain.model.XpConfig.XP_DAILY_COMPLETE_BONUS)

            // Recalculate streak
            val newStreak = repository.recalculateStreak()
            val up = repository.getUserProgress()

            repository.upsertUserProgress(
                up.copy(
                    streak = newStreak,
                    lastStreakDate = date
                )
            )
        }
    }
}

/**
 * Evaluates and unlocks achievements based on current state.
 */
class EvaluateAchievementsUseCase @Inject constructor(
    private val repository: LevelUpRepository
) {
    suspend operator fun invoke() {
        val up           = repository.getUserProgress()
        val achievements = repository.getAchievements()
        val level        = LevelConfig.levelForTotalXp(up.totalXp)
        val workouts     = repository.getWorkouts()

        val pushupWorkouts = workouts.filter { w ->
            val name = w.name.lowercase()
            name.contains("pushup") || name.contains("push-up") || (name.contains("push") && name.contains("up"))
        }
        val lifetimePushups = pushupWorkouts.sumOf { it.lifetimeAmount }

        suspend fun check(id: String, condition: Boolean) {
            if (condition && achievements.none { it.achievementId == id && it.unlocked }) {
                repository.unlockAchievement(id)
            }
        }

        check("first_quest",  up.lifetimeQuestsCompleted >= 1)
        check("first_level",  level >= 2)
        check("quest_master", up.lifetimeDaysCompleted >= 1)

        // Streak achievements
        check("streak_7",     up.streak >= 7)
        check("streak_30",    up.streak >= 30)
        check("streak_365",   up.streak >= 365)

        // Push-up achievements
        check("pushups_100",  lifetimePushups >= 100)
        check("pushups_250",  lifetimePushups >= 250)
        check("pushups_500",  lifetimePushups >= 500)
        check("pushups_750",  lifetimePushups >= 750)
        check("pushups_1000", lifetimePushups >= 1000)
        check("pushups_1500", lifetimePushups >= 1500)
        check("pushups_2000", lifetimePushups >= 2000)
        check("pushups_5000", lifetimePushups >= 5000)
    }
}
