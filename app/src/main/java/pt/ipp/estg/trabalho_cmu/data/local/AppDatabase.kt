package pt.ipp.estg.trabalho_cmu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pt.ipp.estg.trabalho_cmu.data.local.dao.ActivityDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter

@Database(
    // Adiciona aqui todas as outras entities
    entities = [
        Ownership::class,
        Activity::class,
        Animal::class,
        Shelter::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Declara todos os DAOs
    abstract fun ownershipDao(): OwnershipDao
    abstract fun activityDao(): ActivityDao
    abstract fun animalDao(): AnimalDao
    abstract fun shelterDao(): ShelterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_adoption_database"  // Nome da tua base de dados
                )
                    .fallbackToDestructiveMigration(false)  // Durante desenvolvimento
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}