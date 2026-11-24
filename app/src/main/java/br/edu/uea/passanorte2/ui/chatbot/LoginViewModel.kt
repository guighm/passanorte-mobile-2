package br.edu.uea.passanorte2.ui.chatbot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.uea.passanorte2.data.Graph
import br.edu.uea.passanorte2.data.Repositories
import kotlinx.coroutines.launch

/** Estado da tela de login do turista (POST /tourists/login). */
class LoginViewModel(
    private val auth: br.edu.uea.passanorte2.data.AuthRepository,
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(value: String) { email = value }
    fun onPasswordChange(value: String) { password = value }
    fun consumeError() { errorMessage = null }

    fun submit(onSuccess: () -> Unit) {
        if (loading) return
        val mail = email.trim()
        if (mail.isEmpty() || password.isEmpty()) {
            errorMessage = "Informe e-mail e senha."
            return
        }
        loading = true
        errorMessage = null
        viewModelScope.launch {
            auth.touristLogin(mail, password)
                .onSuccess { tokens ->
                    Graph.authStore.tokens = tokens
                    loading = false
                    onSuccess()
                }
                .onFailure { e ->
                    loading = false
                    errorMessage = when ((e as? br.edu.uea.passanorte2.data.ApiException)?.code) {
                        401, 403 -> "E-mail ou senha inválidos."
                        404 -> "E-mail ou senha inválidos."
                        -1 -> "Não foi possível conectar ao servidor."
                        else -> "Erro ${br.edu.uea.passanorte2.data.mapError(e).code}: tente novamente."
                    }
                }
        }
    }
}
