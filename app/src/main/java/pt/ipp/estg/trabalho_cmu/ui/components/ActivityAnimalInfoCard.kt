package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R

@Composable
fun ActivityAnimalInfoCard(
    animalName: String,
    shelterName: String,
    shelterContact: String? = null,
    shelterAddress: String,
    imageRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagem do animal
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Imagem do Animal",
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = animalName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )

            Text(
                text = shelterName,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )

            shelterContact?.let {
                Text(
                    text = stringResource(R.string.contact) + ": $it",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            Text(
                text = shelterAddress,
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
                lineHeight = 14.sp
            )
        }
    }
}