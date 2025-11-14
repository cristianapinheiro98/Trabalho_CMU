package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.ui.screens.SocialTailsComunity.RankingData

@Composable
fun RankingCard(rank: RankingData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone do Troféu (emoji)
            Text(
                text = "🏆",
                fontSize = 24.sp,
                color = rank.trophyColor
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Detalhes do ranking
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${rank.position}º ${rank.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                )

                Text(
                    text = "${rank.distance} km",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}