package com.example.spotted.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotted.data.model.User
import com.example.spotted.data.repository.UserRepository
import com.example.spotted.ui.theme.ThemeManager
import com.example.spotted.utils.Resource
import com.example.spotted.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isImageUploading: Boolean = false,
    val errorMessage: String? = null,
    val followingCount: Int = 14,
    val ignoredCount: Int = 42,
    val messagesCount: Int = 8,
    val notificationsEnabled: Boolean = true,
    val currentTheme: ThemeMode = ThemeMode.SYSTEM
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            ThemeManager.themeMode.collect { theme ->
                _uiState.update { it.copy(currentTheme = theme) }
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = userRepository.getCurrentUser()) {
                is Resource.Success -> _uiState.update { it.copy(user = result.data, isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                else -> {}
            }
        }
    }

    fun changeProfilePicture(bytes: ByteArray, mimeType: String) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isImageUploading = true) }


            when (val uploadResult = userRepository.uploadProfilePicture(bytes, mimeType,userId = currentUser.id)) {
                is Resource.Success -> {
                    val newImageUrl = uploadResult.data


                    when (val updateResult = userRepository.updateProfile(userId = currentUser.id, profilePicture = newImageUrl)) {
                        is Resource.Success -> {

                            loadUserProfile()
                        }
                        is Resource.Error -> {
                            _uiState.update { it.copy(errorMessage = "Errore salvataggio database: ${updateResult.message}") }
                        }
                        else -> {}
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = "Errore caricamento immagine: ${uploadResult.message}") }
                }
                else -> {}
            }
            _uiState.update { it.copy(isImageUploading = false) }
        }
    }

    fun updateTheme(mode: ThemeMode) { ThemeManager.setTheme(mode) }
    fun updateNotifications(enabled: Boolean) { _uiState.update { it.copy(notificationsEnabled = enabled) } }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = userRepository.logout()
            if (result is Resource.Success) onLogoutSuccess()
        }
    }
}