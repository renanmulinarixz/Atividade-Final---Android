package br.com.cotemig.pokefav.userinterface

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cotemig.pokefav.data.imagemPokemon
import coil.compose.AsyncImage

// "bulbasaur" -> "Bulbasaur", "mr-mime" -> "Mr mime"
fun formatarNome(nome: String): String =
    nome.replace("-", " ").replaceFirstChar { it.uppercase() }

// 25 -> "#025"
fun formatarNumero(id: Int): String = "#" + id.toString().padStart(3, '0')

// Pokébola desenhada com Canvas, usada como logo
@Composable
fun Pokebola(tamanho: Dp) {
    Canvas(modifier = Modifier.size(tamanho)) {
        val raio = size.minDimension / 2
        val borda = raio * 0.1f

        drawCircle(color = Color.White, radius = raio - borda / 2)
        drawArc(color = Vermelho, startAngle = 180f, sweepAngle = 180f, useCenter = true)
        drawCircle(color = Azul, radius = raio - borda / 2, style = Stroke(borda))
        drawLine(
            color = Azul,
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = borda
        )
        drawCircle(color = Color.White, radius = raio * 0.3f)
        drawCircle(color = Azul, radius = raio * 0.3f, style = Stroke(borda))
    }
}

// Logo com nome do app (telas de login e cadastro)
@Composable
fun Logo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Pokebola(tamanho = 90.dp)
        Spacer(Modifier.height(12.dp))
        Text("PokéFav", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
        Text("Seus pokémons favoritos em um só lugar", color = Cinza, textAlign = TextAlign.Center)
    }
}

// Título das abas
@Composable
fun Titulo(texto: String, subtitulo: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(texto, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
        Text(subtitulo, color = Cinza)
    }
}

// Campo de texto usado no login e no cadastro
@Composable
fun CampoTexto(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    senha: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        singleLine = true,
        visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (senha) KeyboardType.Password else tipoTeclado),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

// Estado de carregando
@Composable
fun Carregando() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Vermelho)
    }
}

// Estado de erro com botão para tentar de novo
@Composable
fun MensagemErro(mensagem: String, aoTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ops!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
        Spacer(Modifier.height(8.dp))
        Text(mensagem, color = Cinza, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = aoTentarNovamente) {
            Text("Tentar novamente")
        }
    }
}

// Card de um pokémon usado na Pokédex e nos Favoritos
@Composable
fun CardPokemon(id: Int, nome: String, aoClicar: () -> Unit) {
    Card(
        onClick = aoClicar,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(90.dp).background(VermelhoClaro, CircleShape))
                AsyncImage(
                    model = imagemPokemon(id),
                    contentDescription = nome,
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(formatarNumero(id), color = Cinza, fontSize = 12.sp)
            Text(formatarNome(nome), fontWeight = FontWeight.Bold, color = Azul, maxLines = 1)
        }
    }
}
