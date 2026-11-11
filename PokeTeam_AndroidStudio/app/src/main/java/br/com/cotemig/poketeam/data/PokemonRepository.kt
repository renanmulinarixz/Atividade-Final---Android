package br.com.cotemig.poketeam.data

import br.com.cotemig.poketeam.network.PokeApi
import br.com.cotemig.poketeam.network.PokemonDetailDto
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PokemonRepository(
    private val userDao: UserDao,
    private val favoriteDao: FavoriteDao,
    private val api: PokeApi
) {
    suspend fun createUser(name: String, email: String, password: String): UserEntity =
        withContext(Dispatchers.IO) {
            if (userDao.byEmail(email) != null) {
                throw IllegalArgumentException("Este e-mail já está cadastrado.")
            }
            val random = ByteArray(16)
            SecureRandom().nextBytes(random)
            val salt = random.joinToString("") { "%02x".format(it) }
            val user = UserEntity(
                nome = name,
                email = email,
                senhaHash = hash(salt + ":" + password),
                salt = salt
            )
            val id = userDao.insert(user)
            user.copy(id = id)
        }

    suspend fun login(email: String, password: String): UserEntity? =
        withContext(Dispatchers.IO) {
            val user = userDao.byEmail(email)
            if (user != null && user.senhaHash == hash(user.salt + ":" + password)) user else null
        }

    suspend fun findUser(id: Long): UserEntity? = withContext(Dispatchers.IO) {
        userDao.byId(id)
    }

    suspend fun pokemonPage(limit: Int, offset: Int): List<PokemonItem> =
        withContext(Dispatchers.IO) {
            api.list(limit, offset).results.mapNotNull { pokemon ->
                val id = pokemon.url.trimEnd('/').substringAfterLast('/').toIntOrNull()
                id?.let { PokemonItem(it, pokemon.name) }
            }
        }

    suspend fun pokemonDetail(id: Int): PokemonDetailDto =
        withContext(Dispatchers.IO) { api.detail(id) }

    fun favorites(userId: Long): Flow<List<FavoriteEntity>> = favoriteDao.byUser(userId)

    suspend fun addFavorite(userId: Long, pokemon: PokemonItem): Boolean = withContext(Dispatchers.IO) {
        favoriteDao.saveIfSpace(FavoriteEntity(userId, pokemon.id, pokemon.name, pokemon.imageUrl))
    }

    suspend fun removeFavorite(userId: Long, pokemonId: Int) = withContext(Dispatchers.IO) {
        favoriteDao.remove(userId, pokemonId)
    }

    private fun hash(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
