package pt.ipp.estg.trabalho_cmu.data.models

/**
 * Represents an adoption request submitted by a user.
 *
 * @property id Unique identifier of the request.
 * @property nome Name of the applicant.
 * @property email Email of the applicant.
 * @property animal Name or ID of the animal being requested.
 * @property status Current status of the request (default: PENDING).
 */
data class AdoptionRequest(
    val id: String = "",
    val nome: String = "",
    val email: String = "",
    val animal: String = "",
    val status: String = "PENDING"
)
