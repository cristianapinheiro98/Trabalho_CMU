package pt.ipp.estg.trabalho_cmu.ui.screens.User

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PreferencesScreen(
    userViewModel: UserViewModel = viewModel(),
    userId: String // Recebe ID do user logado
) {
    // Observar dados do user via LiveData
    val user by userViewModel.user.observeAsState()

    // Carregar dados ao entrar
    LaunchedEffect(userId) {
        userViewModel.loadUserById(userId)
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("PT") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Notificações ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notificações:", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C3E50))
                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
            }
        }

        // --- Idioma ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Idioma da APP:", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C3E50))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedLanguage == "PT", onClick = { selectedLanguage = "PT" })
                        Text("PT")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedLanguage == "EN", onClick = { selectedLanguage = "EN" })
                        Text("EN")
                    }
                }
            }
        }

        // --- Dados Pessoais (Do Room/Firebase) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (user != null) {
                    UserInfoRow(Icons.Outlined.Person, user!!.name)
                    UserInfoRow(Icons.Outlined.Home, user!!.address)
                    UserInfoRow(Icons.Outlined.Phone, user!!.phone)
                    UserInfoRow(Icons.Outlined.Email, user!!.email)
                } else {
                    CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = { /* TODO: Navegar para edição */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Editar Dados Pessoais")
                }
            }
        }
    }
}

@Composable
fun UserInfoRow(icon: ImageVector, text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF2C3E50), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = Color(0xFF555555))
    }
}

@Preview
@Composable
private fun PreferenceScreenPreview() {
    MaterialTheme {
        PreferencesScreen(userId = "preview_id")
    }
}