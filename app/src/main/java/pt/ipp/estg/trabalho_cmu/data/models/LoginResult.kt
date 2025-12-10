package pt.ipp.estg.trabalho_cmu.data.models

import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType

/**
 * Represents the result of a login operation.
 *
 * A login may return:
 * - a User (regular account),
 * - a Shelter (shelter account),
 * - the detected AccountType.
 *
 * Only one of the two entities (User or Shelter) will be non-null,
 * depending on the authenticated account type.
 *
 * @property user Authenticated user profile (if AccountType.USER).
 * @property shelter Authenticated shelter profile (if AccountType.SHELTER).
 * @property accountType Type of account logged in.
 */
data class LoginResult(
    val user: User? = null,
    val shelter: Shelter? = null,
    val accountType: AccountType
)
