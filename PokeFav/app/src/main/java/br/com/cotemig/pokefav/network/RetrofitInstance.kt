package br.com.cotemig.pokefav.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

// Instância única do Retrofit usada pelo app
object RetrofitInstance {

    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    // ignoreUnknownKeys para não dar erro com os campos do JSON que não usamos
    private val json = Json { ignoreUnknownKeys = true }

    val api: PokeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PokeApiService::class.java)
    }
}
