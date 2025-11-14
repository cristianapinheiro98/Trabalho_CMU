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
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.ShelterDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import pt.ipp.estg.trabalho_cmu.data.local.entities.Ownership
import pt.ipp.estg.trabalho_cmu.data.local.entities.Activity
import pt.ipp.estg.trabalho_cmu.data.local.entities.Animal
import pt.ipp.estg.trabalho_cmu.data.local.entities.Shelter
import pt.ipp.estg.trabalho_cmu.data.local.entities.User
import pt.ipp.estg.trabalho_cmu.data.local.seeds.SeedAnimals
import pt.ipp.estg.trabalho_cmu.data.local.seeds.SeedShelters
import pt.ipp.estg.trabalho_cmu.data.local.seeds.SeedUsers

@Database(
    entities = [
        Ownership::class,
        Activity::class,
        Animal::class,
        Shelter::class,
        User::class
    ],
    version = 5, // Increase value when making changes!!!
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ownershipDao(): OwnershipDao
    abstract fun activityDao(): ActivityDao
    abstract fun animalDao(): AnimalDao
    abstract fun shelterDao(): ShelterDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
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
        }

        private fun ioThread(f: () -> Unit) {
            Thread(f).start()
        }
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_adoption_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            ioThread {
                                val daoUser = INSTANCE?.userDao()
                                val daoShelter = INSTANCE?.shelterDao()
                                val daoAnimal = INSTANCE?.animalDao()

                                println("SEED: Inserindo shelters...")
                                daoShelter?.insertAllSync(SeedShelters.shelters)

                                println("SEED: Inserindo users...")
                                daoUser?.insertAllSync(SeedUsers.users)

                                println("SEED: Inserindo animals...")
                                daoAnimal?.insertAllSync(SeedAnimals.animals)

                                println("SEED concluído com sucesso!")
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}