package pt.ipp.estg.trabalho_cmu.ui.screens.admin

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ipp.estg.trabalho_cmu.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalCreation(
    onSave: (AnimalForm) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // ---- Estado do formulário ----
    var nome by remember { mutableStateOf(TextFieldValue("")) }
    var dataNasc by remember { mutableStateOf<Date?>(null) }
    var cor by remember { mutableStateOf(TextFieldValue("")) }
    var raca by remember { mutableStateOf(TextFieldValue("")) }
    var fotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var erros by remember { mutableStateOf(listOf<String>()) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // ---- DatePicker (Material 3) ----
    //controla a janela do calendario
    var showDatePicker by remember { mutableStateOf(false) }
    //formatador de datas
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // ---- Image Picker (galeria) ----
    val context = LocalContext.current
    val getImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
        // uri-é como se fosse um endereço unico de um ficheiro,ou seja,a aplicação usa-o para pedir para ler o conteudo
    ) { uri ->
        if (uri != null) {
            // A verificação da versão do SDK é feita AQUI DENTRO,verifica se eé possivel converter ui em imagem(só api28)
            fotoBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source).asImageBitmap()
            } else {
                //faz a mesma coisa só para versões antigas
                // Suporte para versões antigas (deprecated, mas funciona)
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                bitmap.asImageBitmap()
            }
        }
    }

    // ---- UI ----
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()), // Adicionado scroll para ecrãs pequenos
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Registar Animal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(24.dp))

        // Formulário...
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = dataNasc?.let { dateFmt.format(it) } ?: "",
            onValueChange = {},
            label = { Text("Data de Nascimento") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        painterResource(R.drawable.ic_calendar),
                        contentDescription = "Escolher data"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = cor,
            onValueChange = { cor = it },
            label = { Text("Cor") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = raca,
            onValueChange = { raca = it },
            label = { Text("Raça") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Secção da Fotografia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElevatedButton(
                onClick = { getImage.launch("image/*") },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_photo),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Escolher foto")
            }
            if (fotoBitmap != null) {
                Image(
                    bitmap = fotoBitmap!!,
                    contentDescription = "Pré-visualização",
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Box(Modifier.size(64.dp)) // Placeholder para manter o layout
            }
        }

        // Mensagens de erro
        if (erros.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            erros.forEach { msg -> Text("• $msg", color = MaterialTheme.colorScheme.error) }
        }

        Spacer(Modifier.weight(1f)) // Empurra o botão para baixo

        // Botão Guardar
        Button(
            onClick = {
                val problemas = buildList {
                    if (nome.text.isBlank()) add("O nome é obrigatório.")
                    if (raca.text.isBlank()) add("A raça é obrigatória.")
                }
                if (problemas.isNotEmpty()) {
                    erros = problemas
                } else {
                    erros = emptyList()
                    val animalForm = AnimalForm(
                            nome = nome.text.trim(),
                            dataNascimento = dataNasc,
                            cor = cor.text.trim().ifBlank { null },
                            raca = raca.text.trim(),
                            fotoBitmap = fotoBitmap
                    )
                    onSave(animalForm)
                    showSuccessDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Text("Guardar", color = Color.White, fontSize = 16.sp)
        }
        if(showSuccessDialog){
            AlertDialog(
                onDismissRequest = { onNavigateBack()},
                title = { Text("Sucesso") },
                text = { Text("O animal foi guardado com sucesso!") },
                confirmButton = {
                    TextButton(onClick = { onNavigateBack() }) {
                        Text("OK")
                    }
                }
            )
        }
        Spacer(Modifier.height(16.dp))
    }

    // Lógica do DatePicker (sem alterações)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let {
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = it
                            dataNasc = calendar.time
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


data class AnimalForm(
    val nome: String,
    val dataNascimento: Date?,
    val cor: String?,
    val raca: String,
    val fotoBitmap: ImageBitmap?
)

@Preview(showBackground = true)
@Composable
private fun PreviewRegistoAnimal() {
    // Para a Preview funcionar bem, é bom envolvê-la num tema
    // MaterialTheme {
    AnimalCreation()
    // }
}

