package br.edu.uea.passanorte2.data.remote

/** POST /api/v1/tourists/login — RF23 (e-mail como credencial). */
data class TouristLoginRequest(
    val email: String,
    val password: String,
)

/** Par de credenciais emitido por /tourists/login (mesmo contrato da fundação). */
data class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String? = "bearer",
    val expiresIn: Long,
    val role: String,
)

/** POST /api/v1/auth/refresh — troca single-use do refresh token. */
data class RefreshRequest(val refreshToken: String)

/** Uma linha da lista de sessões do turista (GET /chatbot/sessions). */
data class SessionListItem(
    val sessionId: Long,
    val createdAt: String,
    val lastMessageAt: String?,
)

data class SessionListResponse(
    val sessions: List<SessionListItem>,
)

/** POST /chatbot/sessions → 201. */
data class CreateSessionResponse(
    val sessionId: Long,
    val createdAt: String,
)

/** Uma mensagem persistida na sessão (turista "tourist" ou assistente "chatbot"). */
data class ChatMessageView(
    val messageId: Long,
    val role: String,
    val content: String,
    val itinerary: ItineraryView?,
    val createdAt: String,
)

/** GET /chatbot/sessions/{session_id} → 200. */
data class SessionHistoryResponse(
    val sessionId: Long,
    val messages: List<ChatMessageView>,
)

/** POST /chatbot/sessions/{session_id}/messages — corpo (texto não vazio). */
data class SendMessageRequest(val text: String)

/** POST .../messages → 201: mensagem do turista + resposta do assistente. */
data class SendMessageResponse(
    val userMessage: ChatMessageView,
    val assistantMessage: ChatMessageView,
)

/** Itinerário estruturado embutido em uma mensagem do assistente (FR-025). */
data class ItineraryView(
    val generatedAt: String,
    val targetDate: String,
    val totalCostCents: Long,
    val items: List<ItineraryItemView>,
)

data class ItineraryItemView(
    val kind: String,
    val refId: Long,
    val displayName: String,
    val suggestedAt: String,
    val ticketCostCents: Long,
)
