package com.example.aura.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.aura.data.model.EquipmentOptions
import com.example.aura.data.model.UserProfile
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.ui.components.AuraCard
import com.example.aura.ui.components.AuraChip
import com.example.aura.ui.components.AuraNumberChip
import com.example.aura.ui.components.AuraStaggeredEntrance
import com.example.aura.ui.components.AuraToggleSwitch
import com.example.aura.ui.theme.AuraTheme
import com.example.aura.ui.theme.InterFontFamily
import kotlin.math.abs
import kotlin.math.roundToInt

// ============================================================================
// STEP 1 OF 9 — ABOUT YOU
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingAboutStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AuraStaggeredEntrance(index = 2, animationKey = 1, reducedMotion = reducedMotion) {
            AuraCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = "Name", color = colors.mutedText, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(3.dp))
                BasicTextField(
                    value = draft.name,
                    onValueChange = { value ->
                        if (value.length <= 40) onUpdate { it.copy(name = value) }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    cursorBrush = SolidColor(colors.accent),
                    textStyle = TextStyle(
                        fontFamily = InterFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    ),
                    decorationBox = { innerTextField ->
                        if (draft.name.isEmpty()) {
                            Text(
                                text = "Your name",
                                color = colors.mutedText.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )
            }
        }

        AuraStaggeredEntrance(index = 3, animationKey = 1, reducedMotion = reducedMotion) {
            AuraCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Age", color = colors.mutedText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${draft.age} years",
                            color = colors.primaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("onboarding_age_value")
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepMiniButton(
                            label = "−",
                            onClick = {
                                onUpdate { it.copy(age = (it.age - 1).coerceIn(13, 90)) }
                            },
                            modifier = Modifier.testTag("onboarding_age_minus")
                        )
                        StepMiniButton(
                            label = "+",
                            onClick = {
                                onUpdate { it.copy(age = (it.age + 1).coerceIn(13, 90)) }
                            },
                            modifier = Modifier.testTag("onboarding_age_plus")
                        )
                    }
                }
                if (draft.age < 18) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Under 18: train with supervision and focus on healthy fuel.",
                        color = colors.warningCoral,
                        fontSize = 12.sp
                    )
                }
            }
        }

        AuraStaggeredEntrance(index = 4, animationKey = 1, reducedMotion = reducedMotion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Gender",
                    color = colors.mutedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { option ->
                        AuraChip(
                            label = option,
                            selected = draft.gender.equals(option, ignoreCase = true),
                            onClick = { onUpdate { it.copy(gender = option) } },
                            modifier = Modifier.testTag("gender_chip_${option.lowercase()}")
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// STEP 2 OF 9 — YOUR BODY (Swipeable Rulers with Haptic Ticks)
// ============================================================================

@Composable
fun OnboardingBodyStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors
    val heightCm = draft.height.roundToInt().coerceIn(120, 225)
    val weightKg = draft.weight.roundToInt().coerceIn(35, 180)
    val targetKg = (draft.targetWeight ?: draft.weight).roundToInt().coerceIn(35, 180)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AuraStaggeredEntrance(index = 2, animationKey = 2, reducedMotion = reducedMotion) {
            AuraCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Height", color = colors.mutedText, fontSize = 12.sp)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.primaryText, fontSize = 44.sp, fontWeight = FontWeight.Bold)) {
                                append("$heightCm")
                            }
                            withStyle(SpanStyle(color = colors.mutedText, fontSize = 16.sp, fontWeight = FontWeight.Normal)) {
                                append(" cm")
                            }
                        },
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("onboarding_height_value")
                    )
                    AuraHorizontalRuler(
                        value = heightCm,
                        minValue = 120,
                        maxValue = 225,
                        onValueChange = { next -> onUpdate { it.copy(height = next.toDouble()) } },
                        reducedMotion = reducedMotion,
                        modifier = Modifier.testTag("onboarding_height_ruler")
                    )
                }
            }
        }

        AuraStaggeredEntrance(index = 3, animationKey = 2, reducedMotion = reducedMotion) {
            AuraCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Weight", color = colors.mutedText, fontSize = 12.sp)
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = colors.primaryText, fontSize = 44.sp, fontWeight = FontWeight.Bold)) {
                                append("$weightKg")
                            }
                            withStyle(SpanStyle(color = colors.mutedText, fontSize = 16.sp, fontWeight = FontWeight.Normal)) {
                                append(" kg")
                            }
                        },
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("onboarding_weight_value")
                    )
                    AuraHorizontalRuler(
                        value = weightKg,
                        minValue = 35,
                        maxValue = 180,
                        onValueChange = { next -> onUpdate { it.copy(weight = next.toDouble()) } },
                        reducedMotion = reducedMotion,
                        modifier = Modifier.testTag("onboarding_weight_ruler")
                    )
                }
            }
        }

        AuraStaggeredEntrance(index = 4, animationKey = 2, reducedMotion = reducedMotion) {
            AuraCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Target weight (optional)", color = colors.mutedText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "$targetKg kg",
                            color = colors.primaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("onboarding_target_weight_value")
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepMiniButton(
                            label = "−",
                            onClick = {
                                val next = (targetKg - 1).coerceIn(35, 180)
                                onUpdate { it.copy(targetWeight = next.toDouble()) }
                            },
                            modifier = Modifier.testTag("onboarding_target_weight_minus")
                        )
                        StepMiniButton(
                            label = "+",
                            onClick = {
                                val next = (targetKg + 1).coerceIn(35, 180)
                                onUpdate { it.copy(targetWeight = next.toDouble()) }
                            },
                            modifier = Modifier.testTag("onboarding_target_weight_plus")
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// STEP 3 OF 9 — YOUR GOAL
// ============================================================================

@Composable
fun OnboardingGoalStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors
    data class GoalOption(val title: String, val subtitle: String, val engineKey: String)
    val options = remember {
        listOf(
            GoalOption("Build muscle", "Gain size and strength, stay lean", "Muscle Gain"),
            GoalOption("Lose fat", "Cut weight, keep your muscle", "Fat Loss"),
            GoalOption("Recomposition", "Lose fat and build muscle together", "Recomposition"),
            GoalOption("Stay fit", "General health and energy", "General Fitness")
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { idx, opt ->
            val selected = draft.goal.equals(opt.engineKey, ignoreCase = true)
            AuraStaggeredEntrance(index = idx + 2, animationKey = 3, reducedMotion = reducedMotion) {
                AuraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_card_${opt.engineKey.lowercase().replace(" ", "_")}"),
                    selected = selected,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    onClick = {
                        onUpdate { curr ->
                            val adjustedTarget = when (opt.engineKey) {
                                "Muscle Gain" -> if ((curr.targetWeight ?: curr.weight) <= curr.weight) curr.weight + 4.0 else curr.targetWeight
                                "Fat Loss" -> if ((curr.targetWeight ?: curr.weight) >= curr.weight) (curr.weight - 4.0).coerceAtLeast(40.0) else curr.targetWeight
                                else -> curr.weight
                            }
                            curr.copy(goal = opt.engineKey, targetWeight = adjustedTarget)
                        }
                    }
                ) {
                    Text(
                        text = opt.title,
                        color = colors.primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = opt.subtitle,
                        color = colors.mutedText,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ============================================================================
// STEP 4 OF 9 — EXPERIENCE & PROTECTED JOINTS
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingExperienceStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors
    val levels = remember {
        listOf(
            "Beginner" to "New to training",
            "Intermediate" to "6 to 24 months, know the main lifts",
            "Advanced" to "2+ years, training with a plan"
        )
    }
    val joints = remember { listOf("Knees", "Lower back", "Shoulders", "Wrists", "None") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        levels.forEachIndexed { idx, (title, subtitle) ->
            val selected = draft.experience.equals(title, ignoreCase = true)
            AuraStaggeredEntrance(index = idx + 2, animationKey = 4, reducedMotion = reducedMotion) {
                AuraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("experience_card_${title.lowercase()}"),
                    selected = selected,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    onClick = { onUpdate { it.copy(experience = title) } }
                ) {
                    Text(
                        text = title,
                        color = colors.primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        color = colors.mutedText,
                        fontSize = 13.sp
                    )
                }
            }
        }

        AuraStaggeredEntrance(index = 5, animationKey = 4, reducedMotion = reducedMotion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Anything we should protect?",
                    color = colors.mutedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    joints.forEach { joint ->
                        val isNone = joint == "None"
                        val selected = if (isNone) {
                            draft.injuries.isEmpty() || draft.injuries.any { it.equals("None", ignoreCase = true) }
                        } else {
                            draft.injuries.any { it.equals(joint, ignoreCase = true) }
                        }
                        AuraChip(
                            label = joint,
                            selected = selected,
                            onClick = {
                                onUpdate { curr ->
                                    val nextList = if (isNone) {
                                        listOf("None")
                                    } else {
                                        val withoutNone = curr.injuries.filterNot { it.equals("None", ignoreCase = true) }
                                        if (withoutNone.any { it.equals(joint, ignoreCase = true) }) {
                                            val rem = withoutNone.filterNot { it.equals(joint, ignoreCase = true) }
                                            rem.ifEmpty { listOf("None") }
                                        } else {
                                            withoutNone + joint
                                        }
                                    }
                                    curr.copy(injuries = nextList)
                                }
                            },
                            modifier = Modifier.testTag("injury_chip_${joint.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// STEP 5 OF 9 — YOUR DAY (Wake, Sleep, Busy Hours, 24h Timeline, Activity)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingDayLifeStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors
    var showWakeDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showBusyDialog by remember { mutableStateOf(false) }

    val activityOptions = remember {
        listOf(
            "Sitting" to "Sedentary",
            "On my feet" to "Moderately Active",
            "Physical work" to "Very Active"
        )
    }

    val busySummary = if (draft.hasWorkOrSchoolHours) {
        "Work / College · ${WorkoutScheduleEngine.formatTimeMinutes(draft.workStartMinutes)} to ${WorkoutScheduleEngine.formatTimeMinutes(draft.workEndMinutes)}"
    } else {
        "Flexible day · No fixed busy block"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AuraStaggeredEntrance(index = 2, animationKey = 5, reducedMotion = reducedMotion) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuraCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("onboarding_wake_card"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    onClick = { showWakeDialog = true }
                ) {
                    Text(text = "Wake up", color = colors.mutedText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = WorkoutScheduleEngine.formatTimeMinutes(draft.wakeTimeMinutes),
                        color = colors.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AuraCard(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("onboarding_sleep_card"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    onClick = { showSleepDialog = true }
                ) {
                    Text(text = "Sleep", color = colors.mutedText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = WorkoutScheduleEngine.formatTimeMinutes(draft.sleepTimeMinutes),
                        color = colors.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        AuraStaggeredEntrance(index = 3, animationKey = 5, reducedMotion = reducedMotion) {
            AuraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_busy_hours_card"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                onClick = { showBusyDialog = true }
            ) {
                Text(text = "Busy hours", color = colors.mutedText, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = busySummary,
                    color = colors.primaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        AuraStaggeredEntrance(index = 4, animationKey = 5, reducedMotion = reducedMotion) {
            AuraDayTimelineCard(
                wakeMinutes = draft.wakeTimeMinutes,
                sleepMinutes = draft.sleepTimeMinutes,
                hasBusyHours = draft.hasWorkOrSchoolHours,
                busyStartMinutes = draft.workStartMinutes,
                busyEndMinutes = draft.workEndMinutes,
                busyLabel = if (draft.hasWorkOrSchoolHours) "Work / College" else "Busy",
                reducedMotion = reducedMotion
            )
        }

        AuraStaggeredEntrance(index = 5, animationKey = 5, reducedMotion = reducedMotion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Your day is mostly",
                    color = colors.mutedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activityOptions.forEach { (label, engineValue) ->
                        val selected = draft.lifestyleActivity.equals(engineValue, ignoreCase = true) ||
                            (label == "Sitting" && draft.lifestyleActivity.contains("Sedentary", ignoreCase = true)) ||
                            (label == "On my feet" && draft.lifestyleActivity.contains("Moderate", ignoreCase = true)) ||
                            (label == "Physical work" && draft.lifestyleActivity.contains("Active", ignoreCase = true) && !draft.lifestyleActivity.contains("Moderate", ignoreCase = true))
                        AuraChip(
                            label = label,
                            selected = selected,
                            onClick = { onUpdate { it.copy(lifestyleActivity = engineValue) } },
                            modifier = Modifier.testTag("activity_chip_${label.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }
    }

    if (showWakeDialog) {
        OnboardingTimePickerDialog(
            title = "Wake up time",
            selectedMinutes = draft.wakeTimeMinutes,
            minHour = 4,
            maxHour = 12,
            onSelect = { mins ->
                onUpdate { curr ->
                    val sleepDuration = computeSleepHours(mins, curr.sleepTimeMinutes)
                    curr.copy(wakeTimeMinutes = mins, sleepHours = sleepDuration)
                }
            },
            onDismiss = { showWakeDialog = false }
        )
    }

    if (showSleepDialog) {
        OnboardingTimePickerDialog(
            title = "Sleep time",
            selectedMinutes = draft.sleepTimeMinutes,
            minHour = 19,
            maxHour = 25,
            onSelect = { mins ->
                onUpdate { curr ->
                    val sleepDuration = computeSleepHours(curr.wakeTimeMinutes, mins)
                    curr.copy(sleepTimeMinutes = mins, sleepHours = sleepDuration)
                }
            },
            onDismiss = { showSleepDialog = false }
        )
    }

    if (showBusyDialog) {
        BusyHoursEditorDialog(
            hasBusyHours = draft.hasWorkOrSchoolHours,
            startMinutes = draft.workStartMinutes,
            endMinutes = draft.workEndMinutes,
            onSave = { enabled, start, end ->
                onUpdate { curr ->
                    curr.copy(
                        hasWorkOrSchoolHours = enabled,
                        workStartMinutes = start,
                        workEndMinutes = end
                    )
                }
            },
            onDismiss = { showBusyDialog = false }
        )
    }
}

private fun computeSleepHours(wakeMin: Int, sleepMin: Int): Double {
    val awakeMinutes = if (sleepMin > wakeMin) sleepMin - wakeMin else (sleepMin + 1440) - wakeMin
    val sleepMinutes = (1440 - awakeMinutes).coerceIn(4 * 60, 11 * 60)
    return (sleepMinutes / 60.0 * 2.0).roundToInt() / 2.0
}

// ============================================================================
// STEP 6 OF 9 — WHERE YOU TRAIN, DAYS PER WEEK, SESSION LENGTH
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingSetupStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors
    data class PlaceOption(val title: String, val subtitle: String, val engineKey: String)
    val places = remember {
        listOf(
            PlaceOption("Gym", "Machines, barbells, dumbbells", "Full Gym"),
            PlaceOption("Home", "Your own equipment", "Home Workout"),
            PlaceOption("Calisthenics only", "Bodyweight, bar and rings", "Calisthenics")
        )
    }
    val dayCounts = remember { listOf(2, 3, 4, 5, 6, 7) }
    val durations = remember { listOf(30, 45, 60, 75) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        places.forEachIndexed { idx, place ->
            val selected = draft.environment.equals(place.engineKey, ignoreCase = true)
            AuraStaggeredEntrance(index = idx + 2, animationKey = 6, reducedMotion = reducedMotion) {
                AuraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("place_card_${place.engineKey.lowercase().replace(" ", "_")}"),
                    selected = selected,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    onClick = { onUpdate { it.copy(environment = place.engineKey) } }
                ) {
                    Text(
                        text = place.title,
                        color = colors.primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = place.subtitle,
                        color = colors.mutedText,
                        fontSize = 13.sp
                    )
                }
            }
        }

        AuraStaggeredEntrance(index = 5, animationKey = 6, reducedMotion = reducedMotion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Days per week",
                    color = colors.mutedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dayCounts.forEach { count ->
                        AuraNumberChip(
                            value = count,
                            selected = draft.daysPerWeek == count,
                            onClick = {
                                onUpdate { curr ->
                                    curr.copy(
                                        daysPerWeek = count,
                                        preferredWorkoutDays = WorkoutScheduleEngine.adjustDaysForCount(
                                            curr.preferredWorkoutDays,
                                            count
                                        ),
                                        trainingSplit = recommendedSplitForDays(count)
                                    )
                                }
                            },
                            modifier = Modifier.testTag("days_chip_$count")
                        )
                    }
                }
            }
        }

        AuraStaggeredEntrance(index = 6, animationKey = 6, reducedMotion = reducedMotion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Session length",
                    color = colors.mutedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durations.forEach { mins ->
                        AuraChip(
                            label = "$mins min",
                            selected = draft.sessionDurationMinutes == mins,
                            onClick = { onUpdate { it.copy(sessionDurationMinutes = mins) } },
                            modifier = Modifier.testTag("duration_chip_$mins")
                        )
                    }
                }
            }
        }
    }
}

private fun recommendedSplitForDays(days: Int): String {
    return when (days) {
        2 -> "Upper / Lower (UL)"
        3 -> "Full Body"
        4 -> "4-Day Upper / Lower"
        5 -> "Upper / Lower / Push / Pull / Legs (ULPPL)"
        6, 7 -> "Push / Pull / Legs (PPL)"
        else -> "4-Day Upper / Lower"
    }
}

// ============================================================================
// STEP 7 OF 9 — EQUIPMENT CHECKLIST
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingEquipmentStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors
    val env = draft.environment.lowercase()
    val isHome = env.contains("home")
    val isCalisthenics = env.contains("calisthenic")

    val options = remember(isHome, isCalisthenics) {
        when {
            isCalisthenics -> EquipmentOptions.CALISTHENICS
            isHome -> EquipmentOptions.HOME
            else -> EquipmentOptions.GYM
        }
    }
    val activeList = when {
        isCalisthenics -> draft.calisthenicsEquipment
        isHome -> draft.homeEquipment
        else -> draft.gymEquipment
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AuraStaggeredEntrance(index = 2, animationKey = 7, reducedMotion = reducedMotion) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { item ->
                    val selected = activeList.any { it.equals(item, ignoreCase = true) }
                    AuraChip(
                        label = item,
                        selected = selected,
                        onClick = {
                            onUpdate { curr ->
                                fun toggle(list: List<String>): List<String> {
                                    return if (list.any { it.equals(item, ignoreCase = true) }) {
                                        list.filterNot { it.equals(item, ignoreCase = true) }
                                    } else {
                                        list + item
                                    }
                                }
                                when {
                                    isCalisthenics -> curr.copy(calisthenicsEquipment = toggle(curr.calisthenicsEquipment))
                                    isHome -> curr.copy(homeEquipment = toggle(curr.homeEquipment))
                                    else -> curr.copy(gymEquipment = toggle(curr.gymEquipment))
                                }
                            }
                        },
                        modifier = Modifier.testTag("equip_chip_${item.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        AuraStaggeredEntrance(index = 3, animationKey = 7, reducedMotion = reducedMotion) {
            Text(
                text = if (!isHome && !isCalisthenics) {
                    "Training at home? Go back and pick Home to see your own equipment list."
                } else {
                    "Bodyweight exercises are always included even if nothing is selected."
                },
                color = colors.mutedText,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// ============================================================================
// STEP 8 OF 9 — FOOD (Diet Type, Allergies, Meals per Day)
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingFoodStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onToggleAllergy: (String) -> Unit,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors
    val diets = remember {
        listOf(
            "Vegetarian" to "Vegetarian",
            "Eggetarian" to "Eggetarian",
            "Non-veg" to "Non-Vegetarian",
            "Vegan" to "Vegan"
        )
    }
    val allergies = remember { listOf("None", "Peanuts", "Dairy", "Gluten", "Seafood") }
    val mealCounts = remember { listOf(3, 4, 5, 6) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AuraStaggeredEntrance(index = 2, animationKey = 8, reducedMotion = reducedMotion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Diet type", color = colors.mutedText, fontSize = 12.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    diets.forEach { (label, engineVal) ->
                        AuraChip(
                            label = label,
                            selected = draft.dietPreference.equals(engineVal, ignoreCase = true),
                            onClick = { onUpdate { it.copy(dietPreference = engineVal) } },
                            modifier = Modifier.testTag("diet_chip_${label.lowercase()}")
                        )
                    }
                }
            }
        }

        AuraStaggeredEntrance(index = 3, animationKey = 8, reducedMotion = reducedMotion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Allergies",
                    color = colors.mutedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allergies.forEach { allergy ->
                        val selected = if (allergy == "None") {
                            draft.allergies.isEmpty() || draft.allergies.any { it.equals("None", ignoreCase = true) }
                        } else {
                            draft.allergies.any { it.equals(allergy, ignoreCase = true) }
                        }
                        AuraChip(
                            label = allergy,
                            selected = selected,
                            onClick = { onToggleAllergy(allergy) },
                            modifier = Modifier.testTag("allergy_chip_${allergy.lowercase()}")
                        )
                    }
                }
            }
        }

        AuraStaggeredEntrance(index = 4, animationKey = 8, reducedMotion = reducedMotion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Meals per day",
                    color = colors.mutedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    mealCounts.forEach { count ->
                        AuraNumberChip(
                            value = count,
                            selected = draft.mealsPerDay == count,
                            onClick = { onUpdate { it.copy(mealsPerDay = count) } },
                            modifier = Modifier.testTag("meals_chip_$count")
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// STEP 9 OF 9 — SCHEDULE (24h Pulsing Timeline + Suggested Slots + Reminder)
// ============================================================================

@Composable
fun OnboardingScheduleStep(
    draft: UserProfile,
    reducedMotion: Boolean,
    onUpdate: ((UserProfile) -> UserProfile) -> Unit
) {
    val colors = AuraTheme.colors
    val context = LocalContext.current

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onUpdate { it.copy(workoutReminderEnabled = granted) }
    }

    val suggestion = remember(
        draft.wakeTimeMinutes,
        draft.hasWorkOrSchoolHours,
        draft.workStartMinutes,
        draft.workEndMinutes,
        draft.sleepTimeMinutes,
        draft.sessionDurationMinutes
    ) {
        WorkoutScheduleEngine.suggestBestWorkoutTime(
            wakeTimeMinutes = draft.wakeTimeMinutes,
            hasWorkOrSchoolHours = draft.hasWorkOrSchoolHours,
            workStartMinutes = draft.workStartMinutes,
            workEndMinutes = draft.workEndMinutes,
            sleepTimeMinutes = draft.sleepTimeMinutes,
            sessionDurationMinutes = draft.sessionDurationMinutes
        )
    }

    data class SlotChoice(val title: String, val subtitle: String, val minutes: Int)
    val slotChoices = remember(suggestion, draft.wakeTimeMinutes, draft.sleepTimeMinutes, draft.hasWorkOrSchoolHours) {
        val bestMin = suggestion.suggestedMinutes
        val bestPeriod = periodLabelFor(bestMin)
        val bestChoice = SlotChoice(
            title = "$bestPeriod · ${WorkoutScheduleEngine.formatTimeMinutes(bestMin)}",
            subtitle = "${suggestion.reason.substringBefore('.')} · Best for you",
            minutes = bestMin
        )

        val morningCandidate = suggestion.freeSlotsMinutes.firstOrNull { it in (5 * 60 + 30)..(9 * 60) && abs(it - bestMin) >= 60 }
            ?: (draft.wakeTimeMinutes + 30).coerceIn(5 * 60 + 30, 8 * 60)
        val nightCandidate = suggestion.freeSlotsMinutes.lastOrNull { it >= (20 * 60) && abs(it - bestMin) >= 60 }
            ?: (draft.sleepTimeMinutes - 120).coerceIn(20 * 60, 22 * 60)

        val list = mutableListOf(bestChoice)
        if (list.none { abs(it.minutes - morningCandidate) < 30 }) {
            list.add(
                SlotChoice(
                    title = "Morning · ${WorkoutScheduleEngine.formatTimeMinutes(morningCandidate)}",
                    subtitle = if (draft.hasWorkOrSchoolHours) "Before your busy hours, needs earlier sleep" else "Quiet early session to start your day",
                    minutes = morningCandidate
                )
            )
        }
        if (list.none { abs(it.minutes - nightCandidate) < 30 }) {
            list.add(
                SlotChoice(
                    title = "Night · ${WorkoutScheduleEngine.formatTimeMinutes(nightCandidate)}",
                    subtitle = "Late session, may affect sleep",
                    minutes = nightCandidate
                )
            )
        }
        list
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AuraStaggeredEntrance(index = 2, animationKey = 9, reducedMotion = reducedMotion) {
            AuraDayTimelineCard(
                wakeMinutes = draft.wakeTimeMinutes,
                sleepMinutes = draft.sleepTimeMinutes,
                hasBusyHours = draft.hasWorkOrSchoolHours,
                busyStartMinutes = draft.workStartMinutes,
                busyEndMinutes = draft.workEndMinutes,
                busyLabel = if (draft.hasWorkOrSchoolHours) "Work / College" else "Busy",
                highlightWorkoutMinutes = draft.preferredWorkoutMinutes,
                workoutDurationMinutes = draft.sessionDurationMinutes,
                reducedMotion = reducedMotion
            )
        }

        slotChoices.forEachIndexed { idx, choice ->
            val isSelected = abs(draft.preferredWorkoutMinutes - choice.minutes) < 15
            AuraStaggeredEntrance(index = idx + 3, animationKey = 9, reducedMotion = reducedMotion) {
                AuraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_slot_$idx"),
                    selected = isSelected,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    onClick = { onUpdate { it.copy(preferredWorkoutMinutes = choice.minutes) } }
                ) {
                    Text(
                        text = choice.title,
                        color = colors.primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = choice.subtitle,
                        color = colors.mutedText,
                        fontSize = 13.sp
                    )
                }
            }
        }

        AuraStaggeredEntrance(index = 6, animationKey = 9, reducedMotion = reducedMotion) {
            AuraCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remind me 30 min before",
                        color = colors.primaryText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    AuraToggleSwitch(
                        checked = draft.workoutReminderEnabled,
                        onCheckedChange = { enable ->
                            if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    onUpdate { it.copy(workoutReminderEnabled = true) }
                                } else {
                                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                onUpdate { it.copy(workoutReminderEnabled = enable) }
                            }
                        },
                        contentDescriptionText = "Remind me 30 minutes before workout",
                        modifier = Modifier.testTag("onboarding_reminder_switch")
                    )
                }
            }
        }
    }
}

private fun periodLabelFor(minutes: Int): String {
    val hour = (((minutes % 1440) + 1440) % 1440) / 60
    return when {
        hour < 12 -> "Morning"
        hour < 17 -> "Afternoon"
        hour < 20 -> "Evening"
        else -> "Night"
    }
}
