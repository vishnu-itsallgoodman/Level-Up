package com.levelup.app.domain.usecase

import com.levelup.app.data.repository.LevelUpRepository
import com.levelup.app.domain.model.*
import javax.inject.Inject

/** Computes RPG stats from lifetime progress data. */
class ComputeStatsUseCase @Inject constructor(
    private val repository: LevelUpRepository
) {
    suspend operator fun invoke(): List<StatAttribute> {
        val up = repository.getUserProgress()
        val allRecords = repository.getAllDailyRecords()
        val workouts = repository.getWorkouts()
        val completedDays = allRecords.count { it.dayCompleted }

        // Strength: from workout completions
        val totalLifts = workouts.sumOf { it.lifetimeAmount }
        val strength = (totalLifts / 10).coerceAtMost(100)

        // Agility: from completed days
        val agility = (completedDays * 2).coerceAtMost(100)

        // Vitality: from current level
        val level = LevelConfig.levelForTotalXp(up.totalXp)
        val vitality = (level * 3).coerceAtMost(100)

        // Intelligence: from lifetime quests
        val intelligence = (up.lifetimeQuestsCompleted / 5).coerceAtMost(100)

        // Discipline: from streak and completed days
        val discipline = (up.longestStreak * 2 + completedDays).coerceAtMost(100)

        return listOf(
            StatAttribute("STRENGTH",     strength,     100),
            StatAttribute("AGILITY",      agility,      100),
            StatAttribute("VITALITY",     vitality,     100),
            StatAttribute("INTELLIGENCE", intelligence, 100),
            StatAttribute("DISCIPLINE",   discipline,   100)
        )
    }
}
