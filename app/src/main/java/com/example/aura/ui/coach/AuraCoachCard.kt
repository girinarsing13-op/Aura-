package com.example.aura.ui.coach

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.ui.theme.AuraTheme

@Composable
fun AuraCoachCard(
    feedback: CoachFeedback,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null
) {
    val accent = AuraTheme.colors.accent
    var tapCounter by remember { mutableIntStateOf(0) }

    // Mood status colors based on mood category
    val (statusColor, badgeBg) = when (feedback.mood.category) {
        "Negative" -> Pair(Color(0xFFEF4444), Color(0x26EF4444))
        "Concerned" -> Pair(Color(0xFFF59E0B), Color(0x26F59E0B))
        "Positive", "Progress" -> Pair(accent, accent.copy(alpha = 0.16f))
        "Recovery" -> Pair(Color(0xFF818CF8), Color(0x26818CF8))
        else -> Pair(AuraTheme.colors.textMuted, AuraTheme.colors.cardBorder)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = statusColor.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AuraTheme.colors.card,
                        AuraTheme.colors.surfaceSecondary
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        statusColor.copy(alpha = 0.45f),
                        AuraTheme.colors.cardBorder,
                        statusColor.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                tapCounter++
                onTap?.invoke()
            }
            .padding(16.dp)
            .testTag("aura_coach_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.14f))
                    .border(1.dp, statusColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                com.example.aura.ui.components.AuraAppLogo(size = 38.dp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // --- Coach Guidance Copy ---
            Column(modifier = Modifier.weight(1f)) {
                // Header badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = feedback.tag.uppercase(),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = feedback.mood.displayName.uppercase(),
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Headline
                Text(
                    text = feedback.headline,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuraTheme.colors.primaryText
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Primary Message
                Text(
                    text = feedback.message,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AuraTheme.colors.secondaryText,
                    lineHeight = 16.sp
                )

                // Data Reasoning (Analytical cause from CoachMoodEngine)
                if (feedback.reason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "DATA: ${feedback.reason}",
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = statusColor.copy(alpha = 0.90f),
                        lineHeight = 12.sp
                    )
                }

                // Suggested Action / Secondary Advice
                val action = feedback.suggestedAction ?: feedback.secondaryAdvice
                if (!action.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "ACTION: $action",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AuraTheme.colors.textMuted,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
