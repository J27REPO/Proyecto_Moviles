package es.uniovi.recetasasturianas.ui.detail

import android.app.Application
import androidx.lifecycle.*
import es.uniovi.recetasasturianas.data.model.Recipe
import es.uniovi.recetasasturianas.data.repository.RecipeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de receta.
 *
 * Gestiona:
 * - Carga de una receta específica
 * - Estado de favorito
 * - Alternar favorito
 */
class RecipeDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository.getRepository(application)

    // ID de la receta actual
    private val _recipeId = MutableLiveData<Int>()

    // Receta observada
    val recipe: LiveData<Recipe?> = _recipeId.switchMap { id ->
        repository.getRecipeById(id)
    }

    // Estado de favorito
    val isFavorite: LiveData<Boolean> = _recipeId.switchMap { id ->
        repository.isFavorite(id)
    }

    // Evento para mostrar mensaje de favorito
    private val _favoriteMessage = MutableLiveData<String?>()
    val favoriteMessage: LiveData<String?> = _favoriteMessage

    /**
     * Establece el ID de la receta a mostrar.
     */
    fun setRecipeId(id: Int) {
        _recipeId.value = id
    }

    /**
     * Alterna el estado de favorito de la receta actual.
     */
    fun toggleFavorite() {
        val id = _recipeId.value ?: return
        viewModelScope.launch {
            val isNowFavorite = repository.toggleFavorite(id)
            // El mensaje se establecerá desde el Fragment con recursos localizados
            _favoriteMessage.value = if (isNowFavorite) "added" else "removed"
        }
    }

    /**
     * Limpia el mensaje de favorito.
     */
    fun clearFavoriteMessage() {
        _favoriteMessage.value = null
    }
}
