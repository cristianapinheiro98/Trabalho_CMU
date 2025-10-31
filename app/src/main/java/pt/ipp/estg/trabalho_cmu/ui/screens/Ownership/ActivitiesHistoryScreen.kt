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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.ui.components.MapLocationButton
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityAnimalInfoCard
import pt.ipp.estg.trabalho_cmu.ui.components.ActivityDateTimeCard


@Composable
fun ActivitiesHistoryScreen(
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    // Carregar atividades do utilizador
    LaunchedEffect(userId) {
        viewModel.loadActivitiesForUser(userId)
    }

    val activitiesWithRelations by viewModel.activitiesWithDetails.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

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
                    activitiesWithRelations = activitiesWithRelations
                )
            }
        }
    }
}

@Composable
private fun ActivitiesHistoryContent(
    activitiesWithRelations: List<ActivityWithAnimalAndShelter>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
                        shelterContact = shelter.contact,
                        shelterAddress = shelter.address,
                        imageRes = R.drawable.cat_image
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MapLocationButton(
                        onClick = {
                            // TODO: Abrir Google Maps com o endereço
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ Datas já são String, não precisa formatar
                    ActivityDateTimeCard(
                        pickupDate = activity.pickupDate,
                        pickupTime = activity.pickupTime,
                        deliveryDate = activity.deliveryDate,
                        deliveryTime = activity.deliveryTime
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ✅ PREVIEW - COM dados mock String
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ActivitiesHistoryContentPreview() {
    MaterialTheme {
        //  Mock data com datas STRING
        val mockActivities = listOf(
            ActivityWithAnimalAndShelter(
                activity = Activity(
                    id = 1,
                    userId = "user1",
                    animalId = "animal1",
                    pickupDate = "10/11/2025",
                    pickupTime = "09:00",
                    deliveryDate = "12/11/2025",
                    deliveryTime = "18:00"
                ),
                animal = Animal(
                    id = 0,
                    shelterId = 0,
                    name = "Mariana",
                    breed = "Golden Retriever",
                    birthDate = "14/05/2020",
                    size = "medium",
                    species = "dog",
                    imageUrl = R.drawable.cat_image
                ),
                shelter = Shelter(
                    id = 0,
                    name = "Abrigo de Felgueiras",
                    address = "Rua da Saúde, 1234 Santa Marta de Farto",
                    contact = "253 000 000"
                )
            ),
            ActivityWithAnimalAndShelter(
                activity = Activity(
                    id = 2,
                    userId = "user1",
                    animalId = "animal2",
                    pickupDate = "15/11/2025",
                    pickupTime = "10:00",
                    deliveryDate = "17/11/2025",
                    deliveryTime = "17:00"
                ),
                animal = Animal(
                    id = 0,
                    shelterId = 0,
                    name = "Max",
                    breed = "Labrador",
                    birthDate = "20/03/2021",
                    size = "large",
                    species = "dog",
                    imageUrl = R.drawable.cat_image
                ),
                shelter = Shelter(
                    id = 0,
                    name = "Abrigo de Felgueiras",
                    address = "Rua da Saúde, 1234 Santa Marta de Farto",
                    contact = "253 000 000"
                )
            )
        )

        ActivitiesHistoryContent(
            activitiesWithRelations = mockActivities
        )
    }
}