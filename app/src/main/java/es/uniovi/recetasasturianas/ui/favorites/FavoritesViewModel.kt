package es.uniovi.recetasasturianas.ui.favorites

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import es.uniovi.recetasasturianas.data.model.Recipe
import es.uniovi.recetasasturianas.data.repository.RecipeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de favoritos.
 *
 * Gestiona:
 * - Lista de recetas favoritas
 * - Eliminación de favoritos
 * - Aplicación de preferencias
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository.getRepository(application)
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    // Lista de recetas favoritas (con filtro de preferencias)
    val favorites: LiveData<List<Recipe>> = repository.getFavorites().map { list ->
        val hideNoTime = prefs.getBoolean("hide_no_time", false)
        if (hideNoTime) list.filter { it.timeMinutes != null } else list
    }

    // Evento para mostrar mensaje
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    /**
     * Elimina una receta de favoritos.
     */
    fun removeFavorite(recipeId: Int) {
        viewModelScope.launch {
            repository.removeFavorite(recipeId)
            _message.value = "removed"
        }
    }

    /**
     * Limpia el mensaje.
     */
    fun clearMessage() {
        _message.value = null
    }
}
