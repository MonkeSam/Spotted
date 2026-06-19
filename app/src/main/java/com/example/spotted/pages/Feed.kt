package com.example.spotted.ui.screens

import android.graphics.drawable.Icon
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotted.ui.components.SwipeCardStack
import com.example.spotted.ui.components.rememberSwipeCardController
import kotlinx.coroutines.launch

// ─── Modello dati ─────────────────────────────────────────────────────────────

data class Spot(
    val id: Int,
    val category: SpotCategory,
    val title: String,
    val description: String,
    val authorLabel: String = "Anonimo",
    val timeAgo: String,
    val followerCount: Int,
)

enum class SpotCategory(val label: String, val emoji: String ) {
    EVENT ("Evento",  "🎉"),
    PLACE ("Luogo",   "📍"),
    PERSON("Persona", "👤"),
    OBJECT("Oggetto", "📦"),
    OTHER ("Altro",   "❓"),
}
// ─── Dati di esempio ─────────────────────────────────────────────────────────
// TODO: sostituire con ViewModel + repository

private val sampleSpots = listOf(
    Spot(1, SpotCategory.EVENT,  "Aperitivo in Piazza Verdi",   "Cerchiamo gente stasera alle 19, siamo già in 3!", timeAgo = "12 min fa", followerCount = 47),
    Spot(2, SpotCategory.PLACE,  "Tavolo libero Aula C",        "Secondo piano vicino alla finestra, posti da 4.",  timeAgo = "5 min fa",  followerCount = 23),
    Spot(3, SpotCategory.PERSON, "Ragazza giacca rossa",        "Era in coda alla macchinetta del caffè al DISI.", timeAgo = "34 min fa", followerCount = 89),
    Spot(4, SpotCategory.OBJECT, "Chiavi trovate in Navile",    "Portachiavi azzurro lasciato alla reception.",     timeAgo = "1h fa",     followerCount = 12),
    Spot(5, SpotCategory.EVENT,  "Studio di gruppo Statistica", "Chi viene? Biblioteca centrale ore 15.",           timeAgo = "8 min fa",  followerCount = 31),
)

// ─── FeedScreen ───────────────────────────────────────────────────────────────
//
// NON ha Scaffold proprio: usa quello della MainActivity.
// NON riceve innerPadding: la MainActivity ignora il padding per l'edge-to-edge.
//
// Il parametro [onSpotFollowed] corrisponde al lambda { } passato in NavGraph:
//   composable<NavigationRoute.Feed> { FeedScreen() { } }
//
// I bottoni swipe flottano sopra la SectionsBar glass tramite WindowInsets.

@Composable
fun FeedScreen(
    onSpotFollowed: (Spot) -> Unit = {},
    innerPadding: PaddingValues
) {
    val scope      = rememberCoroutineScope()
    val controller = rememberSwipeCardController()
    val spots      = remember { sampleSpots }

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
            onSwipedRight = onSpotFollowed,
            onSwipedLeft  = { /* spot ignorato — nessuna azione */ },
            controller    = controller,
            modifier      = Modifier.fillMaxSize(),
            emptyContent  = { EmptyFeedState() },
        ) { spot, swipeProgress ->
            SpotCard(spot = spot, swipeProgress = swipeProgress)

        }

    }
}

// ─── SpotCard ─────────────────────────────────────────────────────────────────

@Composable
fun SpotCard(
    spot:          Spot,
    swipeProgress: Float,
    modifier:      Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(
                when(spot.id % 3){
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
                        text       = "${spot.category.emoji}  ${spot.category.label}",
                        color      = Color.White,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(spot.authorLabel,              color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text("·",                           color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text(spot.timeAgo,                  color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text("·",                           color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text("${spot.followerCount} seguono", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }

            Column(
                modifier.fillMaxSize().align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text       = spot.title,
                    color      = Color.White,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text       = spot.description,
                    color      = Color.White.copy(alpha = 0.88f),
                    fontSize   = 14.sp,
                    lineHeight = 20.sp,
                )
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