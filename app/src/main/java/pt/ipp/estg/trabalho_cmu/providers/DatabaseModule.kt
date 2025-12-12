package pt.ipp.estg.trabalho_cmu.providers

import android.app.Application
import android.content.Context
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.repository.*

/**
 * Centralized dependency provider for Room DAOs and repositories.
 *
 * Updated to support repositories that require Application context
 * for localized error strings and Firebase operations.
 */
object DatabaseModule {

    // ========== DATABASE ==========
    fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // ========== DAOs ==========
    fun provideAnimalDao(context: Context) = provideDatabase(context).animalDao()
    fun provideOwnershipDao(context: Context) = provideDatabase(context).ownershipDao()
    fun provideActivityDao(context: Context) = provideDatabase(context).activityDao()
    fun provideUserDao(context: Context) = provideDatabase(context).userDao()
    fun provideShelterDao(context: Context) = provideDatabase(context).shelterDao()
    fun provideVeterinarianDao(context: Context) = provideDatabase(context).veterinarianDao()
    fun provideFavoriteDao(context: Context) = provideDatabase(context).favoriteDao()

    // ========== REPOSITORIES ==========

    fun provideAuthRepository(application: Application): AuthRepository {
        return AuthRepository(
            appContext = application,
            userDao = provideUserDao(application),
            shelterDao = provideShelterDao(application)
        )
    }

    fun provideAnimalRepository(application: Application): AnimalRepository {
        return AnimalRepository(
            appContext = application,
            animalDao = provideAnimalDao(application),
            ownershipDao = provideOwnershipDao(application)
        )
    }

    fun provideOwnershipRepository(application: Application): OwnershipRepository {
        return OwnershipRepository(
            appContext = application,
            ownershipDao = provideOwnershipDao(application)
        )
    }

    fun provideActivityRepository(application: Application): ActivityRepository {
        return ActivityRepository(
            activityDao = provideActivityDao(application),
            animalDao = provideAnimalDao(application)
        )
    }

    fun provideFavoriteRepository(application: Application): FavoriteRepository {
        return FavoriteRepository(
            appContext = application,
            favoriteDao = provideFavoriteDao(application)
        )
    }

    fun provideUserRepository(application: Application): UserRepository {
        return UserRepository(
            appContext = application,
            userDao = provideUserDao(application)
        )
    }

    fun provideShelterRepository(application: Application): ShelterRepository {
        return ShelterRepository(
            shelterDao = provideShelterDao(application),
            animalDao = provideAnimalDao(application)
        )
    }

    fun provideVeterinarianRepository(application: Application): VeterinarianRepository {
        return VeterinarianRepository(
            veterinarianDao = provideVeterinarianDao(application)
        )
    }
}
