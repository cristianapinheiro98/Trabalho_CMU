// screens/User/MainOptionsViewModel.kt
package pt.ipp.estg.trabalho_cmu.ui.screens.User

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainOptionsViewModel : ViewModel() {

    // Last walk status
    private val _lastWalk = MutableStateFlow<WalkInfo?>(null)
    val lastWalk: StateFlow<WalkInfo?> = _lastWalk.asStateFlow()

    // Medal status
    private val _medals = MutableStateFlow<List<Medal>>(emptyList())
    val medals: StateFlow<List<Medal>> = _medals.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Mock data
        _lastWalk.value = WalkInfo(
            animalName = "Molly",
            distance = "3km",
            duration = "1 hora",
            totalDistance = "5km",
            date = "19/10/2025"
        )

        _medals.value = listOf(
            Medal("🥇", "Primeira caminhada"),
            Medal("🥇", "10km percorridos")
        )
    }

    fun startWalk(animalId: String) {
        // TODO: lógica para iniciar passeio
    }
}

// Temporary Data Classes
data class WalkInfo(
    val animalName: String,
    val distance: String,
    val duration: String,
    val totalDistance: String,
    val date: String
)

data class Medal(
    val icon: String,
    val title: String
)

