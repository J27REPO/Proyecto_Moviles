package es.uniovi.recetasasturianas.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import es.uniovi.recetasasturianas.data.model.Recipe

/**
 * DAO para operaciones de base de datos sobre la tabla de recetas.
 */
@Dao
interface RecipeDao {

    /**
     * Obtiene todas las recetas cacheadas.
     * @return Lista de recetas ordenadas por nombre
     */
    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC")
    fun getAll(): LiveData<List<Recipe>>

    /**
     * Obtiene todas las recetas sincrónicamente (para lógica de caché).
     */
    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllSync(): List<Recipe>

    /**
     * BÚSQUEDA por nombre, restaurante o ingredientes.
     * Se ejecuta en la capa de datos (Room SQL), modifica el dataset.
     * @param query Texto a buscar
     * @return LiveData con las recetas que coinciden
     */
    @Query("""
        SELECT * FROM recipes
        WHERE name LIKE '%' || :query || '%'
           OR restaurant LIKE '%' || :query || '%'
           OR ingredientsHtml LIKE '%' || :query || '%'
        ORDER BY name COLLATE NOCASE ASC
    """)
    fun search(query: String): LiveData<List<Recipe>>

    /**
     * Obtiene una receta por su ID.
     */
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getById(id: Int): LiveData<Recipe?>

    /**
     * Obtiene una receta por su ID de forma síncrona.
     */
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getByIdSync(id: Int): Recipe?

    /**
     * Inserta todas las recetas (reemplaza si existe).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    /**
     * Inserta una sola receta.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe)

    /**
     * Elimina todas las recetas (para refrescar caché).
     */
    @Query("DELETE FROM recipes")
    suspend fun deleteAll()

    /**
     * Obtiene recetas ordenadas por restaurante.
     */
    @Query("SELECT * FROM recipes ORDER BY restaurant COLLATE NOCASE ASC, name COLLATE NOCASE ASC")
    fun getAllByRestaurant(): LiveData<List<Recipe>>

    /**
     * Obtiene recetas filtradas por tiempo máximo (para el filtro UI).
     */
    @Query("SELECT * FROM recipes WHERE timeMinutes IS NOT NULL AND timeMinutes <= :maxMinutes ORDER BY name COLLATE NOCASE ASC")
    fun getByMaxTime(maxMinutes: Int): LiveData<List<Recipe>>

    /**
     * Obtiene recetas que tienen tiempo de preparación.
     */
    @Query("SELECT * FROM recipes WHERE timeMinutes IS NOT NULL ORDER BY name COLLATE NOCASE ASC")
    fun getWithTime(): LiveData<List<Recipe>>
}
