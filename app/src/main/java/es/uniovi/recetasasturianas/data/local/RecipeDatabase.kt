package es.uniovi.recetasasturianas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import es.uniovi.recetasasturianas.data.model.Favorite
import es.uniovi.recetasasturianas.data.model.Recipe

/**
 * Base de datos Room única para recetas y favoritos.
 * Unifica ambas tablas para soportar foreign keys.
 */
@Database(
    entities = [Recipe::class, Favorite::class],
    version = 1,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recetas_asturianas_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
