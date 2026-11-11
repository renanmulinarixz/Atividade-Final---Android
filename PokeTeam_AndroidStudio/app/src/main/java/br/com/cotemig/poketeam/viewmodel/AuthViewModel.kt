package br.com.cotemig.poketeam.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.cotemig.poketeam.PokemonApplication
import br.com.cotemig.poketeam.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PokemonApplication
    private val repository = app.repository
    private val session = app.sessionManager
    private val _userId = MutableStateFlow(session.currentUserId())
    val userId = _userId.asStateFlow()
    private val _user = MutableStateFlow<UserEntity?>(null)
    val user = _user.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        _userId.value?.let { id ->
            viewModelScope.launch {
                try {
                    val savedUser = repository.findUser(id)
                    if (savedUser == null) logout() else _user.value = savedUser
                } catch (_: Exception) {
                    logout()
                }
            }
        }
    }

    fun clearError() { _error.value = null }

    fun login(emailInput: String, password: String) {
        if (_busy.value) return
        val email = emailInput.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isBlank()) {
            _error.value = "Informe um e-mail válido e sua senha."
            return
        }
        _busy.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val found = repository.login(email, password)
                if (found == null) {
                    _error.value = "E-mail ou senha inválidos."
                } else {
                    session.save(found.id)
                    _user.value = found
                    _userId.value = found.id
                }
            } catch (_: Exception) {
                _error.value = "Não foi possível entrar. Tente novamente."
            } finally {
                _busy.value = false
            }
        }
    }

    fun register(nameInput: String, emailInput: String, password: String, confirm: String) {
        if (_busy.value) return
        val name = nameInput.trim()
        val email = emailInput.trim().lowercase()
        _error.value = when {
            name.length < 2 -> "Informe seu nome."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Informe um e-mail válido."
            password.length < 6 -> "A senha deve ter no mínimo 6 caracteres."
            password != confirm -> "As senhas não são iguais."
            else -> null
        }
        if (_error.value != null) return
        _busy.value = true
        viewModelScope.launch {
            try {
                val created = repository.createUser(name, email, password)
                session.save(created.id)
                _user.value = created
                _userId.value = created.id
            } catch (e: IllegalArgumentException) {
                _error.value = e.message
            } catch (_: Exception) {
                _error.value = "Não foi possível cadastrar. Confira os dados e tente novamente."
            } finally {
                _busy.value = false
            }
        }
    }

    fun logout() {
        session.clear()
        _user.value = null
        _userId.value = null
        _error.value = null
    }
}
