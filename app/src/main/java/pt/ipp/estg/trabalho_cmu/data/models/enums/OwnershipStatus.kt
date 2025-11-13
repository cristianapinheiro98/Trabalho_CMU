package pt.ipp.estg.trabalho_cmu.data.models.enums

/**
 * Represents the current status of an ownership request.
 * This allows admins to approve or reject ownerships later.
 */
enum class OwnershipStatus {
    PENDING,// Waiting to be analysed
    ANALYSING, //  Waiting for admin approval
    APPROVED,  // Accepted by shelter/admin
    REJECTED   // Denied by shelter/admin
}