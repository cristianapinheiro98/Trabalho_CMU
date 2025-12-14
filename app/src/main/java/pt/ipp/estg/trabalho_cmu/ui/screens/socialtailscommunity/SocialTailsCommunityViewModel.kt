package pt.ipp.estg.trabalho_cmu.ui.screens.socialtailscommunity

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipp.estg.trabalho_cmu.data.local.entities.Walk
import pt.ipp.estg.trabalho_cmu.data.repository.WalkRepository
import pt.ipp.estg.trabalho_cmu.providers.DatabaseModule
import pt.ipp.estg.trabalho_cmu.utils.NetworkUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel for SocialTails Community Screen.
 *
 * Manages the community feed state including podium rankings (all-time and monthly)
 * and paginated list of public walks from all users.
 *
 * @param application Application context for repository access and network checks
 */
class SocialTailsCommunityViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SocialTailsVM"
        private const val PAGE_SIZE = 5
    }

    private val context = application.applicationContext
    private val walkRepository: WalkRepository = DatabaseModule.provideWalkRepository(application)

    // UI State
    private val _uiState = MutableLiveData<SocialTailsCommunityUiState>(SocialTailsCommunityUiState.Initial)
    val uiState: LiveData<SocialTailsCommunityUiState> = _uiState

    // Pagination tracking
    private var currentPage = 0
    private var allPublicWalks = mutableListOf<Walk>()

    /**
     * Load community data including podiums and public walks feed.
     *
     * Checks for network connectivity first and sets offline state if unavailable.
     * Fetches top walks for all-time and monthly podiums, plus first page of public walks.
     */
    fun loadCommunityData() {
        // Check internet connection
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.value = SocialTailsCommunityUiState.Offline
            return
        }

        _uiState.value = SocialTailsCommunityUiState.Loading

        viewModelScope.launch {
            try {
                // Fetch all data in parallel
                val topAllTimeResult = walkRepository.getTopWalksAllTime(3)
                val topMonthlyResult = walkRepository.getTopWalksMonthly(3)
                val publicWalksResult = walkRepository.getPublicWalksPaginated(PAGE_SIZE, 0)

                // Check for errors
                if (topAllTimeResult.isFailure || topMonthlyResult.isFailure || publicWalksResult.isFailure) {
                    val error = topAllTimeResult.exceptionOrNull()
                        ?: topMonthlyResult.exceptionOrNull()
                        ?: publicWalksResult.exceptionOrNull()
                    _uiState.value = SocialTailsCommunityUiState.Error(
                        error?.message ?: "Failed to load community data"
                    )
                    return@launch
                }

                val topAllTime = topAllTimeResult.getOrDefault(emptyList())
                val topMonthly = topMonthlyResult.getOrDefault(emptyList())
                val publicWalks = publicWalksResult.getOrDefault(emptyList())

                // Reset pagination
                currentPage = 0
                allPublicWalks.clear()
                allPublicWalks.addAll(publicWalks)

                // Get current month name
                val monthName = getCurrentMonthName()

                _uiState.value = SocialTailsCommunityUiState.Success(
                    topWalksAllTime = topAllTime,
                    topWalksMonthly = topMonthly,
                    publicWalks = allPublicWalks.toList(),
                    currentMonthName = monthName,
                    hasMoreWalks = publicWalks.size >= PAGE_SIZE,
                    isLoadingMore = false
                )

                Log.d(TAG, "Loaded community data: ${topAllTime.size} all-time, " +
                        "${topMonthly.size} monthly, ${publicWalks.size} public walks")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading community data", e)
                _uiState.value = SocialTailsCommunityUiState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    /**
     * Load more public walks for infinite scroll pagination.
     *
     * Appends next page of walks to the existing list.
     * Updates isLoadingMore flag during loading.
     */
    fun loadMoreWalks() {
        val currentState = _uiState.value
        if (currentState !is SocialTailsCommunityUiState.Success) return
        if (currentState.isLoadingMore || !currentState.hasMoreWalks) return

        // Set loading more state
        _uiState.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            try {
                currentPage++
                val offset = currentPage * PAGE_SIZE

                val result = walkRepository.getPublicWalksPaginated(PAGE_SIZE, offset)

                result.onSuccess { newWalks ->
                    allPublicWalks.addAll(newWalks)

                    _uiState.value = currentState.copy(
                        publicWalks = allPublicWalks.toList(),
                        hasMoreWalks = newWalks.size >= PAGE_SIZE,
                        isLoadingMore = false
                    )

                    Log.d(TAG, "Loaded ${newWalks.size} more walks, total: ${allPublicWalks.size}")
                }

                result.onFailure { error ->
                    Log.e(TAG, "Error loading more walks", error)
                    // Revert page increment on error
                    currentPage--
                    _uiState.value = currentState.copy(isLoadingMore = false)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in loadMoreWalks", e)
                currentPage--
                _uiState.value = currentState.copy(isLoadingMore = false)
            }
        }
    }

    /**
     * Refresh all community data.
     *
     * Resets pagination and reloads everything from scratch.
     */
    fun refresh() {
        loadCommunityData()
    }

    /**
     * Get the current month name formatted for display.
     *
     * @return Month name with year (e.g., "Dezembro de 2025")
     */
    private fun getCurrentMonthName(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM 'de' yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time).replaceFirstChar { it.uppercase() }
    }

    /**
     * Format duration for display.
     *
     * @param seconds Duration in seconds
     * @return Formatted string (e.g., "1h 23min" or "45min")
     */
    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}min"
            else -> "${minutes}min"
        }
    }

    /**
     * Format distance for display.
     *
     * @param meters Distance in meters
     * @return Formatted string (e.g., "1.5km" or "500m")
     */
    fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            String.format("%.1fkm", meters / 1000)
        } else {
            String.format("%.0fm", meters)
        }
    }

    /**
     * Format relative date for display in feed.
     *
     * @param dateString Date in "dd/MM/yyyy" format
     * @return Relative string (e.g., "Hoje", "Ontem", "Há 3 dias", or the date)
     */
    fun formatRelativeDate(dateString: String): String {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.parse(dateString) ?: return dateString

            val now = Calendar.getInstance()
            val walkDate = Calendar.getInstance().apply { time = date }

            val diffInDays = ((now.timeInMillis - walkDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            when (diffInDays) {
                0 -> "Hoje"
                1 -> "Ontem"
                in 2..7 -> "Há $diffInDays dias"
                else -> dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
}