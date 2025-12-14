package pt.ipp.estg.trabalho_cmu.ui.screens.activity.scheduling

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for Activity Scheduling Screen.
 *
 * Manages the state of the activity scheduling flow, including:
 * - Loading animal and shelter data
 * - Syncing approved ownerships
 * - Validating scheduling constraints
 * - Creating new activities
 */
class ActivitySchedulingViewModel(application: Application) : AndroidViewModel(application) {
    private val activityRepository = DatabaseModule.provideActivityRepository(application)
    private val animalRepository = DatabaseModule.provideAnimalRepository(application)
    private val shelterRepository = DatabaseModule.provideShelterRepository(application)
    private val ownershipRepository = DatabaseModule.provideOwnershipRepository(application)
    private val userRepository = DatabaseModule.provideUserRepository(application)

    private val _uiState = MutableLiveData<ActivitySchedulingUiState>(ActivitySchedulingUiState.Initial)
    val uiState: LiveData<ActivitySchedulingUiState> = _uiState

    /**
     * Loads all necessary data for scheduling an activity.
     *
     * Check internet connectivity
     * Sync approved ownerships
     * Sync owned animals
     * Load animal and shelter
     * Sync shelter data
     * Sync activities for this specific animal
     * Load booked dates for the animal
     *
     * @param animalId The ID of the animal to schedule an activity for.
     * @param userId The ID of the current user.
     */
    fun loadSchedulingData(animalId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = ActivitySchedulingUiState.Loading

            if (!NetworkUtils.isConnected()) {
                _uiState.value = ActivitySchedulingUiState.Offline
                return@launch
            }

            try {
                // Sync user
                val userSyncResult = userRepository.syncSpecificUser(userId)
                userSyncResult.onFailure { error ->
                    _uiState.value = ActivitySchedulingUiState.Error("Failed to sync user: ${error.message}")
                    return@launch
                }

                // Sync ownerships and owned animals
                ownershipRepository.syncUserApprovedOwnerships(userId)
                animalRepository.syncUserOwnedAnimals(userId)

                // Sync specific animal
                val animalSyncResult = animalRepository.syncSpecificAnimal(animalId)
                animalSyncResult.onFailure { error ->
                    _uiState.value = ActivitySchedulingUiState.Error("Animal not found")
                    return@launch
                }

                val animal = animalRepository.getAnimalById(animalId)
                if (animal == null) {
                    _uiState.value = ActivitySchedulingUiState.Error("Animal not found in local DB")
                    return@launch
                }

                // Sync shelter
                shelterRepository.syncSheltersByAnimalIds(listOf(animalId))
                val shelter = shelterRepository.getShelterById(animal.shelterId)
                if (shelter == null) {
                    _uiState.value = ActivitySchedulingUiState.Error("Shelter not found")
                    return@launch
                }

                val bookedDates = activityRepository.getBookedDatesFromFirebase(animalId)

                _uiState.value = ActivitySchedulingUiState.Success(
                    animal = animal,
                    shelter = shelter,
                    bookedDates = bookedDates,
                    selectedDates = emptySet(),
                    startDate = null,
                    endDate = null,
                    pickupTime = shelter.openingTime ?: "09:00",
                    deliveryTime = shelter.closingTime ?: "18:00"
                )

            } catch (e: Exception) {
                _uiState.value = ActivitySchedulingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Handles date clicks in the calendar.
     * Implements the date range selection logic with validation against booked dates.
     *
     * @param date The clicked date in "dd/MM/yyyy" format.
     */
    fun onDateClicked(date: String) {
        val currentState = _uiState.value
        if (currentState !is ActivitySchedulingUiState.Success) return

        if (date in currentState.bookedDates) return

        when {
            currentState.startDate == null -> {
                _uiState.value = currentState.copy(
                    startDate = date,
                    endDate = null,
                    selectedDates = emptySet(),
                    validationError = null
                )
            }
            currentState.endDate == null -> {
                val range = fillDateRangeIfValid(
                    currentState.startDate!!,
                    date,
                    currentState.bookedDates
                )

                if (range != null) {
                    _uiState.value = currentState.copy(
                        endDate = date,
                        selectedDates = range,
                        validationError = null
                    )
                } else {
                    _uiState.value = currentState.copy(
                        startDate = date,
                        endDate = null,
                        selectedDates = emptySet(),
                        validationError = ValidationError.DateConflict
                    )
                }
            }
            else -> {
                _uiState.value = currentState.copy(
                    startDate = date,
                    endDate = null,
                    selectedDates = emptySet(),
                    validationError = null
                )
            }
        }
    }

    /**
     * Fills date range between start and end, validating that no booked dates exist in between.
     *
     * @param start Start date in "dd/MM/yyyy" format.
     * @param end End date in "dd/MM/yyyy" format.
     * @param bookedDates List of dates already booked.
     * @return Set of dates in the range if valid, null if there are booked dates in the range.
     */
    private fun fillDateRangeIfValid(
        start: String,
        end: String,
        bookedDates: List<String>
    ): Set<String>? {
        val dates = mutableSetOf<String>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val startDate = sdf.parse(start) ?: return null
        val endDate = sdf.parse(end) ?: return null

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val endCalendar = Calendar.getInstance()
        endCalendar.time = endDate

        if (endCalendar.before(calendar)) {
            return fillDateRangeIfValid(end, start, bookedDates)
        }

        while (!calendar.after(endCalendar)) {
            val dateStr = sdf.format(calendar.time)

            if (dateStr in bookedDates) {
                return null
            }

            dates.add(dateStr)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }

    /**
     * Updates the selected dates in the UI state.
     * (Kept for backwards compatibility, but now date selection is handled by onDateClicked)
     *
     * @param dates Set of selected dates in "dd/MM/yyyy" format.
     */
    fun onDatesSelected(dates: Set<String>) {
        val currentState = _uiState.value
        if (currentState is ActivitySchedulingUiState.Success) {
            _uiState.value = currentState.copy(selectedDates = dates, validationError = null)
        }
    }

    /**
     * Updates the pickup time and validates it against shelter hours.
     *
     * @param time Time in "HH:mm" format.
     */
    fun onPickupTimeChanged(time: String) {
        val currentState = _uiState.value
        if (currentState is ActivitySchedulingUiState.Success) {
            val error = validateTime(time, currentState.shelter.openingTime, currentState.shelter.closingTime)
            _uiState.value = currentState.copy(pickupTime = time, validationError = error)
        }
    }

    /**
     * Updates the delivery time and validates it against shelter hours.
     *
     * @param time Time in "HH:mm" format.
     */
    fun onDeliveryTimeChanged(time: String) {
        val currentState = _uiState.value
        if (currentState is ActivitySchedulingUiState.Success) {
            val error = validateTime(time, currentState.shelter.openingTime, currentState.shelter.closingTime)
            _uiState.value = currentState.copy(deliveryTime = time, validationError = error)
        }
    }

    /**
     * Schedules a new activity after validating all constraints.
     *
     * @param userId The ID of the current user.
     */
    fun scheduleActivity(userId: String) {
        val state = _uiState.value
        if (state !is ActivitySchedulingUiState.Success) return

        viewModelScope.launch {
            _uiState.value = ActivitySchedulingUiState.Loading

            val validationError = validateScheduling(state, userId)
            if (validationError != null) {
                _uiState.value = state.copy(validationError = validationError)
                return@launch
            }

            val sortedDates = state.selectedDates.sorted()
            val activity = Activity(
                id = "",
                userId = userId,
                animalId = state.animal.id,
                pickupDate = sortedDates.first(),
                pickupTime = state.pickupTime,
                deliveryDate = sortedDates.last(),
                deliveryTime = state.deliveryTime
            )

            val result = activityRepository.createActivity(activity)

            result.fold(
                onSuccess = {
                    _uiState.value = ActivitySchedulingUiState.SchedulingSuccess(
                        animalName = state.animal.name,
                        startDate = sortedDates.first(),
                        endDate = sortedDates.last()
                    )
                },
                onFailure = { e ->
                    _uiState.value = ActivitySchedulingUiState.Error(e.message ?: "Scheduling failed")
                }
            )
        }
    }

    /**
     * Clears any validation errors from the UI state.
     */
    fun clearError() {
        val currentState = _uiState.value
        if (currentState is ActivitySchedulingUiState.Success) {
            _uiState.value = currentState.copy(validationError = null)
        }
    }

    /**
     * Validates all scheduling constraints.
     *
     * @return ValidationError if validation fails, null otherwise.
     */
    private suspend fun validateScheduling(
        state: ActivitySchedulingUiState.Success,
        userId: String
    ): ValidationError? {
        if (state.selectedDates.isEmpty()) {
            return ValidationError.NoDateSelected
        }

        val currentDate = getCurrentDateString()
        val activeActivities = activityRepository.getActiveActivitiesByAnimal(
            state.animal.id,
            currentDate
        )
        if (activeActivities.isNotEmpty()) {
            return ValidationError.ActiveActivityExists
        }

        if (hasDateConflict(state.bookedDates, state.selectedDates.toList())) {
            return ValidationError.DateConflict
        }

        val firstDate = state.selectedDates.sorted().first()
        if (!isAtLeast24HoursAhead(firstDate, state.pickupTime)) {
            return ValidationError.LessThan24Hours
        }

        return null
    }

    /**
     * Validates a time against shelter opening/closing hours.
     *
     * @return ValidationError if time is outside hours, null otherwise.
     */
    private fun validateTime(time: String, opening: String?, closing: String?): ValidationError? {
        if (opening == null || closing == null) return null
        val timeInt = time.replace(":", "").toIntOrNull() ?: return null
        val openingInt = opening.replace(":", "").toIntOrNull() ?: return null
        val closingInt = closing.replace(":", "").toIntOrNull() ?: return null

        return if (timeInt < openingInt || timeInt > closingInt) {
            ValidationError.TimeOutsideOpeningHours
        } else null
    }

    /**
     * Extracts all booked dates from a list of activities.
     */
    private fun extractBookedDates(activities: List<Activity>): List<String> {
        val dates = mutableListOf<String>()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        activities.forEach { activity ->
            val start = activity.pickupDate.toDate()
            val end = activity.deliveryDate.toDate()

            calendar.time = start
            while (!calendar.time.after(end)) {
                dates.add(sdf.format(calendar.time))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        Log.d("BookedDates", "Extracted booked dates: $dates")
        return dates
    }

    /**
     * Checks if any selected dates conflict with booked dates.
     */
    private fun hasDateConflict(bookedDates: List<String>, selectedDates: List<String>): Boolean {
        return selectedDates.any { it in bookedDates }
    }

    /**
     * Checks if the date/time is at least 24 hours in the future.
     */
    private fun isAtLeast24HoursAhead(dateStr: String, timeStr: String): Boolean {
//        return try {
//            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
//            val scheduledDateTime = sdf.parse("$dateStr $timeStr") ?: return false
//            val now = Date()
//            val twentyFourHoursLater = Date(now.time + 24 * 60 * 60 * 1000)
//            scheduledDateTime.after(twentyFourHoursLater)
//        } catch (e: Exception) {
//            false
//        }

        // TEST MODE (Walks), to allow to initiate a walk immediately after scheduling
        return true
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun String.toDate(): Date {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.parse(this) ?: Date()
    }
}