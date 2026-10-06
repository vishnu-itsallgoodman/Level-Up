package com.levelup.app.ui.screens.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.levelup.app.data.repository.DailySnapshot
import com.levelup.app.data.repository.HabitProgressUi
import com.levelup.app.data.repository.WorkoutProgressUi
import com.levelup.app.domain.model.PlayerState
import com.levelup.app.ui.components.*
import com.levelup.app.ui.theme.*

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Level-up dialog
    uiState.levelUpEvent?.let { event ->
        LevelUpDialog(event = event, onDismiss = viewModel::dismissLevelUp)
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentBlue)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        SystemHeader(playerState = uiState.playerState)

        // XP Block
        uiState.playerState.let { ps ->
            XpBlock(current = ps.xpIntoLevel, max = ps.xpForNextLevel, totalXp = ps.totalXp)
        }

        // Streak
        StreakRow(streak = uiState.playerState.streak)

        // Today's quests
        uiState.dailySnapshot?.let { snap ->
            QuestsBlock(
                snapshot = snap,
                onToggleWorkout = { id -> viewModel.onToggleWorkout(id) },
                onToggleHabit = { id -> viewModel.onToggleHabit(id) }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SystemHeader(playerState: PlayerState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "⚔  SYSTEM",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            color = AccentBlue
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "LEVEL ${playerState.level}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextPrimary
            )
            Spacer(Modifier.width(12.dp))
            RankBadge(rank = playerState.rank)
        }
    }
}

@Composable
private fun XpBlock(current: Int, max: Int, totalXp: Int) {
    SystemCard {
        SectionLabel("XP")
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$current / $max", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Total: $totalXp", fontSize = 12.sp, color = TextMuted)
        }
        Spacer(Modifier.height(8.dp))
        XpProgressBar(current = current, max = max)
        Spacer(Modifier.height(4.dp))
        val pct = if (max > 0) (current * 100 / max) else 0
        Text("$pct%", fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
private fun StreakRow(streak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        val streakText = when {
            streak <= 0 -> "NO STREAK"
            streak == 1 -> "1 DAY STREAK"
            else -> "$streak DAY STREAK"
        }
        Text(
            text = streakText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (streak > 0) StreakOrange else TextMuted,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun QuestsBlock(
    snapshot: DailySnapshot,
    onToggleWorkout: (Long) -> Unit,
    onToggleHabit: (Long) -> Unit
) {
    val totalActive = snapshot.workouts.size + snapshot.habits.size
    val completed   = snapshot.workouts.count { it.xpAwarded } + snapshot.habits.count { it.xpAwarded }
    val remaining   = totalActive - completed
    val pct         = if (totalActive > 0) (completed * 100 / totalActive) else 0

    SystemCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("TODAY'S QUESTS")
            Text("$pct%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
        }
        Spacer(Modifier.height(4.dp))
        XpProgressBar(current = completed, max = totalActive.coerceAtLeast(1), fillColor = AccentGreen, height = 4.dp)
        Spacer(Modifier.height(12.dp))

        // Workout quests
        snapshot.workouts.forEach { w ->
            WorkoutQuestRow(workout = w, onToggle = onToggleWorkout)
            Spacer(Modifier.height(6.dp))
        }

        if (snapshot.habits.isNotEmpty()) {
            HorizontalDivider(color = Border, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))
        }

        // Habit quests
        snapshot.habits.forEach { h ->
            HabitQuestRow(habit = h, onToggle = onToggleHabit)
            Spacer(Modifier.height(6.dp))
        }

        if (remaining > 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$remaining QUEST${if (remaining > 1) "S" else ""} REMAINING",
                fontSize = 11.sp,
                color = AccentRed,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        } else if (totalActive > 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "ALL QUESTS COMPLETE  ✓",
                fontSize = 11.sp,
                color = AccentGreen,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun WorkoutQuestRow(workout: WorkoutProgressUi, onToggle: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Checkbox(
                checked = workout.xpAwarded,
                onCheckedChange = { onToggle(workout.workoutId) },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentGreen,
                    uncheckedColor = TextMuted,
                    checkmarkColor = Background
                )
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${workout.name} — ${workout.target} ${workout.unit}",
                fontSize = 14.sp,
                color = if (workout.xpAwarded) AccentGreen else TextPrimary
            )
        }
        Text(
            text = "+${workout.xpReward} XP",
            fontSize = 10.sp,
            color = if (workout.xpAwarded) AccentGreen else AccentGold
        )
    }
}

@Composable
private fun HabitQuestRow(habit: HabitProgressUi, onToggle: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Checkbox(
                checked = habit.completed,
                onCheckedChange = { onToggle(habit.habitId) },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentGreen,
                    uncheckedColor = TextMuted,
                    checkmarkColor = Background
                )
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = habit.name,
                fontSize = 14.sp,
                color = if (habit.completed) AccentGreen else TextPrimary
            )
        }
        Text(
            text = "+${habit.xpReward} XP",
            fontSize = 10.sp,
            color = if (habit.completed) AccentGreen else AccentGold
        )
    }
}

@Composable
private fun LevelUpDialog(event: LevelUpEvent, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(1.dp, AccentBlue, RoundedCornerShape(12.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SYSTEM MESSAGE", fontSize = 10.sp, color = AccentBlue, letterSpacing = 3.sp)
            Spacer(Modifier.height(16.dp))
            Text("⬆ LEVEL UP", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AccentGold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text("LEVEL ${event.fromLevel} → ${event.toLevel}", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("+${event.xpGained} XP", fontSize = 14.sp, color = AccentBlue)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("CONTINUE", letterSpacing = 2.sp)
            }
        }
    }
}
