package com.example.spotted.data.repository

import com.example.spotted.data.model.Discarded
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import com.example.spotted.utils.toResource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

object DiscardedRepository {

    private val db get() = SupabaseModule.client.from("Discarded")

    internal suspend fun getPostIds(userId: String): List<Long> =
        db.select { filter { eq("user_id", userId) } }
            .decodeList<Discarded>()
            .map { it.postId }

    suspend fun discardPost(userId: String, postId: Long): Resource<PostgrestResult> = runCatching {
        db.insert(Discarded(userId = userId, postId = postId))
    }.toResource()
}