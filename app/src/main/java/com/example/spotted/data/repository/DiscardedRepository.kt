package com.example.spotted.data.repository

import com.example.spotted.data.model.Discarded
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

class DiscardedRepository {
    private val db get() = SupabaseModule.client.from("Discarded")

    suspend fun getPostIds(userId: String): List<Long> =
        db.select { filter { eq("user_id", userId) } }
            .decodeList<Discarded>()
            .map { it.postId }

    suspend fun discardPost(userId: String, postId: Long): Resource<PostgrestResult> = try {
        Resource.Success(db.insert(Discarded(userId = userId, postId = postId)))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nello scarto del post")
    }
}