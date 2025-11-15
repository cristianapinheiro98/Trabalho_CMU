package pt.ipp.estg.trabalho_cmu.data.models

import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType

/**
 * Represents the result of a user login attempt.
 *
 * This data class encapsulates all the necessary information returned after a successful
 * authentication. It identifies the type of account that logged in ([AccountType])
 * and provides the corresponding entity ([User] or [Shelter]).
 *
 * @property user The [User] object if the logged-in account is of type [AccountType.USER].
 *           This will be `null` if the account is a shelter or if the login fails.
 * @property shelter The [Shelter] object if the logged-in account is of type [AccountType.SHELTER].
 *           This will be `null` if the account is a standard user or if the login fails.
 * @property accountType The type of the logged-in account, indicating whether it's a [AccountType.USER],
 *           [AccountType.SHELTER], or [AccountType.NONE] in case of failure or an unknown state.
 */
data class LoginResult(
    val user: User? = null,
    val shelter: Shelter? = null,
    val accountType: AccountType
)