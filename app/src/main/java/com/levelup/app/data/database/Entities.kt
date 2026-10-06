package com.levelup.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─── UserProgress ────────────────────────────────────────────────────────────
@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Int = 0,
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val lastStreakDate: String = "",   // ISO date string yyyy-MM-dd
    val onboardingComplete: Boolean = false,
    // Lifetime totals
    val lifetimeQuestsCompleted: Int = 0,
    val lifetimeDaysCompleted: Int = 0
)

// ─── Workout ──────────────────────────────────────────────────────────────────
/** Dynamic user-configured workout quest. */
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val target: Int,
    val unit: String = "reps",
    val xpReward: Int = 20,
    val enabled: Boolean = true,
    val sortOrder: Int = 0,
    val lifetimeAmount: Int = 0
)

// ─── Habit ───────────────────────────────────────────────────────────────────
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val xpReward: Int = 10,
    val isDaily: Boolean = true,
    val enabled: Boolean = true,
    val sortOrder: Int = 0
)

// ─── DailyRecord ─────────────────────────────────────────────────────────────
/** One row per calendar date. */
@Entity(tableName = "daily_records", primaryKeys = ["date"])
data class DailyRecordEntity(
    val date: String,                    // yyyy-MM-dd
    val xpEarned: Int = 0,
    val dayCompleted: Boolean = false,   // all active quests done
    val streakAtEnd: Int = 0,
    val everCompleted: Boolean = false
)

// ─── WorkoutProgress ─────────────────────────────────────────────────────────
@Entity(tableName = "workout_progress", primaryKeys = ["date", "workoutId"])
data class WorkoutProgressEntity(
    val date: String,
    val workoutId: Long,
    val completed: Boolean = false,
    val xpAwarded: Boolean = false,
    val everCompleted: Boolean = false
)

// ─── HabitProgress ───────────────────────────────────────────────────────────
@Entity(tableName = "habit_progress", primaryKeys = ["date", "habitId"])
data class HabitProgressEntity(
    val date: String,
    val habitId: Long,
    val completed: Boolean = false,
    val xpAwarded: Boolean = false,
    val everCompleted: Boolean = false
)

// ─── Achievement ─────────────────────────────────────────────────────────────
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val achievementId: String,
    val title: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockedDate: String = "",
    val iconName: String = ""
)
