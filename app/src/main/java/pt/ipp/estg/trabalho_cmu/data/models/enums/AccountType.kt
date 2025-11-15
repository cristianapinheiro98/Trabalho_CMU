package pt.ipp.estg.trabalho_cmu.data.models.enums

/**
 * Defines the different types of accounts that can exist within the application.
 * This enum is used to differentiate between standard users and shelter administrators,
 * or to represent an unauthenticated state.
 */
enum class AccountType {
    /**
     * Represents a standard user account.
     * These users can browse and request to adopt animals.
     */
    USER,

    /**
     * Represents a shelter administrator account.
     * These users can manage animals and adoption requests for their specific shelter.
     */
    SHELTER,

    /**
     * Represents a state where the account type is not determined or the user is not logged in.
     * This is useful for initial states or guest sessions.
     */
    NONE
}