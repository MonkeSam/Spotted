package com.example.spotted.data.repository

import com.example.spotted.data.model.Message
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class MessageRepository {
    private val db get() = SupabaseModule.client.from("Messages")
    private var channel: RealtimeChannel? = null

    suspend fun getMessagesForChat(chatId: Long): Resource<List<Message>> = try {
        Resource.Success(
            db.select {
                filter { eq("chatId", chatId) }
                order(column = "sendTime", order = Order.ASCENDING)
            }.decodeList<Message>()
        )
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero dei messaggi")
    }

    @OptIn(ExperimentalTime::class)
    suspend fun sendMessage(
        userId: String,
        chatId: Long,
        text: String
    ): Resource<PostgrestResult> = try {
        Resource.Success(
            db.insert(
                Message(
                    userId = userId,
                    sendTime = Clock.System.now(),
                    message = text,
                    chatId = chatId
                )
            )
        )
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nell'invio del messaggio")
    }

    suspend fun getLastMessage(chatId: Long): Message? = runCatching {
        db.select {
            filter { eq("chatId", chatId) }
            order(column = "sendTime", order = Order.DESCENDING)
            limit(count = 1)
        }.decodeList<Message>().firstOrNull()
    }.getOrNull()

    /**
     * Crea (se non esiste già) il canale realtime per una chat e restituisce
     * il flow degli INSERT in tempo reale su quella chat.
     * Va chiamato prima di startListening(), e la collect del flow deve
     * partire prima della subscribe per non perdere i primi eventi.
     */
    fun observeNewMessages(chatId: Long): Flow<Message> {
        val ch = SupabaseModule.client.realtime.channel("messages-chat-$chatId")
        channel = ch

        return ch.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "Messages"
        }.mapNotNull { action ->
            // Log per vedere se arrivano eventi
            println("📩 Realtime event received: $action")
            try {
                val msg = action.decodeRecord<Message>()
                if (msg.chatId == chatId) {
                    println("✅ Decoded message: $msg")
                    msg
                } else {
                    println("❌ chatId mismatch: ${msg.chatId} != $chatId")
                    null
                }
            } catch (e: Exception) {
                println("❌ Decoding error: ${e.message}")
                null
            }
        }
    }

    suspend fun startListening() {
        channel?.subscribe()
    }

    suspend fun stopListening() {
        channel?.unsubscribe()
        channel = null
    }
}