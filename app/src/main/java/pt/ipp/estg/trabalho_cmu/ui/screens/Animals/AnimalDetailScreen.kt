package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.ui.components.calculateAge
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterViewModel
import pt.ipp.estg.trabalho_cmu.utils.dateStringToLong

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalDetailScreen(
    shelterViewModel: ShelterViewModel,
    animalViewModel: AnimalViewModel,
    animalId: String,
    showAdoptButton: Boolean,
    onAdoptClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // Observar dados via LiveData
    val animal by animalViewModel.selectedAnimal.observeAsState()
    val shelter by shelterViewModel.selectedShelter.observeAsState()

    // Carregar Animal ao entrar
    LaunchedEffect(animalId) {
        animalViewModel.selectAnimal(animalId)
    }

    // Carregar Abrigo quando tivermos o animal
    LaunchedEffect(animal) {
        animal?.let { shelterViewModel.loadShelterById(it.shelterId) }
    }

    if (animal == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    AnimalDetailScreenContent(
        animal = animal!!,
        shelter = shelter,
        showAdoptButton = showAdoptButton,
        onAdoptClick = onAdoptClick,
        onNavigateBack = onNavigateBack
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AnimalDetailScreenContent(
    animal: Animal,
    shelter: Shelter?,
    showAdoptButton: Boolean,
    onAdoptClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val imageGallery = animal.imageUrls.ifEmpty { listOf("") }
    var mainImage by remember { mutableStateOf(imageGallery.first()) }
    val ageText = calculateAge(animal.birthDate)?.let { "$it anos" } ?: "N/A"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        // --- Image Gallery ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Color.Black)
        ) {
            Column {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(mainImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.cat_image),
                    error = painterResource(R.drawable.cat_image)
                )

                // (Opcional) Aqui podias pôr uma lista horizontal de miniaturas se existissem várias fotos
            }

            // Botão de Voltar (canto superior direito)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End // Alinha tudo à direita
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x55000000), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // --- Detalhes ---
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = animal.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )
            Text(
                text = "${animal.species} • ${animal.size}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // Informações
            InfoLine(R.string.animal_breed_label, animal.breed)
            InfoLine(R.string.animal_age_label, ageText)
            InfoLine(R.string.shelter_name_label, shelter?.name ?: "Desconhecido")

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

            // Descrição
            Text(
                text = stringResource(R.string.description_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF303F9F)
            )
            Text(
                text = animal.description,
                fontSize = 15.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Botão Adotar
            if (showAdoptButton) {
                Button(
                    onClick = onAdoptClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(R.string.adopt_button), fontSize = 18.sp)
                }
            }
        }
    }
}

// Componente reutilizável para linhas de informação
@Composable
fun InfoLine(labelId: Int, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(labelId), fontWeight = FontWeight.Medium, fontSize = 15.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

// --- PREVIEWS ---

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
private fun PreviewAnimalDetail() {
    val mockAnimal = Animal(
        id = "mock-id-1",
        name = "Leia",
        breed = "Comum",
        species = "Gato",
        size = "Pequeno",
        birthDate = dateStringToLong("2019-01-01"),
        imageUrls = listOf(""),
        shelterId = "shelter-1",
        description = "Muito meiga"
    )
    MaterialTheme {
        AnimalDetailScreenContent(
            animal = mockAnimal,
            shelter = Shelter("s1", "Abrigo Teste", "Rua A", "911", "email"),
            showAdoptButton = true,
            onAdoptClick = {},
            onNavigateBack = {}
        )
    }
}
