package pt.ipp.estg.trabalho_cmu.data.models

/**
 * Represents the current status of an ownership request.
 * This allows admins to approve or reject ownerships later.
 */
enum class OwnershipStatus {
    PENDING,// Waiting to be analysed
    APPROVED,  // Accepted by shelter/admin
    REJECTED   // Denied by shelter/admin
}
