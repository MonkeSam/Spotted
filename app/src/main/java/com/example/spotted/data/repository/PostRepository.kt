package com.example.spotted.data.repository

import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import com.example.spotted.utils.toResource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

object PostRepository {

    private val db get() = SupabaseModule.client.from("Posts")

    suspend fun getAllPosts(): Resource<List<Post>> = runCatching {
        db.select().decodeList<Post>()
    }.toResource()

    suspend fun getPostById(id: Long): Resource<Post> = runCatching {
        db.select { filter { eq("id", id) } }.decodeSingle<Post>()
    }.toResource()

    suspend fun getUnseenPosts(userId: String): Resource<List<Post>> = runCatching {
        val followedIds  = FollowRepository.getPostIds(userId)
        val discardedIds = DiscardedRepository.getPostIds(userId)
        val excludeIds   = (followedIds + discardedIds).toSet()

        db.select().decodeList<Post>().filter { it.id !in excludeIds }
    }.toResource()

    suspend fun getPostsByCategory(category: Int): Resource<List<Post>> = runCatching {
        db.select { filter { eq("category", category) } }.decodeList<Post>()
    }.toResource()

    suspend fun createPost(post: Post): Resource<PostgrestResult> = runCatching {
        db.insert(post)
    }.toResource()
}