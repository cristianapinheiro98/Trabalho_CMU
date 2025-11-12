package pt.ipp.estg.trabalho_cmu.di

import android.content.Context
import androidx.room.Room
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.dao.ActivityDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao

object DatabaseModule {

    @Volatile private var databaseInstance: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tailwagger_db"
            )
                .fallbackToDestructiveMigration()
                .build()
            databaseInstance = instance
            instance
        }
    }

    fun provideAnimalDao(context: Context) = provideDatabase(context).animalDao()
    fun provideOwnershipDao(context: Context) = provideDatabase(context).ownershipDao()
    fun provideActivityDao(context: Context) = provideDatabase(context).activityDao()
    fun provideUserDao(context: Context) = provideDatabase(context).userDao()
}


