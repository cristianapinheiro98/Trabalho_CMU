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

/**
 * The main database class for the application, built on Android Room.
 *
 * This abstract class extends [RoomDatabase] and serves as the central access point
 * to the persisted data. It defines the list of entities contained within the
 * database and provides abstract methods to get the Data Access Objects (DAOs).
 *
 * The database uses the Singleton pattern to ensure that only one instance
 * exists throughout the application's lifecycle.
 *
 * @property version The current version of the database schema. This must be incremented
 *           when the schema is modified.
 * @property entities The list of all data classes annotated with @Entity that represent
 *           the tables in this database.
 * @property exportSchema If set to true, Room exports the database schema into a JSON file.
 *           It's set to false here but recommended for production apps.
 */
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

    /**
     * Provides an instance of [OwnershipDao] to interact with the OwnershipRequests table.
     * @return The Data Access Object for ownership entities.
     */
    abstract fun ownershipDao(): OwnershipDao

    /**
     * Provides an instance of [ActivityDao] to interact with the activities table.
     * @return The Data Access Object for activity entities.
     */
    abstract fun activityDao(): ActivityDao

    /**
     * Provides an instance of [AnimalDao] to interact with the animals table.
     * @return The Data Access Object for animal entities.
     */
    abstract fun animalDao(): AnimalDao

    /**
     * Provides an instance of [ShelterDao] to interact with the shelters table.
     * @return The Data Access Object for shelter entities.
     */
    abstract fun shelterDao(): ShelterDao

    /**
     * Provides an instance of [UserDao] to interact with the users table.
     * @return The Data Access Object for user entities.
     */
    abstract fun userDao(): UserDao

    /**
     * A companion object to manage the singleton instance of the [AppDatabase].
     */
    companion object {

        /**
         * The volatile instance of the database, ensuring that it is always up-to-date
         * and the same for all execution threads.
         */
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

        /**
         * Gets the singleton instance of the [AppDatabase].
         *
         * If the instance is not null, it returns it. Otherwise, it creates the database
         * in a thread-safe way. This method should be used to get a reference to the database.
         *
         * @param context The application context.
         * @return The singleton [AppDatabase] instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_adoption_db"
                )
                    //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    //ISTO DEITA ABAIXO A BD TODA
                    //.fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}