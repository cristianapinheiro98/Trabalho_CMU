package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.viewModelScope
import com.google.common.math.LinearTransformation.vertical
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.ui.components.calculateAge
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Shelter.ShelterViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalDetailScreen(
    shelterViewModel: ShelterViewModel,
    animalViewModel: AnimalViewModel,
    animalId: Int,
    onAdoptClick: () -> Unit = {},
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

    val imageGallery: List<String> = remember(animal) {
        val urls = animal!!.imageUrls
        if (urls.isNotEmpty()) urls else listOf("") // 1 URL vazia => placeholder
    }

    var mainImage by remember(imageGallery) {
        mutableStateOf(imageGallery.first())
    }


    val ageText = remember(animal!!.birthDate) {
        val age = calculateAge(animal!!.birthDate)
        age?.let { "$it anos" } ?: "N/A"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(rememberScrollState())
    ) {
        ImageGallery(
            mainImageUrl = mainImage,
            thumbnails = imageGallery,
            onThumbnailClick = { newUrl -> mainImage = newUrl }
        )

        Column(
            modifier = Modifier.padding(20.dp)
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

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            InfoLine(R.string.animal_breed_label, animal!!.breed)
            InfoLine(R.string.animal_size_label, animal!!.size)
            InfoLine(R.string.animal_birthdate_label, animal!!.birthDate)
            InfoLine(R.string.shelter_name_label, "Abrigo Porto Animal")

            Spacer(modifier = Modifier.height(24.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Olá! Sou ${animal!!.name} 🐾",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF303F9F)
            )

            Text(
                text = "Sou muito meiga e adoro atenção humana. Gosto de sestas longas e mantas fofas.",
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
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Adotar ${animal!!.name}", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun ImageGallery(
    mainImageUrl: String,
    thumbnails: List<String>,
    onThumbnailClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color.Black)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mainImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Imagem Principal",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.dog_image),
            error = painterResource(R.drawable.dog_image)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            thumbnails.forEach { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Miniatura",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onThumbnailClick(url) },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.dog_image),
                    error = painterResource(R.drawable.dog_image)
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
        Text(text = stringResource(id = label), fontWeight = FontWeight.Medium, fontSize = 15.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
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
            imageUrls = listOf(
                "https://placekitten.com/400/300",
                "https://placekitten.com/350/280",
                "https://placekitten.com/360/240"
            ),
            shelterId = 1
        )
    )

    override fun selectAnimal(id: Int) { /* no-op */ }
}
private class MockShelterViewModel : ShelterViewModel(repository = null) {
    override val selectedShelter: LiveData<Shelter?> = MutableLiveData(
        Shelter(
            id = 1,
            name = "Abrigo Porto",
            address = "Rua dos Animais, 123",
            contact = "912345678"
        )
    )

    override fun loadShelterById(shelterId: Int) = viewModelScope.launch {
        // no-op for preview
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AnimalDetailScreenPreview() {
    // ViewModel fake para o animal (já tinhas)
    val mockAnimalVM = Mock()

    // ShelterViewModel real só para o preview
    val mockShelterVM = MockShelterViewModel()

    MaterialTheme {
        AnimalDetailScreen(
            shelterViewModel = mockShelterVM,
            animalViewModel = mockAnimalVM,
            animalId = 1,
            onAdoptClick = {},
            onNavigateBack = {}
        )
    }
}

