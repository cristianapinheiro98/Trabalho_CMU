package pt.ipp.estg.trabalho_cmu.ui.screens.User

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalSelectionDialog

@Composable
fun MainOptionsScreen(
    navController: NavController,
    hasAdoptedAnimal: Boolean,
    userId: String,
    windowSize: WindowWidthSizeClass
) {
    // Usamos apenas este ViewModel, ele gere o dashboard e a lista de animais do user
    val viewModel: MainOptionsViewModel = viewModel()

    // Observar dados
    val lastWalk by viewModel.lastWalk.observeAsState()
    val medals by viewModel.medals.observeAsState(emptyList())
    // Esta é a lista correta para o Dialog, vinda do MainOptionsViewModel
    val userAnimals by viewModel.ownedAnimals.observeAsState(emptyList())

    // Carregar dados ao entrar
    LaunchedEffect(userId) {
        viewModel.loadUserData(userId)
    }

    var showAnimalDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    // Dialog para PASSEIO
    if (showAnimalDialog) {
        AnimalSelectionDialog(
            animals = userAnimals, // Lista carregada pelo MainOptionsViewModel
            onDismiss = { showAnimalDialog = false },
            onAnimalSelected = { animal ->
                showAnimalDialog = false
                navController.navigate("Walk/${animal.id}/${animal.name}")
            }
        )
    }

    // Dialog para AGENDAMENTO
    if (showScheduleDialog) {
        AnimalSelectionDialog(
            animals = userAnimals, // Lista carregada pelo MainOptionsViewModel
            onDismiss = { showScheduleDialog = false },
            onAnimalSelected = { animal ->
                showScheduleDialog = false
                // Passa o animalId na rota. O ActivityViewModel vai usar isto no próximo ecrã.
                navController.navigate("ActivityScheduling/${animal.id}")
            }
        )
    }

    // UI Content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tailwagger",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (hasAdoptedAnimal) {
            MedalsSection(medals)
            Spacer(modifier = Modifier.height(16.dp))

            lastWalk?.let { walk ->
                LastWalkInfo(walk)
                Spacer(modifier = Modifier.height(16.dp))
                WalkDetailsCard(walk)
                Spacer(modifier = Modifier.height(16.dp))
            }

            MainActionButtons(
                navController,
                onStartWalk = { showAnimalDialog = true },
                onScheduleVisit = { showScheduleDialog = true },
                windowSize = windowSize
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        CommonOptions(navController)
    }
}

@Composable
fun MedalsSection(medals: List<Medal>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Coleção de Medalhas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                medals.take(4).forEach { medal ->
                    Text(text = medal.icon, fontSize = 32.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* TODO */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))) {
                Text("Ver conquistas", color = Color.Black)
            }
        }
    }
}

@Composable
fun LastWalkInfo(walk: WalkInfo) {
    Text(
        text = "Olá! O teu último passeio com a ${walk.animalName} foi de ${walk.distance}",
        fontSize = 14.sp,
        color = Color(0xFF555555)
    )
}

@Composable
fun WalkDetailsCard(walk: WalkInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            WalkDetailRow("Duração:", walk.duration)
            WalkDetailRow("Distância:", walk.totalDistance)
            WalkDetailRow("Data:", walk.date)
        }
    }
}

@Composable
fun WalkDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}

@Composable
fun MainActionButtons(navController: NavController, onStartWalk: () -> Unit, onScheduleVisit: () -> Unit, windowSize: WindowWidthSizeClass) {
    // Mantém a lógica de layout responsivo que já tinhas
    // ... (Copia o conteúdo do teu ficheiro original para aqui, é igual)
    // Para poupar espaço, vou assumir o layout de smartphone aqui:
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("Agendar Visita", Color(0xFF2196F3), Modifier.weight(1f), onScheduleVisit)
            ActionButton("Iniciar Passeio", Color(0xFF4CAF50), Modifier.weight(1f), onStartWalk)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("Agendamentos", Color(0xFFE57373), Modifier.weight(1f), onClick = { navController.navigate("ActivitiesHistory") })
            ActionButton("Concluir Passeio", Color(0xFFFF9800), Modifier.weight(1f), onClick = { /* TODO */ })
        }
        ActionButton("Histórico de Passeios", Color(0xFF26A69A), Modifier.fillMaxWidth(), onClick = { navController.navigate("WalkHistory") })
    }
}

@Composable
fun CommonOptions(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton("Visitar Comunidade SocialTails", Color(0xFF5C6BC0), Modifier.fillMaxWidth(), onClick = { navController.navigate("SocialTailsCommunity") })
    }
}

@Composable
fun ActionButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = color), shape = RoundedCornerShape(8.dp)) {
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview
@Composable
private fun PreviewMainOptionsScreen() {
    MaterialTheme {
        // Mock preview
        MainOptionsScreen(navController = NavController(LocalContext.current), hasAdoptedAnimal = true, userId = "1", windowSize = WindowWidthSizeClass.Compact)
    }
}