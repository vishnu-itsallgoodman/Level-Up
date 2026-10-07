package com.levelup.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.levelup.app.data.repository.LevelUpRepository
import com.levelup.app.ui.MainNavGraph
import com.levelup.app.ui.screens.onboarding.OnboardingScreen
import com.levelup.app.ui.theme.Background
import com.levelup.app.ui.theme.LevelUpTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var repository: LevelUpRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LevelUpTheme {
                AppRoot(repository = repository)
            }
        }
    }
}

@Composable
private fun AppRoot(repository: LevelUpRepository) {
    // Determine whether onboarding has been completed
    var onboardingComplete by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        onboardingComplete = repository.isOnboardingComplete()
    }

    when (onboardingComplete) {
        null  -> Box(Modifier.fillMaxSize().background(Background)) // loading splash
        false -> OnboardingScreen(
            onComplete = { onboardingComplete = true }
        )
        true  -> MainNavGraph()
    }
}
