package pt.ipp.estg.trabalho_cmu.ui.screens.shelter

import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.models.AnimalForm
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.repository.CloudinaryRepository
import pt.ipp.estg.trabalho_cmu.ui.screens.auth.AuthViewModel


/**
 * Main screen used by shelters to create new animals.
 *
 * Responsibilities:
 * - Observes animal creation form state from ShelterMngViewModel
 * - Loads available breeds, handles image selection and upload
 * - Displays success or error dialogs
 * - Resets state and navigates back when an animal is successfully created
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalCreationScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    shelterMngViewModel: ShelterMngViewModel = viewModel()
) {
    val currentShelter by authViewModel.currentShelter.observeAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(currentShelter) {
        if (currentShelter != null) {
            shelterMngViewModel.setShelterFirebaseUid(currentShelter!!.id)
        }
    }
    Log.d("AnimalCreationScreen", "Shelter ID: ${currentShelter}")


    val form by shelterMngViewModel.animalForm.observeAsState(AnimalForm())
    val availableBreeds by shelterMngViewModel.availableBreeds.observeAsState(emptyList())
    val selectedImages by shelterMngViewModel.selectedImages.observeAsState(emptyList())
    val isLoadingBreeds by shelterMngViewModel.isLoadingBreeds.observeAsState(false)
    val isUploadingImages by shelterMngViewModel.isUploadingImages.observeAsState(false)

    val uiState by shelterMngViewModel.uiState.observeAsState(ShelterMngUiState.Initial)
    val message by shelterMngViewModel.message.observeAsState()
    val error by shelterMngViewModel.error.observeAsState()

    LaunchedEffect(uiState) {
        if (uiState is ShelterMngUiState.AnimalCreated) {
            shelterMngViewModel.resetState()
            onNavigateBack()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            scope.launch {
                shelterMngViewModel.setUploadingImages(true)

                val url = CloudinaryRepository.uploadImageToFirebase(context, uri)

                if (url != null) {
                    shelterMngViewModel.addImageUrl(url)
                } else {
                    shelterMngViewModel.setUploadingImages(false)
                }

                shelterMngViewModel.setUploadingImages(false)
            }
        }
    }

    AnimalCreationScreenContent(
        form = form,
        availableBreeds = availableBreeds,
        selectedImages = selectedImages,
        isLoadingBreeds = isLoadingBreeds,
        message = message,
        error = error,
        onNameChange = shelterMngViewModel::onNameChange,
        onSpeciesChange = shelterMngViewModel::onSpeciesChange,
        onBreedChange = shelterMngViewModel::onBreedChange,
        onSizeChange = shelterMngViewModel::onSizeChange,
        onBirthDateChange = shelterMngViewModel::onBirthDateChange,
        onDescriptionChange = shelterMngViewModel::onDescriptionChange,
        onSelectImages = { imagePicker.launch("image/*") },
        onSave = shelterMngViewModel::saveAnimal,
        onNavigateBack = onNavigateBack,
        onClearMessage = shelterMngViewModel::clearMessage,
        onClearError = shelterMngViewModel::clearError,
        isUploadingImages = isUploadingImages
    )
}

/**
 * UI content for the animal creation form.
 *
 * Handles:
 * - Form inputs (name, species, breed, size, birthdate, description)
 * - Breed dropdown populated dynamically
 * - Image selection previews
 * - Success/error dialogs
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalCreationScreenContent(
    form: AnimalForm,
    availableBreeds: List<Breed>,
    selectedImages: List<String>,
    isLoadingBreeds: Boolean,
    message: String?,
    error: String?,
    onNameChange: (String) -> Unit,
    onSpeciesChange: (String) -> Unit,
    onBreedChange: (String) -> Unit,
    onSizeChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSelectImages: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearMessage: () -> Unit,
    onClearError: () -> Unit,
    isUploadingImages: Boolean,
) {
    var expandedBreed by remember { mutableStateOf(false) }
    var expandedSpecies by remember { mutableStateOf(false) }
    var expandedSize by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val speciesDog = stringResource(R.string.animal_species_dog)
    val speciesCat = stringResource(R.string.animal_species_cat)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.animal_creation_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Name
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.animal_name_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Species
            ExposedDropdownMenuBox(
                expanded = expandedSpecies,
                onExpandedChange = { expandedSpecies = !expandedSpecies }
            ) {
                OutlinedTextField(
                    value = form.species,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.animal_species_label)) },
                    trailingIcon = { TrailingIcon(expanded = expandedSpecies) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedSpecies,
                    onDismissRequest = { expandedSpecies = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.animal_species_dog)) },
                        onClick = {
                            onSpeciesChange(speciesDog)
                            expandedSpecies = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.animal_species_cat)) },
                        onClick = {
                            onSpeciesChange(speciesCat)
                            expandedSpecies = false
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Breed
            ExposedDropdownMenuBox(
                expanded = expandedBreed,
                onExpandedChange = {
                    if (form.species.isNotBlank()) expandedBreed = !expandedBreed
                }
            ) {
                OutlinedTextField(
                    value = form.breed,
                    readOnly = true,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.animal_breed_label)) },
                    trailingIcon = {
                        if (isLoadingBreeds) CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        else TrailingIcon(expanded = expandedBreed)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    enabled = form.species.isNotBlank()
                )
                ExposedDropdownMenu(
                    expanded = expandedBreed,
                    onDismissRequest = { expandedBreed = false }
                ) {
                    availableBreeds.forEach { breed ->
                        DropdownMenuItem(
                            text = { Text(breed.name) },
                            onClick = {
                                onBreedChange(breed.name)
                                expandedBreed = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Size
            ExposedDropdownMenuBox(
                expanded = expandedSize,
                onExpandedChange = { expandedSize = !expandedSize }
            ) {
                OutlinedTextField(
                    value = form.size,
                    readOnly = true,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.animal_size_label)) },
                    trailingIcon = { TrailingIcon(expanded = expandedSize) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedSize,
                    onDismissRequest = { expandedSize = false }
                ) {
                    listOf(
                        stringResource(R.string.animal_size_small),
                        stringResource(R.string.animal_size_medium),
                        stringResource(R.string.animal_size_large),
                    ).forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size) },
                            onClick = {
                                onSizeChange(size)
                                expandedSize = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Birthdate
            OutlinedTextField(
                value = form.birthDate,
                onValueChange = onBirthDateChange,
                label = { Text(stringResource(R.string.animal_birthdate_label)) },
                placeholder = { Text(stringResource(R.string.animal_birthdate_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = form.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.animal_description_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Image selection button
            Button(
                onClick = onSelectImages,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.select_images_button))
            }

            if (selectedImages.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(selectedImages) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))


            Button(
                onClick = onSave,
                enabled = !isUploadingImages,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isUploadingImages) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.save_animal_button), fontSize = 16.sp)
                }
            }
        }

        // Success message dialog
        message?.let {
            AlertDialog(
                onDismissRequest = onClearMessage,
                confirmButton = {
                    TextButton(onClick = {
                        onClearMessage()
                        onNavigateBack()
                    }) { Text(stringResource(R.string.dialog_ok)) }
                },
                title = { Text(stringResource(R.string.success_title)) },
                text = { Text(it) }
            )
        }

        // Error message dialog
        error?.let {
            AlertDialog(
                onDismissRequest = onClearError,
                confirmButton = {
                    TextButton(onClick = onClearError) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                },
                title = { Text(stringResource(R.string.error_title)) },
                text = { Text(it) }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalCreationPreview() {
    val fakeForm = AnimalForm(
        name = "Rex",
        species = "Cão",
        breed = "Labrador",
        size = "Grande",
        birthDate = "01/01/2020",
        description = "Cão muito amigável"
    )

    MaterialTheme {
        AnimalCreationScreenContent(
            form = fakeForm,
            availableBreeds = listOf(
                Breed("1", "Labrador", "Raça de cão")
            ),
            selectedImages = emptyList(),
            isLoadingBreeds = false,
            message = null,
            error = null,
            onNameChange = {},
            onSpeciesChange = {},
            onBreedChange = {},
            onSizeChange = {},
            onBirthDateChange = {},
            onDescriptionChange = {},
            onSelectImages = {},
            onSave = {},
            onNavigateBack = {},
            onClearMessage = {},
            onClearError = {},
            isUploadingImages = false
        )
    }
}
