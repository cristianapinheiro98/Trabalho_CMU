package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.ui.viewmodel.UserViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.Animals.AnimalViewModel
import pt.ipp.estg.trabalho_cmu.ui.screens.admin.ShelterViewModel


@Composable
fun OwnershipConfirmationScreen(
    userViewModel: UserViewModel,
    animalViewModel: AnimalViewModel,
    shelterViewModel: ShelterViewModel,
    animalId: Int,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {

    LaunchedEffect(animalId) {
        animalViewModel.loadAnimalById(animalId)
    }

    // Observa os dados
    val user by userViewModel.user.observeAsState()
    val animal by animalViewModel.selectedAnimal.observeAsState()
    val shelter by shelterViewModel.selectedShelter.observeAsState()

    // Carrega o shelter quando o animal for carregado
    LaunchedEffect(animal?.shelterId) {
        animal?.shelterId?.let { shelterId ->
            shelterViewModel.loadShelterById(shelterId)
        }
    }

    val userName = user?.name ?: "Utilizador"
    val animalName = animal?.name ?: ""
    val shelterName = shelter?.name ?: ""

    // Mostra loading enquanto carrega os dados
    if (animal == null || shelter == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF2C8B7E))
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFB8D4D0)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Título
                Text(
                    text = stringResource(R.string.confirmation_title, userName),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C),
                    textAlign = TextAlign.Center
                )

                // Mensagem
                Text(
                    text = stringResource(R.string.confirmation_message, shelterName, animalName),
                    fontSize = 16.sp,
                    color = Color(0xFF2C2C2C),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                // Aviso de notificações
                Text(
                    text = stringResource(R.string.confirmation_notification),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2C2C2C),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ícone de check (visto)
                CheckmarkIcon(
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botão Regressar ao Menu Inicial
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C8B7E),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.confirmation_back_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CheckmarkIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val circleColor = Color(0xFF2C8B7E)
        val strokeWidth = 12f

        // Desenhar círculo exterior
        drawCircle(
            color = circleColor,
            radius = size.minDimension / 2,
            style = Stroke(width = strokeWidth)
        )

        // Desenhar o visto (checkmark)
        val checkmarkPath = androidx.compose.ui.graphics.Path().apply {
            // Ponto inicial (parte inferior esquerda do visto)
            moveTo(size.width * 0.25f, size.height * 0.5f)
            // Ponto do meio (canto do visto)
            lineTo(size.width * 0.45f, size.height * 0.7f)
            // Ponto final (parte superior direita do visto)
            lineTo(size.width * 0.75f, size.height * 0.3f)
        }

        drawPath(
            path = checkmarkPath,
            color = circleColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OwnershipConfirmationScreenPreview() {
    Surface {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFB8D4D0)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Parabéns, Utilizador Demo!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "O abrigo Animais Fofos receberá a sua candidatura para adotar Mariana.",
                        fontSize = 16.sp,
                        color = Color(0xFF2C2C2C),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Text(
                        text = "Será notificado quando houver uma resposta.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C2C2C),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CheckmarkIcon(modifier = Modifier.size(120.dp))

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2C8B7E),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Regressar ao Menu Inicial",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}