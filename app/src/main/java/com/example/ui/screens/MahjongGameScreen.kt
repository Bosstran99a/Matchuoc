package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.BoardTile
import com.example.model.GameStatus
import com.example.model.GameUiState
import com.example.model.TrayTile
import com.example.ui.components.LevelsBottomSheet
import com.example.ui.components.MahjongTileCard
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.TrayFullDialog
import com.example.ui.components.TraySparkleOverlay
import com.example.ui.components.VictoryCelebrationOverlay
import com.example.ui.components.VictoryDialog
import com.example.ui.components.formatDuration
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.ActionOrangeDark
import com.example.ui.theme.EmeraldFeltBottom
import com.example.ui.theme.EmeraldFeltMid
import com.example.ui.theme.EmeraldFeltTop
import com.example.ui.theme.EmeraldSpotlight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.PowerUpBronze
import com.example.ui.theme.PowerUpBronzeBorder
import com.example.ui.theme.SurfaceCardJade
import com.example.ui.theme.TextMutedOnTable
import com.example.ui.theme.TextOnTable
import com.example.ui.theme.TrayBackground
import com.example.ui.theme.TrayBorderDanger
import com.example.ui.theme.TrayBorderNeon
import com.example.ui.theme.TraySlotInner
import com.example.viewmodel.MahjongViewModel
import kotlin.math.roundToInt

@Composable
fun MahjongGameScreen(
    viewModel: MahjongViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var showLevelsSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Haptic trigger on combo match
    LaunchedEffect(state.sparkleBursts.size) {
        if (state.sparkleBursts.isNotEmpty() && state.hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val isVictoryStage = state.status == GameStatus.CELEBRATING || state.status == GameStatus.WON
    val cameraZoom by animateFloatAsState(
        targetValue = if (isVictoryStage) 0.85f else 1f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "victoryCameraZoom",
    )
    val victoryGlowAlpha by animateFloatAsState(
        targetValue = if (isVictoryStage) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "victoryGlowAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Rich emerald felt table vertical gradient + center radial spotlight
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(EmeraldFeltTop, EmeraldFeltMid, EmeraldFeltBottom),
                    ),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldSpotlight.copy(alpha = 0.42f + victoryGlowAlpha * 0.22f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.46f),
                        radius = size.maxDimension * (0.48f + victoryGlowAlpha * 0.14f),
                    ),
                )
                if (victoryGlowAlpha > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldAccent.copy(alpha = 0.18f * victoryGlowAlpha),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.48f),
                            radius = size.maxDimension * 0.55f,
                        ),
                    )
                }
            }
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(cameraZoom)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. Compact HUD Bar (Level, Score, Timer)
            TopHudBar(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Three Top Power-up Buttons (Undo, Hint, Shuffle) matching the video
            PowerUpBar(
                canUndo = state.canUndo,
                undoCharges = state.undoCharges,
                hintCharges = state.hintCharges,
                shuffleCharges = state.shuffleCharges,
                onUndo = {
                    if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onUndoClicked()
                },
                onHint = {
                    if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onHintClicked()
                },
                onShuffle = {
                    if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onShuffleClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Center 3D Stacked Mahjong Tile Board
            MahjongBoardArea(
                state = state,
                onTileTap = { tileId ->
                    if (state.hapticEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    viewModel.onTileClicked(tileId)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Bottom 5-Slot Glowing Green Tray + Orange Corner Controls
            BottomControlDock(
                state = state,
                onOpenLevels = { showLevelsSheet = true },
                onOpenSettings = { showSettingsSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp),
            )
        }

        // Full-screen celebratory confetti & zoom-out spotlight overlay
        if (isVictoryStage) {
            VictoryCelebrationOverlay(
                levelNumber = state.currentLevel.levelNumber,
                timeBonus = state.timeBonus,
                showBanner = state.status == GameStatus.CELEBRATING,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("victory_celebration_overlay"),
            )
        }

        // Win / Loss Modals
        if (state.status == GameStatus.WON) {
            VictoryDialog(
                levelNumber = state.currentLevel.levelNumber,
                levelName = state.currentLevel.nameVi,
                score = state.score,
                bestScore = state.bestScore,
                elapsedSeconds = state.elapsedSeconds,
                earnedStars = state.earnedStars,
                onNextLevel = { viewModel.startNextLevel() },
                onReplay = { viewModel.restartCurrentLevel() },
            )
        } else if (state.status == GameStatus.LOST) {
            TrayFullDialog(
                canUndo = state.undoCharges > 0 && state.trayTiles.isNotEmpty(),
                undoCharges = state.undoCharges,
                onUndo = { viewModel.onUndoClicked() },
                onRetry = { viewModel.restartCurrentLevel() },
            )
        }
    }

    if (showLevelsSheet) {
        LevelsBottomSheet(
            state = state,
            onSelectLevel = { level -> viewModel.startLevel(level) },
            onDismiss = { showLevelsSheet = false },
        )
    }

    if (showSettingsSheet) {
        SettingsBottomSheet(
            soundEnabled = state.soundEnabled,
            hapticEnabled = state.hapticEnabled,
            onToggleSound = { viewModel.toggleSound() },
            onToggleHaptic = { viewModel.toggleHaptic() },
            onRestartLevel = { viewModel.restartCurrentLevel() },
            onDismiss = { showSettingsSheet = false },
        )
    }
}

@Composable
private fun TopHudBar(
    state: GameUiState,
    modifier: Modifier = Modifier,
) {
    val activeCount = state.boardTiles.count { !it.isRemoved }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.24f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Màn ${state.currentLevel.levelNumber} • ${state.currentLevel.nameVi}",
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent,
            )
            Text(
                text = stringResource(R.string.tiles_remaining, activeCount),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedOnTable,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(visible = state.comboCount >= 2) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ActionOrange)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "x${state.comboCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${state.score} đ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextOnTable,
                )
                Text(
                    text = formatDuration(state.elapsedSeconds),
                    style = MaterialTheme.typography.labelMedium,
                    color = TrayBorderNeon,
                )
            }
        }
    }
}

@Composable
private fun PowerUpBar(
    canUndo: Boolean,
    undoCharges: Int,
    hintCharges: Int,
    shuffleCharges: Int,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PowerUpButton(
            icon = Icons.AutoMirrored.Rounded.Undo,
            label = stringResource(R.string.action_undo),
            count = undoCharges,
            enabled = canUndo,
            testTag = "undo_button",
            onClick = onUndo,
        )
        PowerUpButton(
            icon = Icons.Rounded.Lightbulb,
            label = stringResource(R.string.action_hint),
            count = hintCharges,
            enabled = hintCharges > 0,
            testTag = "hint_button",
            onClick = onHint,
        )
        PowerUpButton(
            icon = Icons.Rounded.Shuffle,
            label = stringResource(R.string.action_shuffle),
            count = shuffleCharges,
            enabled = shuffleCharges > 0,
            testTag = "shuffle_button",
            onClick = onShuffle,
        )
    }
}

@Composable
private fun PowerUpButton(
    icon: ImageVector,
    label: String,
    count: Int,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.45f)
            .clickable(enabled = enabled, onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag),
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF54321C), PowerUpBronze),
                        ),
                    )
                    .border(2.dp, PowerUpBronzeBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = GoldAccent,
                    modifier = Modifier.size(25.dp),
                )
            }

            // Count badge
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = (-2).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(ActionOrange)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextOnTable,
        )
    }
}

@Composable
private fun MahjongBoardArea(
    state: GameUiState,
    onTileTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeTiles = remember(state.boardTiles) {
        state.boardTiles
            .filter { !it.isRemoved }
            .sortedWith(compareBy<BoardTile>({ it.layer }, { it.gridY }, { it.gridX }))
    }

    val shuffleScale = remember { Animatable(1f) }
    LaunchedEffect(state.shuffleTick) {
        if (state.shuffleTick > 0) {
            shuffleScale.snapTo(0.92f)
            shuffleScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier.scale(shuffleScale.value),
        contentAlignment = Alignment.Center,
    ) {
        if (activeTiles.isEmpty()) return@BoxWithConstraints

        val density = LocalDensity.current
        // Use initial full board bounds so remaining tiles don't jump around as pieces are cleared!
        val allTiles = state.boardTiles
        val minX = allTiles.minOfOrNull { it.gridX } ?: 0f
        val maxX = allTiles.maxOfOrNull { it.gridX } ?: 0f
        val minY = allTiles.minOfOrNull { it.gridY } ?: 0f
        val maxY = allTiles.maxOfOrNull { it.gridY } ?: 0f
        val maxLayer = allTiles.maxOfOrNull { it.layer } ?: 0

        val colsSpan = (maxX - minX) + 1.0f
        val rowsSpan = (maxY - minY) + 1.0f

        val availWidthPx = with(density) { (maxWidth - 16.dp).toPx() }
        val availHeightPx = with(density) { (maxHeight - 16.dp).toPx() }

        // Standard Mahjong tile aspect ratio: width : height = 1 : 1.28
        val aspectRatio = 1.28f
        val layerShiftXPx = with(density) { 2.4.dp.toPx() }
        val layerShiftYPx = with(density) { 3.6.dp.toPx() }

        val maxTileWFromWidth = (availWidthPx - maxLayer * layerShiftXPx) / colsSpan
        val maxTileWFromHeight = ((availHeightPx - maxLayer * layerShiftYPx) / rowsSpan) / aspectRatio
        val tileWidthPx = minOf(maxTileWFromWidth, maxTileWFromHeight)
            .coerceIn(with(density) { 36.dp.toPx() }, with(density) { 68.dp.toPx() })
        val tileHeightPx = tileWidthPx * aspectRatio

        val boardWidthPx = colsSpan * tileWidthPx
        val boardHeightPx = rowsSpan * tileHeightPx
        val originXPx = (constraints.maxWidth - boardWidthPx) / 2f
        val originYPx = (constraints.maxHeight - boardHeightPx) / 2f + (maxLayer * layerShiftYPx * 0.35f)

        val tileWidthDp = with(density) { tileWidthPx.toDp() }
        val tileHeightDp = with(density) { tileHeightPx.toDp() }

        Box(modifier = Modifier.fillMaxSize()) {
            activeTiles.forEach { tile ->
                key(tile.id) {
                    val isBlocked = tile.id in state.blockedTileIds
                    val isHighlighted = tile.id == state.highlightedHintTileId
                    val isShaking = tile.id == state.invalidShakeTileId

                    val shakeX = remember { Animatable(0f) }
                    LaunchedEffect(isShaking) {
                        if (isShaking) {
                            val offsets = listOf(-8f, 8f, -6f, 6f, 0f)
                            offsets.forEach { target ->
                                shakeX.animateTo(target, tween(45))
                            }
                        }
                    }

                    val xPx = originXPx + (tile.gridX - minX) * tileWidthPx - tile.layer * layerShiftXPx + shakeX.value
                    val yPx = originYPx + (tile.gridY - minY) * tileHeightPx - tile.layer * layerShiftYPx

                    MahjongTileCard(
                        type = tile.type,
                        isBlocked = isBlocked,
                        isHighlighted = isHighlighted,
                        depthOffsetDp = 4.dp,
                        onClick = { onTileTap(tile.id) },
                        modifier = Modifier
                            .offset { IntOffset(xPx.roundToInt(), yPx.roundToInt()) }
                            .size(width = tileWidthDp, height = tileHeightDp)
                            .testTag("board_tile_${tile.id}"),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomControlDock(
    state: GameUiState,
    onOpenLevels: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDanger = state.trayTiles.size >= state.trayCapacity - 1
    val trayGlowColor = if (isDanger) TrayBorderDanger else TrayBorderNeon

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Tray + Particle Burst Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Glowing 5-slot Emerald Tray (just like the video!)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .widthIn(max = 360.dp)
                    .height(74.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = trayGlowColor,
                        spotColor = trayGlowColor,
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(TrayBackground)
                    .border(2.5.dp, trayGlowColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("tray_container"),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (slotIndex in 0 until state.trayCapacity) {
                        val trayTile: TrayTile? = state.trayTiles.getOrNull(slotIndex)
                        val isMatching = trayTile != null && trayTile.type == state.matchingClearType

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(TraySlotInner.copy(alpha = 0.72f))
                                .border(
                                    1.dp,
                                    trayGlowColor.copy(alpha = 0.25f),
                                    RoundedCornerShape(10.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            AnimatedContent(
                                targetState = trayTile,
                                transitionSpec = {
                                    (scaleIn(initialScale = 0.65f, animationSpec = tween(160)) + fadeIn(tween(140)))
                                        .togetherWith(scaleOut(targetScale = 1.22f, animationSpec = tween(150)) + fadeOut(tween(120)))
                                },
                                label = "traySlot_$slotIndex",
                            ) { item ->
                                if (item != null) {
                                    val matchScale by animateFloatAsState(
                                        targetValue = if (isMatching) 1.12f else 1f,
                                        animationSpec = tween(140),
                                        label = "matchScale",
                                    )
                                    MahjongTileCard(
                                        type = item.type,
                                        isBlocked = false,
                                        isHighlighted = isMatching,
                                        depthOffsetDp = 2.5.dp,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp)
                                            .scale(matchScale)
                                            .testTag("tray_tile_$slotIndex"),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sparkle burst & floating combo text right over the tray
            TraySparkleOverlay(
                bursts = state.sparkleBursts,
                popups = state.popups,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .widthIn(max = 360.dp)
                    .height(100.dp),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Left & Right Orange Circular Buttons (matching the video!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CornerOrangeButton(
                icon = Icons.Rounded.Menu,
                contentDescription = stringResource(R.string.action_levels),
                testTag = "levels_menu_button",
                onClick = onOpenLevels,
            )

            Text(
                text = if (isDanger) "Sắp đầy khay!" else "Ghép 3 quân giống nhau",
                style = MaterialTheme.typography.labelLarge,
                color = if (isDanger) TrayBorderDanger else TextMutedOnTable,
            )

            CornerOrangeButton(
                icon = Icons.Rounded.Settings,
                contentDescription = stringResource(R.string.action_settings),
                testTag = "settings_button",
                onClick = onOpenSettings,
            )
        }
    }
}

@Composable
private fun CornerOrangeButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = ActionOrange,
        shadowElevation = 6.dp,
        modifier = Modifier
            .size(50.dp)
            .minimumInteractiveComponentSize()
            .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
            .testTag(testTag),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(ActionOrange, ActionOrangeDark)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}
