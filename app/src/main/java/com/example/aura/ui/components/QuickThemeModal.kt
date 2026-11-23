package com.example.aura.ui.components

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aura.ui.theme.AuraTheme
import com.example.aura.ui.theme.ThemeEngine

/**
 * Quick Color Switcher accessible from the global top header.
 * Allows instant live preview and changing to 21+ colors or custom hex codes.
 */
@Composable
fun QuickThemeModal(
    currentAccentHex: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var customHexText by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(AuraTheme.colors.card)
                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(26.dp))
                .padding(20.dp)
                .testTag("quick_theme_modal")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COLOR PALETTE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AuraTheme.colors.secondaryText
                        )
                        Text(
                            text = "Switch Theme Accent",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = AuraTheme.colors.primaryText
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.border, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = AuraTheme.colors.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Category Filter Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThemeEngine.CATEGORIES.forEach { category ->
                        val isCatActive = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCatActive) accent.copy(alpha = 0.2f) else AuraTheme.colors.surfaceSecondary)
                                .border(
                                    1.dp,
                                    if (isCatActive) accent else AuraTheme.colors.border,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = category,
                                fontSize = 10.sp,
                                fontWeight = if (isCatActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCatActive) accent else AuraTheme.colors.secondaryText
                            )
                        }
                    }
                }

                // Color Swatches Grid
                val visiblePresets = if (selectedCategory == "All") {
                    ThemeEngine.PRESETS
                } else {
                    ThemeEngine.PRESETS.filter { it.category.equals(selectedCategory, ignoreCase = true) }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    visiblePresets.chunked(5).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowPresets.forEach { preset ->
                                val isPresetActive = currentAccentHex.equals(preset.hex, ignoreCase = true)
                                val color = ThemeEngine.parseHex(preset.hex)
                                val contrast = ThemeEngine.getContrastColor(preset.hex)

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isPresetActive) 3.dp else 1.dp,
                                            color = if (isPresetActive) Color.White else Color(0x33FFFFFF),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onColorSelected(preset.hex)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPresetActive) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = contrast,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom Hex Code Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AuraTheme.colors.surfaceSecondary)
                        .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "#",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTheme.colors.secondaryText
                        )
                        BasicTextField(
                            value = customHexText,
                            onValueChange = {
                                val filtered = it.removePrefix("#").filter { ch -> ch.isDigit() || ch in 'a'..'f' || ch in 'A'..'F' }.take(6)
                                customHexText = filtered
                                hasError = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AuraTheme.colors.input)
                                .border(1.dp, if (hasError) AuraTheme.colors.error else AuraTheme.colors.inputBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            textStyle = TextStyle(
                                color = AuraTheme.colors.primaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(accent),
                            singleLine = true
                        )

                        // Live Preview Box
                        val parsedColor = if (ThemeEngine.isValidHex(customHexText)) {
                            ThemeEngine.parseHex(customHexText)
                        } else null
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(parsedColor ?: Color(0x22FFFFFF))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        )

                        // Apply Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (ThemeEngine.isValidHex(customHexText)) accent else Color(0x1AFFFFFF))
                                .clickable(enabled = ThemeEngine.isValidHex(customHexText)) {
                                    if (ThemeEngine.isValidHex(customHexText)) {
                                        val normalized = ThemeEngine.normalizeHex(customHexText)
                                        onColorSelected(normalized)
                                        customHexText = ""
                                        onDismiss()
                                    } else {
                                        hasError = true
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Apply",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ThemeEngine.isValidHex(customHexText)) accentContrast else Color(0x55FFFFFF)
                            )
                        }
                    }
                }
            }
        }
    }
}
