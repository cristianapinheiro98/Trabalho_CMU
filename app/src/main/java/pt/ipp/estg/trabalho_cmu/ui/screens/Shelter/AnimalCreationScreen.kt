package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pt.ipp.estg.trabalho_cmu.data.models.Breed
import pt.ipp.estg.trabalho_cmu.data.remote.api.services.uploadImageToFirebase
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel

@Composable
fun AnimalCreationScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    viewModel: ShelterMngViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.observeAsState()
    val currentShelter by authViewModel.currentShelter.observeAsState()
    val accountType by authViewModel.accountType.observeAsState()

    LaunchedEffect(accountType) {
        when (accountType) {
            AccountType.SHELTER -> {
                currentShelter?.let { shelter ->
                    println("🟢 Shelter: ${shelter.name}, ID: ${shelter.id}")
                    viewModel.setShelterId(shelter.id)
                }
            }
            AccountType.USER -> {
                println("⚠️ User não pode criar animais!")
            }
            else -> {
                println("❌ Tipo de conta desconhecido")
            }
        }
    }

    val form by viewModel.animalForm.observeAsState()
    val availableBreeds by viewModel.availableBreeds.observeAsState(emptyList())
    val isLoadingBreeds by viewModel.isLoadingBreeds.observeAsState(false)
    val selectedImages by viewModel.selectedImages.observeAsState(emptyList())
    val message by viewModel.message.observeAsState()
    val error by viewModel.error.observeAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            uploadImageToFirebase(
                uri = uri,
                onSuccess = { url ->
                    viewModel.addImageUrl(url)
                },
                onError = {
                    viewModel.clearError()
                }
            )
        }
    }

    AnimalCreationScreenContent(
        form = form ?: pt.ipp.estg.trabalho_cmu.data.models.AnimalForm(),
        availableBreeds = availableBreeds,
        selectedImages = selectedImages,
        isLoadingBreeds = isLoadingBreeds,
        message = message,
        error = error,
        onNameChange = viewModel::onNameChange,
        onSpeciesChange = viewModel::onSpeciesChange,
        onBreedChange = viewModel::onBreedChange,
        onSizeChange = viewModel::onSizeChange,
        onBirthDateChange = viewModel::onBirthDateChange,
        onSelectImages = { imagePicker.launch("image/*") },
        onSave = viewModel::saveAnimal,
        onNavigateBack = onNavigateBack,
        onClearMessage = viewModel::clearMessage,
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalCreationScreenContent(
    form: pt.ipp.estg.trabalho_cmu.data.models.AnimalForm,
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
    onSelectImages: () -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearMessage: () -> Unit,
    onClearError: () -> Unit
) {
    // Estados dos dropdowns
    var expandedBreed by remember { mutableStateOf(false) }
    var expandedSpecies by remember { mutableStateOf(false) }
    var expandedSize by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Novo Animal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Nome
            OutlinedTextField(
                value = form.name,
                onValueChange = onNameChange,
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Espécie
            ExposedDropdownMenuBox(
                expanded = expandedSpecies,
                onExpandedChange = { expandedSpecies = !expandedSpecies }
            ) {
                OutlinedTextField(
                    value = form.species,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Espécie") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpecies)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedSpecies,
                    onDismissRequest = { expandedSpecies = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cão") },
                        onClick = {
                            onSpeciesChange("Cão")
                            expandedSpecies = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Gato") },
                        onClick = {
                            onSpeciesChange("Gato")
                            expandedSpecies = false
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Raças
            ExposedDropdownMenuBox(
                expanded = expandedBreed,
                onExpandedChange = {
                    if (form.species.isNotBlank()) expandedBreed = !expandedBreed
                }
            ) {
                OutlinedTextField(
                    value = form.breed,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Raça") },
                    trailingIcon = {
                        if (isLoadingBreeds) CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        else ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBreed)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    enabled = form.species.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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

            // Tamanho
            ExposedDropdownMenuBox(
                expanded = expandedSize,
                onExpandedChange = { expandedSize = !expandedSize }
            ) {
                OutlinedTextField(
                    value = form.size,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tamanho") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSize)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedSize,
                    onDismissRequest = { expandedSize = false }
                ) {
                    listOf("Pequeno", "Médio", "Grande").forEach { size ->
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

            // Data de Nascimento
            OutlinedTextField(
                value = form.birthDate,
                onValueChange = onBirthDateChange,
                label = { Text("Data de Nascimento") },
                placeholder = { Text("DD/MM/AAAA") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Botão selecionar imagens
            Button(
                onClick = onSelectImages,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Escolher imagens")
            }

            // Preview das imagens escolhidas
            if (selectedImages.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(selectedImages) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botão Guardar
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar", fontSize = 16.sp)
            }
        }

        // Diálogo de sucesso
        message?.let {
            AlertDialog(
                onDismissRequest = onClearMessage,
                confirmButton = {
                    TextButton(onClick = {
                        onClearMessage()
                        onNavigateBack()
                    }) {
                        Text("OK")
                    }
                },
                title = { Text("Sucesso") },
                text = { Text(it) }
            )
        }

        // Diálogo de erro
        error?.let {
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalCreationPreview() {
    MaterialTheme {
        AnimalCreationScreenContent(
            form = pt.ipp.estg.trabalho_cmu.data.models.AnimalForm(
                name = "Rex",
                species = "Cão",
                breed = "Labrador",
                size = "Grande",
                birthDate = "01/01/2020",
                imageUrl = 1
            ),
            availableBreeds = listOf(
                Breed("1", "Labrador", "Raça de cão"),
                Breed("2", "Golden Retriever", "Raça de cão"),
                Breed("3", "Beagle", "Raça de cão")
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
            onSelectImages = {},
            onSave = {},
            onNavigateBack = {},
            onClearMessage = {},
            onClearError = {}
        )
    }
}

