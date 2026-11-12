package br.com.cotemig.pokefav.data

import android.content.Context
import android.util.Patterns

// Regras de cadastro, login e sessão
class UsuarioRepository(context: Context) {

    private val usuarioDao = AppDatabase.getInstance(context).usuarioDao()
    private val sessao = SessaoManager(context)

    fun estaLogado(): Boolean = sessao.estaLogado()

    // Retorna null se deu certo, ou a mensagem de erro para mostrar na tela
    suspend fun cadastrar(nome: String, email: String, senha: String): String? {
        val emailLimpo = email.trim().lowercase()

        if (nome.isBlank()) return "Informe seu nome."
        if (!Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches()) return "Informe um e-mail válido."
        if (senha.length < 6) return "A senha precisa ter pelo menos 6 caracteres."
        if (usuarioDao.buscarPorEmail(emailLimpo) != null) return "Este e-mail já está cadastrado."

        val novoUsuario = Usuario(nome = nome.trim(), email = emailLimpo, senhaHash = senha.sha256())
        val id = usuarioDao.inserir(novoUsuario)
        sessao.salvarUsuario(id.toInt())
        return null
    }

    // Retorna true se o e-mail e a senha conferem
    suspend fun entrar(email: String, senha: String): Boolean {
        val usuario = usuarioDao.buscarPorEmail(email.trim().lowercase()) ?: return false
        if (usuario.senhaHash != senha.sha256()) return false

        sessao.salvarUsuario(usuario.id)
        return true
    }

    suspend fun usuarioAtual(): Usuario? = usuarioDao.buscarPorId(sessao.usuarioLogado())

    fun sair() {
        sessao.sair()
    }
}

// Gera o hash SHA-256 da senha (nunca salvamos a senha em texto puro)
fun String.sha256(): String =
    java.security.MessageDigest.getInstance("SHA-256")
        .digest(toByteArray()).joinToString("") { "%02x".format(it) }
