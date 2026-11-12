package br.com.cotemig.pokefav.data

import android.content.Context
import br.com.cotemig.pokefav.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Junta a API (Retrofit) e os favoritos (Room) em um lugar só
class PokemonRepository(context: Context) {

    private val api = RetrofitInstance.api
    private val favoritoDao = AppDatabase.getInstance(context).favoritoDao()
    private val sessao = SessaoManager(context)

    // Dados da API

    suspend fun listarPokemons(): List<PokemonResumo> = withContext(Dispatchers.IO) {
        api.listarPokemons().results
    }

    suspend fun buscarPokemon(id: Int): PokemonDetalhe = withContext(Dispatchers.IO) {
        api.buscarPokemon(id)
    }

    // Favoritos (sempre do usuário logado)

    fun listarFavoritos(): Flow<List<Favorito>> =
        favoritoDao.listar(sessao.usuarioLogado())

    fun ehFavorito(pokemonId: Int): Flow<Boolean> =
        favoritoDao.ehFavorito(sessao.usuarioLogado(), pokemonId)

    suspend fun adicionarFavorito(pokemonId: Int, nome: String) {
        favoritoDao.inserir(Favorito(sessao.usuarioLogado(), pokemonId, nome))
    }

    suspend fun removerFavorito(pokemonId: Int, nome: String) {
        favoritoDao.remover(Favorito(sessao.usuarioLogado(), pokemonId, nome))
    }
}
