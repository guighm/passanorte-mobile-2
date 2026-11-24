package br.edu.uea.passanorte2.data

import br.edu.uea.passanorte2.data.remote.ChatbotApi
import br.edu.uea.passanorte2.data.remote.CreateSessionResponse
import br.edu.uea.passanorte2.data.remote.SendMessageRequest
import br.edu.uea.passanorte2.data.remote.SendMessageResponse
import br.edu.uea.passanorte2.data.remote.SessionHistoryResponse
import br.edu.uea.passanorte2.data.remote.SessionListResponse
import br.edu.uea.passanorte2.data.remote.SessionTokens
import br.edu.uea.passanorte2.data.remote.TouristLoginRequest
import retrofit2.HttpException

/** Erro de API com o código HTTP, para mensagens amigáveis nas telas. */
class ApiException(val code: Int, message: String) : Exception(message)

fun mapError(e: Throwable): ApiException = when (e) {
    is ApiException -> e
    is HttpException -> ApiException(e.code(), e.message())
    else -> ApiException(-1, e.message ?: "Falha de rede")
}

/** Login do turista (POST /api/v1/tourists/login). */
class AuthRepository(private val api: br.edu.uea.passanorte2.data.remote.AuthApi) {

    suspend fun touristLogin(email: String, password: String): Result<SessionTokens> =
        runCatching {
            api.touristLogin(TouristLoginRequest(email = email.trim(), password = password))
        }.recoverCatching { throw mapError(it) }
}

/**
 * Fachada dos endpoints de chatbot:
 *  - GET/POST /api/v1/chatbot/sessions
 *  - GET /api/v1/chatbot/sessions/{session_id}
 *  - POST /api/v1/chatbot/sessions/{session_id}/messages
 */
class ChatbotRepository(private val api: ChatbotApi) {

    suspend fun listSessions(): Result<SessionListResponse> =
        runCatching { api.listSessions() }.recoverCatching { throw mapError(it) }

    suspend fun createSession(): Result<CreateSessionResponse> =
        runCatching { api.createSession() }.recoverCatching { throw mapError(it) }

    suspend fun sessionHistory(sessionId: Long): Result<SessionHistoryResponse> =
        runCatching { api.sessionHistory(sessionId) }.recoverCatching { throw mapError(it) }

    suspend fun sendMessage(sessionId: Long, text: String): Result<SendMessageResponse> =
        runCatching { api.sendMessage(sessionId, SendMessageRequest(text = text.trim())) }
            .recoverCatching { throw mapError(it) }
}

/** Acesso global às fachadas, criado sobre o Graph. */
object Repositories {
    val auth by lazy { AuthRepository(Graph.authApi) }
    val chatbot by lazy { ChatbotRepository(Graph.chatbotApi) }
}
