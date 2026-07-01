package com.example.spotted.data.view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotted.data.model.Category
import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.data.repository.CategoryRepository
import com.example.spotted.data.repository.DiscardedRepository
import com.example.spotted.data.repository.FollowRepository
import com.example.spotted.data.repository.PostRepository
import com.example.spotted.utils.Resource
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FeedViewModel(
    private val categoryRepository: CategoryRepository,
    private val postRepository: PostRepository,
    private val followRepository: FollowRepository,
    private val discardedRepository: DiscardedRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts


    private val _categories = MutableStateFlow<Map<Int, Category>>(emptyMap())
    val categories: StateFlow<Map<Int, Category>> = _categories

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val userId: String? = SupabaseModule.client.auth.currentUserOrNull()?.id

    init {
        loadCategories()
        loadFeed()
    }

    /** Carica le categorie una volta sola; fallisce silenziosamente. */
    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = categoryRepository.getCategoryMap()) {
                is Resource.Success -> _categories.value = result.data
                is Resource.Error   -> { /* le categorie non bloccano il feed */ }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadFeed() {
        val uid = userId ?: run {
            _error.value = "Utente non autenticato"
            return
        }
        viewModelScope.launch {
            when (val result = postRepository.getUnseenPosts(uid)) {
                is Resource.Success -> {
                    Log.w("FeedViewModel", "Post ricevuti: ${result.data.size}")
                    _posts.value = result.data
                }
                is Resource.Error   -> {
                    Log.w("FeedViewModel", "Errore: ${result.message}")
                    _error.value = result.message
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun swipeRight(postId: Long) {
        val uid = userId ?: return
        _posts.value = _posts.value.filter { it.id != postId }
        viewModelScope.launch { followRepository.followPost(uid, postId) }
    }

    fun swipeLeft(postId: Long) {
        val uid = userId ?: return
        _posts.value = _posts.value.filter { it.id != postId }
        viewModelScope.launch { discardedRepository.discardPost(uid, postId) }
    }
    @OptIn(ExperimentalTime::class)
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