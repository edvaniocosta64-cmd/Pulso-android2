package com.example

import com.example.game.AppScreen
import com.example.game.BoardEngine
import com.example.game.BoardPos
import com.example.game.GameMode
import com.example.game.PulsoViewModel
import com.example.game.TileKind
import com.example.game.TutorialStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PulsoGameTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun boardEngine_findsConnectedClusterAndCalculatesComboScore() {
        val (grid, spotlight) = BoardEngine.createStep2TutorialBoard()
        val cluster = BoardEngine.findConnectedCluster(grid, BoardPos(1, 0))
        assertEquals(spotlight, cluster)
        assertEquals(60, BoardEngine.calculateClusterPoints(4))
        assertEquals(20, BoardEngine.calculateClusterPoints(2))
        assertEquals(0, BoardEngine.calculateClusterPoints(1))
    }

    @Test
    fun tutorialFlow_advancesCleanlyThroughAllGuidedSteps() {
        val vm = PulsoViewModel()
        vm.openTutorial()

        var state = vm.uiState.value
        assertEquals(AppScreen.GAME, state.currentScreen)
        assertEquals(GameMode.BEGINNER_TUTORIAL, state.mode)
        assertEquals(TutorialStep.STEP_1_CONNECT_PAIR, state.tutorialStep)

        // Tapping outside spotlight does not advance step 1
        vm.onTileTap(BoardPos(0, 0))
        assertEquals(TutorialStep.STEP_1_CONNECT_PAIR, vm.uiState.value.tutorialStep)

        // Tapping highlighted SOLAR pair advances to step 2
        vm.onTileTap(BoardPos(1, 0))
        state = vm.uiState.value
        assertEquals(TutorialStep.STEP_2_COMBO_ENERGY, state.tutorialStep)
        assertEquals(20, state.score)
        assertEquals(2, state.energy)

        // Tapping highlighted ONDA combo cluster fills energy and advances to step 3
        vm.onTileTap(BoardPos(1, 1))
        state = vm.uiState.value
        assertEquals(TutorialStep.STEP_3_PULSE_POWER, state.tutorialStep)
        assertEquals(state.maxEnergy, state.energy)
        assertTrue(state.highlightPulseButton)

        // Arming pulse highlights CHAMA tiles
        vm.onPulseButtonClick()
        state = vm.uiState.value
        assertTrue(state.isPulseArmed)
        assertFalse(state.highlightPulseButton)
        assertTrue(state.spotlightPositions.isNotEmpty())
        assertTrue(
            state.spotlightPositions.all { pos ->
                state.grid[pos.row][pos.col].kind == TileKind.CHAMA
            }
        )

        // Tapping a CHAMA tile clears them and starts Step 4 Guided Practice
        val firstChama = state.spotlightPositions.first()
        vm.onTileTap(firstChama)
        state = vm.uiState.value
        assertEquals(TutorialStep.STEP_4_GUIDED_PRACTICE, state.tutorialStep)
        assertFalse(state.isPulseArmed)
        assertEquals(4, state.grid.size)
    }

    @Test
    fun skipTutorial_movesToBeginnerPractice() {
        val vm = PulsoViewModel()
        vm.openTutorial()
        vm.skipTutorialToPractice()

        val state = vm.uiState.value
        assertEquals(GameMode.BEGINNER_PRACTICE, state.mode)
        assertTrue(state.beginnerTutorialDone)
        assertEquals(null, state.movesLeft)
    }
}
