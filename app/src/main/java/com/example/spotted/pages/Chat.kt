package com.example.spotted.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val id: Int,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean
)
// Sample messages - in a real app these would come from a ViewModel/Repo based on chatId
val messages =
    mutableStateListOf(
        Message(1, "Marco", "Ciao ragazzi, qualcuno è già lì?", System.currentTimeMillis() - 1000 * 60 * 10, false),
        Message(2, "Sofia", "Io sto arrivando, sono a 5 minuti", System.currentTimeMillis() - 1000 * 60 * 8, false),
        Message(3, "Me", "Io sono già al tavolo vicino alla fontana!", System.currentTimeMillis() - 1000 * 60 * 5, true),
        Message(4, "Luca", "Ottimo, arrivo anche io!", System.currentTimeMillis() - 1000 * 60 * 2, false),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chatId: Int = -1, innerPadding: PaddingValues) {




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageItem(message)
            }
        }

        // Message Input
        Surface(
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {

        }
    }
}
@Composable
fun ChatInput(){
    var messageText by remember { mutableStateOf("") }
    BottomAppBar(){
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp).padding(top = 10.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Scrivi un messaggio...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                )
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        messages.add(
                            Message(
                                id = messages.size + 1,
                                sender = "Me",
                                text = messageText,
                                timestamp = System.currentTimeMillis(),
                                isFromMe = true
                            )
                        )
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank(),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (messageText.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Invia",
                    tint = if (messageText.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
@Composable
fun MessageItem(message: Message) {
    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!message.isFromMe) {
            Text(
                text = message.sender,
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
                bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 16.dp
            ),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
