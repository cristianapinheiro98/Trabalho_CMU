package pt.ipp.estg.trabalho_cmu.data.models.enums

/**
 * Defines the type of account authenticated in the system.
 *
 * USER → Regular adopter account
 * SHELTER → Shelter representative account
 * NONE → No account detected / user not logged in
 */
enum class AccountType {
    USER, SHELTER, NONE
}
