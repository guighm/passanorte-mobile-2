package br.edu.uea.passanorte2.data

import br.edu.uea.passanorte2.BuildConfig
import br.edu.uea.passanorte2.data.remote.AuthApi
import br.edu.uea.passanorte2.data.remote.ChatbotApi
import br.edu.uea.passanorte2.data.remote.RefreshRequest
import br.edu.uea.passanorte2.data.remote.SessionTokens
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Contêiner de dependências da camada de rede. Inicializado em MainActivity
 * com o Context do app; expõe AuthApi e ChatbotApi já configurados com
 * Bearer token e renovação automática do access token em 401.
 */
object Graph {

    lateinit var authStore: AuthStore
        private set

    private val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val authInterceptor = Interceptor { chain ->
        val token = authStore.tokens?.accessToken
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(request)
    }

    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            // Só renova uma vez por request para evitar loop infinito.
            if (response.request.header("Authorization-Retried") != null) return null
            if (response.code != 401) return null

            val current = authStore.tokens ?: return null

            val refreshed = runCatching {
                kotlinx.coroutines.runBlocking {
                    retrofitNoAuth().refresh(RefreshRequest(current.refreshToken))
                }
            }.getOrNull()

            return if (refreshed == null) {
                authStore.clear()
                null
            } else {
                authStore.updateAccessToken(refreshed.accessToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${refreshed.accessToken}")
                    .header("Authorization-Retried", "1")
                    .build()
            }
        }
    }

    /** Retrofit sem o interceptor de auth (login/refresh nunca levam Bearer expirado). */
    private fun retrofitNoAuth(): AuthApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    fun init(context: android.content.Context) {
        authStore = AuthStore(context.applicationContext)
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val chatbotApi: ChatbotApi by lazy { retrofit.create(ChatbotApi::class.java) }
}
