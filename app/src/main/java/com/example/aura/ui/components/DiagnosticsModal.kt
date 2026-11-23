package com.example.aura.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.BuildConfig
import com.example.aura.engine.DemoCoverageEntry
import com.example.aura.engine.DiagnosticItem
import com.example.aura.engine.ExerciseDemoRepository
import com.example.aura.ui.theme.AuraTheme
import kotlin.math.roundToInt

@Composable
fun DiagnosticsModal(
    isVisible: Boolean,
    results: List<DiagnosticItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast
    val isDark = AuraTheme.colors.isDark

    // Debug tab: 0 = Tests (26-point suite), 1 = Demo Coverage (debug-only)
    var selectedTab by remember { mutableIntStateOf(if (BuildConfig.DEBUG) 1 else 0) }
    var coverageFilter by remember { mutableStateOf("All") }

    val catalogState by ExerciseDemoRepository.state.collectAsState()
    LaunchedEffect(isVisible) {
        if (isVisible && BuildConfig.DEBUG) {
            ExerciseDemoRepository.ensureInitialized(context)
        }
    }

    val coverageEntries = remember(catalogState, selectedTab, isVisible) {
        if (BuildConfig.DEBUG && isVisible) {
            ExerciseDemoRepository.buildCoverageReport()
        } else {
            emptyList()
        }
    }

    val matchedCount = remember(coverageEntries) { coverageEntries.count { it.match != null } }
    val animationCount = remember(coverageEntries, matchedCount) { coverageEntries.size - matchedCount }

    val filteredCoverage = remember(coverageEntries, coverageFilter) {
        when (coverageFilter) {
            "Photo" -> coverageEntries.filter { it.match != null }
            "Animation" -> coverageEntries.filter { it.match == null }
            else -> coverageEntries
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() }
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(AuraTheme.colors.card)
                    .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
                    .testTag("diagnostics_modal")
            ) {
                Column {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Diagnostics & Validation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (BuildConfig.DEBUG && selectedTab == 1) {
                                    "Debug Demo Coverage Report"
                                } else {
                                    "26-Point Automated Test Suite"
                                },
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AuraTheme.colors.secondaryText
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (BuildConfig.DEBUG && selectedTab == 1) {
                                IconButton(
                                    onClick = { ExerciseDemoRepository.refreshNow(context) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AuraTheme.colors.surfaceSecondary)
                                        .border(1.dp, AuraTheme.colors.border, CircleShape)
                                        .testTag("diagnostics_refresh_demos_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Refresh Exercise DB Catalog",
                                        tint = accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AuraTheme.colors.surfaceSecondary)
                                    .border(1.dp, AuraTheme.colors.border, CircleShape)
                                    .testTag("close_diagnostics_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = AuraTheme.colors.secondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(AuraTheme.colors.surfaceSecondary)
                                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(14.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val tabs = listOf(
                                1 to "Demo Coverage (${coverageEntries.size})",
                                0 to "Tests (${results.size})"
                            )
                            tabs.forEach { (tabId, label) ->
                                val active = selectedTab == tabId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) accent else Color.Transparent)
                                        .clickable { selectedTab = tabId }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) accentContrast else AuraTheme.colors.secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (BuildConfig.DEBUG && selectedTab == 1) {
                        // Summary counts row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CoverageStatCard(
                                label = "TOTAL",
                                value = "${coverageEntries.size}",
                                tint = accent,
                                modifier = Modifier.weight(1f)
                            )
                            CoverageStatCard(
                                label = "PHOTO",
                                value = "$matchedCount",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            CoverageStatCard(
                                label = "ANIMATION",
                                value = "$animationCount",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Catalog fetch status + Filter chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statusLine = when {
                                catalogState.isRefreshing -> "Syncing free-exercise-db..."
                                catalogState.remoteCatalogCount > 0 ->
                                    "Catalog: ${catalogState.remoteCatalogCount} remote"
                                catalogState.lastError != null ->
                                    "Offline fallback (${catalogState.lastError})"
                                else -> "Offline fallback active"
                            }
                            Text(
                                text = statusLine,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AuraTheme.colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("All", "Photo", "Animation").forEach { filter ->
                                    val active = coverageFilter == filter
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) accent.copy(alpha = 0.2f) else AuraTheme.colors.surfaceSecondary)
                                            .border(
                                                1.dp,
                                                if (active) accent else AuraTheme.colors.cardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { coverageFilter = filter }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = filter,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                            color = if (active) accent else AuraTheme.colors.secondaryText
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("demo_coverage_list")
                        ) {
                            items(filteredCoverage, key = { it.id }) { entry ->
                                DemoCoverageRow(entry = entry, isDark = isDark, accent = accent)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(results) { index, item ->
                                val isPass = item.passed
                                val borderColor = if (isPass) (if (isDark) Color(0x4D10B981) else Color(0x66059669)) else (if (isDark) Color(0x4DEF4444) else Color(0x66DC2626))
                                val bgColor = if (isPass) (if (isDark) Color(0x1A10B981) else Color(0x1410B981)) else (if (isDark) Color(0x1AEF4444) else Color(0x14EF4444))
                                val badgeColor = if (isPass) (if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)) else (if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(bgColor)
                                        .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${index + 1}. ${item.name}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isPass) (if (isDark) Color(0x3310B981) else Color(0x2810B981)) else (if (isDark) Color(0x33EF4444) else Color(0x28EF4444)))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isPass) "PASS" else "FAIL",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = badgeColor
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.detail,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
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
    }
}

@Composable
private fun CoverageStatCard(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = tint
        )
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = AuraTheme.colors.secondaryText
        )
    }
}

@Composable
private fun DemoCoverageRow(
    entry: DemoCoverageEntry,
    isDark: Boolean,
    accent: Color
) {
    val matched = entry.match != null
    val badgeColor = when {
        matched -> if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
        else -> if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
    }
    val bgColor = when {
        matched -> Color(0x1410B981)
        else -> AuraTheme.colors.surfaceSecondary
    }
    val borderColor = when {
        matched -> Color(0x4410B981)
        else -> AuraTheme.colors.cardBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTheme.colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.18f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    val pct = entry.match?.let { (it.confidence * 100.0).roundToInt() }
                    Text(
                        text = if (matched) "PHOTO $pct%" else "ANIMATION",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entry.source} • ${entry.id}",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AuraTheme.colors.textMuted
                )
                Text(
                    text = if (entry.match != null) {
                        "-> ${entry.match.remoteName}"
                    } else {
                        "Pattern: ${entry.movementPattern.displayName}"
                    },
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (entry.match != null) accent else AuraTheme.colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
