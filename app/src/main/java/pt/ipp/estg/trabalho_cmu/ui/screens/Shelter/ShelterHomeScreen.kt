package pt.ipp.estg.trabalho_cmu.ui.screens.Shelter

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.UserType
import pt.ipp.estg.trabalho_cmu.ui.screens.Auth.AuthViewModel

@Composable
fun ShelterHomeScreen(
    authViewModel: AuthViewModel,
    onRegisterClick: () -> Unit = {},
    onRequestsClick: () -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bem-Vindo",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onRegisterClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registar Animal", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRequestsClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver pedidos de adoção", color = Color.White)
        }
    }
}

class FakeAuthViewModel : AuthViewModel(Application()) {

    private val _fakeUser = MutableLiveData(
        User(
            id = 1,
            name = "Abrigo Porto",
            adress = "Rua dos Animais 123",
            email = "abrigo@porto.com",
            phone = "912345678",
            password = "xxxx",
            userType = UserType.ABRIGO,
            shelterId = 1
        )
    )

    override val currentUser: LiveData<User?> = _fakeUser

    override fun login(): Job = Job()
    override fun register(): Job = Job()

}
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewShelterHomeScreen() {
    MaterialTheme {
        ShelterHomeScreen(
            authViewModel = FakeAuthViewModel(),
            onRegisterClick = {},
            onRequestsClick = {}
        )
    }
}


