/*package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

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
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType
import pt.ipp.estg.trabalho_cmu.data.repository.CloudinaryRepository
import pt.ipp.estg.trabalho_cmu.data.repository.CloudinaryRepository.uploadImageToFirebase
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel


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
}*/

 */
package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

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
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel

/*
 * Main screen used by shelters to create new animals.
 *
 * Responsibilities:
 * - Observes animal creation form state from ShelterMngViewModel
 * - Loads available breeds, handles image selection and upload
 * - Displays success or error dialogs
 * - Resets state and navigates back when an animal is successfully created
 * - Adapts layout based on device size
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AnimalCreationScreen(
    windowSize: WindowWidthSizeClass,
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
                }
                shelterMngViewModel.setUploadingImages(false)
            }
        }
    }

    AnimalCreationScreenContent(
        windowSize = windowSize,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalCreationScreenContent(
    windowSize: WindowWidthSizeClass,
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
    val isTablet =
        windowSize == WindowWidthSizeClass.Medium || windowSize == WindowWidthSizeClass.Expanded
    val contentPadding = if (isTablet) 32.dp else 24.dp
    val fieldSpacing = if (isTablet) 20.dp else 16.dp
    val maxWidth = if (isTablet) 800.dp else 600.dp

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isTablet) {
                    TabletFormLayout(
                        form = form,
                        availableBreeds = availableBreeds,
                        isLoadingBreeds = isLoadingBreeds,
                        expandedBreed = expandedBreed,
                        expandedSpecies = expandedSpecies,
                        expandedSize = expandedSize,
                        onExpandedBreedChange = { expandedBreed = it },
                        onExpandedSpeciesChange = { expandedSpecies = it },
                        onExpandedSizeChange = { expandedSize = it },
                        onNameChange = onNameChange,
                        onSpeciesChange = onSpeciesChange,
                        onBreedChange = onBreedChange,
                        onSizeChange = onSizeChange,
                        onBirthDateChange = onBirthDateChange,
                        onDescriptionChange = onDescriptionChange,
                        speciesDog = speciesDog,
                        speciesCat = speciesCat,
                        fieldSpacing = fieldSpacing
                    )
                } else {
                    PhoneFormLayout(
                        form = form,
                        availableBreeds = availableBreeds,
                        isLoadingBreeds = isLoadingBreeds,
                        expandedBreed = expandedBreed,
                        expandedSpecies = expandedSpecies,
                        expandedSize = expandedSize,
                        onExpandedBreedChange = { expandedBreed = it },
                        onExpandedSpeciesChange = { expandedSpecies = it },
                        onExpandedSizeChange = { expandedSize = it },
                        onNameChange = onNameChange,
                        onSpeciesChange = onSpeciesChange,
                        onBreedChange = onBreedChange,
                        onSizeChange = onSizeChange,
                        onBirthDateChange = onBirthDateChange,
                        onDescriptionChange = onDescriptionChange,
                        speciesDog = speciesDog,
                        speciesCat = speciesCat,
                        fieldSpacing = fieldSpacing
                    )
                }

                Spacer(Modifier.height(if (isTablet) 32.dp else 24.dp))

                ImageSelectionSection(
                    selectedImages = selectedImages,
                    onSelectImages = onSelectImages,
                    isTablet = isTablet
                )

                Spacer(Modifier.height(fieldSpacing))

                SaveButton(
                    onSave = onSave,
                    isUploadingImages = isUploadingImages,
                    isTablet = isTablet
                )
            }
        }

        ResultDialogs(
            message = message,
            error = error,
            onClearMessage = onClearMessage,
            onClearError = onClearError,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun TabletFormLayout(
    form: AnimalForm,
    availableBreeds: List<Breed>,
    isLoadingBreeds: Boolean,
    expandedBreed: Boolean,
    expandedSpecies: Boolean,
    expandedSize: Boolean,
    onExpandedBreedChange: (Boolean) -> Unit,
    onExpandedSpeciesChange: (Boolean) -> Unit,
    onExpandedSizeChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    onSpeciesChange: (String) -> Unit,
    onBreedChange: (String) -> Unit,
    onSizeChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    speciesDog: String,
    speciesCat: String,
    fieldSpacing: androidx.compose.ui.unit.Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)
        ) {
            NameField(form.name, onNameChange)
            SpeciesDropdown(
                form.species,
                expandedSpecies,
                onExpandedSpeciesChange,
                onSpeciesChange,
                speciesDog,
                speciesCat
            )
            BreedDropdown(
                form.breed,
                form.species,
                expandedBreed,
                onExpandedBreedChange,
                onBreedChange,
                availableBreeds,
                isLoadingBreeds
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(fieldSpacing)
        ) {
            SizeDropdown(form.size, expandedSize, onExpandedSizeChange, onSizeChange)
            BirthDateField(form.birthDate, onBirthDateChange)
            DescriptionField(form.description, onDescriptionChange)
        }
    }
}

@Composable
private fun PhoneFormLayout(
    form: AnimalForm,
    availableBreeds: List<Breed>,
    isLoadingBreeds: Boolean,
    expandedBreed: Boolean,
    expandedSpecies: Boolean,
    expandedSize: Boolean,
    onExpandedBreedChange: (Boolean) -> Unit,
    onExpandedSpeciesChange: (Boolean) -> Unit,
    onExpandedSizeChange: (Boolean) -> Unit,
    onNameChange: (String) -> Unit,
    onSpeciesChange: (String) -> Unit,
    onBreedChange: (String) -> Unit,
    onSizeChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    speciesDog: String,
    speciesCat: String,
    fieldSpacing: androidx.compose.ui.unit.Dp
) {
    Column(verticalArrangement = Arrangement.spacedBy(fieldSpacing)) {
        NameField(form.name, onNameChange)
        SpeciesDropdown(
            form.species,
            expandedSpecies,
            onExpandedSpeciesChange,
            onSpeciesChange,
            speciesDog,
            speciesCat
        )
        BreedDropdown(
            form.breed,
            form.species,
            expandedBreed,
            onExpandedBreedChange,
            onBreedChange,
            availableBreeds,
            isLoadingBreeds
        )
        SizeDropdown(form.size, expandedSize, onExpandedSizeChange, onSizeChange)
        BirthDateField(form.birthDate, onBirthDateChange)
        DescriptionField(form.description, onDescriptionChange)
    }
}

@Composable
private fun NameField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.animal_name_label)) },
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeciesDropdown(
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    speciesDog: String,
    speciesCat: String
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.animal_species_label)) },
            trailingIcon = { TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.animal_species_dog)) },
                onClick = {
                    onValueChange(speciesDog)
                    onExpandedChange(false)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.animal_species_cat)) },
                onClick = {
                    onValueChange(speciesCat)
                    onExpandedChange(false)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreedDropdown(
    value: String,
    species: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    availableBreeds: List<Breed>,
    isLoadingBreeds: Boolean
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (species.isNotBlank()) onExpandedChange(!expanded)
        }
    ) {
        OutlinedTextField(
            value = value,
            readOnly = true,
            onValueChange = {},
            label = { Text(stringResource(R.string.animal_breed_label)) },
            trailingIcon = {
                if (isLoadingBreeds) CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
                else TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = species.isNotBlank()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            availableBreeds.forEach { breed ->
                DropdownMenuItem(
                    text = { Text(breed.name) },
                    onClick = {
                        onValueChange(breed.name)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeDropdown(
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) }
    ) {
        OutlinedTextField(
            value = value,
            readOnly = true,
            onValueChange = {},
            label = { Text(stringResource(R.string.animal_size_label)) },
            trailingIcon = { TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            listOf(
                stringResource(R.string.animal_size_small),
                stringResource(R.string.animal_size_medium),
                stringResource(R.string.animal_size_large),
            ).forEach { size ->
                DropdownMenuItem(
                    text = { Text(size) },
                    onClick = {
                        onValueChange(size)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun BirthDateField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.animal_birthdate_label)) },
        placeholder = { Text(stringResource(R.string.animal_birthdate_placeholder)) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DescriptionField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.animal_description_label)) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ImageSelectionSection(
    selectedImages: List<String>,
    onSelectImages: () -> Unit,
    isTablet: Boolean
) {
    Button(
        onClick = onSelectImages,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.select_images_button))
    }

    if (selectedImages.isNotEmpty()) {
        Spacer(Modifier.height(if (isTablet) 20.dp else 16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(selectedImages) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.size(if (isTablet) 120.dp else 100.dp)
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    onSave: () -> Unit,
    isUploadingImages: Boolean,
    isTablet: Boolean
) {
    Button(
        onClick = onSave,
        enabled = !isUploadingImages,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isTablet) 56.dp else 50.dp)
    ) {
        if (isUploadingImages) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                stringResource(R.string.save_animal_button),
                fontSize = if (isTablet) 18.sp else 16.sp
            )
        }
    }
}

@Composable
private fun ResultDialogs(
    message: String?,
    error: String?,
    onClearMessage: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit
) {
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

@Preview(name = "Phone", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
fun AnimalCreationPhonePreview() {
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
            windowSize = WindowWidthSizeClass.Compact,
            form = fakeForm,
            availableBreeds = listOf(Breed("1", "Labrador", "Raça de cão")),
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

@Preview(name = "Tablet", widthDp = 900, heightDp = 1280, showBackground = true)
@Composable
fun AnimalCreationTabletPreview() {
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
            windowSize = WindowWidthSizeClass.Expanded,
            form = fakeForm,
            availableBreeds = listOf(Breed("1", "Labrador", "Raça de cão")),
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