package com.example.aura.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.data.model.WorkoutDayPlan
import com.example.aura.data.model.WorkoutSessionRecord
import com.example.aura.ui.coach.CoachMood
import com.example.aura.ui.theme.AuraTheme

/**
 * Scrollable Horizontal Weekly Training Timeline
 * Visually distinguishes between Completed workouts, Missed days, Rest days,
 * and Current/Upcoming workouts.
 * Includes a click handler on each day to reveal a comprehensive summary sheet.
 */
@Composable
fun WeeklyTrainingTimeline(
    weekPlans: List<WorkoutDayPlan>,
    sessionRecords: List<WorkoutSessionRecord>,
    currentActiveDayNumber: Int,
    modifier: Modifier = Modifier,
    onDayClicked: ((WorkoutDayPlan) -> Unit)? = null,
    onNavigateToWorkout: (() -> Unit)? = null
) {
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast
    if (weekPlans.isEmpty()) return

    val initialIndex = weekPlans.indexOfFirst { it.dayNumber == currentActiveDayNumber }
        .takeIf { it >= 0 } ?: 0
    var selectedIndex by remember(weekPlans, currentActiveDayNumber) {
        mutableIntStateOf(initialIndex)
    }
    var summarySheetPlan by remember { mutableStateOf<WorkoutDayPlan?>(null) }

    val selectedPlan = weekPlans.getOrNull(selectedIndex) ?: weekPlans[0]
    val matchingSession = sessionRecords.find { it.dayNumber == selectedPlan.dayNumber }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("weekly_training_timeline")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // --- Header & Legend ---
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            text = "WEEKLY TRAINING TIMELINE",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }
                    Text(
                        text = "Training Progression Log",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AuraTheme.colors.primaryText
                    )
                }
                // Interactive tap hint
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraTheme.colors.surfaceSecondary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Tap to inspect sheet",
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AuraTheme.colors.textMuted
                    )
                }
            }

            // --- Horizontal Timeline Nodes with Distinct Status Colors ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekPlans.forEachIndexed { index, plan ->
                    val isSelected = selectedIndex == index
                    val isCurrent = plan.dayNumber == currentActiveDayNumber
                    val sessionForDay = sessionRecords.find { it.dayNumber == plan.dayNumber }

                    // Timeline classification
                    val isCompleted = plan.isCompleted || (sessionForDay != null && sessionForDay.isFullyCompleted)
                    val isMissed = plan.isMissedOrRest
                    val isRest = plan.isRest

                    val dateParts = plan.scheduledDate.split(" ")
                    val weekday = dateParts.firstOrNull()?.replace(",", "") ?: "D${plan.dayNumber}"
                    val dayNum = dateParts.lastOrNull() ?: "${plan.dayNumber}"

                    val nodeBorderColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> accent
                            isCompleted -> Color(0xFF10B981)
                            isMissed -> Color(0xFFF59E0B)
                            isRest -> Color(0xFF818CF8)
                            isCurrent -> accent.copy(alpha = 0.7f)
                            else -> AuraTheme.colors.cardBorder
                        },
                        animationSpec = tween(250),
                        label = "node_border"
                    )

                    val nodeBgColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> if (AuraTheme.colors.isDark) Color(0xFF171725) else accent.copy(alpha = 0.12f)
                            isCompleted -> Color(0x1A10B981)
                            isMissed -> Color(0x1AF59E0B)
                            isRest -> Color(0x14818CF8)
                            else -> AuraTheme.colors.surfaceSecondary
                        },
                        animationSpec = tween(250),
                        label = "node_bg"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(nodeBgColor)
                            .border(if (isSelected) 1.5.dp else 1.dp, nodeBorderColor, RoundedCornerShape(16.dp))
                            .clickable {
                                selectedIndex = index
                                summarySheetPlan = plan
                                onDayClicked?.invoke(plan)
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("timeline_day_node_${plan.dayNumber}")
                    ) {
                        Text(
                            text = weekday.uppercase(),
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accent else AuraTheme.colors.textMuted
                        )
                        Text(
                            text = dayNum,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) (if (AuraTheme.colors.isDark) Color.White else accent) else AuraTheme.colors.primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Status Badge Icon
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> Color(0xFF10B981)
                                        isMissed -> Color(0xFFF59E0B)
                                        isRest -> Color(0xFF818CF8)
                                        isCurrent -> accent
                                        else -> AuraTheme.colors.cardBorder
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCompleted -> {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Completed",
                                        tint = Color.Black,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                isMissed -> {
                                    Text("⚠️", fontSize = 11.sp)
                                }
                                isRest -> {
                                    Text("💤", fontSize = 10.sp)
                                }
                                isCurrent -> {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(accentContrast)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Upcoming",
                                        tint = AuraTheme.colors.textMuted,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = when {
                                isCompleted -> "DONE"
                                isMissed -> "MISSED"
                                isRest -> "REST"
                                isCurrent -> "CURRENT"
                                else -> "UPCOMING"
                            },
                            fontSize = 7.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isCompleted -> if (AuraTheme.colors.isDark) Color(0xFF6EE7B7) else Color(0xFF059669)
                                isMissed -> if (AuraTheme.colors.isDark) Color(0xFFFCD34D) else Color(0xFFD97706)
                                isRest -> if (AuraTheme.colors.isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1)
                                isCurrent -> accent
                                else -> AuraTheme.colors.textMuted
                            }
                        )
                    }
                }
            }

            // --- Inline Inspection Card ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AuraTheme.colors.surfaceSecondary)
                    .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp)
                    .testTag("timeline_day_summary_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header with Date, Title and Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = selectedPlan.scheduledDate.uppercase(),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = selectedPlan.focus,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText
                            )
                            if (selectedPlan.targetMusclesText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedPlan.targetMusclesText,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AuraTheme.colors.secondaryText,
                                    maxLines = 1
                                )
                            }
                        }

                        // Status Tag
                        val (statusText, statusBg, statusColor) = when {
                            selectedPlan.isCompleted || (matchingSession?.isFullyCompleted == true) ->
                                Triple("Completed ✓", Color(0x2610B981), if (AuraTheme.colors.isDark) Color(0xFF6EE7B7) else Color(0xFF059669))
                            selectedPlan.isMissedOrRest ->
                                Triple("Missed / Shifted", Color(0x26F59E0B), if (AuraTheme.colors.isDark) Color(0xFFFCD34D) else Color(0xFFD97706))
                            selectedPlan.dayNumber == currentActiveDayNumber ->
                                Triple("Active Target 🎯", accent.copy(alpha = 0.2f), accent)
                            selectedPlan.isRest ->
                                Triple("Scheduled Rest", Color(0x26818CF8), if (AuraTheme.colors.isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1))
                            else ->
                                Triple("Upcoming", AuraTheme.colors.card, AuraTheme.colors.textMuted)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    // Set Completion Metrics Row
                    if (matchingSession != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AuraTheme.colors.card)
                                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sets Logged: ${matchingSession.setsCompleted} of ${matchingSession.totalSets}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.primaryText
                            )
                            val pct = if (matchingSession.totalSets > 0) {
                                ((matchingSession.setsCompleted.toFloat() / matchingSession.totalSets) * 100).toInt()
                            } else 0
                            Text(
                                text = "$pct% Adherence",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    // Exercises / Rest Content Preview
                    if (selectedPlan.isRest) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraTheme.colors.card)
                                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💤", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Active Restoration & Recovery",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTheme.colors.primaryText
                                )
                                Text(
                                    text = "Hydration, light walking, and mobility work to prepare muscle tissue.",
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    color = AuraTheme.colors.textMuted
                                )
                            }
                        }
                    } else if (selectedPlan.exercises.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "EXERCISES (${selectedPlan.exercises.size})",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.textMuted
                            )
                            selectedPlan.exercises.take(2).forEach { ex ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AuraTheme.colors.card)
                                        .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ExerciseDemo(
                                            exerciseName = ex.name,
                                            exerciseId = ex.id,
                                            primaryMuscle = ex.getEffectivePrimary(),
                                            equipment = ex.equipment,
                                            movementPattern = ex.getEffectivePattern(),
                                            compact = true,
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ex.name, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AuraTheme.colors.primaryText)
                                            Text("${ex.sets} Sets • ${ex.reps}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = AuraTheme.colors.textMuted)
                                        }
                                        Text(
                                            text = if (ex.isBodyweight) "BW" else "${ex.recommendedLoadKg} kg",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = accent
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Button to open full summary sheet modal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AuraTheme.colors.card)
                                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    summarySheetPlan = selectedPlan
                                    onDayClicked?.invoke(selectedPlan)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Reveal Summary Sheet",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.primaryText
                            )
                        }

                        if (onNavigateToWorkout != null && selectedPlan.dayNumber == currentActiveDayNumber && !selectedPlan.isCompleted) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accent)
                                    .clickable { onNavigateToWorkout() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Start Workout",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentContrast
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = accentContrast,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Modal Bottom Sheet: Full Day Summary Sheet ---
    DaySummaryBottomSheet(
        plan = summarySheetPlan,
        sessionRecord = summarySheetPlan?.let { p -> sessionRecords.find { it.dayNumber == p.dayNumber } },
        isCurrentActiveDay = summarySheetPlan?.dayNumber == currentActiveDayNumber,
        onDismiss = { summarySheetPlan = null },
        onStartWorkout = {
            summarySheetPlan = null
            onNavigateToWorkout?.invoke()
        }
    )
}

/**
 * Slide-in Summary Bottom Sheet for inspecting any day on the timeline.
 */
@Composable
fun DaySummaryBottomSheet(
    plan: WorkoutDayPlan?,
    sessionRecord: WorkoutSessionRecord?,
    isCurrentActiveDay: Boolean,
    onDismiss: () -> Unit,
    onStartWorkout: (() -> Unit)? = null
) {
    val isVisible = plan != null
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable { onDismiss() }
                .testTag("day_summary_sheet_scrim"),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (plan != null) {
                val isCompleted = plan.isCompleted || (sessionRecord?.isFullyCompleted == true)
                val isMissed = plan.isMissedOrRest
                val isRest = plan.isRest

                // Coach character mood for this day
                val coachMood = when {
                    isCompleted -> CoachMood.HAPPY
                    isMissed -> CoachMood.CONCERNED
                    isRest -> CoachMood.CALM
                    isCurrentActiveDay -> CoachMood.PLEASED
                    else -> CoachMood.AWARE
                }

                val coachMessage = when {
                    isCompleted -> "All sets executed. Quality stimulus delivered to the target fibers."
                    isMissed -> "Session missed. Your schedule was automatically adjusted to preserve hypertrophy cadence."
                    isRest -> "Recovery in progress. Protein synthesis and glycogen replenishment occur today."
                    isCurrentActiveDay -> "Current target session. Warm up thoroughly and focus on controlled reps."
                    else -> "Upcoming planned session. Calibrated according to your weekly volume split."
                }

                val (badgeText, badgeBg, badgeColor) = when {
                    isCompleted -> Triple("COMPLETED", Color(0x2610B981), if (AuraTheme.colors.isDark) Color(0xFF6EE7B7) else Color(0xFF059669))
                    isMissed -> Triple("MISSED / SHIFTED", Color(0x26F59E0B), if (AuraTheme.colors.isDark) Color(0xFFFCD34D) else Color(0xFFD97706))
                    isRest -> Triple("ACTIVE RECOVERY", Color(0x26818CF8), if (AuraTheme.colors.isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1))
                    isCurrentActiveDay -> Triple("CURRENT ACTIVE", accent.copy(alpha = 0.2f), accent)
                    else -> Triple("UPCOMING", AuraTheme.colors.surfaceSecondary, AuraTheme.colors.textMuted)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(AuraTheme.colors.sheetBackground)
                        .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .clickable(enabled = false) {}
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .navigationBarsPadding()
                        .testTag("day_summary_bottom_sheet")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sheet Handle
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(AuraTheme.colors.cardBorder)
                                .align(Alignment.CenterHorizontally)
                        )

                        // Header Row with Title & Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.scheduledDate.uppercase(),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                                Text(
                                    text = plan.focus,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AuraTheme.colors.primaryText
                                )
                                if (plan.targetMusclesText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "TARGET: ${plan.targetMusclesText}",
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = accent
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AuraTheme.colors.surfaceSecondary)
                                    .testTag("close_day_summary_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = AuraTheme.colors.primaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Status Badge Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }

                            if (sessionRecord != null) {
                                Text(
                                    text = "${sessionRecord.setsCompleted} / ${sessionRecord.totalSets} Sets Logged",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTheme.colors.primaryText
                                )
                            }
                        }

                        // Coach Character Companion Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(AuraTheme.colors.surfaceSecondary)
                                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(18.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AuraAppLogo(size = 36.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "AURA COACH NOTE",
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = accent
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = coachMessage,
                                        fontSize = 11.5.sp,
                                        color = AuraTheme.colors.secondaryText,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Exercises List or Rest Protocol
                        if (isRest) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AuraTheme.colors.surfaceSecondary)
                                    .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(14.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("💤", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Rest & Neuromuscular Recovery",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraTheme.colors.primaryText
                                    )
                                    Text(
                                        text = "Active recovery allows your central nervous system to reset and muscles to synthesize protein without accumulation of micro-trauma.",
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        color = AuraTheme.colors.textMuted
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "PRESCRIBED EXERCISE ROUTINE (${plan.exercises.size})",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTheme.colors.textMuted
                                )
                                plan.exercises.forEach { ex ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AuraTheme.colors.surfaceSecondary)
                                            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 9.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ExerciseDemo(
                                                exerciseName = ex.name,
                                                exerciseId = ex.id,
                                                primaryMuscle = ex.getEffectivePrimary(),
                                                equipment = ex.equipment,
                                                movementPattern = ex.getEffectivePattern(),
                                                compact = true,
                                                modifier = Modifier.size(56.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(ex.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AuraTheme.colors.primaryText)
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = ex.muscle.replaceFirstChar { it.uppercase() },
                                                        fontSize = 9.5.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = AuraTheme.colors.textMuted
                                                    )
                                                    Text(
                                                        text = "• ${ex.sets} Sets • ${ex.reps}",
                                                        fontSize = 9.5.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = AuraTheme.colors.secondaryText
                                                    )
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(accent.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (ex.isBodyweight) "BW" else "${ex.recommendedLoadKg} kg",
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // CTA Buttons
                        if (onStartWorkout != null && isCurrentActiveDay && !isCompleted && !isRest) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(accent)
                                    .clickable { onStartWorkout() }
                                    .padding(vertical = 12.dp)
                                    .testTag("sheet_start_workout_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = accentContrast,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Start Workout Session Now",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentContrast
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AuraTheme.colors.surfaceSecondary)
                                .clickable { onDismiss() }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Close Summary",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
