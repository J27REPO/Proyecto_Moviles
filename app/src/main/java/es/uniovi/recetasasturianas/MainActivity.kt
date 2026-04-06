package es.uniovi.recetasasturianas

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import es.uniovi.recetasasturianas.databinding.ActivityMainBinding
import es.uniovi.recetasasturianas.ui.theme.ThemeManager

/**
 * Activity principal de la aplicación.
 *
 * Contiene:
 * - NavHostFragment para la navegación
 * - BottomNavigationView con 3 tabs: Recetas, Favoritos, Ajustes
 * - Toolbar con título dinámico según el destino
 * - Soporte para maestro-detalle en tablets
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    // Indica si estamos en modo tablet (dos paneles)
    private var isTwoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar tema antes de onCreate
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar como ActionBar
        setSupportActionBar(binding.toolbar!!)

        // Detectar si estamos en modo tablet
        isTwoPane = findViewById<View>(R.id.detail_container) != null

        setupNavigation()
    }

    private fun setupNavigation() {
        // Obtener el NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        // Configurar AppBar con los destinos de nivel superior
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.recipeListFragment,
                R.id.favoritesFragment,
                R.id.settingsFragment
            )
        )

        // Configurar Toolbar con NavController
        binding.toolbar?.setupWithNavController(navController, appBarConfiguration)

        // Configurar BottomNavigationView con NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // En modo tablet, configurar navegación especial
        if (isTwoPane) {
            setupTabletNavigation(navController)
        }
    }

    /**
     * Configura la navegación especial para tablets (maestro-detalle).
     */
    private fun setupTabletNavigation(navController: androidx.navigation.NavController) {
        navController.addOnDestinationChangedListener { _, destination, args ->
            when (destination.id) {
                R.id.recipeDetailFragment -> {
                    val recipeId = args?.getInt("recipeId") ?: return@addOnDestinationChangedListener
                    showDetailInTablet(recipeId)
                    navController.popBackStack(R.id.recipeListFragment, false)
                }
            }
        }
    }

    /**
     * Muestra el detalle de una receta en el panel derecho (solo tablets).
     */
    private fun showDetailInTablet(recipeId: Int) {
        findViewById<View>(R.id.text_placeholder)?.visibility = View.GONE
        
        val detailFragment = es.uniovi.recetasasturianas.ui.detail.RecipeDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("recipeId", recipeId)
            }
        }
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.detail_container, detailFragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
