package pt.ipp.estg.trabalho_cmu.ui.screens.User

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import pt.ipp.estg.trabalho_cmu.ui.components.AnimalSelectionDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainOptionsScreen(
    navController: NavController,
    hasAdoptedAnimal: Boolean
) {
    val viewModel: MainOptionsViewModel = viewModel()
    val lastWalk by viewModel.lastWalk.collectAsState()
    val medals by viewModel.medals.collectAsState()

    var showAnimalDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    if (showAnimalDialog) {
        AnimalSelectionDialog(
            onDismiss = { showAnimalDialog = false },
            onAnimalSelected = { animal ->
                showAnimalDialog = false
                navController.navigate("Walk/${animal.id}/${animal.name}")
            }
        )
    }

    if (showScheduleDialog) {
        AnimalSelectionDialog(
            onDismiss = { showScheduleDialog = false },
            onAnimalSelected = { animal ->
                showScheduleDialog = false
                navController.navigate("ActivityScheduling/${animal.id}")
            }
        )
    }

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
            MedalsSection()

            Spacer(modifier = Modifier.height(16.dp))

            LastWalkInfo()

            Spacer(modifier = Modifier.height(16.dp))

            WalkDetailsCard()

            Spacer(modifier = Modifier.height(16.dp))

            MainActionButtons(navController, onStartWalk = { showAnimalDialog = true }, onScheduleVisit = { showScheduleDialog = true })

            Spacer(modifier = Modifier.height(16.dp))
        }

        CommonOptions(navController)
    }
}

@Composable
fun MedalsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Coleção de Medalhas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(4) {
                    Text(
                        text = if (it < 2) "🥇" else "🥈",
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text("Ver conquistas", color = Color.Black)
            }
        }
    }
}

@Composable
fun LastWalkInfo() {
    Text(
        text = "Olá Miguel! O teu último passeio com a Molly foi de 3km",
        fontSize = 14.sp,
        color = Color(0xFF555555)
    )
}

@Composable
fun WalkDetailsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            WalkDetailRow("Duração:", "1 hora")
            WalkDetailRow("Distância:", "5km")
            WalkDetailRow("Data:", "19/10/2025")
        }
    }
}

@Composable
fun WalkDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}

@Composable
fun MainActionButtons(
    navController: NavController,
    onStartWalk: () -> Unit,
    onScheduleVisit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Agendar Visita",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f),
                onClick = onScheduleVisit
            )
            ActionButton(
                text = "Iniciar Passeio",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                onClick = onStartWalk
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Agendamentos",
                color = Color(0xFFE57373),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("ActivitiesHistory") }
            )
            ActionButton(
                text = "Concluir Passeio",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f),
                onClick = { /* TODO: Navegar */ }
            )
        }

        ActionButton(
            text = "Histórico de Passeios",
            color = Color(0xFF26A69A),
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("WalkHistory") }
        )
    }
}

@Composable
fun CommonOptions(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            text = "Visitar Comunidade SocialTails",
            color = Color(0xFF5C6BC0),
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("SocialTailsCommunity") }
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}