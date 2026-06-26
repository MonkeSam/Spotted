package com.example.spotted.data.repository

import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

class PostRepository(
    private val followRepository: FollowRepository,
    private val discardedRepository: DiscardedRepository
) {
    private val db get() = SupabaseModule.client.from("Posts")

    suspend fun getAllPosts(): Resource<List<Post>> = try {
        Resource.Success(db.select().decodeList<Post>())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero di tutti i post")
    }

    suspend fun getPostById(id: Long): Resource<Post> = try {
        Resource.Success(db.select { filter { eq("id", id) } }.decodeSingle<Post>())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero del post")
    }

    suspend fun getUnseenPosts(userId: String): Resource<List<Post>> = try {
        val followedIds = followRepository.getPostIds(userId)
        val discardedIds = discardedRepository.getPostIds(userId)
        val excludeIds = (followedIds + discardedIds).toSet()
        val posts = db.select().decodeList<Post>().filter { it.id !in excludeIds }
        Resource.Success(posts)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero dei post non visti")
    }

    suspend fun getPostsByCategory(category: Int): Resource<List<Post>> = try {
        Resource.Success(db.select { filter { eq("category", category) } }.decodeList<Post>())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero dei post per categoria")
    }

    suspend fun createPost(post: Post): Resource<PostgrestResult> = try {
        Resource.Success(db.insert(post))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nella creazione del post")
    }
}