package br.com.cotemig.poketeam.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val email: String,
    val senhaHash: String,
    val salt: String
)

@Entity(tableName = "favoritos", primaryKeys = ["userId", "pokemonId"])
data class FavoriteEntity(
    val userId: Long,
    val pokemonId: Int,
    val nome: String,
    val imagem: String
)

data class PokemonItem(val id: Int, val name: String) {
    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png"
}

fun String.pokemonNome(): String = replace("-", " ").replaceFirstChar { it.uppercase() }
