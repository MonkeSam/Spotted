package com.example.spotted.data.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.data.repository.FollowRepository
import com.example.spotted.utils.Resource
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val followRepository: FollowRepository
) : ViewModel() {

    /** Solo i post seguiti che hanno coordinate valide. */
    private val _followedPosts = MutableStateFlow<List<Post>>(emptyList())
    val followedPosts: StateFlow<List<Post>> = _followedPosts.asStateFlow()

    private val userId: String? = SupabaseModule.client.auth.currentUserOrNull()?.id

    init { loadFollowedPostsWithLocation() }

    fun loadFollowedPostsWithLocation() {
        val uid = userId ?: return
        viewModelScope.launch {
            when (val result = followRepository.getFollowedPosts(uid)) {
                is Resource.Success -> {
                    _followedPosts.value = result.data.filter {
                        it.latitude != null && it.longitude != null
                    }
                }
                is Resource.Error   -> { /* silenzioso: la mappa rimane senza pin */ }
                is Resource.Loading -> {}
            }
        }
    }
}
