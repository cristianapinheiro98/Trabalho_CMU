package pt.ipp.estg.trabalho_cmu.data.models

import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.models.enums.AccountType

data class LoginResult(
    val user: User? = null,
    val shelter: Shelter? = null,
    val accountType: AccountType
)