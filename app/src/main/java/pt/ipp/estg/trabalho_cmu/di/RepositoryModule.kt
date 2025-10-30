package pt.ipp.estg.trabalho_cmu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pt.ipp.estg.trabalho_cmu.data.local.dao.OnwershipDao
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideOwnershipRepository(
        ownershipDao: OnwershipDao
    ): OwnershipRepository {
        return OwnershipRepository(ownershipDao)
    }


}