package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.TileSuit
import com.example.model.TileType
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.CrimsonDragon
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkCharcoal
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.TileBlockedOverlay
import com.example.ui.theme.TileBorderStroke
import com.example.ui.theme.TileBottomJadeRim
import com.example.ui.theme.TileFaceIvory
import com.example.ui.theme.TileFaceTopHighlight
import com.example.ui.theme.TileSideBevel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun MahjongTileCard(
    type: TileType,
    isBlocked: Boolean,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    depthOffsetDp: Dp = 4.5.dp,
    onClick: (() -> Unit)? = null,
) {
    val density = LocalDensity.current
    val depthPx = with(density) { depthOffsetDp.toPx() }
    val cornerRadius = 8.dp

    val pulseAlpha = if (isHighlighted) {
        val transition = rememberInfiniteTransition(label = "hintPulse")
        val anim by transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(550),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "hintAlpha",
        )
        anim
    } else {
        0f
    }

    val stateDesc = if (isBlocked) "${type.displayNameVi}, đang bị che" else "${type.displayNameVi}, có thể chọn"

    Box(
        modifier = modifier
            .semantics { contentDescription = stateDesc }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        // 1. Jade bottom-back base of the 3D Mahjong tile
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset((depthPx * 0.45f).roundToInt(), depthPx.roundToInt()) }
                .shadow(elevation = if (isBlocked) 2.dp else 6.dp, shape = RoundedCornerShape(cornerRadius))
                .clip(RoundedCornerShape(cornerRadius))
                .background(TileBottomJadeRim),
        )

        // 2. Warm ivory side bevel layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset((depthPx * 0.22f).roundToInt(), (depthPx * 0.55f).roundToInt()) }
                .clip(RoundedCornerShape(cornerRadius))
                .background(TileSideBevel),
        )

        // 3. Main creamy ivory tile face
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(TileFaceTopHighlight, TileFaceIvory, Color(0xFFF3ECDC)),
                    ),
                )
                .border(
                    width = if (isHighlighted) 2.4.dp else 1.dp,
                    color = if (isHighlighted) GoldAccent.copy(alpha = pulseAlpha) else TileBorderStroke,
                    shape = RoundedCornerShape(cornerRadius),
                ),
            contentAlignment = Alignment.Center,
        ) {
            MahjongTileSymbol(
                type = type,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 3.dp, vertical = 3.dp),
            )

            // Blocked shadow overlay for covered tiles
            if (isBlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TileBlockedOverlay),
                )
            }

            // Highlight shimmer for hint
            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GoldAccent.copy(alpha = 0.18f * pulseAlpha)),
                )
            }
        }
    }
}

@Composable
fun MahjongTileSymbol(
    type: TileType,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val tileHeight = maxHeight
        val density = LocalDensity.current

        when (type.suit) {
            TileSuit.CHARACTERS -> {
                val numeral = when (type.rank) {
                    1 -> "一"
                    2 -> "二"
                    3 -> "三"
                    4 -> "四"
                    5 -> "五"
                    6 -> "六"
                    7 -> "七"
                    8 -> "八"
                    else -> "九"
                }
                val topSizeSp = with(density) { (tileHeight * 0.34f).toSp() }
                val wanSizeSp = with(density) { (tileHeight * 0.36f).toSp() }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = numeral,
                        color = InkCharcoal,
                        fontSize = topSizeSp,
                        lineHeight = topSizeSp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "萬",
                        color = CrimsonDragon,
                        fontSize = wanSizeSp,
                        lineHeight = wanSizeSp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            TileSuit.WINDS -> {
                val glyph = when (type) {
                    TileType.WIND_EAST -> "東"
                    TileType.WIND_SOUTH -> "南"
                    TileType.WIND_WEST -> "西"
                    else -> "北"
                }
                val glyphSp = with(density) { (tileHeight * 0.56f).toSp() }
                Text(
                    text = glyph,
                    color = InkCharcoal,
                    fontSize = glyphSp,
                    lineHeight = glyphSp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                )
            }

            TileSuit.DRAGONS -> {
                when (type) {
                    TileType.DRAGON_RED -> {
                        val glyphSp = with(density) { (tileHeight * 0.58f).toSp() }
                        Text(
                            text = "中",
                            color = CrimsonDragon,
                            fontSize = glyphSp,
                            lineHeight = glyphSp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                        )
                    }
                    TileType.DRAGON_GREEN -> {
                        val glyphSp = with(density) { (tileHeight * 0.56f).toSp() }
                        Text(
                            text = "發",
                            color = JadeGreen,
                            fontSize = glyphSp,
                            lineHeight = glyphSp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center,
                        )
                    }
                    else -> {
                        // White Dragon (Bạch Bản): classic royal blue double frame
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val padX = size.width * 0.16f
                            val padY = size.height * 0.14f
                            val w = size.width - padX * 2
                            val h = size.height - padY * 2
                            drawRoundRect(
                                color = CobaltBlue,
                                topLeft = Offset(padX, padY),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
                                style = Stroke(width = (size.minDimension * 0.085f).coerceAtLeast(2f)),
                            )
                            val innerPad = size.minDimension * 0.09f
                            drawRoundRect(
                                color = CobaltBlue.copy(alpha = 0.72f),
                                topLeft = Offset(padX + innerPad, padY + innerPad),
                                size = Size((w - innerPad * 2).coerceAtLeast(1f), (h - innerPad * 2).coerceAtLeast(1f)),
                                cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                                style = Stroke(width = (size.minDimension * 0.038f).coerceAtLeast(1f)),
                            )
                        }
                    }
                }
            }

            TileSuit.DOTS -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawDotsTile(type.rank)
                }
            }

            TileSuit.BAMBOOS -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawBambooTile(type.rank)
                }
            }
        }
    }
}

private fun DrawScope.drawDotCircle(
    center: Offset,
    radius: Float,
    outerColor: Color,
    innerColor: Color = Color.White,
) {
    drawCircle(color = outerColor, radius = radius, center = center)
    drawCircle(
        color = innerColor,
        radius = radius * 0.62f,
        center = center,
        style = Stroke(width = (radius * 0.22f).coerceAtLeast(1.1f)),
    )
    drawCircle(color = outerColor, radius = radius * 0.24f, center = center)
}

private fun DrawScope.drawDotsTile(rank: Int) {
    val w = size.width
    val h = size.height
    when (rank) {
        1 -> {
            // Ornate large 1-Dot medallion
            val c = Offset(w / 2f, h / 2f)
            val r = size.minDimension * 0.40f
            drawCircle(color = JadeGreen, radius = r, center = c)
            for (i in 0 until 8) {
                val angle = (i * PI / 4.0).toFloat()
                val petal = Offset(
                    c.x + cos(angle) * r * 0.62f,
                    c.y + sin(angle) * r * 0.62f,
                )
                drawCircle(color = TileFaceIvory, radius = r * 0.16f, center = petal)
            }
            drawCircle(color = CrimsonDragon, radius = r * 0.42f, center = c)
            drawCircle(color = TileFaceIvory, radius = r * 0.22f, center = c)
            drawCircle(color = CobaltBlue, radius = r * 0.11f, center = c)
        }
        2 -> {
            val r = w * 0.22f
            drawDotCircle(Offset(w * 0.5f, h * 0.29f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.5f, h * 0.71f), r, CobaltBlue)
        }
        3 -> {
            val r = w * 0.17f
            drawDotCircle(Offset(w * 0.27f, h * 0.24f), r, CobaltBlue)
            drawDotCircle(Offset(w * 0.50f, h * 0.50f), r, CrimsonDragon)
            drawDotCircle(Offset(w * 0.73f, h * 0.76f), r, JadeGreen)
        }
        4 -> {
            val r = w * 0.185f
            drawDotCircle(Offset(w * 0.30f, h * 0.30f), r, CobaltBlue)
            drawDotCircle(Offset(w * 0.70f, h * 0.30f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.30f, h * 0.70f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.70f, h * 0.70f), r, CobaltBlue)
        }
        5 -> {
            val r = w * 0.155f
            drawDotCircle(Offset(w * 0.27f, h * 0.25f), r, CobaltBlue)
            drawDotCircle(Offset(w * 0.73f, h * 0.25f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.50f, h * 0.50f), r, CrimsonDragon)
            drawDotCircle(Offset(w * 0.27f, h * 0.75f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.73f, h * 0.75f), r, CobaltBlue)
        }
        6 -> {
            val r = w * 0.155f
            drawDotCircle(Offset(w * 0.31f, h * 0.21f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.69f, h * 0.21f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.31f, h * 0.54f), r, CrimsonDragon)
            drawDotCircle(Offset(w * 0.69f, h * 0.54f), r, CrimsonDragon)
            drawDotCircle(Offset(w * 0.31f, h * 0.80f), r, CrimsonDragon)
            drawDotCircle(Offset(w * 0.69f, h * 0.80f), r, CrimsonDragon)
        }
        7 -> {
            val r = w * 0.135f
            drawDotCircle(Offset(w * 0.25f, h * 0.17f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.50f, h * 0.27f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.75f, h * 0.37f), r, JadeGreen)
            drawDotCircle(Offset(w * 0.31f, h * 0.59f), r, CrimsonDragon)
            drawDotCircle(Offset(w * 0.69f, h * 0.59f), r, CrimsonDragon)
            drawDotCircle(Offset(w * 0.31f, h * 0.82f), r, CrimsonDragon)
            drawDotCircle(Offset(w * 0.69f, h * 0.82f), r, CrimsonDragon)
        }
        8 -> {
            val r = w * 0.145f
            val ys = listOf(0.17f, 0.39f, 0.61f, 0.83f)
            ys.forEach { y ->
                drawDotCircle(Offset(w * 0.31f, h * y), r, CobaltBlue)
                drawDotCircle(Offset(w * 0.69f, h * y), r, CobaltBlue)
            }
        }
        else -> {
            val r = w * 0.128f
            val xs = listOf(0.24f, 0.50f, 0.76f)
            val ys = listOf(0.20f, 0.50f, 0.80f)
            val rowColors = listOf(JadeGreen, CrimsonDragon, CobaltBlue)
            ys.forEachIndexed { rowIdx, y ->
                xs.forEach { x ->
                    drawDotCircle(Offset(w * x, h * y), r, rowColors[rowIdx])
                }
            }
        }
    }
}

private fun DrawScope.drawBambooStalk(
    topCenter: Offset,
    stalkHeight: Float,
    stalkWidth: Float,
    color: Color,
) {
    val halfW = stalkWidth / 2f
    val top = topCenter.y
    val bottom = top + stalkHeight
    // Main shaft
    drawLine(
        color = color,
        start = Offset(topCenter.x, top + halfW * 0.5f),
        end = Offset(topCenter.x, bottom - halfW * 0.5f),
        strokeWidth = stalkWidth,
        cap = StrokeCap.Round,
    )
    // Inner highlight line
    drawLine(
        color = Color.White.copy(alpha = 0.45f),
        start = Offset(topCenter.x, top + stalkHeight * 0.15f),
        end = Offset(topCenter.x, bottom - stalkHeight * 0.15f),
        strokeWidth = (stalkWidth * 0.25f).coerceAtLeast(1f),
        cap = StrokeCap.Round,
    )
    // Bamboo joint rings (top, mid, bottom)
    val jointColor = color
    val jointSpan = stalkWidth * 0.85f
    listOf(top + stalkHeight * 0.12f, top + stalkHeight * 0.5f, bottom - stalkHeight * 0.12f).forEach { jy ->
        drawLine(
            color = jointColor,
            start = Offset(topCenter.x - jointSpan, jy),
            end = Offset(topCenter.x + jointSpan, jy),
            strokeWidth = (stalkWidth * 0.36f).coerceAtLeast(1.2f),
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawBambooTile(rank: Int) {
    val w = size.width
    val h = size.height
    when (rank) {
        1 -> {
            // Stylized Jade Peacock / Bird (matching the green bird tile in the video)
            val cx = w * 0.5f
            val cy = h * 0.52f
            val bodyPath = Path().apply {
                moveTo(cx - w * 0.12f, cy - h * 0.25f)
                cubicTo(
                    cx + w * 0.22f, cy - h * 0.28f,
                    cx + w * 0.26f, cy + h * 0.06f,
                    cx - w * 0.04f, cy + h * 0.18f,
                )
                lineTo(cx - w * 0.24f, cy + h * 0.30f)
                lineTo(cx - w * 0.10f, cy + h * 0.04f)
                close()
            }
            drawPath(path = bodyPath, color = JadeGreen)
            // Wing accent in crimson
            val wingPath = Path().apply {
                moveTo(cx - w * 0.04f, cy - h * 0.06f)
                quadraticTo(cx + w * 0.24f, cy - h * 0.02f, cx + w * 0.14f, cy + h * 0.20f)
                quadraticTo(cx, cy + h * 0.08f, cx - w * 0.04f, cy - h * 0.06f)
            }
            drawPath(path = wingPath, color = CrimsonDragon)
            // Head & beak
            drawCircle(color = JadeGreen, radius = w * 0.10f, center = Offset(cx - w * 0.05f, cy - h * 0.24f))
            drawCircle(color = TileFaceIvory, radius = w * 0.032f, center = Offset(cx - w * 0.07f, cy - h * 0.25f))
            // Tail feathers
            drawLine(
                color = CobaltBlue,
                start = Offset(cx - w * 0.05f, cy + h * 0.12f),
                end = Offset(cx + w * 0.18f, cy + h * 0.32f),
                strokeWidth = w * 0.06f,
                cap = StrokeCap.Round,
            )
            // Crest
            drawPath(
                path = Path().apply {
                    moveTo(cx - w * 0.05f, cy - h * 0.32f)
                    lineTo(cx + w * 0.06f, cy - h * 0.38f)
                    lineTo(cx + w * 0.03f, cy - h * 0.28f)
                },
                color = CrimsonDragon,
                style = Stroke(width = w * 0.045f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        2 -> {
            val sw = w * 0.12f
            val sh = h * 0.34f
            drawBambooStalk(Offset(w * 0.5f, h * 0.12f), sh, sw, CobaltBlue)
            drawBambooStalk(Offset(w * 0.5f, h * 0.54f), sh, sw, JadeGreen)
        }
        3 -> {
            val sw = w * 0.11f
            val sh = h * 0.33f
            drawBambooStalk(Offset(w * 0.5f, h * 0.12f), sh, sw, CobaltBlue)
            drawBambooStalk(Offset(w * 0.31f, h * 0.54f), sh, sw, JadeGreen)
            drawBambooStalk(Offset(w * 0.69f, h * 0.54f), sh, sw, JadeGreen)
        }
        4 -> {
            val sw = w * 0.11f
            val sh = h * 0.33f
            drawBambooStalk(Offset(w * 0.32f, h * 0.12f), sh, sw, CobaltBlue)
            drawBambooStalk(Offset(w * 0.68f, h * 0.12f), sh, sw, JadeGreen)
            drawBambooStalk(Offset(w * 0.32f, h * 0.54f), sh, sw, JadeGreen)
            drawBambooStalk(Offset(w * 0.68f, h * 0.54f), sh, sw, CobaltBlue)
        }
        5 -> {
            val sw = w * 0.10f
            val sh = h * 0.32f
            drawBambooStalk(Offset(w * 0.28f, h * 0.12f), sh, sw, JadeGreen)
            drawBambooStalk(Offset(w * 0.72f, h * 0.12f), sh, sw, CobaltBlue)
            drawBambooStalk(Offset(w * 0.50f, h * 0.34f), sh, sw, CrimsonDragon)
            drawBambooStalk(Offset(w * 0.28f, h * 0.56f), sh, sw, CobaltBlue)
            drawBambooStalk(Offset(w * 0.72f, h * 0.56f), sh, sw, JadeGreen)
        }
        6 -> {
            val sw = w * 0.10f
            val sh = h * 0.32f
            val xs = listOf(0.26f, 0.50f, 0.74f)
            xs.forEach { x ->
                drawBambooStalk(Offset(w * x, h * 0.13f), sh, sw, JadeGreen)
                drawBambooStalk(Offset(w * x, h * 0.55f), sh, sw, CobaltBlue)
            }
        }
        7 -> {
            val sw = w * 0.095f
            val sh = h * 0.24f
            drawBambooStalk(Offset(w * 0.50f, h * 0.09f), sh, sw, CrimsonDragon)
            val xs = listOf(0.26f, 0.50f, 0.74f)
            xs.forEach { x ->
                drawBambooStalk(Offset(w * x, h * 0.39f), sh, sw, JadeGreen)
                drawBambooStalk(Offset(w * x, h * 0.67f), sh, sw, CobaltBlue)
            }
        }
        8 -> {
            val sw = w * 0.095f
            val sh = h * 0.29f
            // Upper 4
            drawBambooStalk(Offset(w * 0.25f, h * 0.12f), sh, sw, JadeGreen)
            drawBambooStalk(Offset(w * 0.42f, h * 0.15f), sh * 0.88f, sw, JadeGreen)
            drawBambooStalk(Offset(w * 0.58f, h * 0.15f), sh * 0.88f, sw, JadeGreen)
            drawBambooStalk(Offset(w * 0.75f, h * 0.12f), sh, sw, JadeGreen)
            // Lower 4
            drawBambooStalk(Offset(w * 0.25f, h * 0.56f), sh, sw, CobaltBlue)
            drawBambooStalk(Offset(w * 0.42f, h * 0.56f), sh * 0.88f, sw, CrimsonDragon)
            drawBambooStalk(Offset(w * 0.58f, h * 0.56f), sh * 0.88f, sw, CrimsonDragon)
            drawBambooStalk(Offset(w * 0.75f, h * 0.56f), sh, sw, CobaltBlue)
        }
        else -> {
            val sw = w * 0.092f
            val sh = h * 0.23f
            val xs = listOf(0.25f, 0.50f, 0.75f)
            val colors = listOf(CrimsonDragon, CobaltBlue, JadeGreen)
            xs.forEachIndexed { idx, x ->
                drawBambooStalk(Offset(w * x, h * 0.10f), sh, sw, colors[idx])
                drawBambooStalk(Offset(w * x, h * 0.38f), sh, sw, colors[idx])
                drawBambooStalk(Offset(w * x, h * 0.66f), sh, sw, colors[idx])
            }
        }
    }
}
