package br.com.cotemig.pokefav.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDao {

    // Flow para a tela atualizar sozinha quando a tabela mudar
    @Query("SELECT * FROM favoritos WHERE usuarioId = :usuarioId ORDER BY pokemonId")
    fun listar(usuarioId: Int): Flow<List<Favorito>>

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE usuarioId = :usuarioId AND pokemonId = :pokemonId)")
    fun ehFavorito(usuarioId: Int, pokemonId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(favorito: Favorito)

    @Delete
    suspend fun remover(favorito: Favorito)
}
