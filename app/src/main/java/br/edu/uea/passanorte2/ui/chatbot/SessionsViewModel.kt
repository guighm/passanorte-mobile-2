package br.edu.uea.passanorte2.ui.chatbot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.uea.passanorte2.data.Repositories
import br.edu.uea.passanorte2.data.remote.SessionListItem
import kotlinx.coroutines.launch

/** Tela de conversas (GET/POST /api/v1/chatbot/sessions). */
class SessionsViewModel(
    private val repository: br.edu.uea.passanorte2.data.ChatbotRepository,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set
    var creating by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var sessions by mutableStateOf<List<SessionListItem>>(emptyList())
        private set

    init { refresh() }

    fun refresh() {
        loading = true
        viewModelScope.launch {
            repository.listSessions()
                .onSuccess {
                    sessions = it.sessions
                    errorMessage = null
                }
                .onFailure { e ->
                    errorMessage = friendly(e)
                }
            loading = false
        }
    }

    /** Cria a conversa e navega direto para o chat. */
    fun createSession(onCreated: (Long) -> Unit) {
        if (creating) return
        creating = true
        viewModelScope.launch {
            repository.createSession()
                .onSuccess { onCreated(it.sessionId) }
                .onFailure { e ->
                    errorMessage = friendly(e)
                    // Mesmo se der erro ao listar antes, tenta recuperar a lista.
                    refresh()
                }
            creating = false
        }
    }

    private fun friendly(e: Throwable): String =
        when ((e as? br.edu.uea.passanorte2.data.ApiException)?.code) {
            401 -> "Sessão expirada — entre novamente."
            else -> "Não foi possível carregar suas conversas."
        }
}
