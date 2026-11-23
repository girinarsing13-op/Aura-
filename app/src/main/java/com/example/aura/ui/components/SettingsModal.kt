package com.example.aura.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.zIndex
import com.example.BuildConfig
import com.example.aura.ui.AuraViewModel
import com.example.aura.ui.theme.AuraTheme
import com.example.aura.ui.theme.ThemeEngine
import kotlin.math.roundToInt

@Composable
fun SettingsModal(
    isVisible: Boolean,
    viewModel: AuraViewModel,
    onDismiss: () -> Unit,
    onOpenDiagnostics: (() -> Unit)? = null,
    onOpenAbout: () -> Unit = {},
    onOpenParq: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val draft by viewModel.draftProfile.collectAsState()
    val liveNavTransparency by viewModel.liveNavTransparency.collectAsState()
    val liveNavBlur by viewModel.liveNavBlur.collectAsState()
    val liveThemeMode by viewModel.liveThemeMode.collectAsState()
    val liveThemeAccent by viewModel.liveThemeAccent.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val parqCompleted by viewModel.parqCompleted.collectAsState()
    val parqFlagged by viewModel.parqFlagged.collectAsState()

    val currentThemeMode = (liveThemeMode ?: profile?.themeMode ?: draft.themeMode).uppercase()
    val currentAccentHex = liveThemeAccent ?: profile?.themeAccent ?: draft.themeAccent
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    val currentTransparency = if (liveNavTransparency != null) {
        liveNavTransparency!!.coerceIn(0f, 1f)
    } else {
        val raw = profile?.navTransparency ?: draft.navTransparency
        if (raw == 0.95f) 0.05f else raw.coerceIn(0f, 1f)
    }
    val currentBlur = (liveNavBlur ?: profile?.navBlur ?: draft.navBlur).coerceIn(0f, 1f)

    val transparencyPercent = (currentTransparency * 100f).roundToInt()
    val blurPercent = (currentBlur * 100f).roundToInt()

    var selectedColorCategory by remember { mutableStateOf("All") }
    var customHexCode by remember { mutableStateOf("") }
    var customHexError by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { -it / 3 },
        exit = fadeOut() + slideOutVertically { -it / 3 },
        modifier = modifier
            .fillMaxSize()
            .zIndex(999f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 76.dp) // Leave navigation bar visible and unobscured
                .background(Color.Black.copy(alpha = 0.50f))
                .clickable { onDismiss() }
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(AuraTheme.colors.card)
                    .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(26.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Absorb clicks inside dialog card
                    }
                    .padding(20.dp)
                    .testTag("settings_modal")
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SYSTEM SETTINGS",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = accent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Appearance & Interface",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AuraTheme.colors.surfaceSecondary)
                                .border(1.dp, AuraTheme.colors.border, CircleShape)
                                .testTag("close_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Settings",
                                tint = AuraTheme.colors.secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // ==========================================
                    // 1. THEME MODE SELECTOR (DARK / LIGHT / SYSTEM)
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "THEME MODE",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTheme.colors.textMuted
                                )
                                val activeModeLabel = when (currentThemeMode) {
                                    ThemeEngine.MODE_LIGHT -> "Light"
                                    ThemeEngine.MODE_DARK -> "Dark"
                                    else -> "System (Auto)"
                                }
                                Text(
                                    text = activeModeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accent
                                )
                            }

                            val themeModes = listOf(
                                Triple(ThemeEngine.MODE_DARK, "Dark", "🌙"),
                                Triple(ThemeEngine.MODE_LIGHT, "Light", "☀️"),
                                Triple(ThemeEngine.MODE_SYSTEM, "System", "⚙️")
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AuraTheme.colors.card)
                                    .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(14.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                themeModes.forEach { (modeKey, label, emoji) ->
                                    val isSelected = currentThemeMode.equals(modeKey, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) accent.copy(alpha = 0.20f) else Color.Transparent
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) accent else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                viewModel.setThemeMode(modeKey)
                                            }
                                            .testTag("theme_mode_btn_${modeKey.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = emoji, fontSize = 13.sp)
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) accent else AuraTheme.colors.secondaryText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 2. ACCENT COLOR PALETTE
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ACCENT COLOR PALETTE",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTheme.colors.textMuted
                                )
                                val activePreset = ThemeEngine.PRESETS.find { it.hex.equals(currentAccentHex, ignoreCase = true) }
                                val activeLabel = activePreset?.name ?: "Custom"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                    Text(
                                        text = "$activeLabel ($currentAccentHex)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accent
                                    )
                                }
                            }

                            // Category Filter Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ThemeEngine.CATEGORIES.forEach { category ->
                                    val isCatActive = selectedColorCategory == category
                                    Box(
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCatActive) accent.copy(alpha = 0.2f) else AuraTheme.colors.card)
                                            .border(
                                                1.dp,
                                                if (isCatActive) accent else AuraTheme.colors.border,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedColorCategory = category }
                                            .padding(horizontal = 8.dp, vertical = 7.dp)
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

                            // Color Swatches Grid (chunked by 5)
                            val visiblePresets = if (selectedColorCategory == "All") {
                                ThemeEngine.PRESETS
                            } else {
                                ThemeEngine.PRESETS.filter { it.category.equals(selectedColorCategory, ignoreCase = true) }
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
                                                    color = if (isPresetActive) accent else AuraTheme.colors.border,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    viewModel.updateThemeAccent(preset.hex)
                                                },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isPresetActive) {
                                                    Icon(
                                                        Icons.Filled.Check,
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

                            // Custom Hex Code Input Section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AuraTheme.colors.card)
                                    .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "CUSTOM HEX CODE",
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraTheme.colors.textMuted
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "#",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AuraTheme.colors.textMuted
                                        )
                                        BasicTextField(
                                            value = customHexCode,
                                            onValueChange = {
                                                val filtered = it.removePrefix("#").filter { ch -> ch.isDigit() || ch in 'a'..'f' || ch in 'A'..'F' }.take(6)
                                                customHexCode = filtered
                                                customHexError = false
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AuraTheme.colors.input)
                                                .border(1.dp, if (customHexError) AuraTheme.colors.error else AuraTheme.colors.inputBorder, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                                .testTag("custom_hex_input"),
                                            textStyle = TextStyle(
                                                color = AuraTheme.colors.primaryText,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            cursorBrush = SolidColor(accent),
                                            singleLine = true
                                        )

                                        // Live Swatch Preview
                                        val parsedCustom = if (ThemeEngine.isValidHex(customHexCode)) {
                                            ThemeEngine.parseHex(customHexCode)
                                        } else null
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(parsedCustom ?: AuraTheme.colors.surfaceSecondary)
                                                .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(8.dp))
                                        )

                                        // Apply Button
                                        Box(
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .heightIn(min = 40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (ThemeEngine.isValidHex(customHexCode)) accent else AuraTheme.colors.border)
                                                .clickable(enabled = ThemeEngine.isValidHex(customHexCode)) {
                                                    if (ThemeEngine.isValidHex(customHexCode)) {
                                                        val normalized = ThemeEngine.normalizeHex(customHexCode)
                                                        viewModel.updateThemeAccent(normalized)
                                                        customHexCode = ""
                                                    } else {
                                                        customHexError = true
                                                    }
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                                .testTag("apply_custom_hex_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Apply",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (ThemeEngine.isValidHex(customHexCode)) accentContrast else AuraTheme.colors.secondaryText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 3. NAVIGATION BAR APPEARANCE
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NAVIGATION BAR APPEARANCE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTheme.colors.primaryText
                                )
                                Text(
                                    text = "LIVE CONTROLS",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                            }

                            // 1. Transparency Slider (0% - 100%, default 5%)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Transparency",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AuraTheme.colors.primaryText
                                    )
                                    Text(
                                        text = "$transparencyPercent%",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = accent,
                                        modifier = Modifier.testTag("nav_transparency_value")
                                    )
                                }
                                Slider(
                                    value = currentTransparency,
                                    onValueChange = { newVal ->
                                        viewModel.setNavTransparency(newVal)
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accent,
                                        activeTrackColor = accent,
                                        inactiveTrackColor = AuraTheme.colors.border
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("nav_transparency_slider")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "0% (Opaque)",
                                        fontSize = 10.sp,
                                        color = AuraTheme.colors.textMuted
                                    )
                                    Text(
                                        text = "Default: 5%",
                                        fontSize = 10.sp,
                                        color = AuraTheme.colors.textMuted
                                    )
                                    Text(
                                        text = "100% (Clear)",
                                        fontSize = 10.sp,
                                        color = AuraTheme.colors.textMuted
                                    )
                                }
                            }

                            // 2. Blur Slider (0% - 100%, default 80%)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Blur",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AuraTheme.colors.primaryText
                                    )
                                    Text(
                                        text = "$blurPercent%",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = accent,
                                        modifier = Modifier.testTag("nav_blur_value")
                                    )
                                }
                                Slider(
                                    value = currentBlur,
                                    onValueChange = { newVal ->
                                        viewModel.setNavBlur(newVal)
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = accent,
                                        activeTrackColor = accent,
                                        inactiveTrackColor = AuraTheme.colors.border
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("nav_blur_slider")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "0% (No Blur)",
                                        fontSize = 10.sp,
                                        color = AuraTheme.colors.textMuted
                                    )
                                    Text(
                                        text = "Default: 80%",
                                        fontSize = 10.sp,
                                        color = AuraTheme.colors.textMuted
                                    )
                                    Text(
                                        text = "100% (Max Blur)",
                                        fontSize = 10.sp,
                                        color = AuraTheme.colors.textMuted
                                    )
                                }
                            }

                            // Reset Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AuraTheme.colors.card)
                                    .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setNavTransparency(0.05f)
                                        viewModel.setNavBlur(0.80f)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 14.dp)
                                    .testTag("reset_nav_bar_appearance_btn"),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Defaults",
                                    tint = AuraTheme.colors.secondaryText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = "Reset to Defaults (5% Transparency, 80% Blur)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AuraTheme.colors.secondaryText
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 4. WORKOUT FEEDBACK & SCREEN
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "WORKOUT FEEDBACK & SCREEN",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )

                            SettingsToggleRow(
                                title = "Haptic Feedback",
                                subtitle = "Vibrate on rest timer, hold completion, and milestones",
                                checked = hapticsEnabled,
                                onCheckedChange = { viewModel.setHapticsEnabled(it) },
                                accent = accent,
                                tag = "setting_haptics_switch"
                            )

                            SettingsToggleRow(
                                title = "Rest Timer Sound",
                                subtitle = "Play a gentle tone when rest or hold timers finish",
                                checked = soundEnabled,
                                onCheckedChange = { viewModel.setSoundEnabled(it) },
                                accent = accent,
                                tag = "setting_sound_switch"
                            )

                            SettingsToggleRow(
                                title = "Keep Screen On",
                                subtitle = "Prevent screen sleep during an active workout session",
                                checked = keepScreenOn,
                                onCheckedChange = { viewModel.setKeepScreenOn(it) },
                                accent = accent,
                                tag = "setting_keep_screen_on_switch"
                            )
                        }
                    }

                    // ==========================================
                    // 5. SAFETY, HEALTH & PRIVACY
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "SAFETY & LEGAL",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )

                            SettingsLinkRow(
                                icon = Icons.Default.Info,
                                title = "About & Medical Disclaimer",
                                subtitle = "General fitness disclaimer and offline app info",
                                onClick = onOpenAbout,
                                accent = accent,
                                tag = "settings_about_btn"
                            )

                            SettingsLinkRow(
                                icon = Icons.Default.HealthAndSafety,
                                title = "5-Question Health Check (PAR-Q)",
                                subtitle = when {
                                    parqCompleted && parqFlagged -> "Doctor clearance advised — review answers"
                                    parqCompleted -> "Completed — all clear"
                                    else -> "Optional readiness questionnaire"
                                },
                                onClick = onOpenParq,
                                accent = accent,
                                tag = "settings_parq_btn"
                            )

                            SettingsLinkRow(
                                icon = Icons.Default.Lock,
                                title = "Privacy Policy",
                                subtitle = "Zero accounts, zero tracking, 100% on-device data",
                                onClick = onOpenPrivacy,
                                accent = accent,
                                tag = "settings_privacy_btn"
                            )
                        }
                    }

                    if (BuildConfig.DEBUG && onOpenDiagnostics != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(AuraTheme.colors.surfaceSecondary)
                                .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                .clickable { onOpenDiagnostics() }
                                .padding(16.dp)
                                .testTag("settings_open_diagnostics_btn")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(accent.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BugReport,
                                            contentDescription = "Diagnostics",
                                            tint = accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "DEVELOPER DIAGNOSTICS",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = accent
                                        )
                                        Text(
                                            text = "Diagnostics & Demo Coverage",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AuraTheme.colors.primaryText
                                        )
                                        Text(
                                            text = "Inspect photo matches & animation fallbacks",
                                            fontSize = 10.5.sp,
                                            color = AuraTheme.colors.secondaryText
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Open Diagnostics",
                                    tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Adjustments take effect immediately on the bottom navigation bar and across the app.",
                        fontSize = 11.sp,
                        color = AuraTheme.colors.textMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTheme.colors.primaryText
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = AuraTheme.colors.secondaryText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AuraTheme.colors.accentContrast,
                checkedTrackColor = accent
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
private fun SettingsLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    accent: Color,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(tag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTheme.colors.primaryText
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = AuraTheme.colors.secondaryText
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AuraTheme.colors.secondaryText,
            modifier = Modifier.size(18.dp)
        )
    }
}
