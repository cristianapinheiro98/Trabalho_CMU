package pt.ipp.estg.trabalho_cmu

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
//import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await

//@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    //FirebaseTestScreen()
                    PetAdoptionApp()
                }
            }
        }
    }
}

@Composable
fun FirebaseTestScreen() {
    var status by remember { mutableStateOf("🔄 Testando Firebase...") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val db = Firebase.firestore

            val testData = hashMapOf(
                "app" to "TailWagger",
                "timestamp" to System.currentTimeMillis(),
                "test" to "Ligação com Firebase OK!"
            )

            val docRef = db.collection("test_connection")
                .add(testData)
                .await()

            status = "Firebase ligado!\n\n" +
                    "Documento ID: ${docRef.id}\n\n" +
                    "Verifica na Console do Firebase!"

        } catch (e: Exception) {
            status = "❌ Erro:\n${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Teste Firebase",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(text = status)
            }
        }
    }
}