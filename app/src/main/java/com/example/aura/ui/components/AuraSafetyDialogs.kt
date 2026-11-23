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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.aura.ui.theme.AuraTheme

const val AURA_MEDICAL_DISCLAIMER_TEXT =
    "AURA gives general fitness guidance and is not medical advice. Stop if you feel pain, and check with a doctor before starting if you have a health condition."

@Composable
fun MedicalDisclaimerAboutDialog(
    isFirstLaunch: Boolean,
    onAcknowledge: () -> Unit,
    onOpenPrivacy: (() -> Unit)? = null,
    onOpenParq: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    Dialog(
        onDismissRequest = {
            if (isFirstLaunch) onAcknowledge() else onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AuraTheme.colors.dialogBackground)
                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag(if (isFirstLaunch) "first_launch_disclaimer_dialog" else "about_safety_dialog")
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About AURA",
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isFirstLaunch) "BEFORE YOU START" else "ABOUT & SAFETY",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = "AURA Fitness v${BuildConfig.VERSION_NAME}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText
                            )
                        }
                    }
                    if (!isFirstLaunch) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(48.dp)
                                .clip(CircleShape)
                                .testTag("close_about_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close About dialog",
                                tint = AuraTheme.colors.secondaryText
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AuraTheme.colors.warning.copy(alpha = 0.10f))
                        .border(1.dp, AuraTheme.colors.warning.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "IMPORTANT HEALTH NOTICE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AuraTheme.colors.warning
                        )
                        Text(
                            text = AURA_MEDICAL_DISCLAIMER_TEXT,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = AuraTheme.colors.primaryText
                        )
                    }
                }

                Text(
                    text = "All workout plans, nutrition targets, check-ins, and photos stay offline on your device. No account, ads, or cloud tracking.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = AuraTheme.colors.secondaryText
                )

                if (onOpenParq != null || onOpenPrivacy != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (onOpenParq != null) {
                            OutlinedButton(
                                onClick = onOpenParq,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Health Check", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (onOpenPrivacy != null) {
                            OutlinedButton(
                                onClick = onOpenPrivacy,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (isFirstLaunch) onAcknowledge() else onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = accentContrast
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("acknowledge_disclaimer_btn")
                ) {
                    Text(
                        text = if (isFirstLaunch) "I Understand" else "Done",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ParqHealthCheckDialog(
    onCompleted: (hasRiskFlags: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    val questions = remember {
        listOf(
            "Has a doctor ever said you have a heart condition or high blood pressure?",
            "Do you feel chest pain at rest or during physical activity?",
            "Do you lose balance due to dizziness or ever lose consciousness?",
            "Do you have a bone, joint, or back problem that could worsen with exercise?",
            "Are you aware of any other medical reason you should not exercise without clearance?"
        )
    }
    // 0 = unanswered/No, 1 = Yes (default false = No so user actively toggles any Yes)
    val answers = rememberSaveable { mutableStateListOf(false, false, false, false, false) }
    val hasAnyYes = answers.any { it }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AuraTheme.colors.dialogBackground)
                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("parq_health_dialog")
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = "Health Check",
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "OPTIONAL READINESS CHECK",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = "5-Question Health Check",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(CircleShape)
                            .testTag("close_parq_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Health Check",
                            tint = AuraTheme.colors.secondaryText
                        )
                    }
                }

                Text(
                    text = "Quick PAR-Q readiness check before increasing training load:",
                    fontSize = 12.sp,
                    color = AuraTheme.colors.secondaryText
                )

                questions.forEachIndexed { idx, question ->
                    val isYes = answers[idx]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "${idx + 1}. $question",
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = AuraTheme.colors.primaryText
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(false to "No", true to "Yes").forEach { (value, label) ->
                                    val selected = isYes == value
                                    val bg = when {
                                        selected && value -> AuraTheme.colors.warning.copy(alpha = 0.22f)
                                        selected -> accent.copy(alpha = 0.20f)
                                        else -> AuraTheme.colors.card
                                    }
                                    val stroke = when {
                                        selected && value -> AuraTheme.colors.warning
                                        selected -> accent
                                        else -> AuraTheme.colors.border
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bg)
                                            .border(1.dp, stroke, RoundedCornerShape(12.dp))
                                            .clickable { answers[idx] = value },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected && value) AuraTheme.colors.warning else if (selected) accent else AuraTheme.colors.secondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (hasAnyYes) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AuraTheme.colors.warning.copy(alpha = 0.12f))
                            .border(1.dp, AuraTheme.colors.warning.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "You answered Yes to at least one question. Please consult a doctor before starting or progressing your workout plan, and start gently.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraTheme.colors.warning
                        )
                    }
                } else {
                    Text(
                        text = "All clear based on your answers. Always warm up well and stop if you feel dizziness or sharp pain.",
                        fontSize = 11.5.sp,
                        color = AuraTheme.colors.success
                    )
                }

                Button(
                    onClick = { onCompleted(hasAnyYes) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = accentContrast
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("save_parq_btn")
                ) {
                    Text("Save Check & Continue", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AuraTheme.colors.dialogBackground)
                .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("privacy_policy_dialog")
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Privacy Policy",
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DATA & PRIVACY",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = "Offline-First Privacy Policy",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(CircleShape)
                            .testTag("close_privacy_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Privacy Policy",
                            tint = AuraTheme.colors.secondaryText
                        )
                    }
                }

                PrivacyBullet(
                    title = "100% On-Device Storage",
                    body = "Your profile metrics, workout logs, body check-ins, progress photos, and preferences are stored locally on your phone using Android Room and SharedPreferences."
                )
                PrivacyBullet(
                    title = "Zero Accounts & Zero Tracking",
                    body = "AURA has no user accounts, no cloud database sync, no advertising SDKs, and no third-party analytics. Nothing is sold or shared."
                )
                PrivacyBullet(
                    title = "Network Usage",
                    body = "Internet access is used only to download and cache public-domain exercise demonstration photos and catalog data from GitHub so demos work offline afterwards."
                )
                PrivacyBullet(
                    title = "Your Control",
                    body = "You can export a local JSON backup anytime or erase all stored app data from Profile using Reset All Data."
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = accentContrast
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("close_privacy_confirm_btn")
                ) {
                    Text("Close", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PrivacyBullet(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AuraTheme.colors.surfaceSecondary)
            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AuraTheme.colors.primaryText
        )
        Text(
            text = body,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = AuraTheme.colors.secondaryText
        )
    }
}
