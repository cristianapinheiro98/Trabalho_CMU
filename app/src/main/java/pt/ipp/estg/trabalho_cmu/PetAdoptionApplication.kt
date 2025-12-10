package pt.ipp.estg.trabalho_cmu

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils

/**
 * Custom Application class for the Pet Adoption app.
 *
 * Responsibilities:
 * - Initializes global utilities used across the entire app lifecycle,
 *   such as network state monitoring through [NetworkUtils].
 * - Provides an application-wide coroutine scope using `SupervisorJob`
 *   to safely launch long-running or startup-related asynchronous tasks.
 *
 * This class is instantiated before any Activity, making it ideal for
 * registering listeners, initializing libraries, or preparing shared resources.
 */

class PetAdoptionApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NetworkUtils.init(this)
    }
}