package br.com.cotemig.pokefav.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.cotemig.pokefav.data.Usuario
import br.com.cotemig.pokefav.data.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Usado nas telas de login, cadastro e perfil
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository(application)

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _carregando = MutableStateFlow(false)
    val carregando: StateFlow<Boolean> = _carregando

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    fun estaLogado(): Boolean = repository.estaLogado()

    fun entrar(email: String, senha: String, aoEntrar: () -> Unit) {
        if (email.isBlank() || senha.isBlank()) {
            _erro.value = "Preencha o e-mail e a senha."
            return
        }
        viewModelScope.launch {
            _carregando.value = true
            _erro.value = null
            val deuCerto = repository.entrar(email, senha)
            _carregando.value = false

            // Mensagem genérica para não dizer qual campo está errado
            if (deuCerto) aoEntrar() else _erro.value = "E-mail ou senha incorretos."
        }
    }

    fun cadastrar(nome: String, email: String, senha: String, aoCadastrar: () -> Unit) {
        viewModelScope.launch {
            _carregando.value = true
            _erro.value = null
            val mensagemErro = repository.cadastrar(nome, email, senha)
            _carregando.value = false

            if (mensagemErro == null) aoCadastrar() else _erro.value = mensagemErro
        }
    }

    fun carregarUsuario() {
        viewModelScope.launch {
            _usuario.value = repository.usuarioAtual()
        }
    }

    fun sair(aoSair: () -> Unit) {
        repository.sair()
        aoSair()
    }
}
