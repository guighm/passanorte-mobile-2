package br.edu.uea.passanorte2.ui.chatbot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.uea.passanorte2.data.Repositories
import br.edu.uea.passanorte2.data.remote.ChatMessageView
import kotlinx.coroutines.launch

/** Tela do chat (GET histórico + POST mensagem). */
class ChatViewModel(
    private val repository: br.edu.uea.passanorte2.data.ChatbotRepository,
    private val sessionId: Long,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set
    var sending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var messages by mutableStateOf<List<ChatMessageView>>(emptyList())
        private set
    var draft by mutableStateOf("")
        private set

    init { loadHistory() }

    fun loadHistory() {
        loading = true
        viewModelScope.launch {
            repository.sessionHistory(sessionId)
                .onSuccess {
                    messages = it.messages
                    errorMessage = null
                }
                .onFailure { e ->
                    errorMessage = when ((e as? br.edu.uea.passanorte2.data.ApiException)?.code) {
                        404 -> "Conversa não encontrada."
                        401 -> "Sessão expirada — entre novamente."
                        else -> "Não foi possível carregar o histórico."
                    }
                }
            loading = false
        }
    }

    fun onDraftChange(value: String) {
        if (!sending) draft = value
    }

    fun sendMessage() {
        val text = draft.trim()
        if (text.isEmpty() || sending) return
        sending = true
        errorMessage = null
        // Feedback otimista: mostra a mensagem do turista imediatamente.
        draft = ""
        viewModelScope.launch {
            repository.sendMessage(sessionId, text)
                .onSuccess {
                    messages = messages + it.userMessage + it.assistantMessage
                    sending = false
                }
                .onFailure { e ->
                    // Falhou: devolve o texto para o campo e mostra o erro.
                    draft = text
                    errorMessage = when ((e as? br.edu.uea.passanorte2.data.ApiException)?.code) {
                        404 -> "Conversa não encontrada ou indisponível."
                        else -> "A resposta não pôde ser enviada — tente novamente."
                    }
                    sending = false
                }
        }
    }
}
