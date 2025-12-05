package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.ipp.estg.trabalho_cmu.data.models.enums.AnimalStatus

@Entity(
    tableName = "animals",
    foreignKeys = [
        ForeignKey(
            entity = Shelter::class,
            parentColumns = ["id"],
            childColumns = ["shelterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shelterId"])]
)
data class Animal(
    @PrimaryKey
    val id: String, // Firebase document ID - usado diretamente como PK
    val name: String,
    val breed: String,
    val species: String,
    val size: String,
    val birthDate: Long,
    val imageUrls: List<String>,
    val description: String,
    val shelterId: String, // Foreign Key -> Shelter.id
    val status: AnimalStatus = AnimalStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis()
)
