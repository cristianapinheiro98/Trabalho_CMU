package pt.ipp.estg.trabalho_cmu.data.models.enums

/**
 * Defines the lifecycle stages of an ownership (adoption) request.
 *
 * This enum is crucial for tracking the progress of an adoption from submission
 * to final decision, allowing administrators to manage and update requests.
 */
enum class OwnershipStatus {

    /**
     * The initial state of a newly submitted ownership request.
     * It signifies that the request is waiting for review by a shelter administrator.
     */
    PENDING,

    /**
     * Indicates that the ownership request has been reviewed and accepted
     * by the shelter administrator. The adoption process can proceed.
     */
    APPROVED,

    /**
     * Indicates that the ownership request has been reviewed and denied
     * by the shelter administrator.
     */
    REJECTED   // Denied by shelter/admin
}