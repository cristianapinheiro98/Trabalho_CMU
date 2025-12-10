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
import pt.ipp.estg.trabalho_cmu.utils.dateStringToLong

/**
 * High-level animal detail screen responsible for:
 * - Loading the selected animal and shelter data
 * - Waiting until both are available
 * - Displaying a loading indicator when necessary
 *
 * Delegates UI rendering to [AnimalDetailScreenContent].
 */
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
    val animal by animalViewModel.selectedAnimal.observeAsState()
    val shelter by shelterViewModel.selectedShelter.observeAsState()

    LaunchedEffect(animalId) {
        animalViewModel.selectAnimal(animalId)
    }

    LaunchedEffect(animal) {
        animal?.let { shelterViewModel.loadShelterById(it.shelterId) }
    }

    if (animal == null || shelter == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    AnimalDetailScreenContent(
        animal = animal!!,
        shelter = shelter!!,
        showAdoptButton = showAdoptButton,
        onAdoptClick = onAdoptClick,
        onNavigateBack = onNavigateBack
    )
}

/**
 * Displays the full content of the animal detail screen:
 * - Image gallery
 * - Animal attributes (breed, size, age)
 * - Shelter details
 * - Description section
 * - Adoption button
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AnimalDetailScreenContent(
    animal: Animal,
    shelter: Shelter,
    showAdoptButton: Boolean,
    onAdoptClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val imageGallery = animal.imageUrls.ifEmpty { listOf("") }
    var mainImage by remember { mutableStateOf(imageGallery.first()) }

    val age = calculateAge(animal.birthDate)
    val ageText = age?.let { "$it ${stringResource(R.string.age_years)}" }
        ?: stringResource(R.string.age_not_available)

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
            onThumbnailClick = { if (it.isNotBlank()) mainImage = it },
            onNavigateBack = onNavigateBack
        )

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

            // Info fields
            InfoLine(R.string.animal_breed_label, animal.breed)
            InfoLine(R.string.animal_age_label, ageText)

            InfoLine(R.string.shelter_name_label, shelter.name)
            InfoLine(R.string.shelter_address_label, shelter.address)
            InfoLine(R.string.shelter_email_label, shelter.email)
            InfoLine(R.string.shelter_phone_label, shelter.phone)

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

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
 * Reusable image gallery for the detail screen:
 * - Displays a main large image
 * - Shows a row of thumbnails
 * - Provides a back button overlay
 */
@Composable
fun ImageGallery(
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
 * Simple row composed of a label and a value.
 * Used to display animal and shelter attributes.
 */
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
