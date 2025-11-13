package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firebaseUid: String? = null,
    val name: String,
    val adress: String,
    val email: String,
    val phone: String,
    val password: String
)
