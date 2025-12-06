package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.ui.components.MapLocationButton
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityAnimalInfoCard
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityDateTimeCard
import pt.ipp.estg.trabalho_cmu.utils.openGoogleMaps

/**
 * Screen showing the user's activity history (scheduled visits).
 */
@Composable
fun ActivitiesHistoryScreen(
    userId: Int,
    modifier: Modifier = Modifier
) {
    val activityViewModel: ActivityViewModel = viewModel()

    val scrollState = rememberScrollState()

    // Load user activities
    LaunchedEffect(userId) {
        activityViewModel.loadActivitiesForUser(userId)
    }

    val activitiesWithRelations by activityViewModel.activitiesWithDetails.observeAsState(emptyList())
    val isLoading by activityViewModel.isLoading.observeAsState(false)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2C8B7E)
                )
            }

            activitiesWithRelations.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_activities_scheduled),
                        fontSize = 16.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            else -> {
                ActivitiesHistoryContent(
                    activitiesWithRelations = activitiesWithRelations,
                    onDeleteActivity = { activity ->
                        activityViewModel.deleteActivity(activity)
                    }
                )
            }
        }
    }
}

@Composable
private fun ActivitiesHistoryContent(
    activitiesWithRelations: List<ActivityWithAnimalAndShelter>,
    onDeleteActivity: (Activity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.appointment_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2C2C),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        activitiesWithRelations.forEach { item ->
            val activity = item.activity
            val animal = item.animal
            val shelter = item.shelter
            val imageUrl = animal.imageUrls.firstOrNull()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB8D4D0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    ActivityAnimalInfoCard(
                        animalName = animal.name,
                        shelterName = shelter.name,
                        shelterContact = shelter.phone,
                        shelterAddress = shelter.address,
                        imageUrl = imageUrl
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MapLocationButton(
                        onClick = {
                            openGoogleMaps(context, shelter.address)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ActivityDateTimeCard(
                        pickupDate = activity.pickupDate,
                        pickupTime = activity.pickupTime,
                        deliveryDate = activity.deliveryDate,
                        deliveryTime = activity.deliveryTime
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cancel button
                    Button(
                        onClick = { onDeleteActivity(activity) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.cancel_visit_button))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Preview with mock data
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ActivitiesHistoryContentPreview() {
    MaterialTheme {
        val mockActivities = listOf(
            ActivityWithAnimalAndShelter(
                activity = Activity(
                    id = 1,
                    userId = 1,
                    animalId = 1,
                    pickupDate = "10/11/2025",
                    pickupTime = "09:00",
                    deliveryDate = "12/11/2025",
                    deliveryTime = "18:00"
                ),
                animal = Animal(
                    id = 0,
                    shelterFirebaseUid = "0",
                    name = "Mariana",
                    breed = "Golden Retriever",
                    birthDate = "14/05/2020",
                    size = "medium",
                    species = "dog",
                    imageUrls = listOf(""),
                    description = "Muito dócil"
                ),
                shelter = Shelter(
                    id = 0,
                    firebaseUid = "0",
                    name = "Abrigo de Felgueiras",
                    address = "Rua da Saúde, 1234 Santa Marta de Farto",
                    phone = "253 000 000",
                    email = "abrigo_felgueiras@gmail.com",
                    password = ""
                )
            ),
            ActivityWithAnimalAndShelter(
                activity = Activity(
                    id = 2,
                    userId = 1,
                    animalId = 2,
                    pickupDate = "15/11/2025",
                    pickupTime = "10:00",
                    deliveryDate = "17/11/2025",
                    deliveryTime = "17:00"
                ),
                animal = Animal(
                    id = 0,
                    shelterFirebaseUid = "0",
                    name = "Max",
                    breed = "Labrador",
                    birthDate = "20/03/2021",
                    size = "large",
                    species = "dog",
                    imageUrls = listOf("cat_image"),
                    description = "Muito dócil"
                ),
                shelter = Shelter(
                    id = 0,
                    firebaseUid = "0",
                    name = "Abrigo de Felgueiras",
                    address = "Rua da Saúde, 1234 Santa Marta de Farto",
                    phone = "253 000 000",
                    email = "abrigo_felgueiras@gmail.com",
                    password = ""
                )
            )
        )

        ActivitiesHistoryContent(
            activitiesWithRelations = mockActivities
        )
    }
}