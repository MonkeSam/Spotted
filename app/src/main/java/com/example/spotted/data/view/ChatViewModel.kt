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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ChatViewModel(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    // Stato di autenticazione (osservabile)
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _whoSent = MutableStateFlow<User?>(null)
    val whoSent: StateFlow<User?> = _whoSent.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _userMap = MutableStateFlow<Map<String, User>>(emptyMap())
    val userMap: StateFlow<Map<String, User>> = _userMap.asStateFlow()

    private var realtimeJob: Job? = null

    init {
        // Recupera l'ID utente corrente all'avvio e aggiorna il flusso
        viewModelScope.launch {
            _currentUserId.value = SupabaseModule.client.auth.currentUserOrNull()?.id
        }
    }

    fun loadMessages(chatId: Long) {
        val uid = _currentUserId.value
        if (uid == null) {
            _error.value = "Utente non autenticato"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = messageRepository.getMessagesForChat(chatId)) {
                is Resource.Success -> {
                    _messages.value = result.data
                    syncUserMap(result.data.map { it.userId }, uid)
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }

        startRealtime(chatId, uid)
    }

    private suspend fun syncUserMap(userIds: List<String>, uid: String) {
        val ids = userIds.distinct().filter { it != uid }
        if (ids.isEmpty()) {
            _userMap.value = emptyMap()
            return
        }
        when (val userResult = userRepository.getUsersByIds(ids)) {
            is Resource.Success -> {
                _userMap.value = userResult.data.associateBy { it.id }
            }
            is Resource.Error -> {
                _error.value = "Errore caricamento utenti: ${userResult.message}"
            }
            else -> {}
        }
    }

    private fun startRealtime(chatId: Long, uid: String) {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            messageRepository.stopListening()          // Pulisce il vecchio canale
            val flow = messageRepository.observeNewMessages(chatId)

            // 🔑 SOLUZIONE: Lanciamo la collect in un blocco launch separato.
            // In questo modo il listener si registra sul canale IMMEDIATAMENTE.
            launch {
                flow.collect { newMessage ->
                    if (_messages.value.none { it.id == newMessage.id }) {
                        _messages.value = _messages.value + newMessage
                    }
                }
            }

            // Ora che il flow è in ascolto sul canale locale,
            // diciamo a Supabase di attivare la sottoscrizione globale.
            messageRepository.startListening()
        }
    }

    fun sendMessage(chatId: Long, text: String) {
        val uid = _currentUserId.value ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val tempMessage = Message(
            userId = uid,
            message = trimmed,
            chatId = chatId,
            sendTime = Clock.System.now()
        )
        _messages.value = _messages.value + tempMessage

        viewModelScope.launch {
            _isSending.value = true
            when (val result = messageRepository.sendMessage(uid, chatId, trimmed)) {
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isSending.value = false
        }
    }

    fun isFromMe(message: Message): Boolean = message.userId == _currentUserId.value

    override fun onCleared() {
        super.onCleared()
        realtimeJob?.cancel()
        viewModelScope.launch { messageRepository.stopListening() }
    }
}