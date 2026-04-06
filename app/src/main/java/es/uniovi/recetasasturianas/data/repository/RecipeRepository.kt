package es.uniovi.recetasasturianas.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import es.uniovi.recetasasturianas.data.local.FavoriteDao
import es.uniovi.recetasasturianas.data.local.RecipeDao
import es.uniovi.recetasasturianas.data.local.RecipeDatabase
import es.uniovi.recetasasturianas.data.model.Favorite
import es.uniovi.recetasasturianas.data.model.Recipe
import es.uniovi.recetasasturianas.data.remote.RetrofitClient
import es.uniovi.recetasasturianas.data.remote.dto.RecipeResponse
import es.uniovi.recetasasturianas.data.remote.dto.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

/**
 * Repositorio que actúa como fuente única de datos.
 * Decide cuándo usar caché (Room), red (Retrofit) o assets locales.
 *
 * Estrategia de carga:
 * 1. Intenta API remota (requiere VPN universidad)
 * 2. Si falla, usa archivo local en assets/recetas.json
 * 3. Cachea en Room para uso offline
 */
class RecipeRepository(private val context: Context) {

    private val database: RecipeDatabase = RecipeDatabase.getDatabase(context)
    private val recipeDao: RecipeDao = database.recipeDao()
    private val favoriteDao: FavoriteDao = database.favoriteDao()
    private val apiService = RetrofitClient.recipeApiService
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()

    // Flag para evitar múltiples refrescos simultáneos
    @Volatile
    private var isRefreshing = false

    /**
     * Obtiene todas las recetas.
     */
    fun getAllRecipes(): LiveData<List<Recipe>> = recipeDao.getAll()

    /**
     * Obtiene recetas ordenadas por restaurante.
     */
    fun getAllRecipesByRestaurant(): LiveData<List<Recipe>> = recipeDao.getAllByRestaurant()

    /**
     * Obtiene recetas filtradas por tiempo máximo.
     */
    fun getRecipesByMaxTime(maxMinutes: Int): LiveData<List<Recipe>> = recipeDao.getByMaxTime(maxMinutes)

    /**
     * Obtiene solo recetas que tienen tiempo de preparación.
     */
    fun getRecipesWithTime(): LiveData<List<Recipe>> = recipeDao.getWithTime()

    /**
     * BÚSQUEDA por nombre, restaurante o ingredientes.
     */
    fun searchRecipes(query: String): LiveData<List<Recipe>> = recipeDao.search(query)

    /**
     * Obtiene una receta por ID.
     */
    fun getRecipeById(id: Int): LiveData<Recipe?> = recipeDao.getById(id)

    /**
     * Obtiene todas las recetas sincrónicamente.
     */
    suspend fun getAllRecipesSync(): List<Recipe> = withContext(Dispatchers.IO) {
        recipeDao.getAllSync()
    }

    /**
     * Refresca las recetas si es necesario.
     * Intenta API primero, si falla usa archivo local.
     */
    suspend fun refreshRecipesIfNeeded(): Result<Boolean> = withContext(Dispatchers.IO) {
        if (isRefreshing) {
            return@withContext Result.success(false)
        }

        try {
            isRefreshing = true

            val cached = recipeDao.getAllSync()
            val cacheAge = if (cached.isNotEmpty()) {
                val oldestTimestamp = cached.minOfOrNull { it.cachedAt } ?: 0L
                System.currentTimeMillis() - oldestTimestamp
            } else {
                Long.MAX_VALUE
            }

            val refreshHours = prefs.getString("refresh_hours", "24")?.toIntOrNull() ?: 24
            val refreshIntervalMs = refreshHours * 60 * 60 * 1000L

            if (cached.isEmpty() || cacheAge > refreshIntervalMs) {
                refreshRecipes()
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error refrescando recetas", e)
            Result.failure(e)
        } finally {
            isRefreshing = false
        }
    }

    /**
     * Fuerza el refresco de recetas.
     */
    suspend fun forceRefreshRecipes(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            refreshRecipes()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error en forceRefresh", e)
            Result.failure(e)
        }
    }

    /**
     * Refresca las recetas desde API o archivo local.
     * 
     * Estrategia:
     * 1. Intenta API remota
     * 2. Si falla (VPN, red, timeout) O devuelve lista vacía, carga desde assets
     */
    private suspend fun refreshRecipes() {
        val cachedAt = System.currentTimeMillis()
        
        // Intentar cargar desde API
        var recipes = try {
            Log.d("RecipeRepository", "Intentando cargar desde API...")
            val response = apiService.getRecipes()
            val articles = response.articles.article
            Log.d("RecipeRepository", "API: ${articles.size} recetas cargadas")
            articles.mapIndexed { index, article ->
                article.toEntity(index, cachedAt)
            }
        } catch (e: Exception) {
            Log.w("RecipeRepository", "API falló: ${e.message}")
            emptyList()
        }

        // Si la API falló o devolvió vacío, cargar desde assets
        if (recipes.isEmpty()) {
            Log.d("RecipeRepository", "Cargando desde assets como fallback...")
            recipes = loadFromAssets(cachedAt)
        }

        if (recipes.isNotEmpty()) {
            recipeDao.deleteAll()
            recipeDao.insertAll(recipes)
            Log.d("RecipeRepository", "Guardadas ${recipes.size} recetas en caché")
        }
    }

    /**
     * Carga recetas desde el archivo local en assets.
     */
    private fun loadFromAssets(cachedAt: Long): List<Recipe> {
        return try {
            val inputStream = context.assets.open("recetas.json")
            val reader = InputStreamReader(inputStream)
            val response = gson.fromJson(reader, RecipeResponse::class.java)
            reader.close()
            inputStream.close()
            
            val articles = response.articles.article
            Log.d("RecipeRepository", "Assets: ${articles.size} recetas cargadas")
            articles.mapIndexed { index, article ->
                article.toEntity(index, cachedAt)
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error cargando desde assets", e)
            emptyList()
        }
    }

    // ==================== FAVORITOS ====================

    fun getFavorites(): LiveData<List<Recipe>> = favoriteDao.getAllFavorites()

    fun isFavorite(recipeId: Int): LiveData<Boolean> = favoriteDao.isFavorite(recipeId)

    suspend fun addFavorite(recipeId: Int) = withContext(Dispatchers.IO) {
        favoriteDao.addFavorite(Favorite(recipeId))
    }

    suspend fun removeFavorite(recipeId: Int) = withContext(Dispatchers.IO) {
        favoriteDao.removeFavorite(recipeId)
    }

    suspend fun toggleFavorite(recipeId: Int): Boolean = withContext(Dispatchers.IO) {
        if (favoriteDao.isFavoriteSync(recipeId)) {
            favoriteDao.removeFavorite(recipeId)
            false
        } else {
            favoriteDao.addFavorite(Favorite(recipeId))
            true
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RecipeRepository? = null

        fun getRepository(context: Context): RecipeRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RecipeRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
