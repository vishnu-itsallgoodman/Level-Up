package com.levelup.app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── UserProgressDao ─────────────────────────────────────────────────────────
@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun observe(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun get(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProgressEntity)

    @Query("UPDATE user_progress SET totalXp = MAX(0, totalXp + :xp) WHERE id = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE user_progress SET lifetimeQuestsCompleted = MAX(0, lifetimeQuestsCompleted + :delta) WHERE id = 1")
    suspend fun addQuestsCompleted(delta: Int)

    @Query("UPDATE user_progress SET lifetimeDaysCompleted = MAX(0, lifetimeDaysCompleted + :delta) WHERE id = 1")
    suspend fun addDaysCompleted(delta: Int)
}

// ─── WorkoutDao ──────────────────────────────────────────────────────────────
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts ORDER BY sortOrder, id")
    suspend fun getAll(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE enabled = 1 ORDER BY sortOrder, id")
    fun observeEnabled(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): WorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<WorkoutEntity>)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─── HabitDao ─────────────────────────────────────────────────────────────────
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY sortOrder, id")
    suspend fun getAll(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE enabled = 1 ORDER BY sortOrder, id")
    fun observeEnabled(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<HabitEntity>)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ─── DailyRecordDao ──────────────────────────────────────────────────────────
@Dao
interface DailyRecordDao {
    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun observeAll(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE date = :date")
    suspend fun getByDate(date: String): DailyRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyRecordEntity)

    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    suspend fun getAll(): List<DailyRecordEntity>
}

// ─── WorkoutProgressDao ───────────────────────────────────────────────────────
@Dao
interface WorkoutProgressDao {
    @Query("SELECT * FROM workout_progress WHERE date = :date")
    fun observeByDate(date: String): Flow<List<WorkoutProgressEntity>>

    @Query("SELECT * FROM workout_progress WHERE date = :date")
    suspend fun getByDate(date: String): List<WorkoutProgressEntity>

    @Query("SELECT * FROM workout_progress WHERE date = :date AND workoutId = :workoutId")
    suspend fun getOne(date: String, workoutId: Long): WorkoutProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WorkoutProgressEntity)
}

// ─── HabitProgressDao ─────────────────────────────────────────────────────────
@Dao
interface HabitProgressDao {
    @Query("SELECT * FROM habit_progress WHERE date = :date")
    fun observeByDate(date: String): Flow<List<HabitProgressEntity>>

    @Query("SELECT * FROM habit_progress WHERE date = :date")
    suspend fun getByDate(date: String): List<HabitProgressEntity>

    @Query("SELECT * FROM habit_progress WHERE date = :date AND habitId = :habitId")
    suspend fun getOne(date: String, habitId: Long): HabitProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HabitProgressEntity)
}

// ─── AchievementDao ──────────────────────────────────────────────────────────
@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY achievementId")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY achievementId")
    suspend fun getAll(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE achievementId = :id")
    suspend fun getById(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AchievementEntity>)
}
