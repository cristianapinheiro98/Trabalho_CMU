package pt.ipp.estg.trabalho_cmu.di

import android.content.Context
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase

object DatabaseModule {

    fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    fun provideAnimalDao(context: Context) = provideDatabase(context).animalDao()
    fun provideOwnershipDao(context: Context) = provideDatabase(context).ownershipDao()
    fun provideActivityDao(context: Context) = provideDatabase(context).activityDao()
    fun provideUserDao(context: Context) = provideDatabase(context).userDao()
    fun provideShelterDao(context: Context) = provideDatabase(context).shelterDao()
}