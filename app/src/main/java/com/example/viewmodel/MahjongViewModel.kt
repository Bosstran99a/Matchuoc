package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MahjongSoundEngine
import com.example.data.GamePreferencesRepository
import com.example.game.LevelGenerator
import com.example.model.BoardTile
import com.example.model.FloatingPopup
import com.example.model.GameStatus
import com.example.model.GameUiState
import com.example.model.SparkleBurst
import com.example.model.TileType
import com.example.model.TrayTile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MahjongViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = GamePreferencesRepository(application.applicationContext)
    private val soundEngine = MahjongSoundEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<Int>() // boardTileIds currently in tray in pick order
    private var timerJob: Job? = null
    private var celebrationJob: Job? = null
    private var lastMatchTimeMs: Long = 0L
    private var nextEventId: Long = 1L
    private var isResolvingMatch: Boolean = false

    init {
        viewModelScope.launch {
            prefsRepo.preferencesFlow.collect { prefs ->
                soundEngine.enabled = prefs.soundEnabled
                _uiState.update { state ->
                    state.copy(
                        bestScore = maxOf(state.bestScore, prefs.bestScore),
                        highestUnlockedLevel = maxOf(state.highestUnlockedLevel, prefs.highestUnlockedLevel),
                        starsByLevel = prefs.starsByLevel,
                        soundEnabled = prefs.soundEnabled,
                        hapticEnabled = prefs.hapticEnabled,
                    )
                }
            }
        }
        startLevel(1)
    }

    fun startLevel(levelNumber: Int) {
        if (levelNumber < 1) return
        if (levelNumber > 1 && levelNumber > _uiState.value.highestUnlockedLevel) return
        timerJob?.cancel()
        celebrationJob?.cancel()
        undoStack.clear()
        isResolvingMatch = false
        lastMatchTimeMs = 0L

        val levelDef = LevelGenerator.getLevelDefinition(levelNumber)
        val board = LevelGenerator.generateBoard(levelDef)
        val blocked = LevelGenerator.computeBlockedIds(board)

        _uiState.update { current ->
            current.copy(
                currentLevel = levelDef,
                boardTiles = board,
                blockedTileIds = blocked,
                trayTiles = emptyList(),
                score = 0,
                timeBonus = 0,
                comboCount = 0,
                elapsedSeconds = 0,
                undoCharges = 3,
                hintCharges = 2,
                shuffleCharges = 2,
                canUndo = false,
                highlightedHintTileId = null,
                invalidShakeTileId = null,
                matchingClearType = null,
                popups = emptyList(),
                sparkleBursts = emptyList(),
                status = GameStatus.PLAYING,
                earnedStars = 0,
            )
        }
        startTimer()
    }

    fun restartCurrentLevel() {
        startLevel(_uiState.value.currentLevel.levelNumber)
    }

    fun startNextLevel() {
        startLevel(_uiState.value.currentLevel.levelNumber + 1)
    }

    fun onTileClicked(tileId: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || isResolvingMatch) return

        val tile = state.boardTiles.find { it.id == tileId && !it.isRemoved } ?: return
        if (tileId in state.blockedTileIds) {
            soundEngine.playBlockedTap()
            _uiState.update { it.copy(invalidShakeTileId = tileId) }
            viewModelScope.launch {
                delay(280)
                _uiState.update { current ->
                    if (current.invalidShakeTileId == tileId) current.copy(invalidShakeTileId = null) else current
                }
            }
            return
        }

        if (state.trayTiles.size >= state.trayCapacity) return

        pickTileIntoTray(tile)
    }

    private fun pickTileIntoTray(tile: BoardTile) {
        soundEngine.playTileTap()
        undoStack.add(tile.id)

        val updatedBoard = _uiState.value.boardTiles.map {
            if (it.id == tile.id) it.copy(isRemoved = true) else it
        }
        val updatedBlocked = LevelGenerator.computeBlockedIds(updatedBoard)

        val currentTray = _uiState.value.trayTiles.toMutableList()
        val lastSameIdx = currentTray.indexOfLast { it.type == tile.type }
        val newEntry = TrayTile(boardTileId = tile.id, type = tile.type)
        if (lastSameIdx >= 0) {
            currentTray.add(lastSameIdx + 1, newEntry)
        } else {
            currentTray.add(newEntry)
        }

        _uiState.update { state ->
            state.copy(
                boardTiles = updatedBoard,
                blockedTileIds = updatedBlocked,
                trayTiles = currentTray,
                canUndo = undoStack.isNotEmpty() && state.undoCharges > 0,
                highlightedHintTileId = null,
            )
        }

        val matchingCount = currentTray.count { it.type == tile.type }
        if (matchingCount >= 3) {
            resolveTripleMatch(tile.type)
        } else if (currentTray.size >= _uiState.value.trayCapacity) {
            triggerGameLost()
        }
    }

    private fun resolveTripleMatch(matchedType: TileType) {
        isResolvingMatch = true
        _uiState.update { it.copy(matchingClearType = matchedType) }

        viewModelScope.launch {
            delay(190L)

            val currentState = _uiState.value
            val tray = currentState.trayTiles
            val firstIdx = tray.indexOfFirst { it.type == matchedType }.coerceAtLeast(0)
            val removedIds = tray.filter { it.type == matchedType }.take(3).map { it.boardTileId }.toSet()
            undoStack.removeAll(removedIds)

            val remainingTray = tray.filterNot { it.boardTileId in removedIds }

            val now = System.currentTimeMillis()
            val newCombo = if (now - lastMatchTimeMs <= 5500L) currentState.comboCount + 1 else 1
            lastMatchTimeMs = now

            val pointsEarned = 100 + (newCombo - 1) * 50
            val newScore = currentState.score + pointsEarned

            soundEngine.playTripleMatch(newCombo)

            val centerSlot = (firstIdx + 1f).coerceIn(0.5f, currentState.trayCapacity - 0.5f)
            val centerFraction = (centerSlot + 0.5f) / currentState.trayCapacity.toFloat()

            val burstId = nextEventId++
            val popupId = nextEventId++
            val popupText = if (newCombo >= 2) "+$pointsEarned • x$newCombo!" else "+$pointsEarned"

            val newBurst = SparkleBurst(
                id = burstId,
                slotCenterFraction = centerFraction.coerceIn(0.15f, 0.85f),
                seed = burstId.toInt(),
            )
            val newPopup = FloatingPopup(
                id = popupId,
                text = popupText,
                isCombo = newCombo >= 2,
            )

            val allCleared = currentState.boardTiles.all { it.isRemoved } && remainingTray.isEmpty()

            _uiState.update { state ->
                state.copy(
                    trayTiles = remainingTray,
                    score = newScore,
                    bestScore = maxOf(state.bestScore, newScore),
                    comboCount = newCombo,
                    matchingClearType = null,
                    canUndo = undoStack.isNotEmpty() && state.undoCharges > 0,
                    sparkleBursts = state.sparkleBursts + newBurst,
                    popups = state.popups + newPopup,
                )
            }

            isResolvingMatch = false

            if (allCleared) {
                triggerGameWon()
            }

            // Clean up transient burst & popup after animation completes
            launch {
                delay(900L)
                _uiState.update { state ->
                    state.copy(
                        sparkleBursts = state.sparkleBursts.filterNot { it.id == burstId },
                        popups = state.popups.filterNot { it.id == popupId },
                    )
                }
            }
        }
    }

    fun onUndoClicked() {
        val state = _uiState.value
        if (isResolvingMatch || state.undoCharges <= 0 || undoStack.isEmpty()) return
        if (state.status == GameStatus.WON || state.status == GameStatus.CELEBRATING) return

        val lastTileId = undoStack.removeAt(undoStack.lastIndex)
        val restoredBoard = state.boardTiles.map {
            if (it.id == lastTileId) it.copy(isRemoved = false) else it
        }
        val restoredBlocked = LevelGenerator.computeBlockedIds(restoredBoard)
        val restoredTray = state.trayTiles.filterNot { it.boardTileId == lastTileId }
        val newCharges = state.undoCharges - 1

        soundEngine.playUndo()

        val wasLost = state.status == GameStatus.LOST
        _uiState.update { current ->
            current.copy(
                boardTiles = restoredBoard,
                blockedTileIds = restoredBlocked,
                trayTiles = restoredTray,
                undoCharges = newCharges,
                canUndo = undoStack.isNotEmpty() && newCharges > 0,
                status = GameStatus.PLAYING,
            )
        }
        if (wasLost) {
            startTimer()
        }
    }

    fun onHintClicked() {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || isResolvingMatch || state.hintCharges <= 0) return

        val uncovered = state.boardTiles.filter { !it.isRemoved && it.id !in state.blockedTileIds }
        if (uncovered.isEmpty()) return

        val spaceLeft = state.trayCapacity - state.trayTiles.size
        if (spaceLeft <= 0) return

        // Priority 1: Complete a pair already sitting in the tray (2 in tray + 1 uncovered)
        val trayGroups = state.trayTiles.groupBy { it.type }
        val pairInTray = trayGroups.entries.firstOrNull { (type, items) ->
            items.size == 2 && uncovered.any { it.type == type }
        }
        if (pairInTray != null) {
            val targetTile = uncovered.first { it.type == pairInTray.key }
            consumeHintAndPick(listOf(targetTile))
            return
        }

        // Priority 2: Complete a single tile already in the tray (1 in tray + 2 uncovered, if space >= 2)
        if (spaceLeft >= 2) {
            val singleInTray = trayGroups.entries.firstOrNull { (type, items) ->
                items.size == 1 && uncovered.count { it.type == type } >= 2
            }
            if (singleInTray != null) {
                val targets = uncovered.filter { it.type == singleInTray.key }.take(2)
                consumeHintAndPick(targets)
                return
            }
        }

        // Priority 3: Pick 3 uncovered matching tiles if space >= 3
        if (spaceLeft >= 3) {
            val trioUncovered = uncovered.groupBy { it.type }.entries.firstOrNull { it.value.size >= 3 }
            if (trioUncovered != null) {
                consumeHintAndPick(trioUncovered.value.take(3))
                return
            }
        }

        // Priority 4: Highlight a tile that matches at least 1 in tray (even if 3rd isn't uncovered yet)
        val partialInTray = trayGroups.entries.firstOrNull { (type, _) ->
            uncovered.any { it.type == type }
        }
        val highlightTarget = if (partialInTray != null) {
            uncovered.first { it.type == partialInTray.key }
        } else {
            uncovered.maxByOrNull { it.layer } ?: uncovered.first()
        }

        _uiState.update { current ->
            current.copy(
                hintCharges = current.hintCharges - 1,
                highlightedHintTileId = highlightTarget.id,
            )
        }
        soundEngine.playTileTap()
    }

    private fun consumeHintAndPick(tilesToPick: List<BoardTile>) {
        _uiState.update { it.copy(hintCharges = it.hintCharges - 1) }
        viewModelScope.launch {
            for ((index, tile) in tilesToPick.withIndex()) {
                if (index > 0) delay(120L)
                val latest = _uiState.value
                if (latest.status != GameStatus.PLAYING) break
                val liveTile = latest.boardTiles.find { it.id == tile.id && !it.isRemoved }
                if (liveTile != null) {
                    pickTileIntoTray(liveTile)
                }
            }
        }
    }

    fun onShuffleClicked() {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || isResolvingMatch || state.shuffleCharges <= 0) return

        val activeTiles = state.boardTiles.filter { !it.isRemoved }
        if (activeTiles.size <= 1) return

        val typesPool = activeTiles.map { it.type }.toMutableList()
        var bestArrangement: List<TileType> = typesPool.shuffled()

        // Ensure shuffled uncovered tiles offer playable pairs/matches
        for (attempt in 0 until 6) {
            val candidate = typesPool.shuffled()
            val uncoveredTypes = activeTiles.mapIndexedNotNull { idx, tile ->
                if (tile.id !in state.blockedTileIds) candidate[idx] else null
            }
            val hasDuplicateUncovered = uncoveredTypes.groupingBy { it }.eachCount().values.any { it >= 2 }
            if (hasDuplicateUncovered) {
                bestArrangement = candidate
                break
            }
        }

        var idx = 0
        val shuffledBoard = state.boardTiles.map { tile ->
            if (!tile.isRemoved) {
                tile.copy(type = bestArrangement[idx++])
            } else {
                tile
            }
        }

        soundEngine.playShuffle()
        _uiState.update { current ->
            current.copy(
                boardTiles = shuffledBoard,
                shuffleCharges = current.shuffleCharges - 1,
                highlightedHintTileId = null,
                shuffleTick = current.shuffleTick + 1,
            )
        }
    }

    fun toggleSound() {
        val newValue = !_uiState.value.soundEnabled
        soundEngine.enabled = newValue
        _uiState.update { it.copy(soundEnabled = newValue) }
        viewModelScope.launch { prefsRepo.setSoundEnabled(newValue) }
    }

    fun toggleHaptic() {
        val newValue = !_uiState.value.hapticEnabled
        _uiState.update { it.copy(hapticEnabled = newValue) }
        viewModelScope.launch { prefsRepo.setHapticEnabled(newValue) }
    }

    private fun triggerGameWon() {
        timerJob?.cancel()
        celebrationJob?.cancel()
        val state = _uiState.value
        val timeBonus = maxOf(0, 180 - state.elapsedSeconds) * 5
        val finalScore = state.score + timeBonus
        val stars = when {
            state.elapsedSeconds <= 65 -> 3
            state.elapsedSeconds <= 115 -> 2
            else -> 1
        }
        soundEngine.playWinFanfare()

        _uiState.update { current ->
            current.copy(
                score = finalScore,
                timeBonus = timeBonus,
                bestScore = maxOf(current.bestScore, finalScore),
                highestUnlockedLevel = maxOf(current.highestUnlockedLevel, state.currentLevel.levelNumber + 1),
                status = GameStatus.CELEBRATING,
                earnedStars = stars,
            )
        }

        viewModelScope.launch {
            prefsRepo.recordLevelCleared(
                levelNumber = state.currentLevel.levelNumber,
                starsEarned = stars,
                score = finalScore,
            )
        }

        celebrationJob = viewModelScope.launch {
            delay(1450L)
            _uiState.update { current ->
                if (current.status == GameStatus.CELEBRATING) {
                    current.copy(status = GameStatus.WON)
                } else {
                    current
                }
            }
        }
    }

    private fun triggerGameLost() {
        timerJob?.cancel()
        soundEngine.playLoseTone()
        _uiState.update { it.copy(status = GameStatus.LOST) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { current ->
                    if (current.status == GameStatus.PLAYING) {
                        current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                    } else {
                        current
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        celebrationJob?.cancel()
    }
}
