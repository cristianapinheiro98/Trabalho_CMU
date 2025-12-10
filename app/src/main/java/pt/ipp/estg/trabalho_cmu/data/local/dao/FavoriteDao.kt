package pt.ipp.estg.trabalho_cmu.data.local.dao
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE userId = :userId AND animalId = :animalId")
    suspend fun removeFavorite(userId: String, animalId: String)

    @Query("SELECT * FROM favorites WHERE userId = :userId AND animalId = :animalId LIMIT 1")
    suspend fun getFavorite(userId: String, animalId: String): Favorite?

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesByUserLive(userId: String): LiveData<List<Favorite>>

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    suspend fun getFavoritesByUser(userId: String): List<Favorite>

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllFavoritesForUser(userId: String)

    @Transaction
    suspend fun refreshFavoritesForUser(userId: String, favorites: List<Favorite>) {
        deleteAllFavoritesForUser(userId)
        favorites.forEach { insertFavorite(it) }
    }
}
