package br.com.cotemig.pokefav.userinterface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cotemig.pokefav.viewmodel.AuthViewModel

@Composable
fun TelaPerfil(
    aoSair: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val usuario by viewModel.usuario.collectAsState()

    // Busca os dados do usuário logado quando a tela abre
    LaunchedEffect(Unit) {
        viewModel.carregarUsuario()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Titulo("Perfil", "Sua conta de treinador")

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(80.dp).background(VermelhoClaro, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Vermelho, modifier = Modifier.size(44.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(usuario?.nome ?: "", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Azul)
                Text(usuario?.email ?: "", color = Cinza)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Botão Sair: limpa a sessão e volta para o login
        OutlinedButton(
            onClick = { viewModel.sair(aoSair) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sair")
        }
    }
}
