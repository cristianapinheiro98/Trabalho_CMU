package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shelters")
data class Shelter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firebaseUid: String? = null,
    val name: String,
    val address: String,
    val contact: String,
    val email: String,
    val password: String
)
