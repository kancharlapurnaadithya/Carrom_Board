package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        // Smooth screens crossfade transitions
                        AnimatedContent(
                            targetState = gameViewModel.currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "screen_router"
                        ) { screen ->
                            when (screen) {
                                AppScreen.MENU -> {
                                    MainMenuScreen(viewModel = gameViewModel)
                                }
                                AppScreen.PLAYER_SETUP -> {
                                    PlayerSetupScreen(viewModel = gameViewModel)
                                }
                                AppScreen.GAME -> {
                                    GameScreen(viewModel = gameViewModel)
                                }
                                AppScreen.SETTINGS -> {
                                    SettingsScreen(viewModel = gameViewModel)
                                }
                                AppScreen.STATS -> {
                                    StatsScreen(viewModel = gameViewModel)
                                }
                                AppScreen.ACHIEVEMENTS -> {
                                    AchievementsScreen(viewModel = gameViewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
