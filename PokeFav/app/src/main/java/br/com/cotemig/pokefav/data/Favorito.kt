package br.com.cotemig.pokefav.data

import androidx.room.Entity

// Tabela de favoritos. Cada favorito pertence a um usuário
@Entity(tableName = "favoritos", primaryKeys = ["usuarioId", "pokemonId"])
data class Favorito(
    val usuarioId: Int,
    val pokemonId: Int,
    val nome: String
)
