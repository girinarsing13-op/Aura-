package com.example.aura.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.ui.theme.AuraTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 5,
    itemHeight: Dp = 44.dp,
    labelFormatter: (T) -> String = { it.toString() },
    unitLabel: String? = null,
    testTag: String = "wheel_picker"
) {
    if (items.isEmpty()) return

    val initialIndex = items.indexOf(selectedItem).let { if (it >= 0) it else 0 }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Sync external selection changes into list scroll
    LaunchedEffect(selectedItem) {
        val targetIndex = items.indexOf(selectedItem)
        if (targetIndex >= 0 && targetIndex != listState.firstVisibleItemIndex) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    // Detect center item and fire callback when settled
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices) {
                    onItemSelected(items[index])
                }
            }
    }

    val totalHeight = itemHeight * visibleItemsCount
    val paddingItemsCount = visibleItemsCount / 2

    Box(
        modifier = modifier
            .height(totalHeight)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Center Target Highlight Lens
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(AuraTheme.colors.accent.copy(alpha = 0.12f))
                .border(1.dp, AuraTheme.colors.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
        )

        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * paddingItemsCount),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected by remember {
                    derivedStateOf { listState.firstVisibleItemIndex == index }
                }

                val distanceFromCenter by remember {
                    derivedStateOf {
                        val first = listState.firstVisibleItemIndex
                        val offset = listState.firstVisibleItemScrollOffset
                        // Approximate distance
                        abs(first - index)
                    }
                }

                val alpha = when (distanceFromCenter) {
                    0 -> 1.0f
                    1 -> 0.45f
                    2 -> 0.20f
                    else -> 0.08f
                }

                val scale = when (distanceFromCenter) {
                    0 -> 1.15f
                    1 -> 0.92f
                    else -> 0.82f
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .scale(scale)
                        .alpha(alpha),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = labelFormatter(item),
                            fontSize = if (isSelected) 20.sp else 16.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) AuraTheme.colors.accent else AuraTheme.colors.primaryText,
                            textAlign = TextAlign.Center
                        )
                        if (unitLabel != null && isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = unitLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.secondaryText
                            )
                        }
                    }
                }
            }
        }

        // Top Gradient Shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * paddingItemsCount)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AuraTheme.colors.background,
                            AuraTheme.colors.background.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom Gradient Shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * paddingItemsCount)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AuraTheme.colors.background.copy(alpha = 0.8f),
                            AuraTheme.colors.background
                        )
                    )
                )
        )
    }
}

@Composable
fun HeightWheelPicker(
    heightCm: Double,
    onHeightChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "height_wheel_picker"
) {
    var isImperial by remember { mutableStateOf(false) }

    val cmValues = remember { (130..220).toList() }
    val currentCmInt = heightCm.roundToInt().coerceIn(130, 220)

    // Imperial ft and inches
    val totalInches = (heightCm / 2.54).roundToInt()
    val feet = totalInches / 12
    val inches = totalInches % 12

    val feetValues = remember { (4..7).toList() }
    val inchValues = remember { (0..11).toList() }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Unit Toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AuraTheme.colors.surfaceSecondary)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            UnitToggleChip(
                label = "Metric (cm)",
                isSelected = !isImperial,
                onClick = { isImperial = false }
            )
            UnitToggleChip(
                label = "Imperial (ft / in)",
                isSelected = isImperial,
                onClick = { isImperial = true }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!isImperial) {
            WheelPicker(
                items = cmValues,
                selectedItem = currentCmInt,
                onItemSelected = { onHeightChange(it.toDouble()) },
                unitLabel = "cm",
                labelFormatter = { "$it" },
                modifier = Modifier.fillMaxWidth(),
                testTag = testTag
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(
                    items = feetValues,
                    selectedItem = feet.coerceIn(4, 7),
                    onItemSelected = { newFeet ->
                        val newTotalInches = (newFeet * 12) + inches
                        val newCm = (newTotalInches * 2.54).roundToInt().toDouble()
                        onHeightChange(newCm)
                    },
                    unitLabel = "ft",
                    labelFormatter = { "$it'" },
                    modifier = Modifier.weight(1f),
                    testTag = "${testTag}_feet"
                )
                Spacer(modifier = Modifier.width(12.dp))
                WheelPicker(
                    items = inchValues,
                    selectedItem = inches.coerceIn(0, 11),
                    onItemSelected = { newInch ->
                        val newTotalInches = (feet * 12) + newInch
                        val newCm = (newTotalInches * 2.54).roundToInt().toDouble()
                        onHeightChange(newCm)
                    },
                    unitLabel = "in",
                    labelFormatter = { "$it\"" },
                    modifier = Modifier.weight(1f),
                    testTag = "${testTag}_inches"
                )
            }
        }
    }
}

@Composable
fun WeightWheelPicker(
    weightKg: Double,
    onWeightChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "weight_wheel_picker"
) {
    var isImperial by remember { mutableStateOf(false) }

    val kgValues = remember { (40..160).toList() }
    val currentKgInt = weightKg.roundToInt().coerceIn(40, 160)

    val lbsValues = remember { (90..350).toList() }
    val currentLbsInt = (weightKg * 2.20462).roundToInt().coerceIn(90, 350)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Unit Toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AuraTheme.colors.surfaceSecondary)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            UnitToggleChip(
                label = "Metric (kg)",
                isSelected = !isImperial,
                onClick = { isImperial = false }
            )
            UnitToggleChip(
                label = "Imperial (lbs)",
                isSelected = isImperial,
                onClick = { isImperial = true }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!isImperial) {
            WheelPicker(
                items = kgValues,
                selectedItem = currentKgInt,
                onItemSelected = { onWeightChange(it.toDouble()) },
                unitLabel = "kg",
                labelFormatter = { "$it" },
                modifier = Modifier.fillMaxWidth(),
                testTag = testTag
            )
        } else {
            WheelPicker(
                items = lbsValues,
                selectedItem = currentLbsInt,
                onItemSelected = { newLbs ->
                    val newKg = (newLbs / 2.20462).roundToInt().toDouble()
                    onWeightChange(newKg)
                },
                unitLabel = "lbs",
                labelFormatter = { "$it" },
                modifier = Modifier.fillMaxWidth(),
                testTag = testTag
            )
        }
    }
}

@Composable
private fun UnitToggleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accent else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) accentContrast else AuraTheme.colors.secondaryText
        )
    }
}
