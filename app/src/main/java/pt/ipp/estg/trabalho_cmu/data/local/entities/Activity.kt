package pt.ipp.estg.trabalho_cmu.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "activities",
    /*foreignKeys = [
        ForeignKey(
            entity = Animal::class,
            parentColumns = ["id"],
            childColumns = ["animalId"],
            onDelete = ForeignKey.CASCADE // se o animal for apagado, apaga as atividades associadas
        )
    ]*/
)
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: String,
    val animalId: String,

    val pickupDate: String,
    val pickupTime: String,
    val deliveryDate: String,
    val deliveryTime: String,

    val createdAt: Long = System.currentTimeMillis()
)
