package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.aura.ui.AuraApp
import com.example.aura.ui.AuraViewModel

class MainActivity : ComponentActivity() {
  private val auraViewModel: AuraViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)

    // Hold the plain #010204 system splash until hasSeenWelcome is loaded from DataStore
    splashScreen.setKeepOnScreenCondition {
      auraViewModel.hasSeenWelcome.value == null
    }

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )

    setContent {
      AuraApp(viewModel = auraViewModel)
    }
  }
}
