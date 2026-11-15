package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.FloatingPopup
import com.example.model.SparkleBurst
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.CobaltBlue
import com.example.ui.theme.CrimsonDragon
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SurfaceDarkJade
import com.example.ui.theme.TileFaceIvory
import com.example.ui.theme.TrayBorderNeon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun TraySparkleOverlay(
    bursts: List<SparkleBurst>,
    popups: List<FloatingPopup>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        bursts.forEach { burst ->
            SparkleBurstEffect(
                burst = burst,
                modifier = Modifier.fillMaxSize(),
            )
        }
        popups.forEach { popup ->
            FloatingScoreLabel(
                popup = popup,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

private data class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val gravity: Float,
    val widthDp: Float,
    val heightDp: Float,
    val color: Color,
    val spinSpeed: Float,
    val initialPhase: Float,
    val swayAmplitude: Float,
    val swayFreq: Float,
    val shapeType: Int, // 0 = ribbon rect, 1 = star, 2 = diamond
)

@Composable
fun VictoryCelebrationOverlay(
    levelNumber: Int,
    timeBonus: Int,
    showBanner: Boolean,
    modifier: Modifier = Modifier,
) {
    val confettiPieces = remember(levelNumber) {
        val rng = Random(levelNumber * 4099 + 17)
        val palette = listOf(
            GoldAccent,
            TrayBorderNeon,
            ActionOrange,
            Color.White,
            Color(0xFF60A5FA),
            CrimsonDragon,
            Color(0xFFFDE047),
        )
        List(96) { idx ->
            val source = idx % 3
            val startX: Float
            val startY: Float
            val vx: Float
            val vy: Float
            when (source) {
                0 -> {
                    // Left celebratory cannon
                    startX = 0.08f + rng.nextFloat() * 0.14f
                    startY = 0.78f + rng.nextFloat() * 0.12f
                    vx = 0.16f + rng.nextFloat() * 0.44f
                    vy = -(0.85f + rng.nextFloat() * 0.65f)
                }
                1 -> {
                    // Right celebratory cannon
                    startX = 0.92f - rng.nextFloat() * 0.14f
                    startY = 0.78f + rng.nextFloat() * 0.12f
                    vx = -(0.16f + rng.nextFloat() * 0.44f)
                    vy = -(0.85f + rng.nextFloat() * 0.65f)
                }
                else -> {
                    // Center sky shower
                    startX = 0.12f + rng.nextFloat() * 0.76f
                    startY = -0.04f - rng.nextFloat() * 0.25f
                    vx = (rng.nextFloat() - 0.5f) * 0.24f
                    vy = 0.15f + rng.nextFloat() * 0.28f
                }
            }
            ConfettiPiece(
                startX = startX,
                startY = startY,
                vx = vx,
                vy = vy,
                gravity = 0.68f + rng.nextFloat() * 0.36f,
                widthDp = 6f + rng.nextFloat() * 6f,
                heightDp = 10f + rng.nextFloat() * 8f,
                color = palette[idx % palette.size],
                spinSpeed = (if (idx % 2 == 0) 1f else -1f) * (3.5f + rng.nextFloat() * 6f),
                initialPhase = rng.nextFloat() * (2f * PI).toFloat(),
                swayAmplitude = 0.015f + rng.nextFloat() * 0.03f,
                swayFreq = 2.5f + rng.nextFloat() * 3.0f,
                shapeType = idx % 4,
            )
        }
    }

    var elapsedSec by remember(levelNumber) { mutableFloatStateOf(0f) }
    val ringProgress = remember(levelNumber) { Animatable(0f) }

    LaunchedEffect(levelNumber) {
        ringProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(levelNumber) {
        val startNanos = withFrameNanos { it }
        while (elapsedSec < 5.5f) {
            withFrameNanos { now ->
                elapsedSec = (now - startNanos) / 1_000_000_000f
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Expanding golden-jade radial victory shockwave
            val rp = ringProgress.value
            if (rp < 1f) {
                val maxR = size.maxDimension * 0.65f
                val alpha = (1f - rp).coerceIn(0f, 1f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = 0.30f * alpha),
                            TrayBorderNeon.copy(alpha = 0.14f * alpha),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = (maxR * (0.25f + rp * 0.75f)).coerceAtLeast(1f),
                    ),
                    radius = maxR * (0.25f + rp * 0.75f),
                    center = center,
                )
                drawCircle(
                    color = GoldAccent.copy(alpha = 0.65f * alpha),
                    radius = maxR * rp * 0.72f,
                    center = center,
                    style = Stroke(width = (8.dp.toPx() * (1f - rp * 0.7f)).coerceAtLeast(1f)),
                )
            }

            // 2. Confetti physics simulation
            val t = elapsedSec
            confettiPieces.forEach { piece ->
                val xNorm = piece.startX + piece.vx * t +
                    sin(t * piece.swayFreq + piece.initialPhase) * piece.swayAmplitude
                val yNorm = piece.startY + piece.vy * t + 0.5f * piece.gravity * t * t

                if (yNorm in -0.3f..1.18f && xNorm in -0.15f..1.15f) {
                    val px = xNorm * size.width
                    val py = yNorm * size.height
                    val fadeOut = if (t > 3.8f) ((5.5f - t) / 1.7f).coerceIn(0f, 1f) else 1f
                    val flipScale = cos(t * piece.spinSpeed + piece.initialPhase)
                    val rotDeg = (t * piece.spinSpeed * 55f) % 360f
                    val wPx = piece.widthDp.dp.toPx()
                    val hPx = piece.heightDp.dp.toPx()

                    withTransform({
                        translate(left = px, top = py)
                        rotate(degrees = rotDeg, pivot = Offset.Zero)
                        scale(scaleX = 1f, scaleY = flipScale.coerceIn(-1f, 1f), pivot = Offset.Zero)
                    }) {
                        when (piece.shapeType) {
                            1 -> drawConfettiStar(r = wPx * 0.85f, color = piece.color.copy(alpha = fadeOut))
                            2 -> drawConfettiDiamond(w = wPx * 1.15f, h = hPx * 0.9f, color = piece.color.copy(alpha = fadeOut))
                            else -> drawRect(
                                color = piece.color.copy(alpha = fadeOut),
                                topLeft = Offset(-wPx / 2f, -hPx / 2f),
                                size = Size(wPx, hPx),
                            )
                        }
                    }
                }
            }
        }

        // 3. Center celebratory banner visible during the zoomed-out pause before VictoryDialog
        AnimatedVisibility(
            visible = showBanner,
            enter = scaleIn(
                initialScale = 0.55f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            ) + fadeIn(tween(240)),
            exit = scaleOut(targetScale = 1.08f, animationSpec = tween(200)) + fadeOut(tween(180)),
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SurfaceDarkJade.copy(alpha = 0.94f),
                                Color(0xFF062012).copy(alpha = 0.96f),
                            ),
                        ),
                    )
                    .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
                    .padding(horizontal = 28.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.celebration_board_cleared),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GoldAccent,
                )
                if (timeBonus > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.celebration_time_bonus, timeBonus),
                        style = MaterialTheme.typography.titleMedium,
                        color = TrayBorderNeon,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawConfettiStar(r: Float, color: Color) {
    val path = Path().apply {
        moveTo(0f, -r)
        lineTo(r * 0.32f, -r * 0.32f)
        lineTo(r, 0f)
        lineTo(r * 0.32f, r * 0.32f)
        lineTo(0f, r)
        lineTo(-r * 0.32f, r * 0.32f)
        lineTo(-r, 0f)
        lineTo(-r * 0.32f, -r * 0.32f)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawConfettiDiamond(w: Float, h: Float, color: Color) {
    val path = Path().apply {
        moveTo(0f, -h / 2f)
        lineTo(w / 2f, 0f)
        lineTo(0f, h / 2f)
        lineTo(-w / 2f, 0f)
        close()
    }
    drawPath(path, color = color)
}

private data class ParticleVec(
    val angleRad: Float,
    val speedFactor: Float,
    val radiusDp: Float,
    val color: Color,
    val isStar: Boolean,
)

@Composable
private fun SparkleBurstEffect(
    burst: SparkleBurst,
    modifier: Modifier = Modifier,
) {
    val progress = remember(burst.id) { Animatable(0f) }
    val particles = remember(burst.id) {
        val rng = Random(burst.seed * 7919)
        val palette = listOf(Color.White, GoldAccent, TrayBorderNeon, TileFaceIvory, Color(0xFFFFD86B))
        List(26) { idx ->
            ParticleVec(
                angleRad = (rng.nextFloat() * 2f * PI).toFloat(),
                speedFactor = 0.35f + rng.nextFloat() * 0.85f,
                radiusDp = 3.5f + rng.nextFloat() * 5.5f,
                color = palette[idx % palette.size],
                isStar = idx % 3 == 0,
            )
        }
    }

    LaunchedEffect(burst.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing),
        )
    }

    Canvas(modifier = modifier) {
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)
        val originX = size.width * burst.slotCenterFraction
        val originY = size.height * 0.55f
        val maxSpread = size.width * 0.28f

        // Flash halo
        if (p < 0.45f) {
            val haloAlpha = (1f - p / 0.45f) * 0.65f
            drawCircle(
                color = Color.White.copy(alpha = haloAlpha),
                radius = maxSpread * 0.48f * (0.3f + p),
                center = Offset(originX, originY),
            )
        }

        particles.forEach { vec ->
            val dist = maxSpread * vec.speedFactor * p
            val gravityOffset = p * p * 34.dp.toPx()
            val px = originX + cos(vec.angleRad) * dist
            val py = originY + sin(vec.angleRad) * dist * 0.72f + gravityOffset - p * 18.dp.toPx()
            val r = vec.radiusDp.dp.toPx() * (1f - p * 0.45f)

            if (vec.isStar) {
                val starPath = Path().apply {
                    moveTo(px, py - r * 1.4f)
                    lineTo(px + r * 0.45f, py - r * 0.45f)
                    lineTo(px + r * 1.4f, py)
                    lineTo(px + r * 0.45f, py + r * 0.45f)
                    lineTo(px, py + r * 1.4f)
                    lineTo(px - r * 0.45f, py + r * 0.45f)
                    lineTo(px - r * 1.4f, py)
                    lineTo(px - r * 0.45f, py - r * 0.45f)
                    close()
                }
                drawPath(starPath, color = vec.color.copy(alpha = alpha))
            } else {
                drawCircle(
                    color = vec.color.copy(alpha = alpha),
                    radius = r,
                    center = Offset(px, py),
                )
            }
        }
    }
}

@Composable
private fun FloatingScoreLabel(
    popup: FloatingPopup,
    modifier: Modifier = Modifier,
) {
    val anim = remember(popup.id) { Animatable(0f) }
    LaunchedEffect(popup.id) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 820, easing = FastOutSlowInEasing),
        )
    }

    val p = anim.value
    val yOffsetPx = (-p * 90f).roundToInt()
    val alpha = if (p < 0.7f) 1f else ((1f - p) / 0.3f).coerceIn(0f, 1f)

    Text(
        text = popup.text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = if (popup.isCombo) 23.sp else 19.sp,
            fontWeight = FontWeight.ExtraBold,
        ),
        color = (if (popup.isCombo) GoldAccent else Color.White).copy(alpha = alpha),
        modifier = modifier.offset { IntOffset(0, yOffsetPx) },
    )
}
