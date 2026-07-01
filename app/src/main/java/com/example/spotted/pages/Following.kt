package com.example.spotted.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotted.data.model.Category
import com.example.spotted.data.model.Message
import com.example.spotted.data.model.Post
import com.example.spotted.data.view.FollowingViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.time.ExperimentalTime




@Composable
fun FollowingScreen(innerPadding: PaddingValues, navigate: (Long) -> Unit) {

    val viewModel      : FollowingViewModel = koinViewModel()
    val posts          by viewModel.posts.collectAsState()
    val followerCounts by viewModel.followerCounts.collectAsState()
    val lastMessageMap by viewModel.lastMessageMap.collectAsState()
    val categories     by viewModel.categories.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()


    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadFollowedPosts()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isLoading) {
        Box(
            modifier         = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    if (posts.isEmpty()) {
        EmptyFollowingState(innerPadding)
        return
    }

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = innerPadding,
    ) {
        // ── Sezione Chat ──────────────────────────────────────────────────
        item {
            SectionHeader(
                label    = "Chat",
                count    = posts.size,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
        items(posts, key = { it.id }) { followed ->
            FollowedSpotItem(
                followed    = followed,
                onClick     = { navigate(followed.id) },
                modifier    = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                lastMessage = lastMessageMap[followed.id],
                timeAgo     = viewModel.timeAgo(followed),
                category    = categories[followed.category],
            )
        }
    }
}



@Composable
fun SectionHeader(
    label:    String,
    count:    Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text          = label.uppercase(),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.sp,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
        ) {
            Text(
                text       = count.toString(),
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier   = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }
        // Linea decorativa
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            thickness = 0.5.dp,
            color     = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}



@OptIn(ExperimentalTime::class)
@Composable
fun FollowedSpotItem(
    followed:    Post,
    onClick:     () -> Unit,
    modifier:    Modifier = Modifier,
    lastMessage: Message?,
    timeAgo:     String,
    category:    Category? = null,
) {
    val spot = followed

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape    = RoundedCornerShape(16.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {


            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text     = category?.emoji ?: "📍",
                    fontSize = 20.sp,
                )
            }


            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text       = category?.name ?: spot.category.toString(),
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }


                spot.title?.let {
                    Text(
                        text       = it,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onSurface,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                }


                if (lastMessage != null) {
                    Text(
                        text     = lastMessage.message,
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text(
                        text     = "Nessun messaggio",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }


                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text     = category?.name ?: spot.category.toString(),
                            fontSize = 11.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text     = timeAgo,
                        fontSize = 11.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}



@Composable
fun EmptyFollowingState(innerPadding: PaddingValues) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("👆", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "Nessuno spot seguito",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text      = "Scorri a destra sul feed per seguire uno spot e partecipare alla chat.",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}