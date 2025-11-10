package pt.ipp.estg.trabalho_cmu.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R

@Composable
fun AnimalDetailScreen(
    animalName: String = "Molly",
    onAdoptClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 🐾 Nome do animal
        Text(
            text = animalName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 🐱 Primeira imagem + frase
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(id = R.drawable.gato5),
                contentDescription = "$animalName imagem 1",
                modifier = Modifier
                    .size(120.dp)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Column {
                Text("Meow!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Só te respondo depois de abrires uma lata de atum!", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🐾 Segunda imagem + frase
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "Olha que vais ter que ter tempo para brincar comigo!",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                fontSize = 14.sp
            )
            Image(
                painter = painterResource(id = R.drawable.gato4),
                contentDescription = "$animalName imagem 2",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider(thickness = 1.dp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(20.dp))

        // 🧾 Secção informativa (segunda parte do mockup)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.gato5),
                contentDescription = "$animalName retrato",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "Olha bem para o que está aqui escrito! Eu ajudo-te a ler!",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Detalhes do animal
        Column(modifier = Modifier.fillMaxWidth()) {
            DetailLine("Nome:", "Molly")
            DetailLine("Idade:", "13 anos")
            DetailLine("Peso:", "Não se pergunta a uma senhora meow!")
            DetailLine("Cor do pelo:", "Tricolor (branco, preto e laranja)")
            DetailLine(
                "Personalidade:",
                "Calma e observadora, prefere um bom spot ao sol a grandes aventuras."
            )
            DetailLine("Gosta de:", "Sestas longas, mantas fofas e atenção.")
            DetailLine("Não gosta de:", "Aspiradores barulhentos.")
            DetailLine("Curiosidade:", "Adora brincar com fitas e perseguir sombras.")
            DetailLine("Estado de saúde:", "Em excelente forma para uma senhora da sua idade!")
            DetailLine("Sociabilidade:", "Dá-se bem com humanos, mas não muito com outros gatos.")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🩵 Botão principal
        Button(
            onClick = onAdoptClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
        ) {
            Text("Queres fazer uma special adoption?", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(value, fontSize = 14.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AnimalDetailScreenPreview() {
    MaterialTheme {
        AnimalDetailScreen()
    }
}
