package pt.ipp.estg.trabalho_cmu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.R
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.models.enums.OwnershipStatus
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

    // ========= USER OWNERSHIPS ========= //

    private val _userFirebaseUid = MutableLiveData<String>()

    val ownerships: LiveData<List<Ownership>> =
        _userFirebaseUid.switchMap { firebaseUid ->
            ownershipRepository.getOwnershipsByUser(firebaseUid)
        }

    fun loadOwnershipsForUser(userFirebaseUid: String) {
        _userFirebaseUid.value = userFirebaseUid
        viewModelScope.launch {
            ownershipRepository.fetchOwnerships()
        }
    }

    // ========= UI STATES ========= //

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

    // ========= ANIMAL DETAILS ========= //

    private val _animal = MutableLiveData<Animal?>()
    val animal: LiveData<Animal?> = _animal

    fun loadAnimalDetails(animalId: Int) {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _animal.value = animalRepository.getAnimalById(animalId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = ctx.getString(R.string.error_loading_animal) + " ${e.message}"
                _animal.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAnimalByFirebaseUid(firebaseUid: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _animal.value = animalRepository.getAnimalByFirebaseUid(firebaseUid)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error loading animal: ${e.message}"
                _animal.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========= OWNERSHIP ACTIONS ========= //

    fun submitOwnership(request: Ownership) {
        val ctx = getApplication<Application>()

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = ownershipRepository.createOwnership(request)

                result.onSuccess {
                    _message.value = ctx.getString(R.string.success_ownership_submitted)
                    _error.value = null
                    _submissionSuccess.value = true
                }.onFailure { e ->
                    _error.value = ctx.getString(R.string.error_submit_ownership) + " ${e.message}"
                }

            } catch (e: Exception) {
                _error.value = ctx.getString(R.string.error_submit_ownership) + " ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOwnershipStatus(id: Int, status: OwnershipStatus) {
        viewModelScope.launch {
            try {
                ownershipRepository.updateOwnershipStatus(id, status)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error updating status: ${e.message}"
            }
        }
    }

    fun deleteOwnership(ownership: Ownership) {
        viewModelScope.launch {
            try {
                ownershipRepository.deleteOwnership(ownership)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error deleting request: ${e.message}"
            }
        }
    }

    fun clearError() { _error.value = null }

    fun clearMessage() { _message.value = null }
}