package pt.ipp.estg.trabalho_cmu.data.local.dao
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import pt.ipp.estg.trabalho_cmu.data.local.entities.Favorite

/**
 * Data Access Object (DAO) for the `favorites` table.
 *
 * Provides CRUD operations for Favorite entities and helper
 * methods to manage a user's full favorites list transactionally.
 */
@Dao
interface FavoriteDao {

    /**
     * Inserts a Favorite into the database.
     *
     * If a Favorite with the same primary key already exists,
     * it will be replaced due to [OnConflictStrategy.REPLACE].
     *
     * @param favorite The Favorite entity to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    /**
     * Deletes the given Favorite from the database.
     *
     * The entity must match an existing row (by primary key)
     * in order to be removed.
     *
     * @param favorite The Favorite entity to be deleted.
     */
    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    /**
     * Deletes a specific favorite by user and animal combination.
     *
     * This is a convenience method to remove a favorite without
     * needing to load the entity first.
     *
     * @param userId ID of the user who owns the favorite.
     * @param animalId ID of the favorited animal.
     */
    @Query("DELETE FROM favorites WHERE userId = :userId AND animalId = :animalId")
    suspend fun removeFavorite(userId: String, animalId: String)

    /**
     * Retrieves a single Favorite for a given user and animal.
     *
     * @param userId ID of the user.
     * @param animalId ID of the animal.
     * @return The matching Favorite entity, or null if none exists.
     */
    @Query("SELECT * FROM favorites WHERE userId = :userId AND animalId = :animalId LIMIT 1")
    suspend fun getFavorite(userId: String, animalId: String): Favorite?

    /**
     * Returns a reactive list of favorites for the given user.
     *
     * The returned [LiveData] will emit updates whenever the
     * underlying table data changes.
     *
     * @param userId ID of the user.
     * @return LiveData emitting the list of Favorite entities.
     */
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesByUserLive(userId: String): LiveData<List<Favorite>>

    /**
     * Returns a one-shot list of favorites for the given user.
     *
     * Unlike [getFavoritesByUserLive], this does not observe changes
     * and simply returns the current snapshot of the data.
     *
     * @param userId ID of the user.
     * @return List of Favorite entities for that user.
     */
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    suspend fun getFavoritesByUser(userId: String): List<Favorite>

    /**
     * Deletes all favorites that belong to a specific user.
     *
     * Typically used when refreshing the entire favorites list
     * from a remote source.
     *
     * @param userId ID of the user whose favorites will be removed.
     */
    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllFavoritesForUser(userId: String)

    /**
     * Replaces all favorites for a user with the provided list.
     *
     * This operation is transactional:
     * - First, all existing favorites for the user are deleted.
     * - Then, each Favorite in [favorites] is inserted.
     *
     * @param userId ID of the user whose favorites are being refreshed.
     * @param favorites New list of favorites that should replace the existing ones.
     */
    @Transaction
    suspend fun refreshFavoritesForUser(userId: String, favorites: List<Favorite>) {
        deleteAllFavoritesForUser(userId)
        favorites.forEach { insertFavorite(it) }
    }
}
