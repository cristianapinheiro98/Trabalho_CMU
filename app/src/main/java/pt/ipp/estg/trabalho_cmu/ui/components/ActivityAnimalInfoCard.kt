package pt.ipp.estg.trabalho_cmu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pt.ipp.estg.trabalho_cmu.R

@Composable
fun ActivityAnimalInfoCard(
    animalName: String,
    shelterName: String,
    shelterContact: String? = null,
    shelterAddress: String,
    imageUrl: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // -------- IMAGEM DO ANIMAL --------
        AsyncImage(
            model = imageUrl ?: R.drawable.dog_image,
            contentDescription = "Imagem do Animal",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // -------- TEXTO --------
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
