@file:OptIn(ExperimentalTime::class)

package com.example.spotted.data.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotted.data.model.Message
import com.example.spotted.data.model.User
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.data.repository.MessageRepository
import com.example.spotted.data.repository.UserRepository
import com.example.spotted.utils.Resource
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

class ChatViewModel(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
    ) : ViewModel() {
    
    private val _whoSent = MutableStateFlow<User?>(null)
    val whoSent : StateFlow<User?> = _whoSent.asStateFlow()
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** ID dell'utente autenticato; usato per distinguere i propri messaggi. */
    val currentUserId: String? = SupabaseModule.client.auth.currentUserOrNull()?.id

    // in ChatViewModel.kt
    private val _userMap = MutableStateFlow<Map<String, User>>(emptyMap())
    val userMap: StateFlow<Map<String, User>> = _userMap.asStateFlow()

    fun loadMessages(chatId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = messageRepository.getMessagesForChat(chatId)) {
                is Resource.Success -> {
                    _messages.value = result.data
                    // Carica i dettagli degli utenti (escluso il corrente)
                    val userIds = result.data.map { it.userId }.distinct()
                        .filter { it != currentUserId }
                    if (userIds.isNotEmpty()) {
                        when (val userResult = userRepository.getUsersByIds(userIds)) {
                            is Resource.Success -> {
                                _userMap.value = userResult.data.associateBy { it.id }
                            }
                            is Resource.Error -> _error.value = "Errore caricamento utenti: ${userResult.message}"
                            else -> {}
                        }
                    } else {
                        _userMap.value = emptyMap()
                    }
                }
                is Resource.Error -> _error.value = result.message
                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    /**
     * Invia un messaggio e ricarica la lista al termine.
     * Se l'utente non è autenticato, imposta un errore e ritorna.
     */
    fun sendMessage(chatId: Long, text: String) {
        val uid = currentUserId ?: run {
            _error.value = "Utente non autenticato"
            return
        }
        if (text.isBlank()) return

        viewModelScope.launch {
            _isSending.value = true

            when (val result = messageRepository.sendMessage(uid, chatId, text)) {
                is Resource.Success -> loadMessages(chatId)
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> { /* no-op */ }
            }

            _isSending.value = false
        }
    }

    /** True se il messaggio è stato scritto dall'utente corrente. */
    fun isFromMe(message: Message): Boolean = message.userId == currentUserId


}
