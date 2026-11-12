package br.com.cotemig.pokefav.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.cotemig.pokefav.data.PokemonDetalhe
import br.com.cotemig.pokefav.data.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Tela de detalhe: busca o pokémon na API e controla o favorito
class DetalheViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PokemonRepository(application)

    private val _estado = MutableStateFlow<UiState<PokemonDetalhe>>(UiState.Carregando)
    val estado: StateFlow<UiState<PokemonDetalhe>> = _estado

    private val _favorito = MutableStateFlow(false)
    val favorito: StateFlow<Boolean> = _favorito

    private var pokemonId = 0

    fun iniciar(id: Int) {
        // Evita buscar de novo quando a tela é recomposta (ex: girar o celular)
        if (pokemonId == id) return
        pokemonId = id

        carregar()
        viewModelScope.launch {
            repository.ehFavorito(id).collect { _favorito.value = it }
        }
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.value = UiState.Carregando
            try {
                _estado.value = UiState.Sucesso(repository.buscarPokemon(pokemonId))
            } catch (e: Exception) {
                _estado.value = UiState.Erro("Não foi possível carregar este pokémon. Verifique sua conexão.")
            }
        }
    }

    fun alternarFavorito() {
        val estadoAtual = _estado.value
        if (estadoAtual !is UiState.Sucesso) return

        val pokemon = estadoAtual.dados
        viewModelScope.launch {
            if (_favorito.value) {
                repository.removerFavorito(pokemon.id, pokemon.name)
            } else {
                repository.adicionarFavorito(pokemon.id, pokemon.name)
            }
        }
    }
}
