package com.example.aura.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.engine.AchievementEngine
import com.example.aura.engine.AchievementProgress
import com.example.aura.ui.AuraViewModel
import com.example.aura.ui.components.NavStreaksFilledIcon
import com.example.aura.ui.components.SportScoreboardFilledIcon
import com.example.aura.ui.theme.AuraTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun StreaksScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val draft by viewModel.draftProfile.collectAsState()
    val user = profile ?: draft
    val sessions by viewModel.workoutSessions.collectAsState()
    val habits by viewModel.dailyHabits.collectAsState()
    val exerciseLogs by viewModel.exerciseLogs.collectAsState()
    val checkIns by viewModel.bodyCheckIns.collectAsState()
    val persistedAchievements by viewModel.unlockedAchievements.collectAsState()
    val justCompletedEvent by viewModel.justCompletedWorkoutEvent.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val context = LocalContext.current
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayKey = remember { sdf.format(Date()) }
    val todayHabit = habits[todayKey] ?: DailyHabitStatus(dateKey = todayKey)

    // Calculate real smart streak allowing 1 rest day without breaking
    val rawStreak = remember(user, sessions, habits) { viewModel.calculateCurrentStreak() }
    val currentStreak = if (rawStreak == 0 && todayHabit.workout) 1 else rawStreak
    val achievements = remember(user, sessions, habits, exerciseLogs, checkIns, persistedAchievements) {
        AchievementEngine.evaluate(user, sessions, habits, exerciseLogs, checkIns, persistedAchievements)
    }

    // Animation state for the streak counter
    val counterScale = remember { Animatable(1.0f) }
    val rippleRadius = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    var displayedStreakNumber by remember { mutableIntStateOf(if (currentStreak > 0) currentStreak else 0) }

    // Trigger subtle, high-quality positive reinforcement animation when user completes workout
    LaunchedEffect(justCompletedEvent) {
        if (justCompletedEvent) {
            if (hapticsEnabled) {
                // Trigger haptic vibration
                try {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(
                                VibrationEffect.createWaveform(
                                    longArrayOf(0, 30, 60, 40),
                                    intArrayOf(0, 180, 0, 220),
                                    -1
                                )
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(80)
                        }
                    }
                } catch (_: Exception) {}
            }

            // Sequence subtle spring-based celebration animation
            launch {
                glowAlpha.animateTo(0.6f, tween(300, easing = FastOutSlowInEasing))
                glowAlpha.animateTo(0.15f, tween(700, easing = FastOutSlowInEasing))
            }

            launch {
                // Subtle concentric ripple wave
                rippleRadius.snapTo(0.6f)
                rippleAlpha.snapTo(0.8f)
                rippleRadius.animateTo(1.4f, tween(900, easing = FastOutSlowInEasing))
                rippleAlpha.animateTo(0f, tween(400, easing = LinearEasing))
            }

            // High-quality spring scale bounce
            counterScale.animateTo(
                targetValue = 1.15f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )

            // Number tick up smoothly
            displayedStreakNumber = currentStreak

            counterScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )

            viewModel.resetCompletedWorkoutEvent()
        } else {
            displayedStreakNumber = currentStreak
        }
    }

    // Weekly day items (last 7 days)
    val past7Days = remember(habits) {
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysAgo ->
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val key = sdf.format(c.time)
            val dayLetter = SimpleDateFormat("EEE", Locale.US).format(c.time).take(1)
            val dateNum = SimpleDateFormat("d", Locale.US).format(c.time)
            val isWorkoutDone = habits[key]?.workout == true
            val isDietDone = habits[key]?.diet == true
            val isToday = (daysAgo == 0)
            WeeklyDayCheck(
                key = key,
                letter = dayLetter,
                dayNumber = dateNum,
                isCompleted = isWorkoutDone || isDietDone,
                isWorkoutDone = isWorkoutDone,
                isDietDone = isDietDone,
                isToday = isToday
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraTheme.colors.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(key = "streaks_header") {
                Spacer(modifier = Modifier.height(8.dp))
                StreaksHeader(todayHabit = todayHabit)
            }

            // Streak Counter Hero Card with subtle animation
            item(key = "streak_hero") {
                StreakHeroCounterCard(
                    streakCount = displayedStreakNumber,
                    scale = counterScale.value,
                    rippleRadius = rippleRadius.value,
                    rippleAlpha = rippleAlpha.value,
                    glowAlpha = glowAlpha.value,
                    isWorkoutDoneToday = todayHabit.workout,
                    onSimulateWorkoutComplete = {
                        viewModel.recordWorkoutSession(
                            dayNumber = 1,
                            dayTitle = "Strength Session",
                            setsCompleted = 12,
                            totalSets = 12,
                            volumeKg = 2450.0,
                            prCount = 1
                        )
                    }
                )
            }

            // 7-Day Consistency Matrix
            item(key = "weekly_streak_strip") {
                WeeklyStreakStrip(
                    days = past7Days,
                    onToggleDay = { dateKey ->
                        viewModel.toggleHabit(dateKey, "workout")
                    }
                )
            }

            // Daily Habit Ring / Protocol checklist
            item(key = "daily_habits_checklist") {
                DailyHabitsChecklist(
                    todayKey = todayKey,
                    status = todayHabit,
                    onToggle = { habitName ->
                        viewModel.toggleHabit(todayKey, habitName)
                    }
                )
            }

            // Freeze Protection & 15 Achievement Badges
            item(key = "streak_milestones") {
                StreakMilestonesSection(
                    plannedDaysPerWeek = user.daysPerWeek,
                    achievements = achievements
                )
            }

            // World Championship Standing Card (Inspired by Sport & Fitness Icons Sheet)
            item(key = "world_championship_card") {
                WorldChampionshipStandingCard()
            }

            item(key = "streaks_bottom_spacer") {
                // Bottom spacing for frosted nav bar
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

private data class WeeklyDayCheck(
    val key: String,
    val letter: String,
    val dayNumber: String,
    val isCompleted: Boolean,
    val isWorkoutDone: Boolean,
    val isDietDone: Boolean,
    val isToday: Boolean
)

@Composable
private fun StreaksHeader(todayHabit: DailyHabitStatus) {
    val accent = AuraTheme.colors.accent

    Column {
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
                        text = "CONSISTENCY & STREAKS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Habit Momentum",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuraTheme.colors.primaryText
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (todayHabit.workout) accent.copy(alpha = 0.15f)
                        else AuraTheme.colors.surfaceSecondary
                    )
                    .border(
                        1.dp,
                        if (todayHabit.workout) accent.copy(alpha = 0.4f)
                        else AuraTheme.colors.cardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (todayHabit.workout) Icons.Default.CheckCircle else Icons.Default.ElectricBolt,
                        contentDescription = "Status",
                        tint = if (todayHabit.workout) accent else AuraTheme.colors.secondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (todayHabit.workout) "Trained Today" else "Session Pending",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (todayHabit.workout) accent else AuraTheme.colors.secondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakHeroCounterCard(
    streakCount: Int,
    scale: Float,
    rippleRadius: Float,
    rippleAlpha: Float,
    glowAlpha: Float,
    isWorkoutDoneToday: Boolean,
    onSimulateWorkoutComplete: () -> Unit
) {
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(28.dp))
            .padding(24.dp)
            .testTag("streak_counter_hero_card"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Flame & Ripple Canvas Container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // Subtle ripple wave canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = size.minDimension / 2.6f

                    if (rippleAlpha > 0f) {
                        drawCircle(
                            color = accent.copy(alpha = rippleAlpha * 0.4f),
                            radius = baseRadius * rippleRadius,
                            center = centerOffset,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // Soft ambient ambient glow behind flame
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = glowAlpha),
                                accent.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = baseRadius * 1.2f
                        ),
                        center = centerOffset,
                        radius = baseRadius * 1.2f
                    )
                }

                // Core Circular Badge
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            if (isWorkoutDoneToday) accent.copy(alpha = 0.16f)
                            else AuraTheme.colors.surfaceSecondary
                        )
                        .border(
                            1.5.dp,
                            if (isWorkoutDoneToday) accent.copy(alpha = 0.6f)
                            else AuraTheme.colors.cardBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Flame",
                        tint = if (isWorkoutDoneToday) accent else AuraTheme.colors.secondaryText,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Animated Digit Counter
            Text(
                text = "$streakCount",
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = AuraTheme.colors.primaryText,
                modifier = Modifier
                    .scale(scale)
                    .testTag("streak_count_display")
            )

            Text(
                text = if (streakCount == 1) "DAY STREAK" else "DAYS STREAK",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isWorkoutDoneToday) {
                    "Today's target locked in. Your momentum is building."
                } else {
                    "Complete today's training session to extend your streak."
                },
                fontSize = 13.sp,
                color = AuraTheme.colors.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (!isWorkoutDoneToday) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSimulateWorkoutComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = accentContrast
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("log_today_session_quick_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark Daily Workout Complete",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyStreakStrip(
    days: List<WeeklyDayCheck>,
    onToggleDay: (String) -> Unit
) {
    val accent = AuraTheme.colors.accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAST 7 DAYS",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AuraTheme.colors.secondaryText
                )
                Text(
                    text = "${days.count { it.isCompleted }}/7 Completed",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleDay(day.key) }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = day.letter,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (day.isToday) accent else AuraTheme.colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        day.isWorkoutDone -> accent
                                        day.isCompleted -> accent.copy(alpha = 0.35f)
                                        else -> AuraTheme.colors.surfaceSecondary
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (day.isToday) accent else Color.Transparent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = AuraTheme.colors.accentContrast,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = day.dayNumber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AuraTheme.colors.secondaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyHabitsChecklist(
    todayKey: String,
    status: DailyHabitStatus,
    onToggle: (String) -> Unit
) {
    val accent = AuraTheme.colors.accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "TODAY'S DISCIPLINE PROTOCOL",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accent
            )

            HabitRow(
                title = "Workout Session",
                subtitle = "Strength or programmed cardio protocol",
                isCompleted = status.workout,
                icon = Icons.Default.FitnessCenter,
                onToggle = { onToggle("workout") }
            )

            HabitRow(
                title = "Nutrition & Protein Target",
                subtitle = "Hit target macro balance and calorie goals",
                isCompleted = status.diet,
                icon = Icons.Default.Restaurant,
                onToggle = { onToggle("diet") }
            )

            HabitRow(
                title = "Hydration Protocol",
                subtitle = "Target 3.0L water intake logged",
                isCompleted = status.water,
                icon = Icons.Default.WaterDrop,
                onToggle = { onToggle("water") }
            )
        }
    }
}

@Composable
private fun HabitRow(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: () -> Unit
) {
    val accent = AuraTheme.colors.accent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCompleted) accent.copy(alpha = 0.08f) else AuraTheme.colors.surfaceSecondary)
            .border(
                1.dp,
                if (isCompleted) accent.copy(alpha = 0.35f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onToggle() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) accent.copy(alpha = 0.2f) else AuraTheme.colors.cardBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCompleted) accent else AuraTheme.colors.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) AuraTheme.colors.secondaryText else AuraTheme.colors.primaryText
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = AuraTheme.colors.secondaryText
                )
            }
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isCompleted) accent else AuraTheme.colors.cardBorder),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = AuraTheme.colors.accentContrast,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun StreakMilestonesSection(
    plannedDaysPerWeek: Int,
    achievements: List<AchievementProgress>
) {
    val accent = AuraTheme.colors.accent
    val unlockedCount = achievements.count { it.isUnlocked }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACHIEVEMENTS ($unlockedCount/${achievements.size})",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Text(
                        text = "Based on your $plannedDaysPerWeek planned days",
                        fontSize = 11.sp,
                        color = AuraTheme.colors.secondaryText
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Rest Grace",
                        tint = accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "1 Rest Day Grace",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuraTheme.colors.primaryText
                    )
                }
            }

            achievements.forEach { item ->
                MilestoneBadge(
                    title = item.definition.title,
                    progressLabel = "${item.currentValue.coerceAtMost(item.definition.targetValue)}/${item.definition.targetValue}",
                    isAchieved = item.isUnlocked,
                    desc = item.definition.description
                )
            }
        }
    }
}

@Composable
private fun MilestoneBadge(
    title: String,
    progressLabel: String,
    isAchieved: Boolean,
    desc: String
) {
    val accent = AuraTheme.colors.accent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isAchieved) accent.copy(alpha = 0.08f) else AuraTheme.colors.surfaceSecondary)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAchieved) AuraTheme.colors.primaryText else AuraTheme.colors.secondaryText
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = progressLabel,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = desc,
                fontSize = 11.sp,
                color = AuraTheme.colors.secondaryText
            )
        }

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isAchieved) accent.copy(alpha = 0.2f) else AuraTheme.colors.cardBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAchieved) Icons.Default.Check else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isAchieved) accent else AuraTheme.colors.secondaryText,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun WorldChampionshipStandingCard() {
    val accent = AuraTheme.colors.accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("championship_standing_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = NavStreaksFilledIcon,
                            contentDescription = "Championship Trophy",
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WORLD CHAMPIONSHIP",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraTheme.colors.surfaceSecondary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = SportScoreboardFilledIcon,
                            contentDescription = "Scoreboard",
                            tint = accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GLOBAL RANK #3",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AuraTheme.colors.primaryText
                        )
                    }
                }
            }

            // Headline
            Text(
                text = "22 points away from second place in the world championship",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AuraTheme.colors.primaryText,
                lineHeight = 20.sp
            )

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Gap to 2nd Place: -22 pts",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                    Text(
                        text = "1,878 / 1,900 PTS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AuraTheme.colors.secondaryText
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(AuraTheme.colors.surfaceSecondary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                }
            }

            Text(
                text = "Complete today's workout protocol and log sets to close the championship gap.",
                fontSize = 11.5.sp,
                color = AuraTheme.colors.secondaryText
            )
        }
    }
}
