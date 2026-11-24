package br.edu.uea.passanorte2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.uea.passanorte2.data.Graph
import br.edu.uea.passanorte2.data.Repositories
import br.edu.uea.passanorte2.ui.chatbot.ChatScreen
import br.edu.uea.passanorte2.ui.chatbot.ChatViewModel
import br.edu.uea.passanorte2.ui.chatbot.LoginScreen
import br.edu.uea.passanorte2.ui.chatbot.LoginViewModel
import br.edu.uea.passanorte2.ui.chatbot.SessionsScreen
import br.edu.uea.passanorte2.ui.chatbot.SessionsViewModel
import br.edu.uea.passanorte2.ui.theme.Passanorte2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            Passanorte2Theme {
                PassanorteApp()
            }
        }
    }
}

private const val START_ROUTE = "login"

/**
 * Fluxo do turista:
 *  login → lista de conversas → chat do assistente.
 * Rotas:
 *  login | sessions | chat/{sessionId}
 */
@Composable
private fun PassanorteApp() {
    val navController = rememberNavController()
    val hasSession = Graph.authStore.tokens != null

    val startDestination = if (hasSession) "sessions" else START_ROUTE

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier,
    ) {
        composable("login") {
            val vm: LoginViewModel = viewModel { LoginViewModel(Repositories.auth) }
            LoginScreen(
                vm = vm,
                onLoggedIn = {
                    navController.navigate("sessions") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }

        composable("sessions") {
            val vm: SessionsViewModel = viewModel { SessionsViewModel(Repositories.chatbot) }
            SessionsScreen(
                vm = vm,
                onOpenSession = { sessionId ->
                    navController.navigate("chat/$sessionId")
                },
                onLoggedOut = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable("chat/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull() ?: 0L
            val vm: ChatViewModel = viewModel {
                ChatViewModel(Repositories.chatbot, sessionId)
            }
            ChatScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
