package com.levelup.app.domain.model

// ─── Ranks ──────────────────────────────────────────────────────────────────
enum class Rank(val label: String, val minLevel: Int) {
    E("E", 1),
    D("D", 10),
    C("C", 20),
    B("B", 30),
    A("A", 40),
    S("S", 50);

    companion object {
        fun forLevel(level: Int): Rank =
            entries.reversed().firstOrNull { level >= it.minLevel } ?: E
    }
}

// ─── Level / XP config ──────────────────────────────────────────────────────
object LevelConfig {
    /** XP needed to advance from [level] to level+1. Formula: base * level */
    const val BASE_XP: Int = 100

    fun xpForLevel(level: Int): Int = BASE_XP * level

    fun levelForTotalXp(totalXp: Int): Int {
        var level = 1
        var consumed = 0
        while (true) {
            val needed = xpForLevel(level)
            if (consumed + needed > totalXp) break
            consumed += needed
            level++
        }
        return level
    }

    fun xpWithinCurrentLevel(totalXp: Int): Int {
        var level = 1
        var consumed = 0
        while (true) {
            val needed = xpForLevel(level)
            if (consumed + needed > totalXp) return totalXp - consumed
            consumed += needed
            level++
        }
    }
}

// ─── Stat attribute ─────────────────────────────────────────────────────────
data class StatAttribute(
    val name: String,
    val value: Int,
    val maxValue: Int = 100
)

// ─── Player snapshot ────────────────────────────────────────────────────────
data class PlayerState(
    val level: Int,
    val rank: Rank,
    val totalXp: Int,
    val xpIntoLevel: Int,
    val xpForNextLevel: Int,
    val streak: Int,
    val longestStreak: Int
)
