package com.levelup.app.data.repository

import com.levelup.app.data.database.*
import com.levelup.app.domain.model.*
import com.levelup.app.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// ─── Data Transfer Objects used by higher layers ─────────────────────────────

data class WorkoutProgressUi(
    val workoutId: Long,
    val name: String,
    val target: Int,
    val unit: String,
    val completed: Boolean,
    val xpAwarded: Boolean,
    val xpReward: Int,
    val enabled: Boolean
)

data class HabitProgressUi(
    val habitId: Long,
    val name: String,
    val xpReward: Int,
    val completed: Boolean,
    val xpAwarded: Boolean,
    val enabled: Boolean
)

data class DailySnapshot(
    val date: String,
    val workouts: List<WorkoutProgressUi>,
    val habits: List<HabitProgressUi>,
    val totalXpEarned: Int,
    val dayCompleted: Boolean
)

// ─── Repository ──────────────────────────────────────────────────────────────
@Singleton
class LevelUpRepository @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val workoutDao: WorkoutDao,
    private val habitDao: HabitDao,
    private val dailyRecordDao: DailyRecordDao,
    private val workoutProgressDao: WorkoutProgressDao,
    private val habitProgressDao: HabitProgressDao,
    private val achievementDao: AchievementDao
) {
    // ── User progress ─────────────────────────────────────────────────────────
    fun observeUserProgress(): Flow<UserProgressEntity?> =
        combine(
            userProgressDao.observe(),
            workoutProgressDao.observeByDate(DateUtils.today()),
            habitProgressDao.observeByDate(DateUtils.today()),
            dailyRecordDao.observeAll()
        ) { up, _, _, _ ->
            if (up == null) null
            else {
                val totalQuests = workoutProgressDao.getTotalCompletedCount() + habitProgressDao.getTotalCompletedCount()
                val totalDays = dailyRecordDao.getTotalCompletedDaysCount()
                up.copy(lifetimeQuestsCompleted = totalQuests, lifetimeDaysCompleted = totalDays)
            }
        }

    suspend fun getUserProgress(): UserProgressEntity {
        val up = userProgressDao.get() ?: UserProgressEntity().also { userProgressDao.upsert(it) }
        val totalQuests = workoutProgressDao.getTotalCompletedCount() + habitProgressDao.getTotalCompletedCount()
        val totalDays = dailyRecordDao.getTotalCompletedDaysCount()
        return up.copy(lifetimeQuestsCompleted = totalQuests, lifetimeDaysCompleted = totalDays)
    }

    suspend fun addXp(xp: Int) {
        userProgressDao.addXp(xp)
    }

    suspend fun upsertUserProgress(entity: UserProgressEntity) {
        userProgressDao.upsert(entity)
    }

    // ── Onboarding ────────────────────────────────────────────────────────────
    suspend fun isOnboardingComplete(): Boolean =
        userProgressDao.get()?.onboardingComplete ?: false

    suspend fun completeOnboarding() {
        val current = getUserProgress()
        userProgressDao.upsert(current.copy(onboardingComplete = true))
    }

    // ── Workouts ──────────────────────────────────────────────────────────────
    fun observeWorkouts(): Flow<List<WorkoutEntity>> =
        combine(
            workoutDao.observeAll(),
            workoutProgressDao.observeByDate(DateUtils.today())
        ) { list, _ ->
            list.map { w ->
                val count = workoutProgressDao.getCompletedCountForWorkout(w.id)
                w.copy(lifetimeAmount = count * w.target)
            }
        }

    fun observeEnabledWorkouts(): Flow<List<WorkoutEntity>> = workoutDao.observeEnabled()

    suspend fun getWorkouts(): List<WorkoutEntity> {
        val list = workoutDao.getAll()
        return list.map { w ->
            val count = workoutProgressDao.getCompletedCountForWorkout(w.id)
            w.copy(lifetimeAmount = count * w.target)
        }
    }

    suspend fun getWorkoutById(id: Long): WorkoutEntity? {
        val w = workoutDao.getById(id) ?: return null
        val count = workoutProgressDao.getCompletedCountForWorkout(w.id)
        return w.copy(lifetimeAmount = count * w.target)
    }

    suspend fun upsertWorkout(entity: WorkoutEntity): Long = workoutDao.upsert(entity)

    suspend fun deleteWorkout(id: Long) {
        workoutDao.deleteById(id)
    }

    suspend fun seedWorkoutsIfEmpty() {
        if (workoutDao.getAll().isEmpty()) {
            val defaults = listOf(
                WorkoutEntity(name = "Push-ups", target = 50, unit = "reps", xpReward = 20, sortOrder = 0),
                WorkoutEntity(name = "Sit-ups", target = 50, unit = "reps", xpReward = 20, sortOrder = 1),
                WorkoutEntity(name = "Crunches", target = 50, unit = "reps", xpReward = 20, sortOrder = 2)
            )
            workoutDao.upsertAll(defaults)
        }
    }

    // ── Habits ───────────────────────────────────────────────────────────────
    fun observeHabits(): Flow<List<HabitEntity>> = habitDao.observeAll()
    fun observeEnabledHabits(): Flow<List<HabitEntity>> = habitDao.observeEnabled()

    suspend fun getHabits(): List<HabitEntity> = habitDao.getAll()

    suspend fun upsertHabit(entity: HabitEntity): Long = habitDao.upsert(entity)

    suspend fun deleteHabit(id: Long) {
        habitDao.deleteById(id)
    }

    suspend fun seedHabitsIfEmpty() {
        if (habitDao.getAll().isEmpty()) {
            val defaults = listOf(
                HabitEntity(name = "Brush Teeth – Morning", xpReward = 10, sortOrder = 0),
                HabitEntity(name = "Brush Teeth – Night",   xpReward = 10, sortOrder = 1),
                HabitEntity(name = "Shower",                xpReward = 10, sortOrder = 2),
                HabitEntity(name = "Drink Water",           xpReward = 10, sortOrder = 3),
                HabitEntity(name = "Sleep Goal",            xpReward = 20, sortOrder = 4),
                HabitEntity(name = "Study",                 xpReward = 20, sortOrder = 5),
                HabitEntity(name = "Walk",                  xpReward = 20, sortOrder = 6)
            )
            habitDao.upsertAll(defaults)
        }
    }

    // ── Daily records ─────────────────────────────────────────────────────────
    fun observeAllDailyRecords(): Flow<List<DailyRecordEntity>> =
        dailyRecordDao.observeAll()

    suspend fun getOrCreateDailyRecord(date: String): DailyRecordEntity =
        dailyRecordDao.getByDate(date)
            ?: DailyRecordEntity(date = date).also { dailyRecordDao.upsert(it) }

    suspend fun upsertDailyRecord(entity: DailyRecordEntity) {
        dailyRecordDao.upsert(entity)
    }

    suspend fun getRecentDailyRecords(limit: Int): List<DailyRecordEntity> =
        dailyRecordDao.getRecent(limit)

    suspend fun getAllDailyRecords(): List<DailyRecordEntity> =
        dailyRecordDao.getAll()

    // ── Workout progress ─────────────────────────────────────────────────────
    fun observeWorkoutProgressForDate(date: String): Flow<List<WorkoutProgressEntity>> =
        workoutProgressDao.observeByDate(date)

    suspend fun getWorkoutProgressForDate(date: String): List<WorkoutProgressEntity> =
        workoutProgressDao.getByDate(date)

    suspend fun upsertWorkoutProgress(entity: WorkoutProgressEntity) {
        workoutProgressDao.upsert(entity)
    }

    suspend fun ensureWorkoutProgressForDate(date: String) {
        val workouts = workoutDao.getAll().filter { it.enabled }
        val existing = workoutProgressDao.getByDate(date).map { it.workoutId }.toSet()
        workouts.filter { it.id !in existing }.forEach { w ->
            workoutProgressDao.upsert(
                WorkoutProgressEntity(
                    date = date,
                    workoutId = w.id,
                    completed = false,
                    xpAwarded = false
                )
            )
        }
    }

    // ── Habit progress ────────────────────────────────────────────────────────
    fun observeHabitProgressForDate(date: String): Flow<List<HabitProgressEntity>> =
        habitProgressDao.observeByDate(date)

    suspend fun getHabitProgressForDate(date: String): List<HabitProgressEntity> =
        habitProgressDao.getByDate(date)

    suspend fun upsertHabitProgress(entity: HabitProgressEntity) {
        habitProgressDao.upsert(entity)
    }

    suspend fun ensureHabitProgressForDate(date: String) {
        val habits = habitDao.getAll().filter { it.enabled }
        val existing = habitProgressDao.getByDate(date).map { it.habitId }.toSet()
        habits.filter { it.id !in existing }.forEach { h ->
            habitProgressDao.upsert(
                HabitProgressEntity(date = date, habitId = h.id)
            )
        }
    }

    // ── Achievements ──────────────────────────────────────────────────────────
    fun observeAchievements(): Flow<List<AchievementEntity>> = achievementDao.observeAll()

    suspend fun getAchievements(): List<AchievementEntity> = achievementDao.getAll()

    suspend fun unlockAchievement(achievementId: String) {
        val existing = achievementDao.getById(achievementId) ?: return
        if (!existing.unlocked) {
            achievementDao.upsert(
                existing.copy(unlocked = true, unlockedDate = DateUtils.today())
            )
        }
    }

    suspend fun seedAchievementsIfEmpty() {
        val defaults = listOf(
            AchievementEntity("first_quest",   "First Quest",      "Complete your first quest."),
            AchievementEntity("first_level",   "First Level",      "Reach level 2."),
            AchievementEntity("quest_master",  "Quest Master",     "Complete every daily quest for a day."),

            // Streak achievements
            AchievementEntity("streak_7",      "1 Week Streak",    "Maintain a 7-day streak."),
            AchievementEntity("streak_30",     "1 Month Streak",   "Maintain a 30-day streak."),
            AchievementEntity("streak_365",    "1 Year Streak",    "Maintain a 365-day streak."),

            // Push-up achievements
            AchievementEntity("pushups_100",   "100 Push-ups",     "Complete 100 lifetime push-ups."),
            AchievementEntity("pushups_250",   "250 Push-ups",     "Complete 250 lifetime push-ups."),
            AchievementEntity("pushups_500",   "500 Push-ups",     "Complete 500 lifetime push-ups."),
            AchievementEntity("pushups_750",   "750 Push-ups",     "Complete 750 lifetime push-ups."),
            AchievementEntity("pushups_1000",  "1,000 Push-ups",   "Complete 1,000 lifetime push-ups."),
            AchievementEntity("pushups_1500",  "1,500 Push-ups",   "Complete 1,500 lifetime push-ups."),
            AchievementEntity("pushups_2000",  "2,000 Push-ups",   "Complete 2,000 lifetime push-ups."),
            AchievementEntity("pushups_5000",  "5,000 Push-ups",   "Complete 5,000 lifetime push-ups.")
        )
        val existing = achievementDao.getAll().associateBy { it.achievementId }
        defaults.forEach { defaultAch ->
            if (existing[defaultAch.achievementId] == null) {
                achievementDao.upsert(defaultAch)
            }
        }
    }

    // ── Composed snapshot ─────────────────────────────────────────────────────
    suspend fun buildDailySnapshot(date: String): DailySnapshot {
        val workouts = workoutDao.getAll().filter { it.enabled }
        val habits   = habitDao.getAll().filter { it.enabled }
        val wProgs   = workoutProgressDao.getByDate(date)
        val hProgs   = habitProgressDao.getByDate(date)

        val wProgMap = wProgs.associateBy { it.workoutId }
        val hProgMap = hProgs.associateBy { it.habitId }

        val workoutUis = workouts.map { w ->
            val prog = wProgMap[w.id]
            WorkoutProgressUi(
                workoutId = w.id,
                name      = w.name,
                target    = w.target,
                unit      = w.unit,
                completed = prog?.completed ?: false,
                xpAwarded = prog?.xpAwarded ?: false,
                xpReward  = w.xpReward.coerceAtMost(50),
                enabled   = w.enabled
            )
        }

        val habitUis = habits.map { h ->
            val prog = hProgMap[h.id]
            HabitProgressUi(
                habitId   = h.id,
                name      = h.name,
                xpReward  = h.xpReward.coerceAtMost(50),
                completed = prog?.completed ?: false,
                xpAwarded = prog?.xpAwarded ?: false,
                enabled   = h.enabled
            )
        }

        val dailyRecord = dailyRecordDao.getByDate(date)
        val totalXp = workoutUis.sumOf { if (it.xpAwarded) it.xpReward else 0 } +
                      habitUis.sumOf { if (it.xpAwarded) it.xpReward else 0 }

        return DailySnapshot(
            date          = date,
            workouts      = workoutUis,
            habits        = habitUis,
            totalXpEarned = totalXp,
            dayCompleted  = dailyRecord?.dayCompleted ?: false
        )
    }

    // ── Streak ────────────────────────────────────────────────────────────────
    suspend fun recalculateStreak(): Int {
        val records = dailyRecordDao.getAll().associateBy { it.date }
        val today = LocalDate.now()
        val todayStr = DateUtils.formatDate(today)

        val todayCompleted = records[todayStr]?.dayCompleted == true

        var streak = 0
        var checkDate = if (todayCompleted) today else today.minusDays(1)

        while (true) {
            val dateStr = DateUtils.formatDate(checkDate)
            val record = records[dateStr]
            if (record?.dayCompleted == true) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    // ── Reset ─────────────────────────────────────────────────────────────────
    suspend fun resetTodayProgress(date: String) {
        val wProgs   = workoutProgressDao.getByDate(date)
        val hProgs   = habitProgressDao.getByDate(date)
        val record   = dailyRecordDao.getByDate(date)
        val workouts = workoutDao.getAll().associateBy { it.id }
        val habits   = habitDao.getAll().associateBy { it.id }

        var xpToRemove = 0
        wProgs.forEach { wp ->
            if (wp.xpAwarded) {
                val w = workouts[wp.workoutId]
                if (w != null) {
                    xpToRemove += w.xpReward
                }
            }
            workoutProgressDao.upsert(wp.copy(completed = false, xpAwarded = false))
        }
        hProgs.forEach { hp ->
            if (hp.xpAwarded) {
                xpToRemove += habits[hp.habitId]?.xpReward ?: 0
            }
            habitProgressDao.upsert(hp.copy(completed = false, xpAwarded = false))
        }

        if (record != null) {
            dailyRecordDao.upsert(record.copy(xpEarned = 0, dayCompleted = false))
        }

        val newStreak = recalculateStreak()
        val up = getUserProgress()
        val newXp = maxOf(0, up.totalXp - xpToRemove)
        userProgressDao.upsert(up.copy(totalXp = newXp, streak = newStreak))
    }
}
