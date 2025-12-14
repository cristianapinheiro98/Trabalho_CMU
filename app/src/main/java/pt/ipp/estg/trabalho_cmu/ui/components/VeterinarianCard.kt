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

/**
 * Composable component representing a single veterinarian card in the list.
 *
 * Displays essential details such as name, address, operating hours, and status (Open/Closed).
 * Includes interactive buttons for calling and viewing the location on a map.
 *
 * @param veterinarian The data object containing veterinarian details.
 * @param onPhoneClick Callback triggered when the "Call" button is pressed. Passes the phone number.
 * @param onMapClick Callback triggered when the "Map" button is pressed.
 * @param modifier Modifier for styling and layout customization.
 */
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
            // Name
            Text(
                text = veterinarian.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Address
            Text(
                text = veterinarian.address,
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Today's Schedule
            Text(
                text = veterinarian.todaySchedule,
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // State (Open/Closed)
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

            // Buttons
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