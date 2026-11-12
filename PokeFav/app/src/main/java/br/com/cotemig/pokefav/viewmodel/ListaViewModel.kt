package br.com.cotemig.pokefav.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.cotemig.pokefav.data.PokemonRepository
import br.com.cotemig.pokefav.data.PokemonResumo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Lista de pokémons da aba Pokédex
class ListaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PokemonRepository(application)

    private val _estado = MutableStateFlow<UiState<List<PokemonResumo>>>(UiState.Carregando)
    val estado: StateFlow<UiState<List<PokemonResumo>>> = _estado

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.value = UiState.Carregando
            try {
                _estado.value = UiState.Sucesso(repository.listarPokemons())
            } catch (e: Exception) {
                _estado.value = UiState.Erro("Não foi possível carregar os pokémons. Verifique sua conexão.")
            }
        }
    }
}
