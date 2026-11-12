package br.com.cotemig.pokefav.data

import android.content.Context

// Guarda o id do usuário logado no SharedPreferences.
// Assim a pessoa continua logada mesmo depois de fechar o app.
class SessaoManager(context: Context) {

    private val prefs = context.getSharedPreferences("sessao", Context.MODE_PRIVATE)

    fun salvarUsuario(id: Int) {
        prefs.edit().putInt(CHAVE_USUARIO, id).apply()
    }

    fun usuarioLogado(): Int = prefs.getInt(CHAVE_USUARIO, -1)

    fun estaLogado(): Boolean = usuarioLogado() != -1

    fun sair() {
        prefs.edit().remove(CHAVE_USUARIO).apply()
    }

    companion object {
        private const val CHAVE_USUARIO = "usuario_id"
    }
}
