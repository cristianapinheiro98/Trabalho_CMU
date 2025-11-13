package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.os.Build
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.ui.components.calculateAge
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalDetailScreen(
    animalId: Int,
    animalViewModel: AnimalViewModel,
    shelterViewModel: ShelterViewModel,
    onAdoptClick: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val animal by animalViewModel.selectedAnimal.observeAsState()
    val shelter by shelterViewModel.selectedShelter.observeAsState()
    val isLoadingShelter by shelterViewModel.isLoading.observeAsState(false)
    val errorShelter by shelterViewModel.error.observeAsState()

    LaunchedEffect(animalId) {
        animalViewModel.selectAnimal(animalId)
    }

    LaunchedEffect(animal) {
        animal?.let {
            shelterViewModel.loadShelterById(it.shelterId)
        }
    }

    if (animal == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    AnimalDetailScreenContent(
        animal = animal!!,
        shelter = shelter,
        isLoadingShelter = isLoadingShelter,
        shelterError = errorShelter,
        onClearError = { shelterViewModel.clearError() },
        onAdoptClick = onAdoptClick,
        onNavigateBack = onNavigateBack
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalDetailScreenContent(
    animal: Animal,
    shelter: Shelter?,
    isLoadingShelter: Boolean,
    shelterError: String?,
    onClearError: () -> Unit,
    onAdoptClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val imageGallery = remember {
        if (animal.imageUrl.firstOrNull() is Int)
            animal.imageUrl as List<Int>
        else
            listOf(R.drawable.gato1, R.drawable.gato2, R.drawable.gato3)
    }

    var mainImage by remember { mutableStateOf(imageGallery.first()) }


    val ageText = remember(animal.birthDate) {
        val age = calculateAge(animal.birthDate)
        age?.let { "$it anos" } ?: "N/A"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(rememberScrollState())
    ) {

        Image(
            painter = painterResource(mainImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            imageGallery.forEach { img ->
                Image(
                    painter = painterResource(id = img),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { mainImage = img }
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(animal.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Text(
                "${animal.species} • ${animal.size}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            InfoRow("Raça", animal.breed ?: "Desconhecida")
            InfoRow("Nascimento", animal.birthDate ?: "N/A")
            InfoRow("Idade", ageText)

            InfoRow(
                "Abrigo",
                when {
                    isLoadingShelter -> "A carregar..."
                    shelter != null -> shelter.address
                    else -> "Desconhecido"
                }
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Descrição",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = animal.description ?: "Sem descrição disponível.",
                fontSize = 15.sp
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onAdoptClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adotar ${animal.name}")
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar")
            }
        }
    }

    shelterError?.let {
        AlertDialog(
            onDismissRequest = onClearError,
            confirmButton = {
                TextButton(onClick = onClearError) { Text("OK") }
            },
            title = { Text("Erro") },
            text = { Text(it) }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalDetailScreenContentPreview() {
    MaterialTheme {
        AnimalDetailScreenContent(
            animal = Animal(
                id = 1,
                name = "Miau",
                breed = "Europeu Comum",
                species = "Gato",
                size = "Pequeno",
                birthDate = "2020-01-01",
                description = "Um gato muito brincalhão e meigo, adora dormir ao sol e caçar brinquedos.",
                imageUrl = listOf(R.drawable.gato1, R.drawable.gato2),
                shelterId = 1
            ),
            shelter = Shelter(
                id = 1,
                name = "Abrigo Porto",
                contact = "912345679",
                address = "Rua dos Animais, 123"
            ),
            isLoadingShelter = false,
            shelterError = null,
            onClearError = {},
            onAdoptClick = {},
            onNavigateBack = {}
        )
    }
}
