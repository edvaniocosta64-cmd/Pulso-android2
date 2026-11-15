package com.example.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PulsoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var hintTimerJob: Job? = null

    fun openTutorial() {
        val (grid, spotlight) = BoardEngine.createStep1TutorialBoard()
        _uiState.update { state ->
            state.copy(
                currentScreen = AppScreen.GAME,
                mode = GameMode.BEGINNER_TUTORIAL,
                grid = grid,
                score = 0,
                goalScore = 100,
                movesLeft = null,
                energy = 1,
                isPulseArmed = false,
                tutorialStep = TutorialStep.STEP_1_CONNECT_PAIR,
                spotlightPositions = spotlight,
                highlightPulseButton = false,
                hintPositions = emptySet(),
                lastClearedPositions = emptySet(),
                feedback = null,
                isRoundWon = false,
                isGameOver = false
            )
        }
        cancelHintTimer()
    }

    fun openBeginnerPractice() {
        val grid = BoardEngine.createPlayableBoard(rows = 4, cols = 4)
        _uiState.update { state ->
            state.copy(
                currentScreen = AppScreen.GAME,
                mode = GameMode.BEGINNER_PRACTICE,
                grid = grid,
                score = 0,
                goalScore = 200,
                movesLeft = null,
                energy = 1,
                isPulseArmed = false,
                tutorialStep = TutorialStep.COMPLETED,
                spotlightPositions = emptySet(),
                highlightPulseButton = false,
                hintPositions = emptySet(),
                lastClearedPositions = emptySet(),
                feedback = null,
                isRoundWon = false,
                isGameOver = false
            )
        }
        scheduleBeginnerHint()
    }

    fun openStandardMode() {
        val grid = BoardEngine.createPlayableBoard(rows = 4, cols = 4)
        _uiState.update { state ->
            state.copy(
                currentScreen = AppScreen.GAME,
                mode = GameMode.STANDARD,
                grid = grid,
                score = 0,
                goalScore = 300,
                movesLeft = 15,
                energy = 0,
                isPulseArmed = false,
                tutorialStep = TutorialStep.COMPLETED,
                spotlightPositions = emptySet(),
                highlightPulseButton = false,
                hintPositions = emptySet(),
                lastClearedPositions = emptySet(),
                feedback = null,
                isRoundWon = false,
                isGameOver = false
            )
        }
        cancelHintTimer()
    }

    fun navigateHome() {
        cancelHintTimer()
        _uiState.update { it.copy(currentScreen = AppScreen.HOME, isPulseArmed = false) }
    }

    fun skipTutorialToPractice() {
        _uiState.update { it.copy(beginnerTutorialDone = true) }
        openBeginnerPractice()
    }

    fun restartCurrentMode() {
        when (_uiState.value.mode) {
            GameMode.BEGINNER_TUTORIAL -> openTutorial()
            GameMode.BEGINNER_PRACTICE -> openBeginnerPractice()
            GameMode.STANDARD -> openStandardMode()
        }
    }

    fun setRulesSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showRulesSheet = visible) }
    }

    fun requestHint() {
        val state = _uiState.value
        if (state.isRoundWon || state.isGameOver) return
        if (state.mode == GameMode.BEGINNER_TUTORIAL &&
            state.tutorialStep != TutorialStep.STEP_4_GUIDED_PRACTICE
        ) {
            return
        }
        val best = BoardEngine.findBestHintCluster(state.grid)
        _uiState.update {
            it.copy(
                hintPositions = best,
                feedback = if (best.isNotEmpty()) FeedbackEvent.HintShown else it.feedback
            )
        }
    }

    fun onPulseButtonClick() {
        val state = _uiState.value
        if (state.isRoundWon || state.isGameOver) return
        if (state.energy < state.maxEnergy) return

        // Toggle pulse arming (don't allow unarming during tutorial step 3 once armed)
        if (state.mode == GameMode.BEGINNER_TUTORIAL &&
            state.tutorialStep == TutorialStep.STEP_3_PULSE_POWER
        ) {
            val chamaSpots = BoardEngine.findAllOfKind(state.grid, TileKind.CHAMA)
            _uiState.update {
                it.copy(
                    isPulseArmed = true,
                    highlightPulseButton = false,
                    spotlightPositions = chamaSpots,
                    feedback = FeedbackEvent.PulsePrompt
                )
            }
            return
        }

        val nextArmed = !state.isPulseArmed
        _uiState.update {
            it.copy(
                isPulseArmed = nextArmed,
                feedback = if (nextArmed) FeedbackEvent.PulsePrompt else null
            )
        }
    }

    fun onTileTap(pos: BoardPos) {
        val state = _uiState.value
        if (state.isRoundWon || state.isGameOver) return
        if (pos.row !in state.grid.indices || pos.col !in state.grid[pos.row].indices) return

        // Handle guided tutorial steps 1, 2, 3 strictly so new players learn each rule cleanly
        if (state.mode == GameMode.BEGINNER_TUTORIAL &&
            state.tutorialStep != TutorialStep.STEP_4_GUIDED_PRACTICE &&
            state.tutorialStep != TutorialStep.COMPLETED
        ) {
            handleGuidedTutorialTap(state, pos)
            return
        }

        // Standard or Guided Practice / Beginner Practice gameplay
        if (state.isPulseArmed) {
            executePulseClear(state, pos)
        } else {
            executeClusterTap(state, pos)
        }
    }

    private fun handleGuidedTutorialTap(state: GameUiState, pos: BoardPos) {
        when (state.tutorialStep) {
            TutorialStep.STEP_1_CONNECT_PAIR -> {
                if (pos !in state.spotlightPositions) {
                    _uiState.update { it.copy(feedback = FeedbackEvent.FollowSpotlight) }
                    return
                }
                val (nextGrid, nextSpotlight) = BoardEngine.createStep2TutorialBoard()
                _uiState.update {
                    it.copy(
                        grid = nextGrid,
                        score = it.score + 20,
                        energy = 2,
                        tutorialStep = TutorialStep.STEP_2_COMBO_ENERGY,
                        spotlightPositions = nextSpotlight,
                        highlightPulseButton = false,
                        feedback = FeedbackEvent.Points(points = 20, count = 2)
                    )
                }
            }

            TutorialStep.STEP_2_COMBO_ENERGY -> {
                if (pos !in state.spotlightPositions) {
                    _uiState.update { it.copy(feedback = FeedbackEvent.FollowSpotlight) }
                    return
                }
                val (nextGrid, _) = BoardEngine.createStep3TutorialBoard()
                _uiState.update {
                    it.copy(
                        grid = nextGrid,
                        score = it.score + 60,
                        energy = it.maxEnergy,
                        tutorialStep = TutorialStep.STEP_3_PULSE_POWER,
                        spotlightPositions = emptySet(),
                        highlightPulseButton = true,
                        feedback = FeedbackEvent.ComboEnergy(points = 60)
                    )
                }
            }

            TutorialStep.STEP_3_PULSE_POWER -> {
                if (!state.isPulseArmed) {
                    _uiState.update { it.copy(feedback = FeedbackEvent.FollowSpotlight) }
                    return
                }
                val tappedKind = state.grid[pos.row][pos.col].kind
                if (tappedKind != TileKind.CHAMA) {
                    _uiState.update { it.copy(feedback = FeedbackEvent.FollowSpotlight) }
                    return
                }
                val clearedCount = BoardEngine.findAllOfKind(state.grid, TileKind.CHAMA).size
                val practiceGrid = BoardEngine.createPlayableBoard(rows = 4, cols = 4)
                _uiState.update {
                    it.copy(
                        grid = practiceGrid,
                        score = 0, // Start clean practice score for Step 4
                        goalScore = 100,
                        energy = 1,
                        isPulseArmed = false,
                        highlightPulseButton = false,
                        spotlightPositions = emptySet(),
                        tutorialStep = TutorialStep.STEP_4_GUIDED_PRACTICE,
                        feedback = FeedbackEvent.PulseCleared(
                            count = clearedCount,
                            points = clearedCount * 15
                        )
                    )
                }
                scheduleBeginnerHint()
            }

            else -> Unit
        }
    }

    private fun executePulseClear(state: GameUiState, pos: BoardPos) {
        val targetKind = state.grid[pos.row][pos.col].kind
        val toRemove = BoardEngine.findAllOfKind(state.grid, targetKind)
        val earned = toRemove.size * 15
        val (nextGrid, reshuffled) = BoardEngine.removePositionsAndRefill(
            grid = state.grid,
            toRemove = toRemove,
            ensurePlayableMove = true
        )
        val nextScore = state.score + earned
        val nextMoves = state.movesLeft?.let { (it - 1).coerceAtLeast(0) }
        val won = evaluateWin(state, nextScore)
        val lost = !won && nextMoves != null && nextMoves <= 0

        _uiState.update {
            it.copy(
                grid = nextGrid,
                score = nextScore,
                bestStandardScore = if (state.mode == GameMode.STANDARD) {
                    maxOf(it.bestStandardScore, nextScore)
                } else {
                    it.bestStandardScore
                },
                movesLeft = nextMoves,
                energy = 0,
                isPulseArmed = false,
                hintPositions = emptySet(),
                lastClearedPositions = toRemove,
                tutorialStep = if (won && it.mode == GameMode.BEGINNER_TUTORIAL) {
                    TutorialStep.COMPLETED
                } else {
                    it.tutorialStep
                },
                beginnerTutorialDone = it.beginnerTutorialDone ||
                    (won && it.mode == GameMode.BEGINNER_TUTORIAL),
                isRoundWon = won,
                isGameOver = lost,
                feedback = if (reshuffled) {
                    FeedbackEvent.Reshuffled
                } else {
                    FeedbackEvent.PulseCleared(count = toRemove.size, points = earned)
                }
            )
        }
        if (!won && !lost) {
            scheduleBeginnerHint()
        } else {
            cancelHintTimer()
        }
    }

    private fun executeClusterTap(state: GameUiState, pos: BoardPos) {
        val cluster = BoardEngine.findConnectedCluster(state.grid, pos)
        if (cluster.size < 2) {
            _uiState.update { it.copy(feedback = FeedbackEvent.SingleTileWarning) }
            return
        }

        val earned = BoardEngine.calculateClusterPoints(cluster.size)
        val gainedEnergy = if (cluster.size >= 3) 1 else 0
        val nextEnergy = (state.energy + gainedEnergy).coerceAtMost(state.maxEnergy)

        val (nextGrid, reshuffled) = BoardEngine.removePositionsAndRefill(
            grid = state.grid,
            toRemove = cluster,
            ensurePlayableMove = true
        )

        val nextScore = state.score + earned
        val nextMoves = state.movesLeft?.let { (it - 1).coerceAtLeast(0) }
        val won = evaluateWin(state, nextScore)
        val lost = !won && nextMoves != null && nextMoves <= 0

        val event = when {
            reshuffled -> FeedbackEvent.Reshuffled
            gainedEnergy > 0 -> FeedbackEvent.ComboEnergy(points = earned)
            else -> FeedbackEvent.Points(points = earned, count = cluster.size)
        }

        _uiState.update {
            it.copy(
                grid = nextGrid,
                score = nextScore,
                bestStandardScore = if (state.mode == GameMode.STANDARD) {
                    maxOf(it.bestStandardScore, nextScore)
                } else {
                    it.bestStandardScore
                },
                movesLeft = nextMoves,
                energy = nextEnergy,
                hintPositions = emptySet(),
                lastClearedPositions = cluster,
                tutorialStep = if (won && it.mode == GameMode.BEGINNER_TUTORIAL) {
                    TutorialStep.COMPLETED
                } else {
                    it.tutorialStep
                },
                beginnerTutorialDone = it.beginnerTutorialDone ||
                    (won && it.mode == GameMode.BEGINNER_TUTORIAL),
                isRoundWon = won,
                isGameOver = lost,
                feedback = event
            )
        }
        if (!won && !lost) {
            scheduleBeginnerHint()
        } else {
            cancelHintTimer()
        }
    }

    private fun evaluateWin(state: GameUiState, nextScore: Int): Boolean {
        return when (state.mode) {
            GameMode.BEGINNER_TUTORIAL ->
                state.tutorialStep == TutorialStep.STEP_4_GUIDED_PRACTICE &&
                    nextScore >= state.goalScore

            GameMode.BEGINNER_PRACTICE -> nextScore >= state.goalScore
            GameMode.STANDARD -> nextScore >= state.goalScore
        }
    }

    private fun scheduleBeginnerHint() {
        cancelHintTimer()
        val state = _uiState.value
        val shouldAutoHint = state.mode == GameMode.BEGINNER_PRACTICE ||
            (state.mode == GameMode.BEGINNER_TUTORIAL &&
                state.tutorialStep == TutorialStep.STEP_4_GUIDED_PRACTICE)
        if (!shouldAutoHint) return

        hintTimerJob = viewModelScope.launch {
            delay(3200L)
            val latest = _uiState.value
            if (!latest.isRoundWon && !latest.isGameOver && !latest.isPulseArmed) {
                val hint = BoardEngine.findBestHintCluster(latest.grid)
                _uiState.update { it.copy(hintPositions = hint) }
            }
        }
    }

    private fun cancelHintTimer() {
        hintTimerJob?.cancel()
        hintTimerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelHintTimer()
    }
}
