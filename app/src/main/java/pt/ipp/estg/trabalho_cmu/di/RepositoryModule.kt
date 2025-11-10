package pt.ipp.estg.trabalho_cmu.di

import com.google.android.datatransport.runtime.dagger.Provides
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
import pt.ipp.estg.trabalho_cmu.data.local.dao.ActivityDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.repository.ActivityRepository
import pt.ipp.estg.trabalho_cmu.data.repository.OwnershipRepository
import pt.ipp.estg.trabalho_cmu.data.repository.AnimalRepository
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
object RepositoryModule {

    //adicionar repositórios aqui
    @Provides
    @Singleton
    fun provideOwnershipRepository(
        ownershipDao: OwnershipDao
    ): OwnershipRepository {
        return OwnershipRepository(ownershipDao)
    }

    @Provides
    @Singleton
    fun provideActivityRepository(
        activityDao: ActivityDao
    ): ActivityRepository {
        return ActivityRepository(activityDao)
    }



}