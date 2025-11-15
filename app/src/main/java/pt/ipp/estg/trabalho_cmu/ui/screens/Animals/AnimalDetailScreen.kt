package pt.ipp.estg.trabalho_cmu.ui.screens.Animals

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

/**
 * Wrapper composable that loads both animal and shelter information
 * based on the provided animal ID. Displays loading state when necessary
 * and forwards data to the content composable.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalDetailScreen(
    shelterViewModel: ShelterViewModel,
    animalViewModel: AnimalViewModel,
    animalId: Int,
    showAdoptButton: Boolean,
    onAdoptClick: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val animal by animalViewModel.selectedAnimal.observeAsState()
    val shelter by shelterViewModel.selectedShelter.observeAsState()
    val isLoadingShelter by shelterViewModel.isLoading.observeAsState(false)

    // Load the selected animal when screen is opened
    LaunchedEffect(animalId) {
        animalViewModel.selectAnimal(animalId)
    }

    // Load shelter once animal data is available
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
        isLoadingShelter = isLoadingShelter,
        showAdoptButton = showAdoptButton,
        onAdoptClick = onAdoptClick,
        onNavigateBack = onNavigateBack
    )
}

/**
 * Main detail page content section:
 * - Image gallery
 * - Animal characteristics
 * - Shelter info
 * - Description
 * - Adopt button (optional)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AnimalDetailScreenContent(
    animal: Animal,
    shelter: Shelter?,
    isLoadingShelter: Boolean,
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

        ImageGallery(
            mainImageUrl = mainImage,
            thumbnails = imageGallery,
            onThumbnailClick = {
                if (it.isNotBlank()) mainImage = it
            },
            onNavigateBack = onNavigateBack
        )

        Column(modifier = Modifier.padding(20.dp)) {

            // Animal name
            Text(
                text = animal.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            // Species and size
            Text(
                text = "${animal.species} • ${animal.size}",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // Info table
            InfoLine(R.string.animal_breed_label, animal.breed)
            InfoLine(R.string.animal_size_label, animal.size)
            InfoLine(R.string.animal_age_label, ageText)

            // Shelter info
            InfoLine(
                labelId = R.string.shelter_name_label,
                value = when {
                    isLoadingShelter -> stringResource(R.string.loading_shelter)
                    shelter != null -> shelter.name
                    else -> stringResource(R.string.unknown_shelter)
                }
            )

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

            // Description section
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
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Adopt button
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

/**
 * Image gallery displaying the main image and a row of thumbnails.
 */
@Composable
private fun ImageGallery(
    mainImageUrl: String,
    thumbnails: List<String>,
    onThumbnailClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color.Black)
    ) {

        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mainImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.main_image_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.cat_image),
                error = painterResource(R.drawable.cat_image)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                thumbnails.filter { it.isNotBlank() }.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = stringResource(R.string.thumbnail_image_description),
                        modifier = Modifier
                            .size(60.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onThumbnailClick(url) },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.cat_image),
                        error = painterResource(R.drawable.cat_image)
                    )
                }
            }
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(12.dp)
                .size(36.dp)
                .align(Alignment.TopEnd)
                .background(Color(0x55000000), RoundedCornerShape(50))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.go_back),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Small reusable component that prints a label + value in one row.
 */
@Composable
fun InfoLine(labelId: Int, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(stringResource(labelId), fontWeight = FontWeight.Medium, fontSize = 15.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewAnimalDetailScreen() {

    val mockAnimal = Animal(
        id = 1,
        name = "Leia",
        breed = "Europeu Comum",
        species = "Gato",
        size = "Pequeno",
        birthDate = "2019-01-01",
        description = "Muito meiga, adora colo e mantinhas!",
        imageUrls = listOf(
            "https://placekitten.com/400/300",
            "https://placekitten.com/350/280",
            "https://placekitten.com/360/240"
        ),
        shelterId = 1
    )

    val mockShelter = Shelter(
        id = 1,
        name = "Abrigo Porto",
        address = "Rua dos Animais 123",
        phone = "912345678",
        email = "",
        password = ""
    )

    MaterialTheme {
        AnimalDetailScreenContent(
            animal = mockAnimal,
            shelter = mockShelter,
            isLoadingShelter = false,
            showAdoptButton = true,
            onAdoptClick = {},
            onNavigateBack = {}
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewAnimalDetailWithoutAuth() {

    val mockAnimal = Animal(
        id = 1,
        name = "Leia",
        breed = "Europeu Comum",
        species = "Gato",
        size = "Pequeno",
        birthDate = "2019-01-01",
        description = "Muito meiga, adora colo e mantinhas!",
        imageUrls = listOf(""),
        shelterId = 1
    )

    MaterialTheme {
        AnimalDetailScreenContent(
            animal = mockAnimal,
            shelter = null,
            isLoadingShelter = false,
            showAdoptButton = false,
            onAdoptClick = {},
            onNavigateBack = {}
        )
    }
}
