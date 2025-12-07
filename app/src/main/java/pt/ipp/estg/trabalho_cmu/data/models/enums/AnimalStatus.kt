package pt.ipp.estg.trabalho_cmu.data.models.enums

/**
 * Represents the current adoption availability of an animal.
 *
 * AVAILABLE → The animal can be adopted
 * HASOWNED → The animal already has an owner
 */
enum class AnimalStatus {
    AVAILABLE,
    HASOWNED
}
