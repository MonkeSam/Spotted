package com.example.spotted.data.repository

import com.example.spotted.data.model.Follow
import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import com.example.spotted.utils.toResource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

class FollowRepository{

    private val db get() = SupabaseModule.client.from("Follows")

    internal suspend fun getPostIds(userId: String): List<Long> =
        db.select { filter { eq("user_id", userId) } }
            .decodeList<Follow>()
            .map { it.postId }

    suspend fun getFollowedPosts(userId: String): Resource<List<Post>> = runCatching {
        val postIds = getPostIds(userId).toSet()
        if (postIds.isEmpty()) return@runCatching emptyList()

        SupabaseModule.client.from("Posts")
            .select()
            .decodeList<Post>()
            .filter { it.id in postIds }
    }.toResource()

    suspend fun followPost(userId: String, postId: Long): Resource<PostgrestResult> = runCatching {
        db.insert(Follow(userId = userId, postId = postId))
    }.toResource()

    suspend fun unfollowPost(userId: String, postId: Long): Resource<PostgrestResult> = runCatching {
        db.delete {
            filter {
                eq("user_id", userId)
                eq("post_id", postId)
            }
        }
    }.toResource()

    suspend fun isFollowing(userId: String, postId: Long): Resource<Boolean> = runCatching {
        db.select {
            filter {
                eq("user_id", userId)
                eq("post_id", postId)
            }
        }.decodeList<Follow>().isNotEmpty()
    }.toResource()
    suspend fun getFollowerCount(postId: Long): Int = runCatching {
        db.select { filter { eq("post_id", postId) } }
            .decodeList<Follow>()
            .size
    }.getOrDefault(0)
}