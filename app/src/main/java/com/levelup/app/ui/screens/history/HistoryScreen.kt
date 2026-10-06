package com.levelup.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.levelup.app.data.database.DailyRecordEntity
import com.levelup.app.domain.model.LevelConfig
import com.levelup.app.ui.components.*
import com.levelup.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val up = uiState.userProgress
    val records = uiState.records
    val workouts = uiState.workouts

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Lifetime stats
        if (up != null) {
            item {
                SystemCard {
                    SectionLabel("LIFETIME STATISTICS")
                    Spacer(Modifier.height(12.dp))
                    workouts.forEach { w ->
                        StatLine("Total ${w.name}", "${w.lifetimeAmount} ${w.unit}")
                    }
                    StatLine("Total XP Earned", "${up.totalXp}")
                    StatLine("Total Quests", "${up.lifetimeQuestsCompleted}")
                    StatLine("Days Completed", "${up.lifetimeDaysCompleted}")
                    StatLine("Longest Streak", "${up.longestStreak} days")
                    StatLine("Current Level", "${LevelConfig.levelForTotalXp(up.totalXp)}")
                }
            }
        }

        // Recent 30 days calendar row
        item {
            SystemCard {
                SectionLabel("RECENT DAYS")
                Spacer(Modifier.height(12.dp))
                WeekCalendarRow(records = records.take(30))
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun WeekCalendarRow(records: List<DailyRecordEntity>) {
    val daysFmt = DateTimeFormatter.ofPattern("EEE")
    val today = LocalDate.now()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val recordMap = records.associateBy { it.date }
        (-13..0).forEach { offset ->
            val day = today.plusDays(offset.toLong())
            val dateStr = day.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val rec = recordMap[dateStr]
            val done = rec?.dayCompleted ?: false
            val isToday = offset == 0

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    day.format(daysFmt).take(1),
                    fontSize = 8.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isToday && done -> AccentGreen
                                done -> AccentBlue.copy(alpha = 0.6f)
                                isToday -> Border
                                else -> SurfaceCard
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (done) "✓" else "·",
                        fontSize = 10.sp,
                        color = if (done) Background else TextMuted
                    )
                }
            }
        }
    }
}
