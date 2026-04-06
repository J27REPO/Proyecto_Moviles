package es.uniovi.recetasasturianas.data.remote

import es.uniovi.recetasasturianas.data.remote.dto.RecipeResponse
import retrofit2.http.GET

/**
 * Interfaz Retrofit para la API de recetas asturianas.
 */
interface RecipeApiService {

    /**
     * Obtiene todas las recetas desde la API de la universidad.
     */
    @GET("RecetasCocina.json")
    suspend fun getRecipes(): RecipeResponse
}
