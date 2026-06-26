@file:OptIn(ExperimentalTime::class)

package com.example.spotted.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotted.data.model.Message
import com.example.spotted.data.model.User
import com.example.spotted.data.view.ChatViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime

// ─── ChatScreen ──────────────────────────────────────────────────────────────

@Composable
fun ChatScreen(
    postId: Long,
    innerPadding: PaddingValues,
) {
    val viewModel: ChatViewModel = koinViewModel()
    val messages   by viewModel.messages.collectAsState()
    val isLoading  by viewModel.isLoading.collectAsState()
    val isSending  by viewModel.isSending.collectAsState()
    val error      by viewModel.error.collectAsState()
    val userMap by viewModel.userMap.collectAsState()

    // Carica i messaggi al primo avvio e ogni volta che cambia postId
    LaunchedEffect(postId) {
        viewModel.loadMessages(postId)
    }

    // Scrolla all'ultimo messaggio quando arrivano nuovi
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }

    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .imePadding()
    ) {

        // ── Contenuto principale ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLoading && messages.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null && messages.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text  = "Errore nel caricamento",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text  = error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { viewModel.loadMessages(postId) }) {
                            Text("Riprova")
                        }
                    }
                }
                messages.isEmpty() -> {
                    Text(
                        text     = "Nessun messaggio ancora.\nSii il primo a scrivere!",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        state          = listState,
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageItem(
                                message  = message,
                                isFromMe = viewModel.isFromMe(message),
                                userMap  = viewModel.userMap.value
                            )
                        }
                    }
                }
            }
        }

        // ── Input messaggio ───────────────────────────────────────────────
        Surface(
            tonalElevation = 4.dp,
            modifier       = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value         = messageText,
                    onValueChange = { messageText = it },
                    placeholder   = { Text("Scrivi un messaggio...") },
                    modifier      = Modifier.weight(1f),
                    shape         = RoundedCornerShape(24.dp),
                    maxLines      = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    )
                )

                val canSend = messageText.isNotBlank() && !isSending

                IconButton(
                    onClick = {
                        if (canSend) {
                            viewModel.sendMessage(postId, messageText.trim())
                            messageText = ""
                        }
                    },
                    enabled  = canSend,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (canSend) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color     = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Invia",
                            tint               = if (canSend) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─── MessageItem ─────────────────────────────────────────────────────────────

@Composable
fun MessageItem(
    message: Message,
    isFromMe: Boolean,
    userMap: Map<String, User> // nuova mappa
) {
    val alignment = if (isFromMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isFromMe)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isFromMe)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    // Ottieni l'utente mittente (per i messaggi altrui)
    val senderUser = if (!isFromMe) userMap[message.userId] else null
    val senderName = if (!isFromMe) {
        senderUser?.let {
            listOfNotNull(it.name, it.surname).joinToString(" ").ifBlank { it.email }
        } ?: message.userId.take(8) // fallback
    } else null

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!isFromMe) {
            Text(
                text = senderName ?: "Utente sconosciuto",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.message,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(message.sendTime.toEpochMilliseconds())),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}