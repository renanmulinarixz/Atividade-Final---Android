package br.com.cotemig.poketeam.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.cotemig.poketeam.PokemonApplication
import br.com.cotemig.poketeam.data.FavoriteEntity
import br.com.cotemig.poketeam.data.PokemonItem
import br.com.cotemig.poketeam.network.PokemonDetailDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class CatalogState(
    val items: List<PokemonItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val offset: Int = 0,
    val hasMore: Boolean = true
)

data class DetailState(
    val loading: Boolean = false,
    val pokemon: PokemonDetailDto? = null,
    val error: String? = null
)

class PokemonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as PokemonApplication).repository
    private val _catalog = MutableStateFlow(CatalogState())
    val catalog = _catalog.asStateFlow()
    private val _detail = MutableStateFlow(DetailState())
    val detail = _detail.asStateFlow()
    private val _favorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())
    val favorites = _favorites.asStateFlow()
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()
    private var userId: Long? = null
    private var favoritesJob: Job? = null

    fun selectUser(id: Long) {
        if (userId == id) return
        userId = id
        _favorites.value = emptyList()
        _detail.value = DetailState()
        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            repository.favorites(id).collect { _favorites.value = it }
        }
        if (_catalog.value.items.isEmpty()) loadMore()
    }

    fun loadMore() {
        val current = _catalog.value
        if (current.loading || !current.hasMore) return
        _catalog.value = current.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val page = repository.pokemonPage(30, current.offset)
                _catalog.value = current.copy(
                    items = current.items + page,
                    offset = current.offset + page.size,
                    hasMore = page.size == 30,
                    loading = false,
                    error = null
                )
            } catch (_: Exception) {
                _catalog.value = current.copy(
                    loading = false,
                    error = "Não foi possível carregar a PokéAPI. Verifique sua conexão."
                )
            }
        }
    }

    fun refresh() {
        if (_catalog.value.loading) return
        _catalog.value = CatalogState()
        loadMore()
    }

    fun openDetail(id: Int) {
        _detail.value = DetailState(loading = true)
        viewModelScope.launch {
            try {
                _detail.value = DetailState(pokemon = repository.pokemonDetail(id))
            } catch (_: Exception) {
                _detail.value = DetailState(error = "Falha ao carregar os detalhes. Confira a internet.")
            }
        }
    }

    fun toggleFavorite(item: PokemonItem) {
        val id = userId ?: return
        viewModelScope.launch {
            try {
                if (_favorites.value.any { it.pokemonId == item.id }) {
                    repository.removeFavorite(id, item.id)
                } else if (_favorites.value.size >= 6) {
                    _messages.emit("Seu time está completo: máximo de 6 Pokémon.")
                } else {
                    if (repository.addFavorite(id, item)) {
                        _messages.emit("Pokémon adicionado ao seu time!")
                    } else {
                        _messages.emit("Seu time está completo: máximo de 6 Pokémon.")
                    }
                }
            } catch (_: Exception) {
                _messages.emit("Não foi possível salvar o favorito.")
            }
        }
    }
}
