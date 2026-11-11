package br.com.cotemig.poketeam

import android.app.Application
import br.com.cotemig.poketeam.data.AppDatabase
import br.com.cotemig.poketeam.data.PokemonRepository
import br.com.cotemig.poketeam.data.SessionManager
import br.com.cotemig.poketeam.network.PokeApiClient

class PokemonApplication : Application() {
    private val database by lazy { AppDatabase.get(this) }
    val repository by lazy {
        PokemonRepository(database.userDao(), database.favoriteDao(), PokeApiClient.api)
    }
    val sessionManager by lazy { SessionManager(this) }
}
