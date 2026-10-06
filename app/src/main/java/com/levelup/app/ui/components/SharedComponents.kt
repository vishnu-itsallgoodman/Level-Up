package com.levelup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levelup.app.domain.model.Rank
import com.levelup.app.ui.theme.*

@Composable
fun SystemCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.sp,
        color = TextMuted,
        modifier = modifier
    )
}

@Composable
fun XpProgressBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    fillColor: Color = XpBarFill
) {
    val fraction = if (max > 0) (current.toFloat() / max).coerceIn(0f, 1f) else 0f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(XpBarBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(RoundedCornerShape(height / 2))
                .background(fillColor)
        )
    }
}

@Composable
fun RankBadge(rank: Rank) {
    val color = when (rank) {
        Rank.E -> RankE
        Rank.D -> RankD
        Rank.C -> RankC
        Rank.B -> RankB
        Rank.A -> RankA
        Rank.S -> RankS
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RANK ${rank.label}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = color
        )
    }
}

@Composable
fun StatBar(
    label: String,
    value: Int,
    max: Int = 100,
    color: Color = AccentBlue,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, letterSpacing = 1.5.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Text("$value", fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        XpProgressBar(current = value, max = max, fillColor = color, height = 6.dp)
    }
}
