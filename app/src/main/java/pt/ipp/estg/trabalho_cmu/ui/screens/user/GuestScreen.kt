package pt.ipp.estg.trabalho_cmu.ui.screens.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R

@Composable
fun GuestScreen(
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cat_login),
                contentDescription = "Gato convidado",
                modifier = Modifier
                    .height(200.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                "Olá! 🐾",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Faz login para poderes guardar os teus animais favoritos e enviar pedidos de adoção.",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Iniciar sessão")
            }
        }
    }
}
// Adicione este bloco de código ao final do seu ficheiro GuestScreen.kt
@Preview(showBackground = true)
@Composable
fun GuestScreenPreview() {
    MaterialTheme {
        GuestScreen(
            onLoginClick = {} // Para o preview, podemos passar uma função vazia
        )
    }
}


