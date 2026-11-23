package com.example.aura.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.WorkoutDayPlan
import com.example.aura.engine.WarmupExercise
import com.example.aura.engine.WarmupMobilityEngine
import com.example.aura.ui.components.ExerciseDemo
import com.example.aura.ui.theme.AuraTheme

@Composable
fun WorkoutWarmupSection(
    dayPlan: WorkoutDayPlan,
    modifier: Modifier = Modifier
) {
    val routine = remember(dayPlan) {
        WarmupMobilityEngine.getWarmupProtocol(dayPlan).exercises
    }

    if (routine.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    val completedMap = remember { mutableStateMapOf<String, Boolean>() }
    val completedCount = routine.count { completedMap[it.id] == true }
    val accent = AuraTheme.colors.accent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(22.dp))
            .animateContentSize()
            .testTag("workout_warmup_section")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRE-SESSION ACTIVATION",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Warm-up & Mobility Protocol ($completedCount/${routine.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTheme.colors.primaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Collapse" else "View Drills",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accent
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Warmup",
                        tint = AuraTheme.colors.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    routine.forEach { drill ->
                        val isDone = completedMap[drill.id] == true
                        WarmupDrillRow(
                            drill = drill,
                            isCompleted = isDone,
                            onToggle = { completedMap[drill.id] = !isDone }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WarmupDrillRow(
    drill: WarmupExercise,
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    val accent = AuraTheme.colors.accent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCompleted) accent.copy(alpha = 0.08f) else AuraTheme.colors.surfaceSecondary)
            .border(
                1.dp,
                if (isCompleted) accent.copy(alpha = 0.35f) else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable { onToggle() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) accent else AuraTheme.colors.cardBorder)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = AuraTheme.colors.accentContrast,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            ExerciseDemo(
                exerciseName = drill.name,
                exerciseId = drill.id,
                primaryMuscle = drill.targetJoints,
                equipment = "Bodyweight",
                movementPattern = drill.movementPattern,
                caption = drill.cue,
                compact = true,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = drill.name,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) AuraTheme.colors.secondaryText else AuraTheme.colors.primaryText
                )
                Text(
                    text = "${drill.targetJoints} • ${drill.durationOrReps}",
                    fontSize = 11.sp,
                    color = AuraTheme.colors.secondaryText
                )
                if (drill.cue.isNotBlank()) {
                    Text(
                        text = "Cue: ${drill.cue}",
                        fontSize = 10.5.sp,
                        color = accent.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
