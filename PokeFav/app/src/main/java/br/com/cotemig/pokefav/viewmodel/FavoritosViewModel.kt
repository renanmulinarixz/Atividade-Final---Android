package br.com.cotemig.pokefav.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.cotemig.pokefav.data.Favorito
import br.com.cotemig.pokefav.data.PokemonRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Favoritos salvos no Room do usuário logado
class FavoritosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PokemonRepository(application)

    val favoritos: StateFlow<List<Favorito>> = repository.listarFavoritos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
