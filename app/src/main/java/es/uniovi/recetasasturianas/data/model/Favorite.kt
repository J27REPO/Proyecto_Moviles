package es.uniovi.recetasasturianas.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad Room que representa una receta marcada como favorita.
 *
 * @property recipeId ID de la receta favorita (clave primaria y foránea)
 * @property savedAt Timestamp de cuando se guardó como favorito
 */
@Entity(
    tableName = "favorites",
    primaryKeys = ["recipeId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipeId"])]
)
data class Favorite(
    val recipeId: Int,
    val savedAt: Long = System.currentTimeMillis()
)
