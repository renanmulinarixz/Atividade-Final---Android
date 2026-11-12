package br.com.cotemig.pokefav.userinterface

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

// Rotas type-safe do app
@Serializable object LoginRota
@Serializable object CadastroRota
@Serializable object PokedexRota
@Serializable object FavoritosRota
@Serializable object PerfilRota
@Serializable data class DetalheRota(val id: Int)

// Abas da barra inferior
data class Aba(val titulo: String, val icone: ImageVector, val rota: Any)

val abas = listOf(
    Aba("Pokédex", Icons.AutoMirrored.Filled.List, PokedexRota),
    Aba("Favoritos", Icons.Filled.Favorite, FavoritosRota),
    Aba("Perfil", Icons.Filled.Person, PerfilRota)
)

@Composable
fun AppNavegacao(logado: Boolean) {
    val navController = rememberNavController()
    val entradaAtual by navController.currentBackStackEntryAsState()
    val destinoAtual = entradaAtual?.destination

    // A barra inferior só aparece nas três abas principais
    val mostrarBarra = abas.any { destinoAtual?.hasRoute(it.rota::class) == true }

    Scaffold(
        containerColor = Fundo,
        bottomBar = {
            if (mostrarBarra) BarraInferior(navController, destinoAtual)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (logado) PokedexRota else LoginRota,
            modifier = Modifier.padding(padding)
        ) {
            // Login e cadastro
            composable<LoginRota> {
                TelaLogin(
                    aoEntrar = { irParaInicio(navController) },
                    aoIrParaCadastro = { navController.navigate(CadastroRota) }
                )
            }
            composable<CadastroRota> {
                TelaCadastro(
                    aoCadastrar = { irParaInicio(navController) },
                    aoVoltar = { navController.popBackStack() }
                )
            }

            // Abas
            composable<PokedexRota> {
                TelaPokedex(aoAbrirPokemon = { id -> navController.navigate(DetalheRota(id)) })
            }
            composable<FavoritosRota> {
                TelaFavoritos(aoAbrirPokemon = { id -> navController.navigate(DetalheRota(id)) })
            }
            composable<PerfilRota> {
                TelaPerfil(
                    aoSair = {
                        // Limpa toda a pilha e volta para o login
                        navController.navigate(LoginRota) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }

            // Detalhe recebe o id do pokémon pela rota
            composable<DetalheRota> { entrada ->
                val rota = entrada.toRoute<DetalheRota>()
                TelaDetalhe(
                    pokemonId = rota.id,
                    aoVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}

// Depois do login/cadastro vai para a Pokédex e tira o login da pilha
private fun irParaInicio(navController: NavHostController) {
    navController.navigate(PokedexRota) {
        popUpTo(LoginRota) { inclusive = true }
    }
}

@Composable
private fun BarraInferior(navController: NavHostController, destinoAtual: NavDestination?) {
    NavigationBar(containerColor = Color.White) {
        abas.forEach { aba ->
            NavigationBarItem(
                selected = destinoAtual?.hasRoute(aba.rota::class) == true,
                onClick = {
                    // Volta até a Pokédex antes de abrir a aba, para não empilhar telas repetidas
                    navController.navigate(aba.rota) {
                        popUpTo(PokedexRota)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(aba.icone, contentDescription = aba.titulo) },
                label = { Text(aba.titulo) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Vermelho,
                    selectedTextColor = Vermelho,
                    indicatorColor = VermelhoClaro
                )
            )
        }
    }
}
