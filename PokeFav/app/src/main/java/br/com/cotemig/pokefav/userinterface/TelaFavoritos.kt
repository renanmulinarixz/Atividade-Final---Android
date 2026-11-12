package br.com.cotemig.pokefav.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cotemig.pokefav.viewmodel.FavoritosViewModel

@Composable
fun TelaFavoritos(
    aoAbrirPokemon: (Int) -> Unit,
    viewModel: FavoritosViewModel = viewModel()
) {
    val favoritos by viewModel.favoritos.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Titulo("Favoritos", "${favoritos.size} pokémon(s) salvo(s)")

        if (favoritos.isEmpty()) {
            // Lista vazia
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = Vermelho,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Nenhum favorito ainda", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Azul)
                Text(
                    "Abra um pokémon na Pokédex e toque em \"Favoritar\".",
                    color = Cinza,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoritos, key = { it.pokemonId }) { favorito ->
                    CardPokemon(favorito.pokemonId, favorito.nome) { aoAbrirPokemon(favorito.pokemonId) }
                }
            }
        }
    }
}
