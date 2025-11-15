package pt.ipp.estg.trabalho_cmu.data.models.enums

/**
 * Defines the possible statuses for an animal within the application.
 * This enum is used to track whether an animal is available for adoption
 * or has already been adopted.
 */
enum class AnimalStatus {

    /**
     * Indicates that the animal is currently in a shelter and is available
     * for adoption or other activities.
     */
    AVAILABLE,

    /**
     * Indicates that the animal has been successfully adopted and now has an owner.
     * The name "HASOWNED" suggests it has an owner.
     */
    HASOWNED
}