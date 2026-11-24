package br.edu.uea.passanorte2.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Autenticação de turista — emite o par Bearer usado pelo chatbot. */
interface AuthApi {

    @POST("api/v1/tourists/login")
    suspend fun touristLogin(@Body body: TouristLoginRequest): SessionTokens

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): SessionTokens
}

/** Endpoints de chatbot (FR-002/FR-003/FR-004/FR-005). */
interface ChatbotApi {

    /** Lista as sessões do turista, mais recentes primeiro (FR-002). */
    @GET("api/v1/chatbot/sessions")
    suspend fun listSessions(): SessionListResponse

    /** Cria uma sessão do turista (FR-002). */
    @POST("api/v1/chatbot/sessions")
    suspend fun createSession(): CreateSessionResponse

    /** Histórico completo de uma sessão própria (FR-003/FR-004; 404 se de outro turista). */
    @GET("api/v1/chatbot/sessions/{sessionId}")
    suspend fun sessionHistory(@Path("sessionId") sessionId: Long): SessionHistoryResponse

    /** Envia mensagem e recebe a resposta do assistente (FR-003/FR-005). */
    @POST("api/v1/chatbot/sessions/{sessionId}/messages")
    suspend fun sendMessage(
        @Path("sessionId") sessionId: Long,
        @Body body: SendMessageRequest,
    ): SendMessageResponse
}
