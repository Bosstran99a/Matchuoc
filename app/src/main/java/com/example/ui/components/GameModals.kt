package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import com.example.R
import com.example.game.LevelGenerator
import com.example.model.GameUiState
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.EmeraldFeltBottom
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.SurfaceCardJade
import com.example.ui.theme.SurfaceDarkJade
import com.example.ui.theme.TextMutedOnTable
import com.example.ui.theme.TextOnTable
import com.example.ui.theme.TrayBorderNeon

@Composable
fun VictoryDialog(
    levelNumber: Int,
    levelName: String,
    score: Int,
    bestScore: Int,
    elapsedSeconds: Int,
    earnedStars: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .border(2.dp, GoldAccent.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDarkJade,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (starIndex in 1..3) {
                        val starScale = remember(levelNumber, starIndex) { Animatable(0.2f) }
                        LaunchedEffect(levelNumber, starIndex) {
                            delay((starIndex - 1) * 140L)
                            starScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                            )
                        }
                        Icon(
                            imageVector = if (starIndex <= earnedStars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = null,
                            tint = if (starIndex <= earnedStars) GoldAccent else TextMutedOnTable.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(if (starIndex == 2) 48.dp else 36.dp)
                                .scale(starScale.value),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.win_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldAccent,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Màn $levelNumber • $levelName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMutedOnTable,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCardJade)
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatColumn(label = stringResource(R.string.score_label), value = "$score")
                    StatColumn(label = "Thời gian", value = formatDuration(elapsedSeconds))
                    StatColumn(label = stringResource(R.string.best_score_label), value = "$bestScore")
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_level_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionOrange,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.action_next_level),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onReplay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("replay_win_button"),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = TextOnTable)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_restart),
                        color = TextOnTable,
                    )
                }
            }
        }
    }
}

@Composable
fun TrayFullDialog(
    canUndo: Boolean,
    undoCharges: Int,
    onUndo: () -> Unit,
    onRetry: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .border(2.dp, ActionOrange.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDarkJade,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.lose_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ActionOrange,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.lose_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMutedOnTable,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (canUndo) {
                    Button(
                        onClick = onUndo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("lose_undo_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = InkCharcoal,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stringResource(R.string.action_undo)} ($undoCharges)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("lose_retry_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionOrange,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_restart),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsBottomSheet(
    state: GameUiState,
    onSelectLevel: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val levelsToShow = (1..maxOf(20, state.highestUnlockedLevel + 2)).map {
        LevelGenerator.getLevelDefinition(it)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDarkJade,
        contentColor = TextOnTable,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.action_levels),
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldAccent,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCardJade)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "${stringResource(R.string.best_score_label)}: ${state.bestScore}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TrayBorderNeon,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Brief How-to-play card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(listOf(SurfaceCardJade, EmeraldFeltBottom)),
                    )
                    .border(1.dp, TrayBorderNeon.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.how_to_play_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = TrayBorderNeon,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.how_to_play_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMutedOnTable,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
            ) {
                items(levelsToShow, key = { it.levelNumber }) { lvl ->
                    val isUnlocked = lvl.levelNumber <= state.highestUnlockedLevel
                    val isCurrent = lvl.levelNumber == state.currentLevel.levelNumber
                    val stars = state.starsByLevel[lvl.levelNumber] ?: 0
                    val isCleared = stars > 0
                    val tileCount = lvl.totalTrios * 3

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    isCurrent -> ActionOrange.copy(alpha = 0.24f)
                                    isUnlocked && isCleared -> SurfaceCardJade
                                    isUnlocked -> SurfaceCardJade.copy(alpha = 0.85f)
                                    else -> SurfaceCardJade.copy(alpha = 0.42f)
                                },
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = when {
                                    isCurrent -> ActionOrange
                                    isUnlocked && isCleared -> TrayBorderNeon.copy(alpha = 0.45f)
                                    isUnlocked -> GoldAccent.copy(alpha = 0.25f)
                                    else -> Color.White.copy(alpha = 0.08f)
                                },
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable(enabled = isUnlocked) {
                                onSelectLevel(lvl.levelNumber)
                                onDismiss()
                            }
                            .padding(10.dp)
                            .testTag("level_item_${lvl.levelNumber}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (isUnlocked) {
                            Text(
                                text = "Màn ${lvl.levelNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isCurrent) ActionOrange else TextOnTable,
                            )
                            Text(
                                text = lvl.nameVi,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedOnTable,
                                maxLines = 1,
                            )
                            Text(
                                text = "$tileCount quân",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent.copy(alpha = 0.85f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (s in 1..3) {
                                    Icon(
                                        imageVector = if (s <= stars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                        contentDescription = null,
                                        tint = if (s <= stars) GoldAccent else TextMutedOnTable.copy(alpha = 0.35f),
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Chưa mở khóa",
                                tint = TextMutedOnTable.copy(alpha = 0.45f),
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Màn ${lvl.levelNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextMutedOnTable.copy(alpha = 0.55f),
                            )
                            Text(
                                text = "$tileCount quân",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedOnTable.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    onRestartLevel: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDarkJade,
        contentColor = TextOnTable,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.action_settings),
                style = MaterialTheme.typography.headlineMedium,
                color = GoldAccent,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCardJade)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = null,
                        tint = TrayBorderNeon,
                    )
                    Text(
                        text = stringResource(R.string.sound_effect_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnTable,
                    )
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { onToggleSound() },
                    colors = SwitchDefaults.colors(checkedThumbColor = InkCharcoal, checkedTrackColor = TrayBorderNeon),
                    modifier = Modifier.testTag("sound_toggle_switch"),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCardJade)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Vibration,
                        contentDescription = null,
                        tint = TrayBorderNeon,
                    )
                    Text(
                        text = stringResource(R.string.haptic_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnTable,
                    )
                }
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = { onToggleHaptic() },
                    colors = SwitchDefaults.colors(checkedThumbColor = InkCharcoal, checkedTrackColor = TrayBorderNeon),
                    modifier = Modifier.testTag("haptic_toggle_switch"),
                )
            }

            Button(
                onClick = {
                    onRestartLevel()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("settings_restart_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActionOrange,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_restart),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMutedOnTable,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = TextOnTable,
        )
    }
}

fun formatDuration(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(mins, secs)
}
