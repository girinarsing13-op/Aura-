package com.example.aura.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.UserProfile
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.ui.AuraViewModel
import com.example.aura.ui.components.AuraCard
import com.example.aura.ui.components.AuraPillButton
import com.example.aura.ui.components.AuraRing
import com.example.aura.ui.components.AuraStaggeredEntrance
import com.example.aura.ui.components.rememberReducedMotion
import com.example.aura.ui.theme.AuraTheme
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private const val STEP_WELCOME = 0
private const val STEP_FIRST_QUESTION = 1
private const val STEP_LAST_QUESTION = 9
private const val STEP_BUILDING = 10
private const val STEP_REVEAL = 11

@Composable
fun OnboardingScreen(
    viewModel: AuraViewModel,
    onFinished: () -> Unit,
    onOpenParq: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val draft by viewModel.draftProfile.collectAsState()
    val currentStep by viewModel.onboardingStep.collectAsState()
    val backupStatus by viewModel.backupStatusMessage.collectAsState()
    val reducedMotion = rememberReducedMotion()
    val colors = AuraTheme.colors

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackupFromUri(uri)
        }
    }

    BackHandler(enabled = currentStep > STEP_WELCOME && currentStep != STEP_BUILDING) {
        val target = if (currentStep == STEP_REVEAL) STEP_LAST_QUESTION else currentStep - 1
        viewModel.setOnboardingStep(target)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            when (currentStep) {
                STEP_WELCOME -> {
                    WelcomeOnboardingScreen(
                        reducedMotion = reducedMotion,
                        backupMessage = backupStatus,
                        onGetStarted = {
                            viewModel.clearBackupStatus()
                            viewModel.setOnboardingStep(STEP_FIRST_QUESTION)
                        },
                        onRestoreBackup = {
                            restoreLauncher.launch(arrayOf("application/json", "*/*"))
                        }
                    )
                }

                in STEP_FIRST_QUESTION..STEP_LAST_QUESTION -> {
                    QuestionOnboardingScaffold(
                        stepNumber = currentStep,
                        draft = draft,
                        reducedMotion = reducedMotion,
                        onBack = { viewModel.setOnboardingStep(currentStep - 1) },
                        onContinue = {
                            if (currentStep == 5) {
                                // Refresh suggested workout slot when transitioning from Day to Setup/Schedule
                                val suggestion = WorkoutScheduleEngine.suggestBestWorkoutTime(
                                    wakeTimeMinutes = draft.wakeTimeMinutes,
                                    hasWorkOrSchoolHours = draft.hasWorkOrSchoolHours,
                                    workStartMinutes = draft.workStartMinutes,
                                    workEndMinutes = draft.workEndMinutes,
                                    sleepTimeMinutes = draft.sleepTimeMinutes,
                                    sessionDurationMinutes = draft.sessionDurationMinutes
                                )
                                viewModel.updateDraft { it.copy(preferredWorkoutMinutes = suggestion.suggestedMinutes) }
                            }
                            viewModel.setOnboardingStep(currentStep + 1)
                        },
                        onUpdate = { viewModel.updateDraft(it) },
                        onToggleAllergy = { viewModel.toggleAllergy(it) }
                    )
                }

                STEP_BUILDING -> {
                    BuildingPlanScreen(
                        draft = draft,
                        reducedMotion = reducedMotion,
                        onAutoAdvance = { viewModel.setOnboardingStep(STEP_REVEAL) }
                    )
                }

                else -> {
                    PlanReadyRevealScreen(
                        draft = draft,
                        reducedMotion = reducedMotion,
                        onStartFirstWorkout = {
                            viewModel.completeOnboarding(draft)
                            onFinished()
                        }
                    )
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 0 — WELCOME
// ============================================================================

@Composable
private fun WelcomeOnboardingScreen(
    reducedMotion: Boolean,
    backupMessage: String?,
    onGetStarted: () -> Unit,
    onRestoreBackup: () -> Unit
) {
    val colors = AuraTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AuraStaggeredEntrance(index = 0, animationKey = "welcome", reducedMotion = reducedMotion) {
                WelcomePulseRings(reducedMotion = reducedMotion)
            }

            AuraStaggeredEntrance(index = 1, animationKey = "welcome", reducedMotion = reducedMotion) {
                Text(
                    text = "AURA",
                    color = colors.primaryText,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp
                )
            }

            AuraStaggeredEntrance(index = 2, animationKey = "welcome", reducedMotion = reducedMotion) {
                Text(
                    text = "Your personal coach.\nFree, in your pocket.",
                    color = colors.mutedText,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (!backupMessage.isNullOrBlank()) {
                Text(
                    text = backupMessage,
                    color = colors.accent,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuraPillButton(
                text = "Get started",
                onClick = onGetStarted,
                modifier = Modifier.testTag("onboarding_get_started")
            )
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onRestoreBackup)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("onboarding_restore_account"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "I already have an account",
                    color = colors.mutedText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// STEPS 1..9 SHARED SCAFFOLD
// ============================================================================

@Composable
private fun QuestionOnboardingScaffold(
    stepNumber: Int,
    draft: UserProfile,
    reducedMotion: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit,
    onToggleAllergy: (String) -> Unit
) {
    val colors = AuraTheme.colors
    val scrollState = rememberScrollState()

    LaunchedEffect(stepNumber) {
        scrollState.scrollTo(0)
    }

    val (title, subtitle) = when (stepNumber) {
        1 -> "Let's get to know you" to "Your coach needs the basics first."
        2 -> "Height and weight" to "Slide to set. This sets your calories and protein."
        3 -> "What is your goal?" to "We build everything around it."
        4 -> "Your training so far" to "Be honest. We start where you are."
        5 -> "Tell us about your day" to "Your life decides when and how hard you train."
        6 -> "Where do you train?" to "You can change this any time."
        7 -> {
            val env = draft.environment.lowercase()
            val q = when {
                env.contains("calisthenic") -> "What calisthenics gear do you have?"
                env.contains("home") -> "What equipment do you have?"
                else -> "What does your gym have?"
            }
            q to "We only pick exercises you can actually do."
        }
        8 -> "How do you eat?" to "Your diet plan will match this."
        else -> "When do you want to train?" to "Best slot found from your free time."
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 104.dp)
        ) {
            // Top bar: Back arrow + 9-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("onboarding_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = colors.mutedText,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..9) {
                        val barColor by animateColorAsState(
                            targetValue = if (i <= stepNumber) colors.accent else colors.trackMuted,
                            animationSpec = tween(250),
                            label = "stepBar$i"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(barColor)
                        )
                    }
                }
            }

            AuraStaggeredEntrance(index = 0, animationKey = stepNumber, reducedMotion = reducedMotion) {
                Column {
                    Text(
                        text = "Step $stepNumber of 9",
                        color = colors.mutedText,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = title,
                        color = colors.primaryText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                }
            }

            AuraStaggeredEntrance(index = 1, animationKey = stepNumber, reducedMotion = reducedMotion) {
                Text(
                    text = subtitle,
                    color = colors.mutedText,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                )
            }

            when (stepNumber) {
                1 -> OnboardingAboutStep(draft, reducedMotion, onUpdate)
                2 -> OnboardingBodyStep(draft, reducedMotion, onUpdate)
                3 -> OnboardingGoalStep(draft, reducedMotion, onUpdate)
                4 -> OnboardingExperienceStep(draft, reducedMotion, onUpdate)
                5 -> OnboardingDayLifeStep(draft, reducedMotion, onUpdate)
                6 -> OnboardingSetupStep(draft, reducedMotion, onUpdate)
                7 -> OnboardingEquipmentStep(draft, reducedMotion, onUpdate)
                8 -> OnboardingFoodStep(draft, reducedMotion, onToggleAllergy, onUpdate)
                9 -> OnboardingScheduleStep(draft, reducedMotion, onUpdate)
            }
        }

        // Fixed bottom CTA
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(colors.background.copy(alpha = 0.96f))
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp)
        ) {
            AuraPillButton(
                text = if (stepNumber == STEP_LAST_QUESTION) "Build my plan" else "Continue",
                onClick = onContinue,
                modifier = Modifier.testTag("onboarding_continue_button")
            )
        }
    }
}

// ============================================================================
// SCREEN 10 — BUILDING YOUR PLAN (Spinner + Staggered Checklist -> Auto-Advance)
// ============================================================================

@Composable
private fun BuildingPlanScreen(
    draft: UserProfile,
    reducedMotion: Boolean,
    onAutoAdvance: () -> Unit
) {
    val colors = AuraTheme.colors
    var revealedCount by remember { mutableIntStateOf(0) }

    val placePhrase = when {
        draft.environment.contains("calisthenic", ignoreCase = true) -> "Choosing calisthenics moves"
        draft.environment.contains("home", ignoreCase = true) -> "Choosing exercises for home"
        else -> "Choosing exercises for your gym"
    }
    val checklist = remember(placePhrase) {
        listOf(
            "Calculating your calories",
            placePhrase,
            "Planning your week around your day",
            "Writing your diet"
        )
    }

    LaunchedEffect(Unit) {
        val stepDelayMs = if (reducedMotion) 200L else 800L
        for (step in 1..checklist.size) {
            delay(stepDelayMs)
            revealedCount = step
        }
        delay(if (reducedMotion) 250L else 700L)
        onAutoAdvance()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp),
            contentAlignment = Alignment.Center
        ) {
            BuildingPlanSpinner(reducedMotion = reducedMotion)
        }

        Text(
            text = "Building your plan",
            color = colors.primaryText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            checklist.forEachIndexed { idx, label ->
                val isVisible = revealedCount > idx
                val isDone = revealedCount > idx + 1 || (idx == checklist.lastIndex && revealedCount >= checklist.size)
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(if (reducedMotion) 0 else 500)) +
                        slideInVertically(tween(if (reducedMotion) 0 else 500)) { if (reducedMotion) 0 else 28 }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (isDone) "✓" else "…",
                            color = if (isDone) colors.accent else colors.mutedText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(18.dp)
                        )
                        Text(
                            text = label,
                            color = colors.primaryText,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SCREEN 11 — PLAN READY REVEAL
// ============================================================================

@Composable
private fun PlanReadyRevealScreen(
    draft: UserProfile,
    reducedMotion: Boolean,
    onStartFirstWorkout: () -> Unit
) {
    val colors = AuraTheme.colors
    val scrollState = rememberScrollState()

    val bmr = remember(draft) { FitnessCalcEngine.calcBMR(draft.weight, draft.height, draft.age, draft.gender) }
    val tdee = remember(bmr, draft.daysPerWeek) { FitnessCalcEngine.calcTDEE(bmr, draft.daysPerWeek) }
    val calResult = remember(tdee, draft) {
        FitnessCalcEngine.calcSafeCalories(
            tdee = tdee,
            goal = draft.goal,
            bmr = bmr,
            weightKg = draft.weight,
            targetWeightKg = draft.targetWeight,
            durationMonths = draft.durationMonths
        )
    }
    val macros = remember(calResult.targetCals, draft.weight, draft.goal) {
        FitnessCalcEngine.calcMacroTargets(calResult.targetCals, draft.weight, draft.goal)
    }
    val waterLiters = remember(draft.weight) {
        FitnessCalcEngine.calcWaterTargetLiters(draft.weight, isWorkoutDay = true)
    }
    val whyNote = remember(draft, bmr, tdee, calResult.targetCals, macros.protein, waterLiters) {
        FitnessCalcEngine.buildWhyTheseNumbersSummary(
            profile = draft,
            bmr = bmr,
            tdee = tdee,
            targetCals = calResult.targetCals,
            proteinG = macros.protein,
            waterLiters = waterLiters,
            isWorkoutDay = true
        )
    }

    val formattedCals = remember(calResult.targetCals) {
        NumberFormat.getIntegerInstance(Locale.US).format(calResult.targetCals)
    }
    val formattedWater = remember(waterLiters) {
        String.format(Locale.US, "%.1f L", waterLiters)
    }
    val placeLabel = when {
        draft.environment.contains("calisthenic", ignoreCase = true) -> "Calisthenics"
        draft.environment.contains("home", ignoreCase = true) -> "Home"
        else -> "Gym"
    }
    val goalLabel = when (FitnessCalcEngine.normalizeGoal(draft.goal)) {
        "Muscle Gain" -> "Build muscle"
        "Fat Loss" -> "Lose fat"
        "Recomposition" -> "Recomposition"
        else -> "Stay fit"
    }
    val splitHeadline = "${draft.daysPerWeek}-day ${draft.trainingSplit.substringBefore(" (").substringBefore(" –")}"
    val timeLabel = WorkoutScheduleEngine.formatTimeMinutes(draft.preferredWorkoutMinutes)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 104.dp)
        ) {
            AuraStaggeredEntrance(index = 0, animationKey = "reveal", reducedMotion = reducedMotion) {
                Column {
                    Text(
                        text = "READY",
                        color = colors.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your plan is ready",
                        color = colors.primaryText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AuraStaggeredEntrance(index = 1, animationKey = "reveal", reducedMotion = reducedMotion) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RevealMetricRingCard(
                        value = formattedCals,
                        label = "kcal / day",
                        progress = 0.85f,
                        ringColor = colors.accent,
                        reducedMotion = reducedMotion,
                        modifier = Modifier.weight(1f)
                    )
                    RevealMetricRingCard(
                        value = "${macros.protein} g",
                        label = "protein",
                        progress = 0.75f,
                        ringColor = colors.proteinGreen,
                        reducedMotion = reducedMotion,
                        modifier = Modifier.weight(1f)
                    )
                    RevealMetricRingCard(
                        value = formattedWater,
                        label = "water",
                        progress = 0.65f,
                        ringColor = colors.waterBlue,
                        reducedMotion = reducedMotion,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AuraStaggeredEntrance(index = 2, animationKey = "reveal", reducedMotion = reducedMotion) {
                AuraCard(
                    modifier = Modifier.fillMaxWidth(),
                    highlighted = true
                ) {
                    Text(
                        text = splitHeadline,
                        color = colors.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$placeLabel · ${draft.sessionDurationMinutes} min · $timeLabel · $goalLabel",
                        color = colors.mutedText,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AuraStaggeredEntrance(index = 3, animationKey = "reveal", reducedMotion = reducedMotion) {
                AuraCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Why these numbers",
                        color = colors.primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = whyNote,
                        color = colors.mutedText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(colors.background.copy(alpha = 0.96f))
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp)
        ) {
            AuraPillButton(
                text = "Start my first workout",
                onClick = onStartFirstWorkout,
                modifier = Modifier.testTag("onboarding_finish_button")
            )
        }
    }
}

@Composable
private fun RevealMetricRingCard(
    value: String,
    label: String,
    progress: Float,
    ringColor: androidx.compose.ui.graphics.Color,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    AuraCard(
        modifier = modifier,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuraRing(
                progress = progress,
                color = ringColor,
                size = 64.dp,
                strokeWidth = 6.dp,
                reducedMotion = reducedMotion
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = colors.primaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = label,
                color = colors.mutedText,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}
