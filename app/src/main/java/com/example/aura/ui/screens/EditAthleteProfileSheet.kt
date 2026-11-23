package com.example.aura.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.aura.data.model.EquipmentOptions
import com.example.aura.data.model.UserProfile
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.engine.SplitType
import com.example.aura.engine.WorkoutBlueprintEngine
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.engine.WorkoutSplitEngine
import com.example.aura.ui.components.HeightWheelPicker
import com.example.aura.ui.components.WeightWheelPicker
import com.example.aura.ui.components.WheelPicker
import kotlin.math.roundToInt

@Composable
fun EditAthleteProfileSheet(
    isVisible: Boolean,
    currentProfile: UserProfile,
    onSave: (UserProfile) -> Unit,
    onDismiss: () -> Unit,
    initialSectionIndex: Int = 0
) {
    if (!isVisible) return

    var draft by remember(currentProfile) { mutableStateOf(currentProfile) }
    var selectedSectionIndex by rememberSaveable(initialSectionIndex) { mutableIntStateOf(initialSectionIndex) }

    val sections = listOf(
        "Biometrics" to Icons.Default.Person,
        "Training" to Icons.Default.FitnessCenter,
        "Health & Focus" to Icons.Default.Healing,
        "Nutrition" to Icons.Default.Restaurant,
        "Recovery" to Icons.Default.Bedtime
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("edit_athlete_profile_modal"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
            ) {
                // Drag Handle & Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PROFILE SETTINGS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Edit Profile & Schedule",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .testTag("close_edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Tabs (Material 3 Expressive Chips with 48dp touch targets)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sections.indices.toList()) { index ->
                        val (label, icon) = sections[index]
                        val isSelected = selectedSectionIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSectionIndex = index },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Editor Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedSectionIndex) {
                        0 -> EditBiometricsSection(draft = draft, onUpdate = { draft = it })
                        1 -> EditTrainingSection(draft = draft, onUpdate = { draft = it })
                        2 -> EditInjuriesAndFocusSection(draft = draft, onUpdate = { draft = it })
                        3 -> EditDietAndAllergiesSection(draft = draft, onUpdate = { draft = it })
                        4 -> EditRecoverySection(draft = draft, onUpdate = { draft = it })
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons with M3 styling and 48dp touch target
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onSave(draft)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("save_athlete_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditBiometricsSection(
    draft: UserProfile,
    onUpdate: (UserProfile) -> Unit
) {
    val goals = listOf("Muscle Gain", "Fat Loss", "Recomposition", "General Fitness")
    val normGoal = FitnessCalcEngine.normalizeGoal(draft.goal)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "YOUR NAME",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = draft.name,
                onValueChange = { onUpdate(draft.copy(name = it)) },
                label = { Text("Name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("athlete_name_input")
            )
        }

        item {
            Text(
                text = "AGE (${draft.age} YEARS)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WheelPicker(
                    items = (13..85).toList(),
                    selectedItem = draft.age.coerceIn(13, 85),
                    onItemSelected = { onUpdate(draft.copy(age = it)) },
                    labelFormatter = { "$it Years Old" },
                    modifier = Modifier.height(100.dp)
                )
            }
        }

        item {
            Text(
                text = "HEIGHT (${draft.height.toInt()} CM)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                HeightWheelPicker(
                    heightCm = draft.height,
                    onHeightChange = { onUpdate(draft.copy(height = it)) },
                    modifier = Modifier.height(100.dp)
                )
            }
        }

        item {
            Text(
                text = "CURRENT BODY WEIGHT (${draft.weight.toInt()} KG)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WeightWheelPicker(
                    weightKg = draft.weight,
                    onWeightChange = { onUpdate(draft.copy(weight = it)) },
                    modifier = Modifier.height(100.dp)
                )
            }
        }

        item {
            Text(
                text = "TARGET BODY WEIGHT (${(draft.targetWeight ?: draft.weight).toInt()} KG)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WeightWheelPicker(
                    weightKg = draft.targetWeight ?: draft.weight,
                    onWeightChange = { onUpdate(draft.copy(targetWeight = it)) },
                    modifier = Modifier.height(100.dp)
                )
            }
        }

        item {
            Text(
                text = "TIMELINE (${draft.durationMonths} MONTHS)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = draft.durationMonths.toFloat(),
                onValueChange = { onUpdate(draft.copy(durationMonths = it.roundToInt().coerceIn(1, 12))) },
                valueRange = 1f..12f,
                steps = 10
            )
            val pace = FitnessCalcEngine.calcPaceAssessment(
                currentW = draft.weight,
                targetW = draft.targetWeight,
                durationMonths = draft.durationMonths,
                goal = normGoal
            )
            val advisory = FitnessCalcEngine.evaluateSafetyAdvisory(draft)
            val warnings = advisory.messages.ifEmpty {
                if (pace.isWarning) listOf(pace.message) else emptyList()
            }
            if (warnings.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        warnings.forEach { msg ->
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "MAIN GOAL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                goals.forEach { g ->
                    val isSel = normGoal.equals(g, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onUpdate(draft.copy(goal = g)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSel) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSel) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = g,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditTrainingSection(
    draft: UserProfile,
    onUpdate: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    var notificationDeniedHelp by rememberSaveable { mutableStateOf(false) }
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationDeniedHelp = !granted
        onUpdate(draft.copy(workoutReminderEnabled = granted))
    }

    val daysRange = (1..7).toList()
    val durations = listOf(30, 45, 60, 75, 90, 120)
    val environments = listOf(
        "Full Gym" to "Gym",
        "Home Workout" to "Home",
        "Calisthenics" to "Calisthenics only"
    )
    val normEnv = WorkoutBlueprintEngine.normalizeEnvironment(draft.environment)
    val experiences = listOf("Beginner", "Intermediate", "Advanced")
    val splits = WorkoutSplitEngine.ALL_SPLITS
    val currentSelectedSplitType = SplitType.fromIdOrTitle(draft.trainingSplit)
    val activeWeekdays = WorkoutScheduleEngine.resolveWorkoutDays(draft).toSet()
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

    val (equipTitle, availEquip, chosenEquip) = when (normEnv) {
        "Full Gym" -> Triple("GYM EQUIPMENT", EquipmentOptions.GYM, draft.gymEquipment)
        "Home Workout" -> Triple("HOME EQUIPMENT", EquipmentOptions.HOME, draft.homeEquipment)
        else -> Triple("CALISTHENICS GEAR", EquipmentOptions.CALISTHENICS, draft.calisthenicsEquipment)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TRAINING AVAILABILITY
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAYS PER WEEK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${draft.daysPerWeek} Days / Week",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WheelPicker(
                    items = daysRange,
                    selectedItem = draft.daysPerWeek.coerceIn(1, 7),
                    onItemSelected = { newDays ->
                        val adjusted = WorkoutScheduleEngine.adjustDaysForCount(draft.preferredWorkoutDays, newDays)
                        onUpdate(draft.copy(daysPerWeek = newDays, preferredWorkoutDays = adjusted))
                    },
                    labelFormatter = { "$it Days / Week" },
                    modifier = Modifier.height(100.dp)
                )
            }
        }

        // 1b. SPECIFIC WORKOUT DAYS
        item {
            Text(
                text = "WORKOUT DAYS (${activeWeekdays.size} SELECTED)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WorkoutScheduleEngine.WEEKDAYS.forEach { (weekdayId, label) ->
                    val isSel = weekdayId in activeWeekdays
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            val nextSet = activeWeekdays.toMutableSet()
                            if (isSel) {
                                if (nextSet.size > 1) nextSet.remove(weekdayId)
                            } else {
                                nextSet.add(weekdayId)
                            }
                            val sorted = nextSet.sorted()
                            onUpdate(draft.copy(preferredWorkoutDays = sorted, daysPerWeek = sorted.size))
                        },
                        label = { Text(label) }
                    )
                }
            }
        }

        // 1c. DAILY SCHEDULE & PREFERRED WORKOUT TIME
        item {
            Text(
                text = "WORKOUT TIMING & REMINDER",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetTimeStepperRow(
                    label = "Wake time",
                    minutes = draft.wakeTimeMinutes,
                    minMinutes = 4 * 60,
                    maxMinutes = 12 * 60,
                    onMinutesChange = { onUpdate(draft.copy(wakeTimeMinutes = it)) }
                )
                SheetTimeStepperRow(
                    label = "Sleep time",
                    minutes = draft.sleepTimeMinutes,
                    minMinutes = 19 * 60,
                    maxMinutes = 23 * 60 + 30,
                    onMinutesChange = { onUpdate(draft.copy(sleepTimeMinutes = it)) }
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Best free slot: ${WorkoutScheduleEngine.formatTimeMinutes(suggestion.suggestedMinutes)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (draft.preferredWorkoutMinutes != suggestion.suggestedMinutes) {
                            FilledTonalButton(
                                onClick = { onUpdate(draft.copy(preferredWorkoutMinutes = suggestion.suggestedMinutes)) },
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Text("Use", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                SheetTimeStepperRow(
                    label = "Workout time",
                    minutes = draft.preferredWorkoutMinutes,
                    minMinutes = 5 * 60,
                    maxMinutes = 22 * 60 + 30,
                    onMinutesChange = { onUpdate(draft.copy(preferredWorkoutMinutes = it)) }
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Daily workout reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = draft.workoutReminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            notificationDeniedHelp = false
                                            onUpdate(draft.copy(workoutReminderEnabled = true))
                                        } else {
                                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    } else {
                                        notificationDeniedHelp = false
                                        onUpdate(draft.copy(workoutReminderEnabled = enabled))
                                    }
                                }
                            )
                        }

                        if (notificationDeniedHelp) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Notifications are blocked in settings.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    )
                                    OutlinedButton(
                                        onClick = { openSheetNotificationSettings(context) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.heightIn(min = 40.dp)
                                    ) {
                                        Text("Open Settings", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. SESSION LENGTH
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SESSION LENGTH",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${draft.sessionDurationMinutes} Minutes",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WheelPicker(
                    items = durations,
                    selectedItem = draft.sessionDurationMinutes,
                    onItemSelected = { onUpdate(draft.copy(sessionDurationMinutes = it)) },
                    labelFormatter = { "$it Minutes" },
                    modifier = Modifier.height(100.dp)
                )
            }
        }

        // 3. PROGRAM SPLIT
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WORKOUT SPLIT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentSelectedSplitType.shortName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                splits.forEach { split ->
                    val isSel = currentSelectedSplitType == split.splitType

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdate(draft.copy(trainingSplit = split.title))
                            }
                            .testTag("split_card_${split.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            width = if (isSel) 2.dp else 1.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isSel) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (isSel) "Selected" else "Not selected",
                                        tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = split.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "${split.defaultDays}D",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = split.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. TRAINING PLACE
        item {
            Text(
                text = "TRAINING PLACE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                environments.forEach { (envValue, label) ->
                    val isSel = normEnv.equals(envValue, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onUpdate(draft.copy(environment = envValue)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSel) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSel) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 4b. EQUIPMENT CHECKLIST FOR SELECTED PLACE
        item {
            Text(
                text = "$equipTitle (${chosenEquip.size} SELECTED)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availEquip.forEach { eq ->
                    val isChecked = chosenEquip.any { it.equals(eq, ignoreCase = true) }
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            val updated = if (isChecked) {
                                chosenEquip.filterNot { it.equals(eq, ignoreCase = true) }
                            } else {
                                chosenEquip + eq
                            }
                            onUpdate(
                                when (normEnv) {
                                    "Full Gym" -> draft.copy(gymEquipment = updated)
                                    "Home Workout" -> draft.copy(homeEquipment = updated)
                                    else -> draft.copy(calisthenicsEquipment = updated)
                                }
                            )
                        },
                        leadingIcon = if (isChecked) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        label = { Text(eq) }
                    )
                }
            }
        }

        // 5. EXPERIENCE LEVEL
        item {
            Text(
                text = "EXPERIENCE LEVEL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                experiences.forEach { exp ->
                    val isSel = draft.experience.equals(exp, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onUpdate(draft.copy(experience = exp)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp),
                        border = if (isSel) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = exp,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetTimeStepperRow(
    label: String,
    minutes: Int,
    minMinutes: Int,
    maxMinutes: Int,
    onMinutesChange: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onMinutesChange((minutes - 30).coerceIn(minMinutes, maxMinutes)) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Earlier 30 min")
                }
                Text(
                    text = WorkoutScheduleEngine.formatTimeMinutes(minutes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { onMinutesChange((minutes + 30).coerceIn(minMinutes, maxMinutes)) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Later 30 min")
                }
            }
        }
    }
}

private fun openSheetNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(fallback)
        } catch (_: Exception) {
        }
    }
}

@Composable
private fun EditInjuriesAndFocusSection(
    draft: UserProfile,
    onUpdate: (UserProfile) -> Unit
) {
    val jointOptions = listOf("None", "Lower Back", "Shoulders", "Knees", "Wrists", "Elbows", "Neck")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Joint Injury Shield
        item {
            Column {
                Text(
                    text = "JOINT HEALTH & INJURY SHIELD",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tap joints to shield against axial load & high shear exercises. 'None' clears shields.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                jointOptions.forEach { joint ->
                    val isChecked = if (joint == "None") {
                        draft.injuries.contains("None") || draft.injuries.isEmpty()
                    } else {
                        draft.injuries.contains(joint) && !draft.injuries.contains("None")
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                val updated = if (joint == "None") {
                                    listOf("None")
                                } else {
                                    val current = draft.injuries.filter { it != "None" }
                                    if (current.contains(joint)) {
                                        val remaining = current.filter { it != joint }
                                        remaining.ifEmpty { listOf("None") }
                                    } else {
                                        current + joint
                                    }
                                }
                                onUpdate(draft.copy(injuries = updated))
                            },
                        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = joint,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                color = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Lagging Muscle Priorities (Multi-select, maximum 2)
        item {
            val selectedMuscles = com.example.aura.data.model.PriorityMuscle.toSet(draft.focusBodyParts)
            val allMuscles = com.example.aura.data.model.PriorityMuscle.ALL

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRIORITY MUSCLE FOCUS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${selectedMuscles.size} of 2 selected",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Choose up to 2 muscle groups for prioritized volume (+1 set) and early workout sequencing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allMuscles.forEach { muscle ->
                    val isChecked = selectedMuscles.contains(muscle)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                val updatedSet = if (isChecked) {
                                    selectedMuscles - muscle
                                } else if (selectedMuscles.size < 2) {
                                    selectedMuscles + muscle
                                } else {
                                    selectedMuscles
                                }
                                onUpdate(draft.copy(focusBodyParts = updatedSet.map { it.displayName }))
                            }
                            .testTag("priority_muscle_${muscle.id}"),
                        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp),
                        border = if (isChecked) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Default.Check else Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = muscle.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isChecked) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "+1 Set",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditDietAndAllergiesSection(
    draft: UserProfile,
    onUpdate: (UserProfile) -> Unit
) {
    val diets = listOf("Vegetarian", "Eggetarian", "Non-Vegetarian", "Vegan")
    val allergiesList = listOf(
        "None",
        "Dairy",
        "Lactose Intolerance",
        "Gluten",
        "Peanuts",
        "Tree Nuts",
        "Soy",
        "Eggs",
        "Seafood",
        "Other"
    )
    val mealsRange = listOf(2, 3, 4, 5, 6)
    val waterRange = listOf(2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dietary Ethos
        item {
            Text(
                text = "DIETARY ETHOS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                diets.forEach { d ->
                    val isSel = draft.dietPreference.equals(d, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onUpdate(draft.copy(dietPreference = d)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSel) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = d,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Allergies & Food Intolerances
        item {
            Column {
                Text(
                    text = "ALLERGIES & FOOD INTOLERANCES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Meals containing selected ingredients are shielded from macro recipes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allergiesList.forEach { allergy ->
                    val hasSpecificAllergy = draft.allergies.any { !it.equals("None", ignoreCase = true) }
                    val isChecked = if (allergy.equals("None", ignoreCase = true)) {
                        !hasSpecificAllergy
                    } else {
                        hasSpecificAllergy && draft.allergies.any { it.equals(allergy, ignoreCase = true) }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                val updated = if (allergy.equals("None", ignoreCase = true)) {
                                    listOf("None")
                                } else {
                                    val withoutNone = draft.allergies.filter { !it.equals("None", ignoreCase = true) }
                                    if (withoutNone.any { it.equals(allergy, ignoreCase = true) }) {
                                        val remaining = withoutNone.filter { !it.equals(allergy, ignoreCase = true) }
                                        remaining.ifEmpty { listOf("None") }
                                    } else {
                                        withoutNone + allergy
                                    }
                                }
                                onUpdate(draft.copy(allergies = updated))
                            },
                        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = allergy,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                color = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Meals Pacing
        item {
            Text(
                text = "DAILY MEALS PACING (${draft.mealsPerDay} MEALS / DAY)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WheelPicker(
                    items = mealsRange,
                    selectedItem = draft.mealsPerDay,
                    onItemSelected = { onUpdate(draft.copy(mealsPerDay = it)) },
                    labelFormatter = { "$it Meals / Day" },
                    modifier = Modifier.height(90.dp)
                )
            }
        }

        // Daily Water Target
        item {
            Text(
                text = "DAILY WATER TARGET (${draft.waterLiters} L / DAY)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WheelPicker(
                    items = waterRange,
                    selectedItem = draft.waterLiters,
                    onItemSelected = { onUpdate(draft.copy(waterLiters = it)) },
                    labelFormatter = { "$it Liters" },
                    modifier = Modifier.height(90.dp)
                )
            }
        }
    }
}

@Composable
private fun EditRecoverySection(
    draft: UserProfile,
    onUpdate: (UserProfile) -> Unit
) {
    val sleepHoursOptions = listOf(5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0)
    val sleepQualities = listOf("Poor", "Fair", "Good", "Optimal")
    val activityLevels = listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Athlete")
    val stressLevels = listOf("Low", "Moderate", "High", "Severe")
    val cardioOptions = listOf("LISS Walking", "HIIT Sprinting", "Zone 2 Jogging", "Cycling", "Rowing")
    val postureOptions = listOf("Neutral Spine", "Thoracic Extension", "Anterior Pelvic Tilt Correction", "Shoulder Retraction")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sleep Duration
        item {
            Text(
                text = "NIGHTLY SLEEP DURATION (${draft.sleepHours} HOURS)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                WheelPicker(
                    items = sleepHoursOptions,
                    selectedItem = draft.sleepHours,
                    onItemSelected = { onUpdate(draft.copy(sleepHours = it)) },
                    labelFormatter = { "$it Hours" },
                    modifier = Modifier.height(90.dp)
                )
            }
        }

        // Sleep Quality
        item {
            Text(
                text = "SLEEP QUALITY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sleepQualities.forEach { sq ->
                    val isSel = draft.sleepQuality.equals(sq, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onUpdate(draft.copy(sleepQuality = sq)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sq,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Daily Lifestyle Activity
        item {
            Text(
                text = "DAILY LIFESTYLE ACTIVITY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activityLevels.forEach { act ->
                    val isSel = draft.lifestyleActivity.equals(act, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onUpdate(draft.copy(lifestyleActivity = act)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSel) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = act,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Daily Stress Level
        item {
            Text(
                text = "DAILY STRESS LEVEL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                stressLevels.forEach { sl ->
                    val isSel = draft.stressLevel.equals(sl, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onUpdate(draft.copy(stressLevel = sl)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sl,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Cardio Protocol
        item {
            Text(
                text = "CARDIO PROTOCOL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cardioOptions.forEach { c ->
                    val isSel = draft.cardioPreference.equals(c, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onUpdate(draft.copy(cardioPreference = c)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSel) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = c,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Posture & Spine Alignment
        item {
            Text(
                text = "POSTURE & SPINE ALIGNMENT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                postureOptions.forEach { p ->
                    val isSel = draft.postureFocus.equals(p, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onUpdate(draft.copy(postureFocus = p)) },
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSel) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = p,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Cycle Tracking (when Female)
        if (draft.gender.equals("Female", ignoreCase = true)) {
            item {
                Text(
                    text = "CYCLE TRACKING",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enable cycle tracking",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = draft.trackCycle,
                                onCheckedChange = { enabled ->
                                    onUpdate(
                                        draft.copy(
                                            trackCycle = enabled,
                                            periodWeekActive = if (enabled) draft.periodWeekActive else false
                                        )
                                    )
                                }
                            )
                        }
                        if (draft.trackCycle) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Period week active (lighter sets)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Switch(
                                    checked = draft.periodWeekActive,
                                    onCheckedChange = { active ->
                                        onUpdate(draft.copy(trackCycle = true, periodWeekActive = active))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
