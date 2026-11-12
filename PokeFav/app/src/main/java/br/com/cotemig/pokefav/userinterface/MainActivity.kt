package br.com.cotemig.pokefav.userinterface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cotemig.pokefav.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokeFavTheme {
                // Confere uma vez se já existe alguém logado para decidir a tela inicial
                val authViewModel: AuthViewModel = viewModel()
                val logado = rememberSaveable { authViewModel.estaLogado() }

                AppNavegacao(logado = logado)
            }
        }
    }
}
