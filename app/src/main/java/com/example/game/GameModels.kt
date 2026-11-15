package com.example.game

import androidx.annotation.StringRes
import com.example.R

enum class TileKind(@StringRes val labelRes: Int) {
    SOLAR(R.string.tile_solar),
    ONDA(R.string.tile_onda),
    CHAMA(R.string.tile_chama),
    FOLHA(R.string.tile_folha)
}

data class BoardPos(val row: Int, val col: Int)

data class GameTile(
    val id: Long,
    val kind: TileKind
)

enum class GameMode {
    BEGINNER_TUTORIAL,
    BEGINNER_PRACTICE,
    STANDARD
}

enum class TutorialStep(val stepIndex: Int) {
    STEP_1_CONNECT_PAIR(1),
    STEP_2_COMBO_ENERGY(2),
    STEP_3_PULSE_POWER(3),
    STEP_4_GUIDED_PRACTICE(4),
    COMPLETED(4)
}

enum class AppScreen {
    HOME,
    GAME
}

sealed interface FeedbackEvent {
    data class Points(val points: Int, val count: Int) : FeedbackEvent
    data class ComboEnergy(val points: Int) : FeedbackEvent
    data class PulseCleared(val count: Int, val points: Int) : FeedbackEvent
    data object SingleTileWarning : FeedbackEvent
    data object FollowSpotlight : FeedbackEvent
    data object PulsePrompt : FeedbackEvent
    data object Reshuffled : FeedbackEvent
    data object HintShown : FeedbackEvent
}

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val mode: GameMode = GameMode.BEGINNER_TUTORIAL,
    val grid: List<List<GameTile>> = emptyList(),
    val score: Int = 0,
    val goalScore: Int = 100,
    val bestStandardScore: Int = 0,
    val movesLeft: Int? = null,
    val energy: Int = 0,
    val maxEnergy: Int = 3,
    val isPulseArmed: Boolean = false,
    val tutorialStep: TutorialStep = TutorialStep.STEP_1_CONNECT_PAIR,
    val beginnerTutorialDone: Boolean = false,
    val spotlightPositions: Set<BoardPos> = emptySet(),
    val highlightPulseButton: Boolean = false,
    val hintPositions: Set<BoardPos> = emptySet(),
    val lastClearedPositions: Set<BoardPos> = emptySet(),
    val feedback: FeedbackEvent? = null,
    val isRoundWon: Boolean = false,
    val isGameOver: Boolean = false,
    val showRulesSheet: Boolean = false
)
