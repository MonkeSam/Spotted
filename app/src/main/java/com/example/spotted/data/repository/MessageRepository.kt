package com.example.spotted.data.repository

import com.example.spotted.data.model.Message
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class MessageRepository {
    private val db get() = SupabaseModule.client.from("Messages")

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
}