package com.levelup.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.levelup.app.domain.model.StatAttribute
import com.levelup.app.ui.components.*
import com.levelup.app.ui.theme.*

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CHARACTER ATTRIBUTES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            color = AccentBlue
        )

        SystemCard {
            SectionLabel("BASE STATS")
            Spacer(Modifier.height(16.dp))
            uiState.stats.forEach { attr ->
                StatEntry(attr)
                Spacer(Modifier.height(16.dp))
            }
        }

        StatDescriptionCard()

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatEntry(attr: StatAttribute) {
    val color = when (attr.name) {
        "STRENGTH"     -> AccentRed
        "AGILITY"      -> AccentGreen
        "VITALITY"     -> AccentBlue
        "INTELLIGENCE" -> AccentPurple
        "DISCIPLINE"   -> AccentGold
        else           -> AccentBlue
    }
    StatBar(
        label = attr.name,
        value = attr.value,
        max   = attr.maxValue,
        color = color
    )
}

@Composable
private fun StatDescriptionCard() {
    SystemCard {
        SectionLabel("HOW STATS GROW")
        Spacer(Modifier.height(12.dp))
        StatDesc("STRENGTH",     "Increased through push-ups, sit-ups, crunches")
        StatDesc("AGILITY",      "Increased through consistent daily completion")
        StatDesc("VITALITY",     "Increases with your level and self-care habits")
        StatDesc("INTELLIGENCE", "Increased through study and quest completions")
        StatDesc("DISCIPLINE",   "Driven by streaks and completed days")
    }
}

@Composable
private fun StatDesc(label: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, color = AccentBlue, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(desc, fontSize = 13.sp, color = TextSecondary)
    }
}
