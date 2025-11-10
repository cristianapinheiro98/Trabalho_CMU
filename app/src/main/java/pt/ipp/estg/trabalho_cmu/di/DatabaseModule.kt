package pt.ipp.estg.trabalho_cmu.di

import android.content.Context
import com.google.android.datatransport.runtime.dagger.Module
import com.google.android.datatransport.runtime.dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
import pt.ipp.estg.trabalho_cmu.data.local.AppDatabase
import pt.ipp.estg.trabalho_cmu.data.local.dao.ActivityDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.AnimalDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.OwnershipDao
import pt.ipp.estg.trabalho_cmu.data.local.dao.UserDao
import javax.inject.Singleton

@Module
//@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /*@Provides
    @Singleton
    fun provideDatabase(
        //@ApplicationContext context: Context
    ): AppDatabase {
        //return AppDatabase.getDatabase(context)
    }*/

    //Adicionar os daos aqui
    @Provides
    fun provideOwnershipDao(database: AppDatabase): OwnershipDao {
        return database.ownershipDao()
    }

    @Provides
    fun provideActivityDao(database: AppDatabase): ActivityDao {
        return database.activityDao()
    }

    @Provides
    fun provideAnimalDao(database: AppDatabase): AnimalDao {
        return database.animalDao()
    }

    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }


}