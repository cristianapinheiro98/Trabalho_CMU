package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalDetailScreen(
    viewModel: AnimalViewModel,
    animalId: Int,
    onAdoptClick: () -> Unit = {}
) {
    val animal by viewModel.selectedAnimal.observeAsState()

    LaunchedEffect(animalId) {
        viewModel.selectAnimal(animalId)
    }

    if (animal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val imageGallery: List<Int> = remember(animal) {
        val imgs = animal!!.imageUrl
        if (imgs.isNotEmpty() && imgs.first() is Int) {
            @Suppress("UNCHECKED_CAST")
            (imgs as List<Int>)
        } else {
            listOf(R.drawable.gato1, R.drawable.gato2, R.drawable.gato3)
        }
    }
    var mainImage by remember(imageGallery) { mutableStateOf(imageGallery.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(rememberScrollState())
    ) {
        ImageGallery(
            mainImage = mainImage,
            thumbnails = imageGallery,
            onThumbnailClick = { newImage -> mainImage = newImage }
        )

        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = animal!!.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Text(
                text = "${animal!!.species} • ${animal!!.size}",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))

            InfoLine("Raça", animal!!.breed ?: "Desconhecida")
            InfoLine("Porte", animal!!.size ?: "Desconhecido")
            InfoLine("Nascimento", animal!!.birthDate ?: "N/A")
            InfoLine("Abrigo", "Abrigo Porto Animal")

            Spacer(modifier = Modifier.height(24.dp))
            Divider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Olá! Sou ${animal!!.name} 🐾",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF303F9F)
            )

            Text(
                text = "Sou muito meiga e adoro atenção humana. Gosto de sestas longas e mantas fofas. Não gosto de aspiradores barulhentos e prefiro ambientes calmos.",
                fontSize = 15.sp,
                textAlign = TextAlign.Start,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAdoptClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
            ) {
                Text("Adotar ${animal!!.name}", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ImageGallery(
    @DrawableRes mainImage: Int,
    thumbnails: List<Int>,
    onThumbnailClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = mainImage),
            contentDescription = "Imagem Principal",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            thumbnails.forEach { thumbnail ->
                Image(
                    painter = painterResource(id = thumbnail),
                    contentDescription = "Miniatura",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onThumbnailClick(thumbnail) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}
private class Mock : AnimalViewModel(repository = null) {
    override val selectedAnimal: LiveData<Animal?> = MutableLiveData(
        Animal(
            id = 1,
            name = "Leia",
            breed = "Europeu Comum",
            species = "Gato",
            size = "Pequeno",
            birthDate = "2019-01-01",
            imageUrl = listOf(R.drawable.gato1, R.drawable.gato2, R.drawable.gato3),
            shelterId = 1
        )
    )

    override fun selectAnimal(id: Int) { /* no-op for preview */ }
}

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AnimalDetailScreenPreview() {
    val mockViewModel = Mock()
    MaterialTheme {
        AnimalDetailScreen(viewModel = mockViewModel, animalId = 1)
    }
}
