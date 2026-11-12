package br.com.cotemig.pokefav.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Resposta de /pokemon?limit=151
@Serializable
data class PokemonListaResposta(
    val results: List<PokemonResumo>
)

@Serializable
data class PokemonResumo(
    val name: String,
    val url: String
) {
    // A API não manda o id na lista, então pegamos do final da url (.../pokemon/25/)
    val id: Int
        get() = url.trimEnd('/').substringAfterLast('/').toInt()
}

// Resposta de /pokemon/{id}
@Serializable
data class PokemonDetalhe(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TipoSlot>,
    val stats: List<StatusSlot>
)

@Serializable
data class TipoSlot(
    val type: ItemNome
)

@Serializable
data class StatusSlot(
    @SerialName("base_stat") val valorBase: Int,
    val stat: ItemNome
)

@Serializable
data class ItemNome(
    val name: String
)

// Endereço da imagem oficial de cada pokémon
fun imagemPokemon(id: Int): String =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
