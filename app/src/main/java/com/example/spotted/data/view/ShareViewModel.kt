// ShareViewModel.kt
package com.example.spotted.data.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotted.data.model.Category
import com.example.spotted.data.model.Post
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.data.repository.CategoryRepository
import com.example.spotted.data.repository.PostRepository
import com.example.spotted.data.repository.StorageRepository
import com.example.spotted.utils.Resource
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ShareViewModel(
    private val categoryRepository: CategoryRepository,
    private val postRepository: PostRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()


    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val userId: String? = SupabaseModule.client.auth.currentUserOrNull()?.id

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = categoryRepository.getAllCategories()) {
                is Resource.Success -> _categories.value = result.data.sortedBy { it.id }
                is Resource.Error   -> { /* fallisce silenziosamente, l'utente può riprovare */ }
                is Resource.Loading -> {}
            }
        }
    }

    /**
     * Pubblica un nuovo Spot.
     *
     * @param title        titolo del post
     * @param category     ID categoria
     * @param description  descrizione testuale
     * @param latitude     latitudine
     * @param longitude    longitudine
     * @param photoBytes   byte array dell'immagine (null se non allegata)
     * @param photoMime    tipo MIME dell'immagine (default "image/jpeg")
     */
    @OptIn(ExperimentalTime::class)
    fun publishSpot(
        title: String,
        category: Int,
        description: String,
        latitude: Double,
        longitude: Double,
        photoBytes: ByteArray? = null,
        photoMime: String = "image/jpeg"
    ) {
        val uid = userId ?: run {
            _error.value = "Utente non autenticato"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value     = null
            _isSuccess.value = false


            var photoUrl: String? = null
            if (photoBytes != null) {
                when (val uploadResult = storageRepository.uploadPostPhoto(photoBytes, photoMime)) {
                    is Resource.Success -> photoUrl = uploadResult.data
                    is Resource.Error   -> {
                        _error.value = "Errore upload foto: ${uploadResult.message}"
                        _isLoading.value = false
                        return@launch
                    }
                    is Resource.Loading -> {}
                }
            }


            val newPost = Post(
                category    = category,
                title       = title,
                description = description,
                timestamp   = Clock.System.now(),
                latitude    = latitude,
                longitude   = longitude,
                photo       = photoUrl
            )

            when (val result = postRepository.createPost(newPost)) {
                is Resource.Success -> _isSuccess.value = true
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }
}