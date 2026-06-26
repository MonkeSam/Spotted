@file:OptIn(ExperimentalTime::class)

package com.example.spotted.data.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotted.data.model.Category
import com.example.spotted.data.model.Message
import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.data.repository.CategoryRepository
import com.example.spotted.data.repository.FollowRepository
import com.example.spotted.data.repository.MessageRepository
import com.example.spotted.utils.Resource
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FollowingViewModel(
    private val categoryRepository: CategoryRepository,
    private val followRepository: FollowRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // postId → numero di follower
    private val _followerCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val followerCounts: StateFlow<Map<Long, Int>> = _followerCounts.asStateFlow()

    // Un Message per ogni post seguito (l'ultimo inviato nella chat).
    private val _lastMessageMap = MutableStateFlow<Map<Long, Message?>>(emptyMap())
    val lastMessageMap: StateFlow<Map<Long, Message?>> = _lastMessageMap.asStateFlow()

    // Mappa categoryId → Category, per risolvere emoji e nome nel composable
    private val _categories = MutableStateFlow<Map<Int, Category>>(emptyMap())
    val categories: StateFlow<Map<Int, Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val userId: String? = SupabaseModule.client.auth.currentUserOrNull()?.id

    init {
        loadCategories()
        loadFollowedPosts()
    }

    /** Carica le categorie una volta sola; fallisce silenziosamente. */
    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = categoryRepository.getCategoryMap()) {
                is Resource.Success -> _categories.value = result.data
                is Resource.Error   -> { /* le categorie non bloccano il resto */ }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadFollowedPosts() {
        val uid = userId ?: run { _error.value = "Utente non autenticato"; return }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value     = null

            when (val result = followRepository.getFollowedPosts(uid)) {
                is Resource.Success -> {
                    val postsList = result.data
                    val enriched = postsList.map { post ->
                        async {
                            val count   = followRepository.getFollowerCount(post.id)
                            val lastMsg =  messageRepository.getLastMessage(post.id)
                            Triple(post.id, count, lastMsg)
                        }
                    }.awaitAll()

                    _followerCounts.value = enriched.associate { (id, count, _) -> id to count }
                    _lastMessageMap.value = enriched.associate { (id, _, msg) -> id to msg }
                    _posts.value          = postsList
                }
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun unfollowPost(postId: Long) {
        val uid = userId ?: return
        viewModelScope.launch {
            followRepository.unfollowPost(uid, postId)
            loadFollowedPosts()
        }
    }

    /** Converte il timestamp del post in una stringa relativa ("2h fa", "3g fa", …). */
    fun timeAgo(post: Post): String {
        val diff = post.timestamp?.let { Clock.System.now() - it } ?: return ""
        return when {
            diff.inWholeMinutes < 1 -> "Ora"
            diff.inWholeHours   < 1 -> "${diff.inWholeMinutes}m fa"
            diff.inWholeDays    < 1 -> "${diff.inWholeHours}h fa"
            diff.inWholeDays    < 7 -> "${diff.inWholeDays}g fa"
            else                    -> "${diff.inWholeDays / 7}sett. fa"
        }
    }
}