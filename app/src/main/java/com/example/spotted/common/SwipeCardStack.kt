package com.example.spotted.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign



private const val SWIPE_THRESHOLD_FRACTION  = 0.38f  // % larghezza schermo
private const val VELOCITY_THRESHOLD        = 900f   // px/s per swipe veloce
private const val MAX_ROTATION_DEG          = 12f    // rotazione massima card
private const val FLY_OFF_MULTIPLIER        = 1.6f   // moltiplicatore fly-off



/**
 * Permette di triggerare lo swipe da pulsanti esterni (es. ♥ e ✕ nella UI).
 * Usa [rememberSwipeCardController] per crearlo nel composable padre.
 */
class SwipeCardController {
    internal var triggerLeft:  (suspend () -> Unit)? = null
    internal var triggerRight: (suspend () -> Unit)? = null

    /** Swipe verso sinistra (PASSA) via codice. */
    suspend fun swipeLeft()  { triggerLeft?.invoke() }

    /** Swipe verso destra (SEGUI) via codice. */
    suspend fun swipeRight() { triggerRight?.invoke() }
}

@Composable
fun rememberSwipeCardController() = remember { SwipeCardController() }

// ─── SwipeCardStack ───────────────────────────────────────────────────────────

/**
 * Stack di card con swipe Tinder-like.
 *
 * @param items          Lista degli elementi da mostrare.
 * @param onSwipedLeft   Callback quando l'utente swipa a sinistra (PASSA).
 * @param onSwipedRight  Callback quando l'utente swipa a destra (SEGUI).
 * @param controller     [SwipeCardController] opzionale per swipe programmatico.
 * @param emptyContent   Composable mostrato quando non ci sono più card.
 * @param cardContent    Composable del contenuto della singola card.
 *                       Riceve l'item e [swipeProgress] ∈ [-1, 1]:
 *                         -1 = completamente a sinistra
 *                          0 = centro
 *                         +1 = completamente a destra
 *                       Usalo per mostrare i timbri SEGUI/PASSA.
 */
@Composable
fun <T> SwipeCardStack(
    items: List<T>,
    onSwipedLeft:  (T) -> Unit,
    onSwipedRight: (T) -> Unit,
    modifier:      Modifier = Modifier,
    controller:    SwipeCardController? = null,
    emptyContent:  @Composable () -> Unit = {},
    cardContent:   @Composable BoxScope.(item: T, swipeProgress: Float) -> Unit,
) {
    val scope   = rememberCoroutineScope()
    val density = LocalDensity.current
    val config  = LocalConfiguration.current

    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val threshold     = screenWidthPx * SWIPE_THRESHOLD_FRACTION

    // Indice della card in cima allo stack
    var topIndex by remember(items) { mutableIntStateOf(0) }

    // Offset della card in cima
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    // Come la card si stacca
    suspend fun flyOff(dir: Float, item: T) {
        coroutineScope {
            launch { offsetX.animateTo(screenWidthPx * FLY_OFF_MULTIPLIER * dir, tween(320)) }
            launch { offsetY.animateTo(offsetY.value + 80f, tween(320)) }
        }
        if (dir > 0f) onSwipedRight(item) else onSwipedLeft(item)
        topIndex++
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
    }

    suspend fun snapBack() {
        coroutineScope {
            launch {
                offsetX.animateTo(
                    targetValue   = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow,
                    )
                )
            }
            launch {
                offsetY.animateTo(
                    targetValue   = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessLow,
                    )
                )
            }
        }
    }

    // ── Registra i trigger nel controller ────────────────────────────────────

    if (controller != null && topIndex < items.size) {
        controller.triggerLeft  = { flyOff(-1f, items[topIndex]) }
        controller.triggerRight = { flyOff(+1f, items[topIndex]) }
    }

    // ── Empty state ──────────────────────────────────────────────────────────

    if (topIndex >= items.size) {
        emptyContent()
        return
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    val swipeProgress = (offsetX.value / threshold).coerceIn(-1f, 1f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {

        // Card dietro (al massimo 2)
        for (i in minOf(topIndex + 2, items.lastIndex) downTo topIndex + 1) {
            val stackPos = i - topIndex          // 1 o 2
            val drag     = (abs(offsetX.value) / threshold).coerceIn(0f, 1f)
            val scale    = 1f - stackPos * 0.05f + drag * stackPos * 0.05f
            val ty       = stackPos * 20f * (1f - drag * 0.5f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX       = scale
                        scaleY       = scale
                        translationY = ty
                    }
            ) {
                cardContent(items[i], 0f)
            }
        }

        // Top card
        val velocityTracker = remember { VelocityTracker() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                    translationY = offsetY.value
                    rotationZ    = swipeProgress * MAX_ROTATION_DEG
                }
                .pointerInput(topIndex) {
                    detectDragGestures(
                        onDragStart  = { velocityTracker.resetTracking() },
                        onDragCancel = { scope.launch { snapBack() } },
                        onDragEnd    = {
                            val vel = velocityTracker.calculateVelocity()
                            scope.launch {
                                val byDistance = abs(offsetX.value) > threshold
                                val byVelocity = abs(vel.x) > VELOCITY_THRESHOLD
                                if (byDistance || byVelocity) {
                                    val dir = sign(
                                        if (byVelocity && !byDistance) vel.x else offsetX.value
                                    )
                                    flyOff(dir, items[topIndex])
                                } else {
                                    snapBack()
                                }
                            }
                        },
                        onDrag = { change, delta ->
                            change.consume()
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            scope.launch {
                                offsetX.snapTo(offsetX.value + delta.x)
                                offsetY.snapTo(offsetY.value + delta.y)
                            }
                        },
                    )
                }
        ) {
            cardContent(items[topIndex], swipeProgress)
        }
    }
}
