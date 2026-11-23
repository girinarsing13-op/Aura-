package com.example.aura.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aura.ui.components.DiagnosticsModal
import com.example.aura.ui.components.DynamicIslandRestPill
import com.example.aura.ui.components.LiquidGlassNavBar
import com.example.aura.ui.components.MedicalDisclaimerAboutDialog
import com.example.aura.ui.components.ParqHealthCheckDialog
import com.example.aura.ui.components.PrivacyPolicyDialog
import com.example.aura.ui.components.QuickThemeModal
import com.example.aura.ui.components.SettingsModal
import com.example.aura.ui.components.rememberRestTimerController
import com.example.aura.ui.screens.DietScreen
import com.example.aura.ui.screens.HomeScreen
import com.example.aura.ui.screens.LaunchSplash
import com.example.aura.ui.screens.OnboardingScreen
import com.example.aura.ui.screens.ProfileScreen
import com.example.aura.ui.screens.ProgressScreen
import com.example.aura.ui.screens.StreaksScreen
import com.example.aura.ui.screens.WelcomeScreen
import com.example.aura.ui.screens.WorkoutScreen
import com.example.aura.ui.theme.AuraTheme

@Composable
fun AuraApp(
    viewModel: AuraViewModel = viewModel()
) {
    val profile by viewModel.userProfile.collectAsState()
    val draft by viewModel.draftProfile.collectAsState()
    val hasSeenWelcome by viewModel.hasSeenWelcome.collectAsState()
    val isAppReady by viewModel.isAppReady.collectAsState()
    val backupStatus by viewModel.backupStatusMessage.collectAsState()
    val user = profile ?: draft

    val liveTransparency by viewModel.liveNavTransparency.collectAsState()
    val liveBlur by viewModel.liveNavBlur.collectAsState()
    val liveThemeMode by viewModel.liveThemeMode.collectAsState()
    val liveThemeAccent by viewModel.liveThemeAccent.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val disclaimerAcknowledged by viewModel.disclaimerAcknowledged.collectAsState()
    val parqCompleted by viewModel.parqCompleted.collectAsState()
    val parqFlagged by viewModel.parqFlagged.collectAsState()

    val currentMode = (liveThemeMode ?: user.themeMode).lowercase()
    val currentAccentHex = liveThemeAccent ?: user.themeAccent

    val navTransparency = liveTransparency ?: user.navTransparency
    val navBlur = liveBlur ?: user.navBlur

    var activeTab by rememberSaveable { mutableStateOf("home") }
    var isProgressOpen by rememberSaveable { mutableStateOf(false) }
    var isWorkoutFullScreen by rememberSaveable { mutableStateOf(false) }
    var openTodayWorkoutTrigger by rememberSaveable { mutableStateOf(0) }
    var isQuickThemeOpen by rememberSaveable { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var isDiagnosticsOpen by rememberSaveable { mutableStateOf(false) }
    var isAboutOpen by rememberSaveable { mutableStateOf(false) }
    var isParqOpen by rememberSaveable { mutableStateOf(false) }
    var isPrivacyOpen by rememberSaveable { mutableStateOf(false) }

    var launchPhase by rememberSaveable { mutableStateOf<String?>(null) }

    val restoreAccountLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackupFromUri(uri)
        }
    }

    LaunchedEffect(hasSeenWelcome) {
        if (launchPhase == null && hasSeenWelcome != null) {
            launchPhase = if (hasSeenWelcome == true) "splash" else "welcome"
        }
    }

    LaunchedEffect(profile?.onboardingCompleted, launchPhase) {
        if (launchPhase == "welcome" && profile?.onboardingCompleted == true) {
            viewModel.markWelcomeSeen()
            launchPhase = "app"
        }
    }

    val restTimerController = rememberRestTimerController()

    AuraTheme(
        themeMode = currentMode,
        accentHex = currentAccentHex
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (launchPhase != "app") Color(0xFF010204) else AuraTheme.colors.background)
        ) {
            when (launchPhase) {
                null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF010204))
                    )
                }

                "welcome" -> {
                    WelcomeScreen(
                        backupMessage = backupStatus,
                        onGetStarted = {
                            viewModel.clearBackupStatus()
                            viewModel.markWelcomeSeen()
                            viewModel.setOnboardingStep(1)
                            launchPhase = "app"
                        },
                        onExistingAccount = {
                            restoreAccountLauncher.launch(arrayOf("application/json", "*/*"))
                        }
                    )
                }

                "splash" -> {
                    LaunchSplash(
                        isAppReady = isAppReady,
                        onFinished = {
                            launchPhase = "app"
                        }
                    )
                }

                else -> if (!user.onboardingCompleted) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onOpenParq = { isParqOpen = true },
                    onFinished = {
                        activeTab = "home"
                        isProgressOpen = false
                        isWorkoutFullScreen = false
                    }
                )
            } else {
                // Primary App Content
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isProgressOpen) {
                        ProgressScreen(
                            viewModel = viewModel,
                            onBack = { isProgressOpen = false }
                        )
                    } else {
                        when (activeTab) {
                            "home" -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToWorkout = {
                                    openTodayWorkoutTrigger += 1
                                    activeTab = "workout"
                                },
                                onOpenProgress = { isProgressOpen = true },
                                onOpenColorModal = { isQuickThemeOpen = true },
                                onOpenDiagnostics = { isDiagnosticsOpen = true }
                            )
                            "workout" -> WorkoutScreen(
                                viewModel = viewModel,
                                restTimerController = restTimerController,
                                onNavigateBack = {
                                    isWorkoutFullScreen = false
                                    activeTab = "home"
                                },
                                openTodayDirectlyTrigger = openTodayWorkoutTrigger,
                                onFullScreenChanged = { full ->
                                    isWorkoutFullScreen = full
                                }
                            )
                            "diet" -> DietScreen(
                                viewModel = viewModel
                            )
                            "streaks" -> StreaksScreen(
                                viewModel = viewModel
                            )
                            "profile" -> ProfileScreen(
                                viewModel = viewModel,
                                onOpenSettings = { isSettingsOpen = true },
                                onOpenProgress = { isProgressOpen = true },
                                onOpenColorModal = { isQuickThemeOpen = true },
                                onOpenDiagnostics = { isDiagnosticsOpen = true },
                                onOpenAbout = { isAboutOpen = true },
                                onOpenParq = { isParqOpen = true },
                                onOpenPrivacy = { isPrivacyOpen = true },
                                parqCompleted = parqCompleted,
                                parqFlagged = parqFlagged,
                                onNavigateBack = { activeTab = "home" }
                            )
                            else -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToWorkout = {
                                    openTodayWorkoutTrigger += 1
                                    activeTab = "workout"
                                },
                                onOpenProgress = { isProgressOpen = true },
                                onOpenColorModal = { isQuickThemeOpen = true },
                                onOpenDiagnostics = { isDiagnosticsOpen = true }
                            )
                        }
                    }
                }

                // Floating Pill Navigation Bar (Hidden on onboarding, Progress, Today's workout, Session, Rest, Complete)
                if (!isProgressOpen && !(activeTab == "workout" && isWorkoutFullScreen)) {
                    LiquidGlassNavBar(
                        activeTab = activeTab,
                        onTabSelected = {
                            isProgressOpen = false
                            if (it != "workout") isWorkoutFullScreen = false
                            activeTab = it
                        },
                        navTransparency = navTransparency,
                        navBlur = navBlur,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                // Dynamic Island Rest Timer Pill (hidden during focused full-screen workout flow)
                if (!(activeTab == "workout" && isWorkoutFullScreen)) {
                    DynamicIslandRestPill(
                        controller = restTimerController,
                        hapticsEnabled = hapticsEnabled,
                        soundEnabled = soundEnabled,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                // Quick Theme Accent Modal
                if (isQuickThemeOpen) {
                    QuickThemeModal(
                        currentAccentHex = currentAccentHex,
                        onColorSelected = { newHex ->
                            viewModel.updateThemeAccent(newHex)
                        },
                        onDismiss = { isQuickThemeOpen = false }
                    )
                }

                // Settings Modal
                SettingsModal(
                    isVisible = isSettingsOpen,
                    viewModel = viewModel,
                    onDismiss = { isSettingsOpen = false },
                    onOpenDiagnostics = {
                        isSettingsOpen = false
                        isDiagnosticsOpen = true
                    },
                    onOpenAbout = {
                        isSettingsOpen = false
                        isAboutOpen = true
                    },
                    onOpenParq = {
                        isSettingsOpen = false
                        isParqOpen = true
                    },
                    onOpenPrivacy = {
                        isSettingsOpen = false
                        isPrivacyOpen = true
                    }
                )

                // Diagnostics Modal
                DiagnosticsModal(
                    isVisible = isDiagnosticsOpen,
                    results = viewModel.runDiagnostics(),
                    onDismiss = { isDiagnosticsOpen = false }
                )

                // Non-blocking First-Launch Medical Disclaimer OR Permanent About Dialog
                if (!disclaimerAcknowledged || isAboutOpen) {
                    val isFirstLaunchNotice = !disclaimerAcknowledged && !isAboutOpen
                    MedicalDisclaimerAboutDialog(
                        isFirstLaunch = isFirstLaunchNotice,
                        onAcknowledge = {
                            viewModel.acknowledgeDisclaimer()
                            isAboutOpen = false
                        },
                        onOpenPrivacy = {
                            if (isFirstLaunchNotice) viewModel.acknowledgeDisclaimer()
                            isAboutOpen = false
                            isPrivacyOpen = true
                        },
                        onOpenParq = {
                            if (isFirstLaunchNotice) viewModel.acknowledgeDisclaimer()
                            isAboutOpen = false
                            isParqOpen = true
                        },
                        onDismiss = {
                            isAboutOpen = false
                        }
                    )
                }
            }
            }

            if (isParqOpen) {
                ParqHealthCheckDialog(
                    onCompleted = { hasRiskFlags ->
                        viewModel.saveParqResult(hasRiskFlags)
                        isParqOpen = false
                    },
                    onDismiss = { isParqOpen = false }
                )
            }

            if (isPrivacyOpen) {
                PrivacyPolicyDialog(
                    onDismiss = { isPrivacyOpen = false }
                )
            }
        }
    }
}
