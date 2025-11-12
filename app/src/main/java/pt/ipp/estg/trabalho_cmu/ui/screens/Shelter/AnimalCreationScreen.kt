package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel

@Composable
fun AnimalCreationScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    viewModel: ShelterMngViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.observeAsState()
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            println("🟢 User carregado: ${user.name}, ID: ${user.id}, ShelterId: ${user.shelterId}")
            viewModel.getShelterIdByUserId(user.id)
        } ?: run {
            println("❌ ERRO: currentUser é null!")
        }
    }

    val form by viewModel.animalForm.observeAsState()
    val availableBreeds by viewModel.availableBreeds.observeAsState(emptyList())
    val isLoadingBreeds by viewModel.isLoadingBreeds.observeAsState(false)
    val message by viewModel.message.observeAsState()
    val error by viewModel.error.observeAsState()

    AnimalCreationScreenContent(
        form = form ?: pt.ipp.estg.trabalho_cmu.data.models.AnimalForm(),
        availableBreeds = availableBreeds,
        isLoadingBreeds = isLoadingBreeds,
        message = message,
        error = error,
        onNameChange = viewModel::onNameChange,
        onSpeciesChange = viewModel::onSpeciesChange,
        onBreedChange = viewModel::onBreedChange,
        onSizeChange = viewModel::onSizeChange,
        onBirthDateChange = viewModel::onBirthDateChange,
        onImageUrlChange = viewModel::onImageUrlChange,
        onSave = viewModel:: saveAnimal,
        onNavigateBack = onNavigateBack,
        onClearMessage = viewModel::clearMessage,
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalCreationScreenContent(
    form: pt.ipp.estg.trabalho_cmu.data.models.AnimalForm,
    availableBreeds: List<pt.ipp.estg.trabalho_cmu.data.models.Breed>,
    isLoadingBreeds: Boolean,
    message: String?,
    error: String?,
    onNameChange: (String) -> Unit,
    onSpeciesChange: (String) -> Unit,
    onBreedChange: (String) -> Unit,
    onSizeChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearMessage: () -> Unit,
    onClearError: () -> Unit
) {
    // Estado para controlar dropdowns
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

            // Espécie (Dropdown)
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
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
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

            // Raça (Dropdown dinâmico)
            ExposedDropdownMenuBox(
                expanded = expandedBreed,
                onExpandedChange = {
                    if (form.species.isNotBlank()) {
                        expandedBreed = !expandedBreed
                    }
                }
            ) {
                OutlinedTextField(
                    value = form.breed,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Raça") },
                    trailingIcon = {
                        if (isLoadingBreeds) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBreed)
                        }
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
                    if (availableBreeds.isEmpty() && !isLoadingBreeds) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma raça disponível") },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        availableBreeds.forEach { breed ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = breed.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        breed.description?.let { desc ->
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onBreedChange(breed.name)
                                    expandedBreed = false
                                }
                            )
                        }
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
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
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

            // URL da Imagem
            OutlinedTextField(
                value = form.imageUrl.toString(),
                onValueChange = onImageUrlChange,
                label = { Text("Imagem (ID)") },
                placeholder = { Text("Ex: 1, 2, 3...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
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
                    TextButton(onClick = onClearError) {
                        Text("OK")
                    }
                },
                title = { Text("Erro") },
                text = { Text(it) }
            )
        }
    }

    // Preview sem ViewModel (para evitar crash no preview)
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
                    pt.ipp.estg.trabalho_cmu.data.models.Breed("1", "Labrador", "Raça de cão"),
                    pt.ipp.estg.trabalho_cmu.data.models.Breed(
                        "2",
                        "Golden Retriever",
                        "Raça de cão"
                    ),
                    pt.ipp.estg.trabalho_cmu.data.models.Breed("3", "Beagle", "Raça de cão")
                ),
                isLoadingBreeds = false,
                message = null,
                error = null,
                onNameChange = {},
                onSpeciesChange = {},
                onBreedChange = {},
                onSizeChange = {},
                onBirthDateChange = {},
                onImageUrlChange = {},
                onSave = {},
                onNavigateBack = {},
                onClearMessage = {},
                onClearError = {}
            )
        }
    }
}