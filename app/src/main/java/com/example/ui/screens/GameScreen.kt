package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TimerOff
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.BoardPos
import com.example.game.FeedbackEvent
import com.example.game.GameMode
import com.example.game.GameTile
import com.example.game.GameUiState
import com.example.game.TileKind
import com.example.game.TutorialStep
import com.example.ui.theme.LocalPulsoPalette
import com.example.ui.theme.TileColorPair

@Composable
fun GameScreen(
    uiState: GameUiState,
    onTileTap: (BoardPos) -> Unit,
    onPulseClick: () -> Unit,
    onHintClick: () -> Unit,
    onSkipTutorial: () -> Unit,
    onRestart: () -> Unit,
    onOpenRules: () -> Unit,
    onContinueBeginner: () -> Unit,
    onGoToStandard: () -> Unit,
    onNavigateHome: () -> Unit
) {
    BackHandler { onNavigateHome() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameTopBar(
                    uiState = uiState,
                    onBack = onNavigateHome,
                    onHint = onHintClick,
                    onOpenRules = onOpenRules,
                    onRestart = onRestart
                )

                if (uiState.mode == GameMode.BEGINNER_TUTORIAL &&
                    uiState.tutorialStep != TutorialStep.COMPLETED
                ) {
                    TutorialCoachCard(
                        step = uiState.tutorialStep,
                        isPulseArmed = uiState.isPulseArmed,
                        onSkip = onSkipTutorial
                    )
                }

                HudStatusBar(
                    uiState = uiState,
                    onPulseClick = onPulseClick
                )

                FeedbackBanner(feedback = uiState.feedback)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BoardGrid(
                        grid = uiState.grid,
                        spotlightPositions = uiState.spotlightPositions,
                        hintPositions = uiState.hintPositions,
                        isPulseArmed = uiState.isPulseArmed,
                        hasSpotlight = uiState.spotlightPositions.isNotEmpty(),
                        onTileTap = onTileTap
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.isRoundWon || uiState.isGameOver,
                enter = fadeIn(tween(220)) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                exit = fadeOut(tween(150))
            ) {
                RoundEndOverlay(
                    uiState = uiState,
                    onContinueBeginner = onContinueBeginner,
                    onGoToStandard = onGoToStandard,
                    onPlayAgain = onRestart,
                    onBackHome = onNavigateHome
                )
            }
        }
    }
}

@Composable
private fun GameTopBar(
    uiState: GameUiState,
    onBack: () -> Unit,
    onHint: () -> Unit,
    onOpenRules: () -> Unit,
    onRestart: () -> Unit
) {
    val showHintButton = uiState.mode == GameMode.BEGINNER_PRACTICE ||
        (uiState.mode == GameMode.BEGINNER_TUTORIAL &&
            uiState.tutorialStep == TutorialStep.STEP_4_GUIDED_PRACTICE)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("game_back_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Text(
                    text = when (uiState.mode) {
                        GameMode.BEGINNER_TUTORIAL -> stringResource(R.string.mode_tutorial_label)
                        GameMode.BEGINNER_PRACTICE -> stringResource(R.string.mode_beginner_label)
                        GameMode.STANDARD -> stringResource(R.string.mode_standard_label)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showHintButton) {
                OutlinedButton(
                    onClick = onHint,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("game_hint_button"),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = stringResource(R.string.cd_hint),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.btn_hint),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                FilledTonalIconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("game_restart_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.btn_restart)
                    )
                }
            }

            FilledTonalIconButton(
                onClick = onOpenRules,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("game_rules_button"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                    contentDescription = stringResource(R.string.cd_rules),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun TutorialCoachCard(
    step: TutorialStep,
    isPulseArmed: Boolean,
    onSkip: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.tutorial_progress_format, step.stepIndex, 4),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("skip_tutorial_button")
                ) {
                    Text(
                        text = stringResource(R.string.btn_skip_tutorial),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 4-Step Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (index in 1..4) {
                    val active = index <= step.stepIndex
                    val barColor by animateColorAsState(
                        targetValue = if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        },
                        label = "stepBarColor"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(barColor)
                    )
                }
            }

            AnimatedContent(
                targetState = step to isPulseArmed,
                label = "tutorialInstructionTransition"
            ) { (currentStep, armed) ->
                val titleRes = when (currentStep) {
                    TutorialStep.STEP_1_CONNECT_PAIR -> R.string.tut_step1_title
                    TutorialStep.STEP_2_COMBO_ENERGY -> R.string.tut_step2_title
                    TutorialStep.STEP_3_PULSE_POWER -> R.string.tut_step3_title
                    TutorialStep.STEP_4_GUIDED_PRACTICE -> R.string.tut_step4_title
                    TutorialStep.COMPLETED -> R.string.complete_tutorial_title
                }
                val bodyRes = when (currentStep) {
                    TutorialStep.STEP_1_CONNECT_PAIR -> R.string.tut_step1_desc
                    TutorialStep.STEP_2_COMBO_ENERGY -> R.string.tut_step2_desc
                    TutorialStep.STEP_3_PULSE_POWER ->
                        if (armed) R.string.tut_step3_armed_desc else R.string.tut_step3_desc
                    TutorialStep.STEP_4_GUIDED_PRACTICE -> R.string.tut_step4_desc
                    TutorialStep.COMPLETED -> R.string.complete_tutorial_body
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(bodyRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HudStatusBar(
    uiState: GameUiState,
    onPulseClick: () -> Unit
) {
    val palette = LocalPulsoPalette.current
    val animatedScore by animateIntAsState(
        targetValue = uiState.score,
        animationSpec = tween(durationMillis = 280),
        label = "scoreCounter"
    )
    val isPulseReady = uiState.energy >= uiState.maxEnergy
    val infiniteTransition = rememberInfiniteTransition(label = "pulseBtnAnim")
    val pulseBtnScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState.highlightPulseButton || uiState.isPulseArmed) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Goal
                Column {
                    Text(
                        text = stringResource(R.string.hud_score),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = animatedScore.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.hud_goal_format, uiState.goalScore),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.successAccent,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                // Moves / Unlimited Pill
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (uiState.movesLeft == null) {
                            Icon(
                                imageVector = Icons.Rounded.TimerOff,
                                contentDescription = null,
                                tint = palette.successAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.hud_unlimited_moves),
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.successAccent
                            )
                        } else {
                            Text(
                                text = stringResource(
                                    R.string.hud_moves_left_format,
                                    uiState.movesLeft
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (uiState.movesLeft <= 3) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }

            // Energy Gauge + Pulse Power Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.hud_energy_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (slot in 1..uiState.maxEnergy) {
                            val filled = slot <= uiState.energy
                            val segmentColor by animateColorAsState(
                                targetValue = if (filled) {
                                    palette.energyAccent
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                label = "energySlotColor"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(segmentColor)
                            )
                        }
                    }
                }

                Button(
                    onClick = onPulseClick,
                    enabled = isPulseReady,
                    modifier = Modifier
                        .height(48.dp)
                        .scale(pulseBtnScale)
                        .testTag("pulse_power_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isPulseArmed) {
                            palette.chama.fill
                        } else {
                            palette.energyAccent
                        },
                        contentColor = Color(0xFF1F1300),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            uiState.isPulseArmed -> stringResource(R.string.btn_pulse_armed)
                            isPulseReady -> stringResource(R.string.btn_pulse_ready)
                            else -> stringResource(
                                R.string.btn_pulse_charging,
                                uiState.energy
                            )
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackBanner(feedback: FeedbackEvent?) {
    val palette = LocalPulsoPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        if (feedback != null) {
            val (text, tint) = when (feedback) {
                is FeedbackEvent.Points ->
                    stringResource(
                        R.string.feedback_points_format,
                        feedback.points,
                        feedback.count
                    ) to MaterialTheme.colorScheme.primary

                is FeedbackEvent.ComboEnergy ->
                    stringResource(
                        R.string.feedback_combo_energy_format,
                        feedback.points
                    ) to palette.energyAccent

                is FeedbackEvent.PulseCleared ->
                    stringResource(
                        R.string.feedback_pulse_cleared_format,
                        feedback.count,
                        feedback.points
                    ) to palette.chama.fill

                FeedbackEvent.SingleTileWarning ->
                    stringResource(R.string.feedback_single_tile_warning) to
                        MaterialTheme.colorScheme.error

                FeedbackEvent.FollowSpotlight ->
                    stringResource(R.string.feedback_follow_spotlight) to
                        MaterialTheme.colorScheme.primary

                FeedbackEvent.PulsePrompt ->
                    stringResource(R.string.feedback_pulse_prompt) to palette.chama.fill

                FeedbackEvent.Reshuffled ->
                    stringResource(R.string.feedback_reshuffled) to palette.successAccent

                FeedbackEvent.HintShown ->
                    stringResource(R.string.feedback_hint_shown) to palette.energyAccent
            }

            Surface(
                color = tint.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, tint.copy(alpha = 0.45f))
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = tint,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BoardGrid(
    grid: List<List<GameTile>>,
    spotlightPositions: Set<BoardPos>,
    hintPositions: Set<BoardPos>,
    isPulseArmed: Boolean,
    hasSpotlight: Boolean,
    onTileTap: (BoardPos) -> Unit
) {
    if (grid.isEmpty()) return
    val rows = grid.size
    val cols = grid[0].size

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val boardSide = minOf(maxWidth, maxHeight, 440.dp)
        val spacing = if (cols <= 3) 12.dp else 9.dp

        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                width = if (isPulseArmed) 2.dp else 1.dp,
                color = if (isPulseArmed) {
                    LocalPulsoPalette.current.chama.fill
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                }
            ),
            modifier = Modifier.size(boardSide)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        for (c in 0 until cols) {
                            val pos = BoardPos(r, c)
                            val tile = grid[r][c]
                            val isSpotlight = pos in spotlightPositions
                            val isHint = pos in hintPositions
                            val dimmed = hasSpotlight && !isSpotlight

                            GameTileButton(
                                tile = tile,
                                pos = pos,
                                isSpotlight = isSpotlight,
                                isHint = isHint,
                                dimmed = dimmed,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                onClick = { onTileTap(pos) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameTileButton(
    tile: GameTile,
    pos: BoardPos,
    isSpotlight: Boolean,
    isHint: Boolean,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalPulsoPalette.current
    val colors: TileColorPair = when (tile.kind) {
        TileKind.SOLAR -> palette.solar
        TileKind.ONDA -> palette.onda
        TileKind.CHAMA -> palette.chama
        TileKind.FOLHA -> palette.folha
    }
    val icon: ImageVector = when (tile.kind) {
        TileKind.SOLAR -> Icons.Rounded.WbSunny
        TileKind.ONDA -> Icons.Rounded.Waves
        TileKind.CHAMA -> Icons.Rounded.LocalFireDepartment
        TileKind.FOLHA -> Icons.Rounded.Eco
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hintTileAnim_${tile.id}")
    val hintScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHint || isSpotlight) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintPulse"
    )
    val tileAlpha by animateFloatAsState(
        targetValue = if (dimmed) 0.28f else 1f,
        animationSpec = tween(220),
        label = "tileDimAlpha"
    )

    val tileShape = RoundedCornerShape(18.dp)
    val tileDescription = stringResource(tile.kind.labelRes)

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .scale(hintScale)
            .alpha(tileAlpha)
            .clip(tileShape)
            .background(colors.lip)
            .then(
                if (isSpotlight || isHint) {
                    Modifier.border(
                        width = 2.5.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = tileShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = tileDescription }
            .testTag("tile_${pos.row}_${pos.col}")
    ) {
        // Raised top face for tactile arcade button depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 5.dp)
                .clip(tileShape)
                .background(colors.fill),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }
}

@Composable
private fun RoundEndOverlay(
    uiState: GameUiState,
    onContinueBeginner: () -> Unit,
    onGoToStandard: () -> Unit,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    val palette = LocalPulsoPalette.current
    val isTutorialEnd = uiState.mode == GameMode.BEGINNER_TUTORIAL && uiState.isRoundWon

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.82f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.isRoundWon) {
                                palette.successAccent.copy(alpha = 0.2f)
                            } else {
                                palette.chama.fill.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isRoundWon) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.Bolt
                        },
                        contentDescription = null,
                        tint = if (uiState.isRoundWon) palette.successAccent else palette.chama.fill,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = when {
                        isTutorialEnd -> stringResource(R.string.complete_tutorial_title)
                        uiState.isRoundWon -> stringResource(R.string.complete_round_title)
                        else -> stringResource(R.string.gameover_title)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when {
                        isTutorialEnd -> stringResource(R.string.complete_tutorial_body)
                        uiState.isRoundWon -> stringResource(R.string.complete_round_body, uiState.score)
                        else -> stringResource(R.string.gameover_body, uiState.score)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isTutorialEnd || uiState.mode == GameMode.BEGINNER_PRACTICE) {
                    Button(
                        onClick = onContinueBeginner,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("overlay_continue_beginner_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isTutorialEnd) {
                                stringResource(R.string.btn_continue_beginner)
                            } else {
                                stringResource(R.string.btn_play_again)
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = onGoToStandard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("overlay_go_standard_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_try_standard),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("overlay_play_again_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_play_again),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                TextButton(
                    onClick = onBackHome,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("overlay_back_home_button")
                ) {
                    Text(
                        text = stringResource(R.string.btn_back_home),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
