package com.example.model

enum class TileSuit {
    CHARACTERS, // Vạn (萬)
    BAMBOOS,    // Sách (條)
    DOTS,       // Đồng (筒)
    WINDS,      // Phong (東 南 西 北)
    DRAGONS,    // Rồng (中 發 白)
}

enum class TileType(
    val suit: TileSuit,
    val rank: Int,
    val displayNameVi: String,
    val symbolText: String,
) {
    CHAR_1(TileSuit.CHARACTERS, 1, "Nhất Vạn", "一萬"),
    CHAR_2(TileSuit.CHARACTERS, 2, "Nhị Vạn", "二萬"),
    CHAR_3(TileSuit.CHARACTERS, 3, "Tam Vạn", "三萬"),
    CHAR_4(TileSuit.CHARACTERS, 4, "Tứ Vạn", "四萬"),
    CHAR_5(TileSuit.CHARACTERS, 5, "Ngũ Vạn", "五萬"),
    CHAR_6(TileSuit.CHARACTERS, 6, "Lục Vạn", "六萬"),
    CHAR_7(TileSuit.CHARACTERS, 7, "Thất Vạn", "七萬"),
    CHAR_8(TileSuit.CHARACTERS, 8, "Bát Vạn", "八萬"),
    CHAR_9(TileSuit.CHARACTERS, 9, "Cửu Vạn", "九萬"),

    BAMBOO_1(TileSuit.BAMBOOS, 1, "Nhất Sách", "1條"),
    BAMBOO_2(TileSuit.BAMBOOS, 2, "Nhị Sách", "2條"),
    BAMBOO_3(TileSuit.BAMBOOS, 3, "Tam Sách", "3條"),
    BAMBOO_4(TileSuit.BAMBOOS, 4, "Tứ Sách", "4條"),
    BAMBOO_5(TileSuit.BAMBOOS, 5, "Ngũ Sách", "5條"),
    BAMBOO_6(TileSuit.BAMBOOS, 6, "Lục Sách", "6條"),
    BAMBOO_7(TileSuit.BAMBOOS, 7, "Thất Sách", "7條"),
    BAMBOO_8(TileSuit.BAMBOOS, 8, "Bát Sách", "8條"),
    BAMBOO_9(TileSuit.BAMBOOS, 9, "Cửu Sách", "9條"),

    DOT_1(TileSuit.DOTS, 1, "Nhất Đồng", "1筒"),
    DOT_2(TileSuit.DOTS, 2, "Nhị Đồng", "2筒"),
    DOT_3(TileSuit.DOTS, 3, "Tam Đồng", "3筒"),
    DOT_4(TileSuit.DOTS, 4, "Tứ Đồng", "4筒"),
    DOT_5(TileSuit.DOTS, 5, "Ngũ Đồng", "5筒"),
    DOT_6(TileSuit.DOTS, 6, "Lục Đồng", "6筒"),
    DOT_7(TileSuit.DOTS, 7, "Thất Đồng", "7筒"),
    DOT_8(TileSuit.DOTS, 8, "Bát Đồng", "8筒"),
    DOT_9(TileSuit.DOTS, 9, "Cửu Đồng", "9筒"),

    WIND_EAST(TileSuit.WINDS, 1, "Đông Phong", "東"),
    WIND_SOUTH(TileSuit.WINDS, 2, "Nam Phong", "南"),
    WIND_WEST(TileSuit.WINDS, 3, "Tây Phong", "西"),
    WIND_NORTH(TileSuit.WINDS, 4, "Bắc Phong", "北"),

    DRAGON_RED(TileSuit.DRAGONS, 1, "Hồng Trung", "中"),
    DRAGON_GREEN(TileSuit.DRAGONS, 2, "Phát Tài", "發"),
    DRAGON_WHITE(TileSuit.DRAGONS, 3, "Bạch Bản", "白"),
}

data class BoardTile(
    val id: Int,
    val type: TileType,
    val gridX: Float,
    val gridY: Float,
    val layer: Int,
    val isRemoved: Boolean = false,
)

data class TrayTile(
    val boardTileId: Int,
    val type: TileType,
)

data class FloatingPopup(
    val id: Long,
    val text: String,
    val isCombo: Boolean,
)

data class SparkleBurst(
    val id: Long,
    val slotCenterFraction: Float,
    val seed: Int,
)

enum class LayoutStyle {
    PYRAMID,
    TWIN_DRAGONS,
    LOTUS,
    FORTRESS,
    OCTAGON,
    BRIDGE,
    PAGODA,
    GRAND_MASTER,
    HEART,
    CROWN,
    BUTTERFLY,
    CROSS,
    TWIN_TOWERS,
    STAR,
    SHIP,
    TORII_GATE,
    LABYRINTH,
    HOURGLASS,
    SPIRAL,
    IMPERIAL_PALACE,
}

data class LevelDefinition(
    val levelNumber: Int,
    val nameVi: String,
    val layoutStyle: LayoutStyle,
    val tileTypesCount: Int,
    val totalTrios: Int,
)

enum class GameStatus {
    PLAYING,
    CELEBRATING,
    WON,
    LOST,
}

data class GameUiState(
    val currentLevel: LevelDefinition = LevelDefinition(1, "Kim Tự Tháp", LayoutStyle.PYRAMID, 12, 12),
    val boardTiles: List<BoardTile> = emptyList(),
    val blockedTileIds: Set<Int> = emptySet(),
    val trayTiles: List<TrayTile> = emptyList(),
    val trayCapacity: Int = 5,
    val score: Int = 0,
    val timeBonus: Int = 0,
    val bestScore: Int = 0,
    val comboCount: Int = 0,
    val elapsedSeconds: Int = 0,
    val undoCharges: Int = 3,
    val hintCharges: Int = 2,
    val shuffleCharges: Int = 2,
    val canUndo: Boolean = false,
    val highlightedHintTileId: Int? = null,
    val invalidShakeTileId: Int? = null,
    val matchingClearType: TileType? = null,
    val popups: List<FloatingPopup> = emptyList(),
    val sparkleBursts: List<SparkleBurst> = emptyList(),
    val status: GameStatus = GameStatus.PLAYING,
    val earnedStars: Int = 0,
    val highestUnlockedLevel: Int = 1,
    val starsByLevel: Map<Int, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val shuffleTick: Int = 0,
)
