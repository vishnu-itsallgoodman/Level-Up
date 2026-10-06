package com.levelup.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProgressEntity::class,
        WorkoutEntity::class,
        HabitEntity::class,
        DailyRecordEntity::class,
        WorkoutProgressEntity::class,
        HabitProgressEntity::class,
        AchievementEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LevelUpDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun habitDao(): HabitDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun workoutProgressDao(): WorkoutProgressDao
    abstract fun habitProgressDao(): HabitProgressDao
    abstract fun achievementDao(): AchievementDao
}
