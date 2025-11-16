package pt.ipp.estg.trabalho_cmu.ui.screens.Walk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun WalkSummaryScreen(
    navController: NavController,
    animalName: String
) {

    var showConfirmDialog by remember { mutableStateOf(false) }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar") },
            text = { Text("Tem a certeza que apenas quer guardar no histórico?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        navController.navigate("WalkHistory") {
                            popUpTo("UserHome") { inclusive = false }
                        }
                    }
                ) {
                    Text("Sim", color = Color(0xFF4CAF50))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 🗺️ Mapa Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🗺️ Mapa (placeholder)",
                fontSize = 18.sp,
                color = Color(0xFF757575)
            )
        }

        // 📊 Resumo do Passeio
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Resumo do Passeio",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Card com detalhes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    WalkInfoRow("Duração:", "1 hora")
                    WalkInfoRow("Distância:", "5km")
                    WalkInfoRow("Data:", "19/10/2025")

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    WalkInfoRow("Total:", "10 €")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de ação
            Button(
                onClick = { /* TODO: Partilhar na comunidade */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Partilhar na Comunidade SocialTails",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Guardar Apenas no Histórico Pessoal",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    // Descartar e voltar ao menu principal
                    navController.navigate("UserHome") {
                        popUpTo("UserHome") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE91E63)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Descartar Passeio",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Preview
@Composable
private fun WalkSummaryScreenPreview() {
    MaterialTheme {
        WalkSummaryScreen(navController = NavController(LocalContext.current), animalName = "Molly")

    }
}