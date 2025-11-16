package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shelters",
    indices = [Index(value = ["firebaseUid"], unique = true)]
)
data class Shelter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firebaseUid: String,
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val password: String
)
