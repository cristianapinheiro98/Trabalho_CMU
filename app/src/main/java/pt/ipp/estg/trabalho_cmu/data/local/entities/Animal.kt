package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.ipp.estg.trabalho_cmu.data.models.AnimalStatus
import pt.ipp.estg.trabalho_cmu.data.models.OwnershipStatus
import java.util.Date

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
    val shelterId: Int,
    val status: AnimalStatus = AnimalStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis()
)
