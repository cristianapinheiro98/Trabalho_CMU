package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String, // Firebase Auth UID - usado diretamente como PK
    val name: String,
    val address: String,
    val email: String,
    val phone: String
)
