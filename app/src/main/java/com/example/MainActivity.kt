package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.AppScreen
import com.example.game.PulsoViewModel
import com.example.ui.components.RulesModalSheet
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.PulsoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            PulsoTheme {
                val viewModel: PulsoViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Crossfade(
                    targetState = uiState.currentScreen,
                    animationSpec = tween(durationMillis = 240),
                    label = "appScreenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.HOME -> HomeScreen(
                            uiState = uiState,
                            onStartTutorial = viewModel::openTutorial,
                            onStartBeginnerPractice = viewModel::openBeginnerPractice,
                            onStartStandardMode = viewModel::openStandardMode,
                            onOpenRules = { viewModel.setRulesSheetVisible(true) }
                        )

                        AppScreen.GAME -> GameScreen(
                            uiState = uiState,
                            onTileTap = viewModel::onTileTap,
                            onPulseClick = viewModel::onPulseButtonClick,
                            onHintClick = viewModel::requestHint,
                            onSkipTutorial = viewModel::skipTutorialToPractice,
                            onRestart = viewModel::restartCurrentMode,
                            onOpenRules = { viewModel.setRulesSheetVisible(true) },
                            onContinueBeginner = viewModel::openBeginnerPractice,
                            onGoToStandard = viewModel::openStandardMode,
                            onNavigateHome = viewModel::navigateHome
                        )
                    }
                }

                if (uiState.showRulesSheet) {
                    RulesModalSheet(
                        onDismiss = { viewModel.setRulesSheetVisible(false) }
                    )
                }
            }
        }
    }
}
