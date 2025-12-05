package pt.ipp.estg.trabalho_cmu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pt.ipp.estg.trabalho_cmu.data.local.dao.ActivityDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.FavoriteDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.VeterinarianDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.local.entities.Veterinarian

@Database(
    entities = [
        Ownership::class,
        Activity::class,
        Animal::class,
        Shelter::class,
        User::class,
        Veterinarian::class,
        Favorite::class
    ],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ownershipDao(): OwnershipDao
    abstract fun activityDao(): ActivityDao
    abstract fun animalDao(): AnimalDao
    abstract fun shelterDao(): ShelterDao
    abstract fun userDao(): UserDao
    abstract fun veterinarianDao(): VeterinarianDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        //migration example
       /* private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN firebaseUid TEXT")
                db.execSQL("ALTER TABLE shelters ADD COLUMN firebaseUid TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX index_animals_shelterId ON animals(shelterId)")
                db.execSQL("CREATE INDEX index_OwnershipRequests_userId ON OwnershipRequests(userId)")
                db.execSQL("CREATE INDEX index_OwnershipRequests_animalId ON OwnershipRequests(animalId)")
                db.execSQL("CREATE INDEX index_OwnershipRequests_shelterId ON OwnershipRequests(shelterId)")
            }
        }*/

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_adoption_db"
                )
                    //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    //ISTO DEITA ABAIXO A BD TODA
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}