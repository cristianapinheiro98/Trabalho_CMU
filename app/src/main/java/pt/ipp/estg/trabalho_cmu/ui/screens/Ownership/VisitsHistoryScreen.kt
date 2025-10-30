package pt.ipp.estg.trabalho_cmu.ui.screens.Ownership

import androidx.compose.foundation.Image
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R

@Composable
fun VisitsHistoryScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Dados hardcoded do animal e abrigo
    val animalData = listOf(
        AnimalData(
            name = "Molly",
            imageRes = R.drawable.cat_image,
            shelterName = "Abrigo dos Animais Ferozes",
            shelterContact = "253 111 111",
            shelterAddress = "Rua dos Animais nº 1001"
        ),
        AnimalData(
            name = "Mariana",
            imageRes = R.drawable.dog_image,
            shelterName = "Abrigo dos Gatinhos",
            shelterContact = "253 000 000",
            shelterAddress = "Rua do Gatil, Santa Maria da Feira"
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Cor de fundo suave
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título da tela
            Text(
                text = stringResource(id = R.string.visit_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lista de animais agendados
            animalData.forEach { animal ->
                // Cartão com informações do animal e abrigo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB8D4D0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Imagem do animal
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            // Aqui você pode usar uma imagem real do animal
                            Image(painter = painterResource(id = animal.imageRes), contentDescription = "Animal Image")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Nome do animal
                        Text(
                            text = animal.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C2C2C)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dados do abrigo
                        Text(
                            text = "${stringResource(R.string.shelter)}: ${animal.shelterName}",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )

                        Text(
                            text = "${stringResource(R.string.contact)}: ${animal.shelterContact}",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )

                        Text(
                            text = "${stringResource(R.string.address)}: ${animal.shelterAddress}",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        // Botão para ver localização no Google Maps
                        Button(
                            onClick = { /* Ação para abrir Google Maps */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2C8B7E),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.view_location),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cartão com datas e horas
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Data e Hora do Agendamento
                                Text(
                                    text = "${stringResource(R.string.pickup_date)}: 25/10/2025",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2C2C2C)
                                )

                                Text(
                                    text = "${stringResource(R.string.pickup_time)}: 10:00",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2C2C2C)
                                )

                                Text(
                                    text = "${stringResource(R.string.delivery_date)}: 25/10/2025",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2C2C2C)
                                )

                                Text(
                                    text = "${stringResource(R.string.delivery_time)}: 18:00",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2C2C2C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class AnimalData(
    val name: String,
    val imageRes: Int,
    val shelterName: String,
    val shelterContact: String,
    val shelterAddress: String
)


@Preview(showBackground = true)
@Composable
fun AppointmentScreenPreview() {
    VisitsHistoryScreen()  // Mostra a tela de agendamento com dados de exemplo
}
