package es.uniovi.recetasasturianas.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import es.uniovi.recetasasturianas.data.model.Favorite
import es.uniovi.recetasasturianas.data.model.Recipe

/**
 * DAO para operaciones de base de datos sobre la tabla de favoritos.
 */
@Dao
interface FavoriteDao {

    /**
     * Obtiene todas las recetas favoritas con sus datos completos.
     * JOIN con la tabla de recetas para obtener toda la información.
     */
    @Transaction
    @Query("""
        SELECT r.* FROM recipes r
        INNER JOIN favorites f ON r.id = f.recipeId
        ORDER BY f.savedAt DESC
    """)
    fun getAllFavorites(): LiveData<List<Recipe>>

    /**
     * Verifica si una receta está en favoritos.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE recipeId = :recipeId)")
    fun isFavorite(recipeId: Int): LiveData<Boolean>

    /**
     * Verifica si una receta está en favoritos (suspend).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE recipeId = :recipeId)")
    suspend fun isFavoriteSync(recipeId: Int): Boolean

    /**
     * Añade una receta a favoritos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    /**
     * Elimina una receta de favoritos.
     */
    @Query("DELETE FROM favorites WHERE recipeId = :recipeId")
    suspend fun removeFavorite(recipeId: Int)

    /**
     * Obtiene el timestamp de cuando se guardó como favorito.
     */
    @Query("SELECT savedAt FROM favorites WHERE recipeId = :recipeId")
    suspend fun getSavedAt(recipeId: Int): Long?
}
