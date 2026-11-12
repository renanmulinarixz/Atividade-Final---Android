package br.com.cotemig.pokefav.network

import br.com.cotemig.pokefav.data.PokemonDetalhe
import br.com.cotemig.pokefav.data.PokemonListaResposta
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints da PokéAPI que o app usa
interface PokeApiService {

    // Lista dos pokémons (por padrão os 151 da primeira geração)
    @GET("pokemon")
    suspend fun listarPokemons(@Query("limit") limite: Int = 151): PokemonListaResposta

    // Dados completos de um pokémon pelo id
    @GET("pokemon/{id}")
    suspend fun buscarPokemon(@Path("id") id: Int): PokemonDetalhe
}
