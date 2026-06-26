package com.example.spotted.data.repository

import com.example.spotted.data.model.Follow
import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

class FollowRepository {

    private val db get() = SupabaseModule.client.from("Follows")

    suspend fun getPostIds(userId: String): List<Long> =
        db.select { filter { eq("user_id", userId) } }
            .decodeList<Follow>()
            .map { it.postId }

    suspend fun getFollowedPosts(userId: String): Resource<List<Post>> = try {
        val postIds = getPostIds(userId).toSet()
        if (postIds.isEmpty()) {
            Resource.Success(emptyList())
        } else {
            val posts = SupabaseModule.client.from("Posts")
                .select()
                .decodeList<Post>()
                .filter { it.id in postIds }
            Resource.Success(posts)
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero dei post seguiti")
    }

    suspend fun followPost(userId: String, postId: Long): Resource<PostgrestResult> = try {
        Resource.Success(db.insert(Follow(userId = userId, postId = postId)))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel seguire il post")
    }

    suspend fun unfollowPost(userId: String, postId: Long): Resource<PostgrestResult> = try {
        Resource.Success(db.delete {
            filter {
                eq("user_id", userId)
                eq("post_id", postId)
            }
        })
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nell'unfollow del post")
    }

    suspend fun isFollowing(userId: String, postId: Long): Resource<Boolean> = try {
        val result = db.select {
            filter {
                eq("user_id", userId)
                eq("post_id", postId)
            }
        }.decodeList<Follow>().isNotEmpty()
        Resource.Success(result)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel controllo follow")
    }

    suspend fun getFollowerCount(postId: Long): Int = try {
        db.select { filter { eq("post_id", postId) } }
            .decodeList<Follow>()
            .size
    } catch (e: Exception) {
        0
    }
}