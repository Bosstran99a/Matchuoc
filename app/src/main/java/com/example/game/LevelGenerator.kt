package com.example.game

import com.example.model.BoardTile
import com.example.model.LayoutStyle
import com.example.model.LevelDefinition
import com.example.model.TileType
import kotlin.math.abs
import kotlin.random.Random

data class TileSlot(
    val gridX: Float,
    val gridY: Float,
    val layer: Int,
)

object LevelGenerator {

    private val levelCatalog = listOf(
        LevelDefinition(1, "Kim Tự Tháp", LayoutStyle.PYRAMID, tileTypesCount = 10, totalTrios = 12),
        LevelDefinition(2, "Song Long Hội", LayoutStyle.TWIN_DRAGONS, tileTypesCount = 13, totalTrios = 16),
        LevelDefinition(3, "Đài Sen Ngọc", LayoutStyle.LOTUS, tileTypesCount = 16, totalTrios = 20),
        LevelDefinition(4, "Thành Cổ", LayoutStyle.FORTRESS, tileTypesCount = 18, totalTrios = 24),
        LevelDefinition(5, "Bát Quái Trận", LayoutStyle.OCTAGON, tileTypesCount = 21, totalTrios = 28),
        LevelDefinition(6, "Cầu Ngọc Bích", LayoutStyle.BRIDGE, tileTypesCount = 24, totalTrios = 32),
        LevelDefinition(7, "Tháp Cửu Trùng", LayoutStyle.PAGODA, tileTypesCount = 27, totalTrios = 36),
        LevelDefinition(8, "Đại Thách Đấu", LayoutStyle.GRAND_MASTER, tileTypesCount = 30, totalTrios = 40),
        LevelDefinition(9, "Trái Tim Ngọc", LayoutStyle.HEART, tileTypesCount = 16, totalTrios = 18),
        LevelDefinition(10, "Vương Miện Vàng", LayoutStyle.CROWN, tileTypesCount = 18, totalTrios = 22),
        LevelDefinition(11, "Hồ Điệp Vũ", LayoutStyle.BUTTERFLY, tileTypesCount = 20, totalTrios = 26),
        LevelDefinition(12, "Chữ Thập Thánh", LayoutStyle.CROSS, tileTypesCount = 22, totalTrios = 30),
        LevelDefinition(13, "Song Tháp Đôi", LayoutStyle.TWIN_TOWERS, tileTypesCount = 24, totalTrios = 32),
        LevelDefinition(14, "Ngôi Sao Mai", LayoutStyle.STAR, tileTypesCount = 25, totalTrios = 34),
        LevelDefinition(15, "Chiến Thuyền", LayoutStyle.SHIP, tileTypesCount = 26, totalTrios = 36),
        LevelDefinition(16, "Cổng Tam Quan", LayoutStyle.TORII_GATE, tileTypesCount = 28, totalTrios = 38),
        LevelDefinition(17, "Mê Cung Rồng", LayoutStyle.LABYRINTH, tileTypesCount = 29, totalTrios = 40),
        LevelDefinition(18, "Đồng Hồ Cát", LayoutStyle.HOURGLASS, tileTypesCount = 30, totalTrios = 42),
        LevelDefinition(19, "Thiên Hà Xoắn", LayoutStyle.SPIRAL, tileTypesCount = 32, totalTrios = 44),
        LevelDefinition(20, "Hoàng Cung", LayoutStyle.IMPERIAL_PALACE, tileTypesCount = 34, totalTrios = 48),
    )

    fun getLevelDefinition(levelNumber: Int): LevelDefinition {
        if (levelNumber <= levelCatalog.size) {
            return levelCatalog[(levelNumber - 1).coerceAtLeast(0)]
        }
        val styles = LayoutStyle.entries
        val style = styles[(levelNumber - 1) % styles.size]
        val extraTrios = ((levelNumber - levelCatalog.size) * 2).coerceAtMost(12)
        val totalTrios = 40 + extraTrios
        val typesCount = (30 + (levelNumber - levelCatalog.size)).coerceAtMost(TileType.entries.size)
        val cycle = ((levelNumber - 1) / levelCatalog.size) + 1
        val baseName = levelCatalog[(levelNumber - 1) % levelCatalog.size].nameVi
        return LevelDefinition(
            levelNumber = levelNumber,
            nameVi = "$baseName $cycle",
            layoutStyle = style,
            tileTypesCount = typesCount,
            totalTrios = totalTrios,
        )
    }

    fun getAllPresetLevels(): List<LevelDefinition> = levelCatalog

    fun generateBoard(level: LevelDefinition, seed: Long = System.currentTimeMillis()): List<BoardTile> {
        val random = Random(seed)
        val targetCount = level.totalTrios * 3
        val slots = buildSlots(level.layoutStyle, targetCount)

        val shuffledCatalog = TileType.entries.shuffled(random)
        val chosenTypes = shuffledCatalog.take(level.tileTypesCount.coerceIn(3, TileType.entries.size))

        val triosNeeded = slots.size / 3
        val trioTypes = MutableList(triosNeeded) { index ->
            chosenTypes[index % chosenTypes.size]
        }.shuffled(random)

        val remaining = slots.mapIndexed { idx, slot -> idx to slot }.toMutableList()
        val assigned = arrayOfNulls<TileType>(slots.size)

        var trioIdx = 0
        while (remaining.isNotEmpty() && trioIdx < trioTypes.size) {
            val uncovered = remaining.filter { (_, slot) ->
                remaining.none { (_, other) -> isOverlappingAbove(slot, other) }
            }

            val pickPool = if (uncovered.size >= 3) {
                uncovered
            } else {
                remaining.sortedByDescending { it.second.layer }
            }

            val chosen = pickPool.shuffled(random).take(3)
            val type = trioTypes[trioIdx++]
            for ((slotIndex, _) in chosen) {
                assigned[slotIndex] = type
                remaining.removeAll { it.first == slotIndex }
            }
        }

        return slots.mapIndexed { index, slot ->
            BoardTile(
                id = index + 1,
                type = assigned[index] ?: chosenTypes.first(),
                gridX = slot.gridX,
                gridY = slot.gridY,
                layer = slot.layer,
                isRemoved = false,
            )
        }
    }

    fun computeBlockedIds(tiles: List<BoardTile>): Set<Int> {
        val active = tiles.filter { !it.isRemoved }
        val blocked = HashSet<Int>()
        for (i in active.indices) {
            val tile = active[i]
            for (j in active.indices) {
                if (i == j) continue
                val other = active[j]
                if (other.layer > tile.layer &&
                    abs(other.gridX - tile.gridX) < 0.92f &&
                    abs(other.gridY - tile.gridY) < 0.92f
                ) {
                    blocked.add(tile.id)
                    break
                }
            }
        }
        return blocked
    }

    private fun isOverlappingAbove(lower: TileSlot, upper: TileSlot): Boolean {
        return upper.layer > lower.layer &&
            abs(upper.gridX - lower.gridX) < 0.92f &&
            abs(upper.gridY - lower.gridY) < 0.92f
    }

    private fun buildSlots(style: LayoutStyle, targetCount: Int): List<TileSlot> {
        val raw = when (style) {
            LayoutStyle.PYRAMID -> buildPyramidSlots()
            LayoutStyle.TWIN_DRAGONS -> buildTwinDragonsSlots()
            LayoutStyle.LOTUS -> buildLotusSlots()
            LayoutStyle.FORTRESS -> buildFortressSlots()
            LayoutStyle.OCTAGON -> buildOctagonSlots()
            LayoutStyle.BRIDGE -> buildBridgeSlots()
            LayoutStyle.PAGODA -> buildPagodaSlots()
            LayoutStyle.GRAND_MASTER -> buildGrandMasterSlots()
            LayoutStyle.HEART -> buildHeartSlots()
            LayoutStyle.CROWN -> buildCrownSlots()
            LayoutStyle.BUTTERFLY -> buildButterflySlots()
            LayoutStyle.CROSS -> buildCrossSlots()
            LayoutStyle.TWIN_TOWERS -> buildTwinTowersSlots()
            LayoutStyle.STAR -> buildStarSlots()
            LayoutStyle.SHIP -> buildShipSlots()
            LayoutStyle.TORII_GATE -> buildToriiGateSlots()
            LayoutStyle.LABYRINTH -> buildLabyrinthSlots()
            LayoutStyle.HOURGLASS -> buildHourglassSlots()
            LayoutStyle.SPIRAL -> buildSpiralSlots()
            LayoutStyle.IMPERIAL_PALACE -> buildImperialPalaceSlots()
        }.distinct()

        val desired = (targetCount / 3) * 3
        if (raw.size == desired) return raw
        if (raw.size > desired) {
            return raw.take(desired)
        }
        val mutable = raw.toMutableList()
        var extraLayer = (raw.maxOfOrNull { it.layer } ?: 0) + 1
        while (mutable.size < desired) {
            for (y in 1..4) {
                for (x in 1..4) {
                    if (mutable.size >= desired) break
                    val candidate = TileSlot(
                        x.toFloat() + (extraLayer % 2) * 0.5f,
                        y.toFloat() + (extraLayer % 2) * 0.5f,
                        extraLayer,
                    )
                    if (candidate !in mutable) {
                        mutable.add(candidate)
                    }
                }
            }
            extraLayer++
        }
        return mutable.take(desired)
    }

    // 36 tiles
    private fun buildPyramidSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..4) {
            for (x in 0..4) {
                if ((x == 0 || x == 4) && (y == 0 || y == 4)) continue
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        }
        for (y in 0..2) {
            for (x in 0..2) {
                list.add(TileSlot(x + 1.0f, y + 1.0f, 1))
            }
        }
        for (y in 0..1) {
            for (x in 0..1) {
                list.add(TileSlot(x + 1.5f, y + 1.5f, 2))
            }
        }
        list.add(TileSlot(1.5f, 2.0f, 3))
        list.add(TileSlot(2.5f, 2.0f, 3))
        return list
    }

    // 48 tiles
    private fun buildTwinDragonsSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..4) {
            for (x in 0..5) {
                if ((x == 2 || x == 3) && (y == 0 || y == 4)) continue
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        }
        for (y in 1..3) {
            list.add(TileSlot(0.5f, y.toFloat(), 1))
            list.add(TileSlot(1.5f, y.toFloat(), 1))
            list.add(TileSlot(3.5f, y.toFloat(), 1))
            list.add(TileSlot(4.5f, y.toFloat(), 1))
        }
        list.add(TileSlot(2.5f, 1.5f, 1))
        list.add(TileSlot(2.5f, 2.5f, 1))
        list.add(TileSlot(1.0f, 2.0f, 2))
        list.add(TileSlot(4.0f, 2.0f, 2))
        list.add(TileSlot(2.0f, 2.0f, 2))
        list.add(TileSlot(3.0f, 2.0f, 2))
        list.add(TileSlot(1.0f, 1.0f, 2))
        list.add(TileSlot(4.0f, 3.0f, 2))
        list.add(TileSlot(1.0f, 2.0f, 3))
        list.add(TileSlot(4.0f, 2.0f, 3))
        return list
    }

    // 60 tiles
    private fun buildLotusSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..5) {
            for (x in 0..5) {
                val edge = (x == 0 || x == 5) && (y == 0 || y == 5)
                val nearEdge = (x == 0 || x == 5) && (y == 1 || y == 4)
                if (edge || nearEdge) continue
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        }
        for (y in 1..4) {
            for (x in 1..4) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 1))
            }
        }
        for (y in 0..2) {
            for (x in 0..2) {
                list.add(TileSlot(x + 1.5f, y + 1.5f, 2))
            }
        }
        for (y in 0..1) {
            for (x in 0..1) {
                list.add(TileSlot(x + 2.0f, y + 2.0f, 3))
            }
        }
        list.add(TileSlot(2.5f, 1.5f, 4))
        list.add(TileSlot(2.5f, 2.5f, 4))
        list.add(TileSlot(2.5f, 3.5f, 4))
        return list
    }

    // 72 tiles
    private fun buildFortressSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..5) {
            for (x in 0..5) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        }
        for (y in listOf(0.5f, 4.5f)) {
            for (x in listOf(0.5f, 4.5f)) {
                list.add(TileSlot(x, y, 1))
            }
        }
        for (y in 1..4) {
            list.add(TileSlot(0.5f, y.toFloat(), 1))
            list.add(TileSlot(4.5f, y.toFloat(), 1))
        }
        for (y in 2..3) {
            for (x in 2..3) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 1))
            }
        }
        for (y in 0..2) {
            for (x in 0..2) {
                list.add(TileSlot(x + 1.5f, y + 1.5f, 2))
            }
        }
        list.add(TileSlot(0.5f, 0.5f, 2))
        list.add(TileSlot(4.5f, 0.5f, 2))
        list.add(TileSlot(0.5f, 4.5f, 2))
        for (y in 0..1) {
            for (x in 0..1) {
                list.add(TileSlot(x + 2.0f, y + 2.0f, 3))
            }
        }
        return list
    }

    // 84 tiles
    private fun buildOctagonSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..5) {
            for (x in 0..6) {
                if ((x == 0 || x == 6) && (y == 0 || y == 5)) continue
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        }
        for (y in 0..3) {
            for (x in 0..4) {
                list.add(TileSlot(x + 1.0f, y + 1.0f, 1))
            }
        }
        for (y in 0..2) {
            for (x in 0..3) {
                list.add(TileSlot(x + 1.5f, y + 1.5f, 2))
            }
        }
        for (y in 0..2) {
            for (x in 0..2) {
                list.add(TileSlot(x + 2.0f, y + 1.5f, 3))
            }
        }
        list.add(TileSlot(2.5f, 2.0f, 4))
        list.add(TileSlot(3.5f, 2.0f, 4))
        list.add(TileSlot(2.5f, 3.0f, 4))
        list.add(TileSlot(3.5f, 3.0f, 4))
        list.add(TileSlot(3.0f, 2.5f, 5))
        return list
    }

    // 96 tiles
    private fun buildBridgeSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..5) {
            for (x in 0..6) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        }
        for (y in 0..3) {
            for (x in 0..5) {
                list.add(TileSlot(x + 0.5f, y + 1.0f, 1))
            }
        }
        for (y in 0..2) {
            for (x in 0..4) {
                list.add(TileSlot(x + 1.0f, y + 1.5f, 2))
            }
        }
        for (y in 0..1) {
            for (x in 0..3) {
                list.add(TileSlot(x + 1.5f, y + 2.0f, 3))
            }
        }
        list.add(TileSlot(0.5f, 1.0f, 2))
        list.add(TileSlot(5.5f, 1.0f, 2))
        list.add(TileSlot(0.5f, 4.0f, 2))
        list.add(TileSlot(5.5f, 4.0f, 2))
        list.add(TileSlot(2.0f, 2.5f, 4))
        list.add(TileSlot(3.0f, 2.5f, 4))
        list.add(TileSlot(4.0f, 2.5f, 4))
        return list
    }

    // 108 tiles
    private fun buildPagodaSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..6) {
            for (x in 0..6) {
                if ((x == 0 || x == 6) && (y == 0 || y == 6)) continue
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        }
        for (y in 1..5) {
            for (x in 1..5) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 1))
            }
        }
        for (y in 0..3) {
            for (x in 0..3) {
                list.add(TileSlot(x + 1.5f, y + 1.5f, 2))
            }
        }
        for (y in 0..2) {
            for (x in 0..2) {
                list.add(TileSlot(x + 2.0f, y + 2.0f, 3))
            }
        }
        for (y in 0..2) {
            for (x in 0..1) {
                list.add(TileSlot(x + 2.5f, y + 2.0f, 4))
            }
        }
        list.add(TileSlot(2.0f, 3.0f, 4))
        list.add(TileSlot(4.0f, 3.0f, 4))
        list.add(TileSlot(3.0f, 1.5f, 4))
        for (y in 0..1) {
            for (x in 0..1) {
                list.add(TileSlot(x + 2.5f, y + 2.5f, 5))
            }
        }
        return list
    }

    // 120 tiles
    private fun buildGrandMasterSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..6) {
            for (x in 0..6) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        }
        for (y in 0..4) {
            for (x in 0..5) {
                list.add(TileSlot(x + 0.5f, y + 1.0f, 1))
            }
        }
        for (y in 0..3) {
            for (x in 0..4) {
                list.add(TileSlot(x + 1.0f, y + 1.5f, 2))
            }
        }
        for (y in 0..2) {
            for (x in 0..3) {
                list.add(TileSlot(x + 1.5f, y + 2.0f, 3))
            }
        }
        for (y in 0..1) {
            for (x in 0..2) {
                list.add(TileSlot(x + 2.0f, y + 2.5f, 4))
            }
        }
        list.add(TileSlot(2.5f, 3.0f, 5))
        list.add(TileSlot(3.5f, 3.0f, 5))
        list.add(TileSlot(3.0f, 2.5f, 6))
        return list
    }

    // 54 tiles: Symmetric Heart silhouette
    private fun buildHeartSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        val heartRowsLayer0 = listOf(
            listOf(1f, 2f, 4f, 5f),
            listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f),
            listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f),
            listOf(1f, 2f, 3f, 4f, 5f),
            listOf(2f, 3f, 4f),
            listOf(3f),
        )
        heartRowsLayer0.forEachIndexed { y, xs ->
            xs.forEach { x -> list.add(TileSlot(x, y.toFloat(), 0)) }
        } // 4+7+7+5+3+1 = 27
        val heartLayer1 = listOf(
            listOf(1.5f, 4.5f),
            listOf(1f, 2f, 3f, 4f, 5f),
            listOf(1.5f, 2.5f, 3.5f, 4.5f),
            listOf(2f, 3f, 4f),
            listOf(3f),
        )
        heartLayer1.forEachIndexed { idx, xs ->
            xs.forEach { x -> list.add(TileSlot(x, (idx + 1).toFloat(), 1)) }
        } // 2+5+4+3+1 = 15 (total 42)
        listOf(
            TileSlot(1.5f, 1.5f, 2), TileSlot(4.5f, 1.5f, 2),
            TileSlot(2.0f, 2.5f, 2), TileSlot(3.0f, 2.5f, 2), TileSlot(4.0f, 2.5f, 2),
            TileSlot(2.5f, 3.5f, 2), TileSlot(3.5f, 3.5f, 2),
            TileSlot(3.0f, 4.0f, 2),
        ).forEach { list.add(it) } // 8 (total 50)
        list.add(TileSlot(2.5f, 2.0f, 3))
        list.add(TileSlot(3.5f, 2.0f, 3))
        list.add(TileSlot(3.0f, 3.0f, 3))
        list.add(TileSlot(3.0f, 2.5f, 4)) // 4 (total 54)
        return list
    }

    // 66 tiles: Royal 3-Peak Crown
    private fun buildCrownSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        // Top 3 spires + wide base
        listOf(0f, 3f, 6f).forEach { x -> list.add(TileSlot(x, 0f, 0)) }
        for (x in 0..6) {
            if (x == 2 || x == 4) continue
            list.add(TileSlot(x.toFloat(), 1f, 0))
        } // 5
        for (y in 2..4) {
            for (x in 0..6) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        } // 21 (layer 0 = 29)
        // Layer 1: spires & band
        listOf(0f, 3f, 6f).forEach { x -> list.add(TileSlot(x, 0.5f, 1)) }
        for (x in 0..5) {
            list.add(TileSlot(x + 0.5f, 2.5f, 1))
            list.add(TileSlot(x + 0.5f, 3.5f, 1))
        } // 12 (layer 1 = 15 -> total 44)
        // Layer 2: jewels
        for (x in 1..5) {
            list.add(TileSlot(x.toFloat(), 3.0f, 2))
            list.add(TileSlot(x.toFloat(), 4.0f, 2))
        } // 10
        list.add(TileSlot(0f, 0.5f, 2))
        list.add(TileSlot(3f, 0.5f, 2))
        list.add(TileSlot(6f, 0.5f, 2)) // 13 (total 57)
        // Layer 3 & 4 center crest
        for (x in 2..4) {
            list.add(TileSlot(x.toFloat(), 3.0f, 3))
            list.add(TileSlot(x.toFloat(), 3.8f, 3))
        } // 6 (total 63)
        list.add(TileSlot(3.0f, 1.5f, 3))
        list.add(TileSlot(2.5f, 3.4f, 4))
        list.add(TileSlot(3.5f, 3.4f, 4)) // 3 (total 66)
        return list
    }

    // 78 tiles: Butterfly wings
    private fun buildButterflySlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        // Layer 0: left & right wings (6x6 with waist indentation)
        for (y in 0..5) {
            for (x in 0..6) {
                if (x == 3 && (y == 0 || y == 5)) continue
                if ((x == 0 || x == 6) && y == 3) continue
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        } // 42 - 4 = 38
        // Layer 1: wing inner patterns + thorax
        for (y in 1..4) {
            list.add(TileSlot(0.5f, y.toFloat(), 1))
            list.add(TileSlot(1.5f, y.toFloat(), 1))
            list.add(TileSlot(4.5f, y.toFloat(), 1))
            list.add(TileSlot(5.5f, y.toFloat(), 1))
        } // 16
        for (y in 1..4) {
            list.add(TileSlot(3.0f, y.toFloat(), 1))
        } // 4 (total 58)
        // Layer 2: upper & lower wing spots + center spine
        list.add(TileSlot(1.0f, 1.5f, 2))
        list.add(TileSlot(5.0f, 1.5f, 2))
        list.add(TileSlot(1.0f, 3.5f, 2))
        list.add(TileSlot(5.0f, 3.5f, 2))
        for (y in 1..4) {
            list.add(TileSlot(3.0f, y + 0.25f, 2))
        } // 8 (total 66)
        // Layer 3 & 4: butterfly body & crown
        list.add(TileSlot(1.0f, 2.0f, 3))
        list.add(TileSlot(5.0f, 2.0f, 3))
        for (y in 1..4) {
            list.add(TileSlot(3.0f, y.toFloat(), 3))
        } // 6 (total 72)
        list.add(TileSlot(1.0f, 2.0f, 4))
        list.add(TileSlot(5.0f, 2.0f, 4))
        list.add(TileSlot(3.0f, 2.0f, 4))
        list.add(TileSlot(3.0f, 3.0f, 4))
        list.add(TileSlot(3.0f, 2.5f, 5))
        list.add(TileSlot(3.0f, 1.2f, 5)) // 6 (total 78)
        return list
    }

    // 90 tiles: Stepped Temple Cross
    private fun buildCrossSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        // Layer 0: 7x7 cross (center 3 cols all rows, center 3 rows all cols)
        for (y in 0..6) {
            for (x in 0..6) {
                val inVert = x in 2..4
                val inHoriz = y in 2..4
                if (inVert || inHoriz) {
                    list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
                }
            }
        } // 33
        // Layer 1: slightly inset cross
        for (y in 0..5) {
            for (x in 0..5) {
                val inVert = x in 2..3
                val inHoriz = y in 2..3
                if (inVert || inHoriz) {
                    list.add(TileSlot(x + 0.5f, y + 0.5f, 1))
                }
            }
        } // 20 (total 53)
        // Layer 2: 3x3 center + 4 arm caps = 13
        for (y in 2..4) {
            for (x in 2..4) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 2))
            }
        }
        list.add(TileSlot(3.0f, 0.5f, 2))
        list.add(TileSlot(3.0f, 5.5f, 2))
        list.add(TileSlot(0.5f, 3.0f, 2))
        list.add(TileSlot(5.5f, 3.0f, 2)) // total 66
        // Layer 3: 3x3 staggered + 4 = 13
        for (y in 0..2) {
            for (x in 0..2) {
                list.add(TileSlot(x + 2.0f, y + 2.0f, 3))
            }
        }
        list.add(TileSlot(3.0f, 1.0f, 3))
        list.add(TileSlot(3.0f, 5.0f, 3))
        list.add(TileSlot(1.0f, 3.0f, 3))
        list.add(TileSlot(5.0f, 3.0f, 3)) // total 79
        // Layer 4: 2x2 + 4 + layer 5 top = 11 (total 90)
        for (y in 0..1) {
            for (x in 0..1) {
                list.add(TileSlot(x + 2.5f, y + 2.5f, 4))
            }
        }
        list.add(TileSlot(3.0f, 1.5f, 4))
        list.add(TileSlot(3.0f, 4.5f, 4))
        list.add(TileSlot(1.5f, 3.0f, 4))
        list.add(TileSlot(4.5f, 3.0f, 4))
        list.add(TileSlot(2.5f, 3.0f, 5))
        list.add(TileSlot(3.5f, 3.0f, 5))
        list.add(TileSlot(3.0f, 2.5f, 6))
        return list
    }

    // 96 tiles: Twin 6-layer Towers + Bridge
    private fun buildTwinTowersSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        // Layer 0: two 3x6 towers + 2-row bridge
        for (y in 0..5) {
            for (x in 0..2) list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            for (x in 4..6) list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
        } // 36
        list.add(TileSlot(3f, 2f, 0))
        list.add(TileSlot(3f, 3f, 0)) // 38
        // Layer 1..5 towers
        for (layer in 1..4) {
            val inset = (layer - 1) * 0.25f
            for (y in 1..4) {
                list.add(TileSlot(0.5f + inset, y.toFloat(), layer))
                list.add(TileSlot(1.5f - inset, y.toFloat(), layer))
                list.add(TileSlot(4.5f + inset, y.toFloat(), layer))
                list.add(TileSlot(5.5f - inset, y.toFloat(), layer))
            }
        }
        //Wait: simplify tower layers cleanly so distinct counts match >= 96!
        list.clear()
        for (y in 0..5) {
            for (x in 0..2) list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            for (x in 4..6) list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
        } // 36
        list.add(TileSlot(3f, 2f, 0))
        list.add(TileSlot(3f, 3f, 0))
        list.add(TileSlot(3f, 2.5f, 1)) // 39
        // Layer 1: two 2x5 wings (20)
        for (y in 0..4) {
            list.add(TileSlot(0.5f, y + 0.5f, 1))
            list.add(TileSlot(1.5f, y + 0.5f, 1))
            list.add(TileSlot(4.5f, y + 0.5f, 1))
            list.add(TileSlot(5.5f, y + 0.5f, 1))
        } // total 59
        // Layer 2: two 2x4 wings + center bridge (18)
        for (y in 1..4) {
            list.add(TileSlot(0.5f, y.toFloat(), 2))
            list.add(TileSlot(1.5f, y.toFloat(), 2))
            list.add(TileSlot(4.5f, y.toFloat(), 2))
            list.add(TileSlot(5.5f, y.toFloat(), 2))
        }
        list.add(TileSlot(3.0f, 2.0f, 2))
        list.add(TileSlot(3.0f, 3.0f, 2)) // total 77
        // Layer 3: two 1x3 spines + caps (10)
        for (y in 1..3) {
            list.add(TileSlot(1.0f, y + 0.5f, 3))
            list.add(TileSlot(5.0f, y + 0.5f, 3))
        }
        list.add(TileSlot(0.5f, 2.5f, 3))
        list.add(TileSlot(1.5f, 2.5f, 3))
        list.add(TileSlot(4.5f, 2.5f, 3))
        list.add(TileSlot(5.5f, 2.5f, 3)) // total 87
        // Layer 4 & 5 crowns (9 -> total 96)
        list.add(TileSlot(1.0f, 1.5f, 4))
        list.add(TileSlot(1.0f, 2.5f, 4))
        list.add(TileSlot(1.0f, 3.5f, 4))
        list.add(TileSlot(5.0f, 1.5f, 4))
        list.add(TileSlot(5.0f, 2.5f, 4))
        list.add(TileSlot(5.0f, 3.5f, 4))
        list.add(TileSlot(1.0f, 2.5f, 5))
        list.add(TileSlot(5.0f, 2.5f, 5))
        list.add(TileSlot(3.0f, 2.5f, 3))
        return list
    }

    // 102 tiles: 4-point Compass Star
    private fun buildStarSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        // Layer 0: 7x7 diamond/star
        for (y in 0..6) {
            for (x in 0..6) {
                val dist = abs(x - 3) + abs(y - 3)
                if (dist <= 4) {
                    list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
                }
            }
        } // 41
        // Layer 1: dist <= 3 on 0.5 offset grid (25)
        for (y in 0..5) {
            for (x in 0..5) {
                val dist = abs((x + 0.5f) - 3f) + abs((y + 0.5f) - 3f)
                if (dist <= 3.0f) {
                    list.add(TileSlot(x + 0.5f, y + 0.5f, 1))
                }
            }
        } // 20? Wait: 6x6 where |x-2.5|+|y-2.5| <= 3 -> 24 tiles (total 65)
        // Layer 2: 4x4 + 4 cardinal tips = 20 (total 85)
        for (y in 1..4) {
            for (x in 1..4) {
                if ((x == 1 || x == 4) && (y == 1 || y == 4)) continue
                list.add(TileSlot(x + 0.5f, y + 0.5f, 2))
            }
        }
        // Wait: 1..4 is 16 - 4 = 12, let's add tips cleanly:
        list.add(TileSlot(3f, 0f, 2))
        list.add(TileSlot(3f, 6f, 2))
        list.add(TileSlot(0f, 3f, 2))
        list.add(TileSlot(6f, 3f, 2))
        // Layer 3: 3x3 + 4 (13)
        for (y in 2..4) {
            for (x in 2..4) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 3))
            }
        }
        list.add(TileSlot(3f, 1f, 3))
        list.add(TileSlot(3f, 5f, 3))
        list.add(TileSlot(1f, 3f, 3))
        list.add(TileSlot(5f, 3f, 3))
        // Layer 4: 2x2 + layer 5 crown (8)
        for (y in 0..1) {
            for (x in 0..1) {
                list.add(TileSlot(x + 2.5f, y + 2.5f, 4))
            }
        }
        list.add(TileSlot(3.0f, 2.0f, 5))
        list.add(TileSlot(3.0f, 4.0f, 5))
        list.add(TileSlot(2.0f, 3.0f, 5))
        list.add(TileSlot(4.0f, 3.0f, 5))
        list.add(TileSlot(3.0f, 3.0f, 6))
        return list
    }

    // 108 tiles: Dragon Ship (wide hull + tall sail mast)
    private fun buildShipSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        // Layer 0: hull rows 3..6 + mast base
        for (y in 3..6) {
            val startX = if (y == 6) 1 else 0
            val endX = if (y == 6) 6 else 7
            for (x in startX..endX) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        } // 8+8+8+6 = 30
        for (y in 0..2) {
            for (x in 2..5) list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
        } // 12 (layer 0 = 42)
        // Layer 1: sail & deck (30 -> total 72)
        for (y in 3..5) {
            for (x in 1..6) list.add(TileSlot(x.toFloat(), y.toFloat(), 1))
        } // 18
        for (y in 0..2) {
            for (x in 2..5) list.add(TileSlot(x + 0.5f, y.toFloat(), 1))
        } // 12
        // Layer 2: upper deck & sail (20 -> total 92)
        for (y in 3..4) {
            for (x in 1..5) list.add(TileSlot(x + 0.5f, y.toFloat(), 2))
        } // 10
        for (y in 0..2) {
            for (x in 2..4) list.add(TileSlot(x + 0.5f, y + 0.5f, 2))
        } // 9
        list.add(TileSlot(3.5f, 5.0f, 2))
        // Layer 3 & 4: mast peak & cabin (16 -> total 108)
        for (y in 1..4) {
            list.add(TileSlot(3.0f, y.toFloat(), 3))
            list.add(TileSlot(4.0f, y.toFloat(), 3))
        } // 8
        for (y in 1..4) {
            list.add(TileSlot(3.5f, y.toFloat(), 4))
        } // 4
        list.add(TileSlot(3.5f, 2.0f, 5))
        list.add(TileSlot(3.5f, 3.0f, 5))
        list.add(TileSlot(2.5f, 4.0f, 4))
        list.add(TileSlot(4.5f, 4.0f, 4))
        return list
    }

    // 114 tiles: Torii Three-Arch Temple Gate
    private fun buildToriiGateSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        // Layer 0: Top lintels (y=0,1), 2 pillars (x=1,5), bottom plinths
        for (y in 0..1) {
            for (x in 0..6) list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
        } // 14
        for (y in 2..6) {
            list.add(TileSlot(1f, y.toFloat(), 0))
            list.add(TileSlot(2f, y.toFloat(), 0))
            list.add(TileSlot(4f, y.toFloat(), 0))
            list.add(TileSlot(5f, y.toFloat(), 0))
        } // 20
        for (x in 0..6) list.add(TileSlot(x.toFloat(), 6f, 0)) // +3
        list.add(TileSlot(3f, 2f, 0))
        list.add(TileSlot(3f, 3f, 0))
        // Layer 1: full upper beams & pillars
        for (y in 0..1) {
            for (x in 0..5) list.add(TileSlot(x + 0.5f, y + 0.25f, 1))
        } // 12
        for (y in 2..5) {
            list.add(TileSlot(1.5f, y.toFloat(), 1))
            list.add(TileSlot(4.5f, y.toFloat(), 1))
            list.add(TileSlot(3.0f, y.toFloat(), 1))
        } // 12
        for (x in 0..6) list.add(TileSlot(x.toFloat(), 5.5f, 1)) // 7
        // Layer 2..5 stacked gate beams & center plaque (needs >= 114)
        for (y in 0..1) {
            for (x in 1..5) list.add(TileSlot(x.toFloat(), y + 0.5f, 2))
        } // 10
        for (y in 2..5) {
            list.add(TileSlot(1.5f, y + 0.5f, 2))
            list.add(TileSlot(4.5f, y + 0.5f, 2))
        } // 8
        for (x in 1..5) list.add(TileSlot(x.toFloat(), 3.0f, 2)) // 5
        for (y in 0..4) {
            for (x in 2..4) list.add(TileSlot(x.toFloat(), y + 1.0f, 3))
        } // 15
        for (y in 1..3) {
            for (x in 2..4) list.add(TileSlot(x + 0.5f, y + 0.5f, 4))
        } // 9
        list.add(TileSlot(3.0f, 1.0f, 5))
        list.add(TileSlot(3.0f, 2.0f, 5))
        list.add(TileSlot(3.0f, 3.0f, 5))
        list.add(TileSlot(2.0f, 1.0f, 5))
        list.add(TileSlot(4.0f, 1.0f, 5))
        return list
    }

    // 120 tiles: Dragon Labyrinth
    private fun buildLabyrinthSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        // Layer 0: 7x7 outer ring + inner cross (45)
        for (y in 0..6) {
            for (x in 0..6) {
                if ((x == 1 || x == 5) && (y == 1 || y == 5)) continue
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        } // 45
        // Layer 1: 6x6 staggered (36)
        for (y in 0..5) {
            for (x in 0..5) {
                list.add(TileSlot(x + 0.5f, y + 0.5f, 1))
            }
        } // total 81
        // Layer 2: 5x4 (20)
        for (y in 1..4) {
            for (x in 1..5) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 2))
            }
        } // total 101
        // Layer 3: 3x4 (12)
        for (y in 1..4) {
            for (x in 2..4) {
                list.add(TileSlot(x.toFloat(), y + 0.5f, 3))
            }
        } // total 113
        // Layer 4 & 5: 7 crown tiles
        for (y in 2..3) {
            for (x in 2..3) {
                list.add(TileSlot(x + 0.5f, y + 0.5f, 4))
            }
        }
        list.add(TileSlot(3.0f, 2.0f, 5))
        list.add(TileSlot(3.0f, 4.0f, 5))
        list.add(TileSlot(3.0f, 3.0f, 6))
        return list
    }

    // 126 tiles: Hourglass
    private fun buildHourglassSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        val rowWidths = listOf(7, 5, 3, 2, 3, 5, 7)
        // Layer 0 (32)
        rowWidths.forEachIndexed { y, w ->
            val startX = (7 - w) / 2f
            for (i in 0 until w) {
                list.add(TileSlot(startX + i, y.toFloat(), 0))
            }
        }
        // Layer 1 (28)
        listOf(6, 4, 3, 2, 3, 4, 6).forEachIndexed { y, w ->
            val startX = (7 - w) / 2f
            for (i in 0 until w) {
                list.add(TileSlot(startX + i, y.toFloat(), 1))
            }
        }
        // Layer 2 (24)
        listOf(5, 4, 2, 2, 2, 4, 5).forEachIndexed { y, w ->
            val startX = (7 - w) / 2f
            for (i in 0 until w) {
                list.add(TileSlot(startX + i, y + 0.25f, 2))
            }
        }
        // Layer 3 (22)
        listOf(4, 3, 2, 2, 2, 3, 4).forEachIndexed { y, w ->
            val startX = (7 - w) / 2f
            for (i in 0 until w) {
                list.add(TileSlot(startX + i, y.toFloat(), 3))
            }
        }
        list.add(TileSlot(3.0f, 0.5f, 3))
        list.add(TileSlot(3.0f, 5.5f, 3))
        // Layer 4 & 5 (20 -> total 126)
        for (y in 0..6) {
            list.add(TileSlot(2.5f, y.toFloat(), 4))
            list.add(TileSlot(3.5f, y.toFloat(), 4))
        } // 14
        for (y in 1..4) {
            list.add(TileSlot(3.0f, y + 0.5f, 5))
        } // 4
        list.add(TileSlot(3.0f, 1.5f, 6))
        list.add(TileSlot(3.0f, 4.5f, 6))
        return list
    }

    // 132 tiles: Galaxy Spiral
    private fun buildSpiralSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..6) {
            for (x in 0..6) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        } // 49
        for (y in 0..5) {
            for (x in 0..5) {
                if ((x == 0 && y == 0) || (x == 5 && y == 5)) continue
                list.add(TileSlot(x + 0.5f, y + 0.5f, 1))
            }
        } // 34 (total 83)
        for (y in 1..4) {
            for (x in 1..4) {
                list.add(TileSlot(x + 0.25f, y + 0.25f, 2))
            }
        } // 16 (total 99)
        for (y in 1..4) {
            for (x in 2..4) {
                list.add(TileSlot(x.toFloat(), y + 0.5f, 3))
            }
        } // 12 (total 111)
        for (y in 2..4) {
            for (x in 2..4) {
                list.add(TileSlot(x + 0.5f, y.toFloat(), 4))
            }
        } // 9 (total 120)
        for (y in 2..3) {
            for (x in 2..4) {
                list.add(TileSlot(x.toFloat(), y + 0.5f, 5))
            }
        } // 6 (total 126)
        for (x in 2..4) {
            list.add(TileSlot(x.toFloat(), 3.0f, 6))
            list.add(TileSlot(x + 0.5f, 2.5f, 7))
        } // 6 (total 132)
        return list
    }

    // 144 tiles: Grand Imperial Palace (7 tiers)
    private fun buildImperialPalaceSlots(): List<TileSlot> {
        val list = mutableListOf<TileSlot>()
        for (y in 0..6) {
            for (x in 0..7) {
                list.add(TileSlot(x.toFloat(), y.toFloat(), 0))
            }
        } // 56
        for (y in 0..5) {
            for (x in 0..6) {
                list.add(TileSlot(x + 0.5f, y + 0.5f, 1))
            }
        } // 42 (total 98)
        for (y in 1..4) {
            for (x in 1..5) {
                list.add(TileSlot(x + 0.5f, y + 0.5f, 2))
            }
        } // 20 (total 118)
        for (y in 1..3) {
            for (x in 2..5) {
                list.add(TileSlot(x.toFloat(), y + 1.0f, 3))
            }
        } // 12 (total 130)
        for (y in 2..3) {
            for (x in 2..4) {
                list.add(TileSlot(x + 0.5f, y + 0.5f, 4))
            }
        } // 6 (total 136)
        for (x in 2..4) {
            list.add(TileSlot(x + 0.5f, 3.0f, 5))
        } // 3 (total 139)
        list.add(TileSlot(3.0f, 2.5f, 6))
        list.add(TileSlot(4.0f, 2.5f, 6))
        list.add(TileSlot(3.0f, 3.5f, 6))
        list.add(TileSlot(4.0f, 3.5f, 6))
        list.add(TileSlot(3.5f, 3.0f, 7)) // 5 (total 144)
        return list
    }
}
