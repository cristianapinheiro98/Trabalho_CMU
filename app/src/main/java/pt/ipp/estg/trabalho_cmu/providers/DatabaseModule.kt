package pt.ipp.estg.trabalho_cmu.providers

import android.app.Application
import android.content.Context
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.repository.*

/**
 * DatabaseModule - Dependency Injection Provider
 *
 * Fornece instâncias de DAOs e Repositories para a aplicação
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
            userDao = provideUserDao(application),
            shelterDao = provideShelterDao(application),
            application = application
        )
    }

    fun provideAnimalRepository(application: Application): AnimalRepository {
        return AnimalRepository(
            animalDao = provideAnimalDao(application),
            application = application
        )
    }

    fun provideOwnershipRepository(application: Application): OwnershipRepository {
        return OwnershipRepository(
            ownershipDao = provideOwnershipDao(application),
            application = application
        )
    }

    fun provideActivityRepository(application: Application): ActivityRepository {
        return ActivityRepository(
            activityDao = provideActivityDao(application),
            application = application
        )
    }

    fun provideFavoriteRepository(application: Application): FavoriteRepository {
        return FavoriteRepository(
            favoriteDao = provideFavoriteDao(application),
            application = application
        )
    }


    fun provideUserRepository(application: Application): UserRepository {
        return UserRepository(
            userDao = provideUserDao(application)
        )
    }

    fun provideShelterRepository(application: Application): ShelterRepository {
        return ShelterRepository(
            shelterDao = provideShelterDao(application)
        )
    }

    fun provideVeterinarianRepository(application: Application): VeterinarianRepository {
        return VeterinarianRepository(
            veterinarianDao = provideVeterinarianDao(application)
        )
    }
}
