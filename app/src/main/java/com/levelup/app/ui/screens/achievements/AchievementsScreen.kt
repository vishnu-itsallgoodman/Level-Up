package com.levelup.app.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.levelup.app.data.database.AchievementEntity
import com.levelup.app.ui.components.SectionLabel
import com.levelup.app.ui.theme.*

@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel = hiltViewModel()) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    val unlocked = achievements.count { it.unlocked }
    val total    = achievements.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("ACHIEVEMENTS")
                Text("$unlocked / $total", fontSize = 13.sp, color = AccentGold, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
        }

        items(achievements) { ach ->
            AchievementRow(ach)
        }

        if (achievements.isEmpty()) {
            item {
                Text("Loading achievements...", color = TextMuted, modifier = Modifier.padding(16.dp))
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun AchievementRow(achievement: AchievementEntity) {
    val unlocked = achievement.unlocked
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (unlocked) SurfaceCard else Background)
            .border(
                1.dp,
                if (unlocked) AccentGold.copy(alpha = 0.5f) else Border,
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (unlocked) AccentGold.copy(alpha = 0.15f) else Border.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (unlocked) "🏆" else "🔒",
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                achievement.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) AccentGold else TextMuted
            )
            Text(
                achievement.description,
                fontSize = 12.sp,
                color = if (unlocked) TextSecondary else TextMuted
            )
            if (unlocked && achievement.unlockedDate.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text("Unlocked: ${achievement.unlockedDate}", fontSize = 10.sp, color = TextMuted)
            }
        }
        if (unlocked) {
            Text("✓", fontSize = 16.sp, color = AccentGreen)
        }
    }
}
