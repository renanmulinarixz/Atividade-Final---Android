package br.com.cotemig.pokefav.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.cotemig.pokefav.viewmodel.AuthViewModel

@Composable
fun TelaCadastro(
    aoCadastrar: () -> Unit,
    aoVoltar: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    val erro by viewModel.erro.collectAsState()
    val carregando by viewModel.carregando.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo()
        Spacer(Modifier.height(32.dp))

        Text("Criar conta", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Azul)
        Spacer(Modifier.height(16.dp))

        CampoTexto(nome, { nome = it }, "Nome")
        Spacer(Modifier.height(12.dp))
        CampoTexto(email, { email = it }, "E-mail", tipoTeclado = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        CampoTexto(senha, { senha = it }, "Senha (mínimo 6 caracteres)", senha = true)

        if (erro != null) {
            Spacer(Modifier.height(12.dp))
            Text(erro ?: "", color = Vermelho, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.cadastrar(nome, email, senha, aoCadastrar) },
            enabled = !carregando,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (carregando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Text("Cadastrar", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = aoVoltar) {
            Text("Já tenho conta")
        }
    }
}
