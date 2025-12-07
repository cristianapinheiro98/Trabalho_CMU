package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


/**
 * Entity representing an application user stored locally in Room.
 *
 * Users may be either regular adopters or shelters (identified separately
 * in the authentication/remote backend layer). This table stores only
 * common user profile data used offline.
 *
 * @property id Unique identifier of the user (same as Firebase UID).
 * @property name Full name of the user.
 * @property address Registered physical address.
 * @property email Email used for authentication and communication.
 * @property phone Contact phone number.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val email: String,
    val phone: String
)
