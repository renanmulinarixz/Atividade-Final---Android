package br.com.cotemig.poketeam.userinterface

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cotemig.poketeam.data.PokemonItem
import br.com.cotemig.poketeam.data.pokemonNome
import br.com.cotemig.poketeam.network.PokemonDetailDto
import br.com.cotemig.poketeam.viewmodel.PokemonViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collect

@Composable
fun DetailScreen(pokemon: PokemonViewModel, pokemonId: Int, onBack: () -> Unit) {
    val detail by pokemon.detail.collectAsState()
    val favorites by pokemon.favorites.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(pokemonId) { pokemon.openDetail(pokemonId) }
    LaunchedEffect(pokemon) { pokemon.messages.collect { snackbar.showSnackbar(it) } }

    Scaffold(containerColor = Pale, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Navy)
                }
                Text("Detalhes do Pokémon", color = Navy, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
            when {
                detail.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Red)
                }
                detail.error != null -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(detail.error.orEmpty(), color = Red)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { pokemon.openDetail(pokemonId) }) { Text("Tentar novamente") }
                }
                detail.pokemon != null -> {
                    val item = detail.pokemon ?: return@Column
                    DetailContent(
                        item = item,
                        isFavorite = favorites.any { it.pokemonId == item.id },
                        onFavorite = { pokemon.toggleFavorite(PokemonItem(item.id, item.name)) }
                    )
                }
            }
        }
    }
}

private fun typeName(raw: String): String = when (raw) {
    "normal" -> "Normal"
    "fire" -> "Fogo"
    "water" -> "Água"
    "electric" -> "Elétrico"
    "grass" -> "Planta"
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
    "steel" -> "Metal"
    "fairy" -> "Fada"
    else -> raw.pokemonNome()
}

@Composable
private fun DetailContent(item: PokemonDetailDto, isFavorite: Boolean, onFavorite: () -> Unit) {
    val imageUrl = PokemonItem(item.id, item.name).imageUrl
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(230.dp).background(Color(0xFFFFE6E9), CircleShape))
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagem de " + item.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(240.dp)
            )
        }
        Text("#" + item.id.toString().padStart(3, '0'), color = Muted)
        Spacer(Modifier.height(6.dp))
        Text(item.name.pokemonNome(), fontSize = 32.sp, color = Navy, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(15.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item.types.forEach { type ->
                Box(
                    modifier = Modifier.background(Color(0xFFFFE5E7), RoundedCornerShape(50.dp))
                        .padding(horizontal = 15.dp, vertical = 8.dp)
                ) { Text(typeName(type.type.name), color = Red, fontWeight = FontWeight.SemiBold) }
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(onClick = onFavorite, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
            Text(if (isFavorite) "  Remover do meu time" else "  Adicionar ao meu time")
        }
        Spacer(Modifier.height(28.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Altura", color = Muted)
                Text((item.height / 10f).toString() + " m", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Peso", color = Muted)
                Text((item.weight / 10f).toString() + " kg", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Atributos", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(14.dp))
        StatLine(item, "hp", "HP")
        StatLine(item, "attack", "Ataque")
        StatLine(item, "defense", "Defesa")
        StatLine(item, "special-attack", "Ataque especial")
        StatLine(item, "special-defense", "Defesa especial")
        StatLine(item, "speed", "Velocidade")
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatLine(pokemon: PokemonDetailDto, key: String, title: String) {
    val value = pokemon.stats.firstOrNull { it.stat.name == key }?.base_stat ?: 0
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), color = Muted, fontSize = 13.sp)
        Text(value.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
        LinearProgressIndicator(
            progress = { (value / 200f).coerceIn(0f, 1f) },
            modifier = Modifier.size(width = 130.dp, height = 8.dp),
            color = Red,
            trackColor = Color(0xFFFFE0E4)
        )
    }
    Spacer(Modifier.height(13.dp))
}
