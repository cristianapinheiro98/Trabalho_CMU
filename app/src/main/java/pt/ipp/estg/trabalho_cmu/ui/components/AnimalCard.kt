package pt.ipp.estg.trabalho_cmu.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


@Composable
fun AnimalCard(
    animal: Animal,
    isFavorite: Boolean = false,
    onClick: (() -> Unit)? = null,
    isLoggedIn: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    val age = calculateAge(animal.birthDate)

    val mainImageUrl: String =
        animal.imageUrls.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "placeholder"

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box {
                AsyncImage(
                    model = mainImageUrl,
                    contentDescription = animal.name,
                    placeholder = painterResource(R.drawable.cat_image),
                    error = painterResource(R.drawable.cat_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )

                if (isLoggedIn && onToggleFavorite != null) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = animal.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${age ?: "?"} anos",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateAge(birthDate: String?): Int? {
    if (birthDate.isNullOrBlank()) return null

    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateOfBirth = LocalDate.parse(birthDate, formatter)
        val today = LocalDate.now()

        Period.between(dateOfBirth, today).years
    } catch (e: DateTimeParseException) {
        null
    }
}



