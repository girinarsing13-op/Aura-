package com.example.aura.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aura.data.model.BodyCheckIn
import com.example.aura.engine.AchievementEngine
import com.example.aura.engine.ProgressAnalyticsEngine
import com.example.aura.engine.ProgressionEngine
import com.example.aura.ui.AuraViewModel
import com.example.aura.ui.components.BodyCheckInDialog
import com.example.aura.ui.components.ConsistencyHeatmapGrid
import com.example.aura.ui.components.ExerciseStrengthLineChartCanvas
import com.example.aura.ui.components.WeightTrendLineChartCanvas
import com.example.aura.ui.theme.AuraTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: AuraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val profile by viewModel.userProfile.collectAsState()
    val draft by viewModel.draftProfile.collectAsState()
    val user = profile ?: draft
    val blueprint by viewModel.blueprint.collectAsState()
    val sessions by viewModel.workoutSessions.collectAsState()
    val habits by viewModel.dailyHabits.collectAsState()
    val exerciseLogs by viewModel.exerciseLogs.collectAsState()
    val checkIns by viewModel.bodyCheckIns.collectAsState()
    val persistedAchievements by viewModel.unlockedAchievements.collectAsState()

    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast
    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var selectedWeightWindow by rememberSaveable { mutableIntStateOf(30) }
    var isCheckInDialogOpen by rememberSaveable { mutableStateOf(false) }
    var showEstimated1Rm by rememberSaveable { mutableStateOf(true) }

    val weightPoints = remember(checkIns, selectedWeightWindow, todayKey) {
        ProgressAnalyticsEngine.buildWeightTrend(checkIns, selectedWeightWindow, todayKey)
    }
    val weighInDue = remember(checkIns, todayKey) {
        ProgressAnalyticsEngine.isWeighInDue(checkIns, todayKey)
    }
    val loggedExercises = remember(exerciseLogs) {
        ProgressAnalyticsEngine.getLoggedExerciseNames(exerciseLogs)
    }
    var selectedExercise by rememberSaveable {
        mutableStateOf(loggedExercises.firstOrNull() ?: "")
    }
    if (selectedExercise.isBlank() && loggedExercises.isNotEmpty()) {
        selectedExercise = loggedExercises.first()
    }
    val strengthPoints = remember(selectedExercise, exerciseLogs) {
        if (selectedExercise.isBlank()) emptyList()
        else ProgressAnalyticsEngine.buildExerciseStrengthSeries(selectedExercise, exerciseLogs)
    }
    val personalBests = remember(exerciseLogs) {
        ProgressAnalyticsEngine.buildPersonalBests(exerciseLogs)
    }
    val weeklySummary = remember(user, blueprint, sessions, habits, exerciseLogs) {
        ProgressAnalyticsEngine.buildWeeklySummary(user, blueprint, sessions, habits, exerciseLogs)
    }
    val heatmapWeeks = remember(user, sessions, habits) {
        ProgressAnalyticsEngine.build12WeekHeatmap(user, sessions, habits)
    }
    val deloadRec = remember(sessions, habits, user, todayKey) {
        ProgressionEngine.evaluateDeloadNeed(sessions, habits, user, todayKey)
    }
    val achievements = remember(user, sessions, habits, exerciseLogs, checkIns, persistedAchievements) {
        AchievementEngine.evaluate(user, sessions, habits, exerciseLogs, checkIns, persistedAchievements)
    }
    val smartStreak = remember(user, sessions, habits) {
        ProgressAnalyticsEngine.calculateSmartStreak(user, sessions, habits)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraTheme.colors.background)
            .testTag("progress_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar
            item(key = "progress_top_bar") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AuraTheme.colors.surfaceSecondary)
                                .testTag("progress_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = AuraTheme.colors.primaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PROGRESS & COACHING",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Strength & Check-Ins",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText
                            )
                        }
                    }

                    Button(
                        onClick = { isCheckInDialogOpen = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = accentContrast
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("progress_weigh_in_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Weigh In", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Gentle weekly weigh-in nudge
            if (weighInDue) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(accent.copy(alpha = 0.12f))
                            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable { isCheckInDialogOpen = true }
                            .padding(14.dp)
                            .testTag("weekly_weighin_due_banner"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorWeight,
                                contentDescription = "Weekly weigh-in",
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Weekly weigh-in",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTheme.colors.primaryText
                                )
                                Text(
                                    text = "Log a quick check-in to keep your weight curve and calorie targets calibrated.",
                                    fontSize = 11.5.sp,
                                    color = AuraTheme.colors.secondaryText
                                )
                            }
                        }
                    }
                }
            }

            // Deload offer or active banner
            if (user.deloadActive || deloadRec.shouldOffer) {
                item {
                    val blue = Color(0xFF3898FF)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(blue.copy(alpha = 0.11f))
                            .border(1.dp, blue.copy(alpha = 0.38f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                            .testTag("deload_coach_card"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = "Deload week",
                                tint = blue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (user.deloadActive) "DELOAD WEEK ACTIVE (-40% VOLUME)" else "DELOAD WEEK RECOMMENDED",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = blue
                            )
                        }
                        Text(
                            text = if (user.deloadActive) {
                                "Your sets are reduced by ~40% this week so joints and nervous system recover cleanly."
                            } else {
                                deloadRec.reason
                            },
                            fontSize = 12.5.sp,
                            color = AuraTheme.colors.primaryText
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (user.deloadActive) {
                                OutlinedButton(
                                    onClick = { viewModel.endDeloadWeek() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("end_deload_button")
                                ) {
                                    Text("Return to Normal Volume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.acceptDeloadWeek() },
                                    colors = ButtonDefaults.buttonColors(containerColor = blue, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("accept_deload_button")
                                ) {
                                    Text("Start Deload (-40% Sets)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.dismissDeloadForThisWeek() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("Not Now", fontSize = 12.sp, color = AuraTheme.colors.secondaryText)
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Summary Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AuraTheme.colors.card)
                        .border(
                            1.dp,
                            if (weeklySummary.isSunday) accent.copy(alpha = 0.5f) else AuraTheme.colors.cardBorder,
                            RoundedCornerShape(22.dp)
                        )
                        .padding(18.dp)
                        .testTag("weekly_summary_card"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (weeklySummary.isSunday) "SUNDAY WEEKLY SUMMARY" else "WEEKLY SUMMARY",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = weeklySummary.weekLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.primaryText
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AuraTheme.colors.surfaceSecondary)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${weeklySummary.workoutsDone}/${weeklySummary.workoutsPlanned} Workouts",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryStatTile(
                            label = "Volume",
                            value = "${ProgressionEngine.formatKg(weeklySummary.totalVolumeKg)} kg",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatTile(
                            label = "Protein",
                            value = "${weeklySummary.proteinConsistencyPct}%",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatTile(
                            label = "Water",
                            value = "${weeklySummary.waterConsistencyPct}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = weeklySummary.coachInsight,
                        fontSize = 12.5.sp,
                        color = AuraTheme.colors.primaryText,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "Next week focus: ${weeklySummary.nextWeekFocus}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        lineHeight = 16.sp
                    )
                }
            }

            // Weight Trend Chart (7 / 30 / 90 days) + Goal Line
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AuraTheme.colors.card)
                        .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .testTag("weight_trend_card"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "WEIGHT TREND",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            val latestWeight = weightPoints.lastOrNull()?.weightKg ?: user.weight
                            Text(
                                text = "${ProgressionEngine.formatKg(latestWeight)} kg",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(7, 30, 90).forEach { days ->
                                FilterChip(
                                    selected = selectedWeightWindow == days,
                                    onClick = { selectedWeightWindow = days },
                                    label = { Text("${days}d", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accent.copy(alpha = 0.2f),
                                        selectedLabelColor = accent
                                    ),
                                    modifier = Modifier.testTag("weight_window_${days}d")
                                )
                            }
                        }
                    }

                    WeightTrendLineChartCanvas(
                        points = weightPoints,
                        goalWeightKg = user.targetWeight,
                        accentColor = accent
                    )

                    if (checkIns.isNotEmpty()) {
                        Text(
                            text = "Recent check-ins",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTheme.colors.secondaryText
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            checkIns.take(10).forEach { entry ->
                                CheckInEntryChip(
                                    entry = entry,
                                    onDelete = { viewModel.deleteBodyCheckIn(entry.id, entry.photoPath) }
                                )
                            }
                        }
                    }
                }
            }

            // Per-Exercise Strength Chart
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AuraTheme.colors.card)
                        .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .testTag("exercise_strength_card"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STRENGTH TREND",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = if (selectedExercise.isNotBlank()) selectedExercise else "Tap an exercise",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.primaryText
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = showEstimated1Rm,
                                onClick = { showEstimated1Rm = true },
                                label = { Text("1RM", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = !showEstimated1Rm,
                                onClick = { showEstimated1Rm = false },
                                label = { Text("Best Set", fontSize = 11.sp) }
                            )
                        }
                    }

                    if (loggedExercises.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            loggedExercises.forEach { exName ->
                                val isSel = exName == selectedExercise
                                Box(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) accent else AuraTheme.colors.surfaceSecondary)
                                        .clickable { selectedExercise = exName }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = exName,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSel) accentContrast else AuraTheme.colors.primaryText
                                    )
                                }
                            }
                        }
                    }

                    ExerciseStrengthLineChartCanvas(
                        points = strengthPoints,
                        showEstimated1Rm = showEstimated1Rm,
                        accentColor = accent
                    )
                }
            }

            // Personal Bests List
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AuraTheme.colors.card)
                        .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .testTag("personal_bests_card"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERSONAL BESTS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        Text(
                            text = "${personalBests.size} lifts",
                            fontSize = 11.sp,
                            color = AuraTheme.colors.secondaryText
                        )
                    }

                    if (personalBests.isEmpty()) {
                        Text(
                            text = "Complete working sets to record your personal bests with dates.",
                            fontSize = 12.5.sp,
                            color = AuraTheme.colors.secondaryText
                        )
                    } else {
                        personalBests.take(12).forEach { pb ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AuraTheme.colors.surfaceSecondary)
                                    .clickable { selectedExercise = pb.exerciseName }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = pb.exerciseName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AuraTheme.colors.primaryText
                                        )
                                        Text(
                                            text = ProgressAnalyticsEngine.formatShortLabel(pb.dateKey),
                                            fontSize = 11.sp,
                                            color = AuraTheme.colors.secondaryText
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val setText = if (pb.bestWeightKg > 0.0) {
                                        "${ProgressionEngine.formatKg(pb.bestWeightKg)} kg × ${pb.bestReps}"
                                    } else {
                                        "${pb.bestReps} reps"
                                    }
                                    Text(
                                        text = setText,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AuraTheme.colors.primaryText
                                    )
                                    if (pb.estimated1RmKg > 0.0) {
                                        Text(
                                            text = "1RM ${ProgressionEngine.formatKg(pb.estimated1RmKg)} kg",
                                            fontSize = 10.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = accent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 12-Week Consistency Heatmap + Smart Streak note
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AuraTheme.colors.card)
                        .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .testTag("consistency_heatmap_card"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "12-WEEK CONSISTENCY",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = "Smart Streak: $smartStreak days",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.primaryText
                            )
                        }
                        Text(
                            text = "1 rest day grace",
                            fontSize = 11.sp,
                            color = AuraTheme.colors.secondaryText
                        )
                    }

                    ConsistencyHeatmapGrid(
                        weeks = heatmapWeeks,
                        accentColor = accent
                    )
                }
            }

            // 15 Simple Achievements
            item {
                val unlockedCount = achievements.count { it.isUnlocked }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(AuraTheme.colors.card)
                        .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                        .testTag("achievements_card"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACHIEVEMENTS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        Text(
                            text = "$unlockedCount / ${achievements.size} Unlocked",
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }

                    achievements.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (item.isUnlocked) accent.copy(alpha = 0.1f)
                                    else AuraTheme.colors.surfaceSecondary
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = if (item.isUnlocked) "Unlocked" else "Locked",
                                tint = if (item.isUnlocked) accent else AuraTheme.colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.definition.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraTheme.colors.primaryText
                                    )
                                    Text(
                                        text = "${item.currentValue.coerceAtMost(item.definition.targetValue)}/${item.definition.targetValue}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (item.isUnlocked) accent else AuraTheme.colors.secondaryText
                                    )
                                }
                                Text(
                                    text = item.definition.description,
                                    fontSize = 11.sp,
                                    color = AuraTheme.colors.secondaryText
                                )
                                LinearProgressIndicator(
                                    progress = { item.progressFraction },
                                    color = accent,
                                    trackColor = AuraTheme.colors.cardBorder,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isCheckInDialogOpen) {
            BodyCheckInDialog(
                currentWeightKg = user.weight,
                onSave = { w, waist, chest, arm, uri, note ->
                    viewModel.saveBodyCheckIn(w, waist, chest, arm, uri, note)
                    isCheckInDialogOpen = false
                },
                onDismiss = { isCheckInDialogOpen = false }
            )
        }
    }
}

@Composable
private fun SummaryStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AuraTheme.colors.surfaceSecondary)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = AuraTheme.colors.primaryText
        )
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = AuraTheme.colors.secondaryText
        )
    }
}

@Composable
private fun CheckInEntryChip(
    entry: BodyCheckIn,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AuraTheme.colors.surfaceSecondary)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!entry.photoPath.isNullOrBlank()) {
            AsyncImage(
                model = File(entry.photoPath),
                contentDescription = "Progress photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        Column {
            Text(
                text = "${ProgressionEngine.formatKg(entry.weightKg)} kg",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTheme.colors.primaryText
            )
            Text(
                text = ProgressAnalyticsEngine.formatShortLabel(entry.dateKey),
                fontSize = 10.5.sp,
                color = AuraTheme.colors.secondaryText
            )
            val meas = listOfNotNull(
                entry.waistCm?.let { "W ${it.toInt()}" },
                entry.chestCm?.let { "C ${it.toInt()}" },
                entry.armCm?.let { "A ${it.toInt()}" }
            ).joinToString(" • ")
            if (meas.isNotBlank()) {
                Text(
                    text = meas,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AuraTheme.colors.accent
                )
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.minimumInteractiveComponentSize().size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete check-in",
                tint = AuraTheme.colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
