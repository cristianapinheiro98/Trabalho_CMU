package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "shelters")
data class Shelter(
    @PrimaryKey
    val id: String, // Firebase Auth UID - usado diretamente como PK
    val name: String,
    val address: String,
    val phone: String,
    val email: String
)
