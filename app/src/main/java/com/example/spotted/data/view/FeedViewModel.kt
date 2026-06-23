package com.example.spotted.data.view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.data.repository.DiscardedRepository
import com.example.spotted.data.repository.FollowRepository
import com.example.spotted.data.repository.PostRepository
import com.example.spotted.utils.Resource
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Recupera l'uid una volta sola al momento della creazione del ViewModel
    private val userId: String? = SupabaseModule.client.auth.currentUserOrNull()?.id

    init {
        loadFeed()
    }

    private fun loadFeed() {
        val uid = userId ?: run {
            _error.value = "Utente non autenticato"
            return
        }
        viewModelScope.launch {
            when (val result = PostRepository.getUnseenPosts(uid)) {
                is Resource.Success -> {
                    Log.w("FeedViewModel", "Post ricevuti: ${result.data.size}")
                    _posts.value = result.data
                }
                is Resource.Error   -> {
                    Log.w("FeedViewModel", "Post ricevuti: ${result.message}")
                    _error.value = result.message
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun swipeRight(postId: Long) {
        val uid = userId ?: return
        viewModelScope.launch { FollowRepository.followPost(uid, postId) }
    }

    fun swipeLeft(postId: Long) {
        val uid = userId ?: return
        viewModelScope.launch { DiscardedRepository.discardPost(uid, postId) }
    }
}