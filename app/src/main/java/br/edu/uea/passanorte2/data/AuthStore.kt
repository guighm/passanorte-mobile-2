package br.edu.uea.passanorte2.data

import android.content.Context
import android.content.SharedPreferences
import br.edu.uea.passanorte2.data.remote.SessionTokens
import androidx.core.content.edit

/**
 * Armazena o par de credenciais do turista (access + refresh) em SharedPreferences.
 * Os endpoints do chatbot exigem Bearer token de turista.
 */
class AuthStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("passanorte_auth", Context.MODE_PRIVATE)

    var tokens: SessionTokens?
        get() {
            val access = prefs.getString(KEY_ACCESS, null) ?: return null
            val refresh = prefs.getString(KEY_REFRESH, null) ?: return null
            return SessionTokens(
                accessToken = access,
                refreshToken = refresh,
                tokenType = prefs.getString(KEY_TYPE, "bearer") ?: "bearer",
                expiresIn = prefs.getLong(KEY_EXPIRES, 0L),
                role = prefs.getString(KEY_ROLE, "tourist") ?: "tourist",
            )
        }
        set(value) {
            prefs.edit {
                if (value == null) {
                    remove(KEY_ACCESS)
                    remove(KEY_REFRESH)
                    remove(KEY_TYPE)
                    remove(KEY_EXPIRES)
                    remove(KEY_ROLE)
                } else {
                    putString(KEY_ACCESS, value.accessToken)
                    putString(KEY_REFRESH, value.refreshToken)
                    putString(KEY_TYPE, value.tokenType ?: "bearer")
                    putLong(KEY_EXPIRES, value.expiresIn)
                    putString(KEY_ROLE, value.role)
                }
            }
        }

    /** Renova apenas o access token, mantendo o refresh single-use atual. */
    fun updateAccessToken(newAccess: String) {
        tokens?.let { current ->
            this.tokens = current.copy(accessToken = newAccess)
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_TYPE = "token_type"
        const val KEY_EXPIRES = "expires_in"
        const val KEY_ROLE = "role"
    }
}
