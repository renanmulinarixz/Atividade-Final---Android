package br.com.cotemig.poketeam.userinterface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.cotemig.poketeam.viewmodel.AuthViewModel

@Composable
private fun Logo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.background(Red, CircleShape).padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("◉", fontSize = 54.sp, color = Color.White)
        }
        Spacer(Modifier.height(18.dp))
        Text("PokéTeam", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text("Seu mundo Pokémon, seu time favorito.", color = Muted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FormField(value: String, onChange: (String) -> Unit, label: String, password: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else if (label == "E-mail") KeyboardType.Email else KeyboardType.Text),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthError(message: String?) {
    if (message != null) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }
}

@Composable
fun LoginScreen(auth: AuthViewModel, onRegister: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val error by auth.error.collectAsState()
    val busy by auth.busy.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().background(Pale).imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo()
        Spacer(Modifier.height(48.dp))
        Text("Boas-vindas de volta!", fontWeight = FontWeight.Bold, fontSize = 21.sp)
        Spacer(Modifier.height(18.dp))
        FormField(email, { email = it }, "E-mail")
        Spacer(Modifier.height(12.dp))
        FormField(password, { password = it }, "Senha", password = true)
        Spacer(Modifier.height(14.dp))
        AuthError(error)
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { auth.login(email, password) },
            enabled = !busy,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            if (busy) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            else Text("Entrar", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ainda não tem uma conta?", color = Muted)
            TextButton(onClick = onRegister) { Text("Cadastre-se") }
        }
    }
}

@Composable
fun RegisterScreen(auth: AuthViewModel, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val error by auth.error.collectAsState()
    val busy by auth.busy.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().background(Pale).imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 38.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo()
        Spacer(Modifier.height(28.dp))
        Text("Crie sua conta", fontWeight = FontWeight.Bold, fontSize = 23.sp)
        Spacer(Modifier.height(18.dp))
        FormField(name, { name = it }, "Nome")
        Spacer(Modifier.height(10.dp))
        FormField(email, { email = it }, "E-mail")
        Spacer(Modifier.height(10.dp))
        FormField(password, { password = it }, "Senha", password = true)
        Spacer(Modifier.height(10.dp))
        FormField(confirm, { confirm = it }, "Confirmar senha", password = true)
        Spacer(Modifier.height(12.dp))
        AuthError(error)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { auth.register(name, email, password, confirm) },
            enabled = !busy,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            if (busy) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            else Text("Criar conta", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onBack) { Text("Já tenho uma conta") }
    }
}
