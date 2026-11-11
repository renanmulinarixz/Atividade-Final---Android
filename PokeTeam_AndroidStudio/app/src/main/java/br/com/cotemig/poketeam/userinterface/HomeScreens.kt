package br.com.cotemig.poketeam.userinterface

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cotemig.poketeam.data.PokemonItem
import br.com.cotemig.poketeam.data.pokemonNome
import br.com.cotemig.poketeam.viewmodel.AuthViewModel
import br.com.cotemig.poketeam.viewmodel.PokemonViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collect

@Composable
fun HomeScreen(auth: AuthViewModel, pokemon: PokemonViewModel, onDetail: (Int) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(pokemon) {
        pokemon.messages.collect { snackbar.showSnackbar(it) }
    }
    Scaffold(
        containerColor = Pale,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Pokédex") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Meu time") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            when (tab) {
                0 -> PokedexTab(pokemon, onDetail)
                1 -> FavoritesTab(pokemon, onDetail, onExplore = { tab = 0 })
                else -> ProfileTab(auth, pokemon)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = Muted)
    }
}

@Composable
fun PokemonCard(item: PokemonItem, favorite: Boolean, onClick: () -> Unit, onFavorite: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("#" + item.id.toString().padStart(3, '0'), color = Muted, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (favorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                        tint = Red
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(98.dp).background(Color(0xFFFFEFF0), CircleShape))
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "Imagem do Pokémon " + item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(110.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(item.name.pokemonNome(), fontWeight = FontWeight.Bold, color = Navy, maxLines = 1)
            Spacer(Modifier.height(5.dp))
            Text("Ver detalhes", color = Red, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PokedexTab(pokemon: PokemonViewModel, onDetail: (Int) -> Unit) {
    val state by pokemon.catalog.collectAsState()
    val favorites by pokemon.favorites.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = state.items.filter { it.name.contains(query.trim(), ignoreCase = true) }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { SectionHeader("Pokédex", "Descubra e escolha seu time.") }
            IconButton(onClick = { pokemon.refresh() }, modifier = Modifier.padding(end = 12.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Atualizar Pokémon", tint = Red)
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Buscar entre os Pokémon carregados") },
            shape = RoundedCornerShape(20.dp),
            singleLine = true
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                PokemonCard(
                    item = item,
                    favorite = favorites.any { it.pokemonId == item.id },
                    onClick = { onDetail(item.id) },
                    onFavorite = { pokemon.toggleFavorite(item) }
                )
            }
            item {
                if (state.loading) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Red)
                    }
                }
            }
            item {
                if (state.error != null) {
                    Column(Modifier.padding(12.dp)) {
                        Text(state.error.orEmpty(), color = Red, fontSize = 13.sp)
                        OutlinedButton(onClick = { pokemon.loadMore() }) { Text("Tentar novamente") }
                    }
                } else if (!state.loading && state.hasMore && query.isBlank()) {
                    OutlinedButton(onClick = { pokemon.loadMore() }) { Text("Carregar mais") }
                }
            }
            if (query.isNotBlank() && filtered.isEmpty()) {
                item { Text("Nenhum Pokémon encontrado nesta página.", color = Muted) }
            }
        }
    }
}

@Composable
private fun FavoritesTab(pokemon: PokemonViewModel, onDetail: (Int) -> Unit, onExplore: () -> Unit) {
    val favorites by pokemon.favorites.collectAsState()
    Column(Modifier.fillMaxSize()) {
        SectionHeader("Meu time", "" + favorites.size + "/6 Pokémon selecionados")
        if (favorites.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Red, modifier = Modifier.size(58.dp))
                Spacer(Modifier.height(16.dp))
                Text("Seu time está vazio", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Explore a Pokédex e escolha até 6 Pokémon.", color = Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onExplore) { Text("Explorar Pokémon") }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                items(favorites, key = { it.pokemonId }) { fav ->
                    PokemonCard(
                        item = PokemonItem(fav.pokemonId, fav.nome),
                        favorite = true,
                        onClick = { onDetail(fav.pokemonId) },
                        onFavorite = { pokemon.toggleFavorite(PokemonItem(fav.pokemonId, fav.nome)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(auth: AuthViewModel, pokemon: PokemonViewModel) {
    val user by auth.user.collectAsState()
    val favorites by pokemon.favorites.collectAsState()
    Column(Modifier.fillMaxSize()) {
        SectionHeader("Meu perfil", "Sua conta de treinador")
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier.size(72.dp).background(Color(0xFFFFE7EA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Red, modifier = Modifier.size(42.dp))
                }
                Spacer(Modifier.height(18.dp))
                Text(user?.nome ?: "Treinador", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(user?.email ?: "Carregando perfil...", color = Muted)
                Spacer(Modifier.height(24.dp))
                Text("Pokémon no meu time", color = Muted)
                Text(favorites.size.toString() + " de 6", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Red)
            }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = { auth.logout() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Text("  Sair da conta")
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Dados da PokéAPI • Salvos localmente no aparelho",
            color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )
    }
}
