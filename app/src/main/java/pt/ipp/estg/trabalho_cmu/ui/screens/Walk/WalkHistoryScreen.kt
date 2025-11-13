package pt.ipp.estg.trabalho_cmu.ui.screens.Walk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WalkHistoryItem(
    val animalName: String,
    val duration: String,
    val distance: String,
    val date: String
)

@Composable
fun WalkHistoryScreen() {
    // mock
    val walkHistory = listOf(
        WalkHistoryItem("Molly", "1 hora", "5km", "19/10/2025"),
        WalkHistoryItem("Bolinhas", "45 minutos", "4km", "11/10/2025"),
        WalkHistoryItem("Max", "2 horas", "8km", "05/10/2025")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Placeholder Map
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

        // Walks list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Histórico de Passeios",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(walkHistory) { walk ->
                    WalkHistoryCard(walk)
                }
            }
        }
    }
}

@Composable
fun WalkHistoryCard(walk: WalkHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Animal: ${walk.animalName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Duração: ${walk.duration}",
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )
            Text(
                text = "Distância: ${walk.distance}",
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )
            Text(
                text = "Data: ${walk.date}",
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )
        }
    }
}