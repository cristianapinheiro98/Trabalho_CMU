package pt.ipp.estg.trabalho_cmu.ui.screens.Veterinarians

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Veterinarian(
    val name: String,
    val schedule: String,
    val isOpen: Boolean
)

@Composable
fun VeterinariansScreen() {
    // Dados mock
    val veterinarians = listOf(
        Veterinarian("Centro Veterinário da Liza", "Horário: Seg a Sex 9h às 18h", true),
        Veterinarian("Centro Veterinário de Felgueiras", "Horário: Seg a Sáb 9h às 18h", true),
        Veterinarian("Centro Veterinário S. Jorge", "Horário: Seg a Sáb 9h às 18h", false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Centros Veterinários perto de ti",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(veterinarians) { vet ->
                VeterinarianCard(vet)
            }
        }
    }
}

@Composable
fun VeterinarianCard(veterinarian: Veterinarian) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = veterinarian.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = veterinarian.schedule,
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (veterinarian.isOpen) "ABERTO" else "FECHADO",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (veterinarian.isOpen) Color(0xFF4CAF50) else Color(0xFFE91E63)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* TODO: Abrir Google Maps */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Localização Google Maps",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}