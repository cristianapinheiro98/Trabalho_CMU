package pt.ipp.estg.trabalho_cmu.data.models.enums

/**
 * Represents the status of an ownership (adoption) request.
 *
 * PENDING - Waiting for shelter approval
 * APPROVED - The request has been accepted
 * REJECTED - The request was denied
 */
enum class OwnershipStatus {
    PENDING,
    APPROVED,
    REJECTED
}
