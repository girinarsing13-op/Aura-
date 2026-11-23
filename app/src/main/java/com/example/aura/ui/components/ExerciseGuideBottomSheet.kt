package com.example.aura.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.engine.ExerciseGuideEngine
import com.example.aura.ui.theme.AuraTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseGuideBottomSheet(
    exercise: BlueprintExercise?,
    onDismiss: () -> Unit
) {
    if (exercise == null) return

    val colors = AuraTheme.colors
    val guide = remember(exercise.id, exercise.name) {
        ExerciseGuideEngine.getGuide(
            name = exercise.name,
            muscle = exercise.muscle,
            equipment = exercise.equipment,
            detail = exercise.detail,
            exerciseId = exercise.id
        )
    }
    val muscleTags = remember(guide.primaryMuscles, exercise.secondaryMuscles) {
        val primaryList = guide.primaryMuscles
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val secondaryList = exercise.getEffectiveSecondary()
            .filter { sec -> primaryList.none { it.equals(sec, ignoreCase = true) } }
        primaryList to secondaryList
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.primaryText,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = colors.border)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
                .testTag("exercise_guide_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "How to do it",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = exercise.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText,
                        lineHeight = 28.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .testTag("close_exercise_guide_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close guide",
                        tint = colors.primaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Muscles & Equipment Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Muscles worked",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.mutedText
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    muscleTags.first.forEach { muscle ->
                        GuideTagPill(
                            text = muscle,
                            backgroundColor = colors.accentTint,
                            borderColor = colors.accent.copy(alpha = 0.45f),
                            textColor = colors.accent
                        )
                    }
                    muscleTags.second.forEach { sec ->
                        GuideTagPill(
                            text = sec,
                            backgroundColor = colors.surface2,
                            borderColor = colors.border,
                            textColor = colors.primaryText
                        )
                    }
                    GuideTagPill(
                        text = guide.equipmentRequired,
                        backgroundColor = colors.surface2,
                        borderColor = colors.border,
                        textColor = colors.secondaryText
                    )
                }
            }

            // Demo + Photo / Animation Toggle
            var displayMode by remember(exercise.id) { mutableStateOf(DemoDisplayMode.AUTO) }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Form demo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.secondaryText
                    )
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.surface2)
                            .border(1.dp, colors.border, CircleShape)
                            .padding(3.dp)
                            .testTag("demo_mode_toggle"),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val isPhotoSelected = displayMode != DemoDisplayMode.ANIMATION
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isPhotoSelected) colors.accent else Color.Transparent)
                                .clickable { displayMode = DemoDisplayMode.PHOTO }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("demo_toggle_photo"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Photo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPhotoSelected) colors.accentContrast else colors.secondaryText
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (!isPhotoSelected) colors.accent else Color.Transparent)
                                .clickable { displayMode = DemoDisplayMode.ANIMATION }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("demo_toggle_animation"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Animation",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!isPhotoSelected) colors.accentContrast else colors.secondaryText
                            )
                        }
                    }
                }

                ExerciseDemo(
                    exerciseName = exercise.name,
                    exerciseId = exercise.id,
                    primaryMuscle = exercise.getEffectivePrimary(),
                    equipment = exercise.equipment,
                    movementPattern = exercise.getEffectivePattern(),
                    caption = guide.formCue,
                    preferredMode = displayMode,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Numbered Steps Card
            AuraCard(
                contentPadding = PaddingValues(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Step-by-step",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = guide.whatToDo,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = colors.secondaryText
                )
                Spacer(modifier = Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    guide.steps.forEachIndexed { idx, stepText ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentTint)
                                    .border(1.dp, colors.accent.copy(alpha = 0.55f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                            }
                            Text(
                                text = stepText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = colors.primaryText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Key Cue & Breathing Card
            AuraCard(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Breathing & cue",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = guide.formCue,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = guide.breathing,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = colors.secondaryText
                )
            }

            // Common Mistake Card (Coral Accent)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.warningCoral.copy(alpha = 0.12f))
                    .border(1.dp, colors.warningCoral.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
                    .testTag("guide_common_mistake_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.warningCoral.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = colors.warningCoral,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Common mistake",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.warningCoral
                        )
                        Text(
                            text = guide.mistakeToAvoid,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = colors.primaryText
                        )
                    }
                }
            }

            AuraPillButton(
                text = "Got it",
                onClick = onDismiss,
                isPrimary = true,
                modifier = Modifier.testTag("guide_got_it_button")
            )
        }
    }
}

@Composable
private fun GuideTagPill(
    text: String,
    backgroundColor: Color,
    borderColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
