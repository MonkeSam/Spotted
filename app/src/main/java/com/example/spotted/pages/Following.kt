//package com.example.spotted.pages
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.List
//import androidx.compose.material.icons.filled.Email
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.spotted.ui.screens.Spot
//import com.example.spotted.ui.screens.SpotCategory
//
//// ─── Modello ─────────────────────────────────────────────────────────────────
//
//data class FollowedSpot(
//    val spot: Spot,
//    val isActive: Boolean,
//    val unreadCount: Int = 0,
//    val lastMessage: String? = null,
//)
//
//// ─── Sample data ─────────────────────────────────────────────────────────────
//
//private val sampleFollowed = listOf(
//    FollowedSpot(
//        spot = Spot(1, SpotCategory.EVENT, "Aperitivo in Piazza Verdi",
//            "Cerchiamo gente stasera alle 19!", timeAgo = "12 min fa", followerCount = 47),
//        isActive = true, unreadCount = 5,
//        lastMessage = "Siamo al tavolino vicino alla fontana!"
//    ),
//    FollowedSpot(
//        spot = Spot(2, SpotCategory.PLACE, "Tavolo libero Aula C",
//            "Secondo piano vicino alla finestra.", timeAgo = "1h fa", followerCount = 23),
//        isActive = true, unreadCount = 0,
//        lastMessage = "Ancora libero, venite!"
//    ),
//    FollowedSpot(
//        spot = Spot(3, SpotCategory.PERSON, "Ragazza giacca rossa",
//            "Era in coda alla macchinetta.", timeAgo = "3h fa", followerCount = 89),
//        isActive = false, unreadCount = 0,
//        lastMessage = "Qualcuno l'ha trovata?"
//    ),
//    FollowedSpot(
//        spot = Spot(4, SpotCategory.EVENT, "Studio di gruppo Statistica",
//            "Chi viene? Biblioteca centrale ore 15.", timeAgo = "ieri", followerCount = 31),
//        isActive = false, unreadCount = 0,
//        lastMessage = "Siamo in 4, ci vediamo lì."
//    ),
//    FollowedSpot(
//        spot = Spot(5, SpotCategory.EVENT, "Studio di gruppo Statistica",
//            "Chi viene? Biblioteca centrale ore 15.", timeAgo = "ieri", followerCount = 31),
//        isActive = false, unreadCount = 0,
//        lastMessage = "Siamo in 4, ci vediamo lì."
//    ),
//    FollowedSpot(
//        spot = Spot(6, SpotCategory.EVENT, "Studio di gruppo Statistica",
//            "Chi viene? Biblioteca centrale ore 15.", timeAgo = "ieri", followerCount = 31),
//        isActive = false, unreadCount = 0,
//        lastMessage = "Siamo in 4, ci vediamo lì."
//    ),
//)
//
//// ─── FollowingScreen ─────────────────────────────────────────────────────────
//
//@Composable
//fun FollowingScreen(innerPadding: PaddingValues, navigate: (chatId: Int) -> Unit) {
//    val followed = remember { sampleFollowed }
//    val active   = followed.filter { it.isActive }
//    val closed   = followed.filter { !it.isActive }
//
//    if (followed.isEmpty()) {
//        EmptyFollowingState(innerPadding)
//        return
//    }
//
//    LazyColumn(
//        modifier            = Modifier.fillMaxSize(),
//        contentPadding      = innerPadding,
//    ) {
//        // ── Sezione Attivi ────────────────────────────────────────────────
//        if (active.isNotEmpty()) {
//            item {
//                SectionHeader(
//                    label    = "Attivi",
//                    count    = active.size,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
//                )
//            }
//            items(active, key = { it.spot.id }) { followed ->
//                FollowedSpotItem(
//                    followed    = followed,
//                    onClick     = { navigate(followed.spot.id) },
//                    modifier    = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp, vertical = 4.dp),
//                )
//            }
//        }
//
//        // ── Sezione Chiusi ────────────────────────────────────────────────
//        if (closed.isNotEmpty()) {
//            item {
//                SectionHeader(
//                    label    = "Chiusi",
//                    count    = closed.size,
//                    modifier = Modifier.padding(
//                        start  = 16.dp,
//                        end    = 16.dp,
//                        top    = if (active.isNotEmpty()) 20.dp else 10.dp,
//                        bottom = 10.dp,
//                    ),
//                )
//            }
//            items(closed, key = { it.spot.id }) { followed ->
//                FollowedSpotItem(
//                    followed    = followed,
//                    onClick     = {navigate} ,
//                    modifier    = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp, vertical = 4.dp),
//                )
//            }
//        }
//
//        item { Spacer(Modifier.height(8.dp)) }
//    }
//}
//
//// ─── SectionHeader ───────────────────────────────────────────────────────────
//
//@Composable
//fun SectionHeader(
//    label:    String,
//    count:    Int,
//    modifier: Modifier = Modifier,
//) {
//    Row(
//        modifier          = modifier,
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//    ) {
//        Text(
//            text       = label.uppercase(),
//            fontSize   = 11.sp,
//            fontWeight = FontWeight.Bold,
//            letterSpacing = 1.sp,
//            color      = MaterialTheme.colorScheme.onSurfaceVariant,
//        )
//        Surface(
//            color = MaterialTheme.colorScheme.surfaceVariant,
//            shape = CircleShape,
//        ) {
//            Text(
//                text     = count.toString(),
//                fontSize = 10.sp,
//                fontWeight = FontWeight.SemiBold,
//                color    = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
//            )
//        }
//        // Linea decorativa
//        HorizontalDivider(
//            modifier  = Modifier.weight(1f),
//            thickness = 0.5.dp,
//            color     = MaterialTheme.colorScheme.outlineVariant,
//        )
//    }
//}
//
//// ─── FollowedSpotItem ─────────────────────────────────────────────────────────
//
//@Composable
//fun FollowedSpotItem(
//    followed: FollowedSpot,
//    onClick:  () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    val spot       = followed.spot
//    val hasUnread  = followed.unreadCount > 0
//    val dimAlpha   = if (followed.isActive) 1f else 0.5f
//
//    Surface(
//        modifier = modifier.clickable(onClick = onClick),
//        shape    = RoundedCornerShape(16.dp),
//        color    = if (hasUnread)
//            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
//        else
//            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
//        tonalElevation = if (hasUnread) 2.dp else 0.dp,
//    ) {
//        Row(
//            modifier          = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 14.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(12.dp),
//        ) {
//
//            // ── Emoji categoria ──────────────────────────────────────────
//            Box(
//                modifier          = Modifier
//                    .size(44.dp)
//                    .clip(RoundedCornerShape(12.dp)),
////                    .background(spot.category.color.copy(alpha = if (followed.isActive) 1f else 0.4f)),
//                contentAlignment  = Alignment.Center,
//            ) {
//                Text(spot.category.emoji, fontSize = 20.sp)
//            }
//
//            // ── Testo centrale ───────────────────────────────────────────
//            Column(
//                modifier = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(3.dp),
//            ) {
//                // Badge categoria + status dot
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(6.dp),
//                ) {
//                    Surface(
////                        color = spot.category.color.copy(alpha = if (followed.isActive) 0.15f else 0.08f),
//                        shape = RoundedCornerShape(4.dp),
//                    ) {
//                        Text(
//                            text       = spot.category.label,
//                            fontSize   = 10.sp,
//                            fontWeight = FontWeight.SemiBold,
////                            color      = spot.category.color.copy(alpha = dimAlpha),
//                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
//                        )
//                    }
//                    // Status dot
//                    Box(
//                        modifier = Modifier
//                            .size(6.dp)
//                            .clip(CircleShape)
//                            .background(
//                                if (followed.isActive) Color(0xFF22C55E)
//                                else MaterialTheme.colorScheme.outline
//                            )
//                    )
//                }
//
//                // Titolo spot
//                Text(
//                    text       = spot.title,
//                    fontSize   = 14.sp,
//                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
//                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = dimAlpha),
//                    maxLines   = 1,
//                    overflow   = TextOverflow.Ellipsis,
//                )
//
//                // Ultimo messaggio (preview)
//                if (followed.lastMessage != null) {
//                    Text(
//                        text     = followed.lastMessage,
//                        fontSize = 12.sp,
//                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dimAlpha),
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                    )
//                }
//
//                // Stats: followers · messaggi · tempo
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment     = Alignment.CenterVertically,
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(3.dp),
//                    ) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.List,
//                            contentDescription = null,
//                            modifier = Modifier.size(11.dp),
//                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dimAlpha),
//                        )
//                        Text(
//                            "${spot.followerCount}",
//                            fontSize = 11.sp,
//                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dimAlpha),
//                        )
//                    }
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(3.dp),
//                    ) {
//                        Icon(
//                            Icons.Default.Email,
//                            contentDescription = null,
//                            modifier = Modifier.size(11.dp),
//                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dimAlpha),
//                        )
//                        Text(
//                            "23",
//                            fontSize = 11.sp,
//                            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dimAlpha),
//                        )
//                    }
//                    Text(
//                        "· ${spot.timeAgo}",
//                        fontSize = 11.sp,
//                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dimAlpha),
//                    )
//                }
//            }
//
//            // ── Badge messaggi non letti ─────────────────────────────────
//            if (hasUnread) {
//                Box(
//                    modifier         = Modifier
//                        .size(24.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.primary),
//                    contentAlignment = Alignment.Center,
//                ) {
//                    Text(
//                        text       = if (followed.unreadCount > 9) "9+" else followed.unreadCount.toString(),
//                        fontSize   = 10.sp,
//                        fontWeight = FontWeight.Bold,
//                        color      = Color.White,
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ─── EmptyFollowingState ──────────────────────────────────────────────────────
//
//@Composable
//fun EmptyFollowingState(innerPadding: PaddingValues) {
//    Column(
//        modifier            = Modifier
//            .fillMaxSize()
//            .padding(innerPadding)
//            .padding(horizontal = 32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
//    ) {
//        Text("👆", fontSize = 48.sp)
//        Spacer(Modifier.height(16.dp))
//        Text(
//            text       = "Nessuno spot seguito",
//            style      = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold,
//            color      = MaterialTheme.colorScheme.onSurface,
//        )
//        Spacer(Modifier.height(6.dp))
//        Text(
//            text      = "Scorri a destra sul feed per seguire uno spot e partecipare alla chat.",
//            style     = MaterialTheme.typography.bodySmall,
//            color     = MaterialTheme.colorScheme.onSurfaceVariant,
//            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
//        )
//    }
//}