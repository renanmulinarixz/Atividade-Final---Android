package br.com.cotemig.pokefav.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cotemig.pokefav.viewmodel.ListaViewModel
import br.com.cotemig.pokefav.viewmodel.UiState

@Composable
fun TelaPokedex(
    aoAbrirPokemon: (Int) -> Unit,
    viewModel: ListaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var busca by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Titulo("Pokédex", "Toque em um pokémon para ver os detalhes")

        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            placeholder = { Text("Buscar pelo nome") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )

        // Mostra a tela de acordo com o estado da requisição
        when (val atual = estado) {
            is UiState.Carregando -> Carregando()

            is UiState.Erro -> MensagemErro(atual.mensagem) { viewModel.carregar() }

            is UiState.Sucesso -> {
                val lista = atual.dados.filter { it.name.contains(busca.trim(), ignoreCase = true) }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.id }) { pokemon ->
                        CardPokemon(pokemon.id, pokemon.name) { aoAbrirPokemon(pokemon.id) }
                    }
                }
            }
        }
    }
}
