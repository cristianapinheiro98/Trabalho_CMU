package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian

@Composable
fun VeterinarianCard(
    veterinarian: Veterinarian,
    onPhoneClick: (String) -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Nome
            Text(
                text = veterinarian.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Endereço
            Text(
                text = veterinarian.address,
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horário de hoje
            Text(
                text = veterinarian.todaySchedule,
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Estado (Aberto/Fechado)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (veterinarian.isOpenNow) "ABERTO" else "FECHADO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (veterinarian.isOpenNow) Color(0xFF4CAF50) else Color(0xFFE91E63)
                )

                // Rating (if it exists)
                veterinarian.rating?.let { rating ->
                    Text(
                        text = "⭐ ${String.format("%.1f", rating)}",
                        fontSize = 14.sp,
                        color = Color(0xFFFFA726)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhoneButton(
                    phoneNumber = veterinarian.phone,
                    modifier = Modifier.weight(1f),
                    text = "Ligar"
                )

                MapLocationButton(
                    onClick = onMapClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}