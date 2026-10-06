package com.levelup.app.ui.screens.onboarding

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.levelup.app.data.database.WorkoutEntity
import com.levelup.app.ui.components.SystemCard
import com.levelup.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()

    var phase by remember { mutableIntStateOf(0) } // 0 = intro, 1 = configure, 2 = done

    when (phase) {
        0 -> OnboardingIntroPhase(onContinue = { phase = 1 })
        1 -> OnboardingConfigPhase(
            workouts = workouts,
            onTargetChange = viewModel::updateTarget,
            onBegin = {
                viewModel.completeOnboarding()
                phase = 2
            }
        )
        2 -> OnboardingDonePhase(onProceed = onComplete)
    }
}

@Composable
private fun OnboardingIntroPhase(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚔", fontSize = 48.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            "SYSTEM INITIALIZED",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            color = AccentBlue,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Welcome, Player.",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Configure your Daily Quests\nand begin your journey.",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("CONFIGURE QUESTS", letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OnboardingConfigPhase(
    workouts: List<WorkoutEntity>,
    onTargetChange: (Long, Int) -> Unit,
    onBegin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "DAILY QUEST TARGETS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            color = AccentBlue
        )
        Text(
            "Set your starting targets. You can adjust, add, or remove these any time in Settings.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        SystemCard {
            workouts.forEach { w ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(w.name, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(w.unit, fontSize = 11.sp, color = TextMuted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onTargetChange(w.id, w.target - 5) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextSecondary)
                        }
                        Text(
                            "${w.target}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            modifier = Modifier.widthIn(min = 48.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { onTargetChange(w.id, w.target + 5) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = AccentBlue)
                        }
                    }
                }
            }
        }

        // Default habits preview
        SystemCard {
            Text("DEFAULT HABITS", fontSize = 11.sp, letterSpacing = 2.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            listOf(
                "Brush Teeth – Morning  +10 XP",
                "Brush Teeth – Night  +10 XP",
                "Shower  +10 XP",
                "Drink Water  +10 XP",
                "Sleep Goal  +15 XP",
                "Study  +20 XP",
                "Walk  +15 XP"
            ).forEach { line ->
                Text("• $line", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 2.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onBegin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("BEGIN", letterSpacing = 4.sp, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OnboardingDonePhase(onProceed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚔", fontSize = 48.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            "SYSTEM INITIALIZED",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            color = AccentBlue,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Daily Quests generated.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Start your journey.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("ENTER THE SYSTEM", letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        }
    }
}
