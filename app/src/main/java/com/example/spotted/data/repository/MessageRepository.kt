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

object MessageRepository {

    private val db get() = SupabaseModule.client.from("Messages")

    suspend fun getMessages(chatId: Long): Resource<List<Message>> = runCatching {
        db.select {
            filter { eq("chatId", chatId) }
            order("sendTime", Order.ASCENDING)
        }.decodeList<Message>()
    }.toResource()

    @OptIn(ExperimentalTime::class)
    suspend fun sendMessage(userId: String, chatId: Long, text: String): Resource<PostgrestResult> = runCatching {
        db.insert(
            Message(
                userId   = userId,
                sendTime = Clock.System.now(),
                message  = text,
                chatId   = chatId
            )
        )
    }.toResource()
}