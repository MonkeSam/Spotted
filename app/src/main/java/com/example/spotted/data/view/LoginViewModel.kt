package com.example.spotted.data.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotted.data.model.User
import com.example.spotted.data.repository.UserRepository
import com.example.spotted.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _user      = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _error     = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _isLoading.value = true
            val result = UserRepository.getCurrentUser()
            if (result is Resource.Success) _user.value = result.data
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value     = null

            when (val result = UserRepository.login(email, password)) {
                is Resource.Success -> _user.value  = result.data
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> {}
            }

            _isLoading.value = false
        }
    }
}