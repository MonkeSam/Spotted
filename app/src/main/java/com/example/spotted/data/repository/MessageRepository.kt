package com.example.spotted.data.repository

import com.example.spotted.data.model.Message
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import com.example.spotted.utils.toResource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class MessageRepository {

    private val db get() = SupabaseModule.client.from("Messages")

    /** Restituisce tutti i messaggi di una chat, ordinati per data crescente. */
    suspend fun getMessagesForChat(chatId: Long): Resource<List<Message>> = runCatching {
        db.select {
            filter { eq("chatId", chatId) }
            order(column = "sendTime", order = Order.ASCENDING)
        }.decodeList<Message>()
    }.toResource()

    /** Invia un nuovo messaggio nella chat indicata. */
    @OptIn(ExperimentalTime::class)
    suspend fun sendMessage(
        userId: String,
        chatId: Long,
        text: String
    ): Resource<PostgrestResult> = runCatching {
        db.insert(
            Message(
                userId = userId,
                sendTime = Clock.System.now(),
                message = text,
                chatId = chatId
            )
        )
    }.toResource()

    /**
     * Restituisce l'ultimo messaggio di una chat, o null se non ne esistono.
     * Usato da FollowingViewModel per la preview nelle card.
     */
    suspend fun getLastMessage(chatId: Long): Message? = runCatching {
        db.select {
            filter { eq("chatId", chatId) }
            order(column = "sendTime", order = Order.DESCENDING)
            limit(count = 1)
        }.decodeList<Message>().firstOrNull()
    }.getOrNull()
}
