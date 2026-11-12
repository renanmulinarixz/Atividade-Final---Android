package br.com.cotemig.pokefav.userinterface

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cores do app
val Vermelho = Color(0xFFE94C52)
val VermelhoClaro = Color(0xFFFFE6E9)
val Azul = Color(0xFF243149)
val Fundo = Color(0xFFF6F7FB)
val Cinza = Color(0xFF798498)

private val cores = lightColorScheme(
    primary = Vermelho,
    onPrimary = Color.White,
    background = Fundo,
    onBackground = Azul,
    surface = Color.White,
    onSurface = Azul,
    error = Vermelho
)

@Composable
fun PokeFavTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = cores, content = content)
}
