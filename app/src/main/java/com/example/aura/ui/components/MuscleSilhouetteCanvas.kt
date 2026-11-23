package com.example.aura.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.ui.theme.AuraTheme

@Composable
fun MuscleSilhouetteCanvas(
    selectedMuscles: List<String>,
    modifier: Modifier = Modifier,
    diagramHeight: Dp = 170.dp,
    showFocusFooter: Boolean = true,
    isCompactThumbnail: Boolean = false
) {
    val accent = AuraTheme.colors.accent
    val isAllBalanced = selectedMuscles.isEmpty()
    val hasChest = selectedMuscles.any { it.contains("chest", ignoreCase = true) || it.contains("pectoral", ignoreCase = true) }
    val hasTriceps = selectedMuscles.any { it.contains("tricep", ignoreCase = true) || it.contains("arm", ignoreCase = true) }
    val hasBack = selectedMuscles.any { it.contains("back", ignoreCase = true) || it.contains("lat", ignoreCase = true) }
    val hasBiceps = selectedMuscles.any { it.contains("bicep", ignoreCase = true) || it.contains("arm", ignoreCase = true) }
    val hasShoulders = selectedMuscles.any { it.contains("shoulder", ignoreCase = true) || it.contains("delt", ignoreCase = true) }
    val hasQuads = selectedMuscles.any { it.contains("quad", ignoreCase = true) }
    val hasHamstringsGlutes = selectedMuscles.any { it.contains("hamstring", ignoreCase = true) || it.contains("glute", ignoreCase = true) }
    val hasCalves = selectedMuscles.any { it.contains("calv", ignoreCase = true) }
    val hasAbs = selectedMuscles.any { it.contains("ab", ignoreCase = true) || it.contains("core", ignoreCase = true) }

    val defaultMuscleColor = Color(0xFF2A2A38)
    val inactiveDark = Color(0xFF1C1C26)
    val headColor = Color(0xFF22222E)

    if (isCompactThumbnail) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0B0B14))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scaleX = size.width / 100f
                val scaleY = size.height / 190f
                val showPosterior = (hasBack || hasTriceps || hasHamstringsGlutes) && !hasChest && !hasAbs && !hasQuads && !hasBiceps

                drawCircle(
                    color = headColor,
                    radius = 10f * scaleX,
                    center = Offset(50f * scaleX, 18f * scaleY)
                )
                if (!showPosterior) {
                    val shoulderCol = if (hasShoulders || isAllBalanced) accent else defaultMuscleColor
                    drawPath(Path().apply {
                        moveTo(28f * scaleX, 36f * scaleY); lineTo(72f * scaleX, 36f * scaleY)
                        lineTo(68f * scaleX, 50f * scaleY); lineTo(32f * scaleX, 50f * scaleY); close()
                    }, color = shoulderCol)

                    val chestCol = if (hasChest || isAllBalanced) accent else defaultMuscleColor
                    drawPath(Path().apply {
                        moveTo(41f * scaleX, 38f * scaleY); lineTo(59f * scaleX, 38f * scaleY)
                        lineTo(57f * scaleX, 58f * scaleY); lineTo(43f * scaleX, 58f * scaleY); close()
                    }, color = chestCol)

                    val bicepCol = if (hasBiceps || isAllBalanced) accent else defaultMuscleColor
                    drawRect(bicepCol, topLeft = Offset(22f * scaleX, 46f * scaleY), size = Size(8f * scaleX, 32f * scaleY))
                    drawRect(bicepCol, topLeft = Offset(70f * scaleX, 46f * scaleY), size = Size(8f * scaleX, 32f * scaleY))

                    val absCol = if (hasAbs || isAllBalanced) accent else defaultMuscleColor
                    drawRect(absCol, topLeft = Offset(44f * scaleX, 60f * scaleY), size = Size(12f * scaleX, 30f * scaleY))

                    val quadCol = if (hasQuads || isAllBalanced) accent else defaultMuscleColor
                    drawRect(quadCol, topLeft = Offset(38f * scaleX, 94f * scaleY), size = Size(10f * scaleX, 42f * scaleY))
                    drawRect(quadCol, topLeft = Offset(52f * scaleX, 94f * scaleY), size = Size(10f * scaleX, 42f * scaleY))

                    val calfCol = if (hasCalves || isAllBalanced) accent else defaultMuscleColor
                    drawRect(calfCol, topLeft = Offset(39f * scaleX, 140f * scaleY), size = Size(8f * scaleX, 34f * scaleY))
                    drawRect(calfCol, topLeft = Offset(53f * scaleX, 140f * scaleY), size = Size(8f * scaleX, 34f * scaleY))
                } else {
                    val backCol = if (hasBack || isAllBalanced) accent else defaultMuscleColor
                    drawPath(Path().apply {
                        moveTo(34f * scaleX, 36f * scaleY); lineTo(66f * scaleX, 36f * scaleY)
                        lineTo(58f * scaleX, 82f * scaleY); lineTo(42f * scaleX, 82f * scaleY); close()
                    }, color = backCol)

                    val tricepCol = if (hasTriceps || isAllBalanced) accent else defaultMuscleColor
                    drawRect(tricepCol, topLeft = Offset(22f * scaleX, 44f * scaleY), size = Size(8f * scaleX, 32f * scaleY))
                    drawRect(tricepCol, topLeft = Offset(70f * scaleX, 44f * scaleY), size = Size(8f * scaleX, 32f * scaleY))

                    val hamCol = if (hasHamstringsGlutes || isAllBalanced) accent else defaultMuscleColor
                    drawRect(hamCol, topLeft = Offset(38f * scaleX, 86f * scaleY), size = Size(24f * scaleX, 18f * scaleY))
                    drawRect(hamCol, topLeft = Offset(39f * scaleX, 108f * scaleY), size = Size(9f * scaleX, 34f * scaleY))
                    drawRect(hamCol, topLeft = Offset(52f * scaleX, 108f * scaleY), size = Size(9f * scaleX, 34f * scaleY))

                    val calfCol = if (hasCalves || isAllBalanced) accent else defaultMuscleColor
                    drawRect(calfCol, topLeft = Offset(39f * scaleX, 144f * scaleY), size = Size(8f * scaleX, 30f * scaleY))
                    drawRect(calfCol, topLeft = Offset(53f * scaleX, 144f * scaleY), size = Size(8f * scaleX, 30f * scaleY))
                }
            }
        }
        return
    }

    val containerPadding = if (showFocusFooter) 16.dp else 10.dp
    val canvasHeight = (diagramHeight - 20.dp).coerceAtLeast(80.dp)
    val canvasWidth = (canvasHeight * 0.54f).coerceAtLeast(46.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (showFocusFooter) 24.dp else 20.dp))
            .background(Color(0xFF06060A))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(if (showFocusFooter) 24.dp else 20.dp))
            .padding(containerPadding)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(diagramHeight),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- ANTERIOR (FRONT) ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ANTERIOR",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AuraTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(modifier = Modifier.width(canvasWidth).height(canvasHeight)) {
                        val scaleX = size.width / 100f
                        val scaleY = size.height / 190f

                        // Head & Neck
                        drawCircle(
                            color = headColor,
                            radius = 10f * scaleX,
                            center = Offset(50f * scaleX, 16f * scaleY)
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(44f * scaleX, 26f * scaleY)
                                lineTo(56f * scaleX, 26f * scaleY)
                                lineTo(58f * scaleX, 34f * scaleY)
                                lineTo(42f * scaleX, 34f * scaleY)
                                close()
                            },
                            color = defaultMuscleColor
                        )

                        // Shoulders
                        val shoulderCol = if (hasShoulders || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(30f * scaleX, 35f * scaleY)
                                lineTo(42f * scaleX, 35f * scaleY)
                                lineTo(40f * scaleX, 48f * scaleY)
                                lineTo(28f * scaleX, 42f * scaleY)
                                close()
                            },
                            color = shoulderCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(70f * scaleX, 35f * scaleY)
                                lineTo(58f * scaleX, 35f * scaleY)
                                lineTo(60f * scaleX, 48f * scaleY)
                                lineTo(72f * scaleX, 42f * scaleY)
                                close()
                            },
                            color = shoulderCol
                        )

                        // Chest
                        val chestCol = if (hasChest || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(42f * scaleX, 36f * scaleY)
                                lineTo(50f * scaleX, 38f * scaleY)
                                lineTo(58f * scaleX, 36f * scaleY)
                                lineTo(57f * scaleX, 52f * scaleY)
                                lineTo(50f * scaleX, 54f * scaleY)
                                lineTo(43f * scaleX, 52f * scaleY)
                                close()
                            },
                            color = chestCol
                        )

                        // Biceps
                        val bicepCol = if (hasBiceps || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(26f * scaleX, 44f * scaleY)
                                lineTo(28f * scaleX, 66f * scaleY)
                                lineTo(22f * scaleX, 64f * scaleY)
                                close()
                            },
                            color = bicepCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(74f * scaleX, 44f * scaleY)
                                lineTo(72f * scaleX, 66f * scaleY)
                                lineTo(78f * scaleX, 64f * scaleY)
                                close()
                            },
                            color = bicepCol
                        )

                        // Forearms
                        drawRect(
                            color = inactiveDark,
                            topLeft = Offset(20f * scaleX, 68f * scaleY),
                            size = Size(6f * scaleX, 24f * scaleY)
                        )
                        drawRect(
                            color = inactiveDark,
                            topLeft = Offset(74f * scaleX, 68f * scaleY),
                            size = Size(6f * scaleX, 24f * scaleY)
                        )

                        // Abdominals & Core
                        val absCol = if (hasAbs || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(43f * scaleX, 55f * scaleY)
                                lineTo(57f * scaleX, 55f * scaleY)
                                lineTo(55f * scaleX, 86f * scaleY)
                                lineTo(45f * scaleX, 86f * scaleY)
                                close()
                            },
                            color = absCol
                        )

                        // Quads
                        val quadCol = if (hasQuads || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(41f * scaleX, 90f * scaleY)
                                lineTo(49f * scaleX, 90f * scaleY)
                                lineTo(47f * scaleX, 132f * scaleY)
                                lineTo(37f * scaleX, 130f * scaleY)
                                close()
                            },
                            color = quadCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(59f * scaleX, 90f * scaleY)
                                lineTo(51f * scaleX, 90f * scaleY)
                                lineTo(53f * scaleX, 132f * scaleY)
                                lineTo(63f * scaleX, 130f * scaleY)
                                close()
                            },
                            color = quadCol
                        )

                        // Calves
                        val calfCol = if (hasCalves || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(37f * scaleX, 136f * scaleY)
                                lineTo(46f * scaleX, 136f * scaleY)
                                lineTo(43f * scaleX, 172f * scaleY)
                                lineTo(36f * scaleX, 172f * scaleY)
                                close()
                            },
                            color = calfCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(63f * scaleX, 136f * scaleY)
                                lineTo(54f * scaleX, 136f * scaleY)
                                lineTo(57f * scaleX, 172f * scaleY)
                                lineTo(64f * scaleX, 172f * scaleY)
                                close()
                            },
                            color = calfCol
                        )
                    }
                }

                // --- POSTERIOR (BACK) ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "POSTERIOR",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AuraTheme.colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Canvas(modifier = Modifier.width(canvasWidth).height(canvasHeight)) {
                        val scaleX = size.width / 100f
                        val scaleY = size.height / 190f

                        // Head & Traps
                        drawCircle(
                            color = headColor,
                            radius = 10f * scaleX,
                            center = Offset(50f * scaleX, 16f * scaleY)
                        )

                        // Back & Lats
                        val backCol = if (hasBack || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(42f * scaleX, 27f * scaleY)
                                lineTo(50f * scaleX, 32f * scaleY)
                                lineTo(58f * scaleX, 27f * scaleY)
                                lineTo(64f * scaleX, 36f * scaleY)
                                lineTo(36f * scaleX, 36f * scaleY)
                                close()
                            },
                            color = backCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(36f * scaleX, 38f * scaleY)
                                lineTo(64f * scaleX, 38f * scaleY)
                                lineTo(58f * scaleX, 78f * scaleY)
                                lineTo(42f * scaleX, 78f * scaleY)
                                close()
                            },
                            color = backCol
                        )

                        // Triceps
                        val tricepCol = if (hasTriceps || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(26f * scaleX, 42f * scaleY)
                                lineTo(27f * scaleX, 66f * scaleY)
                                lineTo(22f * scaleX, 64f * scaleY)
                                close()
                            },
                            color = tricepCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(74f * scaleX, 42f * scaleY)
                                lineTo(73f * scaleX, 66f * scaleY)
                                lineTo(78f * scaleX, 64f * scaleY)
                                close()
                            },
                            color = tricepCol
                        )

                        // Forearms
                        drawRect(
                            color = inactiveDark,
                            topLeft = Offset(20f * scaleX, 68f * scaleY),
                            size = Size(6f * scaleX, 24f * scaleY)
                        )
                        drawRect(
                            color = inactiveDark,
                            topLeft = Offset(74f * scaleX, 68f * scaleY),
                            size = Size(6f * scaleX, 24f * scaleY)
                        )

                        // Glutes & Hamstrings
                        val hamCol = if (hasHamstringsGlutes || hasQuads || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(38f * scaleX, 80f * scaleY)
                                lineTo(62f * scaleX, 80f * scaleY)
                                lineTo(60f * scaleX, 98f * scaleY)
                                lineTo(40f * scaleX, 98f * scaleY)
                                close()
                            },
                            color = hamCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(40f * scaleX, 102f * scaleY)
                                lineTo(48f * scaleX, 102f * scaleY)
                                lineTo(46f * scaleX, 134f * scaleY)
                                lineTo(38f * scaleX, 134f * scaleY)
                                close()
                            },
                            color = hamCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(60f * scaleX, 102f * scaleY)
                                lineTo(52f * scaleX, 102f * scaleY)
                                lineTo(54f * scaleX, 134f * scaleY)
                                lineTo(62f * scaleX, 134f * scaleY)
                                close()
                            },
                            color = hamCol
                        )

                        // Calves
                        val calfCol = if (hasCalves || isAllBalanced) accent else defaultMuscleColor
                        drawPath(
                            path = Path().apply {
                                moveTo(37f * scaleX, 138f * scaleY)
                                lineTo(45f * scaleX, 138f * scaleY)
                                lineTo(43f * scaleX, 172f * scaleY)
                                lineTo(36f * scaleX, 172f * scaleY)
                                close()
                            },
                            color = calfCol
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(63f * scaleX, 138f * scaleY)
                                lineTo(55f * scaleX, 138f * scaleY)
                                lineTo(57f * scaleX, 172f * scaleY)
                                lineTo(64f * scaleX, 172f * scaleY)
                                close()
                            },
                            color = calfCol
                        )
                    }
                }
            }

            if (showFocusFooter) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0x0FFFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Focus:",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AuraTheme.colors.textMuted
                    )
                    Text(
                        text = if (isAllBalanced) "Balanced Full Body" else "${selectedMuscles.joinToString(", ")} (${selectedMuscles.size}/2)",
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
