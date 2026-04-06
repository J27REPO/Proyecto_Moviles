package es.uniovi.recetasasturianas.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Entidad Room que representa una receta asturiana.
 *
 * @property id Identificador único de la receta
 * @property name Nombre de la receta
 * @property restaurant Nombre del restaurante autor (sin el prefijo "Por ")
 * @property preparationHtml Instrucciones de preparación en formato HTML
 * @property ingredientsHtml Lista de ingredientes en formato HTML
 * @property imageUrl URL completa de la imagen de la receta
 * @property restaurantUrl URL del restaurante (para el WebView)
 * @property timeMinutes Tiempo de preparación en minutos (null si no disponible)
 * @property tipsHtml Trucos y consejos en formato HTML (null si no disponible)
 * @property notesHtml Notas adicionales en formato HTML (null si no disponible)
 * @property cachedAt Timestamp de cuando se cacheó la receta
 */
@Entity(tableName = "recipes")
@Parcelize
data class Recipe(
    @PrimaryKey val id: Int,
    val name: String,
    val restaurant: String,
    val preparationHtml: String,
    val ingredientsHtml: String,
    val imageUrl: String?,
    val restaurantUrl: String?,
    val timeMinutes: Int?,
    val tipsHtml: String?,
    val notesHtml: String?,
    val cachedAt: Long
) : Parcelable
