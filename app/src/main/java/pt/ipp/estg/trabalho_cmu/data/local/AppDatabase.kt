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
import pt.ipp.estg.trabalho_cmu.data.local.entities.*

/**
 * Main Room database for the application.
 *
 * This class defines:
 * - all Room entities used in local storage,
 * - DAOs to access those entities,
 * - database versioning and migrations,
 * - a singleton instance provider.
 *
 * The database uses TypeConverters to support non-primitive fields.
 *
 * @version 14 Current database schema version.
 */
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
    version = 17,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for ownership/adoption requests. */
    abstract fun ownershipDao(): OwnershipDao

    /** DAO for activity logs/actions. */
    abstract fun activityDao(): ActivityDao

    /** DAO for animals. */
    abstract fun animalDao(): AnimalDao

    /** DAO for shelters. */
    abstract fun shelterDao(): ShelterDao

    /** DAO for users. */
    abstract fun userDao(): UserDao

    /** DAO for veterinarians. */
    abstract fun veterinarianDao(): VeterinarianDao

    /** DAO for favorite animals. */
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Example migration from version 1 → 2.
         * Adds Firebase UID columns to users and shelters.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN firebaseUid TEXT")
                db.execSQL("ALTER TABLE shelters ADD COLUMN firebaseUid TEXT")
            }
        }

        /**
         * Example migration from version 2 → 3.
         * Adds indexes for faster lookup of foreign key relations.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX index_animals_shelterId ON animals(shelterId)")
                db.execSQL("CREATE INDEX index_OwnershipRequests_userId ON OwnershipRequests(userId)")
                db.execSQL("CREATE INDEX index_OwnershipRequests_animalId ON OwnershipRequests(animalId)")
                db.execSQL("CREATE INDEX index_OwnershipRequests_shelterId ON OwnershipRequests(shelterId)")
            }
        }

        /**
         * Returns a singleton instance of the database.
         *
         * Uses fallbackToDestructiveMigration() to automatically recreate the database
         * when a schema mismatch occurs—erasing local data but preventing crashes.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_adoption_db"
                )
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    // Currently disabled to avoid schema conflicts
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
