package com.levelup.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background as composeBackground
import androidx.compose.foundation.border as composeBorder
import androidx.compose.foundation.layout.Arrangement as ComposeArrangement
import androidx.compose.foundation.layout.Box as ComposeBox
import androidx.compose.foundation.layout.Column as ComposeColumn
import androidx.compose.foundation.layout.Row as ComposeRow
import androidx.compose.foundation.layout.Spacer as ComposeSpacer
import androidx.compose.foundation.layout.fillMaxWidth as composeFillMaxWidth
import androidx.compose.foundation.layout.height as composeHeight
import androidx.compose.foundation.layout.padding as composePadding
import androidx.compose.foundation.layout.width as composeWidth
import androidx.compose.foundation.shape.RoundedCornerShape as ComposeRoundedCornerShape
import androidx.compose.material3.Text as ComposeText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment as ComposeAlignment
import androidx.compose.ui.Modifier as ComposeModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight as ComposeFontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.*
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.levelup.app.MainActivity
import com.levelup.app.data.database.LevelUpDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Widget state keys ────────────────────────────────────────────────────────
val WIDGET_STREAK     = intPreferencesKey("widget_streak")
val WIDGET_DAY_PCT    = intPreferencesKey("widget_day_pct")
val WIDGET_QUEST_LIST = stringPreferencesKey("widget_quest_list")

// ─── Color Palette (Dark Holographic RPG System Theme) ──────────────────────
private val ColorVoidBg         = Color(0xFF0B0C10)
private val ColorCyanAccent     = Color(0xFF00E5FF)
private val ColorCyanMuted      = Color(0xFF00B4D8)
private val ColorGreenDone      = Color(0xFF00FF9D)
private val ColorTextMuted      = Color(0xFF7090B0)
private val ColorTextIncomplete = Color(0xFF8A99AD)
private val ColorUncheckedBox   = Color(0xFF3A4759)
private val ColorStreakOrange   = Color(0xFFFF8C00)
private val ColorTrackBg        = Color(0xFF1A2634)

// ─── Widget ───────────────────────────────────────────────────────────────────
class LevelUpWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @Composable
    private fun WidgetContent() {
        val prefs     = currentState<Preferences>()
        val streak    = prefs[WIDGET_STREAK]     ?: 0
        val pct       = prefs[WIDGET_DAY_PCT]    ?: 0
        val rawQuests = prefs[WIDGET_QUEST_LIST] ?: ""
        val questList = if (rawQuests.isNotEmpty()) rawQuests.split("\n").filter { it.isNotBlank() } else emptyList()

        val size = LocalSize.current
        val totalHeight = if (size.height.value > 0) size.height else 220.dp
        val availableQuestHeight = maxOf(40.dp, totalHeight - 75.dp)

        val context = LocalContext.current
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val launchAction = actionStartActivity(intent)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorVoidBg)
                .appWidgetBackground()
                .clickable(launchAction)
                .padding(10.dp)
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(launchAction)
            ) {
                // 1. Top Section (Fixed): Header
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(launchAction),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "SYSTEM // LEVEL UP",
                        style = TextStyle(
                            color      = ColorProvider(ColorCyanAccent),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(GlanceModifier.height(4.dp))

                // Luminous divider
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ColorCyanAccent)
                ) {}

                Spacer(GlanceModifier.height(4.dp))

                // Section Label: DAILY QUEST
                Text(
                    "DAILY QUEST",
                    style = TextStyle(
                        color      = ColorProvider(ColorCyanMuted),
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(GlanceModifier.height(2.dp))

                // 2. Middle Section: All Active Quests with bounded height
                if (questList.isEmpty()) {
                    Text(
                        "NO ACTIVE QUESTS",
                        style = TextStyle(
                            color    = ColorProvider(ColorTextMuted),
                            fontSize = 11.sp
                        )
                    )
                } else {
                    val itemPadding = when {
                        questList.size > 8 -> 0.dp
                        questList.size > 5 -> 1.dp
                        else -> 2.dp
                    }
                    val questFontSize = when {
                        questList.size > 10 -> 9.sp
                        questList.size > 7 -> 10.sp
                        else -> 11.sp
                    }

                    LazyColumn(
                        modifier = GlanceModifier
                            .height(availableQuestHeight)
                            .fillMaxWidth()
                    ) {
                        items(questList) { line ->
                            val parts = line.split("|")
                            val name = parts.getOrElse(0) { "" }
                            val targetUnit = parts.getOrElse(1) { "" }
                            val done = parts.getOrElse(2) { "0" } == "1"

                            Row(
                                modifier          = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(vertical = itemPadding)
                                    .clickable(launchAction),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (done) "[✓] " else "[ ] ",
                                    style = TextStyle(
                                        color      = ColorProvider(if (done) ColorGreenDone else ColorUncheckedBox),
                                        fontSize   = questFontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = name,
                                    style = TextStyle(
                                        color      = ColorProvider(if (done) ColorGreenDone else ColorTextIncomplete),
                                        fontSize   = questFontSize,
                                        fontWeight = if (done) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                Spacer(GlanceModifier.defaultWeight())
                                if (targetUnit.isNotEmpty()) {
                                    Text(
                                        text = targetUnit,
                                        style = TextStyle(
                                            color      = ColorProvider(if (done) ColorGreenDone else ColorTextIncomplete),
                                            fontSize   = questFontSize,
                                            fontWeight = if (done) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Flexibly absorbs leftover space to anchor the bottom section
                Spacer(GlanceModifier.defaultWeight())

                // 3. Bottom Section (Fixed & Anchored): Divider, Streak & Progress
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x6600E5FF))
                ) {}

                Spacer(GlanceModifier.height(4.dp))

                // Guaranteed Streak Row
                val streakText = when {
                    streak <= 0 -> "NO STREAK"
                    streak == 1 -> "1 DAY"
                    else -> "$streak DAYS"
                }
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(launchAction),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🔥 STREAK",
                        style = TextStyle(
                            color      = ColorProvider(ColorStreakOrange),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        streakText,
                        style = TextStyle(
                            color      = ColorProvider(if (streak > 0) ColorStreakOrange else ColorTextMuted),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(GlanceModifier.height(3.dp))

                // Fixed Quest Progress Row
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(launchAction),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUEST PROGRESS",
                        style = TextStyle(
                            color      = ColorProvider(ColorTextMuted),
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        text = "$pct%",
                        style = TextStyle(
                            color      = ColorProvider(ColorCyanAccent),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(GlanceModifier.height(2.dp))

                LinearProgressIndicator(
                    progress = (pct / 100f).coerceIn(0f, 1f),
                    modifier = GlanceModifier.fillMaxWidth().height(4.dp),
                    color = ColorProvider(ColorCyanAccent),
                    backgroundColor = ColorProvider(ColorTrackBg)
                )
            }
        }
    }
}

// ─── Receiver ─────────────────────────────────────────────────────────────────
class LevelUpWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LevelUpWidget()
}

// ─── Updater called from repository/worker ────────────────────────────────────
suspend fun updateAllWidgets(context: Context, database: LevelUpDatabase) {
    withContext(Dispatchers.IO) {
        try {
            val today    = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val up       = database.userProgressDao().get() ?: return@withContext
            val workouts = database.workoutDao().getAll().filter { it.enabled }
            val habits   = database.habitDao().getAll().filter { it.enabled }
            val wProgs   = database.workoutProgressDao().getByDate(today)
            val hProgs   = database.habitProgressDao().getByDate(today)

            val wProgMap = wProgs.associateBy { it.workoutId }
            val hProgMap = hProgs.associateBy { it.habitId }

            val totalActive = workouts.size + habits.size
            val completed   = workouts.count { w -> wProgMap[w.id]?.xpAwarded == true } +
                              habits.count { h -> hProgMap[h.id]?.xpAwarded == true }
            val pct = if (totalActive > 0) completed * 100 / totalActive else 0

            val questLines = mutableListOf<String>()

            // Active workouts
            workouts.forEach { w ->
                val done = if (wProgMap[w.id]?.xpAwarded == true) "1" else "0"
                val label = w.name.uppercase()
                val targetUnit = "${w.target} ${w.unit}".uppercase()
                questLines.add("$label|$targetUnit|$done")
            }

            // Active habits
            habits.forEach { h ->
                val done = if (hProgMap[h.id]?.xpAwarded == true) "1" else "0"
                val label = h.name.uppercase()
                questLines.add("$label||$done")
            }

            val questData = questLines.joinToString("\n")

            val glanceIds = GlanceAppWidgetManager(context)
                .getGlanceIds(LevelUpWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WIDGET_STREAK]     = up.streak
                        this[WIDGET_DAY_PCT]    = pct
                        this[WIDGET_QUEST_LIST] = questData
                    }
                }
                LevelUpWidget().update(context, glanceId)
            }
        } catch (_: Exception) {
            // Widget update is best-effort; never crash the app
        }
    }
}

// ─── Android Studio Compose Visual Preview ─────────────────────────────────────
@Preview(showBackground = true, widthDp = 280, heightDp = 260)
@Composable
fun LevelUpWidgetPreview() {
    ComposeBox(
        modifier = ComposeModifier
            .composeFillMaxWidth()
            .composeHeight(260.dp)
            .composeBackground(ColorVoidBg)
            .composeBorder(1.dp, ColorCyanAccent, ComposeRoundedCornerShape(4.dp))
            .composePadding(10.dp)
    ) {
        ComposeColumn(modifier = ComposeModifier.composeFillMaxWidth()) {
            ComposeText(
                "SYSTEM // LEVEL UP",
                color = ColorCyanAccent,
                fontSize = 11.sp,
                fontWeight = ComposeFontWeight.Bold
            )
            ComposeSpacer(ComposeModifier.composeHeight(4.dp))
            ComposeBox(
                modifier = ComposeModifier
                    .composeFillMaxWidth()
                    .composeHeight(1.dp)
                    .composeBackground(ColorCyanAccent)
            )
            ComposeSpacer(ComposeModifier.composeHeight(4.dp))
            ComposeText(
                "DAILY QUEST",
                color = ColorCyanMuted,
                fontSize = 10.sp,
                fontWeight = ComposeFontWeight.Bold
            )
            ComposeSpacer(ComposeModifier.composeHeight(2.dp))

            // Sample Quests
            val sampleQuests = listOf(
                Triple("PUSH-UPS", "50 REPS", true),
                Triple("PULL-UPS", "10 REPS", true),
                Triple("SQUATS", "50 REPS", false),
                Triple("BRUSH TEETH – MORNING", "", true),
                Triple("BRUSH TEETH – NIGHT", "", false),
                Triple("SHOWER", "", true),
                Triple("DRINK WATER", "", true)
            )
            ComposeColumn(modifier = ComposeModifier.weight(1f)) {
                sampleQuests.forEach { (name, targetUnit, done) ->
                    ComposeRow(
                        modifier = ComposeModifier.composeFillMaxWidth().composePadding(vertical = 1.dp),
                        verticalAlignment = ComposeAlignment.CenterVertically
                    ) {
                        ComposeText(
                            if (done) "[✓] " else "[ ] ",
                            color = if (done) ColorGreenDone else ColorUncheckedBox,
                            fontSize = 11.sp,
                            fontWeight = ComposeFontWeight.Bold
                        )
                        ComposeText(
                            name,
                            color = if (done) ColorGreenDone else ColorTextIncomplete,
                            fontSize = 11.sp,
                            fontWeight = if (done) ComposeFontWeight.Bold else ComposeFontWeight.Normal
                        )
                        ComposeSpacer(ComposeModifier.weight(1f))
                        if (targetUnit.isNotEmpty()) {
                            ComposeText(
                                targetUnit,
                                color = if (done) ColorGreenDone else ColorTextIncomplete,
                                fontSize = 11.sp,
                                fontWeight = if (done) ComposeFontWeight.Bold else ComposeFontWeight.Normal
                            )
                        }
                    }
                }
            }

            ComposeBox(
                modifier = ComposeModifier
                    .composeFillMaxWidth()
                    .composeHeight(1.dp)
                    .composeBackground(Color(0x6600E5FF))
            )
            ComposeSpacer(ComposeModifier.composeHeight(4.dp))
            ComposeRow(
                modifier = ComposeModifier.composeFillMaxWidth(),
                verticalAlignment = ComposeAlignment.CenterVertically
            ) {
                ComposeText("🔥 STREAK", color = ColorStreakOrange, fontSize = 10.sp, fontWeight = ComposeFontWeight.Bold)
                ComposeSpacer(ComposeModifier.weight(1f))
                ComposeText("7 DAYS", color = ColorStreakOrange, fontSize = 10.sp, fontWeight = ComposeFontWeight.Bold)
            }
            ComposeSpacer(ComposeModifier.composeHeight(3.dp))
            ComposeRow(
                modifier = ComposeModifier.composeFillMaxWidth(),
                verticalAlignment = ComposeAlignment.CenterVertically
            ) {
                ComposeText("QUEST PROGRESS", color = ColorTextMuted, fontSize = 9.sp, fontWeight = ComposeFontWeight.Bold)
                ComposeSpacer(ComposeModifier.weight(1f))
                ComposeText("71%", color = ColorCyanAccent, fontSize = 10.sp, fontWeight = ComposeFontWeight.Bold)
            }
            ComposeSpacer(ComposeModifier.composeHeight(2.dp))
            ComposeBox(
                modifier = ComposeModifier
                    .composeFillMaxWidth()
                    .composeHeight(4.dp)
                    .composeBackground(ColorTrackBg)
            ) {
                ComposeBox(
                    modifier = ComposeModifier
                        .composeFillMaxWidth(0.71f)
                        .composeHeight(4.dp)
                        .composeBackground(ColorCyanAccent)
                )
            }
        }
    }
}
