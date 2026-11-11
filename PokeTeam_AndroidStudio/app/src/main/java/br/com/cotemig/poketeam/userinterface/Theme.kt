package br.com.cotemig.poketeam.userinterface

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Red = Color(0xFFE94C52)
val Navy = Color(0xFF243149)
val Pale = Color(0xFFF6F7FB)
val Muted = Color(0xFF798498)

private val colors = lightColorScheme(
    primary = Red,
    onPrimary = Color.White,
    background = Pale,
    surface = Color.White,
    onBackground = Navy,
    onSurface = Navy
)

@Composable
fun PokeTeamTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, content = content)
}
