package br.com.cotemig.poketeam.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class PokemonListResponse(val results: List<PokemonSummaryDto>)
data class PokemonSummaryDto(val name: String, val url: String)
data class PokemonResourceDto(val name: String)
data class PokemonTypeDto(val type: PokemonResourceDto)
data class PokemonStatDto(val base_stat: Int, val stat: PokemonResourceDto)
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonTypeDto>,
    val stats: List<PokemonStatDto>
)

interface PokeApi {
    @GET("pokemon/")
    suspend fun list(@Query("limit") limit: Int, @Query("offset") offset: Int): PokemonListResponse

    @GET("pokemon/{id}/")
    suspend fun detail(@Path("id") id: Int): PokemonDetailDto
}

object PokeApiClient {
    val api: PokeApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApi::class.java)
    }
}
