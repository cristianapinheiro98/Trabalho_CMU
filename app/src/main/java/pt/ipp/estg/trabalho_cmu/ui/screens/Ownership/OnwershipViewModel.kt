package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository

/**
 * ViewModel responsible for:
 * - Loading the user's ownership records
 * - Fetching animal details
 * - Submitting new ownership requests
 */
class OwnershipViewModel(application: Application) : AndroidViewModel(application) {

    private val ownershipRepository: OwnershipRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        OwnershipRepository(
            db.ownershipDao(),
            FirebaseFirestore.getInstance()
        )
    }

    private val animalRepository: AnimalRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        AnimalRepository(db.animalDao(), FirebaseFirestore.getInstance())
    }


    private val _userId = MutableLiveData<Int>()

    val ownerships: LiveData<List<Ownership>> =
        _userId.switchMap { id ->
            ownershipRepository.getOwnershipsByUser(id)
        }

    /**
     * Loads ownerships from Room and updates them by fetching latest data from Firebase.
     */
    fun loadOwnershipsForUser(userId: Int) {
        _userId.value = userId
        viewModelScope.launch {
            ownershipRepository.fetchOwnerships()
        }
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _submissionSuccess = MutableLiveData<Boolean>()
    val submissionSuccess: LiveData<Boolean> = _submissionSuccess

    fun resetSubmissionSuccess() {
        _submissionSuccess.value = false
    }

    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    /**
     * Loads animal details from Room.
     */
    fun loadAnimalDetails(animalId: Int) {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _animal.value = animalRepository.getAnimalById(animalId)
                _error.value = null
            } catch (e: Exception) {
                _error.value =
                    ctx.getString(R.string.error_loading_animal) + " ${e.message}"
                _animal.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Submits an ownership request to Firebase and stores it locally.
     */
    fun submitOwnership(request: Ownership) {
        val ctx = getApplication<Application>()

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val animal = animalRepository.getAnimalById(request.animalId)

                if (animal == null) {
                    _error.value = ctx.getString(R.string.error_animal_not_found_room)
                    _isLoading.value = false
                    return@launch
                }

                val result = ownershipRepository.createOwnership(request)

                result.onSuccess {
                    _message.value = ctx.getString(R.string.success_ownership_submitted)
                    _submissionSuccess.value = true
                }.onFailure { exception ->
                    _error.value =
                        ctx.getString(R.string.error_submit_ownership) + " ${exception.message}"
                }

            } catch (e: Exception) {
                _error.value =
                    ctx.getString(R.string.error_submit_ownership) + " ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
