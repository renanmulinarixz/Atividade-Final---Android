package br.com.cotemig.poketeam.userinterface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.com.cotemig.poketeam.viewmodel.AuthViewModel
import br.com.cotemig.poketeam.viewmodel.PokemonViewModel
import kotlinx.serialization.Serializable

@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object HomeRoute
@Serializable data class DetailRoute(val pokemonId: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokeTeamTheme {
                val auth: AuthViewModel = viewModel()
                val pokemon: PokemonViewModel = viewModel()
                RootNavigation(auth, pokemon)
            }
        }
    }
}

@Composable
private fun RootNavigation(auth: AuthViewModel, pokemon: PokemonViewModel) {
    val userId by auth.userId.collectAsState()
    key(userId) {
        val nav = rememberNavController()
        if (userId == null) {
            AuthNavigation(nav, auth)
        } else {
            val id = userId ?: return@key
            LaunchedEffect(id) { pokemon.selectUser(id) }
            AppNavigation(nav, auth, pokemon)
        }
    }
}

@Composable
private fun AuthNavigation(nav: NavHostController, auth: AuthViewModel) {
    NavHost(navController = nav, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(auth, onRegister = {
                auth.clearError()
                nav.navigate(RegisterRoute)
            })
        }
        composable<RegisterRoute> {
            RegisterScreen(auth, onBack = {
                auth.clearError()
                nav.popBackStack()
            })
        }
    }
}

@Composable
private fun AppNavigation(
    nav: NavHostController,
    auth: AuthViewModel,
    pokemon: PokemonViewModel
) {
    NavHost(navController = nav, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                auth = auth,
                pokemon = pokemon,
                onDetail = { id -> nav.navigate(DetailRoute(id)) }
            )
        }
        composable<DetailRoute> { entry ->
            val route = entry.toRoute<DetailRoute>()
            DetailScreen(
                pokemon = pokemon,
                pokemonId = route.pokemonId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
