package com.example.spotted.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotted.data.model.Post
import com.example.spotted.data.view.FeedViewModel
import com.example.spotted.ui.components.SwipeCardStack
import com.example.spotted.ui.components.rememberSwipeCardController
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@Composable
fun FeedScreen(
    innerPadding: PaddingValues
) {
    val viewModel : FeedViewModel = viewModel()
    val spots by viewModel.posts.collectAsState()
    val controller = rememberSwipeCardController()

    // Box occupa tutto lo spazio assegnato dal NavHost (già fillMaxSize dal
    // Scaffold della MainActivity), con le card che vanno dietro le barre glass.
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(10.dp)
        .padding(bottom = 10.dp)
    ){

        SwipeCardStack(
            items         = spots,
            onSwipedRight = { viewModel.swipeRight(it.id) },
            onSwipedLeft  = { viewModel.swipeLeft(it.id) },
            controller    = controller,
            modifier      = Modifier.fillMaxSize(),
            emptyContent  = { EmptyFeedState() },
        ) { spot, swipeProgress ->
            SpotCard(spot = spot, swipeProgress = swipeProgress)

        }

    }
}

// ─── SpotCard ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTime::class)
@Composable
fun SpotCard(
    spot:          Post,
    swipeProgress: Float,
    modifier:      Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(
                when(spot.category % 3){
                    0 -> MaterialTheme.colorScheme.primary
                    1 -> MaterialTheme.colorScheme.secondary
                    2 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.surfaceTint
                }
            ),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp).fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text       = "${spot.category}",
                        color      = Color.White,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val elapsed = spot.timestamp?.let { (Clock.System.now() - it)}
                    Text(elapsed.toString(),                  color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)

                }
            }

            Column(
                modifier.fillMaxSize().align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center
            ) {

                spot.title?.let {
                    Text(
                        text       = it,
                        color      = Color.White,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp,
                    )
                }

                Spacer(Modifier.height(4.dp))

                spot.description?.let {
                    Text(
                        text       = it,
                        color      = Color.White.copy(alpha = 0.88f),
                        fontSize   = 14.sp,
                        lineHeight = 20.sp,
                    )
                }
            }

        }

        SwipeStamp(
            icon = Icons.Filled.Close,
            description     = "Follow",
            color    = MaterialTheme.colorScheme.inverseOnSurface,
            alpha    = swipeProgress.coerceAtLeast(0f),
            rotation = -8f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 24.dp),
        )

        SwipeStamp(
            icon = Icons.Filled.Favorite,
            description     = "Dismiss",
            color    = MaterialTheme.colorScheme.tertiary,
            alpha    = (-swipeProgress).coerceAtLeast(0f),
            rotation = 8f,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp),
        )
    }
}

// ─── SwipeStamp ───────────────────────────────────────────────────────────────

@Composable
fun SwipeStamp(
    icon:     ImageVector,
    description: String,
    color:    Color,
    alpha:    Float,
    rotation: Float,
    modifier: Modifier = Modifier,
) {
    val animAlpha by animateFloatAsState(targetValue = alpha, label = "stampAlpha")
    Icon(
        icon,
        description,
        modifier = modifier
            .alpha(animAlpha)
            .graphicsLayer { rotationZ = rotation }
            .border(width = 3.dp, color, CircleShape)
            .padding(horizontal = 7.dp, vertical = 7.dp),
        tint = color,
        )
}

// ─── SwipeButtons ─────────────────────────────────────────────────────────────

@Composable
fun SwipeButtons(
    onDismiss: () -> Unit,
    onFollow:  () -> Unit,
    modifier:  Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(40.dp),
        verticalAlignment     = Alignment.Bottom,
    ) {
        FilledIconButton(
            onClick  = onDismiss,
            modifier = Modifier.size(60.dp),
            colors   = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFFFEE2E2),
                contentColor   = Color(0xFFEF4444),
            ),
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Close,"Dismiss",modifier.size(30.dp))
        }

        FilledIconButton(
            onClick  = onFollow,
            modifier = Modifier.size(60.dp),
            colors   = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFFDCFCE7),
                contentColor   = Color(0xFF22C55E),
            ),
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Favorite,"Follow",modifier.size(30.dp))
        }
    }
}

// ─── EmptyFeedState ───────────────────────────────────────────────────────────

@Composable
fun EmptyFeedState() {
    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎓", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text  = "Nessun altro spot per ora",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "Torna più tardi per scoprire nuovi spot",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}