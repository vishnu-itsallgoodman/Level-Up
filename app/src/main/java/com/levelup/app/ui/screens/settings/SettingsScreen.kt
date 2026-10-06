package com.levelup.app.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.levelup.app.data.database.HabitEntity
import com.levelup.app.data.database.WorkoutEntity
import com.levelup.app.ui.components.*
import com.levelup.app.ui.theme.*

private val ALLOWED_XP_OPTIONS = listOf(10, 20, 25, 50)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showResetTodayDialog  by remember { mutableStateOf(false) }
    var showResetAllDialog    by remember { mutableStateOf(false) }

    var editingWorkout        by remember { mutableStateOf<WorkoutEntity?>(null) }
    var addingWorkout         by remember { mutableStateOf(false) }

    var editingHabit          by remember { mutableStateOf<HabitEntity?>(null) }
    var addingHabit           by remember { mutableStateOf(false) }

    if (showResetTodayDialog) {
        ConfirmDialog(
            title = "RESET TODAY",
            body  = "Reset all of today's quest progress? XP earned today will be removed.",
            onConfirm = {
                viewModel.resetTodayProgress()
                showResetTodayDialog = false
            },
            onDismiss = { showResetTodayDialog = false }
        )
    }
    if (showResetAllDialog) {
        ConfirmDialog(
            title = "RESET ALL PROGRESS",
            body  = "This will permanently erase ALL progress, XP, levels, and streaks. This cannot be undone.",
            confirmText = "RESET EVERYTHING",
            confirmColor = AccentRed,
            onConfirm = {
                viewModel.resetAllProgress()
                showResetAllDialog = false
            },
            onDismiss = { showResetAllDialog = false }
        )
    }

    // Workout dialogs
    editingWorkout?.let { workout ->
        WorkoutEditDialog(
            workout = workout,
            onSave = { viewModel.upsertWorkout(it); editingWorkout = null },
            onDismiss = { editingWorkout = null }
        )
    }
    if (addingWorkout) {
        WorkoutEditDialog(
            workout = WorkoutEntity(name = "", target = 50, unit = "reps", xpReward = 20),
            onSave = { viewModel.upsertWorkout(it); addingWorkout = false },
            onDismiss = { addingWorkout = false }
        )
    }

    // Habit dialogs
    editingHabit?.let { habit ->
        HabitEditDialog(
            habit = habit,
            onSave = { viewModel.upsertHabit(it); editingHabit = null },
            onDismiss = { editingHabit = null }
        )
    }
    if (addingHabit) {
        HabitEditDialog(
            habit = HabitEntity(name = "", xpReward = 10),
            onSave = { viewModel.upsertHabit(it); addingHabit = false },
            onDismiss = { addingHabit = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── Workout Quests ──────────────────────────────────────────────────
        SystemCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("WORKOUT QUESTS")
                IconButton(onClick = { addingWorkout = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add workout quest", tint = AccentBlue)
                }
            }
            Spacer(Modifier.height(8.dp))
            uiState.workouts.forEach { workout ->
                WorkoutRow(
                    workout = workout,
                    onEdit = { editingWorkout = it },
                    onDelete = { viewModel.deleteWorkout(it.id) },
                    onToggle = { viewModel.toggleWorkoutEnabled(it) }
                )
                Spacer(Modifier.height(4.dp))
            }
            if (uiState.workouts.isEmpty()) {
                Text("No workout quests yet.", color = TextMuted, fontSize = 13.sp)
            }
        }

        // ─── Habits ──────────────────────────────────────────────────────────
        SystemCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("MY HABITS")
                IconButton(onClick = { addingHabit = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add habit", tint = AccentBlue)
                }
            }
            Spacer(Modifier.height(8.dp))
            uiState.habits.forEach { habit ->
                HabitRow(
                    habit  = habit,
                    onEdit = { editingHabit = it },
                    onDelete = { viewModel.deleteHabit(it.id) },
                    onToggle = { viewModel.toggleHabitEnabled(it) }
                )
                Spacer(Modifier.height(4.dp))
            }
            if (uiState.habits.isEmpty()) {
                Text("No habits yet.", color = TextMuted, fontSize = 13.sp)
            }
        }

        // ─── Danger zone ─────────────────────────────────────────────────────
        SystemCard {
            SectionLabel("DANGER ZONE")
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showResetTodayDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                border = BorderStroke(1.dp, AccentRed)
            ) {
                Text("RESET TODAY'S PROGRESS", letterSpacing = 1.sp, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { showResetAllDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("RESET ALL PROGRESS", letterSpacing = 1.sp, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─── XP Reward Selector ──────────────────────────────────────────────────────
@Composable
private fun XpRewardSelector(
    selectedXp: Int,
    onSelectXp: (Int) -> Unit
) {
    Column {
        Text("XP REWARD", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ALLOWED_XP_OPTIONS.forEach { xp ->
                val isSelected = (selectedXp == xp)
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectXp(xp) },
                    label = {
                        Text(
                            "$xp XP",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlue,
                        selectedLabelColor = TextPrimary,
                        containerColor = Background,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Border,
                        selectedBorderColor = AccentBlue
                    )
                )
            }
        }
    }
}

// ─── Workout Row ──────────────────────────────────────────────────────────────
@Composable
private fun WorkoutRow(
    workout: WorkoutEntity,
    onEdit: (WorkoutEntity) -> Unit,
    onDelete: (WorkoutEntity) -> Unit,
    onToggle: (WorkoutEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (workout.enabled) SurfaceCard else Background.copy(alpha = 0.5f))
            .border(1.dp, Border, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(workout.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (workout.enabled) TextPrimary else TextMuted)
            Text("${workout.target} ${workout.unit} • +${workout.xpReward.coerceAtMost(50)} XP", fontSize = 11.sp, color = AccentGold)
        }
        Switch(
            checked = workout.enabled,
            onCheckedChange = { onToggle(workout) },
            colors = SwitchDefaults.colors(checkedThumbColor = AccentGreen, checkedTrackColor = AccentGreen.copy(alpha = 0.3f))
        )
        IconButton(onClick = { onEdit(workout) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit workout", tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = { onDelete(workout) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete workout", tint = AccentRed, modifier = Modifier.size(16.dp))
        }
    }
}

// ─── Workout Edit Dialog ──────────────────────────────────────────────────────
@Composable
private fun WorkoutEditDialog(
    workout: WorkoutEntity,
    onSave: (WorkoutEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name     by remember { mutableStateOf(workout.name) }
    var target   by remember { mutableStateOf(workout.target.toString()) }
    var unit     by remember { mutableStateOf(workout.unit) }
    var xpReward by remember { mutableIntStateOf(if (workout.xpReward in ALLOWED_XP_OPTIONS) workout.xpReward else 20) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (workout.id == 0L) "ADD WORKOUT QUEST" else "EDIT WORKOUT QUEST",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp, color = AccentBlue
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Workout Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentBlue, unfocusedBorderColor = Border,
                    focusedLabelColor = AccentBlue, unfocusedLabelColor = TextMuted
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.filter(Char::isDigit) },
                    label = { Text("Target") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue, unfocusedBorderColor = Border,
                        focusedLabelColor = AccentBlue, unfocusedLabelColor = TextMuted
                    )
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g. reps, min)") },
                    modifier = Modifier.weight(1.2f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue, unfocusedBorderColor = Border,
                        focusedLabelColor = AccentBlue, unfocusedLabelColor = TextMuted
                    )
                )
            }

            // Fixed XP options selector
            XpRewardSelector(
                selectedXp = xpReward,
                onSelectXp = { xpReward = it }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, Border)
                ) { Text("CANCEL") }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(workout.copy(
                                name = name.trim(),
                                target = target.toIntOrNull() ?: 50,
                                unit = if (unit.isNotBlank()) unit.trim() else "reps",
                                xpReward = xpReward.coerceIn(10, 50)
                            ))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("SAVE") }
            }
        }
    }
}

// ─── Habit Row ────────────────────────────────────────────────────────────────
@Composable
private fun HabitRow(
    habit: HabitEntity,
    onEdit: (HabitEntity) -> Unit,
    onDelete: (HabitEntity) -> Unit,
    onToggle: (HabitEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (habit.enabled) SurfaceCard else Background.copy(alpha = 0.5f))
            .border(1.dp, Border, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(habit.name, fontSize = 14.sp, color = if (habit.enabled) TextPrimary else TextMuted)
            Text("+${habit.xpReward.coerceAtMost(50)} XP", fontSize = 11.sp, color = AccentGold)
        }
        Switch(
            checked = habit.enabled,
            onCheckedChange = { onToggle(habit) },
            colors = SwitchDefaults.colors(checkedThumbColor = AccentGreen, checkedTrackColor = AccentGreen.copy(alpha = 0.3f))
        )
        IconButton(onClick = { onEdit(habit) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit habit", tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = { onDelete(habit) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete habit", tint = AccentRed, modifier = Modifier.size(16.dp))
        }
    }
}

// ─── Habit Edit Dialog ────────────────────────────────────────────────────────
@Composable
private fun HabitEditDialog(
    habit: HabitEntity,
    onSave: (HabitEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name     by remember { mutableStateOf(habit.name) }
    var xpReward by remember { mutableIntStateOf(if (habit.xpReward in ALLOWED_XP_OPTIONS) habit.xpReward else 10) }
    var daily    by remember { mutableStateOf(habit.isDaily) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (habit.id == 0L) "ADD HABIT" else "EDIT HABIT",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp, color = AccentBlue
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentBlue, unfocusedBorderColor = Border,
                    focusedLabelColor = AccentBlue, unfocusedLabelColor = TextMuted
                )
            )

            // Fixed XP options selector
            XpRewardSelector(
                selectedXp = xpReward,
                onSelectXp = { xpReward = it }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Daily Habit", color = TextPrimary, modifier = Modifier.weight(1f))
                Switch(checked = daily, onCheckedChange = { daily = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentGreen))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, Border)
                ) { Text("CANCEL") }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(habit.copy(
                                name = name.trim(),
                                xpReward = xpReward.coerceIn(10, 50),
                                isDaily = daily
                            ))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) { Text("SAVE") }
            }
        }
    }
}

// ─── Confirm Dialog ───────────────────────────────────────────────────────────
@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    confirmText: String = "CONFIRM",
    confirmColor: androidx.compose.ui.graphics.Color = AccentBlue,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(1.dp, AccentRed, RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = AccentRed)
            Text(body, fontSize = 13.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, Border)
                ) { Text("CANCEL") }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                ) { Text(confirmText, fontSize = 11.sp) }
            }
        }
    }
}
