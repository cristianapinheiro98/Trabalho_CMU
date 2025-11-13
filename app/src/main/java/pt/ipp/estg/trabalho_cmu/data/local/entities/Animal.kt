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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val breed: String,
    val species: String,
    val size: String,
    val birthDate: String,
    val imageUrls: List<String>,
    val description:String,
    val shelterId: Int,
    val status: AnimalStatus = AnimalStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis()
)
