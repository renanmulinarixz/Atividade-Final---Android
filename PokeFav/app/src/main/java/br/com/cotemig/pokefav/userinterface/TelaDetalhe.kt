package br.com.cotemig.pokefav.userinterface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cotemig.pokefav.data.PokemonDetalhe
import br.com.cotemig.pokefav.data.imagemPokemon
import br.com.cotemig.pokefav.viewmodel.DetalheViewModel
import br.com.cotemig.pokefav.viewmodel.UiState
import coil.compose.AsyncImage

@Composable
fun TelaDetalhe(
    pokemonId: Int,
    aoVoltar: () -> Unit,
    viewModel: DetalheViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val favorito by viewModel.favorito.collectAsState()

    // Carrega o pokémon que veio pela rota
    LaunchedEffect(pokemonId) {
        viewModel.iniciar(pokemonId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de cima com botão de voltar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            IconButton(onClick = aoVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Azul)
            }
            Text("Detalhes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Azul)
        }

        when (val atual = estado) {
            is UiState.Carregando -> Carregando()
            is UiState.Erro -> MensagemErro(atual.mensagem) { viewModel.carregar() }
            is UiState.Sucesso -> ConteudoDetalhe(
                pokemon = atual.dados,
                favorito = favorito,
                aoFavoritar = { viewModel.alternarFavorito() }
            )
        }
    }
}

@Composable
private fun ConteudoDetalhe(pokemon: PokemonDetalhe, favorito: Boolean, aoFavoritar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagem
        Box(contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(210.dp).background(VermelhoClaro, CircleShape))
            AsyncImage(
                model = imagemPokemon(pokemon.id),
                contentDescription = pokemon.name,
                modifier = Modifier.size(230.dp)
            )
        }

        Text(formatarNumero(pokemon.id), color = Cinza)
        Text(formatarNome(pokemon.name), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
        Spacer(Modifier.height(12.dp))

        // Tipos
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pokemon.types.forEach { tipo ->
                Text(
                    traduzirTipo(tipo.type.name),
                    color = Vermelho,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(VermelhoClaro, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        // Botão de favoritar (salva no Room)
        Button(
            onClick = aoFavoritar,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(if (favorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (favorito) "Remover dos favoritos" else "Favoritar")
        }
        Spacer(Modifier.height(24.dp))

        // Altura e peso (a API manda em decímetros e hectogramas)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Informacao("Altura", "%.1f m".format(pokemon.height / 10.0))
            Informacao("Peso", "%.1f kg".format(pokemon.weight / 10.0))
        }
        Spacer(Modifier.height(24.dp))

        // Atributos base
        Text(
            "Atributos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Azul,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        pokemon.stats.forEach { status ->
            LinhaAtributo(traduzirAtributo(status.stat.name), status.valorBase)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Informacao(titulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, color = Cinza)
        Text(valor, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Azul)
    }
}

@Composable
private fun LinhaAtributo(nome: String, valor: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Text(nome, color = Cinza, fontSize = 14.sp, modifier = Modifier.width(110.dp))
        Text(valor.toString(), fontWeight = FontWeight.Bold, color = Azul, modifier = Modifier.width(40.dp))
        LinearProgressIndicator(
            progress = { valor / 255f },
            color = Vermelho,
            trackColor = VermelhoClaro,
            modifier = Modifier.weight(1f).height(8.dp)
        )
    }
}

private fun traduzirTipo(tipo: String): String = when (tipo) {
    "normal" -> "Normal"
    "fire" -> "Fogo"
    "water" -> "Água"
    "grass" -> "Planta"
    "electric" -> "Elétrico"
    "ice" -> "Gelo"
    "fighting" -> "Lutador"
    "poison" -> "Veneno"
    "ground" -> "Terra"
    "flying" -> "Voador"
    "psychic" -> "Psíquico"
    "bug" -> "Inseto"
    "rock" -> "Pedra"
    "ghost" -> "Fantasma"
    "dragon" -> "Dragão"
    "dark" -> "Sombrio"
    "steel" -> "Aço"
    "fairy" -> "Fada"
    else -> formatarNome(tipo)
}

private fun traduzirAtributo(atributo: String): String = when (atributo) {
    "hp" -> "HP"
    "attack" -> "Ataque"
    "defense" -> "Defesa"
    "special-attack" -> "Atq. especial"
    "special-defense" -> "Def. especial"
    "speed" -> "Velocidade"
    else -> formatarNome(atributo)
}
