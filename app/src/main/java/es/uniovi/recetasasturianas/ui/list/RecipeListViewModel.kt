package es.uniovi.recetasasturianas.ui.list

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import es.uniovi.recetasasturianas.R
import es.uniovi.recetasasturianas.data.model.Recipe
import es.uniovi.recetasasturianas.data.repository.RecipeRepository
import es.uniovi.recetasasturianas.util.NetworkUtils
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de lista de recetas.
 *
 * Gestiona:
 * - Carga y caché de recetas
 * - Búsqueda por nombre/restaurante/ingredientes
 * - Estado de carga y errores
 * - Aplicación de preferencias (orden, ocultar sin tiempo)
 * - Detección de conectividad
 */
class RecipeListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecipeRepository.getRepository(application)
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    private val context: Context = application.applicationContext

    // Estado de la búsqueda actual
    private val _searchQuery = MutableLiveData<String>("")

    // Estado del filtro de tiempo (persistido)
    private val _timeFilter = MutableLiveData<TimeFilter>(
        TimeFilter.valueOf(prefs.getString("last_filter", TimeFilter.ALL.name) ?: TimeFilter.ALL.name)
    )
    val timeFilter: LiveData<TimeFilter> = _timeFilter

    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Estado de error
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Estado de conectividad
    private val _isOffline = MutableLiveData<Boolean>()
    val isOffline: LiveData<Boolean> = _isOffline

    // Recetas observadas (cambian según búsqueda y preferencias)
    val recipes: LiveData<List<Recipe>> = _searchQuery.switchMap { query ->
        _timeFilter.switchMap { filter ->
            android.util.Log.d("RecipeListViewModel", "SwitchMap disparado con query: '$query' y filtro: $filter")
            val hideNoTime = prefs.getBoolean("hide_no_time", false)
            val sortBy = prefs.getString("default_sort", "name") ?: "name"

            val source = if (query.isNullOrBlank()) {
                // Aplicar ordenación inicial según preferencia
                when (sortBy) {
                    "restaurant" -> repository.getAllRecipesByRestaurant()
                    else -> repository.getAllRecipes()
                }
            } else {
                repository.searchRecipes(query)
            }

            // Aplicar filtros y ordenación final
            source.map { list ->
                android.util.Log.d("RecipeListViewModel", "Dataset recibido: ${list.size} recetas")
                
                // 1. Filtro 'Ocultar sin tiempo' (Settings)
                var filtered = if (hideNoTime) list.filter { it.timeMinutes != null } else list
                
                // 2. Filtro de tiempo (UI Chips)
                filtered = when (filter) {
                    TimeFilter.ALL -> filtered
                    TimeFilter.QUICK -> filtered.filter { it.timeMinutes != null && it.timeMinutes <= 30 }
                    TimeFilter.MEDIUM -> filtered.filter { it.timeMinutes != null && it.timeMinutes in 31..60 }
                    TimeFilter.LONG -> filtered.filter { it.timeMinutes != null && it.timeMinutes > 60 }
                }

                // 3. Ordenación final según preferencia
                when (sortBy) {
                    "restaurant" -> filtered.sortedBy { it.restaurant.lowercase() }
                    "time" -> filtered.sortedBy { it.timeMinutes ?: Int.MAX_VALUE }
                    else -> filtered.sortedBy { it.name.lowercase() }
                }
            }
        }
    }

    init {
        android.util.Log.d("RecipeListViewModel", "ViewModel init")
        loadRecipes()
    }

    /**
     * Carga las recetas desde el repositorio.
     * Siempre intenta refrescar si es necesario (el repositorio maneja el fallback a assets si falla API).
     */
    fun loadRecipes() {
        viewModelScope.launch {
            android.util.Log.d("RecipeListViewModel", "loadRecipes() empezando...")
            _isLoading.value = true
            _error.value = null

            // Verificar conectividad para mostrar banner offline
            val hasNetwork = NetworkUtils.isNetworkAvailable(context)
            _isOffline.value = !hasNetwork
            android.util.Log.d("RecipeListViewModel", "Conexión detectada: $hasNetwork")

            // Intentar refrescar (API -> Assets -> Room)
            val result = repository.refreshRecipesIfNeeded()
            
            result.onFailure { exception ->
                android.util.Log.e("RecipeListViewModel", "Refresh falló", exception)
                // Solo mostrar error si no hay recetas después del intento
                if (repository.getAllRecipesSync().isEmpty()) {
                    _error.value = if (!hasNetwork) {
                        context.getString(R.string.error_network)
                    } else {
                        exception.message ?: context.getString(R.string.error_unknown)
                    }
                }
            }
            
            android.util.Log.d("RecipeListViewModel", "loadRecipes() finalizado")
            _isLoading.value = false
        }
    }

    /**
     * Fuerza el refresco de recetas.
     * Muestra error si no hay conectividad.
     */
    fun refresh() {
        viewModelScope.launch {
            // Verificar conectividad
            if (!NetworkUtils.isNetworkAvailable(context)) {
                _isOffline.value = true
                _error.value = context.getString(R.string.error_network)
                return@launch
            }

            _isLoading.value = true
            _error.value = null
            _isOffline.value = false

            val result = repository.forceRefreshRecipes()
            result.onFailure { exception ->
                _error.value = exception.message ?: context.getString(R.string.error_unknown)
            }

            _isLoading.value = false
        }
    }

    /**
     * Aplica un filtro de tiempo y lo persiste.
     */
    fun applyTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
        prefs.edit().putString("last_filter", filter.name).apply()
    }

    /**
     * Realiza una búsqueda por nombre, restaurante o ingredientes.
     * BÚSQUEDA: se ejecuta en la capa de datos (Room SQL).
     */
    fun search(query: String) {
        _searchQuery.value = query
    }

    /**
     * Limpia la búsqueda actual.
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Limpia el error actual.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Fuerza recarga de preferencias (llamar cuando cambien settings).
     */
    fun refreshPreferences() {
        // Forzar re-observación cambiando el query
        val current = _searchQuery.value
        _searchQuery.value = if (current.isNullOrBlank()) " " else current
        if (current.isNullOrBlank()) {
            _searchQuery.value = ""
        }
    }
}
